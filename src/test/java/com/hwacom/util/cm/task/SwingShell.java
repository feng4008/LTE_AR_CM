package com.hwacom.util.cm.task;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.KnownHosts;
import ch.ethz.ssh2.ServerHostKeyVerifier;
import ch.ethz.ssh2.Session;

public class SwingShell {

    /*
     * NOTE: to get this feature to work, replace the "tilde" with your home
     * directory, at least my JVM does not understand it. Need to check the
     * specs.
     */

    static final String knownHostPath = "~/.ssh/known_hosts";

    KnownHosts database = new KnownHosts();

    public SwingShell() {
        File knownHostFile = new File(knownHostPath);
        if (knownHostFile.exists()) {
            try {
                database.addHostkeys(knownHostFile);
            } catch (IOException e) {
            }
        }
    }

    /**
     * TerminalDialog is probably the worst terminal emulator ever written -
     * implementing a real vt100 is left as an exercise to the reader, i.e., to
     * you =)
     * 
     */
    class InvisibleTerminal {

        Session sess;
        InputStream in;
        OutputStream out;

        int x, y;
        
        public InvisibleTerminal(Session sess, int x, int y) throws IOException {
            this.sess = sess;
            this.x = x;
            this.y = y;
            
            in = sess.getStdout();
            out = sess.getStdin();
            
            String command = "scp disk0:/running-config-2015-02-28.cfg feng@172.16.133.1:/Users/feng/Develop/Project/01_JAVA/LTE_AR_CM/running-config-2015-02-28.cfg\n";
            for (int i = 0; i < command.length(); i ++) {
                char c = command.charAt(i);
                out.write(c);
            }
            
            new RemoteConsumer().start();
        }

        /**
         * This thread consumes output from the remote server and displays it in
         * the terminal window.
         * 
         */
        class RemoteConsumer extends Thread {
            int posy = 0;
            int posx = 0;

            public void run() {
                byte[] buff = new byte[8192];

                try {
                    while (true) {
                        int len = in.read(buff);
                        if (len == -1) {
                            return;
                        }
                        addText(buff, len);
                    }
                } catch (Exception e) {
                }
            }

            private void addText(byte[] data, int len) throws IOException {
                char[][] lines = new char[y][];
                for (int i = 0; i < len; i++) {
                    char c = (char) (data[i] & 0xff);

                    if (c == 8) {// Backspace, VERASE
                        if (posx < 0) {
                            continue;
                        }
                        posx--;
                        continue;
                    }

                    if (c == '\r') {
                        posx = 0;
                        continue;
                    }

                    if (c == '\n') {
                        posy++;
                        if (posy >= y) {
                            for (int k = 1; k < y; k++) {
                                lines[k - 1] = lines[k];
                            }
                            posy--;
                            lines[y - 1] = new char[x];
                            for (int k = 0; k < x; k++) {
                                lines[y - 1][k] = ' ';
                            }
                        }
                        continue;
                    }

                    if (c < 32) {
                        continue;
                    }

                    if (posx >= x) {
                        posx = 0;
                        posy++;
                        if (posy >= y) {
                            posy--;
                            for (int k = 1; k < y; k++) {
                                lines[k - 1] = lines[k];
                            }
                            lines[y - 1] = new char[x];
                            for (int k = 0; k < x; k++) {
                                lines[y - 1][k] = ' ';
                            }
                        }
                    }

                    if (lines[posy] == null) {
                        lines[posy] = new char[x];
                        for (int k = 0; k < x; k++) {
                            lines[posy][k] = ' ';
                        }
                    }

                    lines[posy][posx] = c;
                    posx++;
                }

                StringBuffer sb = new StringBuffer(x * y);

                for (int i = 0; i < lines.length; i++) {
                    if (i != 0) {
                        sb.append('\n');
                    }

                    if (null != lines[i] && StringUtils.isNotEmpty(lines[i].toString())) {
                        sb.append(lines[i]);
                    }
                }
                System.out.println(sb.toString());
                String command;
                if (sb.toString().toLowerCase().contains("password")) {
                    command = "2gliodri\n";
                    for (int i = 0; i < command.length(); i ++) {
                        char c = command.charAt(i);
                        out.write(c);
                    }
                }
                if (sb.toString().toLowerCase().contains("bytes copied")) {
                    sess.close();
                }
            }
        }
    }
    
