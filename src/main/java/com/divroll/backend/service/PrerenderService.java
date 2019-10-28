package com.divroll.backend.service;

public interface PrerenderService {
    String prerender(String url, String escapedFragment);
}
