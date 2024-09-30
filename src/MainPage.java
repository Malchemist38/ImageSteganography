import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Image;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class MainPage {

    private JFrame frame;
    private JTextField messageField;
    private JLabel originalImageLabel;
    private JLabel encodedImageLabel;
    private File selectedImageFile;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Button.font", new FontUIResource(new Font("Segoe UI", Font.PLAIN, 14)));
            UIManager.put("Label.font", new FontUIResource(new Font("Segoe UI", Font.PLAIN, 14)));
            UIManager.put("TextField.font", new FontUIResource(new Font("Segoe UI", Font.PLAIN, 14)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventQueue.invokeLater(() -> {
            try {
                MainPage window = new MainPage();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public MainPage() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(200, 200, 1000, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10)); // add spacing between components

        JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
        frame.getContentPane().add(messagePanel, BorderLayout.NORTH);

        JLabel messageLabel = new JLabel("Message:");
        messagePanel.add(messageLabel, BorderLayout.WEST);

        messageField = new JTextField();
        messagePanel.add(messageField, BorderLayout.CENTER);
        messageField.setColumns(10);

        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // padding around the images
        frame.getContentPane().add(imagePanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;

        originalImageLabel = new JLabel();
        originalImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        originalImageLabel.setPreferredSize(new Dimension(400, 400)); // image fixed size
        imagePanel.add(originalImageLabel, gbc);

        gbc.gridx = 1;

        encodedImageLabel = new JLabel();
        encodedImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        encodedImageLabel.setPreferredSize(new Dimension(400, 400));
        imagePanel.add(encodedImageLabel, gbc);

        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        frame.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        JButton selectImageButton = new JButton("Select Image");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.25;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        buttonsPanel.add(selectImageButton, gbc);

        JButton encodeButton = new JButton("Encode");
        gbc.gridx = 1;
        buttonsPanel.add(encodeButton, gbc);

        JButton decodeButton = new JButton("Decode");
        gbc.gridx = 2;
        buttonsPanel.add(decodeButton, gbc);

        JButton clearButton = new JButton("Clear");
        gbc.gridx = 3;
        buttonsPanel.add(clearButton, gbc);

        selectImageButton.addActionListener(e -> {
            selectedImageFile = FileChooser.MakeFileChooser();
            if (selectedImageFile != null) {
                displayImage(selectedImageFile, originalImageLabel);
            }
        });

        encodeButton.addActionListener(e -> {
            if (selectedImageFile != null && !messageField.getText().isEmpty()) {
                File encodedImageFile = EncodeLSB.Encode(selectedImageFile, messageField.getText());
                if (encodedImageFile != null) {
                    displayImage(encodedImageFile, encodedImageLabel);
                    messageField.setText("");
                }
            }
        });

        decodeButton.addActionListener(e -> {
            File imageFile = FileChooser.MakeFileChooser();
            if (imageFile != null) {
                String message = DecodeLSB.Decode(imageFile);
                messageField.setText(message);
            }
        });

        clearButton.addActionListener(e -> {
            selectedImageFile = null;
            originalImageLabel.setIcon(null);
            encodedImageLabel.setIcon(null);
            messageField.setText("");
        });
    }

    private void displayImage(File imageFile, JLabel imageLabel) {
        try {
            Image image = ImageIO.read(imageFile);
            // scale image to fit within the label's preferred size
            Image scaledImage = image.getScaledInstance(imageLabel.getPreferredSize().width, 
                                                        imageLabel.getPreferredSize().height, 
                                                        Image.SCALE_SMOOTH);
            ImageIcon imageIcon = new ImageIcon(scaledImage);
            imageLabel.setIcon(imageIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
