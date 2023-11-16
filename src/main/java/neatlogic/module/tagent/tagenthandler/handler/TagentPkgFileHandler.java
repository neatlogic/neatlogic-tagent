/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tagent.tagenthandler.handler;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.file.core.FileTypeHandlerBase;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.exception.TagentPkgVersionIdNotFoundException;
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
