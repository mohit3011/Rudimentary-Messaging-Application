import java.io.*;
import java.net.*;

public class Server2 {
    public static void main(String args[]) {
    int port = 6789;
    Server2 server = new Server2( port );
    server.startServer();
    }

    // declare a server socket and a client socket for the server;
    // declare the number of connections

    ServerSocket echoServer = null;
    Socket clientSocket = null;
    int numConnections = 0;
    int port;
    
    public Server2( int port ) {
    this.port = port;
    }

    public void stopServer() {
    System.exit(0);
    }

    public void startServer() {
    // Try to open a server socket on the given port
    // Note that we can't choose a port less than 1024 if we are not
    // privileged users (root)
    
        try {
        echoServer = new ServerSocket(port);
        }
        catch (IOException e) {
        System.out.println(e);
        }   
    
    System.out.println( "Server is started and is waiting for connections." );


    // Whenever a connection is received, start a new thread to process the connection
    // and wait for the next connection.
    
    while ( true ) {
        try {
        clientSocket = echoServer.accept();
        numConnections ++;
        Server2Connection oneconnection = new Server2Connection(clientSocket, numConnections, this);
        new Thread(oneconnection).start();
        }   
        catch (IOException e) {
        System.out.println(e);
        }
    }
    }
}

class Server2Connection implements Runnable {
    BufferedReader is;
    DataOutputStream os = null;
    Socket clientSocket;
    int id;
    Server2 server;


    public Server2Connection(Socket clientSocket, int id, Server2 server) {
    this.clientSocket = clientSocket;
    this.id = id;
    this.server = server;
    System.out.println( "Connection " + id + " established with: " + clientSocket );
    try {
        is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        os = new DataOutputStream(clientSocket.getOutputStream());

    } catch (IOException e) {
        System.out.println(e);
    }
    }

    public void run() {
        String line;
    try {
        boolean serverStop = false;

            while (true) {
                line = is.readLine();
        System.out.println(line);
        
        if (line.equals("Close Server")) 
        {
            serverStop = true;
            break;
        }

        if (line.equals("Close Client"))
        {
        	System.out.println( "Connection " + id + " closed." );
            is.close();
            os.close();
            clientSocket.close();
	    }

        System.out.print( "Enter the message (Close Server to stop server, Close Client to stop client): " );
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String keyboardInput = br.readLine();
        os.writeBytes( keyboardInput + "\n" );

        String[] splitted = keyboardInput.split(" ");
        if (splitted[0].equals("Send") && splitted[1].equals("File")) 
        {
     		
        }




	}

        if ( serverStop )
       	{ 
       		server.stopServer();
       	}
    } 
    catch (IOException e) {
        System.out.println(e);
    }
    }
}
