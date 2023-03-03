package neatlogic.module.tagent.common;

import neatlogic.framework.tagent.dto.TagentVo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constants {

    public static String tagentPkgName = "tagent.tar";
    public static String tagentPkgWin32Name = "tagent_windows_x32.tar";
    public static String tagentPkgWin64Name = "tagent_windows_x64.tar";
    public static String tagentPkgLinuxName = "tagent_linux.tar";


    public static String tagentBasePath = "base";
    public static String tagentWin32Path = "win32";
    public static String tagentWin64Path = "win64";
    public static String tagentLinuxPath = "linux";


    public static Map<Long, TagentVo> agentMap = new ConcurrentHashMap<>();
}
