/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
