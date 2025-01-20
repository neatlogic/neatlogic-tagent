/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tagent.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.queue.NeatLogicUniqueBlockingQueue;
import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.cmdb.crossover.IResourceAccountCrossoverMapper;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountBaseVo;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountIpVo;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountProtocolVo;
import neatlogic.framework.cmdb.exception.resourcecenter.ResourceCenterAccountProtocolNotFoundException;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dao.mapper.TenantMapper;
import neatlogic.framework.dto.TenantVo;
import neatlogic.framework.store.mongodb.MongoDbManager;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.exception.TagentAccountNotFoundException;
import neatlogic.framework.tagent.service.TagentService;
import neatlogic.framework.util.I18nUtils;
import neatlogic.framework.util.mongodb.MongoService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Service
public class UpdateTagentInfoThread {
    @Resource
    private TagentService tagentService;

    @Resource
    private MongoService mongoService;

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private TagentMapper tagentMapper;
    private static final Logger logger = LoggerFactory.getLogger(UpdateTagentInfoThread.class);
    private static final NeatLogicUniqueBlockingQueue<TagentVo> blockingQueue = new NeatLogicUniqueBlockingQueue<>(50000);

    @PostConstruct
    public void init() {
        TenantContext.init();
        List<TenantVo> tenantVoList = tenantMapper.getAllActiveTenant();
        for (TenantVo tenantVo : tenantVoList) {
            try {
                TenantContext.get().switchTenant(tenantVo.getUuid()).setUseMasterDatabase(false);
                //如果租户没初始化mongodb,则无需创建collection
                if (MongoDbManager.getMongoClient(tenantVo.getUuid()) != null) {
                    mongoService.createCollectionAndUniqueIndex("_tagent_info", "id", "unique_id");
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                System.out.println("  ✖" + I18nUtils.getStaticMessage("nmtc.updatetagentinfothread.init.mongodbexception", tenantVo.getUuid()));
                System.exit(1);
            }
        }
        TenantContext.get().setUseMasterDatabase(true);
        Thread t = new Thread(new NeatLogicThread("UPDATE-TAGENT-INFO-MANAGER") {
            @Override
            protected void execute() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        TagentVo tagentVo = blockingQueue.take();
                        logger.debug("====TagentUpdateInfo-take:" + JSON.toJSONString(tagentVo));
                        //2、更新tagent信息（包括更新os信息，如果不存在os则insert后再绑定osId、osbitId）
                        tagentService.updateTagentById(tagentVo);
                        //3、当 tagent ip 地址变化(切换网卡)时， 更新 agent ip和账号
                        updateTagentIpAndAccount(tagentVo);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void updateTagentIpAndAccount(TagentVo tagent) {
        JSONObject jsonObj = tagent.getParam();
        if (Objects.equals(jsonObj.getString("needUpdateTagentIp"), "1")) {
            IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
            AccountBaseVo tagentAccountVo = tagentMapper.getTagentAccountByIpAndPort(tagent.getIp(), tagent.getPort());
            if (tagentAccountVo == null) {
                throw new TagentAccountNotFoundException(tagent.getIp(), tagent.getPort());
            }
            String protocolName;
            if (tagent.getPort() == 3939) {
                protocolName = "tagent";
            } else {
                protocolName = "tagent." + tagent.getPort();
            }
            AccountProtocolVo protocolVo = resourceAccountCrossoverMapper.getAccountProtocolVoByProtocolName(protocolName);
            if (protocolVo == null) {
                throw new ResourceCenterAccountProtocolNotFoundException(protocolName);
            }
            List<String> oldIpList = tagentMapper.getTagentIpListByTagentIpAndPort(tagent.getIp(), tagent.getPort());
            List<String> newIpStringList = new ArrayList<>();
            if (jsonObj.getString("ipString") != null) {
                newIpStringList = Arrays.asList(jsonObj.getString("ipString").split(","));
            }
            List<String> newIpList = newIpStringList;

            //删除多余的tagent ip和账号
            if (CollectionUtils.isNotEmpty(oldIpList)) {
                tagentService.deleteTagentIpList(oldIpList.stream().filter(item -> !newIpList.contains(item)).collect(toList()), tagent);
            }
            if (CollectionUtils.isNotEmpty(newIpList)) {
                List<String> insertTagentIpList = newIpList;
                if (CollectionUtils.isNotEmpty(oldIpList)) {
                    insertTagentIpList = newIpList.stream().filter(item -> !oldIpList.contains(item)).collect(toList());
                }
                //新增tagent ip和账号
                if (CollectionUtils.isNotEmpty(insertTagentIpList)) {
                    tagentMapper.insertTagentIp(tagent.getId(), insertTagentIpList);
                    tagentService.updateIpListMG(tagent.getId(), newIpList);
                    List<String> sameIpList = tagentMapper.getAccountIpByIpListAndPort(insertTagentIpList, tagent.getPort());
                    if (CollectionUtils.isNotEmpty(sameIpList)) {
                        insertTagentIpList = insertTagentIpList.stream().filter(item -> !sameIpList.contains(item)).collect(toList());
                    }
                    for (String ip : insertTagentIpList) {
//                        AccountVo newAccountVo = new AccountVo(ip + "_" + tagent.getPort() + "_tagent", protocolVo.getId(), protocolVo.getPort(), ip, tagentAccountVo.getPasswordPlain());
                        AccountBaseVo newAccountVo = new AccountBaseVo(ip + "_" + tagent.getPort() + "_tagent", protocolVo.getId(), protocolVo.getPort(), ip, tagentAccountVo.getPasswordPlain());
                        tagentMapper.insertAccount(newAccountVo);
//                        resourceAccountCrossoverMapper.insertAccount(newAccountVo);
                        tagentMapper.insertAccountIp(new AccountIpVo(newAccountVo.getId(), newAccountVo.getIp()));
                    }
                }
            }
        }
    }

    public static void addUpdateTagent(TagentVo tagentVo) {
        blockingQueue.offer(tagentVo);
    }

    public static int getSize() {
        return blockingQueue.size();
    }
}
