package codedriver.module.tagent.api.runner;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class RunnerGroupSearchApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "Tagent代理组查询接口";
    }

    @Override
    public String getToken() {
        return "tagent/runnergroup/search";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "获取tagent代理组信息")
    @Input({
            @Param(name = "keyword",type = ApiParamType.STRING,desc = "关键词")
    })
    @Output({
            @Param(name = "tagentRunnerGroupInformation",explode = RunnerGroupVo[].class,desc = "所有tagent代理组")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RunnerGroupVo groupVo =JSONObject.toJavaObject(paramObj,RunnerGroupVo.class);
//        int rowNum = tagentMapper.searchRunnerGroupCount();
        return tagentMapper.searchRunnerGroupInformation(groupVo);
    }


}
