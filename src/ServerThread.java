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
                
                // Discover which message type
                switch (input.charAt(0)) {
                    case '0':
                        // Requested update
                        break;
                    case '1':
                        // Painted a pixel
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
