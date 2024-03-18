/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tagent.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountBaseVo;
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
                        AccountBaseVo deletedAccountVo = tagentMapper.getResourceAccountByIpAndPort(ip, tagent.getPort());
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
