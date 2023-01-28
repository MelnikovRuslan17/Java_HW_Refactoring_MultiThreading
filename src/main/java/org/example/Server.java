package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;


import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static java.lang.System.out;

public class Server {
    private Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    private final ExecutorService executorService;
    private ServerSocket serverSocket;

    public Server() {
        executorService = Executors.newFixedThreadPool(64);
    }

    public void listen(int port) {
        out.println("Запускаем сервер...");
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                final var socket = serverSocket.accept();
                executorService.submit(() -> {
                    this.connection(socket);
                });

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void connection(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())) {
            while (true) {
                // read only request line for simplicity
                // must be in form GET /path HTTP/1.1
                final var requestLine = in.readLine();
                if (requestLine == null || requestLine.trim().length() == 0) {
                    break;
                }
                final var parts = requestLine.split(" ");

                if (parts.length != 3) {
                    socket.close();
                    break;
                }

                String requestMethod = parts[0];
                String fullPath = parts[1];
                String httpVersion = parts[2];
                StringBuilder titles = new StringBuilder(" ");
                StringBuilder body = new StringBuilder(" ");

                while (in.ready()){
                    String line = in.readLine();
                    if(line.equals("\r\n")){
                        break;
                    }
                    titles.append(line);
                }
                while (in.ready()){
                    String line  = in.readLine();
                    body.append(line);
                }
                Request request = new Request(requestMethod, fullPath, httpVersion, titles.toString(), body.toString());
                if((!(handlers.containsKey(request.getRequestMethod()))) && (!(handlers.get(request.getRequestMethod()).containsKey(request.getPath())))){
                    notFound404(out);
                    break;
                }else{
                    handlers.get(request.getRequestMethod()).get(request.getPath()).handle(request, out);
                }
            }
        }catch (IOException e){
            e.printStackTrace();


        }
    }
    public void addHandle(String requestMethod, String parth, Handler handler){
        if(handlers.containsKey(requestMethod)){
            handlers.get(requestMethod).put(parth, handler);
        }else {
            handlers.put(requestMethod, new ConcurrentHashMap<>());
            handlers.get(requestMethod).put(parth,handler);
        }
    }

    public static void success200(Request request, BufferedOutputStream out) throws IOException {
        final var filePath = Path.of(".", "public", request.getPath());
        final var mimeType = Files.probeContentType(filePath);
        try {
            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void classicPath(Request request, BufferedOutputStream out) throws IOException {
        final var filePath = Path.of(".", "public", request.getPath());
        final var mimeType = Files.probeContentType(filePath);
        try {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void notFound404(OutputStream out) {
        try {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
