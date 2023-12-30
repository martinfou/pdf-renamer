package com.compica.pdfrenamer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private JTree fileTree;

    private JComboBox<String> projectNames;
    private JSpinner dateSpinner;
    private JComboBox<String> supplierField;
    private JFormattedTextField amountField;

    public PDFPreviewer() {
        frame = new JFrame("PDF Previewer and Renamer");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();

        // Create the "File" menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

            // Create the "Open Folder" menu item
    JMenuItem openFolderMenuItem = new JMenuItem("Open Folder");
    openFolderMenuItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser folderChooser = new JFileChooser();
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = folderChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = folderChooser.getSelectedFile();
                System.out.println(selectedFolder.getAbsolutePath());
            }
        }
    });
    fileMenu.add(openFolderMenuItem);

    // Set the menu bar on the frame
    frame.setJMenuBar(menuBar);


        // Create the input fields
        projectNames = new JComboBox<>(ConfigUtil.getConfig().getProjectList().toArray(new String[0]));
        dateSpinner = new JSpinner(new SpinnerDateModel());
        supplierField = new JComboBox(ConfigUtil.getConfig().getSupplierList().toArray(new String[0]));
        amountField = new JFormattedTextField(Double.valueOf(0.0));
        amountField.setColumns(8);

        // Configure the date spinner
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy/MM/dd");
        dateSpinner.setEditor(dateEditor);

        // Add the input fields to the panel
        panel = new JPanel();
        panel.add(new JLabel("Project:"));
        panel.add(projectNames);
        panel.add(new JLabel("Date:"));
        panel.add(dateSpinner);
        panel.add(new JLabel("Supplier:"));
        panel.add(supplierField);
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);

        button = new JButton("Rename");

        imageLabel = new JLabel();

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (file != null) {
                    String projectName = (String) projectNames.getSelectedItem();
                    String date = new SimpleDateFormat("yyyy-MM-dd").format((Date) dateSpinner.getValue());
                    String supplier = (String) supplierField.getSelectedItem();
                    String amount = amountField.getText();
                    String newName = projectName + "-" + date + "-" + supplier + "_" + amount;
                    File newFile = new File(file.getParentFile(), newName + ".pdf");

                    if (file.renameTo(newFile)) {
                        JOptionPane.showMessageDialog(frame, "File renamed successfully");
                        file = newFile;
                        refreshTree();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Error renaming file");
                    }
                }
            }
        });

        panel.add(button);

        frame.add(panel, BorderLayout.NORTH);

        scrollPane = new JScrollPane(imageLabel);

        frame.add(scrollPane, BorderLayout.CENTER);

        // Create the root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Files");
        // Add files to the root node
        addFilesToNode(root, new File(ConfigUtil.getConfig().getSourceFolder()));
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

    private void refreshTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Files");
        addFilesToNode(root, new File(ConfigUtil.getConfig().getSourceFolder()));
        DefaultTreeModel model = new DefaultTreeModel(root);
        fileTree.setModel(model);
        for (int i = 0; i < fileTree.getRowCount(); i++) {
            fileTree.expandRow(i);
        }
    }

}