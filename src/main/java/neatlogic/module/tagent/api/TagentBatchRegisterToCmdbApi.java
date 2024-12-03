package neatlogic.module.tagent.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.register.core.AfterRegisterJobManager;
import neatlogic.framework.tagent.service.TagentService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Transactional
@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class TagentBatchRegisterToCmdbApi extends PrivateApiComponentBase {

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "批量同步已注册的tagent信息到CMDB";
    }

    @Override
    public String getToken() {
        return "tagent/batch/cmdb/input";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "批量同步已注册的tagent信息到CMDB接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TagentVo tagentVo = new TagentVo();
        long rowNum = tagentService.getTagentListMGCount(tagentVo);
        if (rowNum > 0) {
            tagentVo.setPageSize(100);
            tagentVo.setRowNum((int)rowNum);
            for (int i = 0; i < tagentVo.getPageCount(); i++) {
                tagentVo.setCurrentPage(i + 1);
                List<TagentVo> tagentList = tagentService.searchTagentListMG(tagentVo);
                if (CollectionUtils.isNotEmpty(tagentList)) {
                    for (TagentVo tagent : tagentList) {
                        AfterRegisterJobManager.executeAll(tagent);
                    }
                }
            }
        }
        return null;
    }
}
