/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.dto.TagentSearchVo;
import neatlogic.framework.tagent.enums.TagentAction;
import neatlogic.framework.tagent.service.TagentService;
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
public class SaveTagentConfigBatchApi extends PrivateApiComponentBase {

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "批量修改配置文件";
    }

    @Override
    public String getToken() {
        return "tagent/exec/config/batch/save";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "configKeyValueJson", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "配置key、value的Json"),
            @Param(name = "ipPortList", type = ApiParamType.JSONARRAY, desc = "ip,port列表"),
            @Param(name = "networkVoList", type = ApiParamType.JSONARRAY, desc = "网段列表"),
            @Param(name = "runnerGroupIdList", type = ApiParamType.JSONARRAY, desc = "执行器组id列表")
    })
    @Description(desc = "批量修改配置文件")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TagentMessageVo tagentMessageVo = new TagentMessageVo();
        tagentMessageVo.setData(paramObj.getJSONObject("configKeyValueJson").toString());
        return tagentService.batchExecTagentChannelAction(TagentAction.BATCH_SAVE_CONFIG.getValue(), paramObj.toJavaObject(TagentSearchVo.class), tagentMessageVo);
    }
}
