/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered;

import com.giuseppelamalfa.gameofliferemastered.simulation.SimulationRemoteClient;
import com.giuseppelamalfa.gameofliferemastered.simulation.SimulationGUIServer;
import com.giuseppelamalfa.gameofliferemastered.simulation.SimulationInterface;
import com.giuseppelamalfa.gameofliferemastered.utils.ImageManager;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.*;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.GameMode;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import com.giuseppelamalfa.gameofliferemastered.simulation.DisconnectEventListener;
import static com.giuseppelamalfa.gameofliferemastered.simulation.SimulationCLIServer.getSimulationMode;
import com.giuseppelamalfa.gameofliferemastered.utils.DeferredImageManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import java.net.URL;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;

/**
 *
 * @author glitchedcode
 */
public class ApplicationFrame extends javax.swing.JFrame implements KeyListener, DisconnectEventListener {

    static public final int BOARD_UPDATE_MS = 150;
    static public final int MAX_PLAYER_NAME_LENGTH = 30;
    static public final int MAX_ROWS = 400;
    static public final int MAX_COLS = 400;

    private int localRowCount = 50;
    private int localColumnCount = 70;

    private final SimulationInterface localGrid;
    private final ImageManager tileManager;

    SimulationRemoteClient client;
    SimulationGUIServer server;

    static ImageIcon icon;
    static JTextArea mainStatusLog = new JTextArea();
    boolean isInMenu = true;

    String localPlayerName = "Player";
    PlayerData localPlayerData = new PlayerData("Player", PlayerData.TeamColor.NONE);

    /*
    * JFRAME CODE
     */
    public static void staticInit() {
        URL resource = ApplicationFrame.class.getClassLoader().getResource("Tiles/tile_0083.png");
        icon = new ImageIcon(resource);
    }

