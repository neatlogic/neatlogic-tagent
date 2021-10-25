/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.cmdb.dao.mapper.resourcecenter.ResourceCenterMapper;
import codedriver.framework.cmdb.dto.resourcecenter.AccountVo;
import codedriver.framework.cmdb.exception.resourcecenter.ResourceCenterAccountNotFoundException;
import codedriver.framework.dto.RestVo;
import codedriver.framework.integration.authentication.enums.AuthenticateType;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.exception.TagentActionFailedEcexption;
import codedriver.framework.tagent.exception.TagentRunnerConnectRefusedException;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerBase;
import codedriver.framework.util.RestUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class TagentConfigSaveHandler extends TagentHandlerBase {

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
        return TagentAction.SAVE_CONFIG.getValue();
    }

    @Override
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String url) throws Exception {
        Map<String, String> params = new HashMap<>();
        String result = StringUtils.EMPTY;
        RestVo restVo = null;
        JSONObject resultJson = new JSONObject();
        params.put("type", TagentAction.SAVE_CONFIG.getValue());
        params.put("ip", tagentVo.getIp());
        params.put("data", message.getData());
        params.put("port", (tagentVo.getPort()).toString());
        AccountVo accountVo = resourceCenterMapper.getAccountById(tagentVo.getAccountId());
        if (accountVo == null) {
            throw new ResourceCenterAccountNotFoundException();
        }
        params.put("credential", accountVo.getPasswordCipher());
        url = url + "api/rest/tagent/config/save";
        try {
            restVo = new RestVo(url, AuthenticateType.BUILDIN.getValue(), JSONObject.parseObject(JSON.toJSONString(params)));
            result = RestUtil.sendRequest(restVo);
            resultJson = JSONObject.parseObject(result);
            if (!resultJson.containsKey("Status") || !"OK".equals(resultJson.getString("Status"))) {
                throw new TagentActionFailedEcexption(restVo.getUrl() + ":" + resultJson.getString("Message"));
            }
        } catch (Exception ex) {
            throw new TagentRunnerConnectRefusedException(url, resultJson.getString("Message"));
        }
        return JSONObject.parseObject(result).getJSONObject("Return");
    }
}
