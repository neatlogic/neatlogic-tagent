package codedriver.framework.tagent.api;


import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service

@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TagentSearchApi extends PrivateApiComponentBase {
	@Resource
	TagentMapper tagentMapper;
	@Override
	public String getToken() {
		return "tagent/search";
	}

	@Override
	public String getName() {
		return "获取tagent";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Description(desc = "获取tagent接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		TagentVo tagentVo = JSONObject.toJavaObject(jsonObj,TagentVo.class);
		return TableResultUtil.getResult(tagentMapper.search(tagentVo),tagentVo);
	}

}
