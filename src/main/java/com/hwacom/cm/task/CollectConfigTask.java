package com.hwacom.cm.task;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hwacom.cm.model.CommandEnum;
import com.hwacom.cm.model.ConnectionModel;
import com.hwacom.cm.model.IdentityTypeEmun;
import com.hwacom.cm.model.LoginUser;
import com.hwacom.cm.model.NetworkElement;
import com.hwacom.cm.service.impl.ComplicateSSHConnectionService;
import com.hwacom.cm.util.FileOperate;
import com.hwacom.cm.util.StreamConsumer;

public class CollectConfigTask {
	
	private static final Logger log = LoggerFactory.getLogger(CollectConfigTask.class);
	
	private Map<String, String> nes;
	
	private LoginUser user;
	
	private LoginUser centreAcct;
	
	private IdentityTypeEmun identity;
	
	public CollectConfigTask() {
		super();
	}

    public CollectConfigTask(
            Map<String, String> nes,
            LoginUser user,
            LoginUser centreAcct,
            IdentityTypeEmun identity) {
        super();
        this.nes = nes;
        this.user = user;
        this.centreAcct = centreAcct;
        this.identity = identity;
    }

	@SuppressWarnings("finally")
	public void executeCollect() {
		Set<String> failedNESet = new HashSet<String>();
		
		// processing NE configuration one by one
        Iterator<Entry<String, String>> it = this.nes.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, String> mapEntry = it.next();
			NetworkElement ne = new NetworkElement(mapEntry.getKey(), mapEntry.getValue());
			try {
                this.getRunningConfig(ne);
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
		
		//alarm trigger only work at ROMC centre backup
        if (this.identity.equals(IdentityTypeEmun.ROMC) && !failedNESet.isEmpty()) {
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
                    proc.waitFor();
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

	private void getRunningConfig(NetworkElement ne) throws IOException, InterruptedException {
		
		CommandEnum[] commands = {
                CommandEnum.LAR_BACKUP,
                CommandEnum.CENTRE_BACKUP,
                CommandEnum.LAR_DEL,
                CommandEnum.CENTRE_DEL};
		
        int iTag = -1; //record success admin backup LAR card location
        int jTag = -1; //record success running backup LAR card location

        Calendar today = Calendar.getInstance();
        Calendar weekAgo = Calendar.getInstance();
        weekAgo.add(Calendar.DATE, -7);
        Calendar monthAgo = Calendar.getInstance();
        monthAgo.add(Calendar.DATE, -30);
        
        InetAddress address = InetAddress.getLocalHost();   
        log.info("CM backup server ip [{}]", address.getHostAddress());
        
        ConnectionModel manager = new ConnectionModel();
        manager.setTargetIp(ne.getHost());
        manager.setTargetUserName(this.user.getUserName());
        manager.setTargetPassword(this.user.getPassword());
        manager.setVisitorIp(address.getHostAddress().toString());
        manager.setVisitUserName(this.centreAcct.getUserName());
        manager.setVisitPassword(this.centreAcct.getPassword());
        
        ComplicateSSHConnectionService connectionService = new ComplicateSSHConnectionService(manager);
    	for (CommandEnum command : commands) {
            String[] cmdLarCopies = null;
            String cmdScpCentre = null;
            Boolean[] larBackupResule = new Boolean[] {false, false, false, false, false, false, false, false};
            int larBackupResuleIdx = 0;
            String backupPath = "";
            String oam = "";
            File checkPath;

            if (this.identity.equals(IdentityTypeEmun.ROMC)) {
                // backupPath = /home/larcmbkp/[ADMIN]/LARxxx/
                backupPath = "/home/" + this.centreAcct.getUserName() + "/running-config_backups/%1$s/LAR" + ne.getName() + "/";
                oam = "vrf OAM";
            } else if (this.identity.equals(IdentityTypeEmun.LAR)) {
                // backupPath = /home/larcmbkp/[ADMIN]/
                backupPath = "/home/" + this.centreAcct.getUserName() + "/%1$s/";
                oam = "vrf OAM";
            } else {
                // backupPath = /Users/feng/Develop/Project/01_JAVA/LTE_AR_CM/[ADMIN]/
                backupPath = "/Users/feng/Develop/Project/01_JAVA/LTE_AR_CM/%1$s/";
            }
            
            switch (command.getRunningConfigTypeEmum()) {
                case LAR_BACKUP:
                    log.debug("LAR_BACKUP LARC{}", ne.getName());
                    cmdLarCopies = this.produceLARBackupCmds(command, today);
                    for (String cmd : cmdLarCopies) {
                        log.debug("command [{}]", cmd);
                        
                        connectionService.setCommand(cmd);
                        connectionService.execute();
                        
                        larBackupResule[larBackupResuleIdx] = new Boolean(true);
                        if (larBackupResuleIdx < 4) {
                            iTag = larBackupResuleIdx;
                            log.debug("success backup admin-run at LAR{} card [{}]", ne.getName(), larBackupResuleIdx+1);
                        } else {
                            jTag = larBackupResuleIdx;
                            log.debug("success backup run at LAR{} card [{}]", ne.getName(), larBackupResuleIdx-3);
                        }
                        larBackupResuleIdx ++;
                    }
                    break;
                case LAR_DEL:
                    log.debug("LAR_DEL LAR{}", ne.getName());
                    cmdLarCopies = this.produceLARDelCmds(command, weekAgo);
                    for (String cmd : cmdLarCopies) {
                        log.debug("command [{}]", cmd);
                        
                        connectionService.setCommand(cmd);
                        connectionService.execute();
                    }
                    break;
                case CENTRE_BACKUP:
                    log.debug("CENTRE_BACKUP LAR{}", ne.getName());
                    log.debug("Identity [{}]", this.identity);
                    log.debug("success admin backup location [{}], running backup location [{}]", iTag, jTag);
                    if (!this.identity.equals(IdentityTypeEmun.DEV)) { // ROMC, LAR
                        // backup RUNNING
                        if (jTag >= 4) {
                            String path = String.format(backupPath, "RUNNING");
                            checkPath = new File(path);
                            if (!checkPath.exists()) {
                                FileOperate.newFolder(path);
                            }
                            
                            String location = this.produceLocation(jTag);
//                            log.debug("command [{}]", command.getCommand());
                            cmdScpCentre = String.format(command.getCommand(),
                                    1, "", today, location,
                                    this.centreAcct.getUserName(), address.getHostAddress().toString(),
                                    path, oam);
                            
                            connectionService.setCommand(cmdScpCentre);
                            connectionService.execute();
                            
                            log.info("User[{}], NE[{}] - RUNNING BACKUP COMPLETED, command [{}].", this.user.getUserName(), ne.getName(), cmdScpCentre);
                        }
                        // backup ADMIN
                        if (iTag >= 0) {
                            String path = String.format(backupPath, "ADMIN");
                            checkPath = new File(path);
                            if (!checkPath.exists()) {
                                FileOperate.newFolder(path);
                            }
                            
                            String location = this.produceLocation(iTag);
//                            log.debug("command [{}]", command.getCommand());
                            cmdScpCentre = String.format(command.getCommand(),
                                    1, "admin-", today, location,
                                    this.centreAcct.getUserName(), address.getHostAddress().toString(),
                                    path, oam);
                            
                            connectionService.setCommand(cmdScpCentre);
                            connectionService.execute();
                            
                            log.info("User[{}], NE[{}] - ADMIN BACKUP COMPLETED, command [{}].", this.user.getUserName(), ne.getName(), cmdScpCentre);
                        }
                    } else { //DEV
                        String path = String.format(backupPath, "ADMIN");
                        checkPath = new File(path);
                        if (!checkPath.exists()) {
                            FileOperate.newFolder(path);
                        }
                        log.debug("command [{}]", command.getCommand());
                        cmdScpCentre = String.format(command.getCommand(),
                                0, "admin-", today, "",
                                this.centreAcct.getUserName(), "172.16.133.1",
                                path, oam);
                        log.debug("CENTRE_BACKUP command [{}]", cmdScpCentre);
                        
                        connectionService.setCommand(cmdScpCentre);
                        connectionService.execute();
                        
                        log.info("User[{}], NE[{}] - admin-running-config BACKUP COMPLETED.", this.user.getUserName(), ne.getName());
                        
                        path = String.format(backupPath, "RUNNING");
                        checkPath = new File(path);
                        if (!checkPath.exists()) {
                            FileOperate.newFolder(path);
                        }
                        log.debug("command [{}]", command.getCommand());
                        cmdScpCentre = String.format(command.getCommand(),
                                0, "", today, "",
                                this.centreAcct.getUserName(), "172.16.133.1",
                                path, oam);
                        log.debug("CENTRE_BACKUP command [{}]", cmdScpCentre);
                        
                        connectionService.setCommand(cmdScpCentre);
                        connectionService.execute();
                        
                        log.info("User[{}], NE[{}] - running-config BACKUP COMPLETED.", this.user.getUserName(), ne.getName());
                    }
                    break;
                case CENTRE_DEL:
                    String path = String.format(backupPath, "ADMIN");
                    this.deleteFiles(path, 31, ".cfg");
                    path = String.format(backupPath, "RUNNING");
                    this.deleteFiles(path, 31, ".cfg");
                    break;
            }
    	}
        connectionService.closeSession();
	}
	
    private void deleteFiles(String path, long days, String extension) {
        File folder = new File(path);
        if (folder.exists()) {
            File[] listFiles = folder.listFiles();
            long eligibleForDeletion = System.currentTimeMillis()
                    - (days * 24 * 60 * 60 * 1000L);
            for (File listFile : listFiles) {
                if (listFile.getName().endsWith(extension)
                        && listFile.lastModified() < eligibleForDeletion) {
                    if (!listFile.delete()) {
                        log.debug("Can not delete file [{}].", listFile.getName());
                    }
                }
            }
        }
    }
    
    private String produceLocation(int tag) {
        String location = "location %1$d/RSP%2$d/CPU0";
        switch(tag) {
            case 0:
            case 4:
                location = String.format(location, 0, 0);
                break;
            case 1:
            case 5:
                location = String.format(location, 0, 1);
                break;
            case 2:
            case 6:
                location = String.format(location, 1, 0);
                break;
            case 3:
            case 7:
                location = String.format(location, 1, 1);
                break;
            default:
                location = "";
                break;
        }
        return location;
    }
	
	private String[] produceLARBackupCmds(CommandEnum command, Calendar calendar) {
	    String cmdLarCopies[] = null;
	    if (!this.identity.equals(IdentityTypeEmun.DEV)) {
	        cmdLarCopies = new String[]{
	                String.format(command.getCommand(), "admin ", 1, "admin-", calendar, " location 0/RSP0/CPU0"),
	                String.format(command.getCommand(), "admin ", 1, "admin-", calendar, " location 0/RSP1/CPU0"),
	                String.format(command.getCommand(), "admin ", 1, "admin-", calendar, " location 1/RSP0/CPU0"),
	                String.format(command.getCommand(), "admin ", 1, "admin-", calendar, " location 1/RSP1/CPU0"),
	                String.format(command.getCommand(), "", 1, "", calendar, " location 0/RSP0/CPU0"),
	                String.format(command.getCommand(), "", 1, "", calendar, " location 0/RSP1/CPU0"),
	                String.format(command.getCommand(), "", 1, "", calendar, " location 1/RSP0/CPU0"),
	                String.format(command.getCommand(), "", 1, "", calendar, " location 1/RSP1/CPU0")};
	    } else {
	        cmdLarCopies = new String[]{String.format(command.getCommand(), "admin ", 0, "admin-", calendar, ""),
                    String.format(command.getCommand(), "", 0, "", calendar, "")};
	    }
	    return cmdLarCopies;
	}
    
    private String[] produceLARDelCmds(CommandEnum command, Calendar calendar) {
        String cmdLarCopies[] = null;
        if (!this.identity.equals(IdentityTypeEmun.DEV)) {
            cmdLarCopies = new String[]{
                    String.format(command.getCommand(), 1, "admin-", calendar, " location 0/RSP0/CPU0"),
                    String.format(command.getCommand(), 1, "admin-", calendar, " location 0/RSP1/CPU0"),
                    String.format(command.getCommand(), 1, "admin-", calendar, " location 1/RSP0/CPU0"),
                    String.format(command.getCommand(), 1, "admin-", calendar, " location 1/RSP1/CPU0"),
                    String.format(command.getCommand(), 1, "", calendar, " location 0/RSP0/CPU0"),
                    String.format(command.getCommand(), 1, "", calendar, " location 0/RSP1/CPU0"),
                    String.format(command.getCommand(), 1, "", calendar, " location 1/RSP0/CPU0"),
                    String.format(command.getCommand(), 1, "", calendar, " location 1/RSP1/CPU0")};
        } else {
            cmdLarCopies = new String[]{String.format(command.getCommand(), "admin ", 0, "admin-", calendar, ""),
                    String.format(command.getCommand(), "", 0, "", calendar, "")};
        }
        return cmdLarCopies;
    }

}
