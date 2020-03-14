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

package com.divroll.backend.certificates;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

/**
 * Helper to convert X509 certificates to base 64 encoded strings, JAXB structures,
 * and vice versa.
 *
 * @author Eric Dubuis &lt,eric.dubuis@bfh.ch&gt;
 */
public class CertificateHelper {
    /**
     * Not used.
     */
    private CertificateHelper() {
    }

    /**
     * Converts a string of a Base64 encoded certificate (PEM format) into a byte array.
     * @param str a string representing a Base 64 encoded X509 certificate (PEM format)
     * @return a byte array
     */
    public static byte[] base64PEMStringToByteArray(String str) {
        return str.getBytes();
    }

    /**
     * Converts a byte array to a X509Certificate instance.
     * @param bytes the byte array
     * @return a X509Certificate instance
     * @throws CertificateException if the conversion fails
     */
    public static X509Certificate fromByteArrayToX509Certificate(byte[] bytes) throws CertificateException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(bytes);
        X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
        return cert;
    }

    /**
     * Converts a X509Certificate instance into a Base64 encoded string (PEM format).
     * @param cert a certificate
     * @return a string (PEM format)
     * @throws CertificateEncodingException if the conversion fails
     */
    public static String x509ToBase64PEMString(X509Certificate cert) throws IOException {
        // Convert certificate to PEM format.
        StringWriter sw = new StringWriter();
        try (PEMWriter pw = new PEMWriter(sw)) {
            pw.writeObject(cert);
        }
        return sw.toString();
    }

    public static String writePrivateKeyToPEMString(PrivateKey privateKey) throws Exception {
        StringWriter sw = new StringWriter();
        JcaPEMWriter writer = new JcaPEMWriter(sw);
        writer.writeObject(privateKey);
        writer.close();
        return sw.getBuffer().toString();
    }
}
