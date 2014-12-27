package com.hwacom.util.cm.model;

import com.jcraft.jsch.UserInfo;

public class LocalUserInfo implements UserInfo {

	@Override
	public String getPassphrase() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public boolean promptPassphrase(String message) {
		return false;
	}

	@Override
	public boolean promptPassword(String message) {
		return false;
	}

	@Override
	public boolean promptYesNo(String message) {
		return true;
	}

	@Override
	public void showMessage(String message) {
	}

}
