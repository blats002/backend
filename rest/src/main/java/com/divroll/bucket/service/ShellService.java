package com.divroll.bucket.service;

import com.divroll.bucket.Template;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ShellService {
    private static final Logger LOG
            = Logger.getLogger(ShellService.class.getName());

    protected final static String CERTIFICATE_PATH_TEMPLATE = "/etc/ssl/certs/DOMAIN_NAME.crt";
    protected final static String PRIVATE_KEY_PATH_TEMPLATE = "/etc/ssl/certs/DOMAIN_NAME.key";
    protected final static String NGINX_CONF_PATH_TEMPLATE = "/etc/nginx/conf.d/DOMAIN_NAME.conf";

    public void writeCertificateAndPrivateKeyFile(final String domain, final String certificate, final String privateKey) throws IOException, InterruptedException {
        LOG.info("Domain: " + domain);
        LOG.info("Certificate: " + certificate);
        LOG.info("Private Key: " + privateKey);

        String certPath = writeCertificateFile(domain, certificate);
        String keyPath = writePrivateKeyFile(domain, privateKey);
        String confPath = writeNginxConfFile(domain, Template.createFromTemplate(domain));

        if(!validateNginx()) {
            removeFile(certPath);
            removeFile(keyPath);
            removeFile(confPath);
        }
        LOG.info("Done NGINX reload");
    }

    public String writeCertificateFile(String domain, String certificate) throws IOException, InterruptedException {
        String completePath = CERTIFICATE_PATH_TEMPLATE.replaceAll("DOMAIN_NAME", domain);
        LOG.info(completePath);
        writeFile(completePath, certificate);
        return completePath;
    }

    public String writePrivateKeyFile(String domain, String privateKey) throws IOException, InterruptedException {
        String completePath = PRIVATE_KEY_PATH_TEMPLATE.replaceAll("DOMAIN_NAME", domain);
        LOG.info(completePath);
        writeFile(completePath, privateKey);
        return completePath;
    }

    public String writeNginxConfFile(String domain, String configuration) throws IOException, InterruptedException {
        String completePath = NGINX_CONF_PATH_TEMPLATE.replaceAll("DOMAIN_NAME", domain);
        LOG.info(completePath);
        writeFile(completePath, configuration);
        return completePath;
    }

    private static void writeFile(String path, String str) throws IOException, InterruptedException {
        LOG.info("Path: " + path);
        runCommand("echo '" + str + "' > " + path);
//        FileOutputStream outputStream = new FileOutputStream(path);
//        byte[] strToBytes = str.getBytes();
//        outputStream.write(strToBytes);
//        outputStream.close();
    }

    private static void removeFile(String path) throws IOException, InterruptedException {
        runCommand("rm " + path);
    }

    private boolean validateNginx() throws IOException, InterruptedException {
        int result = runCommand("nginx -t");
        return result == 0;
    }

    private void reloadNginx() throws IOException, InterruptedException {
        runCommand("nginx -s reload");
    }


    public static int runCommand(String command) throws IOException, InterruptedException {
        LOG.info("Command: " + command);
        HttpClient client = new DefaultHttpClient();
        String url = "http://172.104.190.149:1234/shell";
        LOG.info("URL: " + url);
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(command, ContentType.TEXT_PLAIN));
        request.setHeader("Content-type", "text/plain");
        HttpResponse response = client.execute(request);
        // Get the response
        BufferedReader rd = new BufferedReader
                (new InputStreamReader(
                        response.getEntity().getContent()));
        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        JsonObject jsonObject = new JsonParser().parse(sb.toString()).getAsJsonObject();
        String jsonResponse = jsonObject.get("response").getAsString();
        LOG.info("JSON Response: "+ jsonResponse);
        int exitCode = jsonObject.get("exitCode").getAsInt();
        return exitCode;
    }


}
