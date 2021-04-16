package com.coocaa.tvpi;
import com.coocaa.tvpi.bean.CCPayReq;
import com.coocaa.tvpi.bean.CCPayResp;
import com.coocaa.tvpi.IIPayResultCallback;
interface IIPay {
   CCPayResp pay(in CCPayReq type);
   void addCallback(in IIPayResultCallback callback);
}
