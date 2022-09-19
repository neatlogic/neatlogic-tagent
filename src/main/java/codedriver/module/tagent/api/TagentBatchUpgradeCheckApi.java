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
import codedriver.framework.tagent.service.TagentService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentBatchUpgradeCheckApi extends PrivateApiComponentBase {

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "检测批量升级tagent";
    }

    @Override
    public String getToken() {
        return "tagent/batch/action/check";
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
    @Description(desc = "批量升级前筛选出对应的tagent信息,来源于ip：port、网段掩码、代理组三个地方")
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
        return tagentList.size();
    }

}
