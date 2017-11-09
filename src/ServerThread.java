import java.net.*;
import java.io.*;

public class ServerThread implements Runnable {
    private Socket socket;
    
    public ServerThread(Socket s) {
        this.socket = s;
    }
    
    public void run() {
        //
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while (true) {
                String input = in.readUTF();
                
                System.out.println("Incoming message: " + input);
                
                // Discover which message type
                switch (input.charAt(0)) {
                    case '0':
                        // Requested update
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
                        Server.sendGlobalUTF(input);
                        
                        // Painted a pixel
                        String[] args = input.split("_");
                        System.out.println("Trying to paint pixel on server");
                        System.out.println("X: " + args[1] + "\nY: " + args[2]);
                        System.out.println("RGB: " + args[3] + args[4] + args[5]);
                        //Server.serverBlackboard.setPixel(0, 0, 0, 255, 0);
                        Server.serverBlackboard.setPixel(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                        break;
                    case '2':
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Server error: " + e.toString());
        }
   }
}
