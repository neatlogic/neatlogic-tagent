package neatlogic.module.tagent.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentVersionVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.exception.TagentIdNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentPkgListApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "查询tagent安装包列表";
    }

    @Override
    public String getToken() {
        return "tagent/pkg/list";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "tagentId", type = ApiParamType.LONG, desc = "tagent id"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "安装包列表")
    })
    @Description(desc = "查询tagent安装包列表接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long tagentId = paramObj.getLong("tagentId");
        TagentVersionVo tagentVersion = JSONObject.toJavaObject(paramObj, TagentVersionVo.class);
        JSONObject result = new JSONObject();
        if (tagentId != null) {
            TagentVo tagentVo = tagentMapper.getTagentById(tagentId);
            if (tagentVo == null) {
                throw new TagentIdNotFoundException(tagentId);
            }
            tagentVersion.setOsType(tagentVo.getOsType());
            tagentVersion.setOsbit(tagentVo.getOsbit());
        }
        int rowNum = tagentMapper.searchTagentVersionCount();
        if (rowNum > 0) {
            tagentVersion.setRowNum(rowNum);
            result.put("tbodyList", tagentMapper.searchTagentPkgList(tagentVersion));
        }
        return result;
    }

}
