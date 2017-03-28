package com.zsf.flashextract.feregex.feinterfaces;

/**
 * Created by zsf on 2017/3/28.
 */
public interface DynimicRegexTools {
    /**
     * 判断某个subStr是否应该被作为dynamicToken
     * @param subStr
     * @return
     */
    boolean needAddDynimicToken(String subStr);
}
