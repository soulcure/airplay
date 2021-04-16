package com.coocaa.movie.web.product;

import com.coocaa.movie.web.base.HttpExecption;

/**
 * Created by Sea .
 */

public interface PayHttpCallback<T> {
    public void callback(T t);
    public void error(HttpExecption e);
}
