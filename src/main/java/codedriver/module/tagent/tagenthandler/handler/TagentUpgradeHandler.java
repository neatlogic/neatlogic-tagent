package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.cmdb.dao.mapper.resourcecenter.ResourceCenterMapper;
import codedriver.framework.cmdb.dto.resourcecenter.AccountVo;
import codedriver.framework.cmdb.exception.resourcecenter.ResourceCenterAccountNotFoundException;
import codedriver.framework.dto.RestVo;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.exception.file.FileStorageMediumHandlerNotFoundException;
import codedriver.framework.file.core.FileStorageMediumFactory;
import codedriver.framework.file.core.IFileStorageHandler;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.integration.authentication.enums.AuthenticateType;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.exception.TagentActionFailedEcexption;
import codedriver.framework.tagent.exception.TagentPkgNotFoundException;
import codedriver.framework.tagent.exception.TagentPkgVersionAndDefaultVersionAreNotfoundException;
import codedriver.framework.tagent.exception.TagentRunnerConnectRefusedException;
import codedriver.framework.tagent.service.TagentService;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerBase;
import codedriver.framework.util.RestUtil;
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
    TagentMapper tagentMapper;

    @Resource
    TagentService tagentService;
    @Resource
    ResourceCenterMapper resourceCenterMapper;

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
        AccountVo accountVo = resourceCenterMapper.getAccountById(tagentVo.getAccountId());
        if (accountVo == null) {
            throw new ResourceCenterAccountNotFoundException();
        }
        params.put("credential", accountVo.getPasswordCipher());
        String runnerUrl = runnerVo.getUrl() + "public/api/binary/tagent/upgrade";
        JSONObject resultJson = new JSONObject();
        try {
            RestVo restVo = new RestVo.Builder(runnerUrl, AuthenticateType.BASIC.getValue()).setFormData(params).setFileVoList(fileVoList).setContentType(RestUtil.MULTI_FORM_DATA).build();
            result = RestUtil.sendPostRequest(restVo);
            resultJson = JSONObject.parseObject(result);
            if (!resultJson.containsKey("Status") || !"OK".equals(resultJson.getString("Status"))) {
                throw new TagentActionFailedEcexption(runnerVo, resultJson.getString("Message"));
            }
            return resultJson;
        } catch (JSONException ex) {
            throw new TagentRunnerConnectRefusedException(runnerUrl, result);
        }
    }

}

