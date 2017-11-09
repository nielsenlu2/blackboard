import java.util.concurrent.*;
import java.util.ArrayList;
import java.net.*;
import java.io.*;

///
/// This class listens for incoming client connections
/// and creates a thread for each one. It also holds the
/// primary blackboard instance and global variables.
///
public class Server {
    // Blackboard and client sockets
    public static Blackboard serverBlackboard;
    private static ArrayList<Socket> sockets = new ArrayList();
    
    // Global constants
    public static final int PIXEL_SIZE = 8;
    public static final int CANVAS_SIZE = 64;
    
    // Listen for incoming connections
    public static void main(String[] args) {
        // Instanciate blackboard
        serverBlackboard = new Blackboard();
        
        // Thread pool to handle multiple clients
        ExecutorService e = Executors.newCachedThreadPool();
        System.out.println("INFO: Starting server...");

        try {
            ServerSocket server = new ServerSocket(27888);
            System.out.println("INFO: Server started.");

            while (true) {
                Socket socket = server.accept();
                sockets.add(socket);
                e.execute(new ServerThread(socket));
                System.out.println("INFO: New incoming connection");
            }
        } catch (Exception ex) {
            System.out.println("ERROR: Failed to start server: " + ex.toString());
        }
    }
    
    // Send network message to all clients
    public static void sendGlobalUTF(String msg) {
        try {
            for (Socket s : sockets) {
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                out.writeUTF(msg);
            }
        } catch (Exception e) {
            System.out.println("WARNING: Tried sending message to closed socket. Client is probably disconnected.");
        }
    }
}
