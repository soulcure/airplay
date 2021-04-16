package swaiotos.channel.iot.utils;

import java.io.File;

/**
 * @ClassName: Files
 * @Author: lu
 * @CreateDate: 2020/3/17 4:44 PM
 * @Description:
 */
public class Files {
    public static final void ln(File source, File link) throws Exception {
        Runtime.getRuntime().exec("ln -s " + source.getAbsolutePath() + " " + link.getAbsolutePath());
    }
}
