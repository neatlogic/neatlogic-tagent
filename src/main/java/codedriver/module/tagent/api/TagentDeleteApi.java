package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.exception.TagentHasBeenConnectedException;
import codedriver.framework.tagent.exception.TagentIdNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class TagentDeleteApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "tagent 删除接口";
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
            @Param(name = "id",isRequired = true,type = ApiParamType.LONG,desc = "Tagent id")
    })
    @Output({
    })
    @Description(desc = "tagent 删除接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        if (id != null) {
            TagentVo tagent = tagentMapper.searchTagentById(id);
            if (tagent == null) {
                throw new TagentIdNotFoundException(id);
            }
            if ( !StringUtils.equals(tagent.getStatus(), "connect")) {
                tagentMapper.deleteTagentById(id);
            } else {
                throw new TagentHasBeenConnectedException(tagent);
            }
        }
        return null;
    }
}
