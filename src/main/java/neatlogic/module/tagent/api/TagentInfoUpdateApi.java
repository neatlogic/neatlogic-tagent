/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tagent.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.cmdb.crossover.IResourceAccountCrossoverMapper;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountIpVo;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountProtocolVo;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountBaseVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.exception.TagentNotFoundException;
import neatlogic.framework.tagent.service.TagentService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class TagentInfoUpdateApi extends PublicApiComponentBase {


    private Logger logger = LoggerFactory.getLogger(TagentInfoUpdateApi.class);

    @Resource
    private TagentService tagentService;

    @Resource
    private TagentMapper tagentMapper;

    @Autowired
    private RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "更新tagent信息";
    }

    @Override
    public String getToken() {
        return "tagent/info/update";
    }

    @Override
    public String getConfig() {
        return null;
    }

    /**
     * 1、根据tagent ip和port 绑定runner id
     * 2、更新tagent信息（包括更新os信息，如果不存在os则insert后再绑定osId）
     * 3、当 tagent ip 地址变化(切换网卡)时， 更新 agent ip
     * 4、当组信息与cache不一致时，更新cache
     */
    @Input({@Param(name = "agentId", type = ApiParamType.LONG, desc = "tagentId"),
            @Param(name = "pcpu", type = ApiParamType.STRING, desc = "cpu"),
            @Param(name = "ip", type = ApiParamType.STRING, desc = "ip"),
            @Param(name = "mem", type = ApiParamType.STRING, desc = "内存"),
            @Param(name = "runnerId", type = ApiParamType.LONG, desc = "runner Id"),
            @Param(name = "port", type = ApiParamType.INTEGER, desc = "端口"),
            @Param(name = "status", type = ApiParamType.STRING, desc = "状态"),
            @Param(name = "version", type = ApiParamType.STRING, desc = "版本"),
            @Param(name = "proxyGroupId", type = ApiParamType.LONG, desc = "runner组Id,用于对比组信息是否有更新"),
            @Param(name = "proxyGroup", type = ApiParamType.STRING, desc = "runner组信息ip:port,多个用逗号隔开，用于对比组信息是否有更新"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "消息类型(monitor)"),
            @Param(name = "needUpdateTagentIp", type = ApiParamType.STRING, desc = "是否需要更新tagent的 包含ip（ipString）"),
            @Param(name = "ipString", type = ApiParamType.STRING, desc = "包含ip"),
    })
    @Output({})
    @Description(desc = "tagent信息更新接口,用于tagent<->runner心跳更新tagent信息")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String message = "";
        String localGroupInfo = "";
        boolean updateStatus = true;
        boolean needUpdateGroup = true;
        JSONObject result = new JSONObject();
        long id = paramObj.getLong("agentId");
        try {
            TagentVo tagent = new TagentVo(paramObj);
            Long tagentId = tagentMapper.getTagentIdByTagentIpAndPort(tagent.getIp(), tagent.getPort());
            if (tagentId == null) {
                throw new TagentNotFoundException(tagent.getIp(), tagent.getPort());
            }
            // 1、根据tagent runner ip和port 绑定runner id
            if (StringUtils.isNotBlank(tagent.getRunnerIp())) {
                // port允许为空，兼容tagent老版本没有端口信息
                RunnerVo runnerVo = runnerMapper.getRunnerByNettyIpAndNettyPort(tagent.getRunnerIp(), tagent.getRunnerPort());
                if (runnerVo != null) {
                    tagent.setRunnerId(runnerVo.getId());
                }
            }
            //2、更新tagent信息（包括更新os信息，如果不存在os则insert后再绑定osId、osbitId）
            tagentService.updateTagentById(tagent);

            //3、当 tagent ip 地址变化(切换网卡)时， 更新 agent ip和账号
            updateTagentIpAndAccount(paramObj, tagent);

            //4、 当组信息与cache不一致时，更新cache
            Long runnerGroupId = paramObj.getLong("proxyGroupId");
            String remoteGroupInfo = paramObj.getString("proxyGroup");
            List<RunnerVo> runnerList = runnerMapper.getRunnerListByGroupId(runnerGroupId);// 此语句有L2 cache，5分钟失效
            if (runnerList != null && runnerList.size() > 0) {
                localGroupInfo = runnerList.stream().map(e -> e.getHost() + ":" + e.getNettyPort()).collect(Collectors.joining(","));
            }
            if (remoteGroupInfo.equals(localGroupInfo)) {
                needUpdateGroup = false;
            }
        } catch (Exception e) {
            updateStatus = false;
            message = e.getMessage();
            logger.error(e.getMessage(), e);
        }
        // update runner group info
        if (needUpdateGroup) {
            JSONObject groupData = new JSONObject();
            groupData.put("type", "updategroup");
            groupData.put("isNew", "1");
            groupData.put("groupinfo", localGroupInfo);
            result.put("Data", groupData);
        } else {
            result.put("Data", "");
        }
        result.put("Status", updateStatus ? "OK" : "ERROR");
        result.put("Message", updateStatus ? "tagent cpu and memory update succeed" : message);
        return result;
    }

    private void updateTagentIpAndAccount(JSONObject jsonObj, TagentVo tagent) {
        if (Objects.equals(jsonObj.getString("needUpdateTagentIp"), "1")) {
            IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
            AccountBaseVo tagentAccountVo = tagentMapper.getResourceAccountByIpAndPort(tagent.getIp(), tagent.getPort());
            String protocolName;
            if (tagent.getPort() == 3939) {
                protocolName = "tagent";
            } else {
                protocolName = "tagent." + tagent.getPort();
            }
            AccountProtocolVo protocolVo = resourceAccountCrossoverMapper.getAccountProtocolVoByProtocolName(protocolName);

            List<String> oldIpList = tagentMapper.getTagentIpListByTagentIpAndPort(tagent.getIp(), tagent.getPort());
            List<String> newIpStringList = new ArrayList<>();
            if (jsonObj.getString("ipString") != null) {
                newIpStringList = Arrays.asList(jsonObj.getString("ipString").split(","));
            }
            List<String> newIpList = newIpStringList;

            //删除多余的tagent ip和账号
            tagentService.deleteTagentIpList(oldIpList.stream().filter(item -> !newIpList.contains(item)).collect(toList()), tagent);

            if (CollectionUtils.isNotEmpty(newIpList)) {
                List<String> insertTagentIpList = newIpList.stream().filter(item -> !oldIpList.contains(item)).collect(toList());
                //新增tagent ip和账号
                if (CollectionUtils.isNotEmpty(insertTagentIpList)) {
                    tagentMapper.insertTagentIp(tagent.getId(), insertTagentIpList);
                    List<String> sameIpList = tagentMapper.getAccountIpByIpListAndPort(insertTagentIpList, tagent.getPort());
                    if (CollectionUtils.isNotEmpty(sameIpList)) {
                        insertTagentIpList = insertTagentIpList.stream().filter(item -> !sameIpList.contains(item)).collect(toList());
                    }
                    for (String ip : insertTagentIpList) {
//                        AccountVo newAccountVo = new AccountVo(ip + "_" + tagent.getPort() + "_tagent", protocolVo.getId(), protocolVo.getPort(), ip, tagentAccountVo.getPasswordPlain());
                        AccountBaseVo newAccountVo = new AccountBaseVo(ip + "_" + tagent.getPort() + "_tagent", protocolVo.getId(), protocolVo.getPort(), ip, tagentAccountVo.getPasswordPlain());
                        tagentMapper.insertAccount(newAccountVo);
//                        resourceAccountCrossoverMapper.insertAccount(newAccountVo);
                        tagentMapper.insertAccountIp(new AccountIpVo(newAccountVo.getId(), newAccountVo.getIp()));
                    }
                }
            }
        }
    }
}
