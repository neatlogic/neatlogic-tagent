package neatlogic.module.tagent.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentVersionListApi extends PrivateApiComponentBase {
    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "获取所有的Tagent版本";
    }

    @Override
    public String getToken() {
        return "tagent/version/list";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "所有tagent版本")
    })
    @Description(desc = "获取所有的Tagent版本接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<ValueTextVo> returnVersionList = new ArrayList<>();
        List<String> allVersionList = tagentMapper.getAllTagentVersion();
        for (String version : allVersionList) {
            returnVersionList.add(new ValueTextVo(version, version));
        }
        return returnVersionList;
    }


}