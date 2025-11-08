/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tagent.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.ClientSession;
import neatlogic.framework.asynchronization.queue.NeatLogicUniqueBlockingQueue;
import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.cmdb.crossover.IResourceAccountCrossoverMapper;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountBaseVo;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountProtocolVo;
import neatlogic.framework.cmdb.exception.resourcecenter.ResourceCenterAccountProtocolNotFoundException;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dao.mapper.TenantMapper;
import neatlogic.framework.dto.TenantVo;
import neatlogic.framework.store.mongodb.MongoDbManager;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.exception.TagentIpConflictException;
import neatlogic.framework.tagent.service.TagentService;
import neatlogic.framework.transaction.util.TransactionUtil;
import neatlogic.framework.util.I18nUtils;
import neatlogic.framework.util.mongodb.MongoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

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
    private MongoTemplate mongoTemplate;

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
                TenantContext.get().switchTenant(tenantVo.getUuid());
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
        Thread t = new Thread(new NeatLogicThread("UPDATE-TAGENT-INFO-MANAGER") {
            @Override
            protected void execute() {
                while (!Thread.currentThread().isInterrupted()) {
                    ClientSession session = null;
                    TransactionStatus tx = null;
                    try {
                        TagentVo tagentVo = blockingQueue.take();
                        tx = TransactionUtil.openTx();
                        if (logger.isDebugEnabled()) {
                            logger.debug("====TagentUpdateInfo-take:{}", JSON.toJSONString(tagentVo));
                        }
                        //当 tagent ip 地址变化(切换网卡)时， 更新 agent ip和账号
                        updateTagentIpAndAccount(tagentVo);
                        //更新tagent信息（包括更新os信息，如果不存在os则insert后再绑定osId、osbitId）
                        tagentService.updateTagentById(tagentVo);
                        TransactionUtil.commitTx(tx);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        //返回给tagent的错误信息少一些
                        try {
                            if (tx != null) {
                                TransactionUtil.rollbackTx(tx);
                            }
                        } catch (Exception rollbackEx) {
                            logger.error("mysql transaction rollback failed：{}", rollbackEx.getMessage(), rollbackEx);
                        }
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
            /*如果心跳ip和原ip不一样，更新tagent ip和账号name*/
            TagentVo tagentOld = tagentMapper.getTagentById(tagent.getId());
            if (!Objects.equals(tagentOld.getIp(), jsonObj.getString("ip"))) {
                TagentVo tagentExist = tagentMapper.getTagentByIpAndPortAndIdNot(jsonObj.getString("ip"),jsonObj.getInteger("port"), tagent.getId());
                if(tagentExist!= null){
                    throw new TagentIpConflictException(jsonObj.getString("ip"), jsonObj.getInteger("port"));
                }
                tagentMapper.updateTagentIpById(tagentOld.getId(), jsonObj.getString("ip"));
                AccountBaseVo accountBaseVo = new AccountBaseVo();
                accountBaseVo.setId(tagentOld.getAccountId());
                accountBaseVo.setName(jsonObj.getString("ip") + "_" + jsonObj.getString("port") + "_tagent");
                tagentMapper.updateAccountNameById(accountBaseVo);
            }
            /*更新ipList*/
            List<String> oldIpList = tagentMapper.getTagentIpListByTagentId(tagent.getId());
            List<String> newIpStringList = new ArrayList<>();
            if (StringUtils.isNotBlank(jsonObj.getString("ipString"))) {
                newIpStringList = Arrays.asList(jsonObj.getString("ipString").split(","));
            }
            List<String> newIpList = newIpStringList;

            boolean isUpdateMG = false;
            //删除多余的tagent ip和账号
            if (CollectionUtils.isNotEmpty(oldIpList)) {
                for (String ip : oldIpList.stream().filter(item -> !newIpList.contains(item)).collect(toList())) {
                    tagentMapper.deleteTagentIp(tagent.getId(), ip);
                    isUpdateMG = true;
                }
            }
            if (CollectionUtils.isNotEmpty(newIpList)) {
                List<String> insertTagentIpList = newIpList;
                if (CollectionUtils.isNotEmpty(oldIpList)) {
                    insertTagentIpList = newIpList.stream().filter(item -> !oldIpList.contains(item)).collect(toList());
                }
                //新增tagent ip和账号
                if (CollectionUtils.isNotEmpty(insertTagentIpList)) {
                    tagentMapper.insertTagentIp(tagent.getId(), insertTagentIpList);
                    isUpdateMG = true;
                }
            }

            if (isUpdateMG) {
                tagent.setIpList(newIpList);
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
