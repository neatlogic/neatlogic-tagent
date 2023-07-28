/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package neatlogic.module.tagent.tagenthandler.handler;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author longrf
 * @date 2022/11/3 16:08
 */

@Component
public class SaveTagentConfigBatchHandler  extends TagentHandlerBase {

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
        return TagentAction.BATCH_SAVE_CONFIG.getValue();
    }

    @Override
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, RunnerVo runnerVo) throws Exception {
//        IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
        //验证tagent对应的账号是否存在，以便后续从该账号获取对应密文
        AccountBaseVo accountVo = tagentMapper.getAccountById(tagentVo.getAccountId());
        if (accountVo == null) {
            throw new TagentAccountNotFoundException(tagentVo.getAccountId());
        }
        JSONObject paramJson = new JSONObject();
        paramJson.put("type", TagentAction.GET_CONFIG.getValue());
        paramJson.put("ip", tagentVo.getIp());
        paramJson.put("port", (tagentVo.getPort()).toString());
        paramJson.put("credential", accountVo.getPasswordCipher());
        paramJson.put("configKeyValueString", message.getData());
        String url = runnerVo.getUrl() + "api/rest/tagent/config/batch/save";
        String result = null;
        try {
            RestVo restVo = new RestVo.Builder(url, AuthenticateType.BUILDIN.getValue()).setPayload(paramJson).build();
            result = RestUtil.sendPostRequest(restVo);
            JSONObject resultJson = JSONObject.parseObject(result);
            if (!resultJson.containsKey("Status") || !"OK".equals(resultJson.getString("Status"))) {
                throw new TagentActionFailedException(runnerVo, resultJson.getString("Message"));
            }
            return resultJson.getJSONObject("Return");
        } catch (JSONException ex) {
            throw new TagentRunnerConnectRefusedException(url, result);
        }
    }
}
