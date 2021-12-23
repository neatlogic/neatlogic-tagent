package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.cmdb.dto.resourcecenter.IpVo;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.IpUtil;
import codedriver.framework.common.util.PageUtil;
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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

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
            @Param(name = "ipPortList", type = ApiParamType.JSONARRAY, desc = "ip,port列表"),
            @Param(name = "networkVoList", type = ApiParamType.JSONARRAY, desc = "网段")
    })
    @Description(desc = "批量升级前筛选出对应的tagent信息,来源于ip：port和网段掩码两个地方")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {

        JSONArray ipPortArray = paramObj.getJSONArray("ipPortList");
        JSONArray networkVoArray = paramObj.getJSONArray("networkVoList");
        List<NetworkVo> networkVoList = null;
        List<IpVo> ipPortList = null;
        if (CollectionUtils.isNotEmpty(networkVoArray)) {
            networkVoList = networkVoArray.toJavaList(NetworkVo.class);
        }
        if (CollectionUtils.isNotEmpty(ipPortArray)) {
            ipPortList = ipPortArray.toJavaList(IpVo.class);
        }
        if (CollectionUtils.isEmpty(ipPortList) && CollectionUtils.isEmpty(networkVoList)) {
            throw new TagentBatchUpgradeCheckLessTagentIpAndPort();
        }

        List<TagentVo> tagentVoList = new ArrayList<>();

        //ip：port
        if (CollectionUtils.isNotEmpty(ipPortList)) {
            for (IpVo ipVo : ipPortList) {
                TagentVo tagentVo = tagentMapper.getTagentByIpAndPort(ipVo.getIp(), ipVo.getPort());
                if (tagentVo == null) {
                    continue;
                }
                tagentVoList.add(tagentVo);
            }
        }

        //网段掩码
        if (CollectionUtils.isNotEmpty(networkVoList)) {
            List<TagentVo> searchTagentList = new ArrayList<>();
            TagentVo tagentVo = new TagentVo();
            int tagentCount = tagentMapper.searchTagentCount(tagentVo);
            tagentVo.setPageSize(100);
            if (tagentCount > 100) {
                int pageCount = PageUtil.getPageCount(tagentCount, 100);
                for (int i = 1; i <= pageCount; i++) {
                    tagentVo.setCurrentPage(i);
                    searchTagentList.addAll(tagentMapper.searchTagent(tagentVo));
                }
            } else {
                searchTagentList = tagentMapper.searchTagent(tagentVo);
            }
            for (TagentVo tagent : searchTagentList) {
                for (NetworkVo networkVo : networkVoList) {
                    if (IpUtil.isBelongSegment(tagent.getIp(), networkVo.getNetworkIp(), networkVo.getMask())) {
                        tagentVoList.add(tagent);
                    }
                }
            }
        }
        List<TagentVo> returnList = tagentVoList.stream().collect(collectingAndThen((toCollection(() -> new TreeSet<>(Comparator.comparing(p -> p.getIp())))), ArrayList::new));

        if (CollectionUtils.isEmpty(returnList)) {
            throw new TagentBatchUpgradeCheckLessTagentIpAndPort();
        }
        return returnList;
    }

}
