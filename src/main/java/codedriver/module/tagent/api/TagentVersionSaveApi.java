package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.file.FileNotFoundException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVersionVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class TagentVersionSaveApi extends PrivateApiComponentBase {

    @Resource
    FileMapper fileMapper;

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "添加tagent版本";
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
            @Param(name = "fileId", type = ApiParamType.LONG, isRequired = true, desc = "文件id"),
    })
    @Description(desc = "添加tagent版本接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String version = paramObj.getString("version");
        String osType = paramObj.getString("osType");
        String osbit = paramObj.getString("osbit");
        Long fileId = paramObj.getLong("fileId");
        TagentVersionVo versionVo = new TagentVersionVo(osType, version, osbit);
        if (fileMapper.getFileById(fileId) == null) {
            throw new FileNotFoundException(fileId);
        }
        versionVo.setFileId(fileId);
        if (StringUtils.equals(osType, "linux")) {
            versionVo.setIgnoreFile("lib/perl-lib/lib/perl5/JSON.pm");
        } else if (StringUtils.equals(osType, "win32")) {
            versionVo.setIgnoreFile("mod/7-Zip lib/perl-lib/lib/perl5/JSON.pm");
        } else if (StringUtils.equals(osType, "win64")) {
            versionVo.setIgnoreFile("mod/7-Zip lib/perl-lib/lib/perl5/JSON.pm");
        }
        tagentMapper.insertTagentPkgFile(versionVo);
        return null;
    }

}
