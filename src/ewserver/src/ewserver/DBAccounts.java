/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ewserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Diogo
 */
public class DBAccounts {

    private Connection con = null;

    DBAccounts(Connection c) {
        con = c;
    }

    /**
     * Apaga a conta de uma empresa.
     * @param username
     * @return: true em caso de sucesso. 
     */
    public boolean delete(String username) {
        /**
         * TODO: testar
         */
        try {
            Statement s = con.createStatement();
            s.executeQuery("DELETE FROM empresas WHERE username = '" + username + "'");
            s.close();
            return true;

        } catch (SQLException ex) {
            //Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Cria uma nova conta
     * @param comp
     * @return 
     */
    public boolean add(JSONObject acc) {
        String username = "";
        String pass = "";

        try {
            username = acc.getString("username");
            pass = acc.getString("pass");
        } catch (JSONException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: Add Account: Received malformed JSON.");
            return false;
        }

        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO empresas(username, password) values(?, ?)");
            ps.setString(1, username);
            ps.setString(2, pass);

            ps.execute();
            ps.close();
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1) {
                System.out.println("ERROR: Add Account: Username already exists.");
            } else {
                Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }

        System.out.println("Added new account   " + username + ":" + pass);

        return true;

    }

    /**
     * Altera a password da conta indicada.
     * Retorna false em caso de erro.
     * @param username
     * @param password
     * @return 
     */
    public boolean alterPass(String username, String password) {
        try {
            Statement s = con.createStatement();
            s.execute("UPDATE empresas SET password='" + password + "' WHERE username = '" + username + "'");
            s.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Verifica se o login está correcto
     */
    public boolean isValid(String username, String pass) {
        try {
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT password FROM empresas WHERE username = '" + username + "'");

            //conta inexistente
            if (!rs.next()) {
                return false;
            }

            //username e password correctos
            if (rs.getString("PASSWORD").equals(pass)) {
                s.close();
                rs.close();
                return true;
            }
            else {      //password incorrecta
                s.close();
                rs.close();
                return false;
            }

        } catch (SQLException ex) {
            //Logger.getLogger(DBAccounts.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: Account validation: SQL Exception.");
            return false;
        }

    }

    /**
     * Pesquisa contas segundo o nome da empresa (parcial/total) indicado.
     * Retorna um array vazio caso não sejam encontrados resultados, ou null em caso de erro.
     * @param username
     * @return : JSONArray [{idc:"123", nome:"Empresa X", username:"user X"}, ...]
     */
    public JSONArray findByName(String nome) {
        JSONArray accounts = new JSONArray();
        try {

            //pesquisa os ids das empresas que correspondem ao critério de pesquisa
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT emp_id, nome_empresa, username, tipo_conta FROM empresas WHERE lower(NOME_EMPRESA) LIKE '%" + nome.toLowerCase() + "%'");

             while (rs.next()) {
                JSONObject js = new JSONObject();
                js.put("idc", rs.getString("EMP_ID"));
                js.put("nome_emp", (rs.getString("NOME_EMPRESA") == null) ? "" : rs.getString("NOME_EMPRESA"));
                js.put("username", rs.getString("USERNAME"));
                js.put("tipo_conta", rs.getString("TIPO_CONTA"));

                accounts.put(js);
            }

            s.close();
            rs.close();

        } catch (JSONException ex) {
            Logger.getLogger(DBAccounts.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (SQLException ex) {
            Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return accounts;
    }
    
    /**
     * Retorna o tipo de utilizador correspondente (user/admin)
     * @param username
     * @return 
     */
    public String getType(String username) {
        try {
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT tipo_conta FROM empresas WHERE username = '" + username + "'");
            
            if(!rs.next())
                return null;
            
            return rs.getString("TIPO_CONTA");
        } catch (SQLException ex) {
            Logger.getLogger(DBAccounts.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
    }
    
    /**
     * Verifica se existe alguma conta com o username indicado
     * @param username
     * @return 
     */
    public boolean exists(String username) {
        try {
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM empresas WHERE username = '" + username + "'");
            
            if(!rs.next())
                return false;
            else
                return true;
            
        } catch (SQLException ex) {
            //Logger.getLogger(DBAccounts.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: DBAccounts exists: SQL Exception");
            return false;
        }
    }
    
    
    /**
     * Pesquisa contas segundo o username da conta (parcial/total) indicado.
     * Retorna um array vazio caso não sejam encontrados resultados, ou null em caso de erro.
     * @param username
     * @return : JSONArray [{idc:"123", nome:"Empresa X", username:"user X", tipo_conta: "user"}, ...]
     */
    public JSONArray findByUsername(String username) {
        JSONArray accounts = new JSONArray();
        try {

            //pesquisa os ids das empresas que correspondem ao critério de pesquisa
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT emp_id, nome_empresa, username, tipo_conta FROM empresas WHERE lower(username) LIKE '%" + username.toLowerCase() + "%'");

             while (rs.next()) {
                JSONObject js = new JSONObject();
                js.put("idc", rs.getString("EMP_ID"));
                js.put("nome_emp", (rs.getString("NOME_EMPRESA") == null) ? "" : rs.getString("NOME_EMPRESA"));
                js.put("username", rs.getString("USERNAME"));
                js.put("tipo_conta", rs.getString("TIPO_CONTA"));
                
                accounts.put(js);
            }

            s.close();
            rs.close();

        } catch (JSONException ex) {
            Logger.getLogger(DBAccounts.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (SQLException ex) {
            Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return accounts;
    }
    
}
