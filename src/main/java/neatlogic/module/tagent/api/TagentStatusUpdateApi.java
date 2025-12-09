package neatlogic.module.tagent.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.systemuser.SystemUser;
import neatlogic.framework.common.util.IpUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_MANAGE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.exception.TagentNotFoundException;
import neatlogic.framework.tagent.service.TagentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;


@Service
@AuthUser(SystemUser.AUTOEXEC)
@AuthAction(action = TAGENT_MANAGE.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class TagentStatusUpdateApi extends PrivateApiComponentBase {

    private final Logger logger = LoggerFactory.getLogger(TagentStatusUpdateApi.class);

    @Resource
    TagentMapper tagentMapper;

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "更新tagent状态";
    }

    @Override
    public String getToken() {
        return "tagent/status/update";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "ip", type = ApiParamType.STRING, desc = "tagentIP"),
            @Param(name = "port", type = ApiParamType.INTEGER, desc = "tagent端口"),
            @Param(name = "status", type = ApiParamType.ENUM, rule = "disconnected,connected", isRequired = true, desc = "tagent状态"),
            @Param(name = "runnerId", type = ApiParamType.STRING, desc = "runner id"),
            @Param(name = "runnerGroupId", type = ApiParamType.LONG, desc = "runner组id"),
    }
    )
    @Output({})
    @Description(desc = "Tagent状态更新接口,用于tagent<->runner心跳更新tagent状态")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject result = new JSONObject();
        boolean status = true;
        String returnData = null;
        try {
            paramObj.put("runnerIp", IpUtil.getIpAddr(UserContext.get().getRequest()));
            TagentVo tagentPram = JSON.toJavaObject(paramObj, TagentVo.class);
            TagentVo tagentVo = tagentMapper.getTagentByIpAndPort(tagentPram.getIp(), tagentPram.getPort());
            TagentVo tagentMGVo = tagentService.getTagentMGByIpAndPort(tagentPram.getIp(), tagentPram.getPort());
            if (tagentVo == null) {
                //存在历史垃圾数据,则删除
                if (tagentMGVo != null) {
                    tagentService.deleteTagentMGById(tagentPram.getId());
                    tagentService.deleteTagentMGByIpPort(tagentPram.getIp(), tagentPram.getPort());
                }
                throw new TagentNotFoundException(tagentPram.getIp(), tagentPram.getPort());
            }

            if(tagentMGVo != null && !Objects.equals(tagentVo.getId(), tagentMGVo.getId())) {
                tagentService.deleteTagentMGById(tagentMGVo.getId());
                tagentMGVo = null;
            }

            if (tagentMGVo == null) {
                //历史数据,需补充同步到mongodb
                tagentService.updateTagentMGByIdWithLock(tagentVo, true);
            }
            tagentPram.setId(tagentVo.getId());
            tagentService.updateTagentMGByIdWithLock(tagentPram,false);
        } catch (Exception e) {
            status = false;
            logger.error(e.getMessage(), e);
            returnData = e.getMessage();
        }
        result.put("Status", status ? "OK" : "ERROR");
        result.put("Data", status ? paramObj : returnData);
        return result;
    }
}
