package com.victor.design.behavioral.observer;

public abstract class Observer {
    protected Subject subject;
    public abstract void update();
}
