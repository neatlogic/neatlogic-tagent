package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.file.core.FileTypeHandlerBase;
import codedriver.framework.file.dto.FileVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class TagentPkgFileHandler extends FileTypeHandlerBase {
    @Override
    protected boolean myDeleteFile(Long fileId) {
        return false;
    }

    @Override
    public boolean valid(String userUuid, FileVo fileVo, JSONObject jsonObj) {
        if (fileVo != null && StringUtils.isNotBlank(userUuid)) {
            return fileVo.getUserUuid().equals(userUuid);
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
}
