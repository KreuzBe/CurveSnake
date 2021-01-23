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
    private PrintWriter out;
    private BufferedReader in;

    private Consumer<String> inputConsumer;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            socket = new Socket(host, port);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            listeningThread = new Thread(this::listen, "Listening Thread");
            listeningThread.setDaemon(true);
            listeningThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setInputConsumer(Consumer<String> inputConsumer) {
        this.inputConsumer = inputConsumer;
    }

    private void listen() {
        boolean isRunning = true;
        while (isRunning) {

            if (!socket.isConnected())
                break;
            try {
                if (inputConsumer != null)
                    inputConsumer.accept(in.readLine());
                else
                    System.out.println(">> " + in.readLine());
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String s) {
        out.println(s);
    }

    public PrintWriter getPrintWriter() {
        return out;
    }
}
