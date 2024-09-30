import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

public class EncodeLSB {
	public static File Encode(File imageFile, String message) {
		// change this to allow for saving to a user selected location
		String directory = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
		String newImageFileString = directory + "\\export.png";
		File newImageFile = new File(newImageFileString);
		
		BufferedImage image;
		try {
			image = ImageIO.read(imageFile);
			BufferedImage imageToEncode = GetImageToEncode(image); // create a copy of the image to work with
			Pixel[] pixels = GetPixelArray(imageToEncode);
			String[] messageBinary = ConvertMessageToBinary(message);
			EncodeMessageBinaryInPixels(pixels, messageBinary);
			ReplacePixelsInNewBufferedImage(pixels, imageToEncode);
			SaveNewFile(imageToEncode, newImageFile);
		} catch(IOException e) {
			e.printStackTrace();
		}
		return newImageFile;
	}
	
	private static BufferedImage GetImageToEncode(BufferedImage image) {
		ColorModel colorModel = image.getColorModel();
		boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
		WritableRaster raster = image.copyData(null);
		return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
	}
	
	private static Pixel[] GetPixelArray(BufferedImage imageToEncode) {
		int height = imageToEncode.getHeight();
		int width = imageToEncode.getWidth();
		Pixel[] pixels = new Pixel[height * width]; // create a one-dimensional array using two properties
		int count = 0;
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				Color colorToAdd = new Color(imageToEncode.getRGB(x, y));
				pixels[count] = new Pixel(x, y, colorToAdd);
				count++;
			}
		}
		return pixels;
	}
	
	private static String[] ConvertMessageToBinary(String message) {
		int[] messageAscii = ConvertMessageToAscii(message);
		String[] messageBinary = ConvertAsciiToBinary(messageAscii);
		return messageBinary;
	}
	
	private static int[] ConvertMessageToAscii(String message) {
		int[] messageAscii = new int[message.length()];
		for(int i = 0; i < message.length(); i++) {
			messageAscii[i] = (int) message.charAt(i); // type cast each character to its associated ASCII value
		}
		return messageAscii;
	}
	
	private static String[] ConvertAsciiToBinary(int[] messageInAscii) {
		String[] messageBinary = new String[messageInAscii.length];
		for(int i = 0; i < messageInAscii.length; i++) {
			String asciiBinary = padZeros(Integer.toBinaryString(messageInAscii[i])); // convert the ASCII values to a string of binary values
			messageBinary[i] = asciiBinary;
		}
		return messageBinary;
	}
	
	// because toBinaryString returns only as many digits as needed, we extra zeros to the left in order to manipulate LSBs correctly
	private static String padZeros(String binary) {
		StringBuilder sb = new StringBuilder("00000000");
		int offset = 8 - binary.length();
		for(int i = 0; i < binary.length(); i++) {
			sb.setCharAt(i + offset, binary.charAt(i)); // begin writing the binary values so that there are always 8 digits for each character
		}
		return sb.toString();
	}
	
	private static void EncodeMessageBinaryInPixels(Pixel[] pixels, String[] messageBinary) {
		int pixelIndex = 0;
		boolean isLastCharacter = false;
		for(int i = 0; i < messageBinary.length; i++) {
			Pixel[] originalPixels = new Pixel[] {pixels[pixelIndex], pixels[pixelIndex + 1], pixels[pixelIndex + 2]};
			if(i + 1 == messageBinary.length) {
				isLastCharacter = true;
			}
			ChangePixelColor(messageBinary[i], originalPixels, isLastCharacter);
			pixelIndex += 3;
		}
	}
	
	private static void ChangePixelColor(String messageBinary, Pixel[] pixels, boolean isLastCharacter) {
		int messageIndex = 0;
		for(int i = 0; i < pixels.length - 1; i++) {
			char[] messageBinaryChars = new char[] {messageBinary.charAt(messageIndex), messageBinary.charAt(messageIndex + 1), messageBinary.charAt(messageIndex + 2)};
			String[] pixelRGBBinary = GetPixelRGBBinary(pixels[i], messageBinaryChars);
			pixels[i].setColor(GetNewPixelColor(pixelRGBBinary));
			messageIndex += 3;
		}
		if(isLastCharacter == false) {
			char[] messageBinaryChars = new char[] {messageBinary.charAt(messageIndex), messageBinary.charAt(messageIndex + 1), '1'};
			String[] pixelRGBBinary = GetPixelRGBBinary(pixels[pixels.length - 1], messageBinaryChars);
			pixels[pixels.length - 1].setColor(GetNewPixelColor(pixelRGBBinary));
		}
		else {
			char[] messageBinaryChars = new char[] {messageBinary.charAt(messageIndex), messageBinary.charAt(messageIndex + 1), '0'}; // indicating the end of the message to be encoded
			String[] pixelRGBBinary = GetPixelRGBBinary(pixels[pixels.length - 1], messageBinaryChars);
			pixels[pixels.length - 1].setColor(GetNewPixelColor(pixelRGBBinary));
		}
	}
	
	private static String[] GetPixelRGBBinary(Pixel pixel, char[] messageBinaryChars) {
		String[] pixelRGBBinary = new String[3];
		pixelRGBBinary[0] = ChangePixelBinary(Integer.toBinaryString(pixel.getColor().getRed()), messageBinaryChars[0]);
		pixelRGBBinary[1] = ChangePixelBinary(Integer.toBinaryString(pixel.getColor().getGreen()), messageBinaryChars[1]);
		pixelRGBBinary[2] = ChangePixelBinary(Integer.toBinaryString(pixel.getColor().getBlue()), messageBinaryChars[2]);
		return pixelRGBBinary;
	}
	
	private static String ChangePixelBinary(String pixelBinary, char messageBinaryChar) { // alter the original pixel's RGB binary value with the binary value of our message
		StringBuilder sb = new StringBuilder(pixelBinary);
		sb.setCharAt(pixelBinary.length() - 1, messageBinaryChar);
		return sb.toString();
	}
	
	private static Color GetNewPixelColor(String[] colorBinary) { // helper function to revert string color values to integers
		return new Color(Integer.parseInt(colorBinary[0], 2), Integer.parseInt(colorBinary[1], 2), Integer.parseInt(colorBinary[2], 2)); // second parameter of parseInt is radix for base 2
	}
	
	private static void ReplacePixelsInNewBufferedImage(Pixel[] pixels, BufferedImage imageToEncode) {
		for(int i = 0; i < pixels.length; i++) {
			imageToEncode.setRGB(pixels[i].getX(), pixels[i].getY(), pixels[i].getColor().getRGB()); // set the new pixel RGB values in the buffered image
		}
	}
	
	private static void SaveNewFile(BufferedImage newImage, File newImageFile) {
		try {
			ImageIO.write(newImage, "png", newImageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
