package neatlogic.module.tagent.tagenthandler.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.runner.RunnerHttpRequestException;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.enums.TagentAction;
import neatlogic.framework.tagent.enums.TagentStatus;
import neatlogic.framework.tagent.tagenthandler.core.TagentHandlerBase;
import neatlogic.framework.util.HttpRequestUtil;
import org.apache.commons.lang3.StringUtils;
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
        JSONObject resultJson = null;
        String disConnectReason = "";
        try {
            HttpRequestUtil requestUtil = HttpRequestUtil.post(url).setAuthType(AuthenticateType.BUILDIN).setPayload(paramJson.toJSONString()).sendRequest();
            if (StringUtils.isNotBlank(requestUtil.getError())) {
                throw new RunnerHttpRequestException(url + ":" + requestUtil.getError());
            }
            resultJson = requestUtil.getResultJson();
            if (resultJson == null) {
                tagentStatus = TagentStatus.DISCONNECTED.getValue();
            } else if (!resultJson.containsKey("Status") || !"OK".equals(resultJson.getString("Status"))) {
                tagentStatus = TagentStatus.DISCONNECTED.getValue();
                disConnectReason = resultJson.getString("Message");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            tagentStatus = TagentStatus.DISCONNECTED.getValue();
            disConnectReason = "runner返回：" + (resultJson != null ? resultJson.toString() : null);
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
