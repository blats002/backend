package com.divroll.bucket.service;

/**
 * Created by paperspace on 6/24/2017.
 */
public interface PuppetCallback {
    void onSuccess(String response);
    void onError(String error);
}
