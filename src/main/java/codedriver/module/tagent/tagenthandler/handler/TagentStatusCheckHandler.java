package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.RestVo;
import codedriver.framework.integration.authentication.enums.AuthenticateType;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.enums.TagentStatus;
import codedriver.framework.tagent.exception.TagentRunnerConnectRefusedException;
import codedriver.framework.tagent.service.TagentService;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerBase;
import codedriver.framework.util.RestUtil;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagentStatusCheckHandler extends TagentHandlerBase {

    Logger logger = LoggerFactory.getLogger(TagentStatusCheckHandler.class);

    @Autowired
    private TagentService tagentService;

    @Autowired
    private RunnerMapper runnerMapper;


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
        return TagentAction.STATUS_CHECK.getValue();
    }

    @Override
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String url) throws Exception {
        String tagentStatus = TagentStatus.DISCONNECTED.getValue();
        JSONObject paramJson = new JSONObject();
        paramJson.put("ip", tagentVo.getIp());
        paramJson.put("port", (tagentVo.getPort()).toString());
        paramJson.put("type", message.getName());
        url += "api/rest/tagent/status/check";
        String result = null;
        try {
            RestVo restVo = new RestVo.Builder(url, AuthenticateType.BUILDIN.getValue()).setPayload(paramJson).build();;
            result = RestUtil.sendPostRequest(restVo);
            JSONObject resultJson = JSONObject.parseObject(result);
            if (!resultJson.containsKey("Status") || !"OK".equals(resultJson.getString("Status"))) {
                throw new TagentRunnerConnectRefusedException(url, result);
            }
            tagentStatus = TagentStatus.CONNECTED.getValue();
        } catch (JSONException ex) {
            throw new TagentRunnerConnectRefusedException(url, result);
        }
        paramJson.put("status", tagentStatus);
        TagentVo tagent = new TagentVo();
        tagent.setId(tagentVo.getId());
        tagent.setStatus(tagentStatus);
        tagentService.updateTagentById(tagent);
        return paramJson;
    }
}
