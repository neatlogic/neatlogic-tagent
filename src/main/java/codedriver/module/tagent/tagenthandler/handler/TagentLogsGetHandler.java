package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.cmdb.dao.mapper.resourcecenter.ResourceCenterMapper;
import codedriver.framework.cmdb.dto.resourcecenter.AccountVo;
import codedriver.framework.cmdb.exception.resourcecenter.ResourceCenterAccountNotFoundException;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.util.RC4Util;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.exception.RunnerGroupIdNotFoundException;
import codedriver.framework.tagent.exception.RunnerOrTagentNotFoundException;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerBase;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.util.TagentHttpUtil;
import codedriver.module.tagent.common.Constants;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.security.auth.login.AccountNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class TagentLogsGetHandler extends TagentHandlerBase {

    @Resource
    ResourceCenterMapper resourceCenterMapper;

    @Override
    public String getName() {
        return "getlogs";
    }

    @Override
    public void myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String url, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("type", TagentAction.GETLOGS.getValue());
        params.put("ip", tagentVo.getIp());
        params.put("port", (tagentVo.getPort()).toString());
        AccountVo accountVo = resourceCenterMapper.getAccountById(tagentVo.getAccountId());
        if (accountVo == null) {
            throw new ResourceCenterAccountNotFoundException();
        }
        params.put("credential", RC4Util.encrypt(Constants.encryptKey,accountVo.getPasswordPlain()));
        String httpResult = TagentHttpUtil.post(url + "tagent/in/exec", params, new HashMap<>(), true);
        if (StringUtils.isBlank(httpResult)) {
            throw new RunnerOrTagentNotFoundException();
        }
        JSONObject result = JSONObject.parseObject(httpResult);
        response.setContentType(Config.RESPONSE_TYPE_JSON);
        response.getWriter().print(result);
    }


    @Override
    public String getHandler() {
        return null;
    }

    @Override
    public String getHandlerName() {
        return null;
    }

}
