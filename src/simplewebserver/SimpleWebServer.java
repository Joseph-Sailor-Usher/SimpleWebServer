package simplewebserver;

/****************************************************************
SimpleWebServer.java
This toy web server is used to illustrate security vulnerabilities. This web server only supports extremely simple HTTP GET requests.
****************************************************************/

/*
 *  Update SimpleWebServer.java and SimpleWebClient.java to enable the client to upload a file. 
 *      User inputs PUT <fileToUpload> <DestinationPath> in the client program, 
 *          client reads the content of <fileToUpload> client side
 *          sends the command PUT <DestinationPath> and the file content to the server. 
 *      Server receives from the client and:
 *          server saves the file content to <DestinationPath> on the server
 *          logs all client requests into a log file. 
 public class SimpleWebClient {
    private static final String hostName = "localhost";
    private static final int PORT = 8080;

	public static void main(String[] args) throws IOException {
        try (
            Socket serverSocket = new Socket(hostName, PORT);
            PrintWriter out =
                new PrintWriter(serverSocket.getOutputStream(), true);
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(serverSocket.getInputStream()));
            BufferedReader stdIn =
                new BufferedReader(
                    new InputStreamReader(System.in))
        ) {
            String userInput;
            if ((userInput = stdIn.readLine()) != null) {
                // If user input begins with "PUT", then send the file to the server
                String[] input = userInput.split(" ");
                if (input[0].equals("PUT")) {
                    // Read the file content
                    String fileContent = "";
                    try {
                        BufferedReader fileReader = new BufferedReader(new FileReader(input[1]));
                        StringBuilder contentBuilder = new StringBuilder();
                        String line;
                        while ((line = fileReader.readLine()) != null) {
                            contentBuilder.append(line).append("\n");
                        }
                        fileContent = contentBuilder.toString();
                        fileReader.close();
                        
                        // Debug: Print the file content to the console to confirm it's read correctly
                        System.out.println("File content to send:");
                        System.out.println(fileContent);

                    } catch (FileNotFoundException e) {
                        System.out.println("File not found: " + input[1]);
                        System.exit(1);
                    }
                    // Send the file content to the server
                    out.println(userInput);
                    out.println(fileContent);
                }
                else {
                    out.println(userInput);
                    String response=in.readLine();
                    if (response!=null) {
                        System.out.println("Response from Server: ");
                        System.out.println(response);
                        while ((response=in.readLine())!=null) {
                            System.out.println(response);
                        }
                    }
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +  hostName);
            System.exit(1);
        } 
    }
}

 */

import java.io.*;
import java.net.*;
import java.util.*;

public class SimpleWebServer {
    /* Run the HTTP server on this TCP port. */
    private static final int PORT = 8080;

	/* Max Size for files to be served to a client */
	private static final int MAX_FILE_SIZE = 1024 * 1024 * 10;

    /* The socket used to process incoming connections from web clients */
    private static ServerSocket dServerSocket;

    public SimpleWebServer () throws Exception {
    	dServerSocket = new ServerSocket (PORT);
    }

    public void run() throws Exception {
    	while (true) {
    		/* wait for a connection from a client */
    		Socket s = dServerSocket.accept();
    		/* then process the client's request */
    		processRequest(s);
    	}
    }

