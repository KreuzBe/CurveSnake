package util.net;

import visual.Display;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.function.Consumer;

public class Client {

    private Thread listeningThread;
    private String host;
    private int port;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isRunning;

    private Consumer<Object> inputConsumer;
    private Display display;

    public Client(String host, int port) throws IOException {
        this.host = host;
        this.port = port;

        socket = new Socket(host, port);

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        listeningThread = new Thread(this::listen, "Listening Thread");
        listeningThread.setDaemon(true);
        listeningThread.start();
    }

    public void setInputConsumer(Consumer<Object> inputConsumer) {
        this.inputConsumer = inputConsumer;
    }

    private void listen() {
        isRunning = true;
        while (true) {

            if (!socket.isConnected())
                break;
            try {
                if (inputConsumer != null)
                    inputConsumer.accept(in.readObject());
            } catch (IOException | ClassNotFoundException e) {
                System.err.println(e.getMessage());
                System.out.println("Connection lost");
                if (display != null)
                    display.gameOver(null, 0);
                //  System.exit(0);
                isRunning = false;
                break;
            }
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (display != null)
                display.gameOver(null, 0);
        }
    }

    public void send(Object o) throws IOException {
        if (!socket.isClosed()) {
            try {
                out.writeObject(o);
                System.out.println(o.toString());
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Connection lost");
                stop();
                isRunning = false;
            }
        }
    }

    public void stop() {
        isRunning = false;
        try {
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public Display getDisplay() {
        return display;
    }
}
