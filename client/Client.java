import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    public static void main(String[] args) 
    {       
        try 
        {
            Socket clientSocket = new Socket("localhost",6789);  
            DataInputStream is = null;
            DataOutputStream os = null;

            while (true) 
            {
                os = new DataOutputStream(clientSocket.getOutputStream());
                System.out.print( "Enter the message (Close Server to stop server, Close Client to stop client): " );
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String keyboardInput = br.readLine();
                os.writeUTF( keyboardInput + "\n" );
                is = new DataInputStream(clientSocket.getInputStream());


                if (keyboardInput.equals("Close Server") || keyboardInput.equals("Close Client")) 
                {
                    break;
                }

                String[] splittedoutput = keyboardInput.split(" ");

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
                        os.write(contents);
                        System.out.println("Sending file ... "+(current*100)/fileLength+"% complete!");
                        //Thread.sleep(500);
                    }   
                
                    os.flush(); 
                }

                if (splittedoutput[0].equals("UDP"))
                {
                    if(splittedoutput[1].equals("Send"))
                    {

                    }

                    else
                    {
                        DatagramSocket serverSocket = new DatagramSocket(9876);
                        byte[] receiveData = new byte[1024];
                        byte[] sendData = new byte[1024];
                        while(true)
                           {
                              DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                              clientSocket.receive(receivePacket);
                              String sentence = new String( receivePacket.getData());
                              System.out.println("RECEIVED via UDP: " + sentence);
                              InetAddress IPAddress = receivePacket.getAddress();
                              int port = receivePacket.getPort();
                              String capitalizedSentence = sentence.toUpperCase();
                              sendData = capitalizedSentence.getBytes();
                              DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                              clientSocket.send(sendPacket);
                           }
                    }
                }
            
                String responseLine = is.readUTF();
                String[] splitted = responseLine.split(" ");
                //System.out.println(splitted[2]);

                if(splitted[0].equals("Send") && splitted[1].equals("File"))
                {
                    byte[] contents = new byte[10000];
                            
                    //Initialize the FileOutputStream to the output file's full path.
                    FileOutputStream fos = new FileOutputStream(splitted[2]);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    //FileInputStream fis = new FileInputStream(new InputStreamReader(clientSocket.getInputStream()));  
                    //No of bytes read in one read() call
                    InputStream fis = clientSocket.getInputStream();
                    int bytesRead = 0; 
                    
                    while((bytesRead=fis.read(contents))!=-1)
                    {
                        //System.out.println(contents);
                        bos.write(contents, 0, bytesRead);
                        //System.out.println(bos);
                        bos.flush();
                        break;
                    }
                    
                    bos.close();

                }

                else
                {
                    System.out.print(responseLine);
                }


            }

            os.close();
            is.close();
            clientSocket.close();   
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
