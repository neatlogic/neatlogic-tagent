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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_MANAGE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.enums.TagentStatus;
import neatlogic.framework.tagent.service.TagentService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = TAGENT_MANAGE.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class BatchDeleteTagentApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Resource
    TagentService tagentService;
    @Override
    public String getName() {
        return "nmta.batchdeletetagentapi.getname";
    }

    @Input({
            @Param(name = "idList", type = ApiParamType.JSONARRAY, minSize = 1, isRequired = true, desc = "common.id")
    })
    @Output({
    })
    @Description(desc = "nmta.batchdeletetagentapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONArray idArray = paramObj.getJSONArray("idList");
        List<Long> idList = idArray.toJavaList(Long.class);
        List<Long> needDeleteIdList = new ArrayList<>();
        List<TagentVo> tagentList = tagentService.getTagentMGListByIdList(idList);
        for (TagentVo tagentVo : tagentList) {
            if (Objects.equals(tagentVo.getStatus(), TagentStatus.CONNECTED.getValue())) {
                continue;
            }
            needDeleteIdList.add(tagentVo.getId());
        }
        if (CollectionUtils.isNotEmpty(needDeleteIdList)) {
            List<Long> accountIdList = tagentMapper.getAccountIdListByIdList(needDeleteIdList);
            tagentService.deleteTagentByIdList(needDeleteIdList, accountIdList);
            tagentService.deleteTagentMGByIdList(needDeleteIdList);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "tagent/delete/batch";
    }
}
