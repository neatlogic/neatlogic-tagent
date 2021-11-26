package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.dto.RestVo;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.integration.authentication.enums.AuthenticateType;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.enums.TagentStatus;
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
        String tagentStatus = TagentStatus.DISCONNECTED.getValue();
        JSONObject paramJson = new JSONObject();
        paramJson.put("ip", tagentVo.getIp());
        paramJson.put("port", (tagentVo.getPort()).toString());
        paramJson.put("type", message.getName());
        String url = runnerVo.getUrl() + "api/rest/tagent/status/check";
        String result = null;
        try {
            RestVo restVo = new RestVo.Builder(url, AuthenticateType.BUILDIN.getValue()).setPayload(paramJson).build();
            result = RestUtil.sendPostRequest(restVo);
            JSONObject resultJson = JSONObject.parseObject(result);
            if (!resultJson.containsKey("Status") || !"OK".equals(resultJson.getString("Status"))) {
                tagentStatus = TagentStatus.DISCONNECTED.getValue();
                tagentVo.setDisConnectReasion("心跳不通");
            } else {
                tagentVo.setDisConnectReasion("");
            }
        } catch (JSONException ex) {
            tagentVo.setStatus(TagentStatus.DISCONNECTED.getValue());
            tagentVo.setDisConnectReasion("心跳不通");
        }finally {
            tagentMapper.updateTagent(tagentVo);
        }
        paramJson.put("status", tagentStatus);
        TagentVo tagent = new TagentVo();
        tagent.setId(tagentVo.getId());
        tagent.setStatus(tagentStatus);
        tagentService.updateTagentById(tagent);
        return paramJson;
    }
}
