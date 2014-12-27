package com.hwacom.util.cm.service.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.ConnectionInfo;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import com.hwacom.util.cm.model.LoginUser;
import com.hwacom.util.cm.model.NetworkElement;
import com.hwacom.util.cm.service.PerformSSHExecService;

public class PerformSSHExecServiceImpl implements PerformSSHExecService {
	
	private static final Logger log = LoggerFactory.getLogger(PerformSSHExecServiceImpl.class);
	private static final int MAX_RETRIES = 5;
	
	@Override
	public void execCommand(NetworkElement ne, LoginUser user, String command) throws IOException, InterruptedException {
		Connection connection = new Connection(ne.getHost());
		connection.connect();
		boolean isAuthenticated = connection.authenticateWithPassword(user.getUserName(), user.getPassword());
		
		log.debug(connection.getConnectionInfo().toString());
		
		if (isAuthenticated == false) {
			log.error("Login authentication failed. NE [{}-IP {}], User [{}]",
				new Object[] { ne.getName(), ne.getHost(), user.getUserName()});
			throw new IOException("SSH Authentication Failed.");
		}
    	log.info("Host[{}] connected, login by [{}].", ne.getName(), user.getUserName());

    	Session session = connection.openSession();

		if(StringUtils.isNoneBlank(command)) {
			session.execCommand(command);
			log.debug("exec command [{}]", command);
			InputStream stdout = new StreamGobbler(session.getStdout());
			Thread.sleep(3000);
			
			StringBuffer sb = new StringBuffer();
			byte[] tmp = new byte[1024];
			while (stdout.available() > 0) {
				int i = stdout.read(tmp, 0, 1024);
				if (i < 0) {
					break;
				}
				sb.append(new String(tmp, 0, i));
			}
			log.debug("output from remote:\n{}", sb);
			if (sb.indexOf("confirm") >= 0) {
				session.getStdin().write("y\n".getBytes());
				log.debug("exec command [y]");
				Thread.sleep(3000);
				while (stdout.available() > 0) {
					int i = stdout.read(tmp, 0, 1024);
					if (i < 0) {
						break;
					}
					sb.append(new String(tmp, 0, i));
				}
				log.debug("output from remote:\n{}", sb);
			}
		}
		session.close();
		connection.close();
	}

	@Override
	public InputStream execSimpleCommand(NetworkElement ne, LoginUser user, String command) throws IOException, InterruptedException {
		Connection connection = new Connection(ne.getHost());
		ConnectionInfo info;
		info = connection.connect(null, 10000, 0);
		boolean isAuthenticated = false;
		int retries = 0;
		while ((++retries) <= MAX_RETRIES) {
	        isAuthenticated = connection.authenticateWithPassword(user.getUserName(), user.getPassword());
	        if (isAuthenticated) {
	            break;
	        } else {
	            Thread.sleep(2000);
	        }
		}
		
		if (isAuthenticated == false) {
			log.error("Login authentication failed. NE [{}-IP: {}], User [{}], Please RE-RUN this application and using correct authentication...",
				new Object[] { ne.getName(), ne.getHost(), user.getUserName()});
			throw new IOException("SSH Authentication Failed, please RE-RUN this application and using correct authentication.");
		}
    	log.info("Host[{}] connected, login by [{}].", ne.getName(), user.getUserName());

    	Session session = connection.openSession();

		if(StringUtils.isNoneBlank(command)) {
			session.execCommand(command);
			InputStream stdout = new StreamGobbler(session.getStdout());
			Thread.sleep(5000);
			return stdout;
		}
		
		session.close();
		connection.close();
		return null;
	}

}
