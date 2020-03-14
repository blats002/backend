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

package com.divroll.backend.service.jee;

import com.divroll.backend.service.ShellService;
import com.divroll.backend.certificates.Template;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.*;
import java.util.logging.Logger;

public class JeeShellService implements ShellService {
    private static final Logger LOG
            = Logger.getLogger(JeeShellService.class.getName());

    protected final static String CERTIFICATE_PATH_TEMPLATE = "/etc/ssl/certs/DOMAIN_NAME.crt";
    protected final static String PRIVATE_KEY_PATH_TEMPLATE = "/etc/ssl/certs/DOMAIN_NAME.key";
    protected final static String NGINX_CONF_PATH_TEMPLATE = "/etc/nginx/conf.d/DOMAIN_NAME.conf";

    @Override
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
    @Override
    public String writeCertificateFile(String domain, String certificate) throws IOException, InterruptedException {
        String completePath = CERTIFICATE_PATH_TEMPLATE.replaceAll("DOMAIN_NAME", domain);
        LOG.info(completePath);
        writeFile(completePath, certificate);
        return completePath;
    }
    @Override
    public String writePrivateKeyFile(String domain, String privateKey) throws IOException, InterruptedException {
        String completePath = PRIVATE_KEY_PATH_TEMPLATE.replaceAll("DOMAIN_NAME", domain);
        LOG.info(completePath);
        writeFile(completePath, privateKey);
        return completePath;
    }
    @Override
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

        try {
            boolean isWindows = System.getProperty("os.name")
                    .toLowerCase().startsWith("windows");

            if(command == null || command.isEmpty()) {
                return -1;
            }

            String [] cmd ={"-c", command};

            CommandLine cmdLine = new CommandLine("sh");
            cmdLine.addArguments( cmd,false );
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            if (isWindows) {
                DefaultExecutor exec = new DefaultExecutor();
                exec.setStreamHandler(streamHandler);
                exec.setExitValue(0);
                exec.setWorkingDirectory(new File(System.getProperty("user.home")));
                int exitCode = exec.execute(cmdLine);
                return exitCode;
            } else {
                DefaultExecutor exec = new DefaultExecutor();
                exec.setStreamHandler(streamHandler);
                exec.setExitValue(0);
                exec.setWorkingDirectory(new File(System.getenv("HOME")));
                int exitCode = exec.execute(cmdLine);
                return exitCode;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }


}
