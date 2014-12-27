package com.hwacom.util.cm.model;

import java.io.Serializable;

import com.hwacom.util.cm.model.util.IPAddressValidator;

public class NetworkElement implements Serializable {
	
	private static final long serialVersionUID = -7667786960569888335L;

	private String name;
	
	private String host;
	
	public NetworkElement(String name, String host) {
		super();
		this.name = name;
		if (this.validateHost(host)) {
			this.host = host;
		} else {
			throw new IllegalArgumentException(host + " is not a valid IP format.");
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		if (this.validateHost(host)) {
			this.host = host;
		} else {
			throw new IllegalArgumentException(host + " is not a valid IP format.");
		}
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof NetworkElement)) {
			return false;
		}
		NetworkElement other = (NetworkElement) object;
		
		String[] blocks = this.host.split(".");
		String[] otherBlocks = other.getHost().split(".");
		if (Integer.parseInt(blocks[0]) == Integer.parseInt(otherBlocks[0])
				&& Integer.parseInt(blocks[1]) == Integer.parseInt(otherBlocks[1])
				&& Integer.parseInt(blocks[2]) == Integer.parseInt(otherBlocks[2])
				&& Integer.parseInt(blocks[3]) == Integer.parseInt(otherBlocks[3])) {
			return true;
		}

		if (this.name.trim().equals(other.getName().trim())) {
			return true;
		}
		
		return false;
	}
	
	private boolean validateHost(String host) {
		IPAddressValidator ipAddressValidator = new IPAddressValidator();
		if (!ipAddressValidator.validate(host)) {
			return false;
		}
		return true;
	}

}
