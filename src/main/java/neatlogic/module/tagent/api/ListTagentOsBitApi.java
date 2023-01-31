/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */
package neatlogic.module.tagent.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dao.mapper.TagentMapper;
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
        return "查询Tagent的cpu架构类型列表";
    }

    @Override
    public String getToken() {
        return "tagent/osbit/list";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "查询Tagent的cpu架构类型列表接口")
    @Output({
            @Param(explode = ValueTextVo[].class, desc = "tagent的cpu架构类型列表")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<ValueTextVo> returnList = new ArrayList<>();
        List<String> osBitList = tagentMapper.getTagentOsBitList();
        for (String osbit : osBitList) {
            returnList.add(new ValueTextVo(osbit, osbit));
        }
        returnList.add(new ValueTextVo("default", "default"));
        return returnList;
    }
}
