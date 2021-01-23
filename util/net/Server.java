package util.net;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class Server {
    private Thread listeningThread;

    private int port;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private Consumer<Object> inputConsumer;

    public void setInputConsumer(Consumer<Object> inputConsumer) {
        this.inputConsumer = inputConsumer;
    }

    public Server(int port) {
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Waiting for connection on " + InetAddress.getLocalHost().getHostAddress() + ":" + port);
            clientSocket = serverSocket.accept();
            System.out.println("Connected!");

            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            listeningThread = new Thread(this::listen, "Listening Thread");
            listeningThread.setDaemon(true);
            listeningThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    public void send(Object o) throws IOException {
        out.writeObject(o);
        out.flush();
    }

    private void listen() { // TODO LISTEN AS LONG AS YOU CAN
        boolean isRunning = true;
        while (clientSocket.isConnected()) {

            try {
                if (inputConsumer != null) {
                    inputConsumer.accept(in.readObject());
                    // System.out.println(in.readObject().toString());
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println(e.getCause() + " " + e.getMessage());
                System.out.println("Connection lost");
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
}
