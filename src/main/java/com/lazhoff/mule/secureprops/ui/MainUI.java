
package com.lazhoff.mule.secureprops.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class MainUI {

    private static final Logger LOG = LogManager.getLogger(MainUI.class);

    public static void main(String[] args) {
        LOG.debug("Running MainUI.main");
        SwingUtilities.invokeLater(MainUI::initAndShow);
    }

    /* ================== UI SETUP ================== */
    private static void initAndShow() {

        UISettings settings = SettingsManager.load();

        JFrame frame = new JFrame("SecureProps Folder Encryptor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1024, 700));

        // Top bar
        JTextField folderField   = new JTextField(40);
        JButton   browseButton   = new JButton("Browse");
        JButton   settingsButton = new JButton("Settings");

        // File list (JList)
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> fileList            = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane fileScroll            = new JScrollPane(fileList);

        // Map to find File object by name
        Map<String, File> fileMap = new HashMap<>();

        // Preview area
        JLabel     previewLabel   = new JLabel("Preview:");
        JTextArea  previewArea    = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setLineWrap(true);
        JScrollPane previewScroll = new JScrollPane(previewArea);

        JSplitPane splitPane      = createSplitPane(fileScroll, previewLabel, previewScroll);

        // Action buttons
        JButton postmanEncBtn = new JButton("Postman Encrypt");
        JButton postmanDecBtn = new JButton("Postman Decrypt");
        JButton propsEncBtn  = new JButton("Properties Encrypt");
        JButton propsDecBtn  = new JButton("Properties Decrypt");

        JPanel actionPanel = createActionPanel(postmanEncBtn, postmanDecBtn, propsEncBtn, propsDecBtn);

        // Log area
        JTextArea logArea = new JTextArea(10, 80);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);

        /* Layout */
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createTopPanel(folderField, browseButton, settingsButton), BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(splitPane, BorderLayout.CENTER);
        centerWrapper.add(actionPanel, BorderLayout.SOUTH);

        mainPanel.add(centerWrapper, BorderLayout.CENTER);
        mainPanel.add(logScroll, BorderLayout.SOUTH);

        // Redirect stdout/stderr to both console & logArea
        PrintStream originalOut = System.out;
        PrintStream teeStream;
        try {
            teeStream = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    originalOut.write(b);
                    SwingUtilities.invokeLater(() -> logArea.append(String.valueOf((char) b)));
                }
            }, true, "UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        System.setOut(teeStream);
        System.setErr(teeStream);

        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setVisible(true);

        /* Load last folder */
        folderField.setText(settings.lastFolder);
        if (!settings.lastFolder.isEmpty()) {
            File folder = new File(settings.lastFolder);
            if (folder.isDirectory()) {
                UIFileLoader.loadFiles(folder, listModel, fileMap);
            }
        }

        /* Event handlers */
        addBrowseAction(frame, folderField, browseButton, listModel, fileMap, logArea, settings);
        settingsButton.addActionListener(e -> {
            SettingsManager.openSettingsDialog(frame);
            UISettings updated = SettingsManager.load(); // reload new settings
            settings.copyFrom(updated);
            LOG.info("settings were updated and refreshed");
        });

        fileList.addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                String selected = fileList.getSelectedValue();
                if (selected != null) {
                    File file = fileMap.get(selected);
                    if (file != null) {
                        try {
                            String content = Files.readString(file.toPath());
                            previewArea.setText(content);
                            previewLabel.setText("Preview: " + file.getName());
                        } catch (IOException ex) {
                            previewArea.setText("Failed to load file: " + ex.getMessage());
                        }
                    }
                }
            }
        });

        attachCLIAction(postmanEncBtn, "encrypt",  "file-level", folderField, logArea, settings);
        attachCLIAction(postmanDecBtn, "decrypt",  "file-level", folderField, logArea, settings);
        attachCLIAction(propsEncBtn,   "encrypt",  "file",       folderField, logArea, settings);
        attachCLIAction(propsDecBtn,   "decrypt",  "file",       folderField, logArea, settings);
    }

    /* ========== helpers ========== */

    private static JPanel createTopPanel(JTextField folderField, JButton browseButton, JButton settingsButton) {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Folder:"));
        top.add(folderField);
        top.add(browseButton);
        top.add(settingsButton);
        return top;
    }

    private static JSplitPane createSplitPane(JScrollPane fileScroll, JLabel previewLabel, JScrollPane previewScroll) {
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.add(previewLabel, BorderLayout.NORTH);
        previewPanel.add(previewScroll, BorderLayout.CENTER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileScroll, previewPanel);
        splitPane.setResizeWeight(0.3);
        return splitPane;
    }

    private static JPanel createActionPanel(JButton... btns) {
        JPanel p = new JPanel();
        for (JButton b : btns) p.add(b);
        return p;
    }

    private static void addBrowseAction(JFrame frame,
                                        JTextField folderField,
                                        JButton browseButton,
                                        DefaultListModel<String> listModel,
                                        Map<String, File> fileMap,
                                        JTextArea logArea,
                                        UISettings settings) {
        browseButton.addActionListener((ActionEvent e) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (!folderField.getText().isEmpty()) {
                chooser.setCurrentDirectory(new File(folderField.getText()));
            }
            int res = chooser.showOpenDialog(frame);
            if (res == JFileChooser.APPROVE_OPTION) {
                File selected = chooser.getSelectedFile();
                folderField.setText(selected.getAbsolutePath());
                UIFileLoader.loadFiles(selected, listModel, fileMap);
                logArea.append("Loaded folder: " + selected.getAbsolutePath() + "\n");

                settings.lastFolder = selected.getAbsolutePath().replace('\\', '/');
                SettingsManager.save(settings);
            }
        });
    }

    /**
     * Run external CLI jar in separate JVM and stream output to log.
     */
    private static int runCliSubprocess(List<String> cliArgs, Consumer<String> logFn) {
        String jarPath;
        try {
            jarPath = getJarPath();

            logFn.accept("jarPath: " + jarPath);

        } catch (Exception e) {
            logFn.accept("Failed to resolve jar path: " + e.getMessage());
            return -1;
        }

        List<String> cmd = new ArrayList<>();
        cmd.add("java");
        cmd.add("-jar");
        cmd.add(jarPath);
        cmd.addAll(cliArgs);

        logFn.accept("Launching: " + cmd);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        try {
            Process proc = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logFn.accept(line);
                }
            }
            int code = proc.waitFor();
            logFn.accept("CLI exited with code " + code);
            return code;
        } catch (Exception ex) {
            logFn.accept("CLI process failed: " + ex.getMessage());
            return -1;
        }
    }

    private static String getJarPath() throws URISyntaxException {
        return new File(MainUI.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI())
                .getAbsolutePath();
    }

    private static void attachCLIAction(JButton button,
                                        String action,
                                        String fileOrLine,
                                        JTextField folderField,
                                        JTextArea logArea,
                                        UISettings settings) {
        button.addActionListener(e -> {
            File folder = new File(folderField.getText());
            if (!folder.exists() || !folder.isDirectory()) {
                JOptionPane.showMessageDialog(button, "Please select a valid folder first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            button.setEnabled(false);
            logArea.append("Starting " + button.getText() + "...\n");

            new SwingWorker<Integer, String>() {
                @Override
                protected Integer doInBackground() {
                    try {
                        List<String> argsList = new ArrayList<>();
                        argsList.add(action);
                        argsList.add(fileOrLine);
                        argsList.add(folder.getAbsolutePath());
                        argsList.add(settings.algorithm);
                        argsList.add(settings.mode);
                        argsList.add(Boolean.toString(settings.useRandomIV));
                        argsList.add("--envKeyMapping=" + (fileOrLine=="file-level" ? settings.envKeyMappingPostman: settings.envKeyMappingProperties));
                        if (settings.dryRun)  argsList.add("--dryRun");
                        if (settings.debug)   argsList.add("--debug");
                        if (!settings.backup) argsList.add("--noBackup");
                        argsList.add("--tmp=" + settings.lastFolder);
                        argsList.add("--rex=" + settings.secureAttributeRegex);

                        return runCliSubprocess(Collections.unmodifiableList(argsList), this::publish);
                    } catch (Exception ex) {
                        publish("Error: " + ex.getMessage());
                        LOG.error("CLI error", ex);
                        return 1;
                    }
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String line : chunks) logArea.append(line + "\n");
                }

                @Override
                protected void done() {
                    button.setEnabled(true);
                    try {
                        int code = get();
                        logArea.append("Finished " + button.getText() + " (exit " + code + ").\n");
                    } catch (Exception ex) {
                        logArea.append("Finished " + button.getText() + " with error: " + ex.getMessage() + "\n");
                    }
                }
            }.execute();
        });
    }
}
