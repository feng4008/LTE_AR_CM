package com.hwacom.cm.service;

import java.io.InputStream;

import com.hwacom.cm.model.LoginUser;
import com.hwacom.cm.model.RunningConfigTypeEmum;

public interface InputStreamToFileService {

	boolean inputStreamToFile(RunningConfigTypeEmum type, InputStream is, String nameLAR, LoginUser user);
	
}
