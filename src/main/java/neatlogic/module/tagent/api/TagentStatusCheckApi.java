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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.exception.runner.RunnerNotFoundByTagentIdException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.enums.TagentAction;
import neatlogic.framework.tagent.enums.TagentStatus;
import neatlogic.framework.tagent.exception.TagentIdNotFoundException;
import neatlogic.framework.tagent.service.TagentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentStatusCheckApi extends PrivateApiComponentBase {
    @Resource
    TagentService tagentService;

    @Resource
    TagentMapper tagentMapper;

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "检查tagent状态";
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
        TagentMessageVo message = JSON.toJavaObject(paramObj, TagentMessageVo.class);
        TagentVo tagentVo = tagentMapper.getTagentById(message.getTagentId());
        TagentVo tagentMG = tagentService.getTagentMGById(message.getTagentId());
        if (tagentVo == null) {
            //存在历史垃圾数据,则删除
            if (tagentMG != null) {
                tagentService.deleteTagentMGById(message.getTagentId());
            }
            throw new TagentIdNotFoundException(message.getTagentId());
        }
        if (tagentMG == null || tagentMG.getRunnerId() == null) {
            throw new RunnerNotFoundByTagentIdException(tagentVo.getId(), tagentVo.getIp());
        }
        if (runnerMapper.getRunnerById(tagentMG.getRunnerId()) == null) {
            tagentVo.setDisConnectReason("runner 不存在");
            tagentVo.setStatus(TagentStatus.DISCONNECTED.getValue());
            tagentService.updateTagentMGByIdWithLock(tagentVo, false);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("disConnectReason", tagentVo.getDisConnectReason());
            return jsonObject;
        }
        return tagentService.execTagentCmd(message, TagentAction.STATUS_CHECK.getValue());
    }
}
