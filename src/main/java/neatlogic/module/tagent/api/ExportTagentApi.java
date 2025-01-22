/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tagent.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerGroupVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.tagent.auth.label.TAGENT_BASE;
import neatlogic.framework.tagent.dto.TagentVo;
import neatlogic.framework.tagent.service.TagentService;
import neatlogic.framework.util.FileUtil;
import neatlogic.framework.util.excel.ExcelBuilder;
import neatlogic.framework.util.excel.SheetBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExportTagentApi extends PrivateBinaryStreamApiComponentBase {

    private static final Logger logger = LoggerFactory.getLogger(ExportTagentApi.class);

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Resource
    RunnerMapper runnerMapper;

    @Resource
    TagentService tagentService;

    @Override
    public String getName() {
        return "nmta.exporttagentapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "osId", type = ApiParamType.LONG, desc = "term.tagent.osid"),
            @Param(name = "version", type = ApiParamType.STRING, desc = "term.tagent.version"),
            @Param(name = "status", type = ApiParamType.STRING, desc = "term.tagent.status"),
            @Param(name = "runnerGroupId", type = ApiParamType.LONG, desc = "term.tagent.runnergroupid"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword")
    })
    @Description(desc = "nmta.exporttagentapi.getname")
    @Output({})
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<String> headerList = getHeaderList();
        List<String> columnList = getColumnList();
        ExcelBuilder builder = new ExcelBuilder(SXSSFWorkbook.class);
        SheetBuilder sheetBuilder = builder.withBorderColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT)
                .withHeadFontColor(HSSFColor.HSSFColorPredefined.WHITE)
                .withHeadBgColor(HSSFColor.HSSFColorPredefined.DARK_BLUE)
                .withColumnWidth(30)
                .addSheet("tagent列表")
                .withHeaderList(headerList)
                .withColumnList(columnList);

        TagentVo search = paramObj.toJavaObject(TagentVo.class);
        long rowNum = tagentService.getTagentListMGCount(search);
        if (rowNum > 0) {
            search.setRowNum((int) rowNum);
            search.setPageSize(100);
            int pageCount = search.getPageCount();
            for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
                search.setCurrentPage(currentPage);
                List<TagentVo> tagentMGList = tagentService.searchTagentListMG(search);
                if (CollectionUtils.isNotEmpty(tagentMGList)) {
                    Set<Long> runnerGroupIdSet = tagentMGList.stream().filter(Objects::nonNull).map(TagentVo::getRunnerGroupId).filter(Objects::nonNull).collect(Collectors.toSet());
                    if (CollectionUtils.isNotEmpty(runnerGroupIdSet)) {
                        List<RunnerGroupVo> runnerGroupVos = runnerMapper.getRunnerGroupByIdList(new ArrayList<>(runnerGroupIdSet));
                        if (CollectionUtils.isNotEmpty(runnerGroupVos)) {
                            Map<Long, String> runnerGroupIdNameMap = runnerGroupVos.stream().collect(Collectors.toMap(RunnerGroupVo::getId, RunnerGroupVo::getName));
                            for (TagentVo tagent : tagentMGList) {
                                tagent.setRunnerGroupName(runnerGroupIdNameMap.get(tagent.getRunnerGroupId()));
                            }
                        }
                    }
                    for (TagentVo tagent : tagentMGList) {
                        Map<String, Object> dataMap = tagentVoConvertDataMap(tagent);
                        sheetBuilder.addData(dataMap);
                    }
                }
            }
        }
        Workbook workbook = builder.build();
        String fileName = FileUtil.getEncodedFileName("tagent列表_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx");
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + fileName + "\"");
        try (OutputStream os = response.getOutputStream()) {
            workbook.write(os);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            workbook.close();
        }
        return null;
    }

    @Override
    public String getToken() {
        return "tagent/export";
    }

    /**
     * 表头信息
     */
    private List<String> getHeaderList() {
        List<String> headerList = new ArrayList<>();
        headerList.add("名称");
        headerList.add("IP:PORT");
        headerList.add("状态");
        headerList.add("包含IP");
        headerList.add("OS/OS版本");
        headerList.add("CPU架构");
        headerList.add("版本");
        headerList.add("runnerIp");
        headerList.add("Runner组");
        headerList.add("CPU");
        headerList.add("内存");
        headerList.add("更新时间");
        return headerList;
    }

    /**
     * 每列对应的key
     */
    private List<String> getColumnList() {
        List<String> columnList = new ArrayList<>();
        columnList.add("name");
        columnList.add("ip");
        columnList.add("status");
        columnList.add("ipList");
        columnList.add("osNameVersion");
        columnList.add("osbit");
        columnList.add("version");
        columnList.add("runnerIp");
        columnList.add("runnerGroupName");
        columnList.add("pcpu");
        columnList.add("mem");
        columnList.add("lcd");
        return columnList;
    }

    private Map<String, Object> tagentVoConvertDataMap(TagentVo tagent) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", tagent.getName() == null ? StringUtils.EMPTY : tagent.getName());
        String ip = StringUtils.EMPTY;
        if (tagent.getIp() != null) {
            ip = tagent.getIp();
        }
        if (tagent.getPort() != null) {
            ip += ":" + tagent.getPort();
        }
        dataMap.put("ip", ip);
        dataMap.put("status", Objects.equals(tagent.getStatus(), "connected") ? "已连接" : "未连接");
        if (CollectionUtils.isNotEmpty(tagent.getIpList())) {
            dataMap.put("ipList", String.join(",", tagent.getIpList()));
        } else {
            dataMap.put("ipList", StringUtils.EMPTY);
        }
        dataMap.put("osNameVersion", tagent.getOsNameVersion() == null ? StringUtils.EMPTY : tagent.getOsNameVersion());
        dataMap.put("osbit", tagent.getOsbit() == null ? StringUtils.EMPTY : tagent.getOsbit());
        dataMap.put("version", tagent.getVersion() == null ? StringUtils.EMPTY : tagent.getVersion());
        dataMap.put("runnerIp", tagent.getRunnerIp() == null ? StringUtils.EMPTY : tagent.getRunnerIp());
        dataMap.put("runnerGroupName", tagent.getRunnerGroupName() == null ? StringUtils.EMPTY : tagent.getRunnerGroupName());
        if (tagent.getPcpu() != null) {
            dataMap.put("pcpu", tagent.getPcpu() + "%");
        } else {
            dataMap.put("pcpu", StringUtils.EMPTY);
        }
        if (tagent.getMem() != null) {
            dataMap.put("mem", tagent.getMem() + "MB");
        } else {
            dataMap.put("mem", StringUtils.EMPTY);
        }
        if (tagent.getLcd() != null) {
            dataMap.put("lcd", dateTimeFormatter.format(tagent.getLcd().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
        } else {
            dataMap.put("lcd", StringUtils.EMPTY);
        }
        return dataMap;
    }
}
