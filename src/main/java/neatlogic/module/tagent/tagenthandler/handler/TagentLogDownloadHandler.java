/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tagent.tagenthandler.handler;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountBaseVo;
import neatlogic.framework.dto.RestVo;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.enums.TagentAction;
import neatlogic.framework.tagent.exception.TagentAccountNotFoundException;
import neatlogic.framework.tagent.exception.TagentActionFailedException;
import neatlogic.framework.tagent.exception.TagentRunnerConnectRefusedException;
import neatlogic.framework.tagent.tagenthandler.core.TagentHandlerBase;
import neatlogic.framework.util.RestUtil;
import neatlogic.module.tagent.api.TagentLogDownLoadApi;
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
    TagentMapper tagentMapper;

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
//        IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
        //验证tagent对应的账号是否存在，以便后续从该账号获取对应密文
        AccountBaseVo accountVo = tagentMapper.getAccountById(tagentVo.getAccountId());
        if (accountVo == null) {
            throw new TagentAccountNotFoundException(tagentVo.getAccountId());
        }
        JSONObject params = JSONObject.parseObject(JSONObject.toJSONString(message));
        params.put("type", message.getName());
        params.put("ip", tagentVo.getIp());
        params.put("port", (tagentVo.getPort()).toString());
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
                throw new TagentActionFailedException(runnerVo, resultJson.getString("Message"));
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
