package com.divroll.bucket.service;

import java.util.function.Consumer;

public class StringConsumer implements Consumer<String> {

    private String consumed = "";

    @Override
    public void accept(String s) {
        if(s != null) {
            consumed = consumed + s;
        }
    }

    public String getConsumed() {
        return consumed;
    }

}
