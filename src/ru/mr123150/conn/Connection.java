package ru.mr123150.conn;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by victorsnesarevsky on 13.06.15.
 */
public class Connection{
    protected String host;
    protected String address=null;
    protected int port;
    protected boolean isHost;

    ServerSocket ss = null;

    public Connection(int port) throws IOException{
        this.host=null;
        this.port=port;
        isHost=true;
        ss=new ServerSocket(port);
    }

    public Connection(String host, int port) throws IOException{
        this.host=host;
        this.port=port;
        isHost=false;
        Socket s=new Socket(host,port);
        address=s.getLocalAddress().toString().substring(1);
        s.close();
    }

    public boolean isHost(){
        return isHost;
    }

    public String getAddress(){

        return address;
    }

    public void send(String msg) throws IOException{

        DataOutputStream out;

        Socket s;
        if (isHost){
            s = ss.accept();
            out = new DataOutputStream(s.getOutputStream());
        }
        else{
            s = new Socket(host, port);
            OutputStream os = s.getOutputStream();
            out = new DataOutputStream(os);
        }
        out.flush();
        out.writeUTF(msg);
        out.flush();
        s.close();
    }

    public String receive() throws IOException{
        Socket s;
        DataInputStream in;
        if(isHost){
            s=ss.accept();
            in=new DataInputStream(s.getInputStream());
        }
        else{
            s=new Socket(host,port);
            InputStream is=s.getInputStream();
            in=new DataInputStream(is);
        }
        String str="";
        try{str=in.readUTF();}
        catch (EOFException e){}
        s.close();
        return str;
    }
}
