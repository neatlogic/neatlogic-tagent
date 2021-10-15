package codedriver.module.tagent.api.runner;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.exception.RunnerGroupIdNotFoundException;
import codedriver.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class RunnerListApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "查找runner组的runner信息";
    }

    @Override
    public String getToken() {
        return "tagent/runnergroup/runner/list";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "runner组id")
    })
    @Output({
            @Param(name = "tbodyList", desc = "runner列表")
    })
    @Description(desc = "查找runner组的runner信息")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        if (runnerMapper.checkRunnerGroupIdIsExist(id) == 0) {
            throw new RunnerGroupIdNotFoundException(id);
        }
        RunnerGroupVo runnerGroupVo = JSONObject.toJavaObject(paramObj, RunnerGroupVo.class);
        int rowNum = runnerMapper.searchRunnerCount(id);
        runnerGroupVo.setRowNum(rowNum);
        return TableResultUtil.getResult(runnerMapper.searchRunner(runnerGroupVo),runnerGroupVo);
    }
}