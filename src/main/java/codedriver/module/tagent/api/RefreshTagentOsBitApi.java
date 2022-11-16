/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */
package codedriver.module.tagent.api;

import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentOsBitVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author longrf
 * @date 2022/11/15 17:04
 */

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class RefreshTagentOsBitApi extends PrivateApiComponentBase {

    @Resource
    private TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "刷tagent表的osbit_id数据";
    }

    @Override
    public String getToken() {
        return "tagent/osbit/refresh";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Description(desc = "刷tagent的osbit_id数据接口")
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<String> needRegisterOsBitStrList = tagentMapper.getTagentOsBitStringList();
        if (CollectionUtils.isNotEmpty(needRegisterOsBitStrList)) {
            List<TagentOsBitVo> hasRegisterOsBitList = tagentMapper.getTagentOsBitList();
            Map<String, TagentOsBitVo> hasRegisterOsBitMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(hasRegisterOsBitList)) {
                hasRegisterOsBitMap = hasRegisterOsBitList.stream().collect(Collectors.toMap(TagentOsBitVo::getName, e -> e));
            }
            List<TagentOsBitVo> needRegisterOsBitVoList = new ArrayList<>();
            for (String needRegisterOsBitStr : needRegisterOsBitStrList) {
                if (MapUtils.isNotEmpty(hasRegisterOsBitMap) && hasRegisterOsBitMap.containsKey(needRegisterOsBitStr)) {
                    tagentMapper.updateTagentOsBitIdByOsBit(hasRegisterOsBitMap.get(needRegisterOsBitStr).getId(), hasRegisterOsBitMap.get(needRegisterOsBitStr).getName());
                    continue;
                }
                TagentOsBitVo newOsBitVo = new TagentOsBitVo(needRegisterOsBitStr);
                needRegisterOsBitVoList.add(newOsBitVo);
                tagentMapper.updateTagentOsBitIdByOsBit(newOsBitVo.getId(), newOsBitVo.getName());
            }
            if (CollectionUtils.isNotEmpty(needRegisterOsBitVoList)) {
                tagentMapper.insertOsBitList(needRegisterOsBitVoList);
            }
        }
        return null;
    }
}