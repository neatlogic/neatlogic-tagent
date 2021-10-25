/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.asynchronization.threadlocal.UserContext;
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
import codedriver.module.tagent.api.TagentLogDownLoadApi;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Component
public class TagentLogDownloadHandler extends TagentHandlerBase {
    private final static Logger logger = LoggerFactory.getLogger(TagentLogDownLoadApi.class);
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
        return TagentAction.DOWNLOAD_LOG.getValue();
    }

    @Override
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String url) throws Exception {
        JSONObject params = JSONObject.parseObject(JSONObject.toJSONString(message));
        params.put("type", message.getName());
        params.put("ip", tagentVo.getIp());
        params.put("port", (tagentVo.getPort()).toString());
        params.put("credential", tagentVo.getCredential());
        AccountVo accountVo = resourceCenterMapper.getAccountById(tagentVo.getAccountId());
        if (accountVo == null) {
            throw new ResourceCenterAccountNotFoundException();
        }
        params.put("credential", accountVo.getPasswordCipher());
        String result = null;
        url = url + "/api/binary/tagent/log/download";
        try {
            RestVo restVo = new RestVo(url, AuthenticateType.BUILDIN.getValue(), JSONObject.parseObject(JSON.toJSONString(params)));
            result = RestUtil.sendRequest(restVo);
            JSONObject resultJson = JSONObject.parseObject(result);
            if (!resultJson.containsKey("Data")) {
                throw new TagentActionFailedEcexption(restVo.getUrl() + ":" + resultJson.getString("Message"));
            }
            String dataStr = resultJson.getString("Data");
            InputStream inputStream = new ByteArrayInputStream(dataStr.getBytes());
            HttpServletResponse response = UserContext.get().getResponse();
            response.reset();
            response.setHeader("Content-Disposition", " attachment; filename=\"" + message.getPath() + "\"");
            response.setContentType("application/octet-stream");
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            IOUtils.copy(inputStream, outputStream);
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (JSONException ex) {
            throw new TagentRunnerConnectRefusedException(url, result);
        }
        return null;
    }
}
