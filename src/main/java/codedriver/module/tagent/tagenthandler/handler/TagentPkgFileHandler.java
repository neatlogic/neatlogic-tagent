/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

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
    protected boolean myDeleteFile(FileVo fileVo, JSONObject paramObj) {
        Long pkgVersionId = paramObj.getLong("pkgVersionId");
        Long fileId = fileVo.getId();
        if (tagentMapper.getTagentVersionById(pkgVersionId) == null) {
            throw new TagentPkgVersionIdNotFoundException(pkgVersionId);
        }
        tagentMapper.deleteTagentVersionById(pkgVersionId);
        return tagentMapper.getTagentPkgFileIdUsedCount(fileId) <= 1;
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


}
