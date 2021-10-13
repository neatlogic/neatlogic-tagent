package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.common.config.Config;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerBase;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.util.TagentHttpUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class TagentResetPasswordHandler extends TagentHandlerBase {
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
        return "resetpwd";
    }

    @Override
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String url) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("type", message.getName());
        params.put("ip", tagentVo.getIp());
        params.put("port", (tagentVo.getPort()).toString());
        params.put("credential", tagentVo.getCredential());
        String httpResult = TagentHttpUtil.post(url + "tagent/in/exec", params, new HashMap<>(), true);
        return null;
    }
}
