public class Blackboard {
    // 2d array holding each pixel's RGB values
    private int[][][] canvas = new int[Server.CANVAS_SIZE][Server.CANVAS_SIZE][3];
    
    // Get pixel color
    // XY = screen coordinates
    // Z = Color channel (0 = r, 1 = g, 2 = b)
    public int getPixel(int x, int y, int z) {
        // Out of bounds
        if (x >= Server.CANVAS_SIZE || y >= Server.CANVAS_SIZE || z > 2) {
            return 70;
        }
        
        return canvas[x][y][z];
    }
    
    // Paints an individual pixel
    public void setPixel(int x, int y, int r, int g, int b) {
        // Out of bounds
        if (x >= Server.CANVAS_SIZE || y >= Server.CANVAS_SIZE) {
            return;
        }
        
        System.out.println(r + " " + g + " " + b);
        canvas[x][y][0] = r;
        canvas[x][y][1] = g;
        canvas[x][y][2] = b;
    }
}
