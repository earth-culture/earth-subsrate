/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package earthsubstrate;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author Christopher Brett
 */
public class SecureApiRequestListener extends Thread {

    @Override
    public void run() {
        if (ServerEnvironmentVariables.IN_TEST_MODE) { //HTTP
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(ServerEnvironmentVariables.INCOMING_PORT), 0);
                //server.createContext("/", null);
                server.createContext("/Culture", new CultureRequestHandler());
                server.setExecutor(new ThreadPoolExecutor(6, 24, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100)));
                server.start();
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        } else { //HTTPS
            try {
                KeyStore keyStore = KeyStore.getInstance("jks");
                try ( FileInputStream fileInputStream = new FileInputStream(new File("RESTRICTED/NULL"))) {
                    keyStore.load(fileInputStream, ServerEnvironmentVariables.KEY_STORE_PASSWORD.toCharArray());
                }
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                keyManagerFactory.init(keyStore, ServerEnvironmentVariables.KEY_PAIR_PASSWORD.toCharArray());
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
                trustManagerFactory.init(keyStore);
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom()); //can use null for last parameter

                HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(ServerEnvironmentVariables.INCOMING_PORT), 0);
                httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                    @Override
                    public void configure(HttpsParameters params) {
                        try {
                            SSLContext context = getSSLContext();
                            SSLEngine engine = context.createSSLEngine();
                            params.setNeedClientAuth(false);
                            params.setCipherSuites(engine.getEnabledCipherSuites());
                            params.setProtocols(engine.getEnabledProtocols());
                            SSLParameters sslParameters = context.getSupportedSSLParameters();
                            params.setSSLParameters(sslParameters);
                        } catch (Exception ex) {
                            System.out.println("Failed to create HTTPS port");
                        }
                    }
                });
                //httpsServer.createContext("/", null);
                httpsServer.createContext("/Culture", new CultureRequestHandler());
                httpsServer.setExecutor(new ThreadPoolExecutor(6, 24, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100)));
                httpsServer.start();
            } catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException e) {
                System.out.println("Secure Server Failed To Be Created");
                e.printStackTrace(System.out);
            }
        }
    }
}

