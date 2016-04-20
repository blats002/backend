package com.divroll.core.rest.resource.gae;

import com.divroll.core.rest.guice.SelfInjectingServerResource;
import com.divroll.core.rest.resource.MemcacheResource;
import com.divroll.core.rest.service.KinveyService;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.AppengineHttpRequestor;
import com.dropbox.core.v1.DbxClientV1;
import com.dropbox.core.v1.DbxEntry;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GaeMemcacheServerResource extends SelfInjectingServerResource
	implements MemcacheResource {

	final static Logger LOG
			= LoggerFactory.getLogger(GaeMemcacheServerResource.class);

	private static final String ROOT_URI = "/";
	private static final String APP_ROOT_URI = "/weebio/";
	private static final String KEY_SPACE = ":";

	@Inject
	@Named("dropbox.token")
	private String dropboxToken;

	@Inject
	@Named("kinvey.appkey")
	private String appkey;

	@Inject
	@Named("kinvey.mastersecret")
	private String masterSecret;

	@Inject
	private KinveyService kinveyService;

	private String subdomain;

	MemcacheService memCache = MemcacheServiceFactory.getMemcacheService();

	@Override
	public void getCache(Representation entity)
			throws Exception {
		subdomain = getQueryValue("subdomain");
		if(subdomain != null){
			DbxRequestConfig config = new DbxRequestConfig("weebio/1.0", Locale
					.getDefault().toString(), AppengineHttpRequestor.Instance);
			DbxClientV1 client = new DbxClientV1(config, dropboxToken);
			String path = APP_ROOT_URI + subdomain;
			listChildren(path);
			setStatus(Status.SUCCESS_OK, "Cache for " + subdomain + " cleared");
		} else {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "subdomain cannot be null");
		}
	}

	private void listChildren(String path){
		try {
			DbxRequestConfig config = new DbxRequestConfig("weebio/1.0", Locale
					.getDefault().toString(), AppengineHttpRequestor.Instance);
			DbxClientV1 client = new DbxClientV1(config, dropboxToken);
			DbxEntry.WithChildren listing = client.getMetadataWithChildren(path);
			List<String> list = new ArrayList<>();
			System.out.println(path);
			String key = new StringBuilder()
					.append(subdomain)
					.append(KEY_SPACE)
					.append(path).toString();
			if(memCache.contains(key)){
				memCache.delete(key);
				LOG.info("Removed cache: " + key);
			}
			if(listing != null && listing.children != null){
				for (DbxEntry child : listing.children) {
					listChildren(child.path);
//					if(RegexHelper.isDirectory(child.path)){
//						listChildren(child.path);
//					} else {
//						String key = new StringBuilder()
//								.append(subdomain)
//								.append(KEY_SPACE)
//								.append(child.path).toString();
//						if(memCache.contains(key)){
//							System.out.println("Key exist:" + key);
//						}
//					}
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			LOG.error(e.getMessage());
		}
	}
}
