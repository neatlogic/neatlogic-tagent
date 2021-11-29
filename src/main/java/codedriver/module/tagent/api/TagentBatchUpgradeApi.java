package codedriver.module.tagent.api;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
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
import codedriver.framework.tagent.service.TagentService;
import com.alibaba.fastjson.JSONArray;
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
            @Param(name = "networkVoList", type = ApiParamType.JSONARRAY, desc = "网段"),
            @Param(name = "tagentVoList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "tagentVoList")
    })
    @Description(desc = "批量升级tagent接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {

        String pkgVersion = paramObj.getString("pkgVersion");

        JSONArray networkVoArray = paramObj.getJSONArray("networkVoList");
        JSONArray tagentVoArray = paramObj.getJSONArray("tagentVoList");
        List<NetworkVo> networkVoList = null;
        List<TagentVo> tagentVoList = null;
        if (CollectionUtils.isNotEmpty(networkVoArray)) {
            networkVoList = networkVoArray.toJavaList(NetworkVo.class);
        }
        if (CollectionUtils.isNotEmpty(tagentVoArray)) {
            tagentVoList = tagentVoArray.toJavaList(TagentVo.class);
        }

        //插入此次升级记录
        TagentUpgradeAuditVo audit = new TagentUpgradeAuditVo();
        audit.setCount(tagentVoList.size());
        StringBuilder stringBuilder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(networkVoList)) {
            for (int i = 0; i < networkVoList.size(); i++) {
                stringBuilder.append(networkVoList.get(i).getNetworkIp() + " / " + networkVoList.get(i).getMask() + "<br>");
            }
            audit.setNetwork(String.valueOf(stringBuilder));
        }
        tagentMapper.insertUpgradeAudit(audit);


        Long auditId = audit.getId();
        final CountDownLatch latch = new CountDownLatch(tagentVoList.size());
        for (int i = 0; i < tagentVoList.size(); i++) {
            TagentVo tagentVo = tagentVoList.get(i);
            //多线程单个升级
            CodeDriverThread run = new CodeDriverThread("tagentBatchUpgrade") {
                @Override
                protected void execute() {
                    try {
                        //获取对应的安装包版本
                        TagentVersionVo versionVo = tagentService.findTagentPkgVersion(tagentVo, pkgVersion);
                        tagentService.batchUpdradeTagent(tagentVo, versionVo, pkgVersion, auditId);
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

        return null;
    }


}
