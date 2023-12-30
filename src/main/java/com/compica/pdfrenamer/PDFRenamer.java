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

public class PDFRenamer {
    private static final String DOLLAR_SIGN = "$";

    private static final String SEPARATOR = "_";

    ConfigUtil configUtil = new ConfigUtil();
    Config config = configUtil.getConfig();
    private JFrame frame;

    private JButton button;
    private JButton refreshButton;
    private JPanel panel;
    private JScrollPane scrollPane;
    private JLabel imageLabel;
    private File file;
    private JTree fileTree;

    private JComboBox<String> documentTypeCombo;
    private JComboBox<String> projectNames;
    private JSpinner dateSpinner;
    private JComboBox<String> supplierField;
    private JFormattedTextField amountField;
    private JTextField descriptionTextField;

    public PDFRenamer() {
        frame = new JFrame("PDF Previewer and Renamer");

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
                openFolder();
            }
        });
        fileMenu.add(openFolderMenuItem);

        // Set the menu bar on the frame
        frame.setJMenuBar(menuBar);

        // Create the input fields
        documentTypeCombo = new JComboBox<>(config.getDocumentTypeList().toArray(new String[0]));
        projectNames = new JComboBox<>(config.getProjectList().toArray(new String[0]));

        dateSpinner = new JSpinner(new SpinnerDateModel());
        supplierField = new JComboBox<>(config.getSupplierList().toArray(new String[0]));
        amountField = new JFormattedTextField(Double.valueOf(0.00));
        amountField.setColumns(8);
        descriptionTextField = new JTextField(20);

        // Configure the date spinner
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);

        // Add the input fields to the panel
        panel = new JPanel();
        panel.add(new JLabel("Type Document:"));
        panel.add(documentTypeCombo);
        panel.add(new JLabel("Projet:"));
        panel.add(projectNames);
        panel.add(new JLabel("Date:"));
        panel.add(dateSpinner);
        panel.add(new JLabel("Supplier:"));
        panel.add(supplierField);
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);
        panel.add(new JLabel("Description:"));
        panel.add(descriptionTextField);

        button = new JButton("Rename");
        refreshButton = new JButton("refresh");

        imageLabel = new JLabel();

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renameFile();
                refreshComboBoxes();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshComboBoxes();
            }
        });

        panel.add(button);
        panel.add(refreshButton);
        frame.add(panel, BorderLayout.NORTH);
        scrollPane = new JScrollPane(imageLabel);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Create the root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Files");
        // Add files to the root node
        addFilesToNode(root, new File(config.getSourceFolder()));
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
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.getRootPane().setDefaultButton(button);
        refreshTree();
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
                PDFRenamer renamer = new PDFRenamer();
                renamer.frame.setVisible(true);
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
        addFilesToNode(root, new File(configUtil.getConfig().getSourceFolder()));
        DefaultTreeModel model = new DefaultTreeModel(root);
        fileTree.setModel(model);
        for (int i = 0; i < fileTree.getRowCount(); i++) {
            fileTree.expandRow(i);
        }
        // select the first node of the child of the root
        fileTree.setSelectionRow(2);
    }

    private void refreshComboBoxes() {
        documentTypeCombo.setModel(new DefaultComboBoxModel<String>(
                config.getDocumentTypeList().toArray(new String[0])));
        projectNames.setModel(new DefaultComboBoxModel<String>(
                config.getProjectList().toArray(new String[0])));
        supplierField.setModel(new DefaultComboBoxModel<String>(
                config.getSupplierList().toArray(new String[0])));
    }

    private void openFolder() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = folderChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = folderChooser.getSelectedFile();
            System.out.println(selectedFolder.getAbsolutePath());
        }
    }

    private void renameFile() {
        if (file != null) {
            String documentType = (String) documentTypeCombo.getSelectedItem();
            String projectName = (String) projectNames.getSelectedItem();
            String date = new SimpleDateFormat("yyyy-MM-dd").format((Date) dateSpinner.getValue());
            String supplier = (String) supplierField.getSelectedItem();
            String amount = amountField.getText();
            String description = descriptionTextField.getText();
            String newName = "";

            if (documentType.equals("facture")) {
                newName = documentType + SEPARATOR + projectName + SEPARATOR + date + SEPARATOR + supplier
                        + SEPARATOR + DOLLAR_SIGN + amount + SEPARATOR + description;
            } else {
                newName = documentType + SEPARATOR + projectName + SEPARATOR + date + SEPARATOR + supplier + SEPARATOR
                        + description;
            }

            File newFile = new File(file.getParentFile(), newName + ".pdf");
            if (file.renameTo(newFile)) {
                JOptionPane.showMessageDialog(frame, "File renamed successfully");
                file = newFile;
                refreshTree();
            } else {
                JOptionPane.showMessageDialog(frame, "Error renaming file");
                refreshTree();
            }
        }
    }

}