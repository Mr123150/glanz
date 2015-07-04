package ru.mr123150.conn;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by victorsnesarevsky on 05.07.15.
 */
public class MulticastConnection extends Connection{

    int ttl=4;
    MulticastSocket socket;

    public MulticastConnection(String address, int port, boolean isServer) throws IOException{
        this.serverAddress=InetAddress.getByName(address);
        if(serverAddress.isMulticastAddress()){
            System.out.println("Multicast");
        }
        this.isServer=isServer;
        this.port=port;
        if(isServer) {
            socket = new MulticastSocket();
        }
        else{
            socket=new MulticastSocket(port);
            socket.joinGroup(serverAddress);
        }
        socket.setTimeToLive(ttl);
    }
}
