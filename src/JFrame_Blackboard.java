import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.net.*;
import java.io.*;

///
/// This class is responsible for
/// drawing on screen using g2d.
///
class Surface extends JPanel implements ActionListener {
    private final int DELAY = 150;
    private Timer timer;

    Blackboard blackboard = JFrame_Blackboard.blackboard;
    
    public Surface() {
        initTimer();
        ClientThread clientThread = new ClientThread();
        clientThread.setBlackboard(blackboard);
        
        // Request global update since we just
        // created the drawing canvas
        try {
            JFrame_Main.out.writeUTF("0_");
        } catch (Exception e) {
            // TODO - Connection error handling
        }
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

        // Draw the pixels
        for (int i = 0; i < Server.CANVAS_SIZE; ++i) {
            for (int j = 0; j < Server.CANVAS_SIZE; ++j) {
                // Retrieve correct color
                Color currentColor = new Color(blackboard.getPixel(i, j, 0), blackboard.getPixel(i, j, 1), blackboard.getPixel(i, j, 2), 255);
                g2d.setPaint(currentColor);

                // Draw pixel on screen
                int x = i * JFrame_Blackboard.PIXEL_SIZE;
                int y = j * JFrame_Blackboard.PIXEL_SIZE;
                g2d.fillRect(x, y, x + JFrame_Blackboard.PIXEL_SIZE, y + JFrame_Blackboard.PIXEL_SIZE);
            }
        }
        
        // Draw the grid
        if (JFrame_Blackboard.drawGrid == true) {
            g2d.setPaint(new Color(255, 255, 255, 30));
            for (int i = 0; i < Server.CANVAS_SIZE * JFrame_Blackboard.PIXEL_SIZE; i += JFrame_Blackboard.PIXEL_SIZE) {
                g2d.drawLine(i, 0, i, Server.CANVAS_SIZE * JFrame_Blackboard.PIXEL_SIZE);
            }

            for (int j = 0; j < Server.CANVAS_SIZE * JFrame_Blackboard.PIXEL_SIZE; j += JFrame_Blackboard.PIXEL_SIZE) {
                g2d.drawLine(0, j, Server.CANVAS_SIZE * JFrame_Blackboard.PIXEL_SIZE, j);
            }
        }
        
        // Draw the toolbar
        g2d.drawImage(JFrame_Blackboard.toolbarZoomPlus.getImage(), 0, Server.CANVAS_SIZE * Server.PIXEL_SIZE, this);
        g2d.drawImage(JFrame_Blackboard.toolbarZoomMinus.getImage(), 32, Server.CANVAS_SIZE * Server.PIXEL_SIZE, this);
        g2d.drawImage(JFrame_Blackboard.toolbarLeft.getImage(), 64, Server.CANVAS_SIZE * Server.PIXEL_SIZE, this);
        g2d.drawImage(JFrame_Blackboard.toolbarRight.getImage(), 96, Server.CANVAS_SIZE * Server.PIXEL_SIZE, this);
        g2d.drawImage(JFrame_Blackboard.toolbarUp.getImage(), 128, Server.CANVAS_SIZE * Server.PIXEL_SIZE, this);
        g2d.drawImage(JFrame_Blackboard.toolbarDown.getImage(), 160, Server.CANVAS_SIZE * Server.PIXEL_SIZE, this);
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
    
    // Called when user clicks on the canvas
    public void paintPixel(int x, int y, int r, int g, int b) {
        // Paint pixel locally
        blackboard.setPixel(x / JFrame_Blackboard.PIXEL_SIZE, y / JFrame_Blackboard.PIXEL_SIZE, r, g, b);
        
        // Ask server to paint same pixel
        try {
            JFrame_Main.out.writeUTF("1_" + (x / JFrame_Blackboard.PIXEL_SIZE) + "_" + (y / JFrame_Blackboard.PIXEL_SIZE) + "_" + r + "_" + g + "_" + b);
        } catch (Exception e) {
            // TODO - Connection error handling
        }
    }
}

///
/// This class is responsible for handling
/// network messages without locking up the
/// drawing canvas while waiting.
///
class ClientThread implements Runnable {
    private Blackboard blackboard;
    
