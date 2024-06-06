import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
public class Client implements Runnable{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    //private boolean done;

    @Override
    public void run()
    {
        try{

             client=new Socket("0.0.0.0",8080);
             out=new PrintWriter(client.getOutputStream(),true);
             in=new BufferedReader(new InputStreamReader(client.getInputStream()));

             InputHandler inhandler=new InputHandler();
             Thread t=new Thread(inhandler);
             t.start();

        }catch(IOException e)
        {
            shutdown();
        }
    }
    

    public void shutdown()
    {
       // done=true;

        try{
            in.close();
            out.close();
            if(!client.isClosed())
            {
                client.close();
            }
        }catch(IOException e){
            //ignore
        }
    }


    class InputHandler implements Runnable{

        @Override
        public void run()
        {
            try{

                BufferedReader inReader=new BufferedReader(new InputStreamReader(System.in));
                //broadcast
                String m=in.readLine();
                System.out.println(m);
                 
                //do you want to join the pool
                String message=in.readLine();
                System.out.println(message);
               
                String join=inReader.readLine();
                out.println(join);

                boolean g=false;

                while(!g){

                    String start=in.readLine();
                    System.out.println(start);

                    join=inReader.readLine();
                    

                    if(join.equals("yes"))
                    {
                        g=true;
                    }
                    out.println(join);
                }
               
                //guessing starts...
                boolean guessedCorrectly = false;
                while (!guessedCorrectly) {
                    int L = Integer.parseInt(in.readLine());
                    int R = Integer.parseInt(in.readLine());
                    System.out.print("Enter your guess (between " + L + " and " + R + "): ");
                    Random random=new Random();
                    int guess = random.nextInt(R-L+1)+L;
    
                    out.println(guess);
                    
                    //if broadcast ==true
                    if(join.equals("yes"))
                    {
                        String j=in.readLine();
                        System.out.println(j);
                    }

                    //recieves response about the guess
                    String response = in.readLine();
                    System.out.println(response);
    
                    if (response.startsWith("Congratulations")) {
                        guessedCorrectly = true;
                        String esc=in.readLine();
                        System.out.println(esc);
                    }
                }
                shutdown();
    
            }catch(IOException e){
                 
                shutdown();
            }
        }
    }
    
    public static void main(String[] args) {
        Client client=new Client();
        client.run();
    }
}