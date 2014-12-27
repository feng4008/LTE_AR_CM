package com.hwacom.util.cm.service;

import java.io.IOException;
import java.io.InputStream;

import com.hwacom.util.cm.model.LoginUser;
import com.hwacom.util.cm.model.NetworkElement;

public interface PerformSSHExecService {
	
	void execCommand(NetworkElement ne, LoginUser user, String command) throws IOException, InterruptedException;
	
	InputStream execSimpleCommand(NetworkElement ne, LoginUser user, String command) throws IOException, InterruptedException;
	
}
