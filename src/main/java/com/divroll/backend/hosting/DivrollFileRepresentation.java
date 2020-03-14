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
package com.divroll.backend.hosting;

import com.divroll.backend.repository.FileRepository;
import com.divroll.backend.service.CacheService;
import com.divroll.backend.util.StringUtil;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.ByteSource;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class DivrollFileRepresentation extends OutputRepresentation {

    final static Logger LOG = LoggerFactory.getLogger(DivrollFileRepresentation.class);

    private String path;
    private String appId;
    private CacheService cacheService;
    private FileRepository fileRepository;

    public DivrollFileRepresentation(String masterToken, String appId, String path, MediaType mediaType,
                                     CacheService cacheService, FileRepository fileRepository) {
        super(mediaType);
        this.appId = appId;
        this.path = path;
        this.cacheService = cacheService;
        this.fileRepository = fileRepository;
    }

    public DivrollFileRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            LOG.info("Writing bytes...");
            if(appId != null) {
                String cacheKey = appId + ":" + path;
                LOG.info("Cache Key: " + cacheKey);
                byte[] buff = new byte[128*1024];
                Date start = new Date();
                byte[] cached = cacheService.get(cacheKey);
                Date finished = new Date();
                LOG.info("TOTAL FETCHED CACHE: " + (finished.getTime() - start.getTime()));
                if(cached != null && cached.length > 0) {
                    start = new Date();
                    outputStream.write(cached);
                    finished = new Date();
                    LOG.info("CACHED FLOW DONE at " + (finished.getTime() - start.getTime()) );
                } else {
                    byte[] file = fileRepository.get(appId, path);
                    if(file != null) {
                        flow(ByteSource.wrap(file).openStream(), outputStream, buff);
                        cacheService.put(cacheKey, file);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(outputStream != null)       {
                outputStream.close();
            }
        } finally {
            if(outputStream != null)       {
                outputStream.close();
            }
        }
    }

    private static void flow(InputStream is, OutputStream os, byte[] buf )
            throws IOException {
        int numRead;
        while ( (numRead = is.read(buf) ) >= 0) {
            os.write(buf, 0, numRead);
        }
    }

    protected String read404template(){
        InputStream is = this.getClass().getResourceAsStream("/error404.html");
        return StringUtil.toString(is);
    }


}
