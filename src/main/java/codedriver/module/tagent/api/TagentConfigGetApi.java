package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.exception.TagentActionNotFoundEcexption;
import codedriver.framework.tagent.exception.TagentIdNotFoundException;
import codedriver.framework.tagent.tagenthandler.core.ITagentHandler;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerFactory;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentConfigGetApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(TagentConfigGetApi.class);

    @Resource
    TagentMapper tagentMapper;

    @Resource
    RunnerMapper runnerMapper;


    @Override
    public String getName() {
        return "Tagent配置获取接口";
    }

    @Override
    public String getToken() {
        return "tagent/exec/config/get";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "tagentId", type = ApiParamType.LONG, isRequired = true, desc = "tagent id")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TagentMessageVo message = JSONObject.toJavaObject(paramObj, TagentMessageVo.class);
        JSONObject result = null;
        try {
            TagentVo tagent = tagentMapper.getTagentById(message.getTagentId());
            if (tagent == null) {
                throw new TagentIdNotFoundException(tagent.getId());
            }
            RunnerVo runner = runnerMapper.getRunnerById(tagent.getRunnerId());
                ITagentHandler tagentHandler = TagentHandlerFactory.getInstance(TagentAction.GETCONFIG.getValue());
            if (tagentHandler == null) {
                throw new TagentActionNotFoundEcexption(TagentAction.GETCONFIG.getValue());
            } else {
                result = tagentHandler.execTagentCmd(message, tagent, runner);
            }
        } catch (Exception e) {
            logger.error("操作失败", e);
        }
        return result;
    }


}