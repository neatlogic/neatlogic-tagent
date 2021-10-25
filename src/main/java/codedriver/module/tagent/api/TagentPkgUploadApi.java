package codedriver.module.tagent.api;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.exception.file.EmptyFileException;
import codedriver.framework.exception.file.FileExtNotAllowedException;
import codedriver.framework.exception.file.FileTooLargeException;
import codedriver.framework.exception.file.FileTypeHandlerNotFoundException;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.file.core.FileTypeHandlerFactory;
import codedriver.framework.file.core.IFileTypeHandler;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileTypeVo;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.exception.TagentPkgFormatIllegalException;
import codedriver.framework.tagent.exception.TagentPkgVersionIsExists;
import codedriver.module.framework.file.handler.LocalFileSystemHandler;
import codedriver.module.framework.file.handler.MinioFileSystemHandler;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
@Transactional
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class TagentPkgUploadApi extends PrivateBinaryStreamApiComponentBase {
    static Logger logger = LoggerFactory.getLogger(TagentPkgUploadApi.class);

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "tagent安装包上传接口";
    }

    @Override
    public String getToken() {
        return "tagent/pkg/upload";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "param", type = ApiParamType.STRING, desc = "附件参数名称", isRequired = true),
            @Param(name = "type", type = ApiParamType.STRING, desc = "附件类型", isRequired = true),
            @Param(name = "version", type = ApiParamType.STRING, desc = "版本号（唯一性）", isRequired = true),
            @Param(name = "osType", type = ApiParamType.STRING, desc = "OS类型", isRequired = true),
            @Param(name = "osbit", type = ApiParamType.STRING, desc = "CPU架构", isRequired = true)
    })
    @Output({@Param(explode = FileVo.class)})
    @Description(desc = "tagent安装包上传接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //检验安装包版本
        String version = paramObj.getString("version");
        String osType = paramObj.getString("osType");
        String osbit = paramObj.getString("osbit");
        TagentVersionVo versionVo = new TagentVersionVo(osType, version, osbit);
        if (tagentMapper.checkTagentVersion(versionVo) > 0) {
            throw new TagentPkgVersionIsExists(version);
        }
        String tenantUuid = TenantContext.get().getTenantUuid();
        if (StringUtils.isBlank(tenantUuid)) {
            throw new NoTenantException();
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        String paramName = paramObj.getString("param");
        String type = paramObj.getString("type");
        List<FileTypeVo> fileTypeList = FileTypeHandlerFactory.getActiveFileTypeHandler();
        FileTypeVo fileTypeVo = null;
        for (FileTypeVo f : fileTypeList) {
            if (f.getName().equalsIgnoreCase(type)) {
                fileTypeVo = f;
                break;
            }
        }
        if (fileTypeVo == null) {
            throw new FileTypeHandlerNotFoundException(type);
        }
        FileTypeVo fileTypeConfigVo = fileMapper.getFileTypeConfigByType(fileTypeVo.getName());

        MultipartFile multipartFile = multipartRequest.getFile(paramName);

        if (multipartFile != null) {

            IFileTypeHandler fileTypeHandler = FileTypeHandlerFactory.getHandler(type);
            if (fileTypeHandler == null) {
                throw new FileTypeHandlerNotFoundException(type);
            }

            String oldFileName = multipartFile.getOriginalFilename();
            if (StringUtils.isBlank(oldFileName) || (!oldFileName.endsWith(".tar"))) {
                throw new TagentPkgFormatIllegalException(".tar");
            }
            multipartFile.getName();
            String userUuid = UserContext.get().getUserUuid(true);
            long size = multipartFile.getSize();
            // 如果配置为空代表不受任何限制
            if (fileTypeConfigVo != null) {
                boolean isAllowed = false;
                long maxSize = 0L;
                String fileExt = oldFileName.substring(oldFileName.lastIndexOf(".") + 1).toLowerCase();
                JSONObject configObj = fileTypeConfigVo.getConfigObj();
                JSONArray whiteList = new JSONArray();
                JSONArray blackList = new JSONArray();
                if (size == 0) {
                    throw new EmptyFileException();
                }
                if (configObj != null) {
                    whiteList = configObj.getJSONArray("whiteList");
                    blackList = configObj.getJSONArray("blackList");
                    maxSize = configObj.getLongValue("maxSize");
                }
                if (whiteList != null && whiteList.size() > 0) {
                    for (int i = 0; i < whiteList.size(); i++) {
                        if (fileExt.equalsIgnoreCase(whiteList.getString(i))) {
                            isAllowed = true;
                            break;
                        }
                    }
                } else if (blackList != null && blackList.size() > 0) {
                    isAllowed = true;
                    for (int i = 0; i < blackList.size(); i++) {
                        if (fileExt.equalsIgnoreCase(blackList.getString(i))) {
                            isAllowed = false;
                            break;
                        }
                    }
                } else {
                    isAllowed = true;
                }
                if (!isAllowed) {
                    throw new FileExtNotAllowedException(fileExt);
                }
                if (maxSize > 0 && size > maxSize) {
                    throw new FileTooLargeException(size, maxSize);
                }
            }

            FileVo fileVo = new FileVo();
            fileVo.setName(oldFileName);
            fileVo.setSize(size);
            fileVo.setUserUuid(userUuid);
            fileVo.setType(type);
            fileVo.setContentType(multipartFile.getContentType());
            String filePath = null;
            try {
                filePath = FileUtil.saveData(MinioFileSystemHandler.NAME, tenantUuid, multipartFile.getInputStream(), fileVo.getId().toString(), fileVo.getContentType(), fileVo.getType());
            } catch (Exception ex) {
                // 如果minio出现异常，则上传到本地
                logger.error(ex.getMessage(), ex);
                filePath = FileUtil.saveData(LocalFileSystemHandler.NAME, tenantUuid, multipartFile.getInputStream(), fileVo.getId().toString(), fileVo.getContentType(), fileVo.getType());
            }
            fileVo.setPath(filePath);
            fileMapper.insertFile(fileVo);
            fileTypeHandler.afterUpload(fileVo, paramObj);
            FileVo file = fileMapper.getFileById(fileVo.getId());
            versionVo.setFileId(file.getId());
            file.setUrl("api/binary/tagent/pkg/download?id=" + fileVo.getId());

            tagentMapper.insertTagentPkgFile(versionVo);
            return file;
        }

        return null;
    }
}
