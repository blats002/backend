package com.divroll.backend.pubsub;

import com.divroll.backend.Constants;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationInterceptor extends AtmosphereInterceptorAdapter {

    private final static Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    @Override
    public Action inspect(AtmosphereResource r) {
        logger.info("AuthenticationInterceptor - inspect method called");
        if (r.getRequest().getMethod().equalsIgnoreCase("POST")) {
            String appId = r.getRequest().getHeader(Constants.HEADER_APP_ID);
            String masterKey = r.getRequest().getHeader(Constants.HEADER_MASTER_KEY);
            if(!isMaster(appId, masterKey)) {
                return Action.CANCELLED;
            }
        } else if(r.getRequest().getMethod().equalsIgnoreCase("GET")) {
            String appId = r.getRequest().getHeader(Constants.HEADER_APP_ID);
            String apiKey = r.getRequest().getHeader(Constants.HEADER_API_KEY);
            if(!isAuthorized(appId, apiKey)) {
                return Action.CANCELLED;
            }
        }
        return Action.CONTINUE;
    }

    private boolean isMaster(String appId, String masterKey) {
        return false;
    }

    private boolean isAuthorized(String appId, String apiKey) {
        return false;
    }

}
