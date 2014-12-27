package com.hwacom.util.cm.task;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hwacom.util.cm.model.CommandEnum;
import com.hwacom.util.cm.model.LoginUser;
import com.hwacom.util.cm.model.NetworkElement;
import com.hwacom.util.cm.service.InputStreamToFileService;
import com.hwacom.util.cm.service.PerformSSHExecService;
import com.hwacom.util.cm.util.FileOperate;
import com.hwacom.util.cm.util.StreamConsumer;

public class CollectConfigTask {
	
	private static final Logger log = LoggerFactory.getLogger(CollectConfigTask.class);
	
	private InputStreamToFileService inputStreamToFileService;
	
	private PerformSSHExecService performSSHExecService;
	
	private Map<String, String> nes;
	
	private LoginUser user;
	
	private String localUser;
	
	public CollectConfigTask() {
		super();
	}

	public CollectConfigTask(InputStreamToFileService inputStreamToFileService, PerformSSHExecService performSSHExecService, Map<String, String> nes, LoginUser user) {
		super();
		this.inputStreamToFileService = inputStreamToFileService;
		this.performSSHExecService = performSSHExecService;
		this.nes = nes;
		this.user = user;
	}

	@SuppressWarnings("finally")
	public void executeCollect() {
		Iterator<Entry<String, String>> it = this.nes.entrySet().iterator();
		localUser = System.getProperty("user.name");
		log.debug("user[{}]", localUser);
		boolean isGlobeUser = (localUser.equals("larcmbkp")) ? true : false;
		log.debug("Is Run at Globe User? [{}]", isGlobeUser);
		Set<String> failedNESet = new HashSet<String>();
		while(it.hasNext()) {
			Entry<String, String> mapEntry = it.next();
			NetworkElement ne = new NetworkElement(mapEntry.getKey(), mapEntry.getValue());
			try {
		        if (isGlobeUser) {
	                this.getRunningConfig(ne, isGlobeUser);
		        } else if (mapEntry.getKey().equals(localUser.substring(3))) {
                    this.getRunningConfig(ne, isGlobeUser);
		        }
			} catch (InterruptedException e) {
                failedNESet.add(ne.getName());
				log.error("InterruptedException error occur when exec CM backup, please see excption log for detail. " +
				        "User[{}], NE[{}], Reason:[{}], Message: [{}]",
						new Object[] { this.user.getUserName(), ne.getHost(), e.getCause(), e.getMessage() });
                e.printStackTrace();
			} catch (IOException e) {
                failedNESet.add(ne.getName());
                log.error("IOException error occur when connect to NE, please see excption log for detail. " +
                        "User[{}], NE[{}]-IP[{}], Reason:[{}], Message:[{}]",
                        new Object[] { this.user.getUserName(), ne.getName(), ne.getHost(), e.getCause(), e.getMessage() });
                e.printStackTrace();
			} finally {
				continue;
			}
		}
		
		//alarm trigger
        if (isGlobeUser && !failedNESet.isEmpty()) {
            log.error("Total faild process [{}]", failedNESet.size());
            for (String faildNE : new ArrayList<String>(failedNESet)) {
                try {
                    String alarmCall = "/opt/nokia/oss/bin/arcasimx /home/larcmbkp/alarmtrigger/alarm_"
                            + faildNE + ".txt";
                    log.error("TRIGGER NETACT ALARM [{}]", alarmCall);
                    Runtime rt = Runtime.getRuntime();
                    Process proc;
                    proc = rt.exec(alarmCall);
                    StreamConsumer errorConsumer = new StreamConsumer(
                            proc.getErrorStream(), "error");
                    StreamConsumer outputConsumer = new StreamConsumer(
                            proc.getInputStream(), "output");
                    errorConsumer.start();
                    outputConsumer.start();
                    int exitVal = proc.waitFor();
                } catch (IOException e) {
                    log.error(
                            "InterruptedException error occur when exec CM backup, please see excption log for detail. User[{}], NE[{}], Reason:[{}], Message: [{}]",
                            new Object[] { this.user.getUserName(), faildNE, e.getCause(), e.getMessage() });
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    log.error(
                            "InterruptedException error occur when exec CM backup, please see excption log for detail. User[{}], NE[{}], Reason:[{}], Message: [{}]",
                            new Object[] { this.user.getUserName(), faildNE, e.getCause(), e.getMessage() });
                    e.printStackTrace();
                }
            }
        }
        
	}

