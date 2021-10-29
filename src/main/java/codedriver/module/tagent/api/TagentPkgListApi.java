package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.exception.TagentIdNotFoundException;
import codedriver.framework.util.TableResultUtil;
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
        return "tagent安装包列表查询接口";
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
            @Param(name = "tagentId", type = ApiParamType.LONG, desc = "tagent id")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "安装包列表")
    })
    @Description(desc = "tagent安装包列表查询接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long tagentId = paramObj.getLong("tagentId");
        TagentVersionVo tagentVersion = JSONObject.toJavaObject(paramObj, TagentVersionVo.class);
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
            return TableResultUtil.getResult(tagentMapper.searchTagentPkgList(tagentVersion), tagentVersion);
        }
        return null;
    }

}
