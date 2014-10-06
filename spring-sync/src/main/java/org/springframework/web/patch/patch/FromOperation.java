package org.springframework.web.patch.patch;

public abstract class FromOperation extends PatchOperation {

	protected String from;
	
	public FromOperation(String op, String path, String from) {
		super(op, path);
		this.from = from;
	}
	
	public String getFrom() {
		return from;
	}

}
