package com.divroll.core.rest.service.gae;

import com.alibaba.fastjson.JSON;
import com.divroll.core.rest.service.KinveyService;
import com.divroll.core.rest.util.GAEUtil;
import com.google.common.io.CountingOutputStream;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kinvey.java.File;
import com.kinvey.java.Query;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.nativejava.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Kerby on 4/18/2016.
 */
public class GaeKinveyService implements KinveyService {

	final static Logger LOG
			= LoggerFactory.getLogger(GaeKinveyService.class);

	@Inject
	@Named("kinvey.appkey")
	private String appkey;

	@Inject
	@Named("kinvey.mastersecret")
	private String masterSecret;

	@Override
	public void getWebsiteZip(final String id, OutputStream out) {
		try {
			Client kinvey = new Client.Builder(appkey, masterSecret).build();
			kinvey.user().loginBlocking(appkey, masterSecret).execute();
			kinvey.file().downloadWithTTLBlocking(id, getTimeout(), out, new DownloaderProgressListener() {
				@Override
				public void progressChanged(MediaHttpDownloader mediaHttpDownloader)
						throws IOException {
					String jsonString = JSON.toJSONString(mediaHttpDownloader);
					if(mediaHttpDownloader.getDownloadState()
							.equals(MediaHttpDownloader.DownloadState.DOWNLOAD_COMPLETE)) {
						LOG.info("Download complete: " + id);
					}
				}
				@Override
				public void onSuccess(Void aVoid) {
					LOG.info("Success download: " + id);
				}
				@Override
				public void onFailure(Throwable throwable) {
					LOG.info("Failed download: " + id);
				}
			});
		} catch (IOException e) {
			LOG.debug("Error: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e){
			LOG.debug("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void getWebsiteZip(final String subdomain, final String revision,
			OutputStream out) {
		try {
			Client kinvey = new Client.Builder(appkey, masterSecret).build();
			kinvey.user().loginBlocking(appkey, masterSecret).execute();
			Query q = kinvey.query();
			q.equals("subdomain", subdomain);
			q.equals("revision", revision);
			kinvey.file().downloadBlocking(q, out, new DownloaderProgressListener() {
				@Override
				public void progressChanged(MediaHttpDownloader mediaHttpDownloader)
						throws IOException {
					String jsonString = JSON.toJSONString(mediaHttpDownloader);
					if(mediaHttpDownloader.getDownloadState()
							.equals(MediaHttpDownloader.DownloadState.DOWNLOAD_COMPLETE)) {
						LOG.info("Download complete: " + subdomain);
					}
				}
				@Override
				public void onSuccess(Void aVoid) {
					LOG.info("Success download: " + subdomain);
				}
				@Override
				public void onFailure(Throwable throwable) {
					LOG.info("Failed download: " + subdomain);
				}
			});
		} catch (IOException e) {
			LOG.debug("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void getFile(final String subdomain, final String path, final String revision,
			final OutputStream out) {
		try {
			Client kinvey = new Client.Builder(appkey, masterSecret).build();
			kinvey.user().loginBlocking(appkey, masterSecret).execute();
			Query q = kinvey.query();
			q.equals("subdomain", subdomain);
			q.equals("path", path);

			kinvey.file().downloadBlocking(q, out, new DownloaderProgressListener() {
				@Override
				public void progressChanged(MediaHttpDownloader mediaHttpDownloader)
						throws IOException {
					String jsonString = JSON.toJSONString(mediaHttpDownloader);
					if(mediaHttpDownloader.getDownloadState()
							.equals(MediaHttpDownloader.DownloadState.DOWNLOAD_COMPLETE)) {
						LOG.info("Download complete: " + subdomain + path);
					}
				}
				@Override
				public void onSuccess(Void aVoid) {
					LOG.info("Success download: " + subdomain + path);
				}
				@Override
				public void onFailure(Throwable throwable) {
					LOG.info("Failed download: " + subdomain + path);
				}
			});
		} catch (IOException e) {
			LOG.debug("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public boolean writeFile(final String subdomain, final String path, String fileName, String revision,
			byte[] bytes) {
		final Boolean[] isSuccess = { false };
		LOG.info("Writing byte length: " + bytes.length);
		LOG.info("Filename: " + fileName);
		if(bytes.length == 0 || bytes.length == -1){
			return false;
		}
		try{
			Client kinvey = new Client.Builder(appkey, masterSecret).build();
			kinvey.user().loginBlocking(appkey, masterSecret).execute();
			InputStream is = new ByteArrayInputStream(bytes);
			FileMetaData md = new FileMetaData();
			md.set("subdomain", subdomain);
			md.set("path", path);
			md.setFileName(fileName);
			md.setPublic(false);
			kinvey.file().uploadBlocking(md, is, new UploaderProgressListener() {
				@Override
				public void progressChanged(MediaHttpUploader mediaHttpUploader)
						throws IOException {

				}

				@Override public void onSuccess(FileMetaData fileMetaData) {
					LOG.debug("Success upload: " + subdomain + path);
					isSuccess[0] = true;
				}

				@Override public void onFailure(Throwable throwable) {
					LOG.debug("Failed upload: " + subdomain + path);
				}
			});
		}catch (IOException e){
			e.printStackTrace();
			LOG.error("Error: " + e.getMessage());
		}
		return isSuccess[0];
	}

	private int getTimeout() {
		if(GAEUtil.isGaeDev()){
			return 1000;
		} else {
			return 60;
		}
	}
}
