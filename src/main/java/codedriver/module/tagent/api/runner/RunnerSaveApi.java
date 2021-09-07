package codedriver.module.tagent.api.runner;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.RunnerMapper;
import codedriver.framework.tagent.enums.RunnerAuthType;
import codedriver.framework.tagent.exception.RunnerGroupIdNotFoundException;
import codedriver.framework.tagent.exception.RunnerNameRepeatsException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class RunnerSaveApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "runner 保存接口";
    }

    @Override
    public String getToken() {
        return "tagent/runner/save";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = false, desc = "runner id"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "runner 名"),
            @Param(name = "url", type = ApiParamType.STRING, isRequired = true, desc = "url"),
            @Param(name = "accessKey", type = ApiParamType.STRING, isRequired = true, desc = "runner 授权key"),
            @Param(name = "accessSecret", type = ApiParamType.STRING, isRequired = true, desc = "runner 授权key"),
            @Param(name = "nettyIp", type = ApiParamType.STRING, desc = "NETTY IP"),
            @Param(name = "nettyPort", type = ApiParamType.INTEGER, desc = "NETTY 端口"),
            @Param(name = "groupId", type = ApiParamType.STRING, isRequired = true, desc = "runner组id"),
            @Param(name = "authType", explode = RunnerAuthType[].class, desc = "认证方式"),
            @Param(name = "publicKey", type = ApiParamType.STRING, desc = "ssh公钥"),
            @Param(name = "privateKey", type = ApiParamType.STRING, desc = "ssh私钥")
    })
    @Output({
    })
    @Description(desc = "runner 保存接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RunnerVo runnerVo = JSONObject.toJavaObject(paramObj, RunnerVo.class);
        if (runnerVo.getName() != null) {
            if (runnerMapper.checkRunnerNameIsExist(runnerVo) > 0) {
                throw new RunnerNameRepeatsException(runnerVo.getName());
            }
            if (runnerVo.getId() != null) {
                if (runnerMapper.checkRunnerIdIsExist(runnerVo.getId())==0) {
                    throw new RunnerGroupIdNotFoundException(runnerVo.getId());
                }
                runnerMapper.updateRunner(runnerVo);
            } else {
                if (runnerMapper.checkRunnerIdIsExist(runnerVo.getGroupId())==0) {
                    throw new RunnerGroupIdNotFoundException(runnerVo.getGroupId());
                }
                runnerMapper.insertRunner(runnerVo);
            }
        }
        return null;
    }


}
