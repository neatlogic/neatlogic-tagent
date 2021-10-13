package codedriver.module.tagent.tagenthandler.handler;

import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.enums.TagentStatus;
import codedriver.framework.tagent.service.TagentService;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerBase;
import codedriver.framework.tagent.util.TagentHttpUtil;
import codedriver.module.tagent.common.Constants;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TagentRefreshStatusHandler extends TagentHandlerBase {

    Logger logger = LoggerFactory.getLogger(TagentRefreshStatusHandler.class);

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

        JSONObject result = new JSONObject();
        boolean hasConnect = false;

        try {
            Map<String, String> header = new HashMap<>();
            Map<String, String> params = new HashMap<>();
            params.put("ip", tagentVo.getIp());
            params.put("port", (tagentVo.getPort()).toString());
            params.put("type", message.getName());

            Long groupId = tagentVo.getRunnerGroupId();
            RunnerGroupVo groupVo = runnerMapper.getRunnerGroupById(groupId);
            if (groupVo != null) {
                List<RunnerVo> runnerVoList = groupVo.getRunnerList();
                if (runnerVoList != null && runnerVoList.size() > 0) {
                    for (RunnerVo runnerItem : runnerVoList) {
                        try {
                            String checkResult = TagentHttpUtil.post(runnerItem.getUrl() + "/tagent/in/exec", params, header, true);
                            if (StringUtils.isNotBlank(checkResult)) {
                                JSONObject checkObj = JSONObject.parseObject(checkResult);
                                if ("OK".equals(checkObj.getString("Status"))) {
                                    hasConnect = true;
                                }
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                        }
                    }
                }
            }

            result.put("status", hasConnect ? TagentStatus.CONNECTED.getValue() : TagentStatus.DISCONNECTED.getValue());
            result.put("message", hasConnect ? "get active channel" : "can not get active channel");
        } catch (Exception e) {
            logger.error("get tagent status failed ," + e.getMessage());
            result.put("status", "undef");
            result.put("message", "get status failed, " + e.getMessage());
        }
        TagentVo tagent = new TagentVo();
        tagent.setId(tagentVo.getId());
        tagent.setStatus(hasConnect ? TagentStatus.CONNECTED.getValue() : TagentStatus.DISCONNECTED.getValue());
        tagentService.updateTagentById(tagent);
//        response.setContentType(Config.RESPONSE_TYPE_JSON);
//        response.getWriter().print(result);
        return null;
    }
}
