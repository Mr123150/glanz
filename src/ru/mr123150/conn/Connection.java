package ru.mr123150.conn;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Created by victorsnesarevsky on 13.06.15.
 */
public class Connection{
    protected InetAddress server;
    protected InetAddress address;
    protected int port;
    protected boolean isServer;
    protected boolean isBroadcast;

    protected ServerSocket ss = null;

    public Vector<User> users=new Vector<User>();

    public Connection(int port) throws IOException{
        this.server =null;
        this.address=InetAddress.getLocalHost();
        this.port=port;
        isServer=true;
        ss=new ServerSocket(port);
        users.add(new User());
    }

    public Connection(int port, boolean isServer) throws IOException{
        this.server=null;
        this.address=InetAddress.getLocalHost();
        this.port=port;
        this.isServer=isServer;
        if(isServer){
            ss=new ServerSocket(port);
            users.add(new User());
        }
        else{
            this.isBroadcast=true;
        }
    }

    public Connection(String server, int port) throws IOException{
        this.server=InetAddress.getByName(server);
        this.address=InetAddress.getLocalHost();
        this.port=port;
        isServer=false;
    }

    public boolean isServer(){
        return isServer;
    }

    public String getAddress(){

        return address.getHostAddress();
    }

    public void send(String msg,boolean signature) throws IOException{

        DataOutputStream out;
        Socket s;
        if(signature) msg+=(";"+(users.isEmpty()?-1:users.get(0).id()));

        if (isServer){
            s = ss.accept();
            out = new DataOutputStream(s.getOutputStream());
            out.flush();
            out.writeUTF(msg);
            out.flush();
            s.close();
        }
        else if(isBroadcast){
            for(User user:users.subList(1,users.size())) {
                s = new Socket(user.address(), port);
                OutputStream os = s.getOutputStream();
                out = new DataOutputStream(os);
                out.flush();
                out.writeUTF(msg);
                out.flush();
                s.close();
            }
        }
        else{
            s = new Socket(address, port);
            OutputStream os = s.getOutputStream();
            out = new DataOutputStream(os);
            out.flush();
            out.writeUTF(msg);
            out.flush();
            s.close();
        }
    }

    public String receive() throws IOException{
        Socket s;
        DataInputStream in;
        if(isServer){
            s=ss.accept();
            in=new DataInputStream(s.getInputStream());
        }
        else{
            s=new Socket(server,port);
            InputStream is=s.getInputStream();
            in=new DataInputStream(is);
        }
        String str="";
        try{str=in.readUTF();}
        catch (EOFException e){}
        s.close();
        return str;
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
