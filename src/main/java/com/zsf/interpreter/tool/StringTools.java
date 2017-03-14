package com.zsf.interpreter.tool;

/**
 * Created by hasee on 2017/2/27.
 */
public class StringTools {
    public static String getReversedStr(String String) {
        return new StringBuilder(String).reverse().toString();
    }

    public static String getCommonStr(String string1, String string2) {
        int len=Math.min(string1.length(),string2.length());
        String commonStr="";
        for (int i=0;i<len;i++){
            if(string1.charAt(i)==string2.charAt(i)){
                commonStr+=string1.charAt(i);
            }else {
                break;
            }
        }
        return commonStr;
    }
}
