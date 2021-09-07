package codedriver.module.tagent.api.runner;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentRunnerMapper;
import codedriver.framework.tagent.exception.RunnerIdNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class RunnerDeleteApi extends PrivateApiComponentBase {

    @Resource
    TagentRunnerMapper tagentRunnerMapper;

    @Override
    public String getName() {
        return "runner 删除接口";
    }

    @Override
    public String getToken() {
        return "tagent/runner/delete";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "runner id")
    })
    @Output({
    })
    @Description(desc = "runner 删除接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        if (tagentRunnerMapper.checkRunnerIdIsExist(id) == 0) {
            throw new RunnerIdNotFoundException(id);
        }
        tagentRunnerMapper.deleteRunnerById(id);
        return null;
    }

}
