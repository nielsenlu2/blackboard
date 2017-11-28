import java.util.concurrent.*;
import java.util.concurrent.locks.*;
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
    public static final int TOOLBAR_HEIGHT = 64;
    public static int WINDOW_WIDTH = 512;
    public static int WINDOW_HEIGHT = 512 + TOOLBAR_HEIGHT;
    
    // Listen for incoming connections
    public static void main(String[] args) {
        // Instanciate blackboard
        serverBlackboard = new Blackboard(true);
        loadBlackboard();

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
    
    // Save & load blackboard from file
    public static void saveBlackboard() {
        try {
            // Open file for writing
            FileWriter fileWriter = new FileWriter("blackboard.save");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Request info from server
            Socket tempSocket = new Socket("localhost", 27888);
            DataInputStream in = new DataInputStream(tempSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(tempSocket.getOutputStream());
            out.writeUTF("0_");
            
            // Write to file
            bufferedWriter.write(in.readUTF());

            // Release file
            bufferedWriter.close();
        } catch(IOException e) {
            System.out.println("ERROR: Could not write blackboard to file" + '\n' + e.toString());
        }
    }
    
    public static void loadBlackboard() {
        String input = "";
        
        try {
            // Read file
            FileReader fileReader = new FileReader("blackboard.save");
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            input = bufferedReader.readLine();

            // Always close files.
            bufferedReader.close();         
        }
        catch(Exception e) {
            System.out.println("WARNING: Could not load save. Starting blank canvas. \nReason: " + e.toString());
            return;
        }
        
        // Transfer from file into blackboard
        String[] msg = input.split("_");
                        
        for (int i = 0; i < (Server.CANVAS_SIZE * Server.CANVAS_SIZE); ++i) {
            int x = i % Server.CANVAS_SIZE;
            int y = Math.round(i / Server.CANVAS_SIZE);

            int red = Integer.parseInt(msg[ (i*3)+1 ]);
            int green = Integer.parseInt(msg[ (i*3)+2 ]);
            int blue = Integer.parseInt(msg[ (i*3)+3 ]);
            serverBlackboard.setPixel(x, y, red, green, blue);
        }
    }
}
