package com.zsf.tool;

/**
 * Created by hasee on 2017/2/3.
 */
public class RunTimeMeasurer {
    private static long startTime;   //获取开始时间

    public static void startTiming() {
        startTime = System.currentTimeMillis();
    }

    public static void endTiming() {
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("运行时间： " + (endTime - startTime) + "ms");
    }

    public static void endTiming(String tag) {
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("["+tag+"] 运行时间： " + (endTime - startTime) + "ms");
    }
}
