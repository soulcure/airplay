package com.swaiotos.skymirror.sdk.capture;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SocketClientTask extends Thread {
    private String mAddress;
    //端口
    private int port;
    private String TAG = "SocketClient";
    //发送内容
//    Socket mSocket = null;
//    private DataOutputStream out = null;
//    private DataInputStream in =null;// 输入流
    private final int MAX_TCP_SEND_DATA_SIZE = 1460;//TCP 包的大小就应该是 1500 - IP头(20) - TCP头(20) = 1460 (Bytes)
    private final int THREAD_POOL_NUM = 10;
;

    private ArrayList<DataOutputStream> out = new ArrayList<DataOutputStream>();;
    private ArrayList<DataInputStream> in = new ArrayList<DataInputStream>();;// 输入流
    private ArrayList<Socket> mSocket =  new ArrayList<Socket>();
    private boolean isConnected = false;

    public SocketClientTask(String address, final int port) {
        Log.d(TAG, "create SocketClientTask ");
        this.mAddress = address;
        this.port = port;
        DataSocketClientThread st =  new DataSocketClientThread();

        new Thread(st).start();
    }

    private void initThread(){
        Socket socket = null;
        int i=0;
        isConnected =false;
        for(i=0;i<THREAD_POOL_NUM;i++){
            try {
                //1.创建监听指定服务器地址以及指定服务器监听的端口号
                //IP地址，端口号
                Log.d(TAG, "create socket on port "+(port+i));
                socket = new Socket();
                SocketAddress address = new InetSocketAddress(mAddress, port+i);
                socket.connect(address, 10000);// 连接指定IP和端口
                if (socket.isConnected()) {
                    Log.d(TAG, "connect socket sucess ");
                    isConnected =true;
                    mSocket.add(socket);
                    out.add(new DataOutputStream(socket.getOutputStream()));
                    in.add(new DataInputStream(socket.getInputStream()));
                } else {
                    Log.d(TAG, "connect socket failed on port :"+(port+i));
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isOpen(){
        return isConnected;
    }

    class DataSocketClientThread implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "create DataSocketClientThread ");
            initThread();
        }
    }
    /**
     * 设置
     */
    public boolean sendData(byte frameIdx, byte[] data ) throws IOException {
        //4 is header for each slice
        int socket_num = data.length /(MAX_TCP_SEND_DATA_SIZE -8);
        if( (data.length % (MAX_TCP_SEND_DATA_SIZE -8) ) != 0){
            socket_num++;
        }

        if(socket_num > THREAD_POOL_NUM){
            socket_num = THREAD_POOL_NUM;
        }
//        Log.d(TAG,"sendData with socket number"+socket_num);
        int max_slice_size =0;
        if(socket_num < THREAD_POOL_NUM){
            max_slice_size = MAX_TCP_SEND_DATA_SIZE - 8;
        }
        else {
            max_slice_size = data.length / socket_num + socket_num;
        }

//        Log.d(TAG,"sendData with max len"+max_slice_size);
        int pos = 0;
//        try {

            try {
                for (int i = 0; i < socket_num; i++) {
                    int sendSize = 0;
                    if (i != (socket_num - 1)) {
                        sendSize = max_slice_size;
                    } else {
                        sendSize = data.length - max_slice_size * (socket_num - 1);
                    }


                    byte[] sdata = new byte[sendSize + 8];

                    sdata[0] = (byte) ((sendSize >> 0) & 0xff);
                    sdata[1] = (byte) ((sendSize >> 8) & 0xff);
                    sdata[2] = (byte) ((sendSize >> 16) & 0xff);
                    sdata[3] = (byte) ((sendSize >> 24) & 0xff);

                    sdata[4] = (byte) (socket_num);
                    sdata[5] = (byte) (i);
                    sdata[6] = (byte) (frameIdx & 0xff);//frameIndex
                    sdata[7] = 0;//reserved

                    System.arraycopy(data, pos, sdata, 8, sendSize);
                    /*if ( i >= (out.size() - 1)) {
                        break;
                    }*/
                    DataOutputStream o = out.get(i);
                    o.write(sdata);
                    o.flush();
                    pos += sendSize;
                }
            } catch (Exception e) {

            }
//        }
//        catch (IOException e) {
//            Log.d(TAG, "Send Socket error");
//            return false;
//        }

        return true;
    }

    public void close(){
        for(int i=0;i<mSocket.size();i++){
            Socket socket = mSocket.get(i);
            try {
                if (!socket.isInputShutdown()) {
                    socket.shutdownInput();
                }
                if (!socket.isOutputShutdown()) {
                    socket.shutdownOutput();
                }

                DataOutputStream o = out.get(i);
                o.close();
                DataInputStream x= in.get(i);
                x.close();

                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        mSocket.clear();
        out.clear();
        in.clear();
    }
}
