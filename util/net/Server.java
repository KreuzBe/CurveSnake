package util.net;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.function.Consumer;

public class Server {
    private Thread listeningThread;

    private int port;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isRunning;

    private Consumer<Object> inputConsumer;

    public void setInputConsumer(Consumer<Object> inputConsumer) {
        this.inputConsumer = inputConsumer;
    }

    public Server(int port) {
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Waiting for connection...");
            clientSocket = serverSocket.accept();
            System.out.println("Connected!");

            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            listeningThread = new Thread(this::listen, "Listening Thread");
            listeningThread.setDaemon(true);
            listeningThread.start();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    public ObjectOutputStream getOut() {
        return out;
    }

    public void send(Object o) throws IOException {
        if (!serverSocket.isClosed() && !clientSocket.isClosed()) {
            try {
                out.writeObject(o);
                out.flush();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                //System.exit(0);
            }

        }
    }

    private void listen() { // TODO LISTEN AS LONG AS YOU CAN
        isRunning = true;
        while (clientSocket.isConnected()) {

            try {
                if (inputConsumer != null) {
                    inputConsumer.accept(in.readObject());
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println(e.getMessage());
                System.out.println("Connection lost");
                //  System.exit(0);
                isRunning = false;
                break;
            }
        }
        try {
            in.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        isRunning = false;
        try {
            in.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
