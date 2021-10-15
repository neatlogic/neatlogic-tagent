package codedriver.module.tagent.api.runner;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.enums.RunnerAuthType;
import codedriver.framework.tagent.exception.RunnerGroupIdNotFoundException;
import codedriver.framework.tagent.exception.RunnerIdNotFoundException;
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
        return "runner保存接口";
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
            @Param(name = "protocol", type = ApiParamType.ENUM, isRequired = true, rule = "http,https", desc = "协议"),
            @Param(name = "accessKey", type = ApiParamType.STRING, desc = "runner 授权key"),
            @Param(name = "accessSecret", type = ApiParamType.STRING,desc = "runner 授权密码"),
            @Param(name = "host", type = ApiParamType.STRING, desc = "runner ip"),
            @Param(name = "nettyPort", type = ApiParamType.INTEGER, desc = "心跳端口"),
            @Param(name = "port", type = ApiParamType.INTEGER, desc = "命令端口"),
            @Param(name = "groupId", type = ApiParamType.LONG, isRequired = true, desc = "runner组id"),
            @Param(name = "authType", explode = RunnerAuthType[].class, desc = "外部认证"),
    })
    @Output({
    })
    @Description(desc = "runner 保存接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RunnerVo runnerVo = JSONObject.toJavaObject(paramObj, RunnerVo.class);
        Long id = runnerVo.getId();
        if (runnerVo.getName() == null) {
            return null;
        }
        if (id != null) {
            if (runnerMapper.checkRunnerIdIsExist(runnerVo.getId()) == 0) {
                throw new RunnerIdNotFoundException(runnerVo.getId());
            }
            if (runnerMapper.checkRunnerNameIsExist(runnerVo) > 0) {
                throw new RunnerNameRepeatsException(runnerVo.getName());
            }
            runnerMapper.updateRunner(runnerVo);
        }else {
            if (runnerMapper.checkRunnerNameIsExistByName(runnerVo) > 0) {
                throw new RunnerNameRepeatsException(runnerVo.getName());
            }
            if (runnerMapper.checkRunnerGroupIdIsExist(runnerVo.getGroupId()) == 0) {
                throw new RunnerGroupIdNotFoundException(runnerVo.getGroupId());
            }
            runnerMapper.insertRunner(runnerVo);

        }

        return null;
    }


}
