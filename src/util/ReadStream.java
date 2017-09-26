package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadStream implements Runnable {
    String name;
    InputStream is;
    Thread thread; 
    
    private boolean silent;
    
    public ReadStream(String name, InputStream is) {
    	this(name,is,false);
    }      
    public ReadStream(String name, InputStream is,boolean silent) {
        this.name = name;
        this.is = is;
        this.silent=silent;
    }      
    public void start () {
        thread = new Thread (this);
        thread.start ();
    }       
    public void run () {
        try {
            InputStreamReader isr = new InputStreamReader (is);
            BufferedReader br = new BufferedReader (isr);   
            while (true) {
                String s = br.readLine ();
                if (s == null) break;
                if(!silent) System.out.println ("[" + name + "] " + s);
            }
            is.close ();    
        } catch (Exception ex) {
            System.out.println ("Problem reading stream " + name + "... :" + ex);
            ex.printStackTrace ();
        }
    }
}