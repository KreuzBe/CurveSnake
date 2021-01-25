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


    public GameCreator(Consumer<GameCreator> gameStartAction) {
        initGC(gameStartAction);
    }

    private void initGC(Consumer<GameCreator> gameStartAction) {
        if (frame != null) frame.dispose();
        frame = new JFrame();

        JTabbedPane tabbedPane;
        JPanel tabSingleplayer;
        JButton btnSingleplayer;

        JPanel tabConnect;
        JTextField tfConnect;
        JButton btnConnect;

        JPanel tabCreate;
        JTextArea taCreate;
        JButton btnCreate;


        tabbedPane = new JTabbedPane();

        tabSingleplayer = new JPanel();
        tabbedPane.addTab("Singleplayer", tabSingleplayer);
        tabSingleplayer.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnSingleplayer = new JButton("Play against bot");
        btnSingleplayer.setBounds(0, 0, 200, 500);
        btnSingleplayer.addActionListener((a) -> {
            isMultiplayer = false;
            frame.dispose();
            gameStartAction.accept(this);
        });
        tabSingleplayer.add(btnSingleplayer);

        tabConnect = new JPanel();
        tabbedPane.addTab("Connect", tabConnect);
        tabConnect.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        tfConnect = new JTextField();
        tfConnect.setBounds(0, 0, 200, 50);
        tfConnect.setBorder(BorderFactory.createTitledBorder("Host ip:"));
        tfConnect.setPreferredSize(tfConnect.getSize());
        tabConnect.add(tfConnect);

        btnConnect = new JButton("Connect");
        btnConnect.setBounds(0, 0, 200, 500);
        btnConnect.addActionListener(a -> {
            isMultiplayer = true;
            isServer = false;
            try {
                client = new Client(tfConnect.getText(), Display.DEFAULT_PORT);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
            gameStartAction.accept(this);
        });
        tabConnect.add(btnConnect);

        tabCreate = new JPanel();
        tabbedPane.addTab("Create Server", tabCreate);
        tabCreate.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        taCreate = new JTextArea();
        String text = "";
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                if (netint.getHardwareAddress() == null || !netint.isUp()) continue;
                text += netint.getDisplayName() + "(" + netint.getName() + ")\n";
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    if (!inetAddress.isLinkLocalAddress())
                        text += "   >    " + inetAddress.getHostAddress() + "\n";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        taCreate.setText(text);
        taCreate.setEditable(false);
        tabCreate.add(taCreate);

        btnCreate = new JButton("Connect");
        btnCreate.setBounds(0, 0, 200, 500);
        btnCreate.addActionListener(a -> {
            isMultiplayer = true;
            isServer = true;
            server = new Server(Display.DEFAULT_PORT);
            gameStartAction.accept(this);
        });
        tabCreate.add(btnCreate);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(0, 0, 640, 360);
        frame.setLocationRelativeTo(null);
        frame.add(tabbedPane);
        frame.setResizable(false);

        frame.setVisible(true);
        System.out.println("created");
    }

    public boolean isMultiplayer() {
        return isMultiplayer;
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
