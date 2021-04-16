package com.coocaa.tvpi.util.permission;

public interface PermissionListener {

    /**
     * 通过授权
     * @param permission
     */
    void permissionGranted(String[] permission);

    /**
     * 拒绝授权
     * @param permission
     */
    void permissionDenied(String[] permission);
}
