package neatlogic.module.tagent.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.enums.TagentAction;
import neatlogic.framework.tagent.service.TagentService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentConfigGetApi extends PrivateApiComponentBase {
    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "获取Tagent配置";
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
    @Description(desc = "获取Tagent配置接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject returnJson = new JSONObject();
        TagentMessageVo message = JSONObject.toJavaObject(paramObj, TagentMessageVo.class);
        returnJson.put("configJson", tagentService.execTagentCmd(message, TagentAction.GET_CONFIG.getValue()));
        returnJson.put("requiredFieldList", Arrays.asList("credential", "listen.port", "proxy.registeraddress", "tenant"));
        return returnJson;
    }
}
