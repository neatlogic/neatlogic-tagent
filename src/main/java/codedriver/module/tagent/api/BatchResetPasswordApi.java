/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */
package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.cmdb.dto.resourcecenter.IpVo;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.runner.NetworkVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.enums.TagentAction;
import codedriver.framework.tagent.service.TagentService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author longrf
 * @date 2022/9/19 11:09
 */

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class BatchResetPasswordApi extends PrivateApiComponentBase {

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "批量重置tagent密码";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "ipPortList", type = ApiParamType.JSONARRAY, desc = "ip,port列表"),
            @Param(name = "networkVoList", type = ApiParamType.JSONARRAY, desc = "网段"),
            @Param(name = "runnerGroupIdList", type = ApiParamType.JSONARRAY, desc = "代理组id")
    })
    @Description(desc = "批量重置tagent密码")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONArray ipPortArray = paramObj.getJSONArray("ipPortList");
        JSONArray networkVoArray = paramObj.getJSONArray("networkVoList");
        JSONArray runnerGroupIdArray = paramObj.getJSONArray("runnerGroupIdList");

        List<NetworkVo> networkVoList = null;
        List<IpVo> ipPortList = null;
        List<Long> runnerGroupIdList = null;
        if (CollectionUtils.isNotEmpty(networkVoArray)) {
            networkVoList = networkVoArray.toJavaList(NetworkVo.class);
        }
        if (CollectionUtils.isNotEmpty(ipPortArray)) {
            ipPortList = ipPortArray.toJavaList(IpVo.class);
        }
        if (CollectionUtils.isNotEmpty(runnerGroupIdArray)) {
            runnerGroupIdList = runnerGroupIdArray.toJavaList(Long.class);
        }
        List<TagentVo> tagentList = tagentService.getTagentList(ipPortList, networkVoList, runnerGroupIdList);
        if (CollectionUtils.isEmpty(tagentList)) {
            return null;
        }
        return tagentService.batchExecTagentChannelAction(TagentAction.RESET_CREDENTIAL, tagentList);
    }

    @Override
    public String getToken() {
        return "tagent/exec/password/batch/reset";
    }
}
