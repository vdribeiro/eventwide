/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ewserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Diogo
 */
public class DBManager {

    private Connection con = null;
    DBCompanies companies = null;
    DBSessions sessions = null;
    DBAccounts accounts = null;
    DBEvents events = null;
    
    DBManager() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection("jdbc:oracle:thin:@oraalu.fe.up.pt:1521:ALU", "ei07171", "ei07171");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: Could not connect to remote database.");
            System.exit(1);
        }
        companies = new DBCompanies(con);
        sessions = new DBSessions(con);
        accounts = new DBAccounts(con);
        events = new DBEvents(con);
        System.out.println("Successfully connected to the remote database.");
    }

   
}
