/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tagent.tagenthandler.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.file.core.FileTypeHandlerBase;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.exception.TagentPkgVersionIdNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagentPkgFileHandler extends FileTypeHandlerBase {

    @Autowired
    private TagentMapper tagentMapper;

    @Override
    protected boolean myDeleteFile(FileVo fileVo, JSONObject paramObj) {
        Long pkgVersionId = paramObj.getLong("pkgVersionId");
        if (tagentMapper.getTagentVersionById(pkgVersionId) == null) {
            throw new TagentPkgVersionIdNotFoundException(pkgVersionId);
        }
        tagentMapper.deleteTagentVersionById(pkgVersionId);
        return tagentMapper.getTagentPkgFileIdUsedCount(fileVo.getId()) <= 0;
    }

    @Override
    public boolean valid(String userUuid, FileVo fileVo, JSONObject jsonObj) {
        return AuthActionChecker.checkByUserUuid(UserContext.get().getUserUuid(), TAGENT_BASE.class.getSimpleName());
    }

    /**
     * 校验附件是否允许删除
     * @param fileVo 附件信息
     * @return
     */
    public boolean validDeleteFile(FileVo fileVo) {
        return AuthActionChecker.checkByUserUuid(UserContext.get().getUserUuid(), TAGENT_BASE.class.getSimpleName());
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
