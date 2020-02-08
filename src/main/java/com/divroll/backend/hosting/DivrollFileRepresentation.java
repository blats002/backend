package com.divroll.backend.hosting;

import com.divroll.backend.util.StringUtil;
import com.divroll.backend.xodus.XodusVFS;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DivrollFileRepresentation extends OutputRepresentation {

    final static Logger LOG = LoggerFactory.getLogger(DivrollFileRepresentation.class);

    private String path;

    private String instance;

    private XodusVFS vfs;

    public DivrollFileRepresentation(String instance, String path, MediaType mediaType, XodusVFS vfs) {
        super(mediaType);
        this.path = path;
        this.vfs = vfs;
        this.instance = instance;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        byte[] buff = new byte[64*1024];
        InputStream is = vfs.openFile(instance, path);
        flow(is, outputStream, buff);
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
