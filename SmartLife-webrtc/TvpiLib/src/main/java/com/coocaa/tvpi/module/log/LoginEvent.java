package com.coocaa.tvpi.module.log;

import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.google.gson.Gson;

/**
 * @Author: yuzhan
 */
public class LoginEvent {

    public static void submitLogin(String eventName) {
        PayloadEvent.submit("smartscreen.login", eventName, Repository.get(LoginRepository.class).queryCoocaaUserInfo());
    }
}
