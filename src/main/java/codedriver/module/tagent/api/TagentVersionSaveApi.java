package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.exception.TagentPkgVersionIdAndFileIdAreNotNullException;
import codedriver.framework.tagent.exception.TagentPkgVersionIdAndFileIdAreNullException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class TagentVersionSaveApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "tagent版本添加接口";
    }

    @Override
    public String getToken() {
        return "tagent/version/save";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "version", type = ApiParamType.STRING, isRequired = true, desc = "tagent 版本"),
            @Param(name = "osType", type = ApiParamType.STRING, isRequired = true, desc = "os类型"),
            @Param(name = "osbit", type = ApiParamType.STRING, isRequired = true, desc = "cpu架构"),
            @Param(name = "pkgFileId", type = ApiParamType.LONG, desc = "已有安装包的文件id"),
            @Param(name = "fileId", type = ApiParamType.LONG, desc = "新上传的安装包文件id"),
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String version = paramObj.getString("version");
        String osType = paramObj.getString("osType");
        String osbit = paramObj.getString("osbit");
        Long pkgFileId = paramObj.getLong("pkgFileId");
        Long fileId = paramObj.getLong("fileId");
        TagentVersionVo versionVo = new TagentVersionVo(osType, version, osbit);
        if (pkgFileId != null && fileId != null) {
            throw new TagentPkgVersionIdAndFileIdAreNotNullException();
        }
        if (pkgFileId != null) {
            versionVo.setFileId(pkgFileId);
        } else if (fileId != null) {
            versionVo.setFileId(fileId);
        } else {
            throw new TagentPkgVersionIdAndFileIdAreNullException();
        }
        tagentMapper.insertTagentPkgFile(versionVo);
        return null;
    }

}
