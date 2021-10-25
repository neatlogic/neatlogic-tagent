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
public class TagentRestartHandler extends TagentHandlerBase {

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
        return TagentAction.RESTART.getValue();
    }

    @Override
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String runnerUrl) throws Exception {
        JSONObject paramJson = new JSONObject();
        //验证tagent对应的账号是否存在，以便后续从该账号获取对应密文
        AccountVo accountVo = resourceCenterMapper.getAccountById(tagentVo.getAccountId());
        if (accountVo == null) {
            throw new ResourceCenterAccountNotFoundException();
        }
        paramJson.put("credential", accountVo.getPasswordCipher());
        paramJson.put("type", TagentAction.RESTART.getValue());
        paramJson.put("ip", tagentVo.getIp());
        paramJson.put("port", tagentVo.getPort());

        String result = null;
        try {
            RestVo restVo = new RestVo(runnerUrl + "api/rest/tagent/restart", AuthenticateType.BUILDIN.getValue(), paramJson);
            result = RestUtil.sendRequest(restVo);
            JSONObject resultJson = JSONObject.parseObject(result);
            if (!resultJson.containsKey("Status") || !"OK".equals(resultJson.getString("Status"))) {
                throw new TagentActionFailedEcexption(restVo.getUrl() + ":" + resultJson.getString("Message"));
            }
            return resultJson.getJSONObject("Return");
        } catch (JSONException ex) {
            throw new TagentRunnerConnectRefusedException(runnerUrl, result);
        }
    }
}
