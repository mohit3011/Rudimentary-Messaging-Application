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
	    System.exit(0);                // For Stopping the server
    }

    public void startServer()
    {
    
        try 
        {
	        echoserver = new ServerSocket(6789);
        }
        catch (IOException e)
        {
	        System.out.println(e);
        }   
    
    
	    while (true) 
	    {
	        try 
	        {                                              
		        clientsocket = echoserver.accept();       // Multiple Client accept due to threading
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
    DatagramSocket udpsocksend;
    DatagramSocket udpsockrecieve;


    public Server2Connection(Socket clientsocket, int id, Server2 server) 
    {
	    this.clientsocket = clientsocket;
	    this.id = id;
	    this.server = server;
	    System.out.println( "Connection " + id + " established with: " + clientsocket );
	    try 
	    {

            din=new DataInputStream(clientsocket.getInputStream());                 // For each new client this function is called
            dout=new DataOutputStream(clientsocket.getOutputStream());              // For TCP Data Output
            br=new BufferedReader(new InputStreamReader(System.in));
            udpsockrecieve = new DatagramSocket(7010);                  // For UDP recieving
            udpsocksend = new DatagramSocket();                         // For UDP Sending

	    } 
	    catch (IOException e) 
	    {
	        System.out.println(e);
	    }
	}

    public String bargenerate(long bar)
        {
            long i, j;
            String result = "[";
            for(i=0;i<bar;i++)
            {
                result = result+"=";
            }
            result = result+=">";
            for(j=bar;j<10;j++)
            {                                   // Code for generating the The progress bar string
                result = result+" ";
            }

            result = result + "]";
            return result;
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
                    serverStop = true;                          // If the command given is close server
                    break;
                }

                if (splittedinput[0].equals("Send") && splittedinput[1].equals("File"))
                {

                    byte[] contents = new byte[100];
                    long fileLength = din.readLong();                   // Try to open the file
                            

                    if(fileLength!=-1)
                    {
                        FileOutputStream fos = new FileOutputStream(splittedinput[2]);
                        int readbyte = 0;
                        long current = 0;                                                   // If the file name is valid then try copying
                        long bar; 
                        
                        while(fileLength-current>0)
                        {
                            readbyte=din.read(contents);
                            fos.write(contents, 0, readbyte);

                           if(readbyte<100)
                           {
                              current = fileLength; 
                           }

                           else
                           {
                               current = current + 100;                 // Current is the toal number of bytes transfered
                           }

                           bar = ((current*100)/fileLength)/10;             //Progress Bar
                           System.out.print("Recieving "+ splittedinput[2] + " " + bargenerate(bar) + " " + (current*100)/fileLength+"% complete!\r");

                        }

                        System.out.print("\n");
                        System.out.println("Recieved File");
                        fos.close();
                    }
                } 

                else if(splittedinput[0].equals("UDP"))
                {
                    byte[] contents = new byte[100];                // Recieving Over UDP
                    long fileLength = din.readLong();

                    if(fileLength!=-1)                              // If Valid File is recieved
                    {                   
                       // System.out.println("fileLength: " + fileLength);
                        FileOutputStream fos = new FileOutputStream(splittedinput[3]);
                        int readbyte = 0;
                        int current = 0;
                        int bar;
                        DatagramPacket dp = new DatagramPacket(contents, contents.length);

                        while(current<fileLength)
                        {
                          //  System.out.println ("Enter1");

                            udpsockrecieve.receive(dp);
                            contents=dp.getData();
                            readbyte=dp.getLength();        
                           // System.out.println("readbyte : " + readbyte);

                            current = current + readbyte;       // Total number of bytes recieved till now

                            bar = ((current*100)/(int)fileLength)/10;
                            System.out.print("Recieving "+ splittedinput[3] + " " + bargenerate(bar) + " " + (current*100)/fileLength+"% complete!\r");


                            if(readbyte<=0)
                            {
                                break;
                            }
                            fos.write(contents, 0, readbyte);
                            
                        }
                        System.out.print("\n");
                        System.out.println("Recieved File");
                        fos.close();
                    }
                }

                else
                {
                    System.out.println("Alice: "+recieveinput);  
                } 

                //sending part -----------------------------------------------------------------------------------------------

                System.out.print( ">> " );
                String keyboardInput = br.readLine();  
                String[] splittedoutput = keyboardInput.split(" "); 
                dout.writeUTF( keyboardInput);

                
                if (splittedoutput[0].equals("Send") && splittedoutput[1].equals("File")) 
                {
                    File file = new File(splittedoutput[2]);
                    //BufferedInputStream bis = new BufferedInputStream(fis);
                    if(!file.exists() && !file.isFile())
                    {
                        long fileLength = -1;
                        dout.writeLong(fileLength);                         // If the file doesn't exist then show error
                        System.out.println("The file does not exist");
                    }
                    //long int readbyte=0;
                    else
                    {
                        FileInputStream fis = new FileInputStream(file);
                        byte[] contents = new byte[100];
                        long fileLength = file.length(); 
                        dout.writeLong(fileLength);             // Get the file length

                        int current = 0;
                         
                        long readbyte = 0;
                        long bar;
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

                            bar = ((readbyte*100)/fileLength)/10;
                            System.out.print("Sending "+ splittedoutput[2] + " " + bargenerate(bar) + " " + (readbyte*100)/fileLength+"% complete!\r");
                            try {
                                Thread.sleep(5);                 //5 milliseconds is one second.
                            } catch(InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        } 
                        System.out.print("\n");
                        System.out.println("File Sent");
                        fis.close();
                    }
                   // bis.close();  
                }

                else if(splittedoutput[0].equals("UDP"))
                {
                    
                    File file = new File(splittedoutput[3]);            // File Transfer over UDP
                    if(!file.exists() && !file.isFile())
                    {
                        long fileLength = -1;
                        dout.writeLong(fileLength);
                        System.out.println("The file does not exist");
                    }
                    else
                    {
                        FileInputStream fis = new FileInputStream(file);                    
                        byte[] contents = new byte[100];
                        long fileLength = file.length();
                        dout.writeLong(fileLength); 
                        int current = 0;
                        long readbyte = 0;
                        long bar;
                        InetAddress host = InetAddress.getByName("localhost");

                        while((current=fis.read(contents))>0)
                        { 

                            DatagramPacket dp = new DatagramPacket(contents, contents.length, host, 7011);
                            udpsocksend.send(dp);
                            readbyte = readbyte + current;

                            bar = ((readbyte*100)/fileLength)/10;
                            System.out.print("Sending "+ splittedoutput[2] + " " + bargenerate(bar) + " " + (readbyte*100)/fileLength+"% complete!\r");

                            try {
                                Thread.sleep(5);                 //1 milliseconds is one second.
                            } catch(InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        } 
                        System.out.print("\n");
                        System.out.println("File Sent");
                        fis.close();
                    }
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
