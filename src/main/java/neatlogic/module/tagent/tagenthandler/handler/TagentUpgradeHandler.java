package neatlogic.module.tagent.tagenthandler.handler;

import neatlogic.framework.cmdb.crossover.IResourceAccountCrossoverMapper;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountVo;
import neatlogic.framework.cmdb.exception.resourcecenter.ResourceCenterAccountNotFoundException;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dto.RestVo;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.file.FileStorageMediumHandlerNotFoundException;
import neatlogic.framework.file.core.FileStorageMediumFactory;
import neatlogic.framework.file.core.IFileStorageHandler;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.dto.TagentVersionVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.enums.TagentAction;
import neatlogic.framework.tagent.exception.TagentActionFailedException;
import neatlogic.framework.tagent.exception.TagentPkgNotFoundException;
import neatlogic.framework.tagent.exception.TagentPkgVersionAndDefaultVersionAreNotfoundException;
import neatlogic.framework.tagent.exception.TagentRunnerConnectRefusedException;
import neatlogic.framework.tagent.service.TagentService;
import neatlogic.framework.tagent.tagenthandler.core.TagentHandlerBase;
import neatlogic.framework.util.RestUtil;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class TagentUpgradeHandler extends TagentHandlerBase {

    @Resource
    FileMapper fileMapper;

    @Resource
    TagentService tagentService;

    @Override
    public String getHandler() {
        return null;
    }

    @Override
    public String getHandlerName() {
        return null;
    }

    @Override
    public String getName() {
        return TagentAction.UPGRADE.getValue();
    }

    @Override
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, RunnerVo runnerVo) throws Exception {
        IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
        //验证tagent对应的账号是否存在，以便后续从该账号获取对应密文
        AccountVo accountVo = resourceAccountCrossoverMapper.getAccountById(tagentVo.getAccountId());
        if (accountVo == null) {
            throw new ResourceCenterAccountNotFoundException();
        }
        String result = StringUtils.EMPTY;
        String pkgVersion = message.getPkgVersion();
        //获取对应的安装包版本
        TagentVersionVo versionVo = tagentService.findTagentPkgVersion(tagentVo, pkgVersion);
        if (versionVo == null) {
            throw new TagentPkgVersionAndDefaultVersionAreNotfoundException(pkgVersion);
        }
        FileVo fileVo = fileMapper.getFileById(versionVo.getFileId());
        if (fileVo == null) {
            throw new TagentPkgNotFoundException(versionVo.getFileId());
        }
        String prefix = fileVo.getPath().split(":")[0];
        IFileStorageHandler fileStorageHandler = FileStorageMediumFactory.getHandler(prefix.toUpperCase());
        if (fileStorageHandler == null) {
            throw new FileStorageMediumHandlerNotFoundException(prefix);
        }
        if (!fileStorageHandler.isExit(fileVo.getPath())) {
            throw new TagentPkgNotFoundException();
        }

        List<FileVo> fileVoList = new ArrayList<>();
        fileVoList.add(fileVo);
        JSONObject params = new JSONObject();
        params.put("type", TagentAction.UPGRADE.getValue());
        params.put("ip", tagentVo.getIp());
        params.put("port", (tagentVo.getPort()).toString());
        params.put("user", tagentVo.getUser());
        params.put("fileName", fileVo.getName());
        params.put("ignoreFile", versionVo.getIgnoreFile());
        params.put("credential", accountVo.getPasswordCipher());
        String runnerUrl = runnerVo.getUrl() + "public/api/binary/tagent/upgrade";
        try {
            RestVo restVo = new RestVo.Builder(runnerUrl, AuthenticateType.BASIC.getValue()).setFormData(params).setFileVoList(fileVoList).setContentType(RestUtil.MULTI_FORM_DATA).build();
            result = RestUtil.sendPostRequest(restVo);
            JSONObject resultJson = JSONObject.parseObject(result);
            if (!resultJson.containsKey("Status") || !"OK".equals(resultJson.getString("Status"))) {
                throw new TagentActionFailedException(runnerVo, resultJson.getString("Message"));
            }
            return resultJson;
        } catch (JSONException ex) {
            throw new TagentRunnerConnectRefusedException(runnerUrl, result);
        }
    }
}

