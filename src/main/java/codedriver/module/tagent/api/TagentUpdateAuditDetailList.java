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
public class TagentUpdateAuditDetailList extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "tagent升级记录详情查询接口";
    }

    @Override
    public String getToken() {
        return "tagent/upgrade/audit/detail/list";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "auditId", type = ApiParamType.LONG,isRequired = true,desc = "记录id"),
            @Param(name = "status", type = ApiParamType.STRING, desc = "升级状态"),
            @Param(name = "ip", type = ApiParamType.STRING, desc = "ip")
    })
    @Output({
            @Param(name = "tbodyList", explode = TagentUpgradeAuditVo[].class, desc = "tagent升级记录详情列表")
    })
    @Description(desc = "tagent升级记录查询接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TagentUpgradeAuditVo auditVo = JSONObject.toJavaObject(paramObj,TagentUpgradeAuditVo.class);
        int rowNum = tagentMapper.searchTagentUpgradeAuditCountByAuditIdAndStatusAndIp(auditVo);
        if (rowNum > 0) {
            auditVo.setRowNum(rowNum);
            return TableResultUtil.getResult(tagentMapper.searchTagenUpgradeAuditListByAuditIdAndStatusAndIp(auditVo), auditVo);
        }
        return null;
    }

}
