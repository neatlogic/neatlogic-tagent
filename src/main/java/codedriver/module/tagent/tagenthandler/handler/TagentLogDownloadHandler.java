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
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.integration.authentication.enums.AuthenticateType;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.exception.TagentActionFailedEcexption;
import codedriver.framework.tagent.exception.TagentRunnerConnectRefusedException;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerBase;
import codedriver.framework.util.RestUtil;
import codedriver.module.tagent.api.TagentLogDownLoadApi;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;

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
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, RunnerVo runnerVo) throws Exception {
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
        ServletOutputStream outputStream = null;
        InputStream inputStream = null;
        String url = runnerVo.getUrl() + "/api/binary/tagent/log/download";
        try {
            RestVo restVo = new RestVo.Builder(url, AuthenticateType.BUILDIN.getValue()).setPayload(params).build();
            result = RestUtil.sendPostRequest(restVo);
            JSONObject resultJson = JSONObject.parseObject(result);
            if (!resultJson.containsKey("Data")) {
                throw new TagentActionFailedEcexption(runnerVo, resultJson.getString("Message"));
            }
            String dataStr = resultJson.getString("Data");
            inputStream = new ByteArrayInputStream(dataStr.getBytes());
            HttpServletResponse response = UserContext.get().getResponse();
            response.setHeader("Content-Disposition", " attachment; filename=\"" + URLEncoder.encode(message.getPath(), "UTF-8") + "\"");
            response.setContentType("application/octet-stream");
            outputStream = response.getOutputStream();
            IOUtils.copyLarge(inputStream, outputStream);
            outputStream.flush();
        } catch (JSONException ex) {
            throw new TagentRunnerConnectRefusedException(url, result);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return null;
    }
}
