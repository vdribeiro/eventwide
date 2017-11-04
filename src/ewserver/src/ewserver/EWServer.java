/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ewserver;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Diogo
 */
public class EWServer {
    
    static DBManager dbm = new DBManager();
    static SecureRandom random = new SecureRandom();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
     
            // cria instancia do SessionsMaintenance para apagar sessions que tenham expirado
            DBSessionsMaintenance sm = new DBSessionsMaintenance();
            Timer timer = new Timer();   
            timer.scheduleAtFixedRate(sm, 60000, 60000);
            
            System.out.println("TimerTask running.");
            
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 8);
            
            server.createContext("/accounts", new AccountsHandler());
            server.createContext("/companies", new CompaniesHandler());
            server.createContext("/events", new EventsHandler());
            server.start();
  
    }
}
