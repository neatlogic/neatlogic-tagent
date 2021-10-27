/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
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
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentStatusCheckApi extends PrivateApiComponentBase {
    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "tagent状态检查";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public String getToken() {
        return "tagent/exec/status/check";
    }

    @Input({
            @Param(name = "tagentId", type = ApiParamType.LONG, isRequired = true, desc = "tagent id")
    })
    @Output({
            @Param(name = "status", type = ApiParamType.STRING, desc = "tagent状态检查")
    })
    @Description(desc = "tagent状态检查，用于web端主动发起检查agent状态")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TagentMessageVo message = JSONObject.toJavaObject(paramObj, TagentMessageVo.class);
        return tagentService.execTagentCmd(message, TagentAction.STATUS_CHECK.getValue());
    }
}
