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
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dto.TagentSearchVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.service.TagentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentBatchUpgradeCheckApi extends PrivateApiComponentBase {

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "检测批量升级tagent";
    }

    @Override
    public String getToken() {
        return "tagent/batch/action/check";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "ipPortList", type = ApiParamType.JSONARRAY, desc = "ip,port列表"),
            @Param(name = "networkVoList", type = ApiParamType.JSONARRAY, desc = "网段列表")

    })
    @Description(desc = "批量升级前筛选出对应的tagent信息,来源于ip：port、网段掩码两种方式")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        // 批量升级不支持执行器组方式
        paramObj.remove("runnerGroupIdList");
        List<TagentVo> tagentList = tagentService.getTagentList(paramObj.toJavaObject(TagentSearchVo.class));
        return tagentList.size();
    }
}
