package neatlogic.module.tagent.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentMessageVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.enums.TagentAction;
import neatlogic.framework.tagent.exception.TagentActionNotFoundException;
import neatlogic.framework.tagent.exception.TagentIdNotFoundException;
import neatlogic.framework.tagent.tagenthandler.core.ITagentHandler;
import neatlogic.framework.tagent.tagenthandler.core.TagentHandlerFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentLogsGetApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "查看Tagent日志";
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
    @Description(desc = "查看Tagent日志接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        TagentMessageVo message = JSONObject.toJavaObject(paramObj, TagentMessageVo.class);
        TagentVo tagent = tagentMapper.getTagentById(message.getTagentId());
        if (tagent == null) {
            throw new TagentIdNotFoundException(message.getTagentId());
        }
        ITagentHandler tagentHandler = TagentHandlerFactory.getInstance(TagentAction.GET_LOGS.getValue());
        if (tagentHandler == null) {
            throw new TagentActionNotFoundException(TagentAction.GET_LOGS.getValue());
        }
        RunnerVo runner = runnerMapper.getRunnerById(tagent.getRunnerId());
        JSONArray tbodyList = new JSONArray();
        JSONArray data = tagentHandler.execTagentCmd(message, tagent, runner).getJSONArray("Data");
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
