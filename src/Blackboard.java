import java.util.concurrent.locks.*;

public class Blackboard {
    // If this is the server's blackboard, must
    // have additional safeties during get and set
    private boolean isServer;
    private ReentrantLock canvasLock;
    private Condition writeCondition, readCondition;
    private int wantWrite;
    
    // Constructor
    public Blackboard(boolean isServer) {
        this.isServer = isServer;
        wantWrite = 0;
        
        if (isServer) {
            canvasLock = new ReentrantLock(true);
            writeCondition = canvasLock.newCondition();
            readCondition = canvasLock.newCondition();
        }
    }
    
    // 2d array holding each pixel's RGB values
    private int[][][] canvas = new int[Server.CANVAS_SIZE][Server.CANVAS_SIZE][3];
    
    // Get pixel color
    // XY = screen coordinates
    // Z = Color channel (0 = r, 1 = g, 2 = b)
    public int getPixel(int x, int y, int z) {
        // Out of bounds, return gray
        if (x >= Server.CANVAS_SIZE || y >= Server.CANVAS_SIZE || z > 2) {
            return 70;
        }
        
        if (isServer) {
            // Wait if someone is writing / wants to write
            while (wantWrite > 0) {
                try {
                    readCondition.await();
                } catch (InterruptedException e) {
                    // Do nothing if interrupted
                }
            }
            
            canvasLock.lock();
            int color = canvas[x][y][z];
            canvasLock.unlock();
            
            return color;
        }

        return canvas[x][y][z];
    }
    
    // Paints an individual pixel
    public void setPixel(int x, int y, int r, int g, int b) {
        // Out of bounds
        if (x >= Server.CANVAS_SIZE || y >= Server.CANVAS_SIZE) {
            return;
        }
        
        if (isServer) {
            // Obtain lock
            wantWrite++;
            canvasLock.lock();
        }
        
        // Write data
        canvas[x][y][0] = r;
        canvas[x][y][1] = g;
        canvas[x][y][2] = b;
        
        if (isServer)  {
            // Release lock
            wantWrite--;
            readCondition.signalAll();
            canvasLock.unlock();
        }
    }
}
