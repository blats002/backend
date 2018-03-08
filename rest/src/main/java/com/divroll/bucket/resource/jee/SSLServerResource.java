package com.divroll.bucket.resource.jee;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.divroll.bucket.Configuration;
import com.divroll.bucket.resource.SSLResource;
import com.divroll.bucket.service.CertificateHelper;
import com.divroll.bucket.service.HttpChallengeListener;
import com.divroll.bucket.validator.EmailValidator;
import com.mashape.unirest.http.exceptions.UnirestException;
import it.zero11.acme.Acme;
import it.zero11.acme.AcmeChallengeListener;
import it.zero11.acme.storage.impl.DefaultCertificateStorage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

public class SSLServerResource extends BaseServerResource
    implements SSLResource {

    private static final Logger LOG
            = Logger.getLogger(SSLServerResource.class.getName());

    private String appObjectId = null;

    private static final String CERTIFICATE_HEADER = "-----BEGIN CERTIFICATE-----";
    private static final String CERTIFICATE_FOOTER = "-----END CERTIFICATE-----";
    private static final String PRIVATE_KEY_HEADER = "-----BEGIN PRIVATE KEY-----";
    private static final String PRIVATE_KEY_FOOTER = "-----END PRIVATE KEY-----";

    private static final String CA_STAGING_URL = "https://acme-staging.api.letsencrypt.org/acme";
    private static String CA_PRODUCTION_URL = "";
    private static String AGREEMENT_URL = "";

    @Override
    public Representation post(Representation entity) {
        LOG.info("Domain: " + domain);
        LOG.info("Subdomain: " + subdomain);
        JSONObject responseObject = new JSONObject();
        if(!hasUserRole()) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }
        if(subdomain == null || subdomain.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }
        if(domain == null || domain.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }
        if(!checkDomainOwner(domain)) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            responseObject.put("code", Status.CLIENT_ERROR_UNAUTHORIZED.getCode());
            responseObject.put("reason", "Domain not owned");
            Representation representation = new StringRepresentation(responseObject.toJSONString());
            representation.setMediaType(MediaType.APPLICATION_JSON);
            return representation;
        }
        try {
            String body = entity.getText();
            JSONObject jsonObject = JSONObject.parseObject(body);
            String certificate = jsonObject.getString("certificate");
            String privateKey = jsonObject.getString("privateKey");
            if(certificate.startsWith(CERTIFICATE_HEADER) && certificate.endsWith(CERTIFICATE_FOOTER)
                    && privateKey.startsWith(PRIVATE_KEY_HEADER) && privateKey.endsWith(PRIVATE_KEY_FOOTER)) {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            jelasticService.writeCertificateAndPrivateKeyFile(domain, certificate, privateKey);
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
            //LOG.info(domain);
            //LOG.info(body);
            responseObject.put("code", Status.SUCCESS_OK.getCode());
            responseObject.put("reason", Status.SUCCESS_OK.getReasonPhrase());
            setStatus(Status.SUCCESS_OK);
        } catch (Exception e) {
            responseObject = new JSONObject();
            responseObject.put("code",Status.SERVER_ERROR_INTERNAL.getCode());
            responseObject.put("reason",Status.SERVER_ERROR_INTERNAL.getReasonPhrase());
            setStatus(Status.SERVER_ERROR_INTERNAL);
            e.printStackTrace();
        }
        Representation representation = new StringRepresentation(responseObject.toJSONString());
        representation.setMediaType(MediaType.APPLICATION_JSON);
        return representation;
    }

    @Override
    public Representation get(Representation entity) {
        JSONObject responseObject = new JSONObject();
        try {
            if(!hasUserRole()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }

            if(subdomain == null || subdomain.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }
            // Check if Subdomain exists
            // and owned by client
            JSONObject whereObject = new JSONObject();
            whereObject.put("appId", subdomain);

            HttpResponse<String> getRequest = Unirest.get(Configuration.DIVROLL_PARSE_URL +
                    "/classes/Application")
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.DIVROLL_PARSE_REST_API_KEY)
                    .header(X_PARSE_SESSION_TOKEN, sessionToken)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = getRequest.getBody();
            //String appObjectId = null;

            if(getRequest.getStatus() == 200) {
                JSONObject results = JSON.parseObject(body);
                JSONArray resultsArray = results.getJSONArray("results");
                if(!resultsArray.isEmpty()){
                    for(int i=0;i<resultsArray.size();i++){
                        JSONObject jsonObject = resultsArray.getJSONObject(i);
                        LOG.info("jsonObject: " + jsonObject.toJSONString());
                        String appId = jsonObject.getString("objectId");
                        String appSubdomain = jsonObject.getString("appId");
                        JSONObject userPointer = jsonObject.getJSONObject("userId");
                        if(userPointer == null) {
                            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                            return null;
                        } else {
                            String id = userPointer.getString("objectId");
                            if(userId.equals(id) && subdomain.equals(appSubdomain)) {
                                appObjectId = appId;
                                break;
                            }
                        }
                    }
                }

                LOG.info("Subdomain: " + subdomain);
                LOG.info("Application ID: " + appObjectId);

                if(appObjectId == null || appObjectId.isEmpty()) {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    return null;
                }

                try {
                    HttpResponse<String> res = Unirest.get(Configuration.DIVROLL_PARSE_URL + "/config")
                            .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                            .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                            .header("Content-Type", "application/json")
                            .asString();
                    String resBody = res.getBody();
                    JSONObject config = JSONObject.parseObject(resBody);
                    CA_PRODUCTION_URL = config.getJSONObject("params").getString("LETSENCRYPT_PRODUCTION_URL");
                    AGREEMENT_URL = config.getJSONObject("params").getString("LETSENCRYPT_AGREEMENT_URL");
                } catch (UnirestException e) {
                    LOG.info("Error " + e.getMessage());
                }

                ///////////////////////////////
                // Let's Encrypt !
                //////////////////////////////


                Security.addProvider(new BouncyCastleProvider());
                //String mailTo = "mailto:webmaster@***REMOVED***";

                JSONObject userObject = JSONObject.parseObject(user);
                String mailTo = userObject.getString("email");

                if(mailTo == null || mailTo.isEmpty() || !EmailValidator.validate(mailTo)) {
                    return badRequest("Must have valid email");
                }

                //LOG.info("WARNING: this sample application is using the Let's Encrypt staging API. Certificated created with this application won't be trusted.");
                LOG.info("By using this application you agree to Let's Encrypt Terms and Conditions");
                LOG.info(AGREEMENT_URL);
                String[] domains = new String[1];
                String[] contacts = new String[1];
                domains[0] = domain;
                contacts[0] = "mailto:" + mailTo;
                LOG.info("Mail to: " + mailTo);
                LOG.info("Session Token: " + sessionToken);

                //AGREEMENT_URL = "https://letsencrypt.org/documents/LE-SA-v1.2-November-15-2017.pdf";

                AcmeChallengeListener challengeListener = new HttpChallengeListener(sessionToken, subdomain, userId, domains[0], "");
                Acme acme = new Acme(CA_PRODUCTION_URL, new DefaultCertificateStorage(true), true, true);
                X509Certificate cert = acme.getCertificate(domains, AGREEMENT_URL, contacts, challengeListener);

                // TODO:
                KeyPair domainKey = acme.getCertificateStorage().getDomainKeyPair(domains);
                PrivateKey privateKey = domainKey.getPrivate();

                String fullchain = CertificateHelper.x509ToBase64PEMString(cert);
                String privateKeyString =  CertificateHelper.writePrivateKeyToPEMString(privateKey);

                LOG.info(fullchain);
                LOG.info(privateKeyString);
                ////////////////////////////////
                jelasticService.writeCertificateAndPrivateKeyFile(domains[0], fullchain, privateKeyString);
                ////////////////////////////////
                responseObject.put("code", Status.SUCCESS_OK.getCode());
                responseObject.put("reason", Status.SUCCESS_OK.getReasonPhrase());

                JSONObject certificate = new JSONObject();
                certificate.put("certificate", fullchain);
                certificate.put("privateKey", privateKeyString);

                responseObject.put("result", certificate);

                setStatus(Status.SUCCESS_OK);
                Representation representation = new StringRepresentation(responseObject.toJSONString());
                representation.setMediaType(MediaType.APPLICATION_JSON);
                return representation;
            } else {
                return badRequest();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("Failed to get a certificate for domains " + domain);
            return internalError();
        }
    }

    private boolean checkDomainOwner(String domain) {
        boolean isOwner = false;
        try {
            JSONObject whereObject = new JSONObject();
            whereObject.put("name", domain);
            HttpResponse<String> quotaRequest = Unirest.get(Configuration.DIVROLL_PARSE_URL +
                    "/classes/Domain")
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.DIVROLL_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = quotaRequest.getBody();
            JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
            if(!jsonArray.isEmpty()) {
                for(int i=0;i<jsonArray.size();i++){
                    JSONObject domainObject = jsonArray.getJSONObject(i);
                    String name = domainObject.getString("name");
                    String objectId = domainObject.getString("objectId");
                    JSONObject appPointer = domainObject.getJSONObject("appId");
                    if(domain.equals(name)) {
                        // Found domain
                        String appId = appPointer.getString("objectId");
                        JSONObject application = getApplicationByUser(subdomain, userId);
                        if(application != null) {
                            LOG.info("Application: " + appId);
                            LOG.info("Application: " + application.getString("objectId"));
                            if(appId.equals(application.getString("objectId"))) {
                                isOwner = true;
                            }
                        } else {
                            LOG.info("No application found for " + domain);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOwner;
    }
}
