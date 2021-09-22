package codedriver.module.tagent.common;

import codedriver.framework.tagent.dto.TagentVo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constants {

    public static String encryptKey = "#ts=9^0$1";

    public static String tagentPkgName = "tagent.tar";
    public static String tagentPkgWin32Name = "tagent_windows_x32.tar";
    public static String tagentPkgWin64Name = "tagent_windows_x64.tar";
    public static String tagentPkgLinuxName = "tagent_linux.tar";


    public static String tagentBasePath = "base";
    public static String tagentWin32Path = "win32";
    public static String tagentWin64Path = "win64";
    public static String tagentLinuxPath = "linux";

    public enum TagentStatus {
        CONNECTED("connect", "已连接"), DISCONNECTED("disconnect", "未连接");
        private String name;
        private String text;

        TagentStatus(String _name, String _text) {
            this.name = _name;
            this.text = _text;
        }

        public String getValue() {
            return name;
        }

        public String getText() {
            return text;
        }

        public static String getText(String name) {
            for (TagentStatus s : TagentStatus.values()) {
                if (s.getValue().equals(name)) {
                    return s.getText();
                }
            }
            return "";
        }
    }


    public static Map<Long, TagentVo> agentMap = new ConcurrentHashMap<>();
}
