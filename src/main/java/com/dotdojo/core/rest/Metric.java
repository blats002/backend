package com.divroll.core.rest;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.java.model.KinveyMetaData;

public class Metric extends GenericJson {
	@Key("_id")
	private String id;
	@Key
	private String subdomain;
	@Key
	private String domain;
	@Key
	private Long numBytes;
	@Key("_kmd")
	private KinveyMetaData meta;
	@Key("_acl")
	private KinveyMetaData.AccessControlList acl;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public KinveyMetaData getMeta() {
		return meta;
	}

	public void setMeta(KinveyMetaData meta) {
		this.meta = meta;
	}

	public KinveyMetaData.AccessControlList getAcl() {
		return acl;
	}

	public void setAcl(KinveyMetaData.AccessControlList acl) {
		this.acl = acl;
	}

	public Long getNumBytes() {
		return numBytes;
	}

	public void setNumBytes(Long numBytes) {
		this.numBytes = numBytes;
	}
}
