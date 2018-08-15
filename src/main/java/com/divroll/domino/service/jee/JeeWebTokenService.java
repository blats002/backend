/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
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
 */
package com.divroll.domino.service.jee;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.divroll.domino.service.WebTokenService;
import com.google.inject.Inject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeWebTokenService implements WebTokenService {

    private static final Logger LOG
            = Logger.getLogger(JeeWebTokenService.class.getName());

    @Inject
    public JeeWebTokenService() {
    }

    @Override
    public String createToken(String secret, String userId) {
        JWTSigner signer = new JWTSigner(secret);
        JWTVerifier verifier = new JWTVerifier(secret);
        HashMap<String, Object> claims = new HashMap<String, Object>();
        claims.put("id", String.valueOf(userId));
        String token = signer.sign(claims);
        LOG.info("Generated token: " + token);
        return token;
    }

    @Override
    public Map<String, Object> readToken(String secret) {
        return null;
    }

    @Override
    public String readUserIdFromToken(String secret, String token) {
        String id = null;
        try {
            JWTSigner signer = new JWTSigner(secret);
            JWTVerifier verifier = new JWTVerifier(secret);
            Map<String, Object> parsed = verifier.verify(token);
            Object objectId = parsed.get("id");
            if (objectId instanceof String) {
                id = String.valueOf((String) objectId);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
        } catch (IOException e) {
        } catch (SignatureException e) {
        } catch (JWTVerifyException e) {
        } catch (Exception e) {
        }
        return id;
    }
}
