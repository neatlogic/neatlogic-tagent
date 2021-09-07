package codedriver.framework.tagent.dao.mapper;

import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.tagent.dto.TagentOSVo;
import codedriver.framework.tagent.dto.TagentVo;

import java.util.List;

public interface TagentMapper {
    List<TagentVo> searchTagent(TagentVo tagentVo);

    List<String> searchTagentVersion();

    List<TagentOSVo> searchTagentOSType();

    List<RunnerGroupVo> searchTagentRunnerGroup();

    List<RunnerGroupVo> searchRunnerGroupInformation(RunnerGroupVo groupVo);

    int searchTagentCount();

    int searchTagentRunnerCount();

    TagentVo getTagentById(Long tagentId);

    int searchRunnerGroupCount();
}
