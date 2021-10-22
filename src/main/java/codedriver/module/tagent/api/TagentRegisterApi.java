package codedriver.module.tagent.api;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.IpUtil;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.GroupNetworkVo;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentOSVo;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.tagent.exception.RunnerGroupIdNotFoundException;
import codedriver.framework.tagent.exception.RunnerGroupIsEmptyException;
import codedriver.framework.tagent.exception.RunnerNotFoundInGroupException;
import codedriver.framework.tagent.register.core.AfterRegisterJobManager;
import codedriver.framework.tagent.service.TagentService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Transactional
@Service
@OperationType(type = OperationTypeEnum.OPERATE)
public class TagentRegisterApi extends PublicApiComponentBase {
    private final Logger logger = LoggerFactory.getLogger(TagentRegisterApi.class);
    @Resource
    TagentMapper tagentMapper;
    @Resource
    RunnerMapper runnerMapper;

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "注册tagent";
    }

    @Override
    public String getToken() {
        return "tagent/register";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "注册tagent接口")
    @Input({
            @Param(name = "tagentId", type = ApiParamType.LONG, desc = "tagentId,非必填"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "tagent名称"),
            @Param(name = "ip", type = ApiParamType.STRING, desc = "tagentIP"),
            @Param(name = "port", type = ApiParamType.INTEGER, desc = "tagent端口"),
            @Param(name = "user", type = ApiParamType.STRING, desc = "安装用户"),
            @Param(name = "version", type = ApiParamType.STRING, desc = "tagent版本"),
            @Param(name = "credential", type = ApiParamType.STRING, desc = "tagent密码"),
            @Param(name = "osType", type = ApiParamType.STRING, desc = "os类型"),
            @Param(name = "osbit", type = ApiParamType.STRING, desc = "操作系统位数"),
            @Param(name = "osVersion", type = ApiParamType.STRING, desc = "os版本"),
            @Param(name = "pcpu", type = ApiParamType.STRING, desc = "cpu占用"),
            @Param(name = "mem", type = ApiParamType.STRING, desc = "内存占用")

    })
    @Output({
            @Param(name = "tbodyList", explode = RunnerGroupVo[].class, desc = "runner组列表")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultJson = new JSONObject();
        boolean status = true;
        JSONObject data = new JSONObject();
        Long tagentId = paramObj.getLong("tagentId");
        paramObj.put("id", tagentId);
        paramObj.remove("tagentId");
        TagentVo tagentVo = JSONObject.toJavaObject(paramObj, TagentVo.class);
        // !!! 规避直接复制tagent目录的导致id相同的情况，只重新注册断开连接的tagent
        // 如果有id,则根据id更新其他信息，如果没有id,则根据ip + port 确定唯一性后保存
        if (tagentId != null) {
            TagentVo tagentOrigin = tagentMapper.getTagentById(tagentId);
            if (tagentOrigin != null && tagentOrigin.getRunnerId() != null) {
                RunnerVo runner = runnerMapper.getRunnerById(tagentOrigin.getRunnerId());
                if (runner != null) {
                    Map<String, String> params = new HashMap<>();
                    params.put("ip", tagentOrigin.getIp());
                    params.put("port", tagentOrigin.getPort().toString());
                    params.put("type", "tagentStatusCheck");
     /*                   String checkResult = TagentHttpUtil.post(runner.getUrl() + "/tagent/in/exec", params, header, true);
                        if (StringUtils.isNotBlank(checkResult)) {
                            JSONObject checkObj = JSONObject.parseObject(checkResult);
                            if ("OK".equals(checkObj.getString("Status"))) {
                                //throw new RuntimeException("this tagent is connected，please check id is reduplicated or start later");
                                paramObj.put("id", "");
                            }
                        }*/
                }
            }
            tagentVo.setIsFirstCreate(1);
        }
        // http request ip
        String requestIp = IpUtil.getIpAddr(UserContext.get().getRequest());

        String agentIp = paramObj.getString("ip");

        List<GroupNetworkVo> networkList = tagentMapper.getGroupNetworkList();

        RunnerVo requestRunnerVo = runnerMapper.getRunnerByIp(requestIp);

        Long runnerGroupId = null;
        Integer preMatchedMask = 0;
        for (GroupNetworkVo networkRunner : networkList) {
            if (IpUtil.isBelongSegment(agentIp, networkRunner.getNetworkIp(), networkRunner.getMask())) {
                if (networkRunner.getMask() > preMatchedMask) {
                    runnerGroupId = networkRunner.getGroupId();
                    break;
                }

            }
        }
        if (runnerGroupId == null) {
            throw new RunnerGroupIdNotFoundException(agentIp);
        } else {
            List<RunnerVo> runnerList = runnerMapper.getRunnerByGroupId(runnerGroupId);
            if (runnerList == null || runnerList.size() == 0) {
                throw new RunnerGroupIsEmptyException(runnerGroupId);
            }

            RunnerVo runnerVo = null;

            if (requestRunnerVo == null) {
                if (runnerList.size() == 1)
                    runnerVo = runnerList.get(0);
                else {
                    // 随机选一个
                    Random random = new Random();
                    runnerVo = runnerList.get(random.nextInt(runnerList.size()));
                }
            } else {
                for (RunnerVo vo : runnerList) {
                    String runnerHost = vo.getHost();
                    if (runnerHost.equals(requestIp)) {
                        runnerVo = vo;
                        break;
                    }
                }
                // 可能是nat,虚拟的runner ip
                if (runnerVo == null) {
                    if (runnerList.size() == 1)
                        runnerVo = runnerList.get(0);
                    else {
                        // 随机选一个
                        Random random = new Random();
                        runnerVo = runnerList.get(random.nextInt(runnerList.size()));
                    }
                }
            }

            if (runnerVo == null && requestRunnerVo != null) {
                runnerVo = requestRunnerVo;// 兼容旧的模式，通过runner ip找
                if (runnerVo == null) {
                    throw new RunnerNotFoundInGroupException(runnerGroupId);
                }
            }
            tagentVo.setRunnerId(runnerVo.getId());
            tagentVo.setRunnerIp(runnerVo.getHost());
            tagentVo.setRunnerPort(runnerVo.getPort().toString());
            tagentVo.setRunnerGroupId(runnerGroupId);
            paramObj.put("runnerGroupId", runnerGroupId);
            if (StringUtils.isNotBlank(paramObj.getString("id"))) {
                tagentVo.setId(paramObj.getLong("id"));
            }

            if (StringUtils.isNotBlank(tagentVo.getOsType())) {
                String ostype = tagentVo.getOsType();
                TagentOSVo os = tagentMapper.getOsByName(ostype.toLowerCase());
                if (os != null) {
                    tagentVo.setOsId(os.getId());
                } else {
                    TagentOSVo newOS = new TagentOSVo();
                    newOS.setName(ostype);
                    tagentMapper.insertOs(newOS);
                    tagentVo.setOsId(newOS.getId());
                }
            }
            if (paramObj.containsKey("credential")) {
                tagentVo.setCredential(paramObj.getString("credential"));
            }

            Long saveTagentId = tagentService.saveTagent(tagentVo);

            //注册后同步信息到资源中心
            AfterRegisterJobManager.executeAll(tagentVo);

            JSONArray runnerArray = new JSONArray();
            for (RunnerVo runner : runnerList) {
                JSONObject runnerData = new JSONObject();
                runnerData.put("id", runner.getId());
                runnerData.put("ip", runner.getHost());
                runnerData.put("port", runner.getNettyPort());
                runnerArray.add(runnerData);
            }
            data.put("tagentId", saveTagentId);
            //兼容tagent入参 不能改为runner
            data.put("proxyId", runnerVo.getId());
            data.put("proxyIp", runnerVo.getHost());
            data.put("proxyPort", runnerVo.getPort());
            data.put("proxyGroupId", runnerGroupId);
            data.put("proxyList", runnerArray);
        }


        resultJson.put("Status", status ? "OK" : "ERROR");
        resultJson.put("Data", status ? data : "");
        return resultJson;
    }

    @Override
    public boolean isRaw() {
        return true;
    }
}
