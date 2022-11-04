/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */
package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentSearchVo;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.service.TagentService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author longrf
 * @date 2022/11/3 10:13
 */

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class ReSetTagentPasswordBatchApi extends PrivateApiComponentBase {

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "批量修改tagent密码";
    }

    @Override
    public String getToken() {
        return "tagent/exec/password/batch/reset";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "ipPortList", type = ApiParamType.JSONARRAY, desc = "ip,port列表"),
            @Param(name = "networkVoList", type = ApiParamType.JSONARRAY, desc = "网段列表"),
            @Param(name = "runnerGroupIdList", type = ApiParamType.JSONARRAY, desc = "执行器组id列表")
    })
    @Description(desc = "批量修改tagent密码")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        return tagentService.batchExecTagentChannelAction(TagentAction.RESET_CREDENTIAL.getValue(), paramObj.toJavaObject(TagentSearchVo.class), new TagentMessageVo());
    }
}
