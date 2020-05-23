package com.lieutri.caroserver;

import components.Room;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author lieut
 */
public class Caro {
    public static final int PORT = 1998;
    public static boolean isServerON = true;
    public static ArrayList<Room> rooms;
    static Boolean setRoom = false;
    static ArrayList<Socket> clientList;
    
    public static void main(String[] args){
        ServerSocket ss = null;
        Socket s = null;
        clientList = new ArrayList<>();
        rooms = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            rooms.add(new Room(i + "", 0));
        }
        
        System.out.println("Server is running....");
        
        try {
            getServerIP();
        } catch (Exception ex) {
            Logger.getLogger(Caro.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            ss = new ServerSocket(PORT);
            while (isServerON) {
                System.out.println("Server is listening.....");
                s = ss.accept();
                System.out.println("Connected to "+s);
                setRoom = false;
                clientList.add(s);
                ObjectOutputStream listPlayer = new ObjectOutputStream(s.getOutputStream());
                listPlayer.writeObject(rooms);
                CellSender cellSender = new CellSender(s);
                cellSender.start();
            }
            System.out.println("Server was stopped!");
            ss.close();
        } catch (IOException e) {
            System.out.println("ERROR(main): " + e);
        }
    }
    
    public static void getServerIP() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            // drop inactive
            if (!networkInterface.isUp())
                continue;

            // smth we can explore
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while(addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                System.out.println(String.format("NetInterface: Name [%s], IP [%s]",
                        networkInterface.getDisplayName(), addr.getHostAddress()));
            }
        }
    }
}