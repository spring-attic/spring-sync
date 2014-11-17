package org.springframework.sync.diffsync;

public class Shadow<T> {

	private T resource;
	private int clientVersion; // aka clientVersion in the context of a server app
	private int serverVersion;  // aka serverVersion in the context of a server app

	public Shadow(T resource, int serverVersion, int clientVersion) {
		this.resource = resource;
		this.clientVersion = clientVersion;
		this.serverVersion = serverVersion;
	}
	
	public T getResource() {
		return resource;
	}

	public void setResource(T resource) {
		this.resource = resource;
	}

	public int getClientVersion() {
		return clientVersion;
	}

	public void setClientVersion(int clientVersion) {
		this.clientVersion = clientVersion;
	}

	public int getServerVersion() {
		return serverVersion;
	}

	public void setServerVersion(int serverVersion) {
		this.serverVersion = serverVersion;
	}
	
}
