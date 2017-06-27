package com..bucket.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jelastic.api.Callback;
import com.jelastic.api.Response;
import com.jelastic.api.environment.Control;
import com.jelastic.api.environment.File;
import com.jelastic.api.environment.response.ExecResponse;
import com.jelastic.api.environment.response.NodeSSHResponses;
import com.jelastic.api.users.Authentication;
import com.jelastic.api.users.response.AuthenticationResponse;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com..bucket.Configuration;
import com..bucket.Template;

import java.util.logging.Logger;

public class JelasticService {
    private static final Logger LOG
            = Logger.getLogger(JelasticService.class.getName());

    protected final static String CERTIFICATE_PATH_TEMPLATE = "/var/lib/nginx/ssl/DOMAIN_NAME.crt";
    protected final static String PRIVATE_KEY_PATH_TEMPLATE = "/var/lib/nginx/ssl/DOMAIN_NAME.key";
    protected final static String NGINX_CONF_PATH_TEMPLATE = "/etc/nginx/conf.d/DOMAIN_NAME.conf";

    public String getSession() {
        LOG.info("Authenticate user...");
        Authentication authenticationService = new Authentication(Configuration.JELASTIC_CLUSTER_APPID);
        authenticationService.setServiceUrl(Configuration.JELASTIC_AUTH_HOSTER_URL);
        AuthenticationResponse authenticationResponse = authenticationService.signin(Configuration.JELASTIC_USER_EMAIL,
                Configuration.JELASTIC_USER_PASSWORD);
        LOG.info("Signin response: " + authenticationResponse);
        if (!authenticationResponse.isOK()) {
            return null;
        }
        final String session = authenticationResponse.getSession();
        return session;
    }

    public void writeCertificateAndPrivateKeyFile(final String domain, final String certificate, final String privateKey) {
        LOG.info("Domain: " + domain);
        LOG.info("Certificate: " + certificate);
        LOG.info("Private Key: " + privateKey);
        final String session = getSession();
        writeCertificateFile(session, domain, certificate);
        writePrivateKeyFile(session, domain, privateKey);
        writeNginxConfFile(session, domain, Template.createFromTemplate(domain));
        reloadNginx(session);
        LOG.info("Done NGINX reload");
    }

    public void writeCertificateFile(String session, String domain, String certificate) {
        String completePath = CERTIFICATE_PATH_TEMPLATE.replaceAll("DOMAIN_NAME", domain);
        LOG.info(completePath);
        writeFile(session, completePath, certificate);
    }

    public void writePrivateKeyFile(String session, String domain, String privateKey) {
        String completePath = PRIVATE_KEY_PATH_TEMPLATE.replaceAll("DOMAIN_NAME", domain);
        LOG.info(completePath);
        writeFile(session, completePath, privateKey);
    }

    public void writeNginxConfFile(String session, String domain, String configuration) {
        String completePath = NGINX_CONF_PATH_TEMPLATE.replaceAll("DOMAIN_NAME", domain);
        LOG.info(completePath);
        writeFile(session, completePath, configuration);
    }

    @Deprecated
    public void writeFileOld(String session, String path, String body) {
        LOG.info("Session: " + session);
        LOG.info("Path: " + path);
        if(session != null) {
            File fileService = new File("c5803c35c13c538eb0ef83a07ecd23c4");
            fileService.setServerUrl(Configuration.JELASTIC_HOSTER_URL);
            String nodeType = "nginx";
            // indicates whether to execute this command only at master/primary node of application server
            // or at all application server nodes
            Boolean masterOnly = false;
            Boolean isdir = false;
            NodeSSHResponses responses = fileService.write(session, path, body, nodeType, masterOnly, Configuration.JELASTIC_NGINX_NODE_ID);
            LOG.info("Write File Done: " + responses.getRaw());
        }
    }

    private static void writeFile(String session, String path, String body) {
        System.out.println("Session: " + session);
        System.out.println("Path: " + path);
        if (session != null) {
            File fileService = new File(Configuration.JELASTIC_ENV_NAME);
            fileService.setServerUrl(Configuration.JELASTIC_HOSTER_URL);

            String nodeType = "nginx";

            NodeSSHResponses responses = fileService.write(session, path, body, nodeType, "cp", false, Configuration.JELASTIC_NGINX_NODE_ID);

            System.out.println("Error: " + responses.getError());
            System.out.println("Response: " + responses.getResult());
        }
    }

    private void reloadNginx(String s) {
        /*
        try {
            String session = getSession();
            JSONArray commandlist = new JSONArray();
            JSONObject command = new JSONObject();
            command.put("command", "sudo service nginx reload");
            //command.put("params", "");
            commandlist.add(command);
            LOG.info(commandlist.toJSONString());
            Control environmentService = new Control();
            environmentService.setServerUrl(Configuration.JELASTIC_HOSTER_URL);
            ExecResponse response = environmentService.execCmdById(Configuration.JELASTIC_ENV_NAME, session, Configuration.JELASTIC_NGINX_NODE_ID, commandlist.toString(), true);
            LOG.info("Exec error: " + response.getError());
            LOG.info("Exec raw: " + response.getRaw());
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        String HOSTER_URL = "https://app.divroll.space/";
        String USER_EMAIL = Configuration.JELASTIC_USER_EMAIL;
        String USER_PASSWORD = Configuration.JELASTIC_USER_PASSWORD;
        String ENV_NAME = Configuration.JELASTIC_ENV_NAME;
        Integer ContainerID = Configuration.JELASTIC_NGINX_NODE_ID;
        JSONArray commandlist = new JSONArray();
        JSONObject command = new JSONObject();
        Authentication authenticationService = new Authentication(Configuration.JELASTIC_CLUSTER_APPID);
        authenticationService.setServiceUrl(HOSTER_URL + "1.0/users/authentication/");
        AuthenticationResponse authenticationResponse = authenticationService.signin(USER_EMAIL, USER_PASSWORD);
        LOG.info("Signin response: " + authenticationResponse);
        if (authenticationResponse.isOK()) {
            String session = authenticationResponse.getSession();
            try {
                command.put("command", "sudo service nginx reload");
                commandlist.add(command);
                Control environmentService = new Control();
                environmentService.setServerUrl(HOSTER_URL + "1.0/");
                ExecResponse res = environmentService.execCmdById(ENV_NAME, session, ContainerID, commandlist.toString(), true);
                LOG.info("<br>Exec response: " + res.toString());
            }
            catch (Exception e) {
                LOG.info(e.getMessage());
            }
        }
    }

}
