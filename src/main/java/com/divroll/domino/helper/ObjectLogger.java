package com.divroll.domino.helper;

import com.google.gson.Gson;

import java.util.logging.Logger;

public class ObjectLogger {
    private static final Logger LOG
            = Logger.getLogger(ObjectLogger.class.getName());
    public static Object LOG(Object object) {
        String json = new Gson().toJson(object);
        LOG.info(json);
        return object;
    }
}
