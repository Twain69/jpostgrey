package com.flegler.jpostgrey;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class JPostGrey {

    private static final Logger LOG = Logger.getLogger(JPostGrey.class);

    public static void main(String[] args) {

        Util util = new Util();
        LOG.info("jPostgrey started. Version: " + util.getVersion());

        ServerSocket inputSocket = null;
        try {
            inputSocket = new ServerSocket(Settings.INSTANCE.getConfig().port(), 0,
                    InetAddress.getByName(Settings.INSTANCE.getConfig().bindAddress()));
            LOG.info("Socket started successfully on port " + Settings.INSTANCE.getConfig().port());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        while (true) {
            try {
                Socket connectionSocket = inputSocket.accept();
                InputThread input = new InputThread(connectionSocket, UUID.randomUUID());
                input.start();
            } catch (IOException e) {
                for (StackTraceElement element : e.getStackTrace()) {
                    LOG.error(element.toString());
                }
                System.exit(-1);
            } catch (java.lang.OutOfMemoryError e) {
                for (StackTraceElement element : e.getStackTrace()) {
                    LOG.error(element.toString());
                }
                System.gc();
            }
        }
    }
}