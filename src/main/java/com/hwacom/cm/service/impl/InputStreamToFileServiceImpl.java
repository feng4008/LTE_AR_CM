package com.hwacom.cm.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hwacom.cm.model.LoginUser;
import com.hwacom.cm.model.RunningConfigTypeEmum;
import com.hwacom.cm.service.InputStreamToFileService;
import com.hwacom.cm.util.FileOperate;

public class InputStreamToFileServiceImpl implements InputStreamToFileService {
	
	private static final Logger log = LoggerFactory.getLogger(InputStreamToFileServiceImpl.class);
	
	@SuppressWarnings("finally")
	@Override
	public boolean inputStreamToFile(RunningConfigTypeEmum type, InputStream is, String nameLAR, LoginUser user) {

		File dir = null;
		boolean result = false;
		Calendar today = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String basePath = System.getProperty("user.dir");
		String localUser = System.getProperty("user.name");
		
        log.debug("user[{}]", localUser);
        boolean isGlobeUser = (localUser.equals("larcmbkp")) ? true : false;
        log.debug("Is Run at Globe User? [{}]", isGlobeUser);
		
		File theBase = new File(basePath);
        log.debug("ROOT DIR [{}]", basePath);


        StringBuilder path = new StringBuilder();
        StringBuilder deleteFile = new StringBuilder();
        Calendar monthAgo = Calendar.getInstance();
        monthAgo.add(Calendar.DATE, -30);
        
        if (isGlobeUser) {
            // will form to "/LAR711/LAR711-2014-06-12.txt"
            path = new StringBuilder(File.separatorChar + "LAR" + nameLAR + File.separatorChar + "LAR"
                    + nameLAR + "-" + dateFormat.format(today.getTime()) + ".cfg");
            deleteFile = new StringBuilder(File.separatorChar + "LAR" + nameLAR + File.separatorChar + "LAR"
                    + nameLAR + "-" + dateFormat.format(monthAgo.getTime()) + ".cfg");
        } else {
            // will form to "/LAR711-2014-06-12.txt"
            path = new StringBuilder(File.separatorChar + "LAR" + nameLAR + "-" + dateFormat.format(today.getTime()) + ".cfg");
            deleteFile = new StringBuilder(File.separatorChar + "LAR" + nameLAR + "-" + dateFormat.format(monthAgo.getTime()) + ".cfg");
        }

		OutputStream outputStream = null;

		try {
			switch (type) {
				case CENTRE_DEL:
				case CENTRE_BACKUP:
					if (RunningConfigTypeEmum.CENTRE_DEL.equals(type)) {
					    if (isGlobeUser) {
	                        path.insert(0, basePath + File.separatorChar + "running-config_backups" + File.separatorChar + "ADMIN");
	                        deleteFile.insert(0, basePath + File.separatorChar + File.separatorChar + "running-config_backups" + File.separatorChar + "ADMIN");
	                        dir = new File(basePath + File.separatorChar + "running-config_backups" + File.separatorChar + "ADMIN" + File.separatorChar + "LAR" + nameLAR);
					    } else {
                            path.insert(0, basePath + File.separatorChar + "MANUAL-ADMIN");
                            deleteFile.insert(0, basePath + File.separatorChar + "MANUAL-ADMIN");
                            dir = new File(basePath + File.separatorChar + "MANUAL-ADMIN");
					    }
					} else if (RunningConfigTypeEmum.CENTRE_BACKUP.equals(type)) {
                        if (isGlobeUser) {
                            path.insert(0, basePath + File.separatorChar + "running-config_backups" + File.separatorChar + "RUNNING");
                            deleteFile.insert(0, basePath + File.separatorChar + File.separatorChar + "running-config_backups" + File.separatorChar + "RUNNING");
                            dir = new File(basePath + File.separatorChar + "running-config_backups" + File.separatorChar + "RUNNING" + File.separatorChar + "LAR" + nameLAR);
                        } else {
                            path.insert(0, basePath + File.separatorChar + "MANUAL-RUNNING");
                            deleteFile.insert(0, basePath + File.separatorChar + "MANUAL-RUNNING");
                            dir = new File(basePath + File.separatorChar + "MANUAL-RUNNING");
                        }
					}
					
					if (null != dir) {
						if (!dir.exists()) {
							dir.mkdirs();
						} else if (!isGlobeUser) {
						    FileUtils.deleteDirectory(dir);
                            dir.mkdirs();
						}
						
						outputStream = new FileOutputStream(new File(path.toString()));
						
						byte[] bytes = new byte[1024];
						
						while (is.available() > 0) {
							int i = is.read(bytes, 0, 1024);
							if (i < 0) {
								break;
							}
							outputStream.write(bytes, 0, i);
							log.debug(new String(bytes, 0, i));
							Thread.sleep(500);
						}
						
						Thread.sleep(2000);
						
						result = true;
					}
					
					File del = new File(deleteFile.toString());
					log.debug("Detact exist (true/false) [{}] of 30 Days Ago File [{}]", del.exists(), deleteFile);
					if (del.exists()) {
						FileOperate.delFile(deleteFile.toString());
					}
					break;
				case LAR_DEL:
				case LAR_BACKUP:
					break;
			}
			result = false;
			
		} catch (FileNotFoundException e) {
			log.error("Writing File Error. User[{}], File[{}], Cause[{}]", new Object[]{user.getUserName(), path.toString(), e.getCause()});
			e.printStackTrace();
		} catch (IOException e) {
			log.error("Writing File Error. User[{}], File[{}], Cause[{}]", new Object[]{user.getUserName(), path.toString(), e.getCause()});
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
				log.info("File [{}] saved", path.toString());
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				return result;
			}
		}
	}
}
