package com.zsf.interpreter.expressions;

import java.io.Serializable;

/**
 * Created by hasee on 2016/12/27.
 */
public abstract class Expression implements Serializable,Score {

    protected double sceneWeight=1.0;

    public abstract String toString();
    public abstract Expression deepClone();
    public abstract int deepth();

    public void setSceneWeight(double sceneWeight) {
        this.sceneWeight = sceneWeight;
    }

    public double getSceneWeight() {
        return sceneWeight;
    }
}
