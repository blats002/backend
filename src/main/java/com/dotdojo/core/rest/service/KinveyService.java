package com.divroll.core.rest.service;

import java.io.OutputStream;

/**
 * Created by Kerby on 4/18/2016.
 */
public interface KinveyService {
	public void getWebsiteZip(final String id, OutputStream out);
	public void getWebsiteZip(String subdomain, String revision, OutputStream out);
	public void getFile(final String subdomain, final String path, String revision, OutputStream out);
	public boolean writeFile(final String subdomain, final String path, final String fileName, String revision, byte[] bytes);
}
