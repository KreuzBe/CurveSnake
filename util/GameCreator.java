package util;

import util.net.Client;
import util.net.Server;
import visual.Display;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.function.Consumer;

public class GameCreator {
    private JFrame frame;
    private boolean isMultiplayer;
    private boolean isServer;
    private Client client;
    private Server server;
    private int lastTab = 0;

    private String connectedIp = "";


    public GameCreator(Consumer<GameCreator> gameStartAction) {
        initGC(gameStartAction);
    }


    private void initGC(Consumer<GameCreator> gameStartAction) {
        if (client != null) {
            client.stop();
            client = null;
        }
        if (server != null) {
            server.stop();
            server = null;
        }
        if (frame != null && frame.isActive()) frame.dispose();
        frame = new JFrame();

        JTabbedPane tabbedPane;
        JPanel tabSingleplayer;
        JButton btnSingleplayer;

        JPanel tabConnect;
        JTextField tfConnect;
        JButton btnConnect;

        JPanel tabCreate;
        JTextArea taCreate;
        JCheckBox btnToggle;
        JButton btnCreate;


        tabbedPane = new JTabbedPane();
        tabbedPane.setFocusable(false);


        tabSingleplayer = new JPanel();
        tabbedPane.addTab("Singleplayer", tabSingleplayer);
        tabSingleplayer.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnSingleplayer = new JButton("Play against bot");
        btnSingleplayer.setBounds(0, 0, 200, 500);
        btnSingleplayer.addActionListener((a) -> {
            isMultiplayer = false;
            lastTab = 0;
            frame.dispose();
            gameStartAction.accept(this);
        });
        tabSingleplayer.add(btnSingleplayer);

        tabConnect = new JPanel();
        tabbedPane.addTab("Connect", tabConnect);
        tabConnect.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        tfConnect = new JTextField();
        tfConnect.setText(connectedIp);
        tfConnect.setBounds(0, 0, 200, 50);
        tfConnect.setBorder(BorderFactory.createTitledBorder("Host ip:"));
        tfConnect.setPreferredSize(tfConnect.getSize());
        tabConnect.add(tfConnect);

        btnConnect = new JButton("Connect");
        btnConnect.setBounds(0, 0, 200, 500);
        btnConnect.addActionListener(a -> {
            isMultiplayer = true;
            isServer = false;
            lastTab = 1;
            try {
                client = new Client(tfConnect.getText(), Display.DEFAULT_PORT);
                connectedIp = tfConnect.getText();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("GC!!!");
                System.exit(0);
            }
            frame.dispose();
            gameStartAction.accept(this);
        });
        tabConnect.add(btnConnect);

        tabCreate = new JPanel();
        tabbedPane.addTab("Create Server", tabCreate);
        tabCreate.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        taCreate = new JTextArea();
        taCreate.setEditable(false);

        btnCreate = new JButton("Create server");
        btnCreate.setBounds(0, 0, 200, 500);
        btnCreate.addActionListener(a -> {
            isMultiplayer = true;
            isServer = true;
            lastTab = 2;
            server = new Server(Display.DEFAULT_PORT);
            frame.dispose();
            gameStartAction.accept(this);
        });
        tabCreate.add(btnCreate);

        btnToggle = new JCheckBox();
        btnToggle.setBounds(0, 0, 2000, 500);
        btnToggle.setToolTipText("Toggle IP-view");
        btnToggle.addActionListener(a -> {
            String string = "";
            if (btnToggle.isSelected()) {

                try {
                    Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                    for (NetworkInterface netint : Collections.list(nets)) {
                        if (netint.getHardwareAddress() == null || !netint.isUp()) continue;
                        string += netint.getDisplayName() + "(" + netint.getName() + ")\n";
                        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                            if (!inetAddress.isLinkLocalAddress())
                                string += "   >    " + inetAddress.getHostAddress() + "\n";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            taCreate.setText(string);
        });
        tabCreate.add(btnToggle);
        tabCreate.add(taCreate);

        tabbedPane.setSelectedIndex(lastTab);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(0, 0, 320, 360);
        frame.setLocationRelativeTo(null);
        frame.add(tabbedPane);
        frame.setResizable(false);

        frame.setVisible(true);
    }

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public boolean isServer() {
        return isServer;
    }

    public Client getClient() {
        return client;
    }

    public Server getServer() {
        return server;
    }

    public static void createGame(GameCreator gameCreator, Consumer<GameCreator> creatorConsumer) {
        System.out.println("Creating Game");
        if (gameCreator == null) {
            new GameCreator(creatorConsumer);
        } else {
            gameCreator.initGC(creatorConsumer);
        }

    }

}
