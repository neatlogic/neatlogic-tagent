package neatlogic.module.tagent.tagenthandler.handler;

import neatlogic.framework.dto.RestVo;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.enums.TagentAction;
import neatlogic.framework.tagent.enums.TagentStatus;
import neatlogic.framework.tagent.tagenthandler.core.TagentHandlerBase;
import neatlogic.framework.util.RestUtil;
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
        return TagentAction.STATUS_CHECK.getValue();
    }

    @Override
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, RunnerVo runnerVo) throws Exception {
        String tagentStatus = TagentStatus.CONNECTED.getValue();
        JSONObject paramJson = new JSONObject();
        paramJson.put("ip", tagentVo.getIp());
        paramJson.put("port", (tagentVo.getPort()).toString());
        paramJson.put("type", TagentAction.STATUS_CHECK.getValue());
        String url = runnerVo.getUrl() + "api/rest/tagent/status/check";
        String result = null;
        String disConnectReason = "";
        try {
            RestVo restVo = new RestVo.Builder(url, AuthenticateType.BUILDIN.getValue()).setPayload(paramJson).build();
            result = RestUtil.sendPostRequest(restVo);
            JSONObject resultJson = JSONObject.parseObject(result);
            if (!resultJson.containsKey("Status") || !"OK".equals(resultJson.getString("Status"))) {
                tagentStatus = TagentStatus.DISCONNECTED.getValue();
                disConnectReason = resultJson.getString("Message");
            }
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            tagentStatus = TagentStatus.DISCONNECTED.getValue();
            disConnectReason = "runner返回：" + result;
        } finally {
            tagentVo.setStatus(tagentStatus);
            tagentVo.setDisConnectReason(disConnectReason);
            tagentMapper.updateTagentStatusAndDisConnectReasonById(tagentVo.getStatus(), tagentVo.getDisConnectReason(), tagentVo.getId());
        }
        paramJson.put("status", tagentStatus);
        paramJson.put("disConnectReason", disConnectReason);
        return paramJson;
    }
}
