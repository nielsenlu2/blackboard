import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.*;
import java.io.IOException;
import javax.imageio.ImageIO;

public class convertToBlackboard {

    //
    // Taken from https://stackoverflow.com/questions/22391353/get-color-of-each-pixel-of-an-image-using-bufferedimages
    //
    public static Color[][] loadPixelsFromImage(File file) throws IOException {

        BufferedImage image = ImageIO.read(file);
        Color[][] colors = new Color[image.getWidth()][image.getHeight()];

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                colors[x][y] = new Color(image.getRGB(x, y));
            }
        }

        return colors;
    }

    //
    // Reads image and writes the pixel colors out
    // in the same format that our program reads
    //
    public static void main(String[] args) throws IOException {
        // Call function taken from stackOverflow,
        // returns a 2D array of Color objects
        Color[][] colors = loadPixelsFromImage(new File("image.gif"));

        // Converts to our format and writes it to blackboard.save
        try {
            // Open file for writing
            FileWriter fileWriter = new FileWriter("blackboard.save");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Write pixels to string
            String msg = "";
            for (int i = 0; i < (64*64); ++i) {
                int x = i % 64;
                int y = Math.round(i / 64);
                
                msg += "_";
                msg += colors[x][y].getRed() + "_";
                msg += colors[x][y].getGreen() + "_";
                msg += colors[x][y].getBlue();
            }

            // Write string to file
            bufferedWriter.write(msg);

            // Release file
            bufferedWriter.close();
        } catch(IOException e) {
            System.out.println("ERROR: Could not write blackboard to file" + '\n' + e.toString());
        }
    }
}