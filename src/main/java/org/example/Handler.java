package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;

@FunctionalInterface
public interface Handler {
   void handle(Request request, BufferedOutputStream responseStream);

}
