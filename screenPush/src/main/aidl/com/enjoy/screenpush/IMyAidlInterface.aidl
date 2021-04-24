// IMyAidlInterface.aidl
package com.enjoy.screenpush;

// Declare any non-default types here with import statements

interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    /**
     * 提供给第二个app使用的接口
     */
    String getName();

    /**
     * 提供录屏数据
     */
}