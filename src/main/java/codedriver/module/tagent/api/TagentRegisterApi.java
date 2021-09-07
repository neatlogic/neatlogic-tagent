package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentRegisterApi extends PrivateApiComponentBase {
    private final Logger logger = LoggerFactory.getLogger(TagentRegisterApi.class);
    @Resource
    TagentMapper tagentMapper;
    @Resource
    RunnerMapper runnerMapper;

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
            @Param(name = "tagentId", type = ApiParamType.LONG, desc = "tagentId,非必填"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "tagent名称"),
            @Param(name = "ip", type = ApiParamType.STRING, desc = "tagentIP"),
            @Param(name = "port", type = ApiParamType.STRING, desc = "tagent端口"),
            @Param(name = "user", type = ApiParamType.STRING, desc = "安装用户"),
            @Param(name = "version", type = ApiParamType.STRING, desc = "tagent版本"),
            @Param(name = "credential", type = ApiParamType.STRING, desc = "tagent密码"),
            @Param(name = "osType", type = ApiParamType.STRING, desc = "os类型"),
            @Param(name = "osVersion", type = ApiParamType.STRING, desc = "os版本")
    })
    @Output({
            @Param(name = "tbodyList", explode = RunnerGroupVo[].class, desc = "tagent代理组列表")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultJson = new JSONObject();
        boolean status = true;
        JSONObject data = new JSONObject();
        Long tagentId = paramObj.getLong("tagentId");
        paramObj.put("id",tagentId);
        paramObj.remove("tagentId");
        TagentVo tagentVo = JSONObject.toJavaObject(paramObj, TagentVo.class);
        try {
            // !!! 规避直接复制tagent目录的导致id相同的情况，只重新注册断开连接的tagent
            // 如果有id,则根据id更新其他信息，如果没有id,则根据ip + port 确定唯一性后保存
            if(tagentId != null){
                TagentVo tagentOrigin = tagentMapper.getTagentById(tagentId);
                if(tagentOrigin != null && tagentOrigin.getRunnerId() != null){
                    RunnerVo runner = runnerMapper.getRunnerById(tagentOrigin.getRunnerId());
                }

            }
        } catch (Exception e) {
            status = false;
            logger.error("tagent:" + paramObj.toString() + " register failed." + ExceptionUtils.getStackTrace(e), e);
            throw new ApiRuntimeException(e.getMessage());
        }
        resultJson.put("Status", status ? "OK" : "ERROR");
        resultJson.put("Data", status ? data : "");
        return null;
    }
}
