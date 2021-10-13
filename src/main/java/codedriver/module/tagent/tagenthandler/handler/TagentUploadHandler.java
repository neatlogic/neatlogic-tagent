package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.common.config.Config;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerBase;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.util.TagentHttpUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class TagentUploadHandler extends TagentHandlerBase {
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
        return "upload";
    }

    @Override
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String url) throws Exception {
//        Map<String, String> params = new HashMap<>();
//        Map<String, String> header = new HashMap<>();
//        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
//        MultipartFile multipartFile = multipartRequest.getFile("upfile");
//        String fileName = multipartFile.getOriginalFilename();
//        InputStream input = multipartFile.getInputStream();
//        BufferedReader in = new BufferedReader(new InputStreamReader(input));
//        StringBuffer buffer = new StringBuffer();
//        String line = "";
//        while ((line = in.readLine()) != null) {
//            buffer.append(line);
//        }
//        in.close();
//        params.put("type", message.getName());
//        params.put("ip", tagentVo.getIp());
//        params.put("port", (tagentVo.getPort()).toString());
//        params.put("credential", tagentVo.getCredential());
//        params.put("fileName", fileName);
//        params.put("fileData", buffer.toString());
//        String result = TagentHttpUtil.post(url + "tagent/in/exec", params, header, true);
//        response.setContentType(Config.RESPONSE_TYPE_JSON);
//        response.getWriter().print(result);
        return null;
    }
}
