package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.RestVo;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.integration.authentication.enums.AuthenticateType;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.enums.TagentStatus;
import codedriver.framework.tagent.service.TagentService;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerBase;
import codedriver.framework.util.RestUtil;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TagentStatusCheckHandler extends TagentHandlerBase {

    Logger logger = LoggerFactory.getLogger(TagentStatusCheckHandler.class);

    @Autowired
    private TagentService tagentService;

    @Autowired
    private RunnerMapper runnerMapper;


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
        return "tagentStatusCheck";
    }

    @Override
    public JSONObject myExecTagentCmd(TagentMessageVo message, TagentVo tagentVo, String url) throws Exception {
        String tagentStatus = TagentStatus.DISCONNECTED.getValue();
        JSONObject paramJson = new JSONObject();
        paramJson.put("ip", tagentVo.getIp());
        paramJson.put("port", (tagentVo.getPort()).toString());
        paramJson.put("type", message.getName());
        Long groupId = tagentVo.getRunnerGroupId();
        RunnerGroupVo groupVo = runnerMapper.getRunnerGroupById(groupId);
        if (groupVo != null) {
            List<RunnerVo> runnerVoList = groupVo.getRunnerList();
            if (CollectionUtils.isNotEmpty(runnerVoList)) {
                for (RunnerVo runnerItem : runnerVoList) {
                    try {
                        RestVo restVo = new RestVo(runnerItem.getUrl() + "api/rest/tagent/status/check", AuthenticateType.BUILDIN.getValue(), paramJson);
                        String result = RestUtil.sendRequest(restVo);
                        JSONObject resultJson = JSONObject.parseObject(result);
                        if (resultJson.containsKey("Status") && "OK".equals(resultJson.getString("Status"))) {
                            tagentStatus = TagentStatus.CONNECTED.getValue();
                        }
                    } catch (JSONException ignored) {
                    }
                }
            }
        }
        paramJson.put("status", tagentStatus);
        TagentVo tagent = new TagentVo();
        tagent.setId(tagentVo.getId());
        tagent.setStatus(tagentStatus);
        tagentService.updateTagentById(tagent);
        return paramJson;
    }
}
