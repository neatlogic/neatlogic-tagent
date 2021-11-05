package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.IpUtil;
import codedriver.framework.dto.runner.NetworkVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.exception.TagentBatchUpgradeCheckLessTagentIpAndPort;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentBatchUpgradeCheckApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "检测批量升级tagent";
    }

    @Override
    public String getToken() {
        return "tagent/batch/upgrade/check";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "tagentIpAndPort", type = ApiParamType.STRING, desc = "ip:port,多个tagent使用英文”，“分隔"),
            @Param(name = "networkVoList", type = ApiParamType.JSONARRAY, desc = "网段")
    })
    @Description(desc = "批量升级前筛选出对应的tagent信息")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {

        String tagentIpAndPort = paramObj.getString("tagentIpAndPort");
        JSONArray networkVoArray = paramObj.getJSONArray("networkVoList");
        List<NetworkVo> networkVoList = null;
        if (CollectionUtils.isNotEmpty(networkVoArray)) {
            networkVoList = networkVoArray.toJavaList(NetworkVo.class);
        }
        if (StringUtils.isBlank(tagentIpAndPort) && CollectionUtils.isEmpty(networkVoList)) {
            throw new TagentBatchUpgradeCheckLessTagentIpAndPort();
        }

        List<TagentVo> tagentVoList = new ArrayList<>();
        List<Long> tagentIdList = new ArrayList<>();

        if (StringUtils.isNotBlank(tagentIpAndPort)) {
            List<String> ipList = new ArrayList<>();
            List<String> portList = new ArrayList<>();
            String[] tagentArray = tagentIpAndPort.split(",");

            for (String ipAndPortString : tagentArray) {
                String ipAndPort[] = ipAndPortString.split(":");
                if (ipAndPort.length == 2) {
                    ipList.add(ipAndPort[0]);
                    portList.add(ipAndPort[1]);
                }
            }
            if (CollectionUtils.isNotEmpty(ipList) && CollectionUtils.isNotEmpty(portList) && ipList.size() == portList.size()) {
                for (int i = 0; i < ipList.size(); i++) {
                    TagentVo tagentVo = tagentMapper.getTagentByIpAndPort(ipList.get(i), Integer.valueOf(portList.get(i)));
                    if (tagentVo != null) {
                        tagentVoList.add(tagentVo);
                        tagentIdList.add(tagentVo.getId());

                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(networkVoList)) {
            List<TagentVo> searchTagentList = tagentMapper.searchTagent(new TagentVo());
            for (int i = 0; i < searchTagentList.size(); i++) {
                for (int j = 0; j < networkVoList.size(); j++) {
                    TagentVo tagentVo = searchTagentList.get(i);
                    if (IpUtil.isBelongSegment(tagentVo.getIp(), networkVoList.get(j).getNetworkIp(), networkVoList.get(j).getMask()) && !tagentIdList.contains(tagentVo.getId())) {
                        tagentVoList.add(tagentVo);
                        tagentIdList.add(tagentVo.getId());
                    }
                }
            }
        }


        if (CollectionUtils.isEmpty(tagentVoList)) {
            throw new TagentBatchUpgradeCheckLessTagentIpAndPort();
        }
        return tagentVoList;
    }

}
