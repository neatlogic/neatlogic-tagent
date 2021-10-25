package codedriver.module.tagent.api;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.exception.file.FileAccessDeniedException;
import codedriver.framework.exception.file.FileTypeHandlerNotFoundException;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.file.core.FileTypeHandlerFactory;
import codedriver.framework.file.core.IFileTypeHandler;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.exception.TagentPkgVersionIdNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class TagentPkgDeleteApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Autowired
    private FileMapper fileMapper;

    @Override
    public String getName() {
        return "tagent安装包删除接口";
    }

    @Override
    public String getToken() {
        return "tagent/pkg/delete";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "安装包文件id", isRequired = true)
    })
    @Description(desc = "tagent安装包删除接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        TagentVersionVo versionVo = tagentMapper.getTagentVersionByFileId(id);
        if (versionVo == null) {
            throw new TagentPkgVersionIdNotFoundException(id);
        }

        FileVo fileVo = fileMapper.getFileById(versionVo.getFileId());
        String tenantUuid = TenantContext.get().getTenantUuid();
        if (StringUtils.isBlank(tenantUuid)) {
            throw new NoTenantException();
        }
        if (fileVo != null) {
            IFileTypeHandler fileTypeHandler = FileTypeHandlerFactory.getHandler(fileVo.getType());
            if (fileTypeHandler != null) {
                if (fileTypeHandler.valid(UserContext.get().getUserUuid(), fileVo, paramObj)) {
                    fileMapper.deleteFile(fileVo.getId());
                    FileUtil.deleteData(fileVo.getPath());
                    tagentMapper.deleteTagentVersionByFileId(id);
                } else {
                    throw new FileAccessDeniedException(fileVo.getName(), OperationTypeEnum.DELETE.getText());
                }
            } else {
                throw new FileTypeHandlerNotFoundException(fileVo.getType());
            }
        } else {
            throw new TagentPkgVersionIdNotFoundException(versionVo.getFileId());
        }
        return null;
    }

}
