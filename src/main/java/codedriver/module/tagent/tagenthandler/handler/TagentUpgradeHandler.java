package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.cmdb.dao.mapper.resourcecenter.ResourceCenterMapper;
import codedriver.framework.cmdb.dto.resourcecenter.AccountVo;
import codedriver.framework.cmdb.exception.resourcecenter.ResourceCenterAccountNotFoundException;
import codedriver.framework.file.core.FileStorageMediumFactory;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.exception.TagentPkgNotFoundException;
import codedriver.framework.tagent.exception.TagentPkgVersionNotFoundException;
import codedriver.framework.tagent.exception.TagentVersionIsHigHestException;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerBase;
import codedriver.framework.tagent.util.TagentHttpUtil;
import codedriver.framework.tagent.util.TagentVersionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TagentUpgradeHandler extends TagentHandlerBase {

    @Resource
    FileMapper fileMapper;

    @Resource
    TagentMapper tagentMapper;

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
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String url) throws Exception {

        String osType = this.getOsType(tagentVo.getOsType().toLowerCase(), tagentVo.getOsbit());
        String result = StringUtils.EMPTY;
        JSONObject resultJson = new JSONObject();

        //使用版本号、os类型、CPU架构确定安装包文件
        TagentVersionVo versionVo = tagentMapper.getTagentVersionVoByPkgVersionAndOSTypeAndOSBit(message.getPkgVersion(),osType,tagentVo.getOsbit());
        if (versionVo == null) {
            throw new TagentPkgVersionNotFoundException(message.getPkgVersion());
        }
        FileVo fileVo = fileMapper.getFileById(versionVo.getFileId());
        if (fileVo == null) {
            throw new TagentPkgNotFoundException(versionVo.getFileId());
        }
        //判断当前版本高低
        if (TagentVersionUtil.compareVersion(tagentVo.getVersion(), versionVo.getVersion()) > 0) {
            throw new TagentVersionIsHigHestException(tagentVo.getVersion());
        }
        String prefix = fileVo.getPath().split(":")[0];
        FileStorageMediumFactory.getHandler(prefix.toUpperCase()).isExit(fileVo.getPath());

        if (FileStorageMediumFactory.getHandler(prefix.toUpperCase()).isExit(fileVo.getPath())) {
            List<FileVo> fileVoList = new ArrayList<>();
            fileVoList.add(fileVo);
            Map<String, String> params = new HashMap<>();
            params.put("type",TagentAction.UPGRADE.getValue());
            params.put("ip", tagentVo.getIp());
            params.put("data", message.getData());//暂时发现无用data
            params.put("port", (tagentVo.getPort()).toString());
            params.put("user", tagentVo.getUser());
            params.put("fileName", fileVo.getName());
            params.put("ignoreFile",  versionVo.getIgnoreFile());
            AccountVo accountVo = resourceCenterMapper.getAccountById(tagentVo.getAccountId());
            if (accountVo == null) {
                throw new ResourceCenterAccountNotFoundException();
            }
            params.put("credential", accountVo.getPasswordCipher());
            url = url + "public/api/binary/tagent/upgrade";

     /*       try {
                RestVo restVo = new RestVo(url, AuthenticateType.BUILDIN.getValue(), params);
                result = RestUtil.sendRequest(restVo);
                resultJson = JSONObject.parseObject(result);
                if (!resultJson.containsKey("Status") || !"OK".equals(resultJson.getString("Status"))) {
                    throw new TagentActionFailedEcexption(url + ":" + resultJson.getString("Message"));
                }
            } catch (Exception ex) {
                throw new TagentRunnerConnectRefusedException(url, resultJson.getString("Message"));
            }*/

            byte[] upgradeRes = TagentHttpUtil.postFileWithParam(url, params, fileVoList);
            result = new String(upgradeRes);
        } else {
            throw new TagentPkgNotFoundException();//待确定
        }
        return JSON.parseObject(result);
    }

    /**
     * 根据os类型和CPU架构
     * @param type
     * @param cpuBit
     * @return
     */
    private  String getOsType(String type, String cpuBit){
        String osType;
        if (type.equals("windows")) {
            if (cpuBit.contains("64")) {
                osType = TagentVersionVo.TagentOsType.WINDOWS64.getType();
            } else {
                osType = TagentVersionVo.TagentOsType.WINDOWS64.getType();
            }
        } else {
            osType = TagentVersionVo.TagentOsType.LINUX.getType();
        }
        return osType;
    }
}

