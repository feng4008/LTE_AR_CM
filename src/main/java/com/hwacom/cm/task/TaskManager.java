package com.hwacom.cm.task;

import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.hwacom.cm.model.IdentityTypeEmun;
import com.hwacom.cm.model.LoginUser;

public class TaskManager implements ApplicationContextAware {
	
	private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
	
	private ApplicationContext applicationContext;
	
	private Map<String, String> nes;
	
	private String neUserID;
	
	private String password;
	
	private String centreLogin;
	
	private String centrePin;
	
	private IdentityTypeEmun identity;
	
	/*
	 * KEEP THIS constructor! Using for ROMC/DEV spring applicationContext.xml, which defined arguments order.
	 */
    public TaskManager(
            Map<String, String> nes,
            String userName,
            String password,
            String centreLogin,
            String centrePin,
            int identity) {
        super();
        this.nes = nes;
        this.neUserID = userName;
        this.password = password;
        this.centreLogin = centreLogin;
        this.centrePin = centrePin;
        switch (identity) {
            case 0:
                this.identity = IdentityTypeEmun.ROMC;
                break;
            case 1:
                this.identity = IdentityTypeEmun.LAR;
                break;
            case 2:
            default:
                this.identity = IdentityTypeEmun.DEV;
                break;
        }
    }
    
    /*
     * KEEP THIS constructor! Using for NE spring applicationContext.xml, which defined arguments order.
     */
    public TaskManager(Map<String, String> nes, String centreLogin, String centrePin, int identity) {
        super();
        this.nes = nes;
        this.centreLogin = centreLogin;
        this.centrePin = centrePin;
        switch (identity) {
            case 0:
                this.identity = IdentityTypeEmun.ROMC;
                break;
            case 1:
                this.identity = IdentityTypeEmun.LAR;
                break;
            case 2:
            default:
                this.identity = IdentityTypeEmun.DEV;
                break;
        }
    }

	public void arrangeCollectConfigTask() {
	    if (StringUtils.isBlank(this.neUserID) || StringUtils.isBlank(this.password)) {
	        logger.debug("System never find neLoginInfo file, START manu login...");
	        Scanner scanner = new Scanner(System.in);
	        
            System.out.println("Please enter YOUR LAR LOGIN NAME");
	        neUserID = scanner.nextLine();

            System.out.println("Please enter YOUR LAR PASSWORD");
            password = scanner.nextLine();
        }
	    
        LoginUser user = new LoginUser(neUserID, password);
        LoginUser centreAcct = new LoginUser(centreLogin, centrePin);
        
        CollectConfigTask task = new CollectConfigTask(
                this.nes, user,
                centreAcct,
                this.identity);
        
        task.executeCollect();
    }
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public void setUserName(String userName) {
		this.neUserID = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setNes(Map<String, String> nes) {
		this.nes = nes;
	}

	public void setCentreLogin(String centreLogin) {
        this.centreLogin = centreLogin;
    }

    public void setCentrePin(String centrePin) {
        this.centrePin = centrePin;
    }

    public void setIdentity(int identity) {
        switch (identity) {
            case 0:
                this.identity = IdentityTypeEmun.ROMC;
                break;
            case 1:
                this.identity = IdentityTypeEmun.LAR;
                break;
            case 2:
            default:
                this.identity = IdentityTypeEmun.DEV;
                break;
        }
    }

}
