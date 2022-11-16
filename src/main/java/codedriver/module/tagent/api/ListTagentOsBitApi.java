/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */
package codedriver.module.tagent.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentOsBitVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author longrf
 * @date 2022/11/15 14：14
 */

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListTagentOsBitApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "查询TagentOS类型列表";
    }

    @Override
    public String getToken() {
        return "tagent/osbit/list";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "查询Tagent的cpu架构类型接口")
    @Output({
            @Param(explode = TagentOsBitVo[].class, desc = "tagent的cpu架构类型列表")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<ValueTextVo> returnList = new ArrayList<>();
        List<TagentOsBitVo> osBitList = tagentMapper.getTagentOsBitList();
        for (TagentOsBitVo osBitVo : osBitList) {
            returnList.add(new ValueTextVo(osBitVo.getName(), osBitVo.getName()));
        }
        returnList.add(new ValueTextVo("default", "default"));
        return returnList;
    }
}
