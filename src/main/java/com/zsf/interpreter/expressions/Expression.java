package com.zsf.interpreter.expressions;

import java.io.Serializable;

/**
 * Created by hasee on 2016/12/27.
 */
public abstract class Expression implements Serializable,Score {

    public abstract String toString();
    public abstract Expression deepClone();
    public abstract int deepth();
}