    public void setBlackboard(Blackboard blackboard) {
        this.blackboard = blackboard;
    }
    
    public void run() {
        System.out.println("INFO: Starting client thread");
        
        while (true) {
            try {
                String input = JFrame_Main.in.readUTF();
                
                // Discover which message type
                switch (input.charAt(0)) {
                    case '0':
                        // Global canvas update
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
                        blackboard.setPixel(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                        break;
                }
            } catch (Exception e) {
                // TODO: Handle connection error
                break;
            }
        }
    }
}

public class JFrame_Blackboard extends JFrame {
    public static Blackboard blackboard = new Blackboard();
    public static int PIXEL_SIZE = Server.PIXEL_SIZE;
    public static boolean drawGrid = true; 
    
    //Load toolbar images
    public static ImageIcon toolbarZoomPlus = new ImageIcon("32.png");
    public static ImageIcon toolbarZoomMinus = new ImageIcon("32.png");
    public static ImageIcon toolbarLeft = new ImageIcon("32.png");
    public static ImageIcon toolbarRight = new ImageIcon("32.png");
    public static ImageIcon toolbarUp = new ImageIcon("32.png");
    public static ImageIcon toolbarDown = new ImageIcon("32.png");
    
    private Color color = new Color(230, 10, 10);
    
    public JFrame_Blackboard() {
        // Initialize g2d canvas
        initUI();
        
        // Create a separate thread to handle
        // network messages
        ClientThread clientThread = new ClientThread();
        clientThread.setBlackboard(blackboard);
        Thread thread = new Thread(clientThread);
        thread.start();
    }
    
    private void initUI() {
        final Surface surface = new Surface();
        add(surface);

        // Handle window events
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Timer timer = surface.getTimer();
                timer.stop();
            }
        });
        
        // Handle mouse events
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Call function to draw pixel
                int mouse_x = e.getX() - 8;
                int mouse_y = e.getY() - 32;

                // Check if mouse was outside of canvas
                if (mouse_x > Server.CANVAS_SIZE * JFrame_Blackboard.PIXEL_SIZE) {
                    return;
                }
                
                // Check if click was on toolbar
                if (mouse_y > Server.CANVAS_SIZE * Server.PIXEL_SIZE) {
                    int selection = mouse_x / 32;
                    
                    System.out.println("LOG: Selected option #" + selection);
                    
                    switch (selection) {
                        case 0: // Zoom in
                            JFrame_Blackboard.PIXEL_SIZE += 2;
                            break;
                        case 1: // Zoom out
                            if (JFrame_Blackboard.PIXEL_SIZE > Server.PIXEL_SIZE) {
                                JFrame_Blackboard.PIXEL_SIZE -= 2;
                            }
                            break;
                        case 2: // Pan left
                            break;
                        case 3: // Pan right
                            break;
                        case 4: // Pan up
                            break;
                        case 5: // Pan down
                            break;
                    }
                    
                    return;
                }
                
                // If right-mouse pressed
                if (e.getButton() == MouseEvent.BUTTON3) {
                    // Select a new color
                    Color newColor = JColorChooser.showDialog(null, "Choose a color", Color.RED);
                    
                    if (newColor != null)
                        color = newColor;
                } else {
                    // Paint an individual pixel
                    surface.paintPixel(mouse_x, mouse_y, color.getRed(), color.getGreen(), color.getBlue());
                }
            } 
        });

        // Set window properties
        setTitle("Blackboard");
        setSize(Server.CANVAS_SIZE * JFrame_Blackboard.PIXEL_SIZE, 64 + (Server.CANVAS_SIZE * JFrame_Blackboard.PIXEL_SIZE));
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