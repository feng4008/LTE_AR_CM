package com.hwacom.util.cm.service;

import java.io.InputStream;

import com.hwacom.util.cm.model.LoginUser;
import com.hwacom.util.cm.model.RunningConfigTypeEmum;

public interface InputStreamToFileService {

	boolean inputStreamToFile(RunningConfigTypeEmum type, InputStream is, String nameLAR, LoginUser user);
	
}
