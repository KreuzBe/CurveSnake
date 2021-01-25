package util.net;

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
                System.exit(0);
                isRunning = false;
                break;
            }
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(Object o) throws IOException {
        out.writeObject(o);
        out.flush();
    }

    public void stop() {
        isRunning = false;
    }
}
