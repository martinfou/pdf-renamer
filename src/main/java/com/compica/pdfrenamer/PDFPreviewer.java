package com.compica.pdfrenamer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.rendering.*;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class PDFPreviewer {
    private JFrame frame;
    private JLabel label;
    private JTextField textField;
    private JButton button;
    private JPanel panel;
    private JScrollPane scrollPane;
    private JLabel imageLabel;
    private File file;

    public PDFPreviewer() {
        frame = new JFrame("PDF Previewer and Renamer");
        label = new JLabel("Enter new name:");
        textField = new JTextField(20);
        button = new JButton("Rename");
        panel = new JPanel();
        imageLabel = new JLabel();

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (file != null) {
                    String newName = textField.getText();
                    File newFile = new File(file.getParentFile(), newName + ".pdf");
                    if (file.renameTo(newFile)) {
                        JOptionPane.showMessageDialog(frame, "File renamed successfully");
                        file = newFile;
                    } else {
                        JOptionPane.showMessageDialog(frame, "Error renaming file");
                    }
                }
            }
        });

        panel.add(label);
        panel.add(textField);
        panel.add(button);

        frame.add(panel, BorderLayout.NORTH);

        scrollPane = new JScrollPane(imageLabel);
        JTree fileTree; // Declare the fileTree variable

        frame.add(scrollPane, BorderLayout.CENTER);

        // Create the root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Files");
        // Add files to the root node
        addFilesToNode(root, new File("C:\\Users\\marti\\Dropbox\\Factures\\Facture 2023"));
        // Create the tree model and add the root node to it
        DefaultTreeModel model = new DefaultTreeModel(root);
        // Create the tree with the model
        fileTree = new JTree(model);
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if (node == null)
                    return;
                File file = (File) node.getUserObject();
                if (file.isFile()) {
                    previewPDF(file);
                }
            }
        });
        JScrollPane treeScrollPane = new JScrollPane(fileTree);
        // Add the scroll pane to the frame
        frame.getContentPane().add(treeScrollPane, BorderLayout.WEST);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    public void previewPDF(File file) {
        this.file = file;
        try {
            PDDocument document = PDDocument.load(file);
            PDFRenderer renderer = new PDFRenderer(document);
            Image image = renderer.renderImageWithDPI(0, 300);

            // Get the width and height of the scrollPane
            int viewPaneWidth = scrollPane.getWidth();
            int viewPaneHeight = scrollPane.getHeight();

            // Scale the image to fit the scrollPane
            Image scaledImage = image.getScaledInstance(viewPaneWidth, viewPaneHeight, Image.SCALE_SMOOTH);

            imageLabel.setIcon(new ImageIcon(scaledImage));
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                PDFPreviewer previewer = new PDFPreviewer();
                previewer.previewPDF(new File("C:\\Users\\marti\\Dropbox\\Factures\\Facture 2023\\b.pdf"));
            }
        });
    }

    // Method to recursively add files to a node
    private void addFilesToNode(DefaultMutableTreeNode node, File file) {
        if (file.isDirectory()) {
            DefaultMutableTreeNode dirNode = new DefaultMutableTreeNode(file);
            node.add(dirNode);
            for (File f : file.listFiles()) {
                addFilesToNode(dirNode, f);
            }
        } else {
            node.add(new DefaultMutableTreeNode(file));
        }
    }

}