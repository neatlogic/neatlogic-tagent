package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.service.TagentService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class TagentConfigSaveApi extends PrivateApiComponentBase {
    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "Tagent配置保存接口";
    }

    @Override
    public String getToken() {
        return "tagent/exec/config/save";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "tagentId", type = ApiParamType.LONG, isRequired = true, desc = "tagent id"),
            @Param(name = "data", type = ApiParamType.JSONARRAY, isRequired = true, desc = "tagent 配置"),
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TagentMessageVo message = JSONObject.toJavaObject(paramObj, TagentMessageVo.class);
        return tagentService.execTagentCmd(message, TagentAction.SAVE_CONFIG.getValue());
    }
}
