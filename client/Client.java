import java.io.*;
import java.net.*;
import java.util.*;

@SuppressWarnings("serial")
public class Client extends Thread {
    public static String bargenerate(long bar)
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
    public static void main(String[] args) 
    {
        try 
        {
            Socket clientsocket=new Socket("localhost",6789);  
            DataInputStream din=new DataInputStream(clientsocket.getInputStream());  
            DataOutputStream dout=new DataOutputStream(clientsocket.getOutputStream());  
            BufferedReader br=new BufferedReader(new InputStreamReader(System.in)); 
            DatagramSocket udpsockrecieve = new DatagramSocket(7011);
            DatagramSocket udpsocksend = new DatagramSocket(); 
      
            while(true)
            {
                System.out.print( ">> " );
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
                    //BufferedInputStream bis = new BufferedInputStream(fis);
                    if(!file.exists() && !file.isFile())
                    {
                        long fileLength = -1;
                        dout.writeLong(fileLength);
                        System.out.println("The file does not exist");
                    }
                    //long int readbyte=0;
                    else
                    {
                        FileInputStream fis = new FileInputStream(file);
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
                    } 
                }

                else if(splittedoutput[0].equals("UDP"))
                {
                    File file = new File(splittedoutput[3]);
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

                            DatagramPacket dp = new DatagramPacket(contents, contents.length, host, 7010);
                            udpsocksend.send(dp);
                            readbyte = readbyte + current;
                            //System.out.println("readbyte : " + readbyte);
                            
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


                // Recieving part of the client--------------------------------------------------------------------------- 


                String recieveinput=din.readUTF();
                String[] splittedinput = recieveinput.split(" ");

               // System.out.println("Enter1");
                if (splittedinput[0].equals("Send") && splittedinput[1].equals("File"))
                {   

                    byte[] contents = new byte[100];
                    long fileLength = din.readLong();
                            

                    if(fileLength!=-1)
                    {
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
                } 

                else if(splittedinput[0].equals("UDP"))
                {
                    byte[] contents = new byte[100];
                    long fileLength = din.readLong();

                    if(fileLength!=-1)
                    {
                    //System.out.println("fileLength: " + fileLength);
                        FileOutputStream fos = new FileOutputStream(splittedinput[3]);
                        int readbyte = 0;
                        int current = 0;
                        int bar;
                        DatagramPacket dp = new DatagramPacket(contents, contents.length);

                        while(current<fileLength)
                        {
                            udpsockrecieve.receive(dp);
                            contents=dp.getData();
                            readbyte=dp.getLength();

                            current = current + readbyte;

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
                    System.out.println("Bob: "+recieveinput);  
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
