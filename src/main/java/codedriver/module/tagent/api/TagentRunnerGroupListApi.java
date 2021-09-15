package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentRunnerGroupListApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getToken() {
        return "tagent/runnergroup/list";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "获取所有tagent代理组")
    @Input({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "tbodyList",explode = RunnerGroupVo[].class,desc = "tagent代理组列表")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TagentVo tagentVo =JSONObject.toJavaObject(paramObj, TagentVo.class);
        int rowNum =tagentMapper.searchTagentRunnerCount();
        tagentVo.setRowNum(rowNum);
        List<RunnerGroupVo> runnerGroupVoList =tagentMapper.searchTagentRunnerGroup();
        return TableResultUtil.getResult(runnerGroupVoList, tagentVo);
    }


}
