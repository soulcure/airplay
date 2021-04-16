package swaiotos.channel.iot.common.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.common.utils
 * @ClassName: FileUtils
 * @Description: 该类只存储accessToken
 * @Author: wangyuehui
 * @CreateDate: 2020/7/31 11:26
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/7/31 11:26
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class FileAccessTokenUtils {
    /**
     * 向File中保存数据
     */
    public static void saveDataToFile(Context context, String fileName,String accessToken){

        FileOutputStream fileOutputStream=null;
        OutputStreamWriter outputStreamWriter=null;
        BufferedWriter bufferedWriter=null;
        try {
            fileOutputStream=context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStreamWriter=new OutputStreamWriter(fileOutputStream);
            bufferedWriter=new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(accessToken);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (bufferedWriter!=null) {
                    bufferedWriter.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 从File中读取数据
     */
    public static String getDataFromFile(Context context,String fileName) {
        FileInputStream fileInputStream=null;
        InputStreamReader inputStreamReader=null;
        BufferedReader bufferedReader=null;
        StringBuilder stringBuilder=null;
        String line=null;
        try {
            stringBuilder = new StringBuilder();
            fileInputStream=context.openFileInput(fileName);
            inputStreamReader=new InputStreamReader(fileInputStream);
            bufferedReader=new BufferedReader(inputStreamReader);
            while((line=bufferedReader.readLine())!=null){
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (bufferedReader!=null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
