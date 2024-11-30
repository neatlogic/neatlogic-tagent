package neatlogic.module.tagent.api;


import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.service.TagentService;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentSearchApi extends PrivateApiComponentBase {
    @Resource
    TagentMapper tagentMapper;

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
        TagentVo tagentVo = JSONObject.toJavaObject(jsonObj, TagentVo.class);
        List<TagentVo> returnTagentList = new ArrayList<>();
        int rowNum = tagentMapper.searchTagentCount(tagentVo);
        if (rowNum > 0) {
            tagentVo.setRowNum(rowNum);
            returnTagentList = tagentMapper.searchTagent(tagentVo);
            if(CollectionUtils.isNotEmpty(returnTagentList)){
                List<TagentVo> tagentMGList = tagentService.getTagentListMGByTagentIds(returnTagentList.stream().map(TagentVo::getId).collect(Collectors.toList()));
                if(CollectionUtils.isNotEmpty(tagentMGList)){
                    Map<Long,TagentVo> tagentMGMap = tagentMGList.stream().collect(Collectors.toMap(TagentVo::getId, e -> e));
                            returnTagentList.forEach(tagent -> {
                        if(tagentMGMap.containsKey(tagent.getId())){
                            TagentVo tagentMG = tagentMGMap.get(tagent.getId());
                            tagent.setIp(tagentMG.getIp());
                            tagent.setVersion(tagentMG.getVersion());
                            tagent.setRunnerId(tagentMG.getRunnerId());
                            tagent.setRunnerPort(tagentMG.getRunnerPort());
                            tagent.setRunnerIp(tagentMG.getRunnerIp());
                            tagent.setRunnerGroupId(tagentMG.getRunnerGroupId());
                            if(tagentMG.getOsId() != null) {
                                tagent.setOsId(tagentMG.getOsId());
                            }
                            tagent.setStatus(tagentMG.getStatus());
                            tagent.setPcpu(tagentMG.getPcpu());
                            tagent.setMem(tagentMG.getMem());
                            tagent.setDisConnectReason(tagentMG.getDisConnectReason());
                            tagent.setLcd(tagentMG.getLcd());
                        }
                    });
                }
            }
        }

        return TableResultUtil.getResult(returnTagentList, tagentVo);
    }
}
