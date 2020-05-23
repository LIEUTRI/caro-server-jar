package com.lieutri.caroserver;

import components.Cell; 
import components.Room;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lieut
 */
public class CellSender extends Thread{
    private Socket s;
    CellSender(Socket s){
        this.s = s;
    }

    @Override
    public void run() {
        ObjectInputStream inputStream = null;
        Cell dataIN=null;
        String ROOMID="";
        try{
            while (Caro.isServerON){
                if (s.isBound()){
                    inputStream = new ObjectInputStream(s.getInputStream());
                    dataIN = (Cell) inputStream.readObject();
                    ROOMID=dataIN.getRoom();
                    System.out.println("server got: "+dataIN.getRow()+","+dataIN.getCol()+" | owner: "+dataIN.getOwner()+" | room: "+dataIN.getRoom());
                }

                assert dataIN != null;
                for (Room room: Caro.rooms){
                    if (room.getRoomID().equals(dataIN.getRoom()) && !Caro.setRoom){
                        Caro.setRoom = true;
                        switch (room.getPlayers()){
                            case 0: Caro.rooms.get(Caro.rooms.indexOf(room)).setPlayers(1); break;
                            case 1: Caro.rooms.get(Caro.rooms.indexOf(room)).setPlayers(2); break;
                        }
                    }
                }

                for (Socket item: Caro.clientList){
                    ObjectOutputStream output = new ObjectOutputStream(item.getOutputStream());
                    output.writeObject(dataIN);
                }
            }
        }catch (EOFException e){
            System.out.println(s.getRemoteSocketAddress() + " was disconnected | port: "+s.getPort());
            for (Room r : Caro.rooms) {
                if (r.getRoomID().equals(ROOMID)) {
                    switch (r.getPlayers()) {
                        case 1:
                            Caro.rooms.get(Caro.rooms.indexOf(r)).setPlayers(0);
                            break;
                        case 2:
                            Caro.rooms.get(Caro.rooms.indexOf(r)).setPlayers(1);
                            break;
                    }
                }
            }
            try {
                s.close();
            } catch (IOException ex) {
                System.out.println("ERROR: Khong the close socket!");
            }
            Caro.clientList.remove(s);
        }catch (IOException | ClassNotFoundException e){
            System.out.println("ERROR!!!");
            Caro.clientList.remove(s);
        }
    }
}
