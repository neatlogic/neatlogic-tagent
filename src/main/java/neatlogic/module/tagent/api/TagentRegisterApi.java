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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.IpUtil;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.GroupNetworkVo;
import neatlogic.framework.dto.runner.RunnerGroupVo;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.runner.*;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentOSVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.enums.TagentAction;
import neatlogic.framework.tagent.exception.TagentIpIsEmptyException;
import neatlogic.framework.tagent.exception.TagentMultipleException;
import neatlogic.framework.tagent.exception.TagentPortIsEmptyException;
import neatlogic.framework.tagent.exception.TagentStatusIsSuccessException;
import neatlogic.framework.tagent.register.core.AfterRegisterJobManager;
import neatlogic.framework.tagent.service.TagentService;
import neatlogic.framework.util.HttpRequestUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Transactional
@Service
@OperationType(type = OperationTypeEnum.OPERATE)
public class TagentRegisterApi extends PublicApiComponentBase {
    private final Logger logger = LoggerFactory.getLogger(TagentRegisterApi.class);
    @Resource
    TagentMapper tagentMapper;
    @Resource
    RunnerMapper runnerMapper;

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "注册tagent";
    }

    @Override
    public String getToken() {
        return "tagent/register";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "注册tagent接口")
    @Input({
            @Param(name = "tagentId", type = ApiParamType.NOAUTH, desc = "tagentId,非必填"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "tagent名称"),
            @Param(name = "ip", type = ApiParamType.STRING, desc = "tagentIP"),
            @Param(name = "port", type = ApiParamType.INTEGER, desc = "tagent端口"),
            @Param(name = "user", type = ApiParamType.STRING, desc = "安装用户"),
            @Param(name = "version", type = ApiParamType.STRING, desc = "tagent版本"),
            @Param(name = "credential", type = ApiParamType.STRING, desc = "tagent密码"),
            @Param(name = "osType", type = ApiParamType.STRING, desc = "os类型"),
            @Param(name = "osbit", type = ApiParamType.STRING, desc = "操作系统位数"),
            @Param(name = "osVersion", type = ApiParamType.STRING, desc = "os版本"),
            @Param(name = "pcpu", type = ApiParamType.STRING, desc = "cpu占用"),
            @Param(name = "mem", type = ApiParamType.STRING, desc = "内存占用")

    })
    @Output({
            @Param(name = "Data", type = ApiParamType.JSONOBJECT, desc = "tagent注册结果信息（包括tagentId、runner组id、组内runner id列表）")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultJson = new JSONObject();
        JSONObject data = new JSONObject();
        //agent ip
        String insertTagentIp = paramObj.getString("ip");
        Integer insertTagentPort = paramObj.getInteger("port");
        try {
            if (StringUtils.isBlank(insertTagentIp)) {
                throw new TagentIpIsEmptyException(paramObj);
            }
            if (insertTagentPort == null) {
                throw new TagentPortIsEmptyException(paramObj);
            }

            Long insertTagentId = null;
            String tagentIdString = paramObj.getString("tagentId");
            if (StringUtils.isNotEmpty(tagentIdString)) {
                insertTagentId = Long.valueOf(tagentIdString);
            }
            if (StringUtils.isNotBlank(paramObj.getString("tagentId"))) {
                TagentVo oldTagent = tagentMapper.getTagentById(insertTagentId);
                if (oldTagent != null) {
                    if (Objects.equals(oldTagent.getPort(), insertTagentPort)) {
                        if (StringUtils.equals(oldTagent.getIp(), insertTagentIp)) {
                            //输入ip和主ip相同 =》刷状态，查看tagent状态
                            checkTagentStatus(oldTagent);
                        } else {
                            List<String> oldIpList = tagentMapper.getTagentIpListByTagentId(insertTagentId);
                            if (oldIpList.contains(insertTagentIp)) {
                                //输入ip和副ip相同 =》刷状态，查看tagent状态
                                checkTagentStatus(oldTagent);
                            } else {
                                //ip不相同,根据输入ip和输入port查询tagent
                                insertTagentId = getTagentByIpAndPort(insertTagentIp, insertTagentPort);
                            }
                        }
                    } else {
                        //port不相同,根据输入ip和输入port查询tagent
                        insertTagentId = getTagentByIpAndPort(insertTagentIp, insertTagentPort);
                    }
                } else {
                    //通过id找不到tagent，根据输入ip和输入port查询tagent
                    insertTagentId = getTagentByIpAndPort(insertTagentIp, insertTagentPort);
                }
            } else {
                //无输入id，根据输入ip和输入port查询tagent
                insertTagentId = getTagentByIpAndPort(insertTagentIp, insertTagentPort);
            }
            paramObj.put("tagentId", insertTagentId);
            RunnerGroupVo runnerGroupVo = getRunnerGroupByAgentIp(insertTagentIp);
            TagentVo tagentVo = saveTagent(paramObj, runnerGroupVo);
            //注册后同步信息到资源中心
            AfterRegisterJobManager.executeAll(tagentVo);
            //排序保证tagent获取的runner顺序不变
            List<RunnerVo> runnerList = runnerGroupVo.getRunnerList().stream().sorted(Comparator.comparing(RunnerVo::getId)).collect(Collectors.toList());
            returnData(data, runnerList, tagentVo.getId(), runnerGroupVo.getId());
            resultJson.put("Status", "OK");
            resultJson.put("Data", data);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            resultJson.put("Message", ex.getMessage());
            resultJson.put("Status", "ERROR");
        }
        return resultJson;
    }

    /**
     * 保存tagent
     *
     * @param paramObj      入参
     * @param runnerGroupVo runner组
     * @return tagent
     */
    private TagentVo saveTagent(JSONObject paramObj, RunnerGroupVo runnerGroupVo) {
        Long tagentId = paramObj.getLong("tagentId");
        paramObj.put("id", tagentId);
        paramObj.remove("tagentId");
        TagentVo tagentVo = JSONObject.toJavaObject(paramObj, TagentVo.class);
        if (tagentId == null) {
            tagentVo.setIsFirstCreate(1);
        }
        tagentVo.setRunnerGroupId(runnerGroupVo.getId());

        //保存tagent osType
        if (StringUtils.isNotBlank(tagentVo.getOsType())) {
            String osType = tagentVo.getOsType();
            TagentOSVo os = tagentMapper.getOsByName(osType.toLowerCase());
            if (os != null) {
                tagentVo.setOsId(os.getId());
            } else {
                TagentOSVo newOS = new TagentOSVo(osType);
                tagentMapper.insertOs(newOS);
                tagentVo.setOsId(newOS.getId());
            }
        }

        //保存tagent osbit
        if (StringUtils.isNotBlank(tagentVo.getOsbit())) {
            tagentMapper.insertOsBit(tagentVo.getOsbit());
        }
        tagentService.saveTagentAndAccount(tagentVo);
        return tagentVo;
    }

    /**
     * 根据tagentIp 匹配runnerGroup
     *
     * @param agentIp agentIp
     */
    private RunnerGroupVo getRunnerGroupByAgentIp(String agentIp) {
        RunnerGroupVo runnerGroupVo = null;
        List<GroupNetworkVo> networkList = tagentMapper.getGroupNetworkList();
        //找到agent ip符合网端的runnerGroup,如果存在多个group,则只需要第一个group
        for (int i = 0; i < networkList.size(); i++) {
            GroupNetworkVo groupNetworkVo = networkList.get(i);
            if (i == 0) {
                runnerGroupVo = new RunnerGroupVo();
            }
            if (IpUtil.isBelongSegment(agentIp, groupNetworkVo.getNetworkIp(), groupNetworkVo.getMask())) {
                runnerGroupVo.setId(groupNetworkVo.getGroupId());
                runnerGroupVo.setName(groupNetworkVo.getName());
                List<RunnerVo> runnerList = runnerMapper.getRunnerListByGroupId(runnerGroupVo.getId());
                if (CollectionUtils.isNotEmpty(runnerList)) {
                    runnerGroupVo.setRunnerList(runnerList);
                    break;
                }
            }
        }

        //如果tagent ip 和所有网段都不匹配，则抛异常
        if (runnerGroupVo == null) {
            throw new RunnerGroupIdNotFoundException(agentIp);
        }

        //如果虽然匹配到group，但group不存在runner
        if (CollectionUtils.isEmpty(runnerGroupVo.getRunnerList())) {
            throw new RunnerGroupRunnerListEmptyException(runnerGroupVo);
        }
        return runnerGroupVo;
    }

    /**
     * 兼容现有tagent，组装response数据
     */
    private void returnData(JSONObject data, List<RunnerVo> groupRunnerList, Long saveTagentId, Long runnerGroupId) {
        JSONArray runnerArray = new JSONArray();
        for (RunnerVo runner : groupRunnerList) {
            JSONObject runnerData = new JSONObject();
            runnerData.put("id", runner.getId());
            runnerData.put("ip", runner.getHost());
            runnerData.put("port", runner.getNettyPort());
            runnerArray.add(runnerData);
        }
        data.put("tagentId", saveTagentId);
        //兼容tagent入参 不能改为runner
        /*data.put("proxyId", tagentRunnerVo.getId());
        data.put("proxyIp", tagentRunnerVo.getHost());
        data.put("proxyPort", tagentRunnerVo.getPort());*/
        data.put("proxyGroupId", runnerGroupId);
        data.put("proxyList", runnerArray);
    }

    private Long getTagentByIpAndPort(String insertTagentIp, Integer insertTagentPort) {
        List<TagentVo> oldTagentList = tagentMapper.getTagentByIpOrTagentIpAndPort(insertTagentIp, insertTagentPort);
        if (CollectionUtils.isNotEmpty(oldTagentList)) {
            if (oldTagentList.size() == 1) {
                checkTagentStatus(oldTagentList.get(0));
                return oldTagentList.get(0).getId();
            } else {
                //通过输入ip和输入port找到多个个tagent，注册失败
                throw new TagentMultipleException(oldTagentList);
            }
        } else {
            //新tagent，直接注册
            return null;
        }
    }

    private void checkTagentStatus(TagentVo tagentVo) {
        //刷状态，查看tagent状态
        if (tagentVo.getRunnerId() == null) {
            return;
        }
        RunnerVo runnerVo = runnerMapper.getRunnerById(tagentVo.getRunnerId());
        if (runnerVo == null) {
            throw new RunnerIdNotFoundException(tagentVo.getRunnerId());
        }
        if (StringUtils.isBlank(runnerVo.getUrl())) {
            throw new RunnerUrlIsNullException(runnerVo.getId());
        }
        JSONObject paramJson = new JSONObject();
        paramJson.put("ip", tagentVo.getIp());
        paramJson.put("port", (tagentVo.getPort()).toString());
        paramJson.put("type", TagentAction.STATUS_CHECK.getValue());
        String url = runnerVo.getUrl() + "api/rest/tagent/status/check";
        try {
            HttpRequestUtil requestUtil = HttpRequestUtil.post(url).setConnectTimeout(5000).setReadTimeout(5000).setPayload(paramJson.toJSONString()).setAuthType(AuthenticateType.BUILDIN).sendRequest();
            if (StringUtils.isNotBlank(requestUtil.getError())) {
                return;
            }
            JSONObject resultJson = requestUtil.getResultJson();
            if (resultJson.containsKey("Status") && "OK".equals(resultJson.getString("Status"))) {
                //注册失败，抛异常（已存在活动的tagent，可能是ip冲突造成）
                throw new TagentStatusIsSuccessException();
            } else {
                //未连接状态，使用表id注册
            }
        } catch (JSONException ex) {
            //未连接状态，使用表id注册
        }
    }


    @Override
    public boolean isRaw() {
        return true;
    }
}
