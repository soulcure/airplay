package com.coocaa.smartscreen.repository.http;

import com.coocaa.smartscreen.constant.SmartConstans;

/**
 * @Author: yuzhan
 */
public class HttpServer {
    private final static HttpServer instance = new HttpServer();

    private static IServer release = new IServer() {
        @Override
        public String url() {
            return "https://api.skyworthiot.com/";
        }
    };

    private static IServer test = new IServer() {
        @Override
        public String url() {
            return "https://api-sit.skyworthiot.com/";
        }
    };

    private static IServer beta = new IServer() {
        @Override
        public String url() {
            return "https://dot-dev.skyworthiot.com/";
        }
    };

    private HttpServer(){}

    public static HttpServer getInstance() {
        return instance;
    }

    private static IServer server = getServer();

    private interface IServer {
        String url();
    }

    public String getServerUrl() {
        return server.url();
    }

    private static IServer getServer() {
        if(SmartConstans.isBetaServer()) {
            return beta;
        }
        return SmartConstans.isTestServer() ? test : release;
    }
}
