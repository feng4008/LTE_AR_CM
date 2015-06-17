package com.hwacom.cm.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hwacom.cm.model.ConnectionModel;
import com.hwacom.cm.service.SSHConnectionService;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public abstract class AbstractSSHConnectionService implements SSHConnectionService {
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected ConnectionModel manager;
    protected Session session;
    
    protected InputStream in;
    protected OutputStream out;
    
    protected static final int X = 170;
    protected static final int Y = 1;
    
    public AbstractSSHConnectionService (ConnectionModel manager) throws IOException {
        this.manager = manager;
        init();
    }
    
    private void init() throws IOException {
        log.debug("{} method: init", this.getClass().getName());
        if (manager == null) {
            session = null;
        }
        
        Connection conn = new Connection(manager.getTargetIp());
        conn.connect();
        boolean isAuthenticated =
                conn.authenticateWithPassword(manager.getTargetUserName(), manager.getTargetPassword());
        if (isAuthenticated == false){
            throw new IOException("Authentication failed.");
        }
        session = conn.openSession();
        log.debug("session connected IP[{}]", this.manager.getTargetIp());
        
        session.requestPTY("dumb", X, Y, 0, 0, null);
        session.startShell();
        
        this.in = new StreamGobbler(this.session.getStdout());
        this.out = this.session.getStdin();
    }
    
    public void closeSession() {
        try {
            if (null != this.in) {
                this.in.close();
            }
            if (null != this.out) {
                this.out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(session != null){
            session.close();
        }
    }

    public ConnectionModel getManager() {
        return manager;
    }

    public void setCommand(String command) {
        this.manager.setCommand(command);
    }
    
}
