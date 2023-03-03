/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.tagent.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
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
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentStatusCheckApi extends PrivateApiComponentBase {
    @Resource
    TagentService tagentService;

    @Autowired
    TagentMapper tagentMapper;

    @Autowired
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
        TagentMessageVo message = JSONObject.toJavaObject(paramObj, TagentMessageVo.class);
        TagentVo tagentVo = tagentMapper.getTagentById(message.getTagentId());
        if (tagentVo == null) {
            throw new TagentIdNotFoundException(message.getTagentId());
        }
        if (tagentVo.getRunnerId() == null || runnerMapper.getRunnerById(tagentVo.getRunnerId()) == null) {
            tagentVo.setDisConnectReason("runner 不存在");
            tagentVo.setStatus(TagentStatus.DISCONNECTED.getValue());
            tagentMapper.updateTagentStatusAndDisConnectReasonById(tagentVo.getStatus(),tagentVo.getDisConnectReason(),tagentVo.getId());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("disConnectReason", tagentVo.getDisConnectReason());
            return jsonObject;
        }
        return tagentService.execTagentCmd(message, TagentAction.STATUS_CHECK.getValue());
    }
}
