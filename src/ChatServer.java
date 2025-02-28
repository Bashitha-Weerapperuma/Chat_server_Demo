
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

public class ChatServer {

   
    private static final int PORT = 9001;

   
    private static HashSet<String> names = new HashSet<String>();

   
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    
    private static String check; 
    
    private static HashSet<String> clientsName = new HashSet<String>(); 
    private static HashMap<String, PrintWriter> namesWithWriters = new HashMap<>();

  
   
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
            	Socket socket  = listener.accept();
                Thread handlerThread = new Thread(new Handler(socket));
                handlerThread.start();
            }
        } finally {
            listener.close();
        }
    }

   
    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

       
        public Handler(Socket socket) {
            this.socket = socket;
        }

       
        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                for(String element:names) {
            		out.println(element);
            		writers.add(out);
            
            	}
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    
                    // TODO: Add code to ensure the thread safety of the
                    // the shared variable 'names'
                    synchronized (names) {
						
					
                    if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                  
                    
                 }

                // Now that a successful name has been chosen, add the
               
                out.println("NAMEACCEPTED");
                writers.add(out);
                namesWithWriters.put(name, out);
                System.out.println("New client Conected");
                
                // TODO: You may have to add some code here to broadcast all clients the new
         
                for (PrintWriter clients: writers ) {
                	 clients.println(name);
                	
                	}
                
                // Accept messages from this client and broadcast them.
             
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    
               
                    out.println("CHECK");
                    check = in.readLine();
                   
                    if(check.equals("true")) {
                    
                  
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + ": " + input);
                    }
                    }else {
                    	out.println("CLIENTS");
                    	clientsName.add(in.readLine());
                    	
                    	for(String key : namesWithWriters.keySet()) {
                    		for (String Cname:clientsName) {
                    			if(key.equals(Cname)) {
                    				HashSet<PrintWriter> Client = new HashSet<PrintWriter>();
                    				Client.add(namesWithWriters.get(key));
                    				for (PrintWriter writer : Client) {
                                        writer.println("MESSAGE " + name + ": " + input);
                                        out.println("MESSAGE " + "\t\t"+name + ": " + input);
                                    }
                    				
                    				clientsName.clear();
                    				
                    			}
                    		}
                    	}
                    	
                    	
                    }
                }
            }
            catch (java.net.SocketException e) {
            	System.out.println("Seams one client left");
            }
            catch (IOException e) {
                System.out.println(e);
            } finally {
              
                if (name != null) {
                    names.remove(name);
                    namesWithWriters.remove(name);
                }
                for (String key: namesWithWriters.keySet()) {
                	if(!key.equals(name)) {
                	namesWithWriters.get(key).println("REMOVE"+name);
                	}
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}