package org.example;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.FilterOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class Request {
    private String tittle;
    private String fullPath;
    private String path;
    private String requestMethod;
    private String httpVersion;
    private String body;
    private String query;
    private Map<String, String> params;

    public Request(String tittle, String fullPath, String requestMethod, String httpVersion, String body) {
        this.tittle = tittle;
        this.fullPath = fullPath;
        this.requestMethod = requestMethod;
        this.httpVersion = httpVersion;
        this.body = body;

        if (fullPath.contains("?")) {
            path = fullPath.substring(0, fullPath.indexOf("?"));
            query = fullPath.substring(fullPath.indexOf("?")+1);
            params = URLEncodedUtils.parse(this.query, StandardCharsets.UTF_8).stream().collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        }

    }

    public String getTittle() {
        return tittle;
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getPath() {
        return path;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getQueryParams(String name) {
        return params.get(name);
    }

    public Map<String, String> getParams() {
        return params;
    }
    public static void info(Request request){
        System.out.printf("Метод: %s\nПолный путь: %s\nПуть: %s\nПротокол: %s\nЗаголовки", request.getRequestMethod(), request.getFullPath(), request.getPath(),
                request.getHttpVersion(), request.getTittle());
        System.out.println("ПАРАМЕТРЫ");
        request.getParams().entrySet().forEach(System.out::println);
    }
}

