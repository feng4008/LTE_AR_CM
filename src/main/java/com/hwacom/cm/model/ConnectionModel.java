package com.hwacom.cm.model;

public class ConnectionModel {
    
    private String command;
    
    private String visitorIp;
    private String visitUserName;
    private String visitPassword;

    private String targetIp;
    private String targetUserName;
    private String targetPassword;
    
    public String getCommand() {
        return command;
    }
    public void setCommand(String command) {
        this.command = command;
    }
    public String getVisitorIp() {
        return visitorIp;
    }
    public void setVisitorIp(String visitorIp) {
        this.visitorIp = visitorIp;
    }
    public String getVisitUserName() {
        return visitUserName;
    }
    public void setVisitUserName(String visitUserName) {
        this.visitUserName = visitUserName;
    }
    public String getVisitPassword() {
        return visitPassword;
    }
    public void setVisitPassword(String visitPassword) {
        this.visitPassword = visitPassword;
    }
    public String getTargetIp() {
        return targetIp;
    }
    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }
    public String getTargetUserName() {
        return targetUserName;
    }
    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }
    public String getTargetPassword() {
        return targetPassword;
    }
    public void setTargetPassword(String targetPassword) {
        this.targetPassword = targetPassword;
    }
    
    
}
