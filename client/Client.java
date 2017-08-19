import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    public static void main(String[] args) 
    {       
        try 
        {
            Socket clientsocket=new Socket("localhost",6789);  
            DataInputStream din=new DataInputStream(clientsocket.getInputStream());  
            DataOutputStream dout=new DataOutputStream(clientsocket.getOutputStream());  
            BufferedReader br=new BufferedReader(new InputStreamReader(System.in));  
      
            while(true)
            {
                System.out.print( "Enter the message (Close Server to stop server, Close Client to stop client): " );
                String keyboardInput = br.readLine();  
                String[] splittedoutput = keyboardInput.split(" ");
                dout.writeUTF(keyboardInput);

                
                if (keyboardInput.equals("Close Server") || keyboardInput.equals("Close Client")) 
                {
                    break;
                }

                if (splittedoutput[0].equals("Send") && splittedoutput[1].equals("File")) 
                {
                    File file = new File(splittedoutput[2]);
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    
                    byte[] contents;
                    long fileLength = file.length(); 
                    long current = 0;
                     
                    long start = System.nanoTime();
                    while(current!=fileLength)
                    { 
                        int size = 10000;
                        if(fileLength - current >= size)
                            current += size;    
                        else
                        { 
                            size = (int)(fileLength - current); 
                            current = fileLength;
                        } 
                        contents = new byte[size]; 
                        bis.read(contents, 0, size); 
                        dout.write(contents);
                        System.out.println("Sending file ... "+(current*100)/fileLength+"% complete!");
                        //Thread.sleep(500);
                    } 
                
                    dout.flush(); 
                }

                else if(splittedoutput[0].equals("UDP"))
                {
                    DatagramSocket sock = new DatagramSocket();
                                 
                    InetAddress host = InetAddress.getByName("localhost");
                     
                    while(true)
                    {
                        //take input and send the packet
                        byte[] b = keyboardInput.getBytes();
                        System.out.println("Enter1");
                         
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
                }


                // Recieving part of the client--------------------------------------------------------------------------- 


                String recieveinput=din.readUTF();
                String[] splittedinput = recieveinput.split(" ");

               // System.out.println("Enter1");
                if (splittedinput[0].equals("Send") && splittedinput[1].equals("File"))
                {   
                 //   System.out.println("Enter2");

                    byte[] contents = new byte[10000];
                            
                  //  System.out.println("Enter3");

                    //Initialize the FileOutputStream to the output file's full path.
                    FileOutputStream fos = new FileOutputStream(splittedinput[2]);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    //FileInputStream fis = new FileInputStream(new InputStreamReader(clientSocket.getInputStream()));  
                    //No of bytes read in one read() call
                    InputStream fis = clientsocket.getInputStream();
                    int bytesRead = 0; 
                  //  System.out.println("Enter4");
                    
                    while((bytesRead=fis.read(contents))!=-1)
                    {
                    //    System.out.println("Enter5");

                        //System.out.println(contents);
                        bos.write(contents, 0, bytesRead);
                        //System.out.println(bos);
                        bos.flush();
                        break;
                    }
                    
                    bos.close();
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


            }  

            dout.close();
            din.close();
            clientsocket.close();   
        } 
        catch (UnknownHostException e)
        {
            System.err.println("Trying to connect to unknown host: " + e);
        } 
        catch (IOException e) 
        {
            System.err.println("IOException:  " + e);
        }
    }

}

/*if (clientSocket == null || os == null || is == null) {
        System.err.println( "Error: Either Output or Input Stream is null" );
        return;
    }*/
