/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019-present, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * GPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */

package com.divroll.backend.certificates;

import com.divroll.backend.model.File;
import com.divroll.backend.repository.FileRepository;
import com.divroll.backend.service.ShellService;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.exceptions.Exceptions;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class CertificateGenerator {

    private static final ChallengeType CHALLENGE_TYPE = ChallengeType.HTTP;

    // RSA key size of generated key pairs
    private static final int KEY_SIZE = 2048;

    private String certificateChain;

    private String domainKey;

    private String appId;

    private static final Logger LOG = LoggerFactory.getLogger(CertificateGenerator.class);

    private ShellService shellService;

    private FileRepository fileRepository;

    String challengeAttempt = System.getenv("LETS_ENCRYPT_CHALLENGE_ATTEMPT");

    public CertificateGenerator(String appId, FileRepository fileRepository, ShellService shellService) {
        this.shellService = shellService;
        this.appId = appId;
        this.fileRepository = fileRepository;
    }

    public String getCertificateChain() {
        return certificateChain;
    }

    public void setCertificateChain(String certificateChain) {
        this.certificateChain = certificateChain;
    }

    public String getDomainKey() {
        return domainKey;
    }

    public void setDomainKey(String domainKey) {
        this.domainKey = domainKey;
    }

    private enum ChallengeType { HTTP, DNS }

    public Cert fetchCertificate(Collection<String> domains) throws IOException, AcmeException {
        // Load the user key file. If there is no key file, create a new one.
        KeyPair userKeyPair = loadOrCreateUserKeyPair();

        // Create a session for Let's Encrypt.
        // Use "acme://letsencrypt.org" for production server
//        Session session = new Session("https://acme-staging-v02.api.letsencrypt.org/directory");
        Session session = new Session("acme://letsencrypt.org");

        // Get the Account.
        // If there is no account yet, create a new one.
        Account acct = findOrRegisterAccount(session, userKeyPair);

        // Load or create a key pair for the domains. This should not be the userKeyPair!
        KeyPair domainKeyPair = loadOrCreateDomainKeyPair();

        try (StringWriter sw = new StringWriter()) {
            KeyPairUtils.writeKeyPair(domainKeyPair, sw);
            domainKey = sw.toString();
        }
        // Order the certificate
        Order order = acct.newOrder().domains(domains).create();

        // Perform all required authorizations
        for (Authorization auth : order.getAuthorizations()) {
            authorize(auth);
        }

        // Generate a CSR for all of the domains, and sign it with the domain key pair.
        CSRBuilder csrb = new CSRBuilder();
        csrb.addDomains(domains);
        csrb.sign(domainKeyPair);

        // Write the CSR to a file, for later use.
//        try (Writer out = new FileWriter(DOMAIN_CSR_FILE)) {
//            csrb.write(out);
//        }

        // Order the certificate
        order.execute(csrb.getEncoded());

        // Wait for the order to complete
        LOG.info("Order...");
        int attempts = 10;
        if(challengeAttempt != null) {
            attempts = Integer.valueOf(challengeAttempt);
        }

        Certificate certificate = Observable.range(1, attempts)
                .delay(3, TimeUnit.SECONDS)
                .filter(integer -> {
                    try {
                        LOG.info("AUTHORIZE ATTEMPTS: " + integer);
                        order.update();
                    } catch (AcmeException e) {
                        Exceptions.propagate(e);
                    }
                    return order.getStatus() == Status.VALID;
                }).map(integer -> {
                    Certificate cert = order.getCertificate();
                    return cert;
                }).toBlocking().first();

        if(certificate != null) {
            LOG.info("Success! The certificate for domains {} has been generated!", domains);
            LOG.info("Certificate URL: {}", certificate.getLocation());

            // Write a combined file containing the certificate and chain.
            try (StringWriter sw = new StringWriter()) {
                certificate.writeCertificate(sw);
                certificateChain = sw.toString();
            }

            Cert cert = new Cert();
            cert.setDomainKey(domainKey);
            cert.setCertificateChain(certificateChain);

            LOG.info("DOMAIN_KEY:" + domainKey);
            LOG.info("CERTIFICATE_CHAIN:" + certificateChain);

            return cert;
        } else {
            throw new AcmeException("Order failed... Giving up.");
        }

    }

    private KeyPair loadOrCreateUserKeyPair() throws IOException {
        KeyPair userKeyPair = KeyPairUtils.createKeyPair(KEY_SIZE);
        try(StringWriter sw = new StringWriter()) {
            KeyPairUtils.writeKeyPair(userKeyPair, sw);
        }
        return userKeyPair;
    }

    private KeyPair loadOrCreateDomainKeyPair() throws IOException {
        KeyPair domainKeyPair = KeyPairUtils.createKeyPair(KEY_SIZE);
        try (StringWriter sw = new StringWriter()) {
            KeyPairUtils.writeKeyPair(domainKeyPair, sw);
        }
        return domainKeyPair;
    }

    private Account findOrRegisterAccount(Session session, KeyPair accountKey) throws AcmeException {
        // Ask the user to accept the TOS, if server provides us with a link.
        URI tos = session.getMetadata().getTermsOfService();
        if (tos != null) {
            acceptAgreement(tos);
        }

        Account account = new AccountBuilder()
                .agreeToTermsOfService()
                .useKeyPair(accountKey)
                .create(session);
        LOG.info("Registered a new user, URL: {}", account.getLocation());

        return account;
    }

    private void authorize(Authorization auth) throws AcmeException {
        LOG.info("Authorization for domain {}", auth.getIdentifier().getDomain());

        // The authorization is already valid. No need to process a challenge.
        if (auth.getStatus() == Status.VALID) {
            return;
        }

        // Find the desired challenge and prepare it.
        Challenge challenge = null;
        switch (CHALLENGE_TYPE) {
            case HTTP:
                challenge = httpChallenge(auth);
                break;

            case DNS:
                challenge = dnsChallenge(auth);
                break;
        }

        if (challenge == null) {
            throw new AcmeException("No challenge found");
        }

        // If the challenge is already verified, there's no need to execute it again.
        if (challenge.getStatus() == Status.VALID) {
            return;
        }

        try {
            if(CHALLENGE_TYPE.equals(ChallengeType.HTTP)) {
                Http01Challenge http01Challenge = (Http01Challenge) challenge;
                String token = http01Challenge.getToken();
                String authorization = http01Challenge.getAuthorization();
                LOG.info("ACME Authorization: " + authorization);
                String filePath = ".well-known/acme-challenge/" + token;
                File file = writeFile(authorization.getBytes(StandardCharsets.UTF_8), filePath, appId);
                assert file != null;
            }
        } catch (IOException ex) {
            LOG.error("error ", ex);
        }

        // Now trigger the challenge.
        challenge.trigger();

        // Poll for the challenge to complete.
        int attempts = 20;

        Challenge finalChallenge = challenge;
        Boolean givenUp = Observable.range(1, attempts)
                .debounce(3, TimeUnit.SECONDS)
                .filter(integer -> {
                    try {
                        LOG.info("CHALLENGE ATTEMPTS: " + integer);
                        finalChallenge.update();
                    } catch (AcmeException e) {
                        Exceptions.propagate(e);
                    }
                    return finalChallenge.getStatus() == Status.VALID;
                }).map(integer -> {
                    return false;
                }).toBlocking().firstOrDefault(true);

        if(givenUp == true) {
            throw new AcmeException("Failed to pass the challenge for domain "
                    + auth.getIdentifier().getDomain() + ", ... Giving up.");
        }

        LOG.info("Challenge has been completed. Remember to remove the validation resource.");
        completeChallenge("Challenge has been completed.\nYou can remove the resource again now.");
    }

    public Challenge httpChallenge(Authorization auth) throws AcmeException {
        // Find a single http-01 challenge
        Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);
        if (challenge == null) {
            throw new AcmeException("Found no " + Http01Challenge.TYPE + " challenge, don't know what to do...");
        }

        // Output the challenge, wait for acknowledge...
        LOG.info("Please create a file in your web server's base directory.");
        LOG.info("It must be reachable at: http://{}/.well-known/acme-challenge/{}",
                auth.getIdentifier().getDomain(), challenge.getToken());
        LOG.info("File name: {}", challenge.getToken());
        LOG.info("Content: {}", challenge.getAuthorization());
        LOG.info("The file must not contain any leading or trailing whitespaces or line breaks!");
        LOG.info("If you're ready, dismiss the dialog...");

        StringBuilder message = new StringBuilder();
        message.append("Please create a file in your web server's base directory.\n\n");
        message.append("http://")
                .append(auth.getIdentifier().getDomain())
                .append("/.well-known/acme-challenge/")
                .append(challenge.getToken())
                .append("\n\n");
        message.append("Content:\n\n");
        message.append(challenge.getAuthorization());
        acceptChallenge(message.toString());

        return challenge;
    }

    public Challenge dnsChallenge(Authorization auth) throws AcmeException {
        // Find a single dns-01 challenge
        Dns01Challenge challenge = auth.findChallenge(Dns01Challenge.TYPE);
        if (challenge == null) {
            throw new AcmeException("Found no " + Dns01Challenge.TYPE + " challenge, don't know what to do...");
        }

        // Output the challenge, wait for acknowledge...
        LOG.info("Please create a TXT record:");
        LOG.info("_acme-challenge.{}. IN TXT {}",
                auth.getIdentifier().getDomain(), challenge.getDigest());
        LOG.info("If you're ready, dismiss the dialog...");

        StringBuilder message = new StringBuilder();
        message.append("Please create a TXT record:\n\n");
        message.append("_acme-challenge.")
                .append(auth.getIdentifier().getDomain())
                .append(". IN TXT ")
                .append(challenge.getDigest());
        acceptChallenge(message.toString());

        return challenge;
    }

    public void acceptChallenge(String message) throws AcmeException {
    }

    public void completeChallenge(String message) throws AcmeException {
        LOG.info("Complete Challenge");
    }

    public void acceptAgreement(URI agreement) throws AcmeException {
    }
    private File writeFile(byte[] fileBytes, String path, String appId)
            throws IOException {
        return fileRepository.put(appId, path, fileBytes);
    }

}