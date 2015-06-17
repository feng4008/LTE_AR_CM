package com.hwacom.util.cm.task;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Test;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.KnownHosts;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import com.hwacom.cm.model.LoginUser;

public class CollectConfigTaskTest {

	public static void main(String[] args) {
	    
    	//2U1 pilot "10.82.194.247"
    	//local VM "172.16.133.100"
    	String hostname = "172.16.133.100";
    	String username;
    	
    	//2U1 pilot "cht", "NOKIA-3g"
    	//local VM "feng", "2gliodri"
    	LoginUser user = new LoginUser("feng", "2gliodri");
    	
		String[] commands = { "show running-config", "admin show running-config" };
		// String[] commands = {"show running-config | file disk0:running-config.bak"};

		try {

	        
	        Connection conn = new Connection(hostname);
	        KnownHosts database = new KnownHosts();
	        
	        String[] hostkeyAlgos = database.getPreferredServerHostkeyAlgorithmOrder(hostname);
	        

	        if (hostkeyAlgos != null)
	            conn.setServerHostKeyAlgorithms(hostkeyAlgos);
	        
            String lastError = null;
	        
            while (true) {
                if (conn.isAuthMethodAvailable(user.getUserName(), "password")) {
                    
                    boolean res = conn.authenticateWithPassword(user.getUserName(), user.getPassword());

                    if (res) {
                        break;
                    }

                    lastError = "Password authentication failed.";

                    continue;
                }
            }
            
            Session sess = conn.openSession();

            sess.requestPTY("dumb", 0, 0, 0, 0, null);
            sess.startShell();
            
            
            
            
            
            
            
            
            
			conn.connect();

			/*
			 * Authenticate. If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */

			boolean isAuthenticated = conn.authenticateWithPassword(user.getUserName(), user.getPassword());

			if (isAuthenticated == false) {
				throw new IOException("Authentication failed.");
			}

			/* Create a session */

			//Session sess = conn.openSession();
			sess.execCommand("show running-config | file disk0:running-config.bak");

			System.out.println("Here is some information about the remote host:");

			/*
			 * This basic example does not handle stderr, which is sometimes
			 * dangerous (please read the FAQ).
			 */

			InputStream stdout = new StreamGobbler(sess.getStdout());
			StringBuffer sb = new StringBuffer();
			
			byte[] tmp = new byte[1024];
			while (stdout.available() > 0) {
				int i = stdout.read(tmp, 0, 1024);
				if (i < 0) {
					break;
				}
				sb.append(new String(tmp, 0, i));
			}
			System.out.println(sb.toString());
			
			sess.getStdin().write("y\n".getBytes());

			System.out.println("Here is some information about the remote host:");

			while (stdout.available() > 0) {
				int i = stdout.read(tmp, 0, 1024);
				if (i < 0) {
					break;
				}
				sb.append(new String(tmp, 0, i));
			}
			System.out.println(sb.toString());

			System.out.println("ExitCode: " + sess.getExitStatus());

			/* Close this session */

			sess.close();

			/* Close the connection */

			conn.close();

        }
        catch (IOException e)
        {
                e.printStackTrace(System.err);
                System.exit(2);
        }
	}

	@Test
	public void testCollectConfigTask() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DATE, -7);
		System.out.print(dateFormat.format(date.getTime()));
		assertTrue(true);
	}

}
