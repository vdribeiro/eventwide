/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ewserver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Diogo
 */
public class DBSessions {
        
    private Connection con = null;
    
    DBSessions(Connection c) {
        con = c;
    }
    
    /**
     * Retorna o username cuja sessão é identificada pela token indicada.
     * @param token
     * @return 
     */
    public String getUsername(String token) {
        String username = null;
        try {
            //SQL Query - pesquisa sessoes com a token indicada
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT username FROM sessao WHERE token = '" + token + "'");
            
            //Se não encontrar resultados..
            if(!rs.next())
                return null;
            
            //Fetch do username
            username = rs.getString("USERNAME");
            
        } catch (SQLException ex) {
            //Logger.getLogger(DBSessions.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: Get Session's User.");
            return null;
        }
        return username;
    }
    
    /**
     * Verifica se a token indicada já foi atribuída a outra sessão.
     * Em caso de erro (excepção) retorna true.
     * @param token
     * @return 
     */
    /*public boolean existsToken(String token) {
        boolean tokenFound = true;
        try {
            //SQL Query
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM sessao WHERE token = '" + token + "'");
            
            //se não forem encontrados resultados, a token não existe na BD.
            if(!rs.next())
                tokenFound = false;          
            
        } catch (SQLException ex) {
            Logger.getLogger(DBSessions.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        }
        return tokenFound;
    }*/
    
    /**
     * Cria uma nova sessão. Retorna falso em caso de erro (e.g. token ja' existente).
     * @param username
     * @param token
     * @return 
     */
    public boolean add(String username, String token) {
        try {
            Statement s = con.createStatement();
            s.executeQuery("INSERT INTO sessao(username, token) VALUES('" + username + "', '" + token + "')");
        } catch (SQLException ex) {
            return false;
            //Logger.getLogger(DBSessions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;        
    }
    
    /**
     * Apaga sessãos que tenham expirado
     */
    public void deleteOldSessions() {
        try {
            Statement s = con.createStatement();
            s.executeQuery("DELETE FROM sessao WHERE dcriacao > (SYSDATE + 2)");
        } catch (SQLException ex) {
            
            //Logger.getLogger(DBSessions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
