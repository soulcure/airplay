package swaiotos.runtime.h5.common.util;

import java.util.HashMap;
import java.util.Map;

public class URLSplitUtil {
    private static String TruncateUrlPage(String strURL){
        String strAllParam=null;
        String[] arrSplit=null;
        strURL=strURL.trim().toLowerCase();
        arrSplit=strURL.split("[?]");
        if(strURL.length()>1){
            if(arrSplit.length>1){
                for (int i=1;i<arrSplit.length;i++){
                    strAllParam = arrSplit[i];
                }
            }
        }
        return strAllParam;
    }

    public static Map<String, String> urlSplit(String URL){
        Map<String, String> mapRequest = new HashMap<String, String>();
        String[] arrSplit=null;
        String strUrlParam=TruncateUrlPage(URL);
        if(strUrlParam==null){
            return mapRequest;
        }
        arrSplit=strUrlParam.split("[&]");
        for(String strSplit:arrSplit){
            String[] arrSplitEqual=null;
            arrSplitEqual= strSplit.split("[=]");
            //解析出键值
            if(arrSplitEqual.length>1){
                //正确解析
                if(arrSplitEqual[1]!=null && arrSplitEqual[1].contains("#")){
                    String[] rel_arrSplitEqual=null;
                    // 处理VUE#路由框架
                    rel_arrSplitEqual= arrSplitEqual[1].split("[#]");
                    mapRequest.put(arrSplitEqual[0], rel_arrSplitEqual[0]);
                }else{
                    mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
                }
            }else{
                if(arrSplitEqual[0]!=""){
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }
}
