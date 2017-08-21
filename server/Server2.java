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

            din=new DataInputStream(clientsocket.getInputStream());  
            dout=new DataOutputStream(clientsocket.getOutputStream());  
            br=new BufferedReader(new InputStreamReader(System.in));
            udpsockrecieve = new DatagramSocket(7010);
            udpsocksend = new DatagramSocket();

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
            {
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
                    serverStop = true;
                    break;
                }

                if (splittedinput[0].equals("Send") && splittedinput[1].equals("File"))
                {

                    byte[] contents = new byte[100];
                    long fileLength = din.readLong();
                            

                    FileOutputStream fos = new FileOutputStream(splittedinput[2]);
                    int readbyte = 0;
                    long current = 0;
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
                           current = current + 100;
                       }

                       bar = ((current*100)/fileLength)/10;
                       System.out.print("Recieving "+ splittedinput[2] + " " + bargenerate(bar) + " " + (current*100)/fileLength+"% complete!\r");

                    }

                    System.out.print("\n");
                    System.out.println("Recieved File");
                    fos.close();
                } 

                else if(splittedinput[0].equals("UDP"))
                {
                    byte[] contents = new byte[100];
                    long fileLength = din.readLong();
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

                        current = current + readbyte;

                        bar = ((current*100)/(int)fileLength)/10;
                        System.out.print("Recieving "+ splittedinput[3] + " " + bargenerate(bar) + " " + (current*100)/fileLength+"% complete!\r");


                        if(readbyte<=0)
                        {
                            break;
                        }
                        fos.write(contents, 0, readbyte);
                        
                    }

                    System.out.println("Recieved File");
                    fos.close();
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
                    FileInputStream fis = new FileInputStream(file);
                    //BufferedInputStream bis = new BufferedInputStream(fis);
                    
                    //long int readbyte=0;
                    byte[] contents = new byte[100];
                    long fileLength = file.length(); 
                    dout.writeLong(fileLength);

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
                    
                    File file = new File(splittedoutput[3]);
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
