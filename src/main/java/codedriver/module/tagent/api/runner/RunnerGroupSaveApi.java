package codedriver.module.tagent.api.runner;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.GroupNetworkVo;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.exception.RunnerGroupIdNotFoundException;
import codedriver.framework.tagent.exception.RunnerGroupNetworkSameException;
import codedriver.framework.tagent.exception.RunnerGroupNetworkNameRepeatsException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class RunnerGroupSaveApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "Tagent代理组保存接口";
    }

    @Override
    public String getToken() {
        return "tagent/runnergroup/save";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = false, desc = "id"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = false, desc = "runner 分组名"),
            @Param(name = "groupNetworkList", type = ApiParamType.JSONARRAY, isRequired = false, desc = "runner 分组网段列表"),
    })
    @Output({
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RunnerGroupVo runnerGroupVo = JSONObject.toJavaObject(paramObj, RunnerGroupVo.class);
        Long id = paramObj.getLong("id");
        String name = paramObj.getString("name");
        List<GroupNetworkVo> groupNetworkList = runnerGroupVo.getGroupNetworkList();
        if (id != null) {
            if (runnerMapper.checkRunnerGroupIdIsExist(id) == 0) {
                throw new RunnerGroupIdNotFoundException(id);
            }
            if (!CollectionUtils.isEmpty(groupNetworkList)) {
                String checkIpMask = groupNetworkList.get(0).getNetworkIp() + ":" + groupNetworkList.get(0).getMask();
                for (int i = 1; i < groupNetworkList.size(); i++) {
                    if (checkIpMask.equals(groupNetworkList.get(i).getNetworkIp() + ":" + groupNetworkList.get(i).getMask())) {
                        throw new RunnerGroupNetworkSameException(checkIpMask);//TODO 前端提示不准确，192.168.0.0/24和192.168.0.1/24实际上是同一个网段
                    }
                }
            }
            if (runnerMapper.checkGroupNameIsRepeats(runnerGroupVo) > 0) {
                throw new RunnerGroupNetworkNameRepeatsException(name);
            }
            runnerMapper.updateRunnerGroup(runnerGroupVo);
        } else {
            runnerMapper.insertRunnerGroup(runnerGroupVo);
        }
        runnerMapper.deleteGroupNetWork(runnerGroupVo.getId());
        if (groupNetworkList != null && groupNetworkList.size() > 0) {
            for (GroupNetworkVo networkVo : groupNetworkList) {
                networkVo.setGroupId(runnerGroupVo.getId());
                runnerMapper.insertNetwork(networkVo);
            }
        }


        return null;
    }


}
