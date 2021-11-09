package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.register.core.AfterRegisterJobManager;
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
        TagentVo tagentVo = new TagentVo();
        tagentVo.setStatus("connected");
        List<TagentVo> tagentVoList = tagentMapper.searchTagent(tagentVo);
        if (CollectionUtils.isNotEmpty(tagentVoList)) {
            for (TagentVo tagentVo1 : tagentVoList) {
                AfterRegisterJobManager.executeAll(tagentVo1);
            }
        }
        return null;
    }
}
