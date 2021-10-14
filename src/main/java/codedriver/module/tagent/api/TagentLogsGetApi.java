package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentMessageVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.exception.RunnerNotFoundException;
import codedriver.framework.tagent.exception.TagentActionNotFoundEcexption;
import codedriver.framework.tagent.exception.TagentIdNotFoundException;
import codedriver.framework.tagent.tagenthandler.core.ITagentHandler;
import codedriver.framework.tagent.tagenthandler.core.TagentHandlerFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentLogsGetApi extends PrivateBinaryStreamApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(TagentLogsGetApi.class);

    @Resource
    TagentMapper tagentMapper;

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "Tagent查看日志";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Override
    public String getToken() {
        return "tagent/exec/log/get";
    }

    @Input({
            @Param(name = "tagentId", type = ApiParamType.LONG, isRequired = true, desc = "tagent id")
    })
    @Output({
            @Param(name = "tbodyList", desc = "日志列表")
    })
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        TagentMessageVo message = JSONObject.toJavaObject(paramObj, TagentMessageVo.class);
        JSONArray tbodyList = new JSONArray();
        JSONArray data = null;
        TagentVo tagent = tagentMapper.getTagentById(message.getTagentId());
        if (tagent == null) {
            throw new TagentIdNotFoundException(message.getTagentId());
        }
        RunnerVo runner = runnerMapper.getRunnerById(tagent.getRunnerId());
        if (runner == null) {
            throw new RunnerNotFoundException(tagent.getRunnerId());
        }
        ITagentHandler tagentHandler = TagentHandlerFactory.getInstance(TagentAction.GETLOGS.getValue());
        if (tagentHandler == null) {
            throw new TagentActionNotFoundEcexption(TagentAction.GETLOGS.getValue());
        } else {
            data = tagentHandler.execTagentCmd(message, tagent, runner).getJSONArray("Data");
        }
        if (CollectionUtils.isNotEmpty(data)) {
            for (int i = 0; i < data.size(); i++) {
                JSONObject js = new JSONObject();
                js.put("log", data.getString(i).replaceAll("\n", StringUtils.EMPTY));
                tbodyList.add(js);
            }
        }
        return tbodyList;
    }
}
