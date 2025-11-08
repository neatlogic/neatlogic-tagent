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
package neatlogic.module.tagent.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.store.mongodb.MongoDbManager;
import neatlogic.framework.tagent.auth.label.TAGENT_MANAGE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.service.TagentService;
import neatlogic.framework.util.mongodb.MongoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
@AuthAction(action = TAGENT_MANAGE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class RefreshTagentInfoToMongodbApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Resource
    TagentService tagentService;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private MongoService mongoService;

    @Override
    public String getName() {
        return "nmta.importtagentinfotomongodbapi.description.desc";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public String getToken() {
        return "tagent/info/to/mongodb/refresh";
    }

    @Description(desc = "nmta.importtagentinfotomongodbapi.description.desc")
    @Input({
            @Param(name = "isReCreateTagentInfo", type = ApiParamType.INTEGER, desc = "是否删除重建_tagent_info集合，1:是，0:否。默认不删除重建"),
            @Param(name = "ipList", type = ApiParamType.JSONARRAY, desc = "需要重新更新的Tagent ip列表")
    })
    @Output({
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<String> ipList = null;
        if (paramObj.containsKey("ipList")) {
            ipList = paramObj.getJSONArray("ipList").toJavaList(String.class);
        }
        TagentVo tagentVo = new TagentVo();
        tagentVo.setIpList(ipList);

        Integer isReCreateTagentInfo = 0;
        if (paramObj.getInteger("isReCreateTagentInfo") != null) {
            isReCreateTagentInfo = paramObj.getInteger("isReCreateTagentInfo");
        }
        if (isReCreateTagentInfo == 1 && MongoDbManager.getMongoClient(TenantContext.get().getTenantUuid()) != null) {
            mongoTemplate.dropCollection("_tagent_info");
            mongoService.createCollectionAndUniqueIndex("_tagent_info", "id", "unique_id");
        }


        int rowNum = tagentMapper.searchTagentCount(tagentVo);
        if (rowNum > 0) {
            tagentVo.setPageSize(100);
            tagentVo.setRowNum(rowNum);
            for (int i = 0; i < tagentVo.getPageCount(); i++) {
                tagentVo.setCurrentPage(i + 1);
                List<TagentVo> tagentList = tagentMapper.searchTagent(tagentVo);
                if (CollectionUtils.isNotEmpty(tagentList)) {
                    for (TagentVo tagent : tagentList) {
                        tagentService.updateTagentMGByIdWithLock(tagent, true);
                    }
                }
            }
        }
        return null;
    }
}
