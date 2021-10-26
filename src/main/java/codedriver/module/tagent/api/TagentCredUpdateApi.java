package codedriver.module.tagent.api;

import codedriver.framework.cmdb.dao.mapper.resourcecenter.ResourceCenterMapper;
import codedriver.framework.cmdb.dto.resourcecenter.AccountVo;
import codedriver.framework.cmdb.exception.resourcecenter.ResourceCenterAccountNotFoundException;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.exception.TagentNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class TagentCredUpdateApi extends PublicApiComponentBase {

    private final Logger logger = LoggerFactory.getLogger(TagentCredUpdateApi.class);

    @Resource
    TagentMapper tagentMapper;

    @Resource
    ResourceCenterMapper resourceCenterMapper;

    @Override
    public String getName() {
        return "tagent密码更新接口";
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
            throw new TagentNotFoundException();
        }
        if (tagent.getAccountId() == null) {
            throw new ResourceCenterAccountNotFoundException();
        }
        AccountVo accountVo = resourceCenterMapper.getAccountById(tagent.getAccountId());
        accountVo.setPasswordCipher(null);
        accountVo.setPasswordPlain(paramObj.getString("credential"));
        resourceCenterMapper.updateAccount(accountVo);
        return null;

    }
}
