package codedriver.module.tagent.common.config;


import java.io.File;

public class TagentConfig {
    private static final String CONFIG_FILE = "config.properties";
    public static String TAGENT_PATH; //tagent升级文件位置
    public static String TAGENT_PKG_PATH;

    static {
//        TAGENT_PATH = Config.CODEDRIVER_HOME() + Config.getProperty(CONFIG_FILE, "tagent.dir","/data/balantflow/tagent/");
//
//        TAGENT_PKG_PATH = Config.CODEDRIVER_HOME() + Config.getProperty(CONFIG_FILE, "tagent.pkg.dir", "/data/balantflow/tagentpkg/");

        File filedir = new File(TAGENT_PATH);
        if (!filedir.exists()) {
            filedir.mkdirs();
        }

        File pkgdir = new File(TAGENT_PKG_PATH);
        if (!pkgdir.exists()) {
            pkgdir.mkdir();
        }
    }
}
