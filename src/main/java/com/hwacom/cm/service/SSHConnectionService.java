package com.hwacom.cm.service;

import java.io.IOException;

import com.hwacom.cm.model.ConnectionModel;


public interface SSHConnectionService {
    
    void execute() throws IOException, InterruptedException;
    
    ConnectionModel getManager();
}
