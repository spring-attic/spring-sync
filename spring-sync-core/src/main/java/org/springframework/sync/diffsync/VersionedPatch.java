package org.springframework.sync.diffsync;

import java.util.List;

import org.springframework.sync.Patch;
import org.springframework.sync.PatchOperation;

public class VersionedPatch extends Patch {

	private long serverVersion;
	
	private long clientVersion;
	
	public VersionedPatch(List<PatchOperation> operations, long serverVersion, long clientVersion) {
		super(operations);
		this.serverVersion = serverVersion;
		this.clientVersion = clientVersion;
	}

	public long getServerVersion() {
		return serverVersion;
	}
	
	public long getClientVersion() {
		return clientVersion;
	}
	
}
