package com.divroll.backend.service;

import java.io.IOException;

public interface ShellService {

    void writeCertificateAndPrivateKeyFile(final String domain, final String certificate, final String privateKey) throws IOException, InterruptedException;
    String writeCertificateFile(String domain, String certificate) throws IOException, InterruptedException;
    String writePrivateKeyFile(String domain, String privateKey) throws IOException, InterruptedException;
    String writeNginxConfFile(String domain, String configuration) throws IOException, InterruptedException;

}
