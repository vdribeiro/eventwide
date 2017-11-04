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
public class DBCompanies {

    private Connection con = null;

    DBCompanies(Connection c) {
        con = c;
    }

    /**
     * Devolve o JSONObject correspondente à empresa indicada.
     * Modos:
     * 0 Short: idc, nome, cidade
     * 1 Default: idc, nome, cidade, morada, descricao, telefones
     * 2 Full: idc, nome, cidade, morada, descricao, telefones, gc_username, gc_password, gc_nome
     * @param idc
     * @param mode
     * @return 
     */
    public JSONObject get(String idc, int mode) {
        JSONObject comp = new JSONObject();

        try {

            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM empresas WHERE emp_id = " + idc);

            //não existem empresas com o id indicado
            if (!rs.next()) {
                return comp;
            }

            String username = rs.getString("USERNAME");

            //construcao do objecto JSON da empresa
            try {
                comp.put("idc", idc);
                comp.put("nome", (rs.getString("NOME_EMPRESA") == null) ? "" : rs.getString("NOME_EMPRESA"));
                comp.put("cidade", (rs.getString("CIDADE") == null) ? "" : rs.getString("CIDADE"));

                //comp.put("USER", (rs.getString("USERNAME")==null)? "": rs.getString("USERNAME"));

                if (mode == 2) {
                    comp.put("gc_username", (rs.getString("GC_USERNAME") == null) ? "" : rs.getString("GC_USERNAME"));
                    comp.put("gc_password", (rs.getString("GC_PASSWORD") == null) ? "" : rs.getString("GC_PASSWORD"));
                    comp.put("gc_nome", (rs.getString("GC_NOME") == null) ? "" : rs.getString("GC_NOME"));
                }

                if (mode == 1 || mode == 2) {
                    comp.put("descricao", (rs.getString("DESCRICAO") == null) ? "" : rs.getString("DESCRICAO"));
                    comp.put("morada", (rs.getString("MORADA") == null) ? "" : rs.getString("MORADA"));

                    //JSONArray com os telefones
                    JSONArray tels = new JSONArray();

                    rs = s.executeQuery("SELECT * FROM tels WHERE username = '" + username + "'");

                    while (rs.next()) {
                        tels.put(rs.getInt("TELEFONE"));
                    }

                    comp.put("telefones", tels);
                }

            } catch (JSONException ex) {
                Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("ERROR: Get Company: malformed JSON.");
                return null;
            }


            s.close();
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        return comp;
    }

    public boolean update(JSONObject comp, String username) {
        String updtStatement = "UPDATE empresas SET";
        boolean foundFirst = false;

        try {
            //verifica se os seguintes atributos existem no objecto
            //caso existam, adiciona-os ao update statement para serem actualizados
            if (comp.has("nome")) {
                if (!comp.getString("nome").equals("")) {
                    foundFirst = true;
                    updtStatement += " NOME_EMPRESA='" + comp.getString("nome") + "'";
                }
            }

            if (comp.has("morada")) {
                if (foundFirst) {
                    updtStatement += ",";
                } else {
                    foundFirst = true;
                }
                updtStatement += " MORADA='" + comp.getString("morada") + "'";
            }

            if (comp.has("cidade")) {
                if (foundFirst) {
                    updtStatement += ",";
                } else {
                    foundFirst = true;
                }
                updtStatement += " CIDADE='" + comp.getString("cidade") + "'";
            }

            if (comp.has("descricao")) {
                if (foundFirst) {
                    updtStatement += ",";
                } else {
                    foundFirst = true;
                }
                updtStatement += " DESCRICAO='" + comp.getString("descricao") + "'";
            }

            if (comp.has("gc_nome")) {
                if (foundFirst) {
                    updtStatement += ",";
                } else {
                    foundFirst = true;
                }
                updtStatement += " GC_NOME='" + comp.getString("gc_nome") + "'";
            }

            if (comp.has("gc_username")) {
                if (foundFirst) {
                    updtStatement += ",";
                } else {
                    foundFirst = true;
                }
                updtStatement += " GC_USERNAME='" + comp.getString("gc_username") + "'";
            }

            if (comp.has("gc_password")) {
                if (foundFirst) {
                    updtStatement += ",";
                } else {
                    foundFirst = true;
                }
                updtStatement += " GC_PASSWORD='" + comp.getString("gc_password") + "'";
            }
            
            if (comp.has("password")) {
                if (foundFirst) {
                    updtStatement += ",";
                } else {
                    foundFirst = true;
                }
                updtStatement += " PASSWORD='" + comp.getString("password") + "'";
            }

            //nada para actualizar
            if (!foundFirst) {
                return true;
            }

            Statement s = con.createStatement();
            s.executeQuery(updtStatement + " WHERE USERNAME = '" + username + "'");

            s.close();
            
            if(comp.has("telefones"))
                updateTels(comp.getJSONArray("telefones"), username);
            
        } catch (JSONException ex) {
            //Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: Update Company: Received malformed JSON.");
            return false;
        } catch (SQLException ex) {
            //Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: Update Company: Could not update company info.");
            return false;
        }

        //System.out.println("STATEMENT: " + updtStatement + " WHERE USERNAME = '" + username + "'");
        return true;
    }

    /**
     * Retorna um JSONArray com os proximos eventos (short) duma empresa ordenados pela data de inicio.
     * Devolve null em caso de erro (e.g. empresa nao existe)
     * ou um array vazio caso não hajam eventos.
     * @param idc
     * @return 
     */
    public JSONArray getUpcomingEvents(String idc) {

        /**
         * TODO: testar
         */
        JSONArray events = new JSONArray();

        try {

            //pesquisa o username corresponde ao ID indicado
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT username, nome_empresa FROM empresas WHERE emp_id = '" + idc + "'");

            //empresa nao existe
            if (!rs.next()) {
                return null;
            }


            //parse do username
            String username = rs.getString("USERNAME");
            String nome_emp = rs.getString("NOME_EMPRESA");

            s.close();
            rs.close();

            //pesquisa os eventos do user ordenados pela data de inicio
            s = con.createStatement();
            rs = s.executeQuery("SELECT event_id, nome, onde, to_char(dinicio, 'DD-MM-YYYY') as dinit from evento WHERE username = '" + username + "' AND dfim >= SYSDATE ORDER BY dinicio");

            //percorre os resultados da pesquisa e adiciona-os ao array
            while (rs.next()) {
                JSONObject event = new JSONObject();

                event.put("nome_emp", nome_emp);
                event.put("id", rs.getString("EVENT_ID"));
                event.put("nome", rs.getString("NOME"));
                event.put("onde", rs.getString("ONDE"));
                event.put("dinicio", rs.getString("DINIT"));

                events.put(event);

            }

            s.close();
            rs.close();
        } catch (JSONException ex) {
            //Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: Upcoming Events: JSON Exception.");
            return null;
        } catch (SQLException ex) {
            //Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: Upcoming Events: SQL exception.");
            return null;
        }

        return events;
    }

    
    
    /**
     * Pesquisa empresas pelo nome e retorna um JSONArray com os resultados encontrados
     * @param nome
     * @return 
     */
    public JSONArray find(String nome) {
        JSONArray comps = new JSONArray();
        try {

            //pesquisa os ids das empresas que correspondem ao critério de pesquisa
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT emp_id FROM empresas WHERE lower(NOME_EMPRESA) LIKE '%" + nome.toLowerCase() + "%'");

            //retorna se nao forem encontrados resultados
            if (!rs.next()) {
                return comps;
            }

            //pesquisa a informacao de cada um dos resultados
            do {
                JSONObject jso = get(rs.getString("EMP_ID"), 0);
                if (jso != null) {
                    comps.put(jso);
                } else {
                    continue;
                }
            } while (rs.next());

            s.close();
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return comps;
    }

    /**
     * Devolve o id da empresa correspondente ao username indicado
     * @param username
     * @return 
     */
    public String getIDC(String username) {
        try {
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT emp_id FROM empresas WHERE username ='" + username + "'");
            
            if(!rs.next())
                return null;
            
            return rs.getString("EMP_ID");
        } catch (SQLException ex) {
            //Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: DBCompanies getIDC: SQL Exception.");
            return null;
        }
    }

    /**
     * Actualiza os telefones duma empresa
     * @param jSONArray 
     */
    private void updateTels(JSONArray tels, String username) {
        /**
         * TODO: testar
         */
        try {
            Statement s = con.createStatement();
            s.executeQuery("DELETE FROM tels WHERE username = '" + username + "'");
            
            if(tels.length() == 0)
                return;
            
            String updtStatement = "INSERT ALL ";
            for(int i = 0; i < tels.length(); i++)
                updtStatement += "INTO tels VALUES('" + username + "' , " + tels.getString(i) + ") ";
            updtStatement += "SELECT * from DUAL";
            
            s.executeQuery(updtStatement);
            
        } catch (JSONException ex) {
            //Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: DBCompanies updateTels: JSON Exception");
        } catch (SQLException ex) {
            //Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: DBCompanies updateTels: SQLException");
            return;
        }
        
        
    }
}
