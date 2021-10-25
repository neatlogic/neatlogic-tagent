package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.file.core.FileTypeHandlerBase;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.exception.TagentPkgVersionIdNotFoundException;
import codedriver.framework.tagent.exception.TagentPkgVersionIsExists;
import codedriver.framework.tagent.exception.TagentUploadLessParameterException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagentPkgFileHandler extends FileTypeHandlerBase {

    @Autowired
    private TagentMapper tagentMapper;

    @Override
    protected boolean myDeleteFile(Long fileId) {
        return false;
    }

    @Override
    public boolean valid(String userUuid, FileVo fileVo, JSONObject jsonObj) {
        if (AuthActionChecker.checkByUserUuid(UserContext.get().getUserUuid(), TAGENT_BASE.class.getSimpleName())) {
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "TAGENT";
    }

    @Override
    public String getDisplayName() {
        return "tagent安装包文件";
    }

    @Override
    public void afterUpload(FileVo fileVo, JSONObject jsonObj) {
        String version = jsonObj.getString("version");
        String osType = jsonObj.getString("osType");
        String osbit = jsonObj.getString("osbit");
        if (StringUtils.isNotBlank(version) && StringUtils.isNotBlank(osType) && StringUtils.isNotBlank(osbit)) {
            TagentVersionVo versionVo = new TagentVersionVo(osType, version, osbit);
            versionVo.setFileId(fileVo.getId());
            tagentMapper.insertTagentPkgFile(versionVo);
        } else {
            throw new TagentUploadLessParameterException();
        }
    }

    @Override
    public boolean beforeUpload(JSONObject jsonObj) {
        String version = jsonObj.getString("version");
        if (tagentMapper.checkTagentVersion(version) > 0) {
            throw new TagentPkgVersionIsExists(version);
        }
        return true;
    }

    @Override
    public boolean myDelete(JSONObject jsonObj) {
        Long id = jsonObj.getLong("fileId");
        TagentVersionVo versionVo = tagentMapper.getTagentVersionByFileId(id);
        if (versionVo == null) {
            throw new TagentPkgVersionIdNotFoundException(id);
        }
        tagentMapper.deleteTagentVersionByFileId(id);
        return true;
    }
}
