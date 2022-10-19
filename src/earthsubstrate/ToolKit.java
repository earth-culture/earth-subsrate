/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package earthsubstrate;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Christopher Brett
 */
public class ToolKit {

    protected static JSONObject getJSONFromHttpExchangeQuery(HttpExchange exchange) {
        if (exchange.getRequestURI().getQuery() == null) {
            return new JSONObject();
        } else {
            Map<String, String> jsonMap = new HashMap<>();
            for (String param : exchange.getRequestURI().getQuery().split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    jsonMap.put(entry[0], entry[1]);
                } else {
                    jsonMap.put(entry[0], "");
                }
            }
            return new JSONObject(jsonMap);
        }
    }

    protected static JSONObject getJSONFromHttpExchangeBody(HttpExchange exchange) throws ParseException, UnsupportedEncodingException, IOException {
        StringBuilder buffer;
        try ( InputStreamReader inputStreamReader = new InputStreamReader(exchange.getRequestBody(), "utf-8");  BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            int character;
            buffer = new StringBuilder();
            while ((character = bufferedReader.read()) != -1) {
                buffer.append((char) character);
            }
        }
        JSONParser jSONParser = new JSONParser();
        return buffer.isEmpty() ? new JSONObject() : (JSONObject) jSONParser.parse(buffer.toString());
    }

    protected static JSONObject mergeJSONObjects(JSONObject json1, JSONObject json2) {
        json1.putAll(json2);
        return json1;
    }

    protected static void sendApiJSONResponse(ApiResponseData response, HttpExchange exchange) throws IOException {
        String responseBody = response.getResponse().toJSONString();
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set(HttpConstants.HEADER_ALLOW_ACCESS_CONTROL_ORIGIN, "*");
        responseHeaders.set(HttpConstants.HEADER_ACCESS_CONTROL_ALLOW_HEADERS, "Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, AUTH_TOKEN");
        responseHeaders.set(HttpConstants.HEADER_CONTENT_TYPE, String.format(HttpConstants.FORMAT_JSON, HttpConstants.CHARSET_UTF8));
        if (response.getExtraHeaders() != null) {
            for (Map.Entry<String, String> entry : response.getExtraHeaders().entrySet()) {
                responseHeaders.set(entry.getKey(), entry.getValue());
            }
        }
        final byte[] rawResponseBody = responseBody.getBytes(HttpConstants.CHARSET_UTF8);
        exchange.sendResponseHeaders(response.getHttpStatus(), rawResponseBody.length);
        exchange.getResponseBody().write(rawResponseBody);
    }

    protected static void sendFileResponse(File file, HttpExchange exchange) throws IOException {
        byte[] bytesOfFile = new byte[(int) file.length()];
        try ( FileInputStream fileInputStream = new FileInputStream(file)) {
            fileInputStream.read(bytesOfFile);
        }
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set(HttpConstants.HEADER_ALLOW_ACCESS_CONTROL_ORIGIN, "*");
        responseHeaders.set(HttpConstants.HEADER_CONTENT_TYPE, FileContentMimeTypeDetector.detect(file));
        responseHeaders.set(HttpConstants.HEADER_CONTENT_LENGTH, String.valueOf(bytesOfFile.length));
        responseHeaders.set(HttpConstants.HEADER_CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");
        try (OutputStream outputStream = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(HttpConstants.STATUS_OK, bytesOfFile.length);
            outputStream.write(bytesOfFile);
            outputStream.flush();
        }catch(Exception e){
            e.printStackTrace(System.out);
        }
    }

    protected static void sendMethodOptionsResponse(HttpExchange exchange) throws IOException {
        final Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set(HttpConstants.HEADER_ALLOW_ACCESS_CONTROL_ORIGIN, "*");
        responseHeaders.set(HttpConstants.HEADER_ACCESS_CONTROL_ALLOW_METHODS, HttpConstants.ALLOWED_METHODS);
        responseHeaders.set(HttpConstants.HEADER_ACCESS_CONTROL_ALLOW_HEADERS, "Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, AUTH_TOKEN");
        exchange.sendResponseHeaders(HttpConstants.STATUS_NO_CONTENT, HttpConstants.NO_RESPONSE_LENGTH);
    }

    protected static void sendDefaultResponse(HttpExchange exchange) throws IOException {
        final Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set(HttpConstants.HEADER_ALLOW_ACCESS_CONTROL_ORIGIN, "*");
        responseHeaders.set(HttpConstants.HEADER_ACCESS_CONTROL_ALLOW_METHODS, HttpConstants.ALLOWED_METHODS);
        responseHeaders.set(HttpConstants.HEADER_ACCESS_CONTROL_ALLOW_HEADERS, "Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, AUTH_TOKEN");
        exchange.sendResponseHeaders(HttpConstants.STATUS_METHOD_NOT_ALLOWED, HttpConstants.NO_RESPONSE_LENGTH);
    }
    
    protected static Long getLongFromValue(Object value){
        if(value instanceof Integer || value instanceof Long){
            return (Long) value;
        }else{
            return Long.parseLong((String) value);
        }
    }
}