    /**
     * Creates new form ApplicationFrame
     *
     * @throws java.lang.Exception
     */
    public ApplicationFrame() throws Exception {
        tileManager = new DeferredImageManager("tiles.json");
        SimulationGUIServer tmp = new SimulationGUIServer(localRowCount, localColumnCount);
        localGrid = tmp;

        initComponents();
        requestFocus();
        tmp.initializeGridPanel(gridPanel);
        mainStatusLog = statusLog;
        addWindowListener(new WindowAdapter() {
            public void WindowClosing(WindowEvent e) {
                if (client != null) {
                    client.close();
                }
                if (server != null) {
                    server.close();
                }
            }
        });

        if (!tileManager.isInitialized()) {
            throw new Exception("Failed to initialize tile manager.");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        outerLayeredPane = new javax.swing.JLayeredPane();
        menuPanel = new com.giuseppelamalfa.gameofliferemastered.ui.MenuPanel();
        titleLabel = new javax.swing.JLabel();
        hostPortNumber = new javax.swing.JTextField();
        serverAddress = new javax.swing.JTextField();
        serverPortNumber = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        maxPlayerCount = new javax.swing.JTextField();
        hostSandboxGameButton = new javax.swing.JButton();
        joinGameButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        statusLog = new javax.swing.JTextArea();
        playerNameField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        rowField = new javax.swing.JTextField();
        colField = new javax.swing.JTextField();
        unpauseButton = new javax.swing.JButton();
        hostCompetitiveGameButton = new javax.swing.JButton();
        gridPanel = gridPanel = new com.giuseppelamalfa.gameofliferemastered.ui.GridPanel(tileManager);
        gameStatusPanel = new com.giuseppelamalfa.gameofliferemastered.ui.GameStatusPanel();
        unitPalette = new com.giuseppelamalfa.gameofliferemastered.ui.UnitPalette();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("The game of Life: Remastered");
        setBackground(java.awt.Color.black);
        setIconImage(icon.getImage());
        setMinimumSize(new java.awt.Dimension(800, 600));
        setSize(new java.awt.Dimension(800, 600));

        outerLayeredPane.setPreferredSize(new java.awt.Dimension(800, 600));
        outerLayeredPane.addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsListener() {
            public void ancestorMoved(java.awt.event.HierarchyEvent evt) {
            }
            public void ancestorResized(java.awt.event.HierarchyEvent evt) {
                outerLayeredPaneAncestorResized(evt);
            }
        });

        menuPanel.setInheritsPopupMenu(true);
        menuPanel.setOpaque(false);
        menuPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuPanelMouseClicked(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("sansserif", 0, 48)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(255, 255, 255));
        titleLabel.setText("The Game of Life: Remastered");
        titleLabel.setToolTipText("");

        hostPortNumber.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        hostPortNumber.setText("7777");

        serverAddress.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        serverAddress.setText("localhost");

        serverPortNumber.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        serverPortNumber.setText("7777");

        jLabel1.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Host Game");

        jLabel2.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Port:");

        jLabel3.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Join Game");

        jLabel4.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Max Players:");

        maxPlayerCount.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        maxPlayerCount.setText("8");

        hostSandboxGameButton.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        hostSandboxGameButton.setText("Host Sandbox Game");
        hostSandboxGameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                StartSandboxServerHandler(evt);
            }
        });
        hostSandboxGameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostSandboxGameButtonActionPerformed(evt);
            }
        });

        joinGameButton.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        joinGameButton.setText("Connect");
        joinGameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JoinGameHandler(evt);
            }
        });

        statusLog.setEditable(false);
        statusLog.setColumns(20);
        statusLog.setRows(5);
        jScrollPane1.setViewportView(statusLog);

        playerNameField.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        playerNameField.setText("Player");

        jLabel5.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Player name");

        jLabel6.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Rows");

        jLabel7.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Columns");

        rowField.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        rowField.setText("50");

        colField.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        colField.setText("70");

        unpauseButton.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        unpauseButton.setText("Return to game");
        unpauseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                unpauseButtonMouseClicked(evt);
            }
        });
        unpauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unpauseButtonActionPerformed(evt);
            }
        });

        hostCompetitiveGameButton.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        hostCompetitiveGameButton.setText("Host Competitive Game");
        hostCompetitiveGameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                StartCompetitiveServerHandler(evt);
            }
        });

        javax.swing.GroupLayout menuPanelLayout = new javax.swing.GroupLayout(menuPanel);
        menuPanel.setLayout(menuPanelLayout);
        menuPanelLayout.setHorizontalGroup(
            menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(menuPanelLayout.createSequentialGroup()
                        .addGroup(menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3))
                        .addGap(40, 40, 40)
                        .addGroup(menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, menuPanelLayout.createSequentialGroup()
                                .addComponent(serverAddress)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(serverPortNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(menuPanelLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(hostPortNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maxPlayerCount, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(63, 63, 63)
                        .addGroup(menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(hostSandboxGameButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(hostCompetitiveGameButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(joinGameButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(titleLabel)
                    .addGroup(menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, menuPanelLayout.createSequentialGroup()
                            .addComponent(jLabel5)
                            .addGap(18, 18, 18)
                            .addComponent(playerNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel6)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(rowField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(jLabel7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(colField, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(60, Short.MAX_VALUE))
            .addGroup(menuPanelLayout.createSequentialGroup()
                .addComponent(unpauseButton)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        menuPanelLayout.setVerticalGroup(
            menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleLabel)
                .addGap(130, 130, 130)
                .addComponent(hostCompetitiveGameButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostPortNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(maxPlayerCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hostSandboxGameButton))
                .addGap(18, 18, 18)
                .addGroup(menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serverPortNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(joinGameButton))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(playerNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(rowField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                .addComponent(unpauseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        PlainDocument rowFieldDocument = (PlainDocument) rowField.getDocument();
        GridSizeDocumentListener sizeListener = new GridSizeDocumentListener(rowField, colField, this);
        rowFieldDocument.addDocumentListener(sizeListener);
        PlainDocument colFieldDocument = (PlainDocument) colField.getDocument();
        colFieldDocument.addDocumentListener(sizeListener);

        gridPanel.setBackground(new java.awt.Color(61, 63, 65));
        gridPanel.setForeground(new java.awt.Color(186, 186, 186));
        gridPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        gameStatusPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gameStatusPanelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout gridPanelLayout = new javax.swing.GroupLayout(gridPanel);
        gridPanel.setLayout(gridPanelLayout);
        gridPanelLayout.setHorizontalGroup(
            gridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPanelLayout.createSequentialGroup()
                .addComponent(gameStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 558, Short.MAX_VALUE))
        );
        gridPanelLayout.setVerticalGroup(
            gridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPanelLayout.createSequentialGroup()
                .addComponent(gameStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(354, Short.MAX_VALUE))
        );

        gameStatusPanel.setGridPanel(gridPanel);

        javax.swing.GroupLayout unitPaletteLayout = new javax.swing.GroupLayout(unitPalette);
        unitPalette.setLayout(unitPaletteLayout);
        unitPaletteLayout.setHorizontalGroup(
            unitPaletteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        unitPaletteLayout.setVerticalGroup(
            unitPaletteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 34, Short.MAX_VALUE)
        );

        outerLayeredPane.setLayer(menuPanel, javax.swing.JLayeredPane.DRAG_LAYER);
        outerLayeredPane.setLayer(gridPanel, javax.swing.JLayeredPane.PALETTE_LAYER);
        outerLayeredPane.setLayer(unitPalette, javax.swing.JLayeredPane.MODAL_LAYER);

        javax.swing.GroupLayout outerLayeredPaneLayout = new javax.swing.GroupLayout(outerLayeredPane);
        outerLayeredPane.setLayout(outerLayeredPaneLayout);
        outerLayeredPaneLayout.setHorizontalGroup(
            outerLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gridPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(outerLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(menuPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(outerLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(unitPalette, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        outerLayeredPaneLayout.setVerticalGroup(
            outerLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outerLayeredPaneLayout.createSequentialGroup()
                .addComponent(gridPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                .addGap(34, 34, 34))
            .addGroup(outerLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(menuPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(outerLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, outerLayeredPaneLayout.createSequentialGroup()
                    .addContainerGap(566, Short.MAX_VALUE)
                    .addComponent(unitPalette, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        menuPanel.getAccessibleContext().setAccessibleParent(menuPanel);
        menuPanel.setBackground(new Color(0,0,0,127));
        gridPanel.swingInit(unitPalette);
        gridPanel.setSimulation(localGrid);
        addKeyListener(gridPanel);
        try {
            unitPalette.init(tileManager);
        } catch (Exception e) {e.printStackTrace();}

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(outerLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(outerLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void gridCanvasAncestorResized(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_gridCanvasAncestorResized

    }//GEN-LAST:event_gridCanvasAncestorResized

    private void gameStatusPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gameStatusPanelMouseClicked
    }//GEN-LAST:event_gameStatusPanelMouseClicked

    private void unpauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unpauseButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_unpauseButtonActionPerformed

    private void unpauseButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_unpauseButtonMouseClicked
        swapCanvas();
    }//GEN-LAST:event_unpauseButtonMouseClicked

    private boolean CheckPlayerName() {
        int len = playerNameField.getText().length();
        if (len < 1 | len > MAX_PLAYER_NAME_LENGTH) {
            writeToStatusLog("Invalid player name.");
            return false;
        }
        return true;
    }

    private void JoinGameHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_JoinGameHandler
        if (server != null | !CheckPlayerName()) {
            return;
        }

        if (client == null) {
            try {
                client = new SimulationRemoteClient(playerNameField.getText(),
                        serverAddress.getText(), Integer.parseInt(serverPortNumber.getText()));
            } catch (Exception e) {
                writeToStatusLog("Could not connect to  server at address "
                        + serverAddress.getText() + ":" + serverPortNumber.getText());
                writeToStatusLog(e.toString());
                System.out.println(e);
                client = null;
            }
            if (client != null) {
                localGrid.setRunning(false);
                client.initializeGridPanel(gridPanel);
                joinGameButton.setText("Disconnect");
                serverAddress.setEditable(false);
                serverPortNumber.setEditable(false);
                hostSandboxGameButton.setEnabled(false);
                hostCompetitiveGameButton.setEnabled(false);
                playerNameField.setEditable(false);

                client.initializeGridPanel(gridPanel);
                client.addDisconnectEventListener(this);
                gridPanel.setSimulation(client);
            }
        } else {
            writeToStatusLog("Disconnecting...");
            client.close();
            client = null;
            serverAddress.setEditable(true);
            serverPortNumber.setEditable(true);
            hostSandboxGameButton.setEnabled(true);
            hostCompetitiveGameButton.setEnabled(true);
            joinGameButton.setText("Join Game");
            playerNameField.setEditable(true);

            gridPanel.setSimulation(localGrid);
        }
    }//GEN-LAST:event_JoinGameHandler

    @Override
    public void onDisconnect() {
        JoinGameHandler(null);
        if (!isInMenu) {
            swapCanvas();
        }
    }

    private void StartSandboxServerHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_StartSandboxServerHandler
        if (!hostSandboxGameButton.isEnabled()) {
            return;
        }

        toggleServer(GameMode.SANDBOX);
        if (server != null) {
            hostSandboxGameButton.setText("Close Server");
            hostCompetitiveGameButton.setEnabled(false);
            joinGameButton.setEnabled(false);
        } else {
            hostSandboxGameButton.setText("Host Sandbox Game");
            hostCompetitiveGameButton.setEnabled(true);
            joinGameButton.setEnabled(true);
        }
    }//GEN-LAST:event_StartSandboxServerHandler

    private void outerLayeredPaneAncestorResized(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_outerLayeredPaneAncestorResized
        gridPanel.setSize(getSize());
        menuPanel.setSize(getSize());
        gridPanel.resetScreenOrigin();
        gridPanel.repaint();
    }//GEN-LAST:event_outerLayeredPaneAncestorResized

    private void StartCompetitiveServerHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_StartCompetitiveServerHandler
        if (!hostCompetitiveGameButton.isEnabled()) {
            return;
        }

        toggleServer(GameMode.COMPETITIVE);
        if (server != null) {
            hostCompetitiveGameButton.setText("Close Server");
            hostSandboxGameButton.setEnabled(false);
            joinGameButton.setEnabled(false);
        } else {
            hostCompetitiveGameButton.setText("Host Competitive Game");
            hostSandboxGameButton.setEnabled(true);
            joinGameButton.setEnabled(true);
        }
    }//GEN-LAST:event_StartCompetitiveServerHandler

    private void hostSandboxGameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostSandboxGameButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hostSandboxGameButtonActionPerformed

    private void menuPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuPanelMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_menuPanelMouseClicked

    private void toggleServer(GameMode mode) {
        if (server == null) {
            try {
                server = new SimulationGUIServer(playerNameField.getText(), Integer.parseInt(hostPortNumber.getText()),
                        Integer.parseInt(maxPlayerCount.getText()), localRowCount, localColumnCount,
                        mode);
            } catch (Exception e) {
                writeToStatusLog("Could not host server on port " + hostPortNumber.getText());
                writeToStatusLog(e.toString());
                server = null;
            }
            if (server != null) {
                localGrid.setRunning(false);
                hostPortNumber.setEditable(false);
                maxPlayerCount.setEditable(false);
                playerNameField.setEditable(false);

                server.initializeGridPanel(gridPanel);
                gridPanel.setSimulation(server);
            }
        } else {
            writeToStatusLog("Closing server...");
            server.close();
            server = null;
            hostPortNumber.setEditable(true);
            maxPlayerCount.setEditable(true);
            playerNameField.setEditable(true);

            gridPanel.setSimulation(localGrid);
        }
    }

    public static void writeToStatusLog(String string) {
        mainStatusLog.append(string + "\n");
    }

    public int getRowCount() {
        return localRowCount;
    }

    public int getColumnCount() {
        return localColumnCount;
    }

    public void setNextGridSize(int rows, int cols) {
        if (rows <= MAX_ROWS & rows > 0 & cols <= MAX_COLS & cols > 0) {
            localRowCount = rows;
            localColumnCount = cols;
        } else {
            rowField.setText(Integer.toString(localRowCount));
            colField.setText(Integer.toString(localRowCount));
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        //</editor-fold>
        //</editor-fold>

        for (int i = 0; i < args.length; i++) {
            String currentArg = args[i];
            switch (currentArg) {
                case "-S":
                    SpeciesLoader.setCustomSpeciesConfigPath(args[++i]);
                    break;
            }
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                ApplicationFrame.staticInit();
                ApplicationFrame frame = new ApplicationFrame();
                frame.addKeyListener(frame);
                frame.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * Invoked when a key has been typed. See the class description for
     * {@link KeyEvent} for a definition of a key typed event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Invoked when a key has been pressed. See the class description for
     * {@link KeyEvent} for a definition of a key pressed event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent e) {
    }

    /**
     * Invoked when a key has been released. See the class description for
     * {@link KeyEvent} for a definition of a key released event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            swapCanvas();
        }
    }

    private void swapCanvas() {
        if (isInMenu) {
            outerLayeredPane.setLayer(menuPanel, JLayeredPane.DEFAULT_LAYER);
            localGrid.resize(localRowCount, localColumnCount);
            if (server != null) {
                server.resize(localRowCount, localColumnCount);
            }
            gridPanel.requestFocus();
        } else {
            outerLayeredPane.setLayer(menuPanel, JLayeredPane.DRAG_LAYER);
            menuPanel.requestFocus();
        }
        requestFocus();
        isInMenu = !isInMenu;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField colField;
    private com.giuseppelamalfa.gameofliferemastered.ui.GameStatusPanel gameStatusPanel;
    private com.giuseppelamalfa.gameofliferemastered.ui.GridPanel gridPanel;
    private javax.swing.JButton hostCompetitiveGameButton;
    private javax.swing.JTextField hostPortNumber;
    private javax.swing.JButton hostSandboxGameButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton joinGameButton;
    private javax.swing.JTextField maxPlayerCount;
    private com.giuseppelamalfa.gameofliferemastered.ui.MenuPanel menuPanel;
    private javax.swing.JLayeredPane outerLayeredPane;
    private javax.swing.JTextField playerNameField;
    private javax.swing.JTextField rowField;
    private javax.swing.JTextField serverAddress;
    private javax.swing.JTextField serverPortNumber;
    private javax.swing.JTextArea statusLog;
    private javax.swing.JLabel titleLabel;
    private com.giuseppelamalfa.gameofliferemastered.ui.UnitPalette unitPalette;
    private javax.swing.JButton unpauseButton;
    // End of variables declaration//GEN-END:variables
}

class GridSizeDocumentListener implements DocumentListener {

    JTextField rowField;
    JTextField colField;
    ApplicationFrame frame;

    public GridSizeDocumentListener(JTextField rowField, JTextField colField, ApplicationFrame frame) {
        this.rowField = rowField;
        this.colField = colField;
        this.frame = frame;
    }

    @Override
    public void insertUpdate(DocumentEvent ev) {
        try {
            frame.setNextGridSize(Integer.parseInt(rowField.getText()), Integer.parseInt(colField.getText()));
        } catch (Exception e) {
            frame.setNextGridSize(frame.getRowCount(), frame.getColumnCount());
        }
    }

    @Override
    public void removeUpdate(DocumentEvent ev) {
        try {
            frame.setNextGridSize(Integer.parseInt(rowField.getText()), Integer.parseInt(colField.getText()));
        } catch (Exception e) {
            frame.setNextGridSize(frame.getRowCount(), frame.getColumnCount());
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }
}
