/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
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

@Component
public class TagentConfigSaveHandler extends TagentHandlerBase {

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
        return TagentAction.SAVE_CONFIG.getValue();
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
        paramJson.put("type", TagentAction.SAVE_CONFIG.getValue());
        paramJson.put("ip", tagentVo.getIp());
        paramJson.put("data", message.getData());
        paramJson.put("port", (tagentVo.getPort()).toString());
        paramJson.put("credential", accountVo.getPasswordCipher());
        String url = runnerVo.getUrl() + "api/rest/tagent/config/save";
        String result = null;
        try {
            RestVo restVo = new RestVo.Builder(url, AuthenticateType.BUILDIN.getValue()).setPayload(paramJson).build();
            result = RestUtil.sendPostRequest(restVo);
            JSONObject resultJson = JSONObject.parseObject(result);
            if (!resultJson.containsKey("Status") || !"OK".equals(resultJson.getString("Status"))) {
                throw new TagentActionFailedException(runnerVo, resultJson.getString("Message"));
            }
        } catch (JSONException ex) {
            throw new TagentRunnerConnectRefusedException(url, result);
        }
        return null;
    }
}
