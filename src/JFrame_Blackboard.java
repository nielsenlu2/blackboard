import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.net.*;
import java.io.*;

class Surface extends JPanel implements ActionListener {
    private final int PIXEL_SIZE = 8;
    private final int CANVAS_SIZE = 32;
    
    private final int DELAY = 150;
    private Timer timer;

    Blackboard blackboard = JFrame_Blackboard.blackboard;
    
    public Surface() {
        initTimer();
        ClientThread clientThread = new ClientThread();
        clientThread.setBlackboard(blackboard);
        
        //clientThread.run();
    }

    private void initTimer() {
        timer = new Timer(DELAY, this);
        timer.start();
    }
    
    public Timer getTimer() {
        return timer;
    }

    private void Draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        for (int i = 0; i < CANVAS_SIZE; ++i) {
            for (int j = 0; j < CANVAS_SIZE; ++j) {
                // Retrieve correct color
                g2d.setPaint(new Color(blackboard.getPixel(i, j, 0), blackboard.getPixel(i, j, 1), blackboard.getPixel(i, j, 2), 255));
                
                // Draw pixel on screen
                int x = i * PIXEL_SIZE;
                int y = j * PIXEL_SIZE;
                g2d.fillRect(x, y, x + PIXEL_SIZE, y + PIXEL_SIZE);
            }
        }
        
        /*int w = getWidth();
        int h = getHeight();

        Random r = new Random();

        for (int i = 0; i < 2000; i++) {
            int x = Math.abs(r.nextInt()) % w;
            int y = Math.abs(r.nextInt()) % h;
            g2d.drawLine(x, y, x, y);
        }*/
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Draw(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
    
    public void paintPixel(int x, int y, int r, int g, int b) {
        // Paint pixel locally
        blackboard.setPixel(x / PIXEL_SIZE, y / PIXEL_SIZE, r, g, b);
        
        // Ask server to paint same pixel
        try {
            JFrame_Main.out.writeUTF("1_" + (x / PIXEL_SIZE) + "_" + (y / PIXEL_SIZE) + "_" + r + "_" + g + "_" + b);
        } catch (Exception e) {
            // TODO - Connection error handling
        }
        
        try {
            JFrame_Main.out.writeUTF("0_");
        } catch (Exception e) {
            // TODO
        }
    }
}

class ClientThread implements Runnable {
    private Blackboard blackboard;
    
    public void setBlackboard(Blackboard blackboard) {
        this.blackboard = blackboard;
    }
    
    public void run() {
        System.out.println("Starting client thread");
        
        while (true) {
            try {
                String input = JFrame_Main.in.readUTF();
                System.out.println("Message to client: " + input);
                
                switch (input.charAt(0)) {
                    case '0':
                        String[] msg = input.split("_");
                        for (int i = 0; i < (Server.CANVAS_SIZE * Server.CANVAS_SIZE); ++i) {
                            int x = i % Server.CANVAS_SIZE;
                            int y = Math.round(i / Server.CANVAS_SIZE);

                            int red = Integer.parseInt(msg[ (i*3)+1 ]);
                            int green = Integer.parseInt(msg[ (i*3)+2 ]);
                            int blue = Integer.parseInt(msg[ (i*3)+3 ]);
                            blackboard.setPixel(x, y, red, green, blue);
                        }
                        break;
                        
                    case '1':
                        // Painted a pixel
                        String[] args = input.split("_");
                        //System.out.println("Trying to paint pixel on server");
                        //System.out.println("X: " + args[1] + "\nY: " + args[2]);
                        //System.out.println("RGB: " + args[3] + args[4] + args[5]);
                        blackboard.setPixel(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                        break;
                }
            } catch (Exception e) {
                // TODO: Handle connetion error
                break;
            }
        }
    }
}

public class JFrame_Blackboard extends JFrame {
    public static Blackboard blackboard = new Blackboard();
    
    public JFrame_Blackboard() {
        initUI();
        
        ClientThread clientThread = new ClientThread();
        clientThread.setBlackboard(blackboard);
        //clientThread.start();
        Thread thread = new Thread(clientThread);
        thread.start();
    }
    
    private void initUI() {
        final Surface surface = new Surface();
        add(surface);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Timer timer = surface.getTimer();
                timer.stop();
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Call function to draw pixel
                int mouse_x = e.getX() - 8;
                int mouse_y = e.getY() - 32;
                
                surface.paintPixel(mouse_x, mouse_y, 255, 0, 0);
                System.out.println("X: " + mouse_x + "\nY: " + mouse_y);
            } 
        });

        setTitle("Blackboard");
        setSize(32 * 8, 32 * 8);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame_Blackboard ex = new JFrame_Blackboard();
                ex.setVisible(true);
            }
        });
    }
}