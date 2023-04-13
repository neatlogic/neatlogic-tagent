package neatlogic.module.tagent.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.cmdb.exception.resourcecenter.ResourceCenterAccountNotFoundException;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountBaseVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.exception.TagentNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class TagentCredUpdateApi extends PublicApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "更新tagent密码";
    }

    @Override
    public String getToken() {
        return "tagent/cred/update";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "ip", type = ApiParamType.STRING, isRequired = true, desc = "tagent ip"),
            @Param(name = "port", type = ApiParamType.INTEGER, isRequired = true, desc = "tagent端口"),
            @Param(name = "credential", type = ApiParamType.STRING, isRequired = true, desc = "tagent明文密码"),
    })
    @Output({})
    @Description(desc = "tagent密码更新接口，用于tagent重置密码成功后，runner端更新tagent密码")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String ip = paramObj.getString("ip");
        Integer port = Integer.valueOf(paramObj.getString("port"));
        TagentVo tagent = tagentMapper.getTagentByIpAndPort(ip, port);
        if (tagent == null) {
            throw new TagentNotFoundException(ip, port);
        }
        if (tagent.getAccountId() == null) {
            throw new ResourceCenterAccountNotFoundException();
        }
//        IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
        AccountBaseVo accountVo = tagentMapper.getAccountById(tagent.getAccountId());
        accountVo.setPasswordCipher(null);
        accountVo.setPasswordPlain(paramObj.getString("credential"));
        tagentMapper.updateAccount(accountVo);
        return null;

    }
}
