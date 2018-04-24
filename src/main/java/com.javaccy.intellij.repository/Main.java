package com.javaccy.intellij.repository;

import org.eclipse.jetty.server.Server;

public class Main{

    public static void main(String[] args) throws Exception {
        Server server = new Server(6868);
        server.setHandler(new MainHandler());
        server.start();
        server.dumpStdErr();
        server.join();
    }
}