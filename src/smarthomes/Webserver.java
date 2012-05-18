/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

//the real (http) serverclass
//it extends thread so the server is run in a different
//thread than the gui, that is to make it responsive.
//it's really just a macho coding thing.
public class Webserver extends Thread {

    private String ip = "";
    String value;

    //the constructor method
    //the parameters it takes is what port to bind to, the default tcp port
    //for a httpserver is port 80. the other parameter is a reference to
    //the gui, this is to pass messages to our nice interface
    public Webserver(int listen_port) {
        port = listen_port;
        ip = getIP();
        //this makes a new thread, as mentioned before,it's to keep gui in
        //one thread, server in another. You may argue that this is totally
        //unnecessary, but we are gonna have this on the web so it needs to
        //be a bit macho! Another thing is that real pro webservers handles
        //each request in a new thread. This server dosen't, it handles each
        //request one after another in the same thread. This can be a good
        //assignment!! To redo this code so that each request to the server
        //is handled in its own thread. The way it is now it blocks while
        //one client access the server, ex if it transferres a big file the
        //client have to wait real long before it gets any response.
        this.start();
    }
    
    private static String getIP() {
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void s(String s2) { //an alias to avoid typing so much!
        System.out.println(s2);
    }
    private int port; //port we are going to listen to

    //this is a overridden method from the Thread class we extended from
    public void run() {
        //we are now inside our own thread separated from the gui.
        ServerSocket serversocket = null;
        //Pay attention, this is where things starts to cook!
        try {
            //print/send message to the guiwindow
            s("Trying to bind to localhost on port " + Integer.toString(port) + "...");
            //make a ServerSocket and bind it to given port,
            serversocket = new ServerSocket(port);
        } catch (Exception e) { //catch any errors and print errors to gui
            s("\nFatal Error:" + e.getMessage());
            return;
        }

        //go in a infinite loop, wait for connections, process request, send response
        while (true) {
            s("\nReady, Waiting for requests...\n");
            try {
                //this call waits/blocks until someone connects to the port we
                //are listening to
                Socket connectionsocket = serversocket.accept();
                //figure out what ipaddress the client commes from, just for show!
                InetAddress client = connectionsocket.getInetAddress();
                //and print it to gui
                s(client.getHostName() + " connected to server.\n");
                //Read the http request from the client from the socket interface
                //into a buffer.
                BufferedReader input =
                        new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
                //Prepare a outputstream from us to the client,
                //this will be used sending back our response
                //(header + requested file) to the client.
                DataOutputStream output =
                        new DataOutputStream(connectionsocket.getOutputStream());

                //as the name suggest this method handles the http request, see further down.
                //abstraction rules
                http_handler(input, output);
            } catch (Exception e) { //catch any errors, and print them
                s("\nError:" + e.getMessage());
            }

        } //go back in loop, wait for next request
    }

//our implementation of the hypertext transfer protocol
//its very basic and stripped down
    private void http_handler(BufferedReader input, DataOutputStream output) {
        int method = 0; //1 get, 2 head, 0 not supported
        String http = new String(); //a bunch of strings to hold
        String path = new String(); //the various things, what http v, what path,
        String file = new String(); //what file
        String user_agent = new String(); //what user_agent
        try {
            //This is the two types of request we can handle
            //GET /index.html HTTP/1.0
            //HEAD /index.html HTTP/1.0
            String tmp = input.readLine(); //read from the stream
            String tmp2 = new String(tmp);
            String line = input.readLine();
            String header = "";
            while (line != null && !line.equals("")) {
                header += line;
                line = input.readLine();
            }
            if(header.contains("Content-Length")){
                int offset = header.indexOf("Content-Length: ") + "Content-Length: ".length();
                int cLength = Integer.parseInt(header.substring(offset, header.length()));
                char[] buffer = new char[cLength];
                input.read(buffer);
                value = new String(buffer);
            }
            tmp.toUpperCase(); //convert it to uppercase

            if (tmp.startsWith("GET")) { //compare it is it GET
                method = 1;
            } //if we set it to method 1
            if (tmp.startsWith("OPTIONS")) { //same here is it OPTIONS
                method = 2;
            } //set method to 2
            if (tmp.startsWith("POST")) { //same here is it POST
                method = 3;
            } //set method to 3

            if (method == 0) { // not supported
                try {
                    output.writeBytes(construct_http_header(501, 0));
                    output.close();
                    return;
                } catch (Exception e3) { //if some error happened catch it
                    s("error:" + e3.getMessage());
                } //and display error
            }
            //}

            //tmp contains "GET /index.html HTTP/1.0 ......."
            //find first space
            //find next space
            //copy whats between minus slash, then you get "index.html"
            //it's a bit of dirty code, but bear with me...

            int start = 0;
            int end = 0;
            for (int a = 0; a < tmp2.length(); a++) {
                if (tmp2.charAt(a) == ' ' && start != 0) {
                    end = a;
                    break;
                }
                if (tmp2.charAt(a) == ' ' && start == 0) {
                    start = a;
                }
            }
            path = tmp2.substring(start + 1, end); //fill in the path
        } catch (Exception e) {
            s("error" + e.getMessage());
        } //catch any exception

        try {
            //send response header
            output.writeBytes(construct_http_header(200, 5));

            if (method == 1) { //1 is GET 2 is head and skips the body
                //Response with data from kinect (persoon aanwezig, positie, ...)
                //output.writeBytes(response);
            }
            if (method == 2) {
                String msg = "";
                //Description of the sensor sent back (OPTIONS)
                if (path.equals("/")) {
                    msg = "<link rel=\"description\" type=\"text/n3\" href=\"/service\">\n<link rel=\"goal\" type=\"text/n3\" href=\"/serviceGoal\">";
                }
                if (path.equals("/service")) {
                    msg = "@prefix sensor: <http://example.org/sensor/>.\n";
                    msg += "@prefix ex: <http://example.org/example/>. \n";
                    msg += "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. \n";
                    msg += "@prefix environment: <http://example.org/environment/>. \n";
                    msg += "@prefix http: <http://www.w3.org/2011/http#>. \n";
                    msg += "@prefix tmpl: <http://purl.org/restdesc/http-template#>. \n";
                    msg += "{ \n";
                    msg += "?sensor a sensor:Kinect. \n";
                    msg += "} \n";
                    msg += "=> \n";
                    msg += "{ \n";
                    msg += "_:request http:methodName \"POST\"; \n";
                    msg += "tmpl:requestURI (?sensor \"/lightValue\"); \n";
                    msg += "http:body ?environmentLight;\n";
                    msg += "http:resp [ http:body ?lightingValue]. \n";
                    msg += "?environmentLight a environment:lightingCondition.\n";
                    msg += "?sensor sensor:lightingCondition ?lightingValue. \n";
                    msg += "?lightingValue a xsd:String. \n";
                    msg += "}. \n";
                    msg += "<http://" + ip + "/#sensor> a sensor:Kinect.\n";
                }
                if (path.equals("/serviceGoal")) {
                    msg = "@prefix sensor: <http://example.org/sensor/>. \n";
                    msg += "@prefix environment: <http://example.org/environment/>. \n";
                    msg += "{ \n";
                    msg += "<http://" + ip + "/#sensor> sensor:lightingCondition ?value.  \n";
                    msg += "} \n";
                    msg += "=> \n";
                    msg += "{ \n";
                    msg += "<http://" + ip + "/#sensor> sensor:lightingCondition ?value.  \n";
                    msg += "}. \n";
                }
                output.writeBytes(msg);
            }
            if (method == 3) {
                //Add something to sensor (LightSensor data)
                if (path.equals("/lightValue")) {
                    //get Light Value, send to kinect
                    String lightValue = value.substring(value.indexOf("=") + 1, value.length()).trim();
                    EnvironmentVars.getInstance().setLightValue(lightValue);
                    output.writeBytes("OK");
                }
                if (path.equals("/personPresent")) {
                    EnvironmentVars.getInstance().setPersonPresent(true);
                    output.writeBytes("OK");
                }
            }
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //this method makes the HTTP header for the response
    //the headers job is to tell the browser the result of the request
    //among if it was successful or not.
    private String construct_http_header(int return_code, int file_type) {
        String s = "HTTP/1.1 ";
        //you probably have seen these if you have been surfing the web a while
        switch (return_code) {
            case 200:
                s = s + "200 OK";
                break;
            case 400:
                s = s + "400 Bad Request";
                break;
            case 403:
                s = s + "403 Forbidden";
                break;
            case 404:
                s = s + "404 Not Found";
                break;
            case 500:
                s = s + "500 Internal Server Error";
                break;
            case 501:
                s = s + "501 Not Implemented";
                break;
        }

        s = s + "\r\n"; //other header fields,
        s = s + "Connection: close\r\n"; //we can't handle persistent connections
        s = s + "Server: SimpleHTTPtutorial v0\r\n"; //server name

        //Construct the right Content-Type for the header.
        //This is so the browser knows what to do with the
        //file, you may know the browser dosen't look on the file
        //extension, it is the servers job to let the browser know
        //what kind of file is being transmitted. You may have experienced
        //if the server is miss configured it may result in
        //pictures displayed as text!
        switch (file_type) {
            //plenty of types for you to fill in
            case 0:
                break;
            case 1:
                s = s + "Content-Type: image/jpeg\r\n";
                break;
            case 2:
                s = s + "Content-Type: image/gif\r\n";
            case 3:
                s = s + "Content-Type: application/x-zip-compressed\r\n";
            default:
                s = s + "Content-Type: text/html\r\n";
                break;
        }

        ////so on and so on......
        s = s + "\r\n"; //this marks the end of the httpheader
        //and the start of the body
        //ok return our newly created header!
        return s;
    }
}
