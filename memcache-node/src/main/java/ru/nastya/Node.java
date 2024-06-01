package ru.nastya;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Random;

public class Node {
    private String host;
    private int port;
    private HashMap<Integer, String> routingTable;

    public Node(String host) {
        this.host = host;
        this.routingTable = new HashMap<>();
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            //Выбор порта
            this.port = serverSocket.getLocalPort();
            System.out.println("Node listening on " + host + ":" + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                //Поток для обработки подключения
                Thread clientThread = new Thread(new ConnectionHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ConnectionHandler implements Runnable {
        private Socket clientSocket;

        public ConnectionHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            BufferedReader in = null;
            PrintWriter out = null;
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                // Сообщение от клиента
                String message = in.readLine();
                if (message != null) {
                    HashMap<String, Object> jsonMessage = parseJson(message);
                    String messageType = (String) jsonMessage.get("type");

                    if (messageType != null && messageType.equals("Discovery")) {
                        // Отправляем таблицу маршрутизации
//                        out.println(new ObjectMapper().writeValueAsString(routingTable));
                    } else {
                        System.err.println("Received unexpected message. Closing connection.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } /*finally {

                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
        }

//        private <ObjectMapper> HashMap<String, Object> parseJson(String json) throws IOException {
//            //  ObjectMapper objectMapper = new ObjectMapper();
//
//
//        }

        public static void main(String[] args) {
            Node node = new Node("localhost");
            node.start();
        }
    }

    private HashMap<String, Object> parseJson(String message) {
        return null;
    }
}

