package com.swaiotos.skymirror.sdk.util;

import android.media.MediaFormat;
import android.util.Log;

import com.skyworth.dpclientsdk.HexUtil;

import java.nio.ByteBuffer;

public class MediaConfig {

    private static final String TAG = "yao";


    // 直接解析编码器编码的原始码的第一帧数据，第一帧一般为配置帧。H264编码中是按照sps，pps顺序拼接而成一帧；
    //  H265是按照vps，sps，pps顺序拼接成一帧。
    //  回调函数onOutputFormatChanged中，根据MediaFormat的csd-0，csd-1中分析出配置帧的数据。
    //  H264中sps为csd-0，pps为csd-1；H265中vps、sps、pps都在csd-0中按顺序拼接。

    //查找sps pps vps
    public static void searchSPSandPPSFromH264(ByteBuffer buffer, MediaFormat format) {
        String hex = HexUtil.bytesToHex(buffer.array());
        Log.e(TAG, "encodedFrame---" + hex);

        byte[] csd = buffer.array();

        int len = csd.length;
        int p = 4;
        int q = 4;

        if (csd[0] == 0 && csd[1] == 0 && csd[2] == 0 && csd[3] == 1) {
            // Parses the SPS and PPS, they could be in two different packets and in a different order
            //depending on the phone so we don't make any assumption about that
            while (p < len) {
                while (!(csd[p] == 0 && csd[p + 1] == 0 && csd[p + 2] == 0 && csd[p + 3] == 1) && p + 3 < len)
                    p++;
                if (p + 3 >= len) p = len;
                if ((csd[q] & 0x1F) == 7) {
                    byte[] sps = new byte[p - q];
                    System.arraycopy(csd, q, sps, 0, p - q);
                    Log.d(TAG, "colin, searchSPSandPPSFromH264 SPS=" + HexUtil.bytesToHex(sps));
                    format.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
                } else {
                    byte[] pps = new byte[p - q];
                    System.arraycopy(csd, q, pps, 0, p - q);
                    Log.d(TAG, "colin, searchSPSandPPSFromH264 PPS=" + HexUtil.bytesToHex(pps));
                    format.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
                }
                p += 4;
                q = p;
            }
        }

    }


    public static void searchVpsSpsPpsFromH265(ByteBuffer buffer, MediaFormat format) {
        String hex = HexUtil.bytesToHex(buffer.array());
        Log.e(TAG, "encodedFrame---" + hex);

        format.setByteBuffer("csd-0", buffer);

        int vpsPosition = -1;
        int spsPosition = -1;
        int ppsPosition = -1;
        int contBufferInitiation = 0;

        byte[] csdArray = buffer.array();
        for (int i = 0; i < csdArray.length; i++) {
            if (contBufferInitiation == 3 && csdArray[i] == 1) {
                if (vpsPosition == -1) {
                    vpsPosition = i - 3;
                } else if (spsPosition == -1) {
                    spsPosition = i - 3;
                } else {
                    ppsPosition = i - 3;
                }
            }
            if (csdArray[i] == 0) {
                contBufferInitiation++;
            } else {
                contBufferInitiation = 0;
            }
        }
        byte[] vps = new byte[spsPosition];
        byte[] sps = new byte[ppsPosition - spsPosition];
        byte[] pps = new byte[csdArray.length - ppsPosition];
        for (int i = 0; i < csdArray.length; i++) {
            if (i < spsPosition) {
                vps[i] = csdArray[i];
            } else if (i < ppsPosition) {
                sps[i - spsPosition] = csdArray[i];
            } else {
                pps[i - ppsPosition] = csdArray[i];
            }
        }

        Log.d(TAG, "colin, searchVpsSpsPpsFromH265:"
                + "\n" + "vps=" + HexUtil.bytesToHex(vps)
                + "\n" + "sps=" + HexUtil.bytesToHex(sps)
                + "\n" + "pps=" + HexUtil.bytesToHex(pps));
    }


}
