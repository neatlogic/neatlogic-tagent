package neatlogic.module.tagent.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentVersionVo;
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
            @Param(name = "isOverWrite", type = ApiParamType.INTEGER, isRequired = true, desc = "是否覆盖(0不覆盖，1覆盖)（用于在版本、OS类型、cpu架构的情况下，是否覆盖原有文件）"),
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
        TagentVersionVo tagentVersionVo = tagentMapper.getTagentVersionVoByPkgVersionAndOSTypeAndOSBit(version, osType, osbit);
        //查看是否有相同版本、OS类型、cpu架构的安装包，若相同则返回1，前端则会提示对应的安装包已存在，是否选择覆盖，当客户选择覆盖时，则可以传送isOverWrite=1进行覆盖
        if (tagentVersionVo != null) {
            if (paramObj.getInteger("isOverWrite") == 0) {
                return 1;
            }

            versionVo.setId(tagentVersionVo.getId());
        }
        versionVo.setFileId(fileId);
        if (StringUtils.equals(osType, "linux")) {//以osType确定升级时的忽略目录或文件  -by波哥
            versionVo.setIgnoreFile("lib/perl-lib/lib/perl5/JSON.pm");
        } else if (StringUtils.equals(osType, "windows")) {
            versionVo.setIgnoreFile("mod/7-Zip lib/perl-lib/lib/perl5/JSON.pm");
        }
        tagentMapper.replaceTagentPkgFile(versionVo);
        return null;
    }

}
