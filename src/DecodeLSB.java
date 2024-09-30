import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

public class DecodeLSB {

	public static String Decode(File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            Pixel[] pixels = GetPixelArray(image);
            return DecodeMessageFromPixels(pixels);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	private static Pixel[] GetPixelArray(BufferedImage imageToDecode) {
		int height = imageToDecode.getHeight();
		int width = imageToDecode.getWidth();
		Pixel[] pixels = new Pixel[height * width]; // create a one-dimensional array using two properties
		int count = 0;
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				Color colorToAdd = new Color(imageToDecode.getRGB(x, y));
				pixels[count] = new Pixel(x, y, colorToAdd);
				count++;
			}
		}
		return pixels;
	}
	
	private static String DecodeMessageFromPixels(Pixel[] pixels) { // create a new pixel array with the values associated with our message
		boolean completed = false;
		int pixelIndex = 0;
		StringBuilder sb = new StringBuilder("");
		while(completed == false) {
			Pixel[] pixelsToRead = new Pixel[3];
			for(int i = 0; i < 3; i++) {
				pixelsToRead[i] = pixels[pixelIndex];
				pixelIndex++;
			}
			sb.append(ConvertPixelsToCharacter(pixelsToRead));
			if(IsEndOfMessage(pixelsToRead[2]) == true) {
				completed = true;
			}
		}
		return sb.toString();
	}
	
	private static char ConvertPixelsToCharacter(Pixel[] pixelsToRead) {
		ArrayList<String> binaryValues = new ArrayList<String>();
		for(int i = 0; i < pixelsToRead.length; i++) {
			String[] encodedBinary = ConvertPixelIntToBinary(pixelsToRead[i]);
			binaryValues.add(encodedBinary[0]);
			binaryValues.add(encodedBinary[1]);
			binaryValues.add(encodedBinary[2]);
		}
		return ConvertBinaryValuesToCharacter(binaryValues);
	}
	
	private static String[] ConvertPixelIntToBinary(Pixel pixel) {
		String[] values = new String[3];
		values[0] = Integer.toBinaryString(pixel.getColor().getRed());
		values[1] = Integer.toBinaryString(pixel.getColor().getGreen());
		values[2] = Integer.toBinaryString(pixel.getColor().getBlue());
		return values;
	}
	
	private static char ConvertBinaryValuesToCharacter(ArrayList<String> binaryValues) {
		StringBuilder sb = new StringBuilder("");
		for(int i = 0; i < binaryValues.size() - 1; i++) {
			sb.append(binaryValues.get(i).charAt(binaryValues.get(i).length() - 1)); // get the last character in the pixel's binary value to compile the message
		}
		String messageBinaryString = sb.toString();
		String noZeros = RemovePaddedZeros(messageBinaryString);
		int ascii = Integer.parseInt(noZeros, 2);
		return (char) ascii;
	}
	
	private static String RemovePaddedZeros(String messageBinaryString) {
		StringBuilder sb = new StringBuilder(messageBinaryString);
		int paddedZeros = 0;
		for(int i = 0; i < sb.length(); i++) {
			if(sb.charAt(i) == '0') {
				paddedZeros++;
			}
			else {
				break;
			}
		}
		for(int i = 0; i < paddedZeros; i++) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}
	
	private static boolean IsEndOfMessage(Pixel pixel) {
		if(ConvertPixelIntToBinary(pixel)[2].endsWith("1")) { // take index 2 (third pixel binary in the working array) and assess for continuation/termination
			return false;
		}
		return true;
	}
}
