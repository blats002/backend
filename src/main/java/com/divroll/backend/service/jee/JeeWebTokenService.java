/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.service.jee;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.divroll.backend.Constants;
import com.divroll.backend.service.WebTokenService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeWebTokenService implements WebTokenService {

  private static final Logger LOG = LoggerFactory.getLogger(JeeWebTokenService.class);

  @Inject
  public JeeWebTokenService() {}

  @Override
  public String createToken(String secret, Long id) {
    JWTSigner signer = new JWTSigner(secret);
    HashMap<String, Object> claims = new HashMap<String, Object>();
    claims.put(Constants.JWT_ID_KEY, id);
    String token = signer.sign(claims);
    return token;
  }

  @Override
  public String createToken(String secret, String userId) {
    JWTSigner signer = new JWTSigner(secret);
    HashMap<String, Object> claims = new HashMap<String, Object>();
    claims.put(Constants.JWT_ID_KEY, String.valueOf(userId));
    String token = signer.sign(claims);
    // log.info("Generated token: " + token);
    return token;
  }

  @Override
  public String createToken(String secret, String userId, String expiration) {
    JWTSigner signer = new JWTSigner(secret);
    HashMap<String, Object> claims = new HashMap<String, Object>();
    claims.put(Constants.JWT_ID_KEY, String.valueOf(userId));
    claims.put(Constants.JWT_ID_EXPIRATION, String.valueOf(expiration));
    String token = signer.sign(claims);
    // log.info("Generated token: " + token);
    return token;  }

  @Override
  public String createEmailToken(String secret, String email, String expiration) {
    JWTSigner signer = new JWTSigner(secret);
    HashMap<String, Object> claims = new HashMap<String, Object>();
    claims.put(Constants.JWT_ID_EMAIL, email);
    claims.put(Constants.JWT_ID_EXPIRATION, String.valueOf(expiration));
    String token = signer.sign(claims);
    return token;
  }

  @Override
  public String createPasswordResetToken(String secret, String email, String password, String expiration) {
    JWTSigner signer = new JWTSigner(secret);
    HashMap<String, Object> claims = new HashMap<String, Object>();
    claims.put(Constants.JWT_ID_EMAIL, email);
    claims.put(Constants.JWT_ID_PASSWORD, String.valueOf(password));
    claims.put(Constants.JWT_ID_EXPIRATION, String.valueOf(expiration));
    String token = signer.sign(claims);
    return token;
  }

  @Override
  public Map<String, Object> readToken(String secret, String token) {
    try {
      JWTVerifier verifier = new JWTVerifier(secret);
      Map<String, Object> parsed = verifier.verify(token);
      return parsed;
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
    } catch (IOException e) {
    } catch (SignatureException e) {
    } catch (JWTVerifyException e) {
    } catch (Exception e) {
    }
    return null;
  }

  @Override
  public String readUserIdFromToken(String secret, String token) {
    String id = null;
    try {
      JWTVerifier verifier = new JWTVerifier(secret);
      Map<String, Object> parsed = verifier.verify(token);
      Object objectId = parsed.get(Constants.JWT_ID_KEY);
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
