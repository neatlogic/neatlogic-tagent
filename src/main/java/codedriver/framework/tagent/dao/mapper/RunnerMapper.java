package codedriver.framework.tagent.dao.mapper;


import codedriver.framework.dto.runner.GroupNetworkVo;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.dto.runner.RunnerVo;

public interface RunnerMapper {
    int checkGroupNameIsRepeats(RunnerGroupVo runnerGroupVo);

    void updateRunnerGroup(RunnerGroupVo runnerGroupVo);

    void insertRunnerGroup(RunnerGroupVo runnerGroupVo);

    void deleteGroupNetWork(Long id);

    void insertNetwork(GroupNetworkVo networkVo);

    int checkRunnerGroupIdIsExist(Long id);

    void deleteRunnerGroupById(Long id);

    int checkRunnerNameIsExist(RunnerVo runnerVo);

    void updateRunner(RunnerVo runnerVo);

    void insertRunner(RunnerVo runnerVo);

    int checkRunnerIdIsExist(Long id);

    void deleteRunnerById(Long id);
}
