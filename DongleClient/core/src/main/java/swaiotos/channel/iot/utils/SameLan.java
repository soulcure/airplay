package swaiotos.channel.iot.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class SameLan {
    private static final String LINE_SEP = System.getProperty("line.separator");

    public static boolean isInSameLAN(String ipAddress) {
        boolean isInSameLAN = false;

        try {
            Process process = Runtime.getRuntime().exec("ping -c 1 -w 1 " + ipAddress);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));

            StringBuilder sb = new StringBuilder();
            String line;
            if ((line = reader.readLine()) != null) {
                sb.append(line);
                while ((line = reader.readLine()) != null) {
                    sb.append(LINE_SEP).append(line);
                }
            }

            String msg = sb.toString();
            Log.e("yao", "msg----" + msg);

            String[] splits = msg.split(" ");

            for (String item : splits) {
                if (item.contains("ttl")) {
                    if (item.contains("32")
                            || item.contains("64")
                            || item.contains("63")
                            || item.contains("128")
                            || item.contains("255")) {
                        isInSameLAN = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isInSameLAN;
    }


}
