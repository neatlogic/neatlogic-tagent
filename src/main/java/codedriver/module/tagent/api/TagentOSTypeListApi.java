package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentOSVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentOSTypeListApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "查询TagentOS类型列表";
    }

    @Override
    public String getToken() {
        return "tagent/ostype/list";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "查询TagentOS类型接口")
    @Output({
            @Param(name = "tbodyList", explode = TagentOSVo[].class, desc = "tagentOS类型列表")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject tbodyList = new JSONObject();
        tbodyList.put("tbodyList", tagentMapper.searchTagentOSType());
        return tbodyList;
    }
}
