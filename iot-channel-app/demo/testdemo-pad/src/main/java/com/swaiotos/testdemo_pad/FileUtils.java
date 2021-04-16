package com.swaiotos.testdemo_pad;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @ClassName: FileUtils
 * @Author: AwenZeng
 * @CreateDate: 2020/3/18 22:04
 * @Description:
 */
public class FileUtils {
    /**
     * 拷贝assets下的文件
     *
     * @param assetFilePath assets的文件路径
     * @param to            拷贝到的路径
     */

    public static File copyAssetFile(Context context, String assetFilePath, String to) {

        InputStream inputStream = null;

        FileOutputStream fileOutputStream = null;

        try {

            inputStream = context.getAssets().open(

                    assetFilePath);

            File toDir = new File(to);

            toDir.mkdirs();

            File toFile = new File(

                    toDir.getAbsolutePath()

                            + "/"

                            + assetFilePath.substring(assetFilePath

                            .lastIndexOf("/") + 1));

            fileOutputStream = new FileOutputStream(toFile);

            byte[] buffer = new byte[8*1024];

            for (int bytesRead = 0; (bytesRead = inputStream.read(buffer, 0,

                    buffer.length)) != -1; ) {

                fileOutputStream.write(buffer, 0, bytesRead);

            }

            return toFile;

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            try {

                if (inputStream != null) {

                    inputStream.close();

                }

                if (fileOutputStream != null) {

                    fileOutputStream.close();

                }

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

        return null;
    }
}
