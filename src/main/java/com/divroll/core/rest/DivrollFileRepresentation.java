package com.divroll.core.rest;

import com.divroll.core.rest.service.CacheService;
import com.divroll.core.rest.util.CachingOutputStream;
import com.divroll.core.rest.util.StringUtil;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DivrollFileRepresentation extends OutputRepresentation {

    protected final static String FILE_BASE_URI = "http://localhost:9090/divroll/files";
    final static Logger LOG = LoggerFactory.getLogger(DivrollFileRepresentation.class);

    private String path;
    private String appName;
    private CacheService cacheService;

    public DivrollFileRepresentation(String appName, String path, MediaType mediaType, CacheService cacheService) {
        super(mediaType);
        this.appName = appName;
        this.path = path;
        this.cacheService = cacheService;
    }

    public DivrollFileRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            HttpResponse<InputStream> response = Unirest.get(FILE_BASE_URI)
                    .queryString("appName", appName)
                    .queryString("filePath", path)
                    .asBinary();
            if(response.getStatus() == 200) {
                InputStream is = response.getBody();
                byte[] buff = new byte[64*1024];
                final CachingOutputStream cachingOutputStream = new CachingOutputStream(outputStream);
                flow(is, cachingOutputStream, buff);
            }
        } catch (UnirestException e) {
            e.printStackTrace();
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
