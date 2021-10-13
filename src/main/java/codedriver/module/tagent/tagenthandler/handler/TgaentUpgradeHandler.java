package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerBase;
import codedriver.framework.tagent.util.TagentVersionUtil;
import codedriver.module.tagent.common.config.TagentConfig;
import codedriver.module.tagent.util.TagentUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TgaentUpgradeHandler extends TagentHandlerBase {

    @Autowired
    private TagentMapper tagentMapper;

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
        return "upgrade";
    }

    @Override
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String url) throws Exception {

        List<File> upgradeList = new ArrayList<>();
        String tagentPkgName = TagentUtil.getPkgName(tagentVo.getOsType().toLowerCase(), tagentVo.getOsbit());
        String osType, tagentVersion = null, ignoreFile = "";
        if (tagentVo.getOsType().toLowerCase().equals("windows")) {
            if (tagentVo.getOsbit().contains("64")) {
                osType = TagentVersionVo.TagentOsType.WINDOWS64.getType();
            } else {
                osType = TagentVersionVo.TagentOsType.WINDOWS32.getType();
            }
        } else {
            osType = TagentVersionVo.TagentOsType.LINUX.getType();
        }

        File file = new File(TagentConfig.TAGENT_PKG_PATH + File.separator + tagentPkgName);
        if (!file.exists()) {
            TagentUtil.buildTar(tagentVo.getOsType().toLowerCase(), tagentVo.getOsbit());
        }

        List<TagentVersionVo> versionList = tagentMapper.getVersionList();
        for (TagentVersionVo version : versionList) {
            if (version.getOsType().toLowerCase().equals(osType)) {
                tagentVersion = version.getVersion();
                ignoreFile = version.getIgnoreFile();
                break;
            }
        }

        if (StringUtils.isBlank(tagentVersion)) {
            throw new RuntimeException("未定义版本号，请在安装包管理菜单中定义版本号");
        }

        int compareCode = TagentVersionUtil.compareVersion(tagentVo.getVersion(), tagentVersion);
        if (compareCode > 0) {
            throw new RuntimeException("当前版本高于安装包版本，无需升级");
        }

        file = new File(TagentConfig.TAGENT_PKG_PATH + File.separator + tagentPkgName);

        if (file.exists()) {
            upgradeList.add(file);
            Map<String, String> params = new HashMap<>();
            params.put("type", message.getName());
            params.put("ip", tagentVo.getIp());
            params.put("data", message.getData());
            params.put("port", (tagentVo.getPort()).toString());
            params.put("user", tagentVo.getUser());
            params.put("credential", tagentVo.getCredential());
            params.put("fileName", tagentPkgName);
            params.put("ignoreFile", ignoreFile);
//            byte[] upgradeRes = TagentHttpUtil.postFileWithParam(url + "/tagentUpgrade", params, upgradeList);
//            String res = new String(upgradeRes);
//            response.setContentType(Config.RESPONSE_TYPE_JSON);
//            response.getWriter().print(JSONObject.parseObject(res));
        } else {
            JSONObject result = new JSONObject();
            result.put("Status", "ERROR");
            result.put("Message", "can not find upgrade file in " + TagentConfig.TAGENT_PATH);
//            response.setContentType(Config.RESPONSE_TYPE_JSON);
//            response.getWriter().print(result);

        }
        return null;
    }
}

