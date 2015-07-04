package ru.mr123150.conn;

import java.io.*;
import java.net.*;
import java.util.Vector;

/**
 * Created by victorsnesarevsky on 13.06.15.
 */
public class Connection{
    InetAddress serverAddress=null;
    InetAddress address=null;
    protected int port;
    protected boolean isServer;

    //protected ServerSocket ss = null;
    DatagramSocket socket;

    final int MAXSIZE=1024;

    public Vector<User> users=new Vector<>();

    public Connection(int port) throws IOException{
        this.serverAddress=InetAddress.getByName("255.255.255.255");
        this.port=port;
        isServer=true;
        socket=new DatagramSocket(port);
        users.add(new User());
    }

    public Connection(String address, int port) throws IOException{
        this.serverAddress=InetAddress.getByName(address);
        this.port=port;
        isServer=false;
        socket=new DatagramSocket();
        this.address=InetAddress.getLocalHost();
    }

    public boolean isServer(){
        return isServer;
    }

    public String address(){
        return address.getHostAddress();
    }

    public void send(String msg,boolean signature) throws IOException{
        DatagramPacket packet;
        if(signature) msg+=(";"+(users.isEmpty()?-1:users.get(0).id()));
        packet=new DatagramPacket(msg.getBytes(),msg.length(),serverAddress,port);

        socket.send(packet);
    }

    public String receive() throws IOException{
        DatagramPacket packet = new DatagramPacket(new byte[MAXSIZE],MAXSIZE);
        socket.receive(packet);
        return new String(packet.getData());
    }

    public int getUserById(int id){
        int i=-1;
        for(User user:users){
            ++i;
            if(user.id()==id) return i;
        }
        return i;
    }
}
