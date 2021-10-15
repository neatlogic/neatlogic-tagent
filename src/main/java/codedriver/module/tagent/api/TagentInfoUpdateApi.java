package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.service.TagentService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class TagentInfoUpdateApi extends PrivateApiComponentBase {


    private Logger logger = LoggerFactory.getLogger(TagentInfoUpdateApi.class);

    @Resource
    private TagentService tagentService;

    @Resource
    private TagentMapper tagentMapper;

    @Autowired
    private RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "tagent心跳信息";
    }

    @Override
    public String getToken() {
        return "tagent/info/update";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "tagentId", type = ApiParamType.LONG, desc = "tagentId"),
            @Param(name = "pcpu", type = ApiParamType.STRING, desc = "cpu"),
            @Param(name = "mem", type = ApiParamType.STRING, desc = "内存"),
            @Param(name = "runnerId", type = ApiParamType.LONG, desc = "runner Id"),
            @Param(name = "runnerGroupId", type = ApiParamType.LONG, desc = "runner组Id,用于对比组信息是否有更新"),
            @Param(name = "runnerGroup", type = ApiParamType.STRING, desc = "runner组信息ip:port,多个用逗号隔开，用于对比组信息是否有更新"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "消息类型(monitor)")})
    @Output({})
    @Description(desc = "Tagent信息更新接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String message = "";
        String localGroupInfo = "";
        boolean updateStatus = true;
        boolean needUpdateGroup = true;
        JSONObject result = new JSONObject();
        long id = paramObj.getLong("tagentId");
        try {

            // 更新 tagent 基础信息
            TagentVo tagent = JSONObject.toJavaObject(paramObj, TagentVo.class);
            if (StringUtils.isNotBlank(tagent.getRunnerIp())) {
                // port允许为空，兼容tagent老版本没有端口信息
                RunnerVo runnerVo = runnerMapper.getRunnerByIpAndPort(tagent.getRunnerIp(), tagent.getRunnerPort());
                if (runnerVo != null) {
                    tagent.setRunnerId(runnerVo.getId());
                }
            }

            tagentService.updateTagentById(tagent);

            // 当 tagent ip 地址变化(切换网卡)时， 更新 agent ip
            updateTagentIp(paramObj, tagent);

            // 当组信息与cache不一致时，更新cache
            Long runnerGroupId = paramObj.getLong("runnerGroupId");
            String remoteGroupInfo = paramObj.getString("runnerGroup");

            // 此语句有L2 cache，5分钟失效
            List<RunnerVo> runnerList = runnerMapper.getRunnerByGroupId(runnerGroupId);
            if (runnerList != null && runnerList.size() > 0) {
                for (RunnerVo runner : runnerList) {
                    if (StringUtils.isNotBlank(localGroupInfo)) {
                        localGroupInfo += ",";
                    }
                    localGroupInfo += runner.getHost() + ":" + runner.getPort();
                }
            }
            if (remoteGroupInfo.equals(localGroupInfo)) {
                needUpdateGroup = false;
            }

        } catch (Exception e) {
            updateStatus = false;
            message = e.getMessage();
            logger.error("update tagent " + id + " failed. ", e);
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
        result.put("Message", updateStatus ? "tagent cpu and memeory update succeed" : message);
        return result;
    }

    private void updateTagentIp(JSONObject jsonObj, TagentVo tagent) {
        if (jsonObj.getString("needUpdateTagentIp").equals("1")) {
            tagentMapper.deleteAllIpByTagentId(tagent.getId());
            String ipString = jsonObj.getString("ipString");
            if (StringUtils.isNotBlank(ipString)) {
                String[] ipArray = ipString.split(",");
                if (CollectionUtils.isNotEmpty(Arrays.asList(ipArray))) {
                    tagentMapper.insertTagentIp(tagent.getId(), Arrays.asList(ipArray));
                }
            }

        }
    }

}