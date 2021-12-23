package codedriver.module.tagent.api;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
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
import codedriver.framework.tagent.dto.TagentUpgradeAuditVo;
import codedriver.framework.tagent.dto.TagentVersionVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.exception.TagentBatchUpgradeCheckLessTagentIpAndPort;
import codedriver.framework.tagent.service.TagentService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Service
@Transactional
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class TagentBatchUpgradeApi extends PrivateApiComponentBase {

    static final Logger logger = LoggerFactory.getLogger(TagentBatchUpgradeApi.class);

    @Resource
    TagentMapper tagentMapper;

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "批量升级tagent";
    }

    @Override
    public String getToken() {
        return "tagent/batch/upgrade";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "pkgVersion", type = ApiParamType.STRING, isRequired = true, desc = "安装包版本"),
            @Param(name = "ipPortList", type = ApiParamType.JSONARRAY, desc = "ip,port列表"),
            @Param(name = "networkVoList", type = ApiParamType.JSONARRAY, desc = "网段")
    })
    @Description(desc = "批量升级tagent接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {

        String pkgVersion = paramObj.getString("pkgVersion");
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

        Set<Long> tagentIdSet = new HashSet<>();
        TagentUpgradeAuditVo audit = new TagentUpgradeAuditVo();
        Long auditId = audit.getId();

        //ip：port
        if (CollectionUtils.isNotEmpty(ipPortList)) {
            List<TagentVo> tagentVoList = new ArrayList<>();
            for (IpVo ipVo : ipPortList) {
                TagentVo tagentVo = tagentMapper.getTagentByIpAndPort(ipVo.getIp(), ipVo.getPort());
                if (tagentVo == null) {
                    continue;
                }
                tagentVoList.add(tagentVo);
                tagentIdSet.add(tagentVo.getId());
            }
            this.batchUpgradeTagent(tagentVoList, pkgVersion, auditId);
        }

        //网段掩码
        if (CollectionUtils.isNotEmpty(networkVoList)) {
            List<TagentVo> tagentVoList = new ArrayList<>();
            Map<String, Integer> networkVoMap = networkVoList.stream().collect(Collectors.toMap(NetworkVo::getNetworkIp, NetworkVo::getMask));
            TagentVo tagentVo = new TagentVo();
            int tagentCount = tagentMapper.searchTagentCount(tagentVo);
            tagentVo.setPageSize(100);
            List<TagentVo> searchTagentList = new ArrayList<>();
            int pageCount = PageUtil.getPageCount(tagentCount, 100);
            for (int i = 1; i <= pageCount; i++) {
                tagentVo.setCurrentPage(i);
                searchTagentList = tagentMapper.searchTagent(tagentVo);
                for (TagentVo tagent : searchTagentList) {
                    networkVoMap.forEach((k, v) -> {
                        if (IpUtil.isBelongSegment(tagent.getIp(), k, v) && !tagentIdSet.contains(tagent.getId())) {
                            tagentVoList.add(tagent);
                            tagentIdSet.add(tagent.getId());
                        }
                    });
                }
                this.batchUpgradeTagent(tagentVoList, pkgVersion, auditId);
            }
        }

        //插入此次升级记录
        audit.setCount(tagentIdSet.size());
        StringBuilder stringBuilder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(networkVoList)) {
            for (NetworkVo networkVo : networkVoList) {
                stringBuilder.append(networkVo.getNetworkIp()).append(" / ").append(networkVo.getMask()).append("<br>");
            }
            audit.setNetwork(String.valueOf(stringBuilder));
        }
        tagentMapper.insertUpgradeAudit(audit);
        return null;
    }

    private void batchUpgradeTagent(List<TagentVo> tagentVoList, String pkgVersion, Long auditId) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(tagentVoList.size());
        for (TagentVo tagentVo : tagentVoList) {
            //多线程单个升级
            CodeDriverThread run = new CodeDriverThread("tagentBatchUpgrade") {
                @Override
                protected void execute() {
                    try {
                        //获取对应的安装包版本
                        TagentVersionVo versionVo = tagentService.findTagentPkgVersion(tagentVo, pkgVersion);
                        tagentService.batchUpgradeTagent(tagentVo, versionVo, pkgVersion, auditId);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    } finally {
                        latch.countDown();
                    }
                }

            };
            Thread thread = new Thread(run);
            thread.start();
        }
        logger.info("===============主线程：" + Thread.currentThread().getName() + "正在等待tagent升级子线程执行完毕===============");
//        System.out.println("===============主线程：" + Thread.currentThread().getName() + "正在等待tagent升级子线程执行完毕===============");
        latch.await();
        logger.info("===============主线程：" + Thread.currentThread().getName() + "的全部tagent升级子线程执行完毕===============");
//        System.out.println("===============主线程：" + Thread.currentThread().getName() + "的全部tagent升级子线程执行完毕===============");
    }


}
