package com.apiblast.customcode.example;

import com.divroll.astro.Astro;
import com.divroll.astro.AstroClient;

public class AstroKV {
    public static AstroKV INSTANCE = null;
    public static AstroKV getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new AstroKV();
        }
        return INSTANCE;
    }

    private AstroClient client;
    public void Astro() {
        String[] address = {Config.ASTRO_ADDRESS + ":" + Config.ASTRO_PORT};
        client = new AstroClient(address);
    }

    public AstroClient client() {
        return client;
    }

    public static Object put(Object key, Object value) {
        return getInstance().client().put(key, value);
    }

    public static Object get(Object key) {
        return getInstance().client().get(key);
    }

    public static void delete(Object key) {
        getInstance().client().delete(key);
    }

}