    /* Reads the HTTP request from the client, and responds with the file the user requested or a HTTP error code. */
    public void processRequest(Socket s) throws Exception {
    	/* used to read data from the client */
    	BufferedReader br = new BufferedReader (new InputStreamReader (s.getInputStream()));

    	/* used to write data to the client */
    	OutputStreamWriter osw =  new OutputStreamWriter (s.getOutputStream());

    	/* read the HTTP request from the client */
    	String request = br.readLine();

    	String command = null;
    	String pathname = null;

    	/* parse the HTTP request */
    	StringTokenizer st = new StringTokenizer (request, " ");

		/* Try to read the first token */
		if (st.hasMoreElements()) {
			command = st.nextToken();
		} else {
			command = null;
		}
		/* If the request begins with "PUT" */
		if (command.equals("PUT")) {
			/* read the file name and path name from the request */
			String filename = null;
			if(st.hasMoreTokens()) {
				filename = st.nextToken();
			}
			if(st.hasMoreTokens()) {
				pathname = st.nextToken();
			}

			/* store the file content to the server */
			storeFile(br, osw, pathname);
			/* log all client requests into a log file */
			if(filename!=null && pathname!=null) {
				logEntry("log.txt", "PUT "+filename+" "+pathname+"\n");
			}
		}
    	else if (command.equals("GET")) {
			if(st.hasMoreTokens()) {
				pathname = st.nextToken();
			}
    		/* if the request is a GET try to respond with the file the user is requesting */
    		System.out.println("Path name: "+pathname);
    		serveFile (osw,pathname);
    	}
    	else {
    		/* if the request is a NOT a GET, return an error saying this server does not implement the requested command */
    		osw.write ("HTTP/1.0 501 Not Implemented\n\n");
    	}

    	/* close the connection to the client */
    	osw.close();
    }

	/* Serve the file to the client if the file is of a size less than MAX_FILE_SIZE */
    public void serveFile (OutputStreamWriter osw, String pathname) throws Exception {
    	FileReader fr=null;
    	int c=-1;
    	StringBuffer sb = new StringBuffer();

    	/* remove the initial slash at the beginning of the pathname in the request */
    	if (pathname.charAt(0)=='/')
    		pathname=pathname.substring(1);

    	/* if there was no filename specified by the client, serve the "index.html" file */
    	if (pathname.equals(""))
    		pathname="index.html";

    	/* try to open file specified by pathname */
    	try {
    		System.out.println("Path name: "+pathname);
    		fr = new FileReader (pathname);
    		c = fr.read();
    	}
    	catch (Exception e) {
    		/* if the file is not found,return the appropriate HTTP response code  */
    		osw.write ("HTTP/1.0 404 Not Found\n\n");
    		return;
    	}

		/* if the file is too large, return the appropriate HTTP response code */
		File file = new File(pathname);
		if (file.length() > MAX_FILE_SIZE) {
			osw.write ("HTTP/1.0 403 Forbidden\n\n");
			return;
		}
		
 	/* if the requested file can be successfully opened
 	   and read, then return an OK response code and
 	   send the contents of the file */
    	osw.write ("HTTP/1.0 200 OK\n\n");
    	while (c != -1) {
    		sb.append((char)c);
    		c = fr.read();
    	}
    	osw.write (sb.toString());
    }

	public void storeFile(BufferedReader br, OutputStreamWriter osw, String pathname) throws Exception {
		try (FileWriter fw = new FileWriter(pathname)) {
			String line;
			boolean headerEnded = false;
			StringBuilder contentReceived = new StringBuilder();
	
			// Read and discard all header lines up to and including the blank line.
			while ((line = br.readLine()) != null) {
				if (line.isEmpty()) {
					headerEnded = true;
					break;
				}
				contentReceived.append(line).append("\n");
			}
	
			// Print the content received for debugging purposes.
			// System.out.println("Received content:");
			// System.out.println(contentReceived.toString());
	
			// Confirm saving the file.
			System.out.println(pathname + " is saved!");

			// Write it to the file
			fw.write(contentReceived.toString());
		} catch (IOException e) {
			e.printStackTrace(); // Print the stack trace for debugging purposes.
		}
	}	
		
	public void logEntry(String filename, String record) throws Exception {
		FileWriter fw = new FileWriter(filename, true);
		fw.write((new Date()).toString()+" "+record);
		fw.close();
	}

    /* This method is called when the program is run from the command line. */
    public static void main (String argv[]) throws Exception {
    	/* Create a SimpleWebServer object, and run it */
    	SimpleWebServer sws = new SimpleWebServer();
    	sws.run();
    }
}
