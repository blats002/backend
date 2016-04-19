package com.divroll.core.rest.resource.gae;

import com.divroll.core.rest.guice.SelfInjectingServerResource;
import com.divroll.core.rest.resource.ZipDeploymentResource;
import com.divroll.core.rest.service.KinveyService;
import com.divroll.core.rest.util.RegexHelper;
import com.google.apphosting.api.ApiProxy;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GaeZipDeploymentServerResource extends SelfInjectingServerResource
	implements ZipDeploymentResource {

	final static Logger LOG
			= LoggerFactory.getLogger(GaeZipDeploymentServerResource.class);

	@Inject
	private KinveyService kinveyService;

	@Inject
	@Named("kinvey.appkey")
	private String appkey;

	@Inject
	@Named("kinvey.mastersecret")
	private String masterSecret;

	@Override
	protected void doInit() {
		super.doInit();
	}

	@Override
	public void createDeployment(Representation entity) {

		Form form= new Form(entity);
		String appkey = form.getFirstValue("appkey");
		String masterSecret = form.getFirstValue("masterSecret");
		String subdomain = form.getFirstValue("subdomain");
		String revision = form.getFirstValue("revision");

//		if(appkey != this.appkey || masterSecret != this.masterSecret) {
//			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
//			return;
//		}

		String id = revision;

		OutputStream buff = new ByteArrayOutputStream();
		kinveyService.getWebsiteZip(id, buff);
		processZipFile(subdomain, id, ((ByteArrayOutputStream)buff).toByteArray());

	}


	public void processZipFile(String subdomain, String revision, byte[] bytes) {
		try {
			ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(bytes));
			InputStreamReader isr = new InputStreamReader(zipStream);
			ZipEntry ze;
			while ((ze = zipStream.getNextEntry()) != null) {
				String fileName = ze.getName();
				long fileSize = ze.getCompressedSize();
				ByteArrayOutputStream streamBuilder = new ByteArrayOutputStream();
				int bytesRead;
				byte[] tempBuffer = new byte[8192*2];
				while ( (bytesRead = zipStream.read(tempBuffer)) != -1 ){
					streamBuilder.write(tempBuffer, 0, bytesRead);
				}
				String path = fileName;
				if(!path.startsWith("/")){
					path = "/" + path;
				}
				LOG.info("path: " + path);
				kinveyService.writeFile(subdomain, path, RegexHelper.parseFileName(path), revision, streamBuilder.toByteArray());
//				Blob blob = new Blob(streamBuilder.toByteArray());
//				BlobFile blobFile = new BlobFile(fileName, blob);
//				store().put(blobFile);
			}
			zipStream.close();
		} catch (IOException e){
			e.printStackTrace();
			LOG.error(e.getMessage());
		} catch (ApiProxy.RequestTooLargeException e){
			e.printStackTrace();
			LOG.error(e.getMessage());
		}
	}

}
