package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        server.addHandle("GET", "/events.html", (request, responseStream) -> {
            Request.info(request);
            try {
                Server.success200(request, responseStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.listen(9999);
    }
}