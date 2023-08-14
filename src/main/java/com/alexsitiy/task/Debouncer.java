package com.alexsitiy.task;

public interface Debouncer<T> extends Callback<T> {

    void shutdown();
}
