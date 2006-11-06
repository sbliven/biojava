/*
 Copyright (C) 2002 EBI, GRL

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.idmapping.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.ensembl.idmapping.Config;
import org.ensembl.idmapping.IDMappingApplication;

import com.jgoodies.plaf.plastic.PlasticLookAndFeel;

/**
 * @author Glenn Proctor
 * 
 * To change the template for this generated type comment go to Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Main extends JFrame {

  private static final long serialVersionUID = 1L;

	private static final Dimension PANEL_SIZE = new Dimension(600, 650);

    private Config config;

    // these are declared globally since we need to access them from the run button
    private JList sourceDatabaseList, targetDatabaseList;

    private JComboBox sourceHostBox, sourceUserBox, sourcePortBox, targetHostBox, targetUserBox, targetPortBox;

    private JPasswordField sourcePasswordField, targetPasswordField;

    private JTextField globalDirField, globalExonerateField, globalEmailField;

    private JCheckBox globalExonerateBox, globalEmailBox, globalUploadIDsBox, globalUploadArchiveBox, globalUploadEventsBox;

    public Main(Config config) {

        this.config = config;

    }

    public static void main(String[] args) {

        Main m = new Main(new Config(".." + File.separator + "resources" + File.separator + "data" + File.separator + "idmapping.properties"));
        m.initComponents();
        m.show();

    }

    // -------------------------------------------------------------------------

    /**
     * Initialise the GUI
     */
    private void initComponents() {

        Map configChoices = config.buildConfigChoices();

        // ----------------------------
        // Frame

        setSize(PANEL_SIZE);

        setLookAndFeel();

        setTitle("Stable ID Mapping");
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent evt) {

                dispose();
                System.exit(0);
            }
        });

        // ----------------------------
        // Top panel - title

        JPanel topPanel = new JPanel();
        JLabel titleLabel = new JLabel();
        topPanel.setBackground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", 1, 18));
        titleLabel.setText("Stable ID Mapping");
        topPanel.add(titleLabel);

        // ----------------------------
        // Database selection panel

        JPanel databasePanel = new JPanel();
        databasePanel.setBackground(Color.WHITE);
        databasePanel.setBorder(new TitledBorder("Databases"));
        databasePanel.setLayout(new BorderLayout());
        databasePanel.add(buildDatabasePanel(configChoices, "Source", "source"), BorderLayout.WEST);
        databasePanel.add(buildDatabasePanel(configChoices, "Target", "target"), BorderLayout.EAST);

        // ----------------------------
        // Working directory

        JPanel middlePanel = new JPanel();
        middlePanel.setBackground(Color.WHITE);
        middlePanel.setLayout(new BorderLayout());

        JPanel directoryPanel = new JPanel();
        directoryPanel.setBackground(Color.WHITE);
        directoryPanel.setBorder(new TitledBorder("Working directory"));

        final JLabel dirLabel = new JLabel("Base directory: ");
        dirLabel.setBackground(Color.WHITE);
        final JTextField dirField = new JTextField(40);
        dirField.setToolTipText("Choose the base directory for creation of ID mapping files and directories.");
        globalDirField = dirField;
        dirField.setText(System.getProperty("idmapping.base_directory"));
        final JButton dirButton = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("resources/images/folder.gif")));
        dirButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select base directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    dirField.setText(chooser.getSelectedFile().toString());
                }
            }
        });

        directoryPanel.add(dirLabel, BorderLayout.WEST);
        directoryPanel.add(dirField, BorderLayout.CENTER);
        directoryPanel.add(dirButton, BorderLayout.EAST);

        // ----------------------------
        // Exonerate

        JPanel exoneratePanel = new JPanel();
        exoneratePanel.setBackground(Color.WHITE);
        exoneratePanel.setBorder(new TitledBorder("Exonerate"));
        exoneratePanel.setLayout(new BorderLayout());
        JCheckBox exonerateBox = new JCheckBox("Use exonerate", false);
        globalExonerateBox = exonerateBox;
        exonerateBox.setToolTipText("If checked, exonerate will be used for exon sequence matching");
        exonerateBox.setSelected(Config.booleanFromProperty("idmapping.use_exonerate"));
        exonerateBox.setBackground(Color.WHITE);
        final JLabel exonerateLabel = new JLabel("Exonerate executable: ");
        exonerateLabel.setBackground(Color.WHITE);
        final JTextField exonerateField = new JTextField(40);
        globalExonerateField = exonerateField;
        exonerateField.setToolTipText("The location of the exonerate execuable");
        exonerateField.setText(System.getProperty("idmapping.exonerate.path"));
        final JButton exonerateButton = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("resources/images/folder.gif")));
        exonerateButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select exonerate executable");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    exonerateField.setText(chooser.getSelectedFile().toString());
                }

            }
        });

        exoneratePanel.add(exonerateBox, BorderLayout.NORTH);
        exoneratePanel.add(exonerateLabel, BorderLayout.WEST);
        exoneratePanel.add(exonerateField, BorderLayout.CENTER);
        exoneratePanel.add(exonerateButton, BorderLayout.EAST);
        exonerateLabel.setEnabled(exonerateBox.isSelected());
        exonerateField.setEnabled(exonerateBox.isSelected());
        exonerateButton.setEnabled(exonerateBox.isSelected());

        exonerateBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                exonerateLabel.setEnabled(!exonerateLabel.isEnabled());
                exonerateField.setEnabled(!exonerateField.isEnabled());
                exonerateButton.setEnabled(!exonerateButton.isEnabled());
            }
        });

        // ----------------------------
        // Email

        JPanel middle2Panel = new JPanel();
        middle2Panel.setBackground(Color.WHITE);
        middle2Panel.setLayout(new BorderLayout());

        JPanel emailPanel = new JPanel();
        emailPanel.setBackground(Color.WHITE);
        emailPanel.setBorder(new TitledBorder("Email"));
        emailPanel.setLayout(new BorderLayout());
        JCheckBox emailBox = new JCheckBox("Send email on completion", false);
        globalEmailBox = emailBox;
        emailBox.setToolTipText("If checked, email will be sent on completion of the ID mapping run");
        emailBox.setBackground(Color.WHITE);
        if (System.getProperty("idmapping.email") != null && System.getProperty("idmapping.email").length() > 0) {
            emailBox.setSelected(true);
        }
        final JLabel emailLabel = new JLabel("Email address: ");
        final JTextField emailField = new JTextField(20);
        globalEmailField = emailField;
        emailField.setToolTipText("The email address to send email to upon completion");
        emailField.setText(System.getProperty("idmapping.email"));
        emailPanel.add(emailBox, BorderLayout.NORTH);
        emailPanel.add(emailLabel, BorderLayout.WEST);
        emailPanel.add(emailField, BorderLayout.EAST);
        emailLabel.setEnabled(emailBox.isSelected());
        emailField.setEnabled(emailBox.isSelected());

        emailBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                emailLabel.setEnabled(!emailLabel.isEnabled());
                emailField.setEnabled(!emailField.isEnabled());
            }
        });

        // ----------------------------
        // Upload

        JPanel uploadPanel = new JPanel();
        uploadPanel.setBackground(Color.WHITE);
        uploadPanel.setBorder(new TitledBorder("Uploading"));

        JCheckBox idUploadBox = new JCheckBox("Upload stable IDs", true);
        idUploadBox.setBackground(Color.WHITE);
        globalUploadIDsBox = idUploadBox;
        idUploadBox.setToolTipText("Whether to upload mapped stable IDs to the target database");
        idUploadBox.setSelected(Config.booleanFromProperty("idmapping.upload.stableids"));
        JCheckBox archiveUploadBox = new JCheckBox("Upload archive information", true);
        archiveUploadBox.setBackground(Color.WHITE);
        globalUploadArchiveBox = archiveUploadBox;
        archiveUploadBox.setToolTipText("Whether to upload gene and peptide archive data to the target database");
        archiveUploadBox.setSelected(Config.booleanFromProperty("idmapping.upload.archive"));
        JCheckBox eventUploadBox = new JCheckBox("Upload stable ID events", true);
        eventUploadBox.setBackground(Color.WHITE);
        globalUploadEventsBox = eventUploadBox;
        eventUploadBox.setToolTipText("Whether to upload stable ID events to the target database");
        eventUploadBox.setSelected(Config.booleanFromProperty("idmapping.upload.events"));

        uploadPanel.setLayout(new BoxLayout(uploadPanel, BoxLayout.Y_AXIS));
        uploadPanel.add(idUploadBox);
        uploadPanel.add(archiveUploadBox);
        uploadPanel.add(eventUploadBox);

        // ----------------------------
        // Button panel

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        JButton runButton = new JButton("Run");
        buttonPanel.add(runButton);
        //runButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("green_arrow.gif")));
        final Main parent = this;
        runButton.setToolTipText("Run ID mapping with the selected settings");
        runButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("resources/images/green_arrow.gif")));
        runButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                parent.writeSelectedProperties();
                //parent.setState(Frame.ICONIFIED );
                parent.dispose();
                parent.runMainApp();

            }
        });
        JButton quitButton = new JButton("Quit");
        buttonPanel.add(quitButton);
        quitButton.setToolTipText("Quit this session");
        quitButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                dispose();
                System.exit(0);
            }
        });

        // ----------------------------

        // ----------------------------
        // Add basic panels to content pane

        middlePanel.add(directoryPanel, BorderLayout.WEST);
        middlePanel.add(uploadPanel, BorderLayout.CENTER);

        middle2Panel.add(exoneratePanel, BorderLayout.WEST);
        middle2Panel.add(emailPanel, BorderLayout.CENTER);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(topPanel);
        contentPane.add(databasePanel);
        contentPane.add(middlePanel);
        contentPane.add(middle2Panel);
        contentPane.add(buttonPanel);

        pack();

    } // initComponents

    // -------------------------------------------------------------------------

    private void setLookAndFeel() {

        PlasticLookAndFeel.setTabStyle(PlasticLookAndFeel.TAB_STYLE_METAL_VALUE);
        PlasticLookAndFeel.setHighContrastFocusColorsEnabled(true);

        try {
            UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.Plastic3DLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // -------------------------------------------------------------------------

    private JPanel buildDatabasePanel(Map configChoices, String title, String sourceOrTarget) {

        String p = "idmapping." + sourceOrTarget + ".";
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new TitledBorder(title));
        panel.setLayout(new BorderLayout());

        // ----------------------------

        JPanel leftPanel = new JPanel();

        // list is actually in rightPanel but needs to be defined here
        final JList list = new JList();

        leftPanel.setBackground(Color.WHITE);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JPanel hostPanel = new JPanel();
        hostPanel.setBackground(Color.WHITE);

        JLabel hostLabel = new JLabel("Host: ");
        String[] hostChoices = getHostChoices(configChoices, sourceOrTarget);
        final JComboBox hostBox = new JComboBox(hostChoices);
        hostBox.setToolTipText("Select the " + sourceOrTarget + " database host");
        hostBox.setSelectedItem(System.getProperty(p + "host"));
        hostPanel.setLayout(new BorderLayout());
        hostPanel.add(hostLabel, BorderLayout.WEST);
        hostPanel.add(hostBox, BorderLayout.EAST);
        leftPanel.add(hostPanel);

        JPanel portPanel = new JPanel();
        portPanel.setBackground(Color.WHITE);
        JLabel portLabel = new JLabel("Port: ");
        final JComboBox portBox = new JComboBox(getPortsForSelectedHost((String) hostBox.getSelectedItem(), configChoices,
                sourceOrTarget));
        portBox.setToolTipText("Select the " + sourceOrTarget + " port");
        portBox.setSelectedIndex(0);
        portPanel.setLayout(new BorderLayout());
        portPanel.add(portLabel, BorderLayout.WEST);
        portPanel.add(portBox, BorderLayout.EAST);
        leftPanel.add(portPanel);

        final String localSourceOrTarget = sourceOrTarget;
        final Map localConfigChoices = configChoices;

        hostBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String[] ports = getPortsForSelectedHost((String) hostBox.getSelectedItem(), localConfigChoices,
                        localSourceOrTarget);
                portBox.removeAllItems();
                for (int i = 0; i < ports.length; i++) {
                    portBox.addItem(ports[i]);
                }
                portBox.setSelectedIndex(0);
            }
        });

        JPanel userPanel = new JPanel();
        userPanel.setBackground(Color.WHITE);
        JLabel userLabel = new JLabel("User: ");
        List userChoices = (List) configChoices.get(p + "user");
        final JComboBox userBox = new JComboBox(userChoices.toArray());
        userBox.setToolTipText("Select the " + sourceOrTarget + " user");
        userBox.setSelectedItem(System.getProperty(p + "user"));
        userPanel.setLayout(new BorderLayout());
        userPanel.add(userLabel, BorderLayout.WEST);
        userPanel.add(userBox, BorderLayout.EAST);
        leftPanel.add(userPanel);

        JPanel passwordPanel = new JPanel();
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setLayout(new BorderLayout());
        JLabel passwordLabel = new JLabel("Password: ");
        final JPasswordField passwordField = new JPasswordField(10);
        passwordField.setToolTipText("Enter the password (if required) for the " + sourceOrTarget + " user");
        passwordField.setText(System.getProperty(p + "password"));
        passwordPanel.add(passwordLabel, BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.EAST);

        final Main parent = this;
        final String st = sourceOrTarget;
        JButton showButton = new JButton("Show >>");
        showButton.setToolTipText("Show the databases on the " + sourceOrTarget + " host");
        showButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String[] databases = parent.getDatabases((String) hostBox.getSelectedItem(), (String) portBox.getSelectedItem(),
                        (String) userBox.getSelectedItem(), new String(passwordField.getPassword()), st);
                DefaultListModel lm = new DefaultListModel();
                for (int i = 0; i < databases.length; i++) {
                    lm.addElement(databases[i]);
                }
                list.setModel(lm);
            }
        });

        leftPanel.add(passwordPanel);
        leftPanel.add(showButton);
        leftPanel.add(Box.createVerticalStrut(100));
        panel.add(leftPanel, BorderLayout.WEST);

        // ----------------------------

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(300, 200));

        rightPanel.add(listScroller);

        panel.add(rightPanel, BorderLayout.EAST);

        // set global variable to point to the right value. Ugh!
        if (sourceOrTarget.equals("source")) {
            sourceHostBox = hostBox;
            sourcePortBox = portBox;
            sourceUserBox = userBox;
            sourcePasswordField = passwordField;
            sourceDatabaseList = list;
        } else {
            targetHostBox = hostBox;
            targetPortBox = portBox;
            targetUserBox = userBox;
            targetPasswordField = passwordField;
            targetDatabaseList = list;
        }

        return panel;

    } // -------------------------------------------------------------------------

    private String[] getDatabases(String host, String port, String user, String password, String sourceOrTarget) {

        List dbs = new ArrayList();

        try {
            Class.forName("org.gjt.mm.mysql.CoreDriver");
            Connection con = buildConnection(host, port, "", user, password);
            ResultSet rs = con.createStatement().executeQuery("SHOW DATABASES");
            while (rs.next()) {
                dbs.add(rs.getString(1));
            }
        } catch (Exception se) {

            JOptionPane.showMessageDialog(null, "Can't read list of " + sourceOrTarget
                    + " databases - please check host, port, user and password.", "Error reading databases",
                    JOptionPane.WARNING_MESSAGE);

        }

        return (String[]) dbs.toArray(new String[dbs.size()]);

    }

    // -------------------------------------------------------------------------

    private Connection buildConnection(String host, String port, String database, String user, String password) throws SQLException {

        Connection con = null;

        String url = host;
        if (port != null && port.length() > 0) {
            url += ":" + port;
        }

        url += "/" + database;

        con = java.sql.DriverManager.getConnection("jdbc:mysql://" + url, user, password);

        return con;

    }

    // -------------------------------------------------------------------------

    private void writeSelectedProperties() {

        Properties props = new Properties();
        props.setProperty("idmapping.source.host", (String) sourceHostBox.getSelectedItem());
        props.setProperty("idmapping.source.port", (String) sourcePortBox.getSelectedItem());
        props.setProperty("idmapping.source.user", (String) sourceUserBox.getSelectedItem());
        props.setProperty("idmapping.source.password", new String(sourcePasswordField.getPassword()));
        props.setProperty("idmapping.source.database", (String) sourceDatabaseList.getSelectedValue());

        props.setProperty("idmapping.target.host", (String) targetHostBox.getSelectedItem());
        props.setProperty("idmapping.target.port", (String) targetPortBox.getSelectedItem());
        props.setProperty("idmapping.target.user", (String) targetUserBox.getSelectedItem());
        props.setProperty("idmapping.target.password", new String(targetPasswordField.getPassword()));
        props.setProperty("idmapping.target.database", (String) targetDatabaseList.getSelectedValue());

        props.setProperty("idmapping.base_directory", globalDirField.getText());

        props.setProperty("idmapping.use_exonerate", new Boolean(globalExonerateBox.isSelected()).toString());
        props.setProperty("idmapping.exonerate.path", globalExonerateField.getText());

        if (globalEmailBox.isSelected()) {
            props.setProperty("idmapping.email", globalEmailField.getText());
        }

        props.setProperty("idmapping.upload.stableids", new Boolean(globalUploadIDsBox.isSelected()).toString());
        props.setProperty("idmapping.upload.archive", new Boolean(globalUploadArchiveBox.isSelected()).toString());
        props.setProperty("idmapping.upload.events", new Boolean(globalUploadEventsBox.isSelected()).toString());

        String fileName = ".." + File.separator + "resources" + File.separator + "data" + File.separator + "auto.idmapping.properties";
        
        try {
            props.store(new FileOutputStream(fileName), "Automatically generated by ID Mapping GUI");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    // -------------------------------------------------------------------------

    private String[] getHostChoices(Map configChoices, String sourceOrTarget) {
        
        List choices = new ArrayList();
        List hostsAndPorts = (List) configChoices.get("idmapping." + sourceOrTarget + ".host");
        Iterator it = hostsAndPorts.iterator();
        while (it.hasNext()) {
            String hostAndPort = (String) it.next();
            String host = hostAndPort.substring(0, hostAndPort.indexOf(":"));
            if (!choices.contains(host)) {
                choices.add(host);
            }
        }

        return (String[]) choices.toArray(new String[choices.size()]);

    }

    //---------------------------------------------------------------------

    private String[] getPortsForSelectedHost(String selectedHost, Map configChoices, String sourceOrTarget) {

        List choices = new ArrayList();
        List hostsAndPorts = (List) configChoices.get("idmapping." + sourceOrTarget + ".host");
        Iterator it = hostsAndPorts.iterator();
        while (it.hasNext()) {
            String hostAndPort = (String) it.next();
            String host = hostAndPort.substring(0, hostAndPort.indexOf(":"));
            if (host.equals(selectedHost)) {
                String port = hostAndPort.substring(hostAndPort.indexOf(":") + 1, hostAndPort.length());
                if (!choices.contains(port)) {
                    choices.add(port);
                }
            }
        }

        return (String[]) choices.toArray(new String[choices.size()]);

    }

    //---------------------------------------------------------------------

    private void runMainApp() {

        JOptionPane.showMessageDialog(null, "The main application will be started when you click \"OK\" - see console for output", "Main application started",
                JOptionPane.INFORMATION_MESSAGE);
        Thread t = new Thread(new IDMappingApplication("../resources/data/auto.idmapping.properties"));
        t.start();
        
        

    }
    
    //---------------------------------------------------------------------
    
} // Main

// -------------------------------------------------------------------------

