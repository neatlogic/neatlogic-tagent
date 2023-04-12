/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tagent.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.tagent.dto.TagentAccountVo;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.enums.TagentStatus;
import neatlogic.framework.tagent.exception.TagentHasBeenConnectedException;
import neatlogic.framework.tagent.exception.TagentIdNotFoundException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class TagentDeleteApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "删除tagent";
    }

    @Override
    public String getToken() {
        return "tagent/delete";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", isRequired = true, type = ApiParamType.LONG, desc = "tagent id")
    })
    @Output({
    })
    @Description(desc = "删除tagent接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        if (id != null) {
            TagentVo tagent = tagentMapper.getTagentById(id);
            if (tagent == null) {
                throw new TagentIdNotFoundException(id);
            }
            if (!StringUtils.equals(tagent.getStatus(), TagentStatus.CONNECTED.getValue())) {
                List<Long> deletedAccountIdList = new ArrayList<>();
                deletedAccountIdList.add(tagent.getAccountId());
                List<String> deletedIpList = tagentMapper.getTagentIpListByTagentIpAndPort(tagent.getIp(), tagent.getPort());

                //删除tagent
                tagentMapper.deleteTagentById(id);
                tagentMapper.deleteAllIpByTagentId(id);

//                IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
                if (CollectionUtils.isNotEmpty(deletedIpList)) {
                    //两个tagent之间的包含ip列表存在有相同部分的情况，所以根据需要删除的包含ip列表获取仍然需要的tagent的ip
                    List<String> neededIpList = tagentMapper.getTagentIpListByIpList(deletedIpList);
                    if (CollectionUtils.isNotEmpty(neededIpList)) {
                        deletedIpList = deletedIpList.stream().filter(item -> !neededIpList.contains(item)).collect(toList());
                    }
                    for (String ip : deletedIpList) {
                        TagentAccountVo deletedAccountVo = tagentMapper.getResourceAccountByIpAndPort(ip, tagent.getPort());
                        if (deletedAccountVo != null) {
                            deletedAccountIdList.add(deletedAccountVo.getId());
                        }
                    }
                }

                //删掉该tagent account
//                IResourceCenterAccountCrossoverService accountService = CrossoverServiceFactory.getApi(IResourceCenterAccountCrossoverService.class);
//                accountService.deleteAccount(deletedAccountIdList);
                tagentMapper.deleteAccountListByIdList(deletedAccountIdList);
                tagentMapper.deleteAccountIpListByAccountIdList(deletedAccountIdList);
            } else {
                throw new TagentHasBeenConnectedException(tagent);
            }
        }
        return null;
    }
}
