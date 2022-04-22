package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentUpgradeAuditVo;
import codedriver.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentUpgradeAuditList extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "查询tagent升级记录";
    }

    @Override
    public String getToken() {
        return "tagent/upgrade/audit/list";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "fcuName", type = ApiParamType.STRING, desc = "执行用户"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "tbodyList", explode = TagentUpgradeAuditVo[].class, desc = "tagent升级记录列表")
    })
    @Description(desc = "查询tagent升级记录接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TagentUpgradeAuditVo auditVo = JSONObject.toJavaObject(paramObj, TagentUpgradeAuditVo.class);
        int rowNum = tagentMapper.searchTagentUpgradeAuditCountByUserName(auditVo.getFcuName());
        if (rowNum > 0) {
            auditVo.setRowNum(rowNum);
            return TableResultUtil.getResult(tagentMapper.searchTagentUpgradeAuditList(auditVo), auditVo);
        }
        return new JSONObject();
    }

}
