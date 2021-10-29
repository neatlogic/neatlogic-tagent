package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.file.core.FileTypeHandlerBase;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.exception.TagentPkgVersionIdNotFoundException;
import com.alibaba.fastjson.JSONObject;
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

    }

    @Override
    public boolean beforeUpload(JSONObject jsonObj) {
        return true;
    }

    @Override
    public boolean beforeDelete(FileVo fileVo, JSONObject jsonObj) {
        Long pkgVersionId = jsonObj.getLong("pkgVersionId");
        Long fileId = fileVo.getId();
        if (tagentMapper.getTagentVersionById(pkgVersionId) == null) {
            throw new TagentPkgVersionIdNotFoundException(pkgVersionId);
        }
        tagentMapper.deleteTagentVersionById(pkgVersionId);
        if (tagentMapper.checkTagentPkgFileIdUsedCount(fileId) > 1) {
            return false;
        }
        return true;
    }
}
