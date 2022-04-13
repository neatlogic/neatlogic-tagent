package codedriver.module.tagent.api;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.IpUtil;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class TagentStatusUpdateApi extends PublicApiComponentBase {

    private final Logger logger = LoggerFactory.getLogger(TagentStatusUpdateApi.class);

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "更新tagent状态";
    }

    @Override
    public String getToken() {
        return "tagent/status/update";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "ip", type = ApiParamType.STRING, desc = "tagentIP"),
            @Param(name = "port", type = ApiParamType.INTEGER, desc = "tagent端口"),
            @Param(name = "status", type = ApiParamType.ENUM, rule = "disconnected,connected", isRequired = true, desc = "tagent状态"),
            @Param(name = "runnerId", type = ApiParamType.STRING, desc = "runner id"),
            @Param(name = "runnerGroupId", type = ApiParamType.LONG, desc = "runner组id"),
    }
    )
    @Output({})
    @Description(desc = "Tagent状态更新接口,用于tagent<->runner心跳更新tagent状态")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject result = new JSONObject();
        boolean status = true;
        try {
            paramObj.put("runnerIp", IpUtil.getIpAddr(UserContext.get().getRequest()));
            TagentVo tagent = JSONObject.toJavaObject(paramObj, TagentVo.class);
            tagent.setId(tagentMapper.getTagentIdByTagentIpAndPort(tagent.getIp(), tagent.getPort()));
            tagentMapper.updateTagent(tagent);
        } catch (Exception e) {
            status = false;
            logger.error("tagent status update failed, " + ExceptionUtils.getStackTrace(e));
        }
        result.put("Status", status ? "OK" : "ERROR");
        result.put("Data", status ? paramObj : "");
        return result;
    }
}
