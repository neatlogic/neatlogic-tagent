package neatlogic.module.tagent.util;

import neatlogic.framework.tagent.dto.TagentVersionVo;
import neatlogic.module.tagent.common.Constants;
import neatlogic.module.tagent.common.config.TagentConfig;

import java.io.File;

public class TagentUtil {

    public static String getPkgName(String osType, String cpuBit){
        String tagentPkgName;
        if (osType.equals("windows")) {
            if (cpuBit.contains("64")) {
                tagentPkgName = Constants.tagentPkgWin64Name;
            } else {
                tagentPkgName = Constants.tagentPkgWin32Name;
            }
        } else {
            tagentPkgName = Constants.tagentPkgLinuxName;
        }
        return tagentPkgName;
    }


    public static String getOsType(String type, String cpuBit){
        String osType;
        if (type.equals("windows")) {
            if (cpuBit.contains("64")) {
                osType = TagentVersionVo.TagentOsType.WINDOWS64.getType();
            } else {
                osType = TagentVersionVo.TagentOsType.WINDOWS64.getType();
            }
        } else {
            osType = TagentVersionVo.TagentOsType.WINDOWS64.getType();
        }
        return osType;
    }

    public static void buildTar(String osType, String cpuBit){
//        if (osType.equals("windows")) {
//            if (cpuBit.contains("64")) {
//                buildWin64Tar();
//            } else {
//                buildWin32Tar();
//            }
//        } else {
//            buildLinuxTar();
//        }
    }

//    public static void buildWin32Tar() {
//        checkTagentBaseFile();
//        File fileWin32 = new File(TagentConfig.TAGENT_PKG_PATH + File.separator + Constants.tagentPkgWin32Name);
//        if (!fileWin32.exists()) {
//            String[] sourceDirNames = {TagentConfig.TAGENT_PATH + File.separator + Constants.tagentWin32Path, TagentConfig.TAGENT_PATH + File.separator + Constants.tagentBasePath};
//            String targetDirName = TagentConfig.TAGENT_PATH + File.separator + Constants.tagentWin32Path + "tmp";
//            TagentCombineFileUtil.combineDirectory(sourceDirNames, targetDirName);
//            String tagentPkgName = Constants.tagentPkgWin32Name;
//            TarBuilder tar32Builder = new TarBuilder(targetDirName, TagentConfig.TAGENT_PKG_PATH, tagentPkgName, true);
//            tar32Builder.build();
//            File tmpFile = new File(targetDirName);
//            if (tmpFile.exists()) {
//                TagentFileUtil.deleteFile(tmpFile);
//            }
//        }
//    }
//
//    public static void buildWin64Tar() {
//        checkTagentBaseFile();
//        File fileWin64 = new File(TagentConfig.TAGENT_PKG_PATH + File.separator + Constants.tagentPkgWin64Name);
//        if (!fileWin64.exists()) {
//            String[] sourceDirNames = {TagentConfig.TAGENT_PATH + File.separator + Constants.tagentWin64Path, TagentConfig.TAGENT_PATH + File.separator + Constants.tagentBasePath};
//            String targetDirName = TagentConfig.TAGENT_PATH + File.separator + Constants.tagentWin64Path + "tmp";
//            TagentCombineFileUtil.combineDirectory(sourceDirNames, targetDirName);
//            String tagentPkgName = Constants.tagentPkgWin64Name;
//            TarBuilder tar64Builder = new TarBuilder(targetDirName, TagentConfig.TAGENT_PKG_PATH, tagentPkgName, true);
//            tar64Builder.build();
//            File tmpFile = new File(targetDirName);
//            if (tmpFile.exists()) {
//                TagentFileUtil.deleteFile(tmpFile);
//            }
//
//        }
//    }
//
//    public static void buildLinuxTar() {
//        checkTagentBaseFile();
//        File fileLinux = new File(TagentConfig.TAGENT_PKG_PATH + File.separator + Constants.tagentPkgLinuxName);
//        if (!fileLinux.exists()) {
//            String[] sourceDirNames = {TagentConfig.TAGENT_PATH + File.separator + Constants.tagentLinuxPath, TagentConfig.TAGENT_PATH + File.separator + Constants.tagentBasePath};
//            String targetDirName = TagentConfig.TAGENT_PATH + File.separator + Constants.tagentLinuxPath + "tmp";
//            TagentCombineFileUtil.combineDirectory(sourceDirNames, targetDirName);
//            String tagentPkgName = Constants.tagentPkgLinuxName;
//            TarBuilder tar32Builder = new TarBuilder(targetDirName, TagentConfig.TAGENT_PKG_PATH, tagentPkgName, true);
//            tar32Builder.build();
//            File tmpFile = new File(targetDirName);
//            if (tmpFile.exists()) {
//                TagentFileUtil.deleteFile(tmpFile);
//            }
//        }
//    }


    public static void checkTagentBaseFile() {
        File fileBase = new File(TagentConfig.TAGENT_PATH + File.separator + Constants.tagentBasePath);
        if (!fileBase.exists()) {
            throw new RuntimeException("tagent 安装基础文件路径不存在");
        } else {
            File[] fileArray = fileBase.listFiles();
            if (fileArray.length == 0) {
                throw new RuntimeException("tagent 安装包文件目录为空");
            }
        }
    }
}
