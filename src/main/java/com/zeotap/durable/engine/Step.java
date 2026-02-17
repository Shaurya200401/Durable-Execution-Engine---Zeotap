package com.zeotap.durable.engine;

@FunctionalInterface
public interface Step<T> {
    T execute() throws Exception;
}
