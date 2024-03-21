package simplewebclient;

import java.io.*;
import java.net.*;

/*
 *  Update SimpleWebServer.java and SimpleWebClient.java to enable the client to upload a file. 
 *      User inputs PUT <fileToUpload> <DestinationPath> in the client program, 
 *          client reads the content of <fileToUpload> client side
 *          sends the command PUT <DestinationPath> and the file content to the server. 
 *      Server receives from the client and:
 *          server saves the file content to <DestinationPath> on the server
 *          logs all client requests into a log file. 
 */

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
                        // System.out.println("File content to send:");
                        // System.out.println(fileContent);

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
