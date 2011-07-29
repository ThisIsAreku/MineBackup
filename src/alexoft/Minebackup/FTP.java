package alexoft.Minebackup;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

 
/**
 * FTP is a simple package that implements a Java FTP client.
 * With FTP, you can connect to an FTP server and upload multiple files.
 * 
 * Modified version of http://www.jibble.org/simpleftp/
 */
public class FTP {
	
    /**
     * Log variable
     */
    private Logger log;
	
    /**
     * Create an instance of FTP.
     */
    public FTP(Logger log) {
        this.log = log;
        this.log.info("FTP initialized");
    }
	
    /**
     * Connects to the default port of an FTP server and logs in as
     * anonymous/anonymous.
     */
    public synchronized void connect(String host) throws IOException {
        log.info("Connect withour any informations");
        connect(host, 21);
    }
	
    /**
     * Connects to an FTP server and logs in as anonymous/anonymous.
     */
    public synchronized void connect(String host, int port) throws IOException {
        log.info("Connect annonymous");
        connect(host, port, "anonymous", "anonymous");
    }
	
    /**
     * Connects to an FTP server and logs in with the supplied username
     * and password.
     */
    public synchronized void connect(String host, int port, String user, String pass) throws IOException {
        log.info("Connect with user and pass");
        if (socket != null) {
            log.warning("already connected");
            throw new IOException("FTP is already connected. Disconnect first.");
        }
        socket = new Socket(host, port);
        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));
		
        String response = readLine();

        if (!response.startsWith("220 ")) {
            log.warning(response);
            throw new IOException(
                    "FTP received an unknown response when connecting to the FTP server: "
                            + response);
        }
		
        sendLine("USER " + user);
		
        response = readLine();
        if (!response.startsWith("331 ")) {
            log.warning(response);
            throw new IOException(
                    "FTP received an unknown response after sending the user: "
                            + response);
        }
		
        sendLine("PASS " + pass);
		
        response = readLine();
        if (!response.startsWith("230 ")) {
            log.warning(response);
            throw new IOException(
                    "FTP was unable to log in with the supplied password: "
                            + response);
        }
		
        // Now logged in.
    }
	
    /**
     * Disconnects from the FTP server.
     */
    public synchronized void disconnect() throws IOException {
        try {
            sendLine("QUIT");
        } finally {
            socket = null;
        }
    }
	
    /**
     * Returns the working directory of the FTP server it is connected to.
     */
    public synchronized String pwd() throws IOException {
        sendLine("PWD");
        String dir = null;
        String response = readLine();

        if (response.startsWith("257 ")) {
            int firstQuote = response.indexOf('\"');
            int secondQuote = response.indexOf('\"', firstQuote + 1);

            if (secondQuote > 0) {
                dir = response.substring(firstQuote + 1, secondQuote);
            }
        }
        return dir;
    }
 
    /**
     * Changes the working directory (like cd). Returns true if successful.
     */   
    public synchronized boolean cwd(String dir) throws IOException {
        sendLine("CWD " + dir);
        String response = readLine();

        return (response.startsWith("250 "));
    }
	
    /**
     * Sends a file to be stored on the FTP server.
     * Returns true if the file transfer was successful.
     * The file is sent in passive mode to avoid NAT or firewall problems
     * at the client end.
     */
    public synchronized boolean stor(File file) throws IOException {
        if (file.isDirectory()) {
            throw new IOException("FTP cannot upload a directory.");
        }
		
        String filename = file.getName();
 
        return stor(new FileInputStream(file), filename);
    }
	
    /**
     * Sends a file to be stored on the FTP server.
     * Returns true if the file transfer was successful.
     * The file is sent in passive mode to avoid NAT or firewall problems
     * at the client end.
     */
    public synchronized boolean stor(InputStream inputStream, String filename) throws IOException {
 
        BufferedInputStream input = new BufferedInputStream(inputStream);
		
        Socket dataSocket = passive();
 
        sendLine("STOR " + filename);
 
        String response = readLine();

        if (!response.startsWith("150 ")) {
            throw new IOException(
                    "FTP was not allowed to send the file: " + response);
        }
		
        BufferedOutputStream output = new BufferedOutputStream(
                dataSocket.getOutputStream());
        byte[] buffer = new byte[4096];
        int bytesRead = 0;

        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
        output.close();
        input.close();
		
        response = readLine();
        return response.startsWith("226 ");
    }
 
    /**
     * Enter binary mode for sending binary files.
     */
    public synchronized boolean bin() throws IOException {
        sendLine("TYPE I");
        String response = readLine();

        return (response.startsWith("200 "));
    }
	
    /**
     * Enter ASCII mode for sending text files. This is usually the default
     * mode. Make sure you use binary mode if you are sending images or
     * other binary data, as ASCII mode is likely to corrupt them.
     */
    public synchronized boolean ascii() throws IOException {
        sendLine("TYPE A");
        String response = readLine();

        return (response.startsWith("200 "));
    }
 
    /**
     * Just a simple function that log the help command !
     * @return null
     * @throws IOException
     */
    public synchronized String[] help() throws IOException {
        sendLine("HELP");
        String response;
        int nlCount = 0; 

        while (nlCount < 2) {
            response = readLine();
            if (response.charAt(response.length() - 1) == 46) {
                nlCount++;
            }
        }
        return null;
    }
	
    /**
     * This function return a String array corresponding to the filenames
     * that are on the repository on the specified path !
     * 
     * @return The filenames String array requested
     * @throws IOException
     */
    public synchronized String[] ls() throws IOException {
        Socket sok = passive();

        sendLine("LIST");
		
        String response = readLine();

        if (!response.startsWith("150 ")) {
            throw new IOException("Could not read the directory");
        }
        response = readLine();
        if (!response.startsWith("226 ")) {
            throw new IOException("Could not read the directory");
        }
		
        ArrayList<String> files = new ArrayList<String>();
        String[] ls = readInputStream(sok.getInputStream());

        for (int i = 0; i < ls.length; i++) {
            files.add(ls[i].substring(ls[i].lastIndexOf(' ')));
        }
 
        return (String[]) files.toArray(new String[files.size()]);
    }
	
    /**
     * This function is able to delete a file from the repository.
     * 
     * @param filename The filename to delete
     * @return true if all is ok
     * @throws IOException
     */
    public synchronized boolean delete(String filename) throws IOException {
        sendLine("DELE " + filename);
        String response = readLine();

        if (!response.startsWith("250 ")) {
            throw new IOException("FTP is not allowed to delete this file"); 
        }
        return true;
    }
	
    /**
     * This function is used to get a String array from an InputStream
     * 
     * @param is The input stream to read
     * @return The String array requested
     * @throws IOException
     */
    private String[] readInputStream(InputStream is) throws IOException {
        ArrayList<String> a = new ArrayList<String>();
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String s;

        while ((s = r.readLine()) != null) {
            // log.(s);
            a.add(s);
        }
        return (String[]) a.toArray(new String[a.size()]);
    }
	
    /**
     * This function is used to retrieve a Socket from the FTP server
     * while requesting a passive mode !
     * 
     * @return The socket expected
     * @throws IOException
     */
    private synchronized Socket passive() throws IOException {
        sendLine("PASV");
        String response = readLine();

        if (!response.startsWith("227 ")) {
            throw new IOException(
                    "FTP could not request passive mode: " + response);
        }
		
        String ip = null;
        int port = -1;
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);

        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");

            try {
                ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
                        + tokenizer.nextToken() + "." + tokenizer.nextToken();
                port = Integer.parseInt(tokenizer.nextToken()) * 256
                        + Integer.parseInt(tokenizer.nextToken());
            } catch (Exception e) {
                throw new IOException(
                        "FTP received bad data link information: " + response);
            }
        }
		
        return new Socket(ip, port);
    }
	
    /**
     * Sends a raw command to the FTP server.
     */
    private void sendLine(String line) throws IOException {
        // log.debug(">> " + line);
        if (socket == null) {
            throw new IOException("FTP is not connected.");
        }
        try {
            writer.write(line + "\r\n");
            writer.flush();
        } catch (IOException e) {
            socket = null;
            throw e;
        }
    }
	
    private String readLine() throws IOException {
        String line = reader.readLine();

        // log.debug("<< " + line);
        return line;
    }
	
    private Socket socket = null;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;
	
}
