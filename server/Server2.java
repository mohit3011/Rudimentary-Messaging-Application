import java.io.*;
import java.net.*;

@SuppressWarnings("serial")
public class Server2 extends Thread
{
    public static void main(String args[]) 
    {
	    Server2 server = new Server2(6789);
	    server.startServer();
    }


    ServerSocket echoserver = null;
    Socket clientsocket = null;
    int numConnections = 0;
    int port = 6789;
 
    public Server2(int port) 
    {
	    this.port = port;
    }

    public void stopServer()
    {
	    System.exit(0);
    }

    public void startServer()
    {
    // Try to open a server socket on the given port
    // Note that we can't choose a port less than 1024 if we are not
    // privileged users (root)
    
        try 
        {
	        echoserver = new ServerSocket(6789);
        }
        catch (IOException e)
        {
	        System.out.println(e);
        }   
    
    //System.out.println( "Server is started and is waiting for connections." );


    // Whenever a connection is received, start a new thread to process the connection
    // and wait for the next connection.
    
	    while (true) 
	    {
	        try 
	        {
		        clientsocket = echoserver.accept();
		        numConnections ++;
		        Server2Connection oneconnection = new Server2Connection(clientsocket, numConnections, this);
		        new Thread(oneconnection).start();
	        }   
	        catch (IOException e) 
	        {
		        System.out.println(e);
	        }
	    }
    }
}

class Server2Connection implements Runnable 
{
    BufferedReader is;
    Socket clientsocket;
    int id;
    Server2 server;
    DataInputStream din;
    DataOutputStream dout;
    BufferedReader br;


    public Server2Connection(Socket clientsocket, int id, Server2 server) 
    {
	    this.clientsocket = clientsocket;
	    this.id = id;
	    this.server = server;
	    System.out.println( "Connection " + id + " established with: " + clientsocket );
	    try 
	    {

            din=new DataInputStream(clientsocket.getInputStream());  
            dout=new DataOutputStream(clientsocket.getOutputStream());  
            br=new BufferedReader(new InputStreamReader(System.in));

	    } 
	    catch (IOException e) 
	    {
	        System.out.println(e);
	    }
	}

    public void run() 
    {
     	String line;
	    try 
	    {
        	boolean serverStop = false;

            while (true) 
            {
                String recieveinput=din.readUTF();
                String[] splittedinput = recieveinput.split(" ");

                if(splittedinput[0].equals("Close") && splittedinput[1].equals("Server"))
                {
                    serverStop = true;
                    break;
                }

                if (splittedinput[0].equals("Send") && splittedinput[1].equals("File"))
                {
                    //   System.out.println("Enter2");

                    byte[] contents = new byte[100];
                    long fileLength = din.readLong();
                            

                    //Initialize the FileOutputStream to the output file's full path.
                    FileOutputStream fos = new FileOutputStream(splittedinput[2]);
                    //BufferedOutputStream bos = new BufferedOutputStream(fos);
                    //FileInputStream fis = new FileInputStream(new InputStreamReader(clientSocket.getInputStream()));  
                    //No of bytes read in one read() call
                    //InputStream fis = clientsocket.getInputStream();
                    int readbyte = 0; 
                  //  System.out.println("Enter4");
                    
                    while(fileLength-readbyte>0)
                    {
                       // System.out.println("Enter3");

                    //    System.out.println("Enter5");

                        //System.out.println(contents);
                        readbyte=din.read(contents);
                        //System.out.println("readbyte: " + readbyte);
                        fos.write(contents, 0, readbyte);

                        if(readbyte<100)
                        {
                           break;
                        }
                        //System.out.println(bos);
                        //bos.flush();
                    }
                  //  System.out.println("Enter4");

                    
                    System.out.println("Recieved File");
                    fos.close();
                } 

                else if(splittedinput[0].equals("UDP"))
                {
                    DatagramSocket sock = null;
                    sock = new DatagramSocket(7777);
                     
                    byte[] buffer = new byte[65536];
                    DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                     
                    //echo("Server socket created. Waiting for incoming data...");
                     
                    while(true)
                    {
                        sock.receive(incoming);
                        byte[] data = incoming.getData();
                        String s = new String(data, 0, incoming.getLength());
                         
                        //echo the details of incoming data - client ip : client port - client message
                    //    echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);
                         
                        s = "OK : " + s;
                        DatagramPacket dp = new DatagramPacket(s.getBytes() , s.getBytes().length , incoming.getAddress() , incoming.getPort());
                        sock.send(dp);
                    }
                }

                else
                {
                    System.out.println(">> "+recieveinput);  
                } 

                //sending part -----------------------------------------------------------------------------------------------

                System.out.print( "Enter the message (Close Server to stop server, Close Client to stop client): " );
                String keyboardInput = br.readLine();  
                String[] splittedoutput = keyboardInput.split(" "); 
                dout.writeUTF( keyboardInput);

                
                if (splittedoutput[0].equals("Send") && splittedoutput[1].equals("File")) 
                {
                    File file = new File(splittedoutput[2]);
                    FileInputStream fis = new FileInputStream(file);
                    //BufferedInputStream bis = new BufferedInputStream(fis);
                    
                    //long int readbyte=0;
                    byte[] contents = new byte[100];
                    long fileLength = file.length(); 
                    int current = 0;
                     
                    long readbyte = 0;
                    long start = System.nanoTime();
                    while((current=fis.read(contents))>0)
                    { 

                        dout.write(contents,0, current);
                        if(fileLength-readbyte > 100)
                        {
                            readbyte = readbyte + 100;
                        }

                        else
                        {
                            readbyte = fileLength;
                        }
                        System.out.print("Sending file ... "+(readbyte*100)/fileLength+"% complete!\r");
                        try {
                            Thread.sleep(5);                 //1 milliseconds is one second.
                        } catch(InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    } 
                    System.out.print("\n");
                    System.out.println("File Sent");
                    fis.close();
                   // bis.close(); 
                }

                else if(splittedoutput[0].equals("UDP"))
                {
                    DatagramSocket sock = new DatagramSocket();
                                 
                    InetAddress host = InetAddress.getByName("localhost");
                     
                    while(true)
                    {
                        //take input and send the packet
                        byte[] b = keyboardInput.getBytes();
                        //System.out.println("Entermoh1");
                         
                        DatagramPacket  dp = new DatagramPacket(b , b.length , host , 7777);
                        sock.send(dp);
                         
                        //now receive reply
                        //buffer to receive incoming data
                        byte[] buffer = new byte[65536];
                        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                        sock.receive(reply);
                         
                        byte[] data = reply.getData();
                        String s = new String(data, 0, reply.getLength());
                         
                        //echo the details of incoming data - client ip : client port - client message
                        //echo(reply.getAddress().getHostAddress() + " : " + reply.getPort() + " - " + s);
                    }

                    //dout.flush();
                }
			}

	        if (serverStop)
	       	{ 
	       		server.stopServer();
	       	}
	    } 
	    catch (IOException e)
	    {
	        System.out.println(e);
	    }
  	}
}
