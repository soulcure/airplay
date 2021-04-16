package com.coocaa.interfaces;

import android.graphics.Path;

import java.text.ParseException;

/**
 * @Author: yuzhan
 */
public interface IPathParser {
    Path parse(String data) throws ParseException;
}
