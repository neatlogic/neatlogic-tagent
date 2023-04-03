package neatlogic.module.tagent.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.enums.TagentAction;
import neatlogic.framework.tagent.exception.TagentActionNotFoundException;
import neatlogic.framework.tagent.exception.TagentIdNotFoundException;
import neatlogic.framework.tagent.tagenthandler.core.ITagentHandler;
import neatlogic.framework.tagent.tagenthandler.core.TagentHandlerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class TagentReLoadApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "重启Tagent";
    }

    @Override
    public String getToken() {
        return "tagent/exec/reload";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "tagentId", type = ApiParamType.LONG, isRequired = true, desc = "tagent id")
    })
    @Description(desc = "重启Tagent接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TagentMessageVo message = JSONObject.toJavaObject(paramObj, TagentMessageVo.class);
        JSONObject result = null;
        TagentVo tagent = tagentMapper.getTagentById(message.getTagentId());
        if (tagent == null) {
            throw new TagentIdNotFoundException(message.getTagentId());
        }
        RunnerVo runner = runnerMapper.getRunnerById(tagent.getRunnerId());
        ITagentHandler tagentHandler = TagentHandlerFactory.getInstance(TagentAction.RELOAD.getValue());
        if (tagentHandler == null) {
            throw new TagentActionNotFoundException(TagentAction.RELOAD.getValue());
        } else {
            result = tagentHandler.execTagentCmd(message, tagent, runner);
        }
        return result;
    }


}
