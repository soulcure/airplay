package swaiotos.channel.iot.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamUtils {


    /**
     * 将输入流转换为字节数组
     *
     * @param inStream
     * @return
     * @throws IOException
     */
    public static byte[] read(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    /**
     * 将输入流转换为 UTF-8 字符串
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String readStream(InputStream inputStream) throws IOException {
        return readStream(inputStream, "UTF-8");
    }

    /**
     * 将输入流转换为字符串
     *
     * @param inputStream
     * @param charsetName
     * @return
     * @throws IOException
     */
    public static String readStream(InputStream inputStream, String charsetName) throws IOException {
        if (inputStream == null) {
            return "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charsetName));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

}
