package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
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
        return "获取TagentOS类型";
    }

    @Override
    public String getToken() {
        return "tagent/ostype/list";
    }
    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "获取所有tagentOS类型")
    @Output({
            @Param(name = "tagentOStype",type = ApiParamType.JSONARRAY,desc = "所有tagentOS类型")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        return tagentMapper.searchTagentOSType();
    }


}
