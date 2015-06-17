package com.hwacom.cm.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;

import com.hwacom.cm.model.ConnectionModel;

import ch.ethz.ssh2.ChannelCondition;

public class ComplicateSSHConnectionService extends AbstractSSHConnectionService {

    public ComplicateSSHConnectionService(ConnectionModel manager) throws IOException {
        super(manager);
    }

    @Override
    public void execute() throws IOException, InterruptedException {
        this.sendCommand(out, this.getManager().getCommand());
        session.waitForCondition(ChannelCondition.CLOSED | ChannelCondition.EOF | ChannelCondition.EXIT_STATUS, 30000);
        
        while (true) {
            String feedback = this.remoteFeedback(in);
            if (StringUtils.isNotBlank(feedback)) {
                if (feedback.toLowerCase().contains("password")) {
                    Thread.sleep(10000);
                    this.sendCommand(out, this.getManager().getVisitPassword());
                    Thread.sleep(60000);
                } else if (feedback.toLowerCase().contains("confirm")
                        && (feedback.toLowerCase().contains("ok"))) {
                    Thread.sleep(3000);
                } else if (feedback.toLowerCase().contains("ok")
                        || feedback.toLowerCase().contains("bytes/sec")
                        || feedback.toLowerCase().contains("no such file")) {
                    log.debug("CONSOLE PRINT to TERMINATE TASK [{}]", feedback);
                    break;
                }
            } else {
                log.debug("NO OUTPUT in CONSOLE");
                break;
            }
        }
        return;
    }
    
    private void sendCommand(OutputStream out, String command) {
        log.debug("ready send command [{}]", command);
        try {
            for (int i = 0; i < command.length(); i++) {
                char c = command.charAt(i);
                out.write(c);
            }
            out.write(13);
            out.write(10);
            log.debug("send LF CR");
        } catch (IOException e) {
            log.error("Command Input Error []", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String remoteFeedback(InputStream in) {
        try {
            StringBuffer sb = new StringBuffer();
            
            int loop = 0;
            byte[] tmp = new byte[8192];
            while (in.available() > 0) {
                log.debug("parse byte in loop [{}]", ++loop);
                int i = in.read(tmp, 0, 8192);
                if (i < 0) {
                    break;
                }
                sb.append(new String(tmp, 0, i));
            }
            log.debug(sb.toString());
            /*
            if (StringUtils.isNotBlank(sb.toString())) {
                log.debug(sb.toString());
            }
            */
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
