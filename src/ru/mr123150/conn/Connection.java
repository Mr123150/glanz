package ru.mr123150.conn;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by victorsnesarevsky on 13.06.15.
 */
public class Connection{
    protected String host;
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
    }

    public boolean isHost(){
        return isHost;
    }

    public void send(String msg) {

        DataOutputStream out;
        try {
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
            out.writeUTF(msg);
            s.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
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
        String str=in.readUTF();
        s.close();
        return str;
    }
}
