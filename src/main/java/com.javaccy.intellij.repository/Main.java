package com.javaccy.intellij.repository;

import org.eclipse.jetty.server.Server;

public class Main{

    private static int port = 6868;

    public static void main(String[] args) throws Exception {
        if (args.length != 0) {
            try {
                Integer p = Integer.valueOf(args[0]);
                if (p > 1024 && p < 65536) {
                    port = p;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Server server = new Server(port);
        server.setHandler(new MainHandler());
        server.start();
        server.dumpStdErr();
        server.join();
    }
}