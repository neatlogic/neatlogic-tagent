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

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class TagentUpgradeApi extends PrivateApiComponentBase {

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "升级tagent";
    }

    @Override
    public String getToken() {
        return "tagent/upgrade";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "tagentId", type = ApiParamType.LONG, isRequired = true, desc = "tagent id"),
            @Param(name = "pkgVersion", type = ApiParamType.STRING, isRequired = true, desc = "安装包版本"),
    })
    @Description(desc = "升级tagent接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TagentMessageVo message = JSONObject.toJavaObject(paramObj, TagentMessageVo.class);
        return tagentService.execTagentCmd(message, TagentAction.UPGRADE.getValue());
    }
}
