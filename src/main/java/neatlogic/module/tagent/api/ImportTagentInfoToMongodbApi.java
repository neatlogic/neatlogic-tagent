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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.service.TagentService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ImportTagentInfoToMongodbApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "nmta.importtagentinfotomongodbapi.description.desc";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public String getToken() {
        return "tagent/info/to/mongodb/refresh";
    }

    @Description(desc = "nmta.importtagentinfotomongodbapi.description.desc")
    @Input({
    })
    @Output({
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TagentVo tagentVo = new TagentVo();
        int rowNum = tagentMapper.searchTagentCount(tagentVo);
        if (rowNum > 0) {
            tagentVo.setPageSize(100);
            tagentVo.setRowNum(rowNum);
            for (int i = 0; i < tagentVo.getPageCount(); i++) {
                tagentVo.setCurrentPage(i + 1);
                List<TagentVo> tagentList = tagentMapper.searchTagent(tagentVo);
                if (CollectionUtils.isNotEmpty(tagentList)) {
                    for (TagentVo tagent : tagentList) {
                        tagentService.updateTagentMGById(tagent);
                    }
                }
            }
        }
        return null;
    }
}
