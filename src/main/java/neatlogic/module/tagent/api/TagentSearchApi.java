package neatlogic.module.tagent.api;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerGroupVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.service.TagentService;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentSearchApi extends PrivateApiComponentBase {
    @Resource
    RunnerMapper runnerMapper;

    @Resource
    TagentService tagentService;

    @Override
    public String getToken() {
        return "tagent/search";
    }

    @Override
    public String getName() {
        return "查询tagent列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "查询tagent列表接口")
    @Input({
            @Param(name = "osId", type = ApiParamType.LONG, desc = "OS类型id"),
            @Param(name = "version", type = ApiParamType.STRING, desc = "tagent版本"),
            @Param(name = "status", type = ApiParamType.STRING, desc = "tagent状态"),
            @Param(name = "runnerGroupId", type = ApiParamType.LONG, desc = "runner组id"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键词"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "tbodyList", explode = TagentVo[].class, desc = "tagent列表")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        TagentVo tagentVo = JSON.toJavaObject(jsonObj, TagentVo.class);
        List<TagentVo> tagentMGList = new ArrayList<>();
        long rowNum = tagentService.getTagentListMGCount(tagentVo);
        if (rowNum > 0) {
            tagentVo.setRowNum((int) rowNum);
            tagentMGList = tagentService.searchTagentListMG(tagentVo);
            if (CollectionUtils.isNotEmpty(tagentMGList)) {
                Set<Long> runnerGroupIdSet = tagentMGList.stream().filter(Objects::nonNull).map(TagentVo::getRunnerGroupId).filter(Objects::nonNull).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(runnerGroupIdSet)) {
                    List<RunnerGroupVo> runnerGroupVos = runnerMapper.getRunnerGroupByIdList(new ArrayList<>(runnerGroupIdSet));
                    if (CollectionUtils.isNotEmpty(runnerGroupVos)) {
                        Map<Long, String> runnerGroupIdNameMap = runnerGroupVos.stream().collect(Collectors.toMap(RunnerGroupVo::getId, RunnerGroupVo::getName));
                        for (TagentVo tagent : tagentMGList) {
                            tagent.setRunnerGroupName(runnerGroupIdNameMap.get(tagent.getRunnerGroupId()));
                        }
                    }
                }
            }
        }
        return TableResultUtil.getResult(tagentMGList, tagentVo);
    }
}
