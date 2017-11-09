import java.net.*;
import java.io.*;

///
/// This class is instantiated for each client
/// that connects to the server. It is responsible
/// for handling all network messages.
///
public class ServerThread implements Runnable {
    private Socket socket;
    
    public ServerThread(Socket s) {
        this.socket = s;
    }
    
    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while (true) {
                // Read incoming message
                String input = in.readUTF();
                
                // Discover which message type we received
                switch (input.charAt(0)) {
                    case '0':
                        // Message type 0: client has requested a global
                        // canvas update. We must send all pixels' RGB values.
                        String msg = "0";
                        
                        for (int i = 0; i < (Server.CANVAS_SIZE * Server.CANVAS_SIZE); ++i) {
                            int r = Server.serverBlackboard.getPixel(i % Server.CANVAS_SIZE, Math.round(i / Server.CANVAS_SIZE), 0);
                            int g = Server.serverBlackboard.getPixel(i % Server.CANVAS_SIZE, Math.round(i / Server.CANVAS_SIZE), 1);
                            int b = Server.serverBlackboard.getPixel(i % Server.CANVAS_SIZE, Math.round(i / Server.CANVAS_SIZE), 2);
                            msg += "_" + r + "_" + g + "_" + b;
                        }
                        
                        out.writeUTF(msg);
                        break;
                    case '1':
                        // It's a pixel painting message - must
                        // forward it to all users so they see
                        // the newly painted pixel
                        Server.sendGlobalUTF(input);
                        
                        // Paint pixel in global server blackboard
                        String[] args = input.split("_");
                        Server.serverBlackboard.setPixel(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                        break;
                    case '2':
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR (IN SERVERTHREAD): " + e.toString());
        }
    }
}