	private void getRunningConfig(NetworkElement ne, boolean isGlobeUser) throws IOException, InterruptedException {
		
		CommandEnum[] commands = {
				CommandEnum.RUNNING_SHOW,
				CommandEnum.ADMIN_SHOW,
				CommandEnum.RUNNING_LOCAL_BACKUP,
				CommandEnum.ADMIN_LOCAL_BACKUP};
		
//		InputStream stdout = null;

    	for (CommandEnum command : commands) {
    	    Calendar today = Calendar.getInstance();
    	    
    	    StringBuilder backupHere = new StringBuilder();
    	    InetAddress address = InetAddress.getLocalHost();
    	    //"show running-config | file ftp://login:password@xxx.xxx.xxx.xxx/"
    	    backupHere.append(command.getCommand() + user.getUserName() + ":" + user.getPassword() + "@" + address.toString() + "/");
    	    StringBuilder deleteFile = new StringBuilder();
    	    String pathHere = "/home/" + localUser;
    	    File checkPath;
    	    
    		String backupLocal = null;
    		String deletecomm = null;
    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    		Calendar weekAgo = Calendar.getInstance();
    		weekAgo.add(Calendar.DATE, -7);
    		
            Calendar monthAgo = Calendar.getInstance();
            monthAgo.add(Calendar.DATE, -30);
    		
            if (isGlobeUser) {
                //"/LARxxx/LARxxx-yyyy-MM-dd.cfg"
                deleteFile = new StringBuilder(File.separatorChar + "LAR" + ne.getName() + File.separatorChar + "LAR"
                        + ne.getName() + "-" + dateFormat.format(monthAgo.getTime()) + ".cfg");
            } else {
                //"/LARxxx-yyyy-MM-dd.cfg"
                deleteFile = new StringBuilder(File.separatorChar + "LAR" + ne.getName() + "-" + dateFormat.format(monthAgo.getTime()) + ".cfg");
            }
    		
			switch (command.getRunningConfigTypeEmum()) {
				case RUNNING_SAVE:
		            if (isGlobeUser) {
		                //"show running-config | file ftp://login:password@xxx.xxx.xxx.xxx/RUNNING/LARxxx/LARxxx-yyyy-MM-dd.cfg vrf OAM"
		                backupHere.append("RUNNING/");
		                backupHere.append("LAR" + ne.getName() + "/LAR" + ne.getName() + "-" + dateFormat.format(today.getTime()) + ".cfg vrf OAM");
		                log.info("COMMAND [{}]", backupHere.toString());
		                performSSHExecService.execCommand(ne, user, backupHere.toString());
		                
		                pathHere += "/RUNNING/LAR" + ne.getName() + "/LAR" + ne.getName() + "-" + dateFormat.format(today.getTime()) + ".cfg";

		                //"/home/<login>/running-config_backups/RUNNING/LARxxx/LARxxx-yyyy-MM-dd.cfg"
                        deleteFile.insert(0, File.separatorChar + "home" + File.separatorChar + localUser + File.separatorChar + "running-config_backups" + File.separatorChar + "RUNNING");
		            } else {
                        //"show running-config | file ftp://login:password@xxx.xxx.xxx.xxx/MANUAL-RUNNING/LARxxx-yyyy-MM-dd.cfg vrf OAM"
		                backupHere.append("MANUAL-RUNNING/");
		                backupHere.append("LAR" + ne.getName() + "-" + dateFormat.format(today.getTime()) + ".cfg vrf OAM");
		                log.info("COMMAND [{}]", backupHere.toString());
                        performSSHExecService.execCommand(ne, user, backupHere.toString());
		                
		                pathHere += "/MANUAL-RUNNING/LAR" + ne.getName() + "-" + dateFormat.format(today.getTime()) + ".cfg";
		            }
		            /*
					stdout = performSSHExecService.execSimpleCommand(ne, user, command.getCommand());
					inputStreamToFileService.inputStreamToFile(command.getRunningConfigTypeEmum(), stdout, ne.getName(), this.user);
					*/
		            checkPath = new File(pathHere);
		            if (!checkPath.exists()) {
		                FileOperate.newFolder(pathHere);
		            }
		            
                    performSSHExecService.execCommand(ne, user, backupHere.toString());
					log.info("Remote backup completed - RUNNING. User[{}], LAR[{}]", this.user.getUserName(), ne.getName());
					break;
				case ADMIN_SAVE:
                    if (isGlobeUser) {
                        //"show running-config | file ftp://login:password@xxx.xxx.xxx.xxx/ADMIN/LARxxx/LARxxx-yyyy-MM-dd.cfg vrf OAM"
                        backupHere.append("ADMIN/");
                        backupHere.append("LAR" + ne.getName() + "/LAR" + ne.getName() + "-" + dateFormat.format(today.getTime()) + ".cfg vrf OAM");
                        log.info("COMMAND [{}]", backupHere.toString());
                        performSSHExecService.execCommand(ne, user, backupHere.toString());
                        
                        pathHere += "/ADMIN/LAR" + ne.getName() + "/LAR" + ne.getName() + "-" + dateFormat.format(today.getTime()) + ".cfg";

                        //"/home/<login>/running-config_backups/ADMIN/LARxxx/LARxxx-yyyy-MM-dd.cfg"
                        deleteFile.insert(0, File.separatorChar + "home" + File.separatorChar + localUser + File.separatorChar + "running-config_backups" + File.separatorChar + "ADMIN");
                    } else {
                        //"show running-config | file ftp://login:password@xxx.xxx.xxx.xxx/MANUAL-ADMIN/LARxxx-yyyy-MM-dd.cfg vrf OAM"
                        backupHere.append("MANUAL-ADMIN/");
                        backupHere.append("LAR" + ne.getName() + "-" + dateFormat.format(today.getTime()) + ".cfg vrf OAM");
                        log.info("COMMAND [{}]", backupHere.toString());
                        performSSHExecService.execCommand(ne, user, backupHere.toString());
                        
                        pathHere += "/MANUAL-ADMIN/LAR" + ne.getName() + "-" + dateFormat.format(today.getTime()) + ".cfg";
                    }
                    /*
					stdout = performSSHExecService.execSimpleCommand(ne, user, command.getCommand());
					inputStreamToFileService.inputStreamToFile(command.getRunningConfigTypeEmum(), stdout, ne.getName(), this.user);
                    */
                    checkPath = new File(pathHere);
                    if (!checkPath.exists()) {
                        FileOperate.newFolder(pathHere);
                    }
                    
                    performSSHExecService.execCommand(ne, user, backupHere.toString());
					log.info("Remote backup completed - ADMIN. User[{}], LAR[{}]", this.user.getUserName(), ne.getName());
					break;
				case RUNNING_LOCAL_BACKUP:
					backupLocal = command.getCommand() + "." + dateFormat.format(new Date()) + ".cfg";
					deletecomm = "delete disk1:running-config." + dateFormat.format(weekAgo.getTime()) + ".cfg";
				case ADMIN_LOCAL_BACKUP:
					if (StringUtils.isBlank(backupLocal)) {
    					backupLocal = command.getCommand() + "." + dateFormat.format(new Date()) + ".cfg";
					}
					if (StringUtils.isBlank(deletecomm)) {
    					deletecomm = "delete disk1:admin-running-config." + dateFormat.format(weekAgo.getTime()) + ".cfg";
					}
					performSSHExecService.execCommand(ne, user, backupLocal);
					log.info("Local backup completed. User[{}], LAR[{}]", this.user.getUserName(), ne.getName());
					performSSHExecService.execCommand(ne, user, deletecomm);
                    log.info("Local delete completed. User[{}], LAR[{}]", this.user.getUserName(), ne.getName());
					break;
			}
            File del = new File(deleteFile.toString());
            log.debug("Detact exist (true/false) [{}] of 30 Days Ago File [{}]", del.exists(), deleteFile);
            if (del.exists()) {
                FileOperate.delFile(deleteFile.toString());
            }
            
			/*
			if (stdout != null) {
				stdout.close();
			}
			*/
    	}
        log.info("User[{}], NE[{}] - Running-Config BACKUP COMPLETED.", this.user.getUserName(), ne.getName());
	}

}
