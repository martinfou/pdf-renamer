package com.compica.pdfrenamer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
    private JPanel panel;
    private JScrollPane scrollPane;
    private JLabel imageLabel;
    private File file;
    private JTree fileTree;

    private JComboBox<String> documentTypeCombo;
    private JComboBox<String> projectComboBox;
    private JSpinner dateSpinner;
    private JComboBox<String> supplierComboBox;
    private JFormattedTextField amountTextField;
    private JTextField descriptionTextField;

    public PDFRenamer() {
        createGui();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                PDFRenamer renamer = new PDFRenamer();
                renamer.frame.setVisible(true);
            }
        });
    }

    private void previewPDF(File file) {
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

    protected void createGui() {
        logger.info("createGui");
        frame = new JFrame("PDF Previewer and Renamer");

        setupMenuBar();
        setupDocumentTypeComboBox();
        setupProjectNamesComboBox();
        setupSupplierComboBox();
        setupAmountInputField();
        setupDescriptionInputLabel();

        dateSpinner = new JSpinner(new SpinnerDateModel());
        imageLabel = new JLabel();

        // Configure the date spinner
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);

        // Add the input fields to the panel
        panel = new JPanel();
        panel.add(new JLabel("Type Document:"));
        panel.add(documentTypeCombo);
        panel.add(new JLabel("Projet:"));
        panel.add(projectComboBox);
        panel.add(new JLabel("Date:"));
        panel.add(dateSpinner);
        panel.add(new JLabel("Supplier:"));
        panel.add(supplierComboBox);
        panel.add(new JLabel("Amount:"));
        panel.add(amountTextField);
        panel.add(new JLabel("Description:"));
        panel.add(descriptionTextField);

        button = new JButton("Rename");

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renameFile();
            }
        });

        supplierComboBox.addActionListener(e -> {
            logger.info(new LogMessage("supplierComboBox action performed", e.getActionCommand()).toString());
            String newItem = (String) supplierComboBox.getEditor().getItem();
            if (!comboBoxContains(supplierComboBox, newItem)) {
                supplierComboBox.addItem(newItem);
                updateSupplierListConfig(newItem);
            }
        });

        documentTypeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info(new LogMessage("documentTypeCombo action performed", e.getActionCommand()).toString());
                String newItem = (String) documentTypeCombo.getSelectedItem();
                if (!comboBoxContains(documentTypeCombo, newItem)) {
                    documentTypeCombo.addItem(newItem);
                    updateDocumentTypeListConfig(newItem);
                }
            }
        });

        projectComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                logger.info(new LogMessage("projectComboBox action performed", e.getActionCommand()).toString());
                String newItem = (String) projectComboBox.getSelectedItem();
                if (!comboBoxContains(projectComboBox, newItem)) {
                    projectComboBox.addItem(newItem);
                    updateProjectListConfig(newItem);
                }
            }
        });

        projectComboBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    Object selectedItem = projectComboBox.getSelectedItem();
                    projectComboBox.removeItem(selectedItem);
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

    private void setupDescriptionInputLabel() {
        descriptionTextField = new JTextField(20);
    }

    private void setupAmountInputField() {
        amountTextField = new JFormattedTextField(Double.valueOf(0.00));
        amountTextField.setColumns(8);
    }

    private void setupMenuBar() {
        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();
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
        frame.setJMenuBar(menuBar);
    }

    private void setupSupplierComboBox() {
        Collections.sort(config.getSupplierList(), String.CASE_INSENSITIVE_ORDER);
        supplierComboBox = new JComboBox<>(config.getSupplierList().toArray(new String[0]));
        supplierComboBox.setEditable(true);
        final JTextField editor = (JTextField) supplierComboBox.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    Object selectedItem = supplierComboBox.getSelectedItem();
                    supplierComboBox.removeItem(selectedItem);
                    updateSupplierListConfig((String) selectedItem);
                }
            }
        });
    }

    private void setupProjectNamesComboBox() {
        Collections.sort(config.getProjectList(), String.CASE_INSENSITIVE_ORDER);
        projectComboBox = new JComboBox<>(config.getProjectList().toArray(new String[0]));
        final JTextField editor = (JTextField) projectComboBox.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    Object selectedItem = projectComboBox.getSelectedItem();
                    projectComboBox.removeItem(selectedItem);
                    updateProjectListConfig((String) selectedItem);
                }
            }
        });
        projectComboBox.setEditable(true);
    }

    private void setupDocumentTypeComboBox() {
        Collections.sort(config.getDocumentTypeList(), String.CASE_INSENSITIVE_ORDER);
        documentTypeCombo = new JComboBox<>(config.getDocumentTypeList().toArray(new String[0]));
        documentTypeCombo.setEditable(true);
        final JTextField editor = (JTextField) documentTypeCombo.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    Object selectedItem = documentTypeCombo.getSelectedItem();
                    documentTypeCombo.removeItem(selectedItem);
                    updateDocumentTypeListConfig((String) selectedItem);
                }
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
        var isInTheListAlready = config.getSupplierList().contains(newItem);

        if (!isInTheListAlready) {
            logger.info(new LogMessage("supplier list does not contain item", newItem).toString());
            config.getSupplierList().add(newItem);
            java.util.List<String> newList = config.getSupplierList();
            newList.sort(String::compareToIgnoreCase);
            config.setSupplierList(newList);
            logger.info(new LogMessage("supplier list updated with value", newItem).toString());
        } else {
            logger.info(new LogMessage("supplier list contains item", newItem).toString());
            var itemIdex = config.getSupplierList().indexOf(newItem);
            config.getSupplierList().remove(itemIdex);
        }

        ConfigUtil.saveConfig(config);
    }

    private void updateProjectListConfig(String item) {
        var isInTheListAlready = config.getProjectList().contains(item);

        if (!isInTheListAlready) {
            config.getProjectList().add(item);
            java.util.List<String> newList = config.getProjectList();
            config.setProjectList(newList);
            logger.info("project list updated");
        } else {
            logger.info("project list contains item");
            var itemIdex = config.getProjectList().indexOf(item);
            config.getProjectList().remove(itemIdex);
        }

        ConfigUtil.saveConfig(config);
    }

    private void updateDocumentTypeListConfig(String item) {
        var isInTheListAlready = config.getDocumentTypeList().contains(item);

        if (!isInTheListAlready) {
            config.getDocumentTypeList().add(item);
            java.util.List<String> newList = config.getDocumentTypeList();
            config.setDocumentTypeList(newList);
            logger.info("document type list updated");
        } else {
            logger.info("document type list contains item");
            var itemIdex = config.getDocumentTypeList().indexOf(item);
            config.getDocumentTypeList().remove(itemIdex);
        }

        ConfigUtil.saveConfig(config);
    }

    private void renameFile() {
        if (file != null) {
            String documentType = (String) documentTypeCombo.getSelectedItem();
            String projectName = (String) projectComboBox.getSelectedItem();
            String date = new SimpleDateFormat("yyyy-MM-dd").format((Date) dateSpinner.getValue());
            String supplier = (String) supplierComboBox.getSelectedItem();
            String amount = amountTextField.getText();
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

    // Moved the helper method outside the ActionListener for better readability
    private boolean comboBoxContains(JComboBox<String> comboBox, String item) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (item.equals(comboBox.getItemAt(i))) {
                return true;
            }
        }
        return false;
    }

}