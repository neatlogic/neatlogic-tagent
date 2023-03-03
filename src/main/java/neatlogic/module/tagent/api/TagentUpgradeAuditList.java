package neatlogic.module.tagent.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentUpgradeAuditVo;
import neatlogic.framework.util.TableResultUtil;
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
