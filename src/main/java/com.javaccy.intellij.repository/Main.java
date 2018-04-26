package com.javaccy.intellij.repository;

import org.eclipse.jetty.server.Server;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

public class Main{

    // 上传文件存储目录
    private static String host;
    private static final String UPLOAD_DIRECTORY = "upload";
    private static String logPath = null;
    private static PrintStream logger;
    private static int port = 6868;
    private static String path = null;

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

        if (args.length > 1) {
            host = args[1];
        }

        if (args.length > 2) {
            path = args[2];
        }

        if (args.length > 3) {
            logPath = args[3];
        }
        log("started.......");
        Server server = new Server(port);
        server.setHandler(new MainHandler());
        server.start();
        server.dumpStdErr();
        server.join();
    }

    public static int getPort() {
        return port;
    }

    public PrintStream getLogger() throws IOException {
        if (logPath != null && !"0".equals(logPath)) {
            logger = new PrintStream(logPath);
            System.setOut(logger);
        } else {
            logger = System.out;
        }

        return logger;
    }

    public static void log(String str) {
        if (logger != null) {
            logger.println(str);
        }
    }


    public static File getUploadDir() {
        //File dir = new File(new File("").getAbsolutePath() + File.separator + UPLOAD_DIRECTORY);
        if (path == null || !path.startsWith("/") || "0".equals(path)) {
            //path = Main.class.getResource("/").getPath();
            path = new File("").getAbsolutePath();
        }

        File dir = new File(path + File.separator + UPLOAD_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String getBasePath(){
        if (host != null && !"0".equals(host)) {
            return host;
        } else {
            return "http://plugins.idea.javaccy.giize.com";
        }
        //return "http://192.168.99.186:6868";
    }
}