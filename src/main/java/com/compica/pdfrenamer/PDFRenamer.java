package com.compica.pdfrenamer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.rendering.*;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class PDFRenamer {
    Logger logger = Logger.getLogger(PDFRenamer.class.getName());
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
        final JTextField editor = (JTextField) projectNames.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    Object selectedItem = projectNames.getSelectedItem();
                    projectNames.removeItem(selectedItem);
                }
            }
        });
        projectNames.setEditable(true);

        dateSpinner = new JSpinner(new SpinnerDateModel());
        supplierField = new JComboBox<>(config.getSupplierList().toArray(new String[0]));
        supplierField.setEditable(true);
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
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });

        supplierField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("supplierField action performed" + e.getActionCommand());
                String newItem = (String) supplierField.getEditor().getItem();
                if (!comboBoxContains(supplierField, newItem)) {
                    supplierField.addItem(newItem);
                    updateSupplierListConfig(newItem);
                }
            }

            private boolean comboBoxContains(JComboBox<String> comboBox, String item) {
                for (int i = 0; i < comboBox.getItemCount(); i++) {
                    if (item.equals(comboBox.getItemAt(i))) {
                        return true;
                    }
                }
                return false;
            }
        });

        // write the code to listen to the action of the projectNames combo box
        projectNames.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("projectNames action performed = " + e.getActionCommand());
                String newItem = (String) projectNames.getSelectedItem();
                if (!comboBoxContains(projectNames, newItem)) {
                    projectNames.addItem(newItem);
                    updateProjectListConfig(newItem);
                }
            }

            private boolean comboBoxContains(JComboBox<String> comboBox, String item) {
                for (int i = 0; i < comboBox.getItemCount(); i++) {
                    if (item.equals(comboBox.getItemAt(i))) {
                        return true;
                    }
                }
                return false;
            }
        });

        // Assuming `comboBox` is your JComboBox instance
        projectNames.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    Object selectedItem = projectNames.getSelectedItem();
                    projectNames.removeItem(selectedItem);
                }
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

    private void openFolder() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = folderChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = folderChooser.getSelectedFile();
            config.setSourceFolder(selectedFolder.getAbsolutePath());
            logger.info(selectedFolder.getAbsolutePath());
            ConfigUtil.saveConfig(config);
            refreshTree();

        }
    }

    private void updateSupplierListConfig(String newItem) {
        if (!config.getSupplierList().contains(newItem)) {
            config.getSupplierList().add(newItem);
            java.util.List<String> newList = config.getSupplierList();
            config.setSupplierList(newList);
            ConfigUtil.saveConfig(config);
            logger.info("supplier list updated");
        }
    }

    // write the code to update the config file with the new project list
    private void updateProjectListConfig(String newItem) {
        if (!config.getProjectList().contains(newItem)) {
            config.getProjectList().add(newItem);
            java.util.List<String> newList = config.getProjectList();
            config.setProjectList(newList);
            ConfigUtil.saveConfig(config);
            logger.info("project list updated");
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