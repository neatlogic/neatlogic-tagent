package neatlogic.module.tagent.api;

import neatlogic.framework.asynchronization.thread.CodeDriverThread;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.runner.NetworkVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentSearchVo;
import neatlogic.framework.tagent.dto.TagentUpgradeAuditVo;
import neatlogic.framework.tagent.dto.TagentVersionVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.service.TagentService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
            @Param(name = "networkVoList", type = ApiParamType.JSONARRAY, desc = "网段"),
            @Param(name = "runnerGroupIdList", type = ApiParamType.JSONARRAY, desc = "执行器组id列表")
    })
    @Description(desc = "批量升级tagent接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {

        String pkgVersion = paramObj.getString("pkgVersion");
        TagentSearchVo tagentSearchVo = paramObj.toJavaObject(TagentSearchVo.class);
        List<TagentVo> tagentList = tagentService.getTagentList(tagentSearchVo);

        TagentUpgradeAuditVo audit = new TagentUpgradeAuditVo();
        Long auditId = audit.getId();

        //升级
        batchUpgradeTagent(tagentList, pkgVersion, auditId);
        //插入此次升级记录
        audit.setCount(tagentList.size());
        StringBuilder stringBuilder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(tagentSearchVo.getNetworkVoList())) {
            for (NetworkVo networkVo : tagentSearchVo.getNetworkVoList()) {
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
