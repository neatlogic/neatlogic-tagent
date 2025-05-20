/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tagent.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.exception.runner.RunnerNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.exception.TagentNotFoundException;
import neatlogic.framework.tagent.service.TagentService;
import neatlogic.module.tagent.common.UpdateTagentInfoThread;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class TagentInfoUpdateApi extends PrivateApiComponentBase {


    private Logger logger = LoggerFactory.getLogger(TagentInfoUpdateApi.class);

    @Resource
    private TagentMapper tagentMapper;

    @Resource
    private RunnerMapper runnerMapper;

    @Resource
    private TagentService tagentService;

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
        try {
            TagentVo tagent = new TagentVo(paramObj);
            TagentVo tagentVo = tagentMapper.getTagentDetailById(tagent.getId());
            TagentVo tagentMGVo = tagentService.getTagentMGById(tagent.getId());

            if (tagentVo == null) {
                //存在历史垃圾数据,则删除
                if (tagentMGVo != null) {
                    tagentService.deleteTagentMGById(tagent.getId());
                }
                throw new TagentNotFoundException(paramObj.getLong("agentId"), tagent.getIp(), tagent.getPort());
            }
            if (tagentMGVo == null) {
                //历史数据,需补充同步到mongodb
                tagentService.updateTagentMGById(tagentVo, true);
            }
            // 1、根据tagent runner ip和port 绑定runner id
            if (StringUtils.isNotBlank(tagent.getRunnerIp())) {
                // port允许为空，兼容tagent老版本没有端口信息
                RunnerVo runnerVo = runnerMapper.getRunnerByNettyIpAndNettyPort(tagent.getRunnerIp(), tagent.getRunnerPort());
                if (runnerVo != null) {
                    tagent.setRunnerId(runnerVo.getId());
                } else {
                    throw new RunnerNotFoundException(tagent.getRunnerIp(), tagent.getRunnerPort());
                }
            }
            //2、更新tagent信息（包括更新os信息，如果不存在os则insert后再绑定osId、osbitId）
            //3、当 tagent ip 地址变化(切换网卡)时， 更新 agent ip和账号
            tagent.setParam(paramObj);
            logger.debug("====TagentUpdateInfo-api:" + JSON.toJSONString(tagent));
            UpdateTagentInfoThread.addUpdateTagent(tagent);
            //4、 当组信息与cache不一致时，更新cache
            Long runnerGroupId = paramObj.getLong("proxyGroupId");
            String remoteGroupInfo = paramObj.getString("proxyGroup");
            List<RunnerVo> runnerList = runnerMapper.getRunnerListByGroupId(runnerGroupId);// 此语句有L2 cache，5分钟失效
            if (CollectionUtils.isNotEmpty(runnerList)) {
                localGroupInfo = runnerList.stream().map(e -> e.getHost() + ":" + e.getNettyPort()).collect(Collectors.joining(","));
            }
            if (remoteGroupInfo.equals(localGroupInfo)) {
                needUpdateGroup = false;
            }
        } catch (Exception e) {
            updateStatus = false;
            message = e.getMessage();
            if (e instanceof ApiRuntimeException) {
                logger.error(e.getMessage());
            } else {
                logger.error(e.getMessage(), e);
            }
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
        result.put("serverId", Config.SCHEDULE_SERVER_ID);
        return result;
    }


}