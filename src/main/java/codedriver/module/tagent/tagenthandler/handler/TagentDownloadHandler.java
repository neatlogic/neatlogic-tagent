package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TagentDownloadHandler extends TagentHandlerBase {
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
        return "download";
    }

    @Override
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String url) throws Exception {
        JSONObject params =  JSONObject.parseObject(JSONObject.toJSONString(message));
        Map<String, String> header = new HashMap<>();
        params.put("type", message.getName());
        params.put("ip", tagentVo.getIp());
        params.put("port", (tagentVo.getPort()).toString());
        params.put("credential", tagentVo.getCredential());
//        String httpResult = TagentHttpUtil.post(url + "tagent/in/exec", params, header, true);
//        Object httpResultObj = new JSONTokener(httpResult).nextValue();
//        String dataStr = "";
//        if (httpResultObj instanceof JSONObject) {
//            dataStr = ((JSONObject) httpResultObj).getString("Data");
//        } else {
//            dataStr = httpResult;
//        }
//        InputStream inputStream = new ByteArrayInputStream(dataStr.getBytes());
//        response.reset();
//        response.setHeader("Content-Disposition", "attachment;filename=" + message.getPath());
//        response.setContentType("application/octet-stream");
//        //response.addHeader("Content-Length", "" + data.length);
//        OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
//        IOUtils.copy(inputStream, outputStream);
//        outputStream.flush();
//        outputStream.close();
//        inputStream.close();
        return null;
    }
}
