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
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
        AccountVo accountVo = resourceCenterMapper.getAccountById(tagentVo.getAccountId());
        if (accountVo == null) {
            throw new ResourceCenterAccountNotFoundException();
        }
        JSONObject paramJson = new JSONObject();
        paramJson.put("type", TagentAction.SAVE_CONFIG.getValue());
        paramJson.put("ip", tagentVo.getIp());
        paramJson.put("data", message.getData());
        paramJson.put("port", (tagentVo.getPort()).toString());
        paramJson.put("credential", accountVo.getPasswordCipher());
        url = url + "api/rest/tagent/config/save";
        String result = null;
        try {
            RestVo restVo = new RestVo.Builder(url, AuthenticateType.BUILDIN.getValue()).setPayload(paramJson).build();
            result = RestUtil.sendPostRequest(restVo);
            JSONObject resultJson = JSONObject.parseObject(result);
            if (!resultJson.containsKey("Status") || !"OK".equals(resultJson.getString("Status"))) {
                throw new TagentActionFailedEcexption(restVo.getUrl() + ":" + resultJson.getString("Message"));
            }
        } catch (JSONException ex) {
            throw new TagentRunnerConnectRefusedException(url, result);
        }
        return null;
    }
}
