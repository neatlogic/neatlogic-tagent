package neatlogic.module.tagent.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.register.core.AfterRegisterJobManager;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
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
    TagentMapper tagentMapper;

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
        TagentVo paramTagentVo = new TagentVo();
        paramTagentVo.setStatus("connected");
        List<TagentVo> tagentVoList = tagentMapper.searchTagent(paramTagentVo);
        if (CollectionUtils.isNotEmpty(tagentVoList)) {
            for (TagentVo tagentVo : tagentVoList) {
                AfterRegisterJobManager.executeAll(tagentVo);
            }
        }
        return null;
    }
}
