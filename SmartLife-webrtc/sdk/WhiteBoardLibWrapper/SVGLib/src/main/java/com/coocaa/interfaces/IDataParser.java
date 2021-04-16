package com.coocaa.interfaces;

public interface IDataParser<T> {
    <T> T parse(String data);
}
