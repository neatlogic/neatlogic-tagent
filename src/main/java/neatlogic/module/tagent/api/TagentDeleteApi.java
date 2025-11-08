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
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_MANAGE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.enums.TagentStatus;
import neatlogic.framework.tagent.exception.TagentHasBeenConnectedException;
import neatlogic.framework.tagent.service.TagentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@Transactional
@AuthAction(action = TAGENT_MANAGE.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class TagentDeleteApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "删除tagent";
    }

    @Override
    public String getToken() {
        return "tagent/delete";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", isRequired = true, type = ApiParamType.LONG, desc = "tagent id")
    })
    @Output({
    })
    @Description(desc = "删除tagent接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        if (id != null) {
            TagentVo tagent = tagentMapper.getTagentById(id);
            TagentVo tagentMG = tagentService.getTagentMGById(id);
            if (tagent == null) {
                //mongodb tagent过时数据删除
                tagentService.deleteTagentMGById(id);
                return null;
            }
            if (tagentMG == null) {
                //删除tagent
                tagentMapper.deleteTagentById(id);
                tagentMapper.deleteAccountById(tagent.getAccountId());
                tagentMapper.deleteAllIpByTagentId(id);
                return null;
            }
            //非已连接状态或tagent ip在mysql和mongodb不一致时（兼容垃圾数据）
            if (!StringUtils.equals(tagentMG.getStatus(), TagentStatus.CONNECTED.getValue()) || !Objects.equals(tagentMG.getIp(), tagent.getIp())) {
                //删除tagent
                tagentMapper.deleteTagentById(id);
                tagentMapper.deleteAccountById(tagent.getAccountId());
                tagentMapper.deleteAllIpByTagentId(id);
                tagentService.deleteTagentMGById(id);
            } else {
                throw new TagentHasBeenConnectedException(tagent);
            }
        }
        return null;
    }
}