    class SpecialVerifier implements ServerHostKeyVerifier {

        @Override
        public boolean verifyServerHostKey(String hostname, int port,
                String serverHostKeyAlgorithm, byte[] serverHostKey)
                throws Exception {
            final String host = hostname;
            final String algo = serverHostKeyAlgorithm;

            String message;

            /* Check database */
            int result = database.verifyHostkey(hostname, serverHostKeyAlgorithm, serverHostKey);

            switch (result) {
                case KnownHosts.HOSTKEY_IS_OK:
                    return true;

                case KnownHosts.HOSTKEY_IS_NEW:
                    message = "Do you want to accept the hostkey (type " + algo
                            + ") from " + host + " ?\n";
                    break;

                case KnownHosts.HOSTKEY_HAS_CHANGED:
                    message = "WARNING! Hostkey for " + host
                            + " has changed!\nAccept anyway?\n";
                    break;

                default:
                    throw new IllegalStateException();
            }

            /* Include the fingerprints in the message */
            String hexFingerprint = KnownHosts.createHexFingerprint(
                    serverHostKeyAlgorithm, serverHostKey);
            String bubblebabbleFingerprint = KnownHosts
                    .createBubblebabbleFingerprint(serverHostKeyAlgorithm,
                            serverHostKey);

            message += "Hex Fingerprint: " + hexFingerprint
                    + "\nBubblebabble Fingerprint: " + bubblebabbleFingerprint;

            /* Be really paranoid. We use a hashed hostname entry */
            String hashedHostname = KnownHosts.createHashedHostname(hostname);

            /* Add the hostkey to the in-memory database */
            database.addHostkey(new String[] { hashedHostname },
                    serverHostKeyAlgorithm, serverHostKey);

            System.out.println(message);
            /* Also try to add the key to a known_host file */
            try {
                KnownHosts.addHostkeyToFile(new File(knownHostPath),
                        new String[] { hashedHostname },
                        serverHostKeyAlgorithm, serverHostKey);
            } catch (IOException ignore) {
            }

            return true;
        }
        
    }

    /**
     * The SSH-2 connection is established in this thread. If we would not use a
     * separate thread (e.g., put this code in the event handler of the "Login"
     * button) then the GUI would not be responsive (missing window repaints if
     * you move the window etc.)
     */
    class ConnectionThread extends Thread {
        String hostname;
        String username;

        public ConnectionThread(String hostname, String username) {
            this.hostname = hostname;
            this.username = username;
        }

        public void run() {
            Connection conn = new Connection(hostname);

            try {
                /*
                 * CONNECT AND VERIFY SERVER HOST KEY (with callback)
                 */

                String[] hostkeyAlgos = database.getPreferredServerHostkeyAlgorithmOrder(hostname);

                if (hostkeyAlgos != null) {
                    conn.setServerHostKeyAlgorithms(hostkeyAlgos);
                }

                conn.connect(new SpecialVerifier());

                /*
                 * AUTHENTICATION PHASE
                 */

                String lastError = null;

                while (true) {

                    if (conn.isAuthMethodAvailable(username, "password")) {
                        boolean res = conn.authenticateWithPassword(username, "2gliodri");
                        
                        if (res) {
                            break;
                        }

                        lastError = "Password authentication failed.";
                        continue;
                    }

                    throw new IOException("No supported authentication methods available.");
                }

                /* AUTHENTICATION OK. DO SOMETHING.*/

                Session sess = conn.openSession();

                int x_width = 150;
                int y_width = 10;

                sess.requestPTY("dumb", x_width, y_width, 0, 0, null);
                sess.startShell();
                
                InvisibleTerminal td = new InvisibleTerminal(sess, x_width, y_width);

            } catch (IOException e) {
                e.printStackTrace();
            }

            /* CLOSE THE CONNECTION.*/
            
            /*
            conn.close();
            */
        }
    }

    void loginPressed() {
        String hostname = "172.16.133.100";
        String username = "feng";
        ConnectionThread ct = new ConnectionThread(hostname, username);
        ct.start();
    }

    void startGUI() {
        Runnable r = new Runnable() {
            public void run() {
                loginPressed();
            }
        };

        SwingUtilities.invokeLater(r);

    }

    public static void main(String[] args) {
        SwingShell client = new SwingShell();
        client.startGUI();
    }
}
