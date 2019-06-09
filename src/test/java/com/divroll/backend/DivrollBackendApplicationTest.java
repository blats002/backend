/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019, Divroll, and individual contributors
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
package com.divroll.backend;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
@RunWith(Arquillian.class)
@RunAsClient
public class DivrollBackendApplicationTest {
  @Deployment
  public static WebArchive createDeployment() {

    // Import Maven runtime dependencies
    File[] files =
        Maven.resolver()
            .loadPomFromFile("pom.xml")
            .importRuntimeDependencies()
            .resolve()
            .withTransitivity()
            .asFile();
    // Create deploy file
    WebArchive war =
        ShrinkWrap.create(WebArchive.class)
            .addClass(DivrollBackendApplication.class)
            .addAsLibraries(files);

    // Show the deploy structure
    System.out.println(war.toString(true));
    System.out.println("Returning WAR");
    return war;
  }

  @Test
  public void callServletToGetApplicationTest() throws Exception {
    // String body = readAllAndClose(new
    // URL("http://localhost:8080/domino/applications").openStream());
    String body = readAllAndClose(new URL("http://localhost:8080/backend/").openStream());
    System.out.println(body);
    Assert.assertNotNull(body);
  }

  private String readAllAndClose(InputStream is) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      int read;
      while ((read = is.read()) != -1) {
        out.write(read);
      }
    } finally {
      try {
        is.close();
      } catch (Exception ignored) {
      }
    }
    return out.toString();
  }
}
