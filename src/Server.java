import java.util.concurrent.*;
import java.util.ArrayList;
import java.net.*;
import java.io.*;

/**
 *
 * @author aluno
 */
public class Server {
    public static Blackboard serverBlackboard;
    private static ArrayList<Socket> sockets = new ArrayList();
    
    public static void main(String[] args) {
        // Thread pool to handle multiple clients
        ExecutorService e = Executors.newCachedThreadPool();
        System.out.println("Starting server...");

        try {
            ServerSocket server = new ServerSocket(27888);
            System.out.println("Server started");

            while (true) {
                Socket socket = server.accept();
                sockets.add(socket);
                e.execute(new ServerThread(socket));
                System.out.println("New incoming connection");
            }
        } catch (Exception ex) {
            System.out.println("Failed to start server: " + ex.toString());
        }
    }
    
    public static void sendGlobalUTF(String msg) {
        // Send message to all clients
        try {
            for (Socket s : sockets) {
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                out.writeUTF(msg);
            }
        } catch (Exception e) {
            //
        }
            
    }
}
