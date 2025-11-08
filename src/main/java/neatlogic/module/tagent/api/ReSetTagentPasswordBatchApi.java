/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */
package neatlogic.module.tagent.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_MANAGE;
import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.dto.TagentSearchVo;
import neatlogic.framework.tagent.enums.TagentAction;
import neatlogic.framework.tagent.service.TagentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author longrf
 * @date 2022/11/3 10:13
 */

@Service
@AuthAction(action = TAGENT_MANAGE.class)
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
