package com.hwacom.util.cm.task;

import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.hwacom.util.cm.model.LoginUser;
import com.hwacom.util.cm.service.InputStreamToFileService;
import com.hwacom.util.cm.service.PerformSSHExecService;

public class TaskManager implements ApplicationContextAware {
	
	private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
	private ApplicationContext applicationContext;
	private InputStreamToFileService inputStreamToFileService;
	private PerformSSHExecService performSSHExecService;
	
	private Map<String, String> nes;
	
	private String userName;
	
	private String password;
	
	/*
	 * KEEP THIS constructor! Using for ROMC/DEV spring applicationContext.xml, which defined arguments order.
	 */
	public TaskManager(Map<String, String> nes, String userName, String password) {
		super();
		this.nes = nes;
		this.userName = userName;
		this.password = password;
	}
    
    /*
     * KEEP THIS constructor! Using for NE spring applicationContext.xml, which defined arguments order.
     */
    public TaskManager(Map<String, String> nes) {
        super();
        this.nes = nes;
    }

	public void arrangeCollectConfigTask() {
	    if (StringUtils.isBlank(this.userName) || StringUtils.isBlank(this.password)) {
	        logger.debug("System never find neLoginInfo file, START manu login...");
	        Scanner scanner = new Scanner(System.in);
	        
            System.out.println("Please enter YOUR LAR LOGIN NAME");
	        userName = scanner.nextLine();

            System.out.println("Please enter YOUR LAR PASSWORD");
            password = scanner.nextLine();
	    }
		LoginUser user = new LoginUser(userName, password);
		CollectConfigTask task = new CollectConfigTask(inputStreamToFileService, performSSHExecService, nes, user);
		task.executeCollect();
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setNes(Map<String, String> nes) {
		this.nes = nes;
	}

	public void setInputStreamToFileService(
			InputStreamToFileService inputStreamToFileService) {
		this.inputStreamToFileService = inputStreamToFileService;
	}

	public void setPerformSSHExecService(PerformSSHExecService performSSHExecService) {
		this.performSSHExecService = performSSHExecService;
	}

}
