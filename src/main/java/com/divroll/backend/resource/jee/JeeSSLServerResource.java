package com.divroll.backend.resource.jee;

import com.alibaba.fastjson.JSONObject;
import com.divroll.backend.certificates.*;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.Superuser;
import com.divroll.backend.repository.FileRepository;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.resource.SSLResource;
import com.divroll.backend.service.ShellService;
import com.google.inject.Inject;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import java.security.Security;
import java.util.Arrays;
import java.util.logging.Logger;

public class JeeSSLServerResource extends BaseServerResource
    implements SSLResource {

    private static final Logger LOG
            = Logger.getLogger(JeeSSLServerResource.class.getName());

    private String appObjectId = null;

    private static final String CERTIFICATE_HEADER = "-----BEGIN CERTIFICATE-----";
    private static final String CERTIFICATE_FOOTER = "-----END CERTIFICATE-----";
    private static final String PRIVATE_KEY_HEADER = "-----BEGIN PRIVATE KEY-----";
    private static final String PRIVATE_KEY_FOOTER = "-----END PRIVATE KEY-----";
    private static final String PRIVATE_KEY_HEADER_ALT = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String PRIVATE_KEY_FOOTER_ALT = "-----END RSA PRIVATE KEY-----";

    private static String AGREEMENT_URL = "https://letsencrypt.org/documents/LE-SA-v1.1.1-August-1-2016.pdf";

    @Inject
    ShellService shellService;

    @Inject
    UserRepository userRepository;

    @Inject
    FileRepository fileRepository;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
    }

    @Override
    public Representation post(Representation entity) {

        JSONObject responseObject = new JSONObject();

        LOG.info("Domain: " + domainName);
        LOG.info("Subdomain: " + appName);

        if(domainName == null) {
            //domainName = entity.getDomainName();
        }

        if(appName == null) {
            //appName = entity.getAppName();
        }

        Superuser superuser = superuserRepository.getUserByAuthToken(superAuthToken);
        if(!isMaster() && superuser == null) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }

        Application application = applicationService.readByName(appName);
        Superuser owner = application.getSuperuser();

        try {
            if(superuser.getEntityId().equals(owner.getEntityId())) {
                // DO IT
                String body = entity.getText();
                JSONObject jsonObject = JSONObject.parseObject(body);
                String certificate = jsonObject.getString("certificate");
                String privateKey = jsonObject.getString("privateKey");
                if(certificate.startsWith(CERTIFICATE_HEADER) && certificate.endsWith(CERTIFICATE_FOOTER)
                        && (privateKey.startsWith(PRIVATE_KEY_HEADER) || privateKey.startsWith(PRIVATE_KEY_HEADER_ALT))
                        && (privateKey.endsWith(PRIVATE_KEY_FOOTER) || privateKey.endsWith(PRIVATE_KEY_FOOTER_ALT))) {
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    shellService.writeCertificateAndPrivateKeyFile(domainName, certificate, privateKey);
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                } else {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
                //LOG.info(domain);
                //LOG.info(body);
                responseObject.put("code", Status.SUCCESS_OK.getCode());
                responseObject.put("reason", Status.SUCCESS_OK.getReasonPhrase());
                setStatus(Status.SUCCESS_OK);
            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
        } catch (Exception e) {
            responseObject = new JSONObject();
            responseObject.put("code",Status.SERVER_ERROR_INTERNAL.getCode());
            responseObject.put("reason",Status.SERVER_ERROR_INTERNAL.getReasonPhrase());
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        Representation representation = new StringRepresentation(responseObject.toJSONString());
        representation.setMediaType(MediaType.APPLICATION_JSON);
        return representation;
    }

    @Override
    public Representation get(Representation entity) {
        JSONObject responseObject = new JSONObject();
        try {

            Superuser superuser = superuserRepository.getUserByAuthToken(superAuthToken);
            if(!isMaster() && superuser == null) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }

            Application application = applicationService.readByName(appName);
            Superuser owner = application.getSuperuser();

            if(superuser.getEntityId().equals(owner.getEntityId())) {
                // DO IT
                //CA_PRODUCTION_URL = config.getJSONObject("params").getString("LETSENCRYPT_PRODUCTION_URL");
                //AGREEMENT_URL = config.getJSONObject("params").getString("LETSENCRYPT_AGREEMENT_URL");

                ///////////////////////////////
                // Let's Encrypt !
                //////////////////////////////

                Security.addProvider(new BouncyCastleProvider());

                String mailTo = superuser.getUsername();
                if (mailTo == null || mailTo.isEmpty() || !EmailValidator.validate(mailTo)) {
                    return badRequest("Must have valid email");
                }

                //LOG.info("WARNING: this sample application is using the Let's Encrypt staging API. Certificated created with this application won't be trusted.");
                LOG.info("By using this application you agree to Let's Encrypt Terms and Conditions");
                LOG.info(AGREEMENT_URL);
                String[] domains = new String[1];
                String[] contacts = new String[1];
                domains[0] = domainName;
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

                if(domains[0] == null) {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Must provide domain name");
                    return null;
                }

                String fullchain = "";
                String privateKeyString = "";

                LOG.info("Starting Let's Encrypt process...");
                ClientTest ct = new ClientTest(application.getAppId(), fileRepository, shellService);
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
                if (certificateValid && privateKeyValid) {
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    shellService.writeCertificateAndPrivateKeyFile(domains[0], fullchain, privateKeyString);
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

            }
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
            e.printStackTrace();
        }
        return null;
    }

}
