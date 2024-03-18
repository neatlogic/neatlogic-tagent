/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
