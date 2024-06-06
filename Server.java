import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Server implements Runnable{

    private ArrayList<ConnectionHandler> connections;
    private ArrayList<Integer> cid;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    private int correct=0;


    public Server()
    {
        connections=new ArrayList<>();
        cid=new ArrayList<>();
        done=false;
    }

    @Override
    public void run(){
        try{
            server=new ServerSocket(8080);
            pool=Executors.newCachedThreadPool();
            System.out.println("Server started. Waiting for prisoners...");

            int L = (int) (Math.random() * 100000);
            int R = L + (int) (Math.random() * 90001 + 10000);
            int X = L + (int) (Math.random() * (R - L + 1));
            System.out.println("L = " + L + ", R = " + R + ", X = " + X);
            int id=1;


            while(!done){

            Socket client=server.accept();
            ConnectionHandler  handler=new ConnectionHandler(client,L,R,X,id);
            id++;
            connections.add(handler);
            pool.execute(handler);

            }
        }catch(IOException e){
            shutdown();
        }
    }

    public void broadcast(String message)
    {
        for(ConnectionHandler ch: connections)
        {
            if(ch!=null)
            {
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown()
    {
        try{
        done=true;
        if(!server.isClosed()){
            server.close();
        }

        for(ConnectionHandler ch: connections)
        {
            ch.shutdown();
        }
       }catch(IOException e){
           //cannot handle
    }

    }

    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private int L, R, X, id;

        public ConnectionHandler(Socket client,int L,int R,int X,int id)
        {
            this.client=client;
            this.L=L;
            this.R=R;
            this.X=X;
            this.id=id;
        }

        @Override
        public  void run()
        {
            try{
                
                out=new PrintWriter(client.getOutputStream(),true);
                in=new BufferedReader(new InputStreamReader(client.getInputStream()));
                System.out.println("Prisoner "+id+" connected");
                broadcast("Prisoner "+id+" joined the jail");

                out.println("Do you want to join the pool? (yes/no): ");
                String message=in.readLine();
                message=message.toLowerCase();
                
                boolean join=false;

                if(message.equals("yes"))
                {
                    join=true;
                }
                
                //do you want to start guessing

                boolean g=false;
                while(!g){
                out.println("Do you want to start guessing? (yes/no): ");
                String m=in.readLine();
                m=m.toLowerCase();

                if(m.equals("yes"))
                {
                    g=true;
                }
            }
                
                boolean guessedCorrectly = false;
                while (!guessedCorrectly) {
                    out.println(L);
                    out.println(R);
                    int guess = Integer.parseInt(in.readLine());
                    System.out.println("Prisoner "+id+" guessed: "+guess);

                    if(join==true)
                    {
                        broadcast("Prisoner "+id+ " guessed: "+guess);
                    }

                    if (guess > X) {
                        out.println("Value too high");
                        R=guess-1;
                    } else if (guess < X) {
                        out.println("Value too low");
                        L=guess+1;
                    } else {
                        out.println("Congratulations! Prisoner "+id+" guessed the correct value");
                        System.out.println("Prisoner "+id+" escaped");
                        broadcast("Prisoner "+id+" escaped");
						cid.add(id);
                        correct++;
                        guessedCorrectly = true;
                    }
                }
                
				if(correct==connections.size()){
                    System.out.print("Order of escape of prisoners: ");
                for(int i=0;i<cid.size();i++)
				{
					if(cid.get(i)!=0)System.out.print("Prioner "+cid.get(i)+" ");
				}
                System.out.println();
            }
            shutdown();

            }catch(IOException e){
                   shutdown();
            }
        }

        public void sendMessage(String message)
        {
            out.println(message);
        }

        public void shutdown(){

            try{
                in.close();
                out.close();
                if(!client.isClosed())
                {
                    client.close();
                }
            }catch(IOException e)
            {
                //ignore
            }
        }
    }
        public static void main(String[] args) {
            
            Server server=new Server();
            server.run();
        }
    
}