package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TagentUpdateAppsConfigHandler extends TagentHandlerBase {
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
        return "updateAppsConf";
    }

    @Override
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String url) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("type", message.getName());
        params.put("data", message.getData());
        params.put("ip", tagentVo.getIp());
        params.put("port", (tagentVo.getPort()).toString());
        params.put("credential", tagentVo.getCredential());
//        String httpResult = TagentHttpUtil.post(url + "tagent/in/exec", params, new HashMap<>(), true);
        return null;
    }
}
