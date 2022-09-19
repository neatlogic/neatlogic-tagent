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
import codedriver.framework.tagent.dto.TagentSearchVo;
import codedriver.framework.tagent.service.TagentService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author longrf
 * @date 2022/9/19 11:09
 */

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class BatchTagentActionApi extends PrivateApiComponentBase {

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "批量操作tagent";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "action", type = ApiParamType.STRING, desc = "tagent动作（reload(重启)、resetcred(重置密码)）"),
            @Param(name = "ipPortList", type = ApiParamType.JSONARRAY, desc = "ip,port列表"),
            @Param(name = "networkVoList", type = ApiParamType.JSONARRAY, desc = "网段"),
            @Param(name = "runnerGroupIdList", type = ApiParamType.JSONARRAY, desc = "代理组id")
    })
    @Description(desc = "批量操作tagent")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TagentSearchVo tagentSearchVo = paramObj.toJavaObject(TagentSearchVo.class);
        return tagentService.batchExecTagentChannelAction(paramObj.getString("action"), tagentSearchVo.getIpPortList(), tagentSearchVo.getNetworkVoList(), tagentSearchVo.getRunnerGroupIdList());
    }

    @Override
    public String getToken() {
        return "tagent/exec/batch/action";
    }
}
