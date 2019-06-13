package com.divroll.bucket.resource.jee;

import com.alibaba.fastjson.JSONObject;
import com.divroll.backend.sdk.Divroll;
import com.divroll.backend.sdk.DivrollEntities;
import com.divroll.backend.sdk.DivrollUser;
import com.divroll.backend.sdk.filter.EqualQueryFilter;
import com.divroll.backend.sdk.filter.QueryFilter;
import com.divroll.bucket.Cert;
import com.divroll.bucket.ClientTest;
import com.divroll.bucket.resource.SSLResource;
import com.divroll.bucket.validator.CertificateValidator;
import com.divroll.bucket.validator.EmailValidator;
import com.divroll.bucket.validator.PrivateKeyValidator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.shredzone.acme4j.exception.AcmeRateLimitedException;

import java.security.Security;
import java.util.Arrays;
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
    private static final String PRIVATE_KEY_HEADER_ALT = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String PRIVATE_KEY_FOOTER_ALT = "-----END RSA PRIVATE KEY-----";

    private static String AGREEMENT_URL = "https://letsencrypt.org/documents/LE-SA-v1.1.1-August-1-2016.pdf";

    private static final String DIVROLL_APP_ID = "***REMOVED***";
    private static final String DIVROLL_API_KEY = "***REMOVED***";
    private static final String DIVROLL_MASTER_KEY = "***REMOVED***";

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        Divroll.initialize("https://www.divroll.xyz/divroll", DIVROLL_APP_ID,
                DIVROLL_API_KEY, DIVROLL_MASTER_KEY);
    }

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
                    && (privateKey.startsWith(PRIVATE_KEY_HEADER) || privateKey.startsWith(PRIVATE_KEY_HEADER_ALT))
                    && (privateKey.endsWith(PRIVATE_KEY_FOOTER) || privateKey.endsWith(PRIVATE_KEY_FOOTER_ALT))) {
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

            DivrollEntities entities = new DivrollEntities("Subdomain");
            entities.query(new EqualQueryFilter("subdomain", subdomain));

            if(entities.getEntities() != null && !entities.getEntities().isEmpty()) {
                entities.getEntities().forEach(e -> {
                    if(e.getProperty("subdomain").equals(subdomain)) {
                        appObjectId = e.getEntityId();
                    }
                });
                LOG.info("Subdomain: " + subdomain);
                LOG.info("Application ID: " + appObjectId);

                if(appObjectId == null || appObjectId.isEmpty()) {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    return null;
                }

                //CA_PRODUCTION_URL = config.getJSONObject("params").getString("LETSENCRYPT_PRODUCTION_URL");
                //AGREEMENT_URL = config.getJSONObject("params").getString("LETSENCRYPT_AGREEMENT_URL");

                ///////////////////////////////
                // Let's Encrypt !
                //////////////////////////////

                Security.addProvider(new BouncyCastleProvider());

                DivrollUser divrollUser = new DivrollUser();
                divrollUser.setEntityId(userId);
                divrollUser.setAuthToken(authToken);
                divrollUser.retrieve();
                String mailTo = divrollUser.getUsername();
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
                LOG.info("Auth Token: " + authToken);

                //AGREEMENT_URL = "https://letsencrypt.org/documents/LE-SA-v1.2-November-15-2017.pdf";

                /*
                AcmeChallengeListener challengeListener = new HttpChallengeListener(authToken, subdomain, userId, domains[0], "");
                Acme acme = new Acme(CA_PRODUCTION_URL, new DefaultCertificateStorage(true), true, true);
                X509Certificate cert = acme.getCertificate(domains, AGREEMENT_URL, contacts, challengeListener);
                KeyPair domainKey = acme.getCertificateStorage().getDomainKeyPair(domains);
                PrivateKey privateKey = domainKey.getPrivate();

                String fullchain = CertificateHelper.x509ToBase64PEMString(cert);
                String privateKeyString =  CertificateHelper.writePrivateKeyToPEMString(privateKey);

                LOG.info(fullchain);
                LOG.info(privateKeyString);
                */

                String fullchain = "";
                String privateKeyString = "";

                try {
                    LOG.info("Starting Let's Encrypt process...");
                    ClientTest ct = new ClientTest(subdomain, jelasticService);
                    Cert cert = ct.fetchCertificate(Arrays.asList(domains));
                    fullchain = cert.getCertificateChain();
                    privateKeyString = cert.getDomainKey();


                    fullchain = fullchain.trim();
                    privateKeyString = privateKeyString.trim();

                    LOG.info("Domain:\n" + domains[0]);
                    LOG.info("Full Chain:\n" + fullchain);
                    LOG.info("Private Key:\n" + privateKeyString);

                    ////////////////////////////////
                    boolean certificateValid = CertificateValidator.validate(fullchain);
                    boolean privateKeyValid = PrivateKeyValidator.validate(privateKeyString);
                    if(certificateValid && privateKeyValid) {
                        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        jelasticService.writeCertificateAndPrivateKeyFile(domains[0], fullchain, privateKeyString);
                        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        responseObject.put("code", Status.SUCCESS_OK.getCode());
                        responseObject.put("reason", Status.SUCCESS_OK.getReasonPhrase());
                        JSONObject certificate = new JSONObject();
                        certificate.put("certificate", fullchain);
                        certificate.put("privateKey", privateKeyString);
                        responseObject.put("result", certificate);
                        setStatus(Status.SUCCESS_OK);
                    } else {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                    ////////////////////////////////
                    Representation representation = new StringRepresentation(responseObject.toJSONString());
                    representation.setMediaType(MediaType.APPLICATION_JSON);
                    return representation;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if(ex instanceof AcmeRateLimitedException) {
                        setStatus(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
                        return null;
                    } else {
                        LOG.info("Failed to get a certificate for domains " + domains);
                        return internalError();
                    }

                }
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
        final boolean[] isOwner = {false};
        try {
            // TODO - Check if this is a valid way to check if domain is owned
            DivrollEntities entities = new DivrollEntities("Domain");
            QueryFilter queryFilter = new EqualQueryFilter("name", domain);
            entities.query(queryFilter);
            entities.getEntities().forEach(entity -> {
                String name = String.valueOf(entity.getProperty("name"));
                if(name != null && name.equals(domain)) {
                    isOwner[0] = true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOwner[0];
    }
}
