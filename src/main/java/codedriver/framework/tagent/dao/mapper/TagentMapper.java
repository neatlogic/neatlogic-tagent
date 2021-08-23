package codedriver.framework.tagent.dao.mapper;

import codedriver.framework.tagent.dto.TagentVo;

import java.util.List;

public interface TagentMapper {
    List<TagentVo> search(TagentVo tagentVo);
}
