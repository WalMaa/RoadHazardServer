package com.server;

import com.sun.net.httpserver.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;

public class Server {

    public static void main(String[] args) throws Exception {

        try {
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);

            SSLContext sslContext = serverSSLContext("keystore.jks", "Pass123");
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    InetSocketAddress remote = params.getClientAddress();
                    SSLContext c = getSSLContext();
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });
            HttpContext context = server.createContext("/warning", new HandleWarnings());

            UserAuthenticator userAuth = new UserAuthenticator(null);
            context.setAuthenticator(userAuth);
            server.createContext("/registration", new RegistrationHandler(userAuth));
            server.setExecutor(null);
            server.start();
        } catch (FileNotFoundException e) {
            System.out.println("Certificate not found!");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static SSLContext serverSSLContext(String file, String password) throws Exception {
        char[] passphrase = password.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(file), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ssl;
    }

    /*
     * private static SSLContext serverSSlContext() throws Exception {
     * char[] passphrase = "Pass123".toCharArray();
     * KeyStore ks = KeyStore.getInstance("JKS");
     * ks.load(new FileInputStream("keystore.jks"), passphrase);
     * 
     * KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
     * kmf.init(ks, passphrase);
     * TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
     * tmf.init(ks);
     * 
     * SSLContext ssl = SSLContext.getInstance("TLS");
     * ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
     * return ssl;
     * }
     */

}