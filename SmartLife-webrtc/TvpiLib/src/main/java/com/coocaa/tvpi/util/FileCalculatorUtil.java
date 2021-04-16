package com.coocaa.tvpi.util;

public class FileCalculatorUtil {

    public static String getFileSize(long tempSize) {

        double size = Double.valueOf(tempSize) / 1024 / 1024;
        if(0<=size && size < 10){
            return "[0,10)";
        }else if(10<=size && size < 50){
            return "[10,50)";
        }else if(50<=size && size < 100){
            return "[50,100)";
        }else if(100<=size && size < 200){
            return "[100,200)";
        }else if(200<=size && size < 300){
            return "[200,300)";
        }else if(300<=size && size < 400){
            return "[300,400)";
        }else if(400<=size && size < 500){
            return "[400,500)";
        }else if(500<=size && size < 1000){
            return "[500,1000)";
        }else if(1000<=size && size < 2000){
            return "[1000,2000)";
        }else {
            return ">=2000";
        }
    }
}
