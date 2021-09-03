package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.autoexec.dto.RunnerGroupVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
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
public class TagentRunnerGroupListApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "获取所有代理组";
    }

    @Override
    public String getToken() {
        return "tagent/runnergroup/list";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "获取所有tagent代理组")
    @Output({
            @Param(name = "tagentRunnerGroup",explode = RunnerGroupVo[].class,desc = "所有tagent代理组")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        return tagentMapper.searchTagentRunnerGroup();
    }


}
