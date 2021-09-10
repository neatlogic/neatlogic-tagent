package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.cmdb.dao.mapper.resourcecenter.ResourceCenterMapper;
import codedriver.framework.cmdb.dto.resourcecenter.AccountVo;
import codedriver.framework.cmdb.exception.resourcecenter.ResourceCenterAccountNotFoundException;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.RC4Util;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dto.TagentVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentPwdSearchApi extends PrivateApiComponentBase {

    @Resource
    ResourceCenterMapper resourceCenterMapper;

    @Override
    public String getName() {
        return "Tagent 查看密码接口";
    }

    @Override
    public String getToken() {
        return "tagent/pwd/search";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "id",isRequired = true,type = ApiParamType.LONG,desc = "Tagent id")
    })
    @Output({
            @Param(name = "pwd",type = ApiParamType.STRING,desc = "密码")
    })
    @Description(desc = "tagent 查看密码接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject jsonObj = new JSONObject();
        Long id = paramObj.getLong("id");
        if (id != null) {
            AccountVo accountVo = resourceCenterMapper.getAccountByTagentId(id);
            if (accountVo == null) {
                throw new ResourceCenterAccountNotFoundException(id);
            }
            String pwd = RC4Util.decrypt(accountVo.getPassword());
            jsonObj.put("pwd", pwd);
        }
        return jsonObj;
    }


}
