package ewserver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBEvents {

    private Connection con = null;

    DBEvents(Connection c) {
        con = c;
    }
    /**
     * Funcao para aumentar o contador "marcacoes" da base de dados. 
     * @param ide id do evento
     */
    boolean check(String ide) {
        try {
            Statement s = con.createStatement();
            s.executeQuery("UPDATE evento SET marcacoes = marcacoes + 1 WHERE event_id = " + ide);
            
            s.close();
            return true;
        } catch (SQLException e) {
            System.out.println("ERROR: Event check: SQL Exception.");
            //e.printStackTrace();
            return false;
        }
    }

    /**
     * Funcao para diminuir o contador "marcacoes" da base de dados. 
     * @param ide id do evento
     */
    boolean uncheck(String ide) {
        try {
            Statement s = con.createStatement();
            s.executeQuery("UPDATE evento SET marcacoes = marcacoes - 1 WHERE event_id = " + ide + " and marcacoes != 0");
            s.close();
            return true;
        } catch (SQLException e) {
            System.out.println("ERROR: Event uncheck: SQL Exception.");
            //e.printStackTrace();
            return false;
        }
    }

    /**
     * Procura eventos entre dInicio e dFim, no local "onde" e com o nome indicado. Formato SHORT.
     * Formato SHORT: {event: {id = �idx�, nome = �nomex�, onde = �ondex�, dinicio = �data�, nome_emp = �nomeEmp�} }
     * @param dInicio data inicio, no formato: DD-MM-YYYY
     * @param dFim data fim
     * @param onde local
     * @param nome nome do evento
     */
    JSONArray find(String dInicio, String dFim, String onde, String nome, String empresa, String descricao) {
        JSONArray evnts = new JSONArray();
        try {
        	
            Statement s = con.createStatement();
            ResultSet rs = null;
            if(dInicio.equals("") && dFim.equals(""))
            	rs = s.executeQuery("SELECT event_id, nome, onde, to_char(dinicio, 'YYYY-MM-DD') as dinit, nome_empresa FROM evento, empresas WHERE lower(nome) LIKE '%" + nome.toLowerCase() + "%' AND lower(onde) LIKE '%" + onde.toLowerCase() + "%' AND lower(evento.descricao) LIKE '%" + descricao.toLowerCase() + "%' AND SYSDATE <= dfim AND evento.username = empresas.username ORDER BY dinicio");
            else if(dInicio.equals(""))
            	rs = s.executeQuery("SELECT event_id, nome, onde, to_char(dinicio, 'YYYY-MM-DD') as dinit, nome_empresa FROM evento, empresas WHERE lower(nome) LIKE '%" + nome.toLowerCase() + "%' AND lower(evento.descricao) LIKE '%" + descricao.toLowerCase() + "%' AND lower(onde) LIKE '%" + onde.toLowerCase() + "%' AND SYSDATE <= dfim AND to_date('" + dFim + " 23:59', 'YYYY-MM-DD HH24:MI') >= dinicio AND evento.username = empresas.username ORDER BY dinicio");
            else if(dFim.equals(""))
            	rs = s.executeQuery("SELECT event_id, nome, onde, to_char(dinicio, 'YYYY-MM-DD') as dinit, nome_empresa FROM evento, empresas WHERE lower(nome) LIKE '%" + nome.toLowerCase() + "%' AND lower(evento.descricao) LIKE '%" + descricao.toLowerCase() + "%' AND lower(onde) LIKE '%" + onde.toLowerCase() + "%' AND to_date('" + dInicio + "', 'YYYY-MM-DD') <= dfim AND evento.username = empresas.username ORDER BY dinicio");
            else
            	rs = s.executeQuery("SELECT event_id, nome, onde, to_char(dinicio, 'YYYY-MM-DD') as dinit, nome_empresa FROM evento, empresas WHERE lower(nome) LIKE '%" + nome.toLowerCase() + "%' AND lower(evento.descricao) LIKE '%" + descricao.toLowerCase() + "%' AND lower(onde) LIKE '%" + onde.toLowerCase() + "%' AND to_date('" + dInicio + "', 'YYYY-MM-DD') <= dfim AND to_date('" + dFim + "', 'YYYY-MM-DD') >= dinicio AND evento.username = empresas.username ORDER BY dinicio");

            //se nao forem encontrados resultados
            if (!rs.next()) {
                return evnts;
            }

            //adiciona eventos ao array
            do {
                JSONObject jso = new JSONObject();
                jso.put("id", rs.getString("EVENT_ID"));
                jso.put("nome", rs.getString("NOME"));
                jso.put("onde", rs.getString("ONDE"));
                jso.put("dinicio", rs.getString("DINIT"));
                jso.put("nome_emp", rs.getString("NOME_EMPRESA"));
                evnts.put(jso);
            } while (rs.next());

            s.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("ERROR: Event find: SQL Exception.");
            return null;
        } catch (JSONException ex) {
            //Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: Event find: JSON Exception.");
            return null;
        }
        return evnts;
    }

    
    /**
     * 
     * @return
     */
    JSONArray findThisWeek() {
    	JSONArray evnts = new JSONArray();
    	
        try {
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT event_id, nome, onde, to_char(dinicio, 'YYYY-MM-DD HH24:MI:SS') as dinit, nome_empresa FROM evento, empresas WHERE dfim >= SYSDATE AND dfim <= SYSDATE + 7 AND evento.username = empresas.username ORDER BY marcacoes DESC, dinicio");
			
			//se nao forem encontrados resultados
            if (!rs.next()) {
                return evnts;
            }
			
            
            do {
                JSONObject jso = new JSONObject();
                jso.put("id", rs.getString("EVENT_ID"));
                jso.put("nome", rs.getString("NOME"));
                jso.put("onde", rs.getString("ONDE"));
                jso.put("dinicio", rs.getString("DINIT"));
                jso.put("nome_emp", rs.getString("NOME_EMPRESA"));
                evnts.put(jso);
            } while (rs.next());
				
			s.close();
			rs.close();
		} catch (SQLException e) {
			System.out.println("ERROR: Event findThisWeek: SQL Exception.");
			return null;
			//e.printStackTrace();
		} catch (JSONException e) {
			//e.printStackTrace();
			System.out.println("ERROR: Event findThisWeek: JSON Exception.");
			return null;
		}

        return evnts;
    }
    


    /**
     * 
     * @param comps -> ["234", "827"]
     * @return
     */
    JSONArray findThisWeek(JSONArray comps) {
    	JSONArray evnts = new JSONArray();
    	if(comps.length()==0) return evnts;
    	
        try {
			Statement s = con.createStatement();
			String query = "SELECT event_id, nome, onde, to_char(dinicio, 'YYYY-MM-DD HH24:MI:SS') as dinit, nome_empresa FROM evento, empresas WHERE dinicio >= SYSDATE AND dinicio <= SYSDATE + 7 AND evento.username = empresas.username AND empresas.emp_id IN (";
			query +=comps.get(0);

			for(int i = 1;i<comps.length();i++){
				query += ","+comps.get(i);
			}
			query +=") ORDER BY marcacoes DESC, dinicio";
			
			ResultSet rs = s.executeQuery(query);
			
			//se nao forem encontrados resultados
            if (!rs.next()) {
                return evnts;
            }
			
            
            do {
                JSONObject jso = new JSONObject();
                jso.put("id", rs.getString("EVENT_ID"));
                jso.put("nome", rs.getString("NOME"));
                jso.put("onde", rs.getString("ONDE"));
                jso.put("dinicio", rs.getString("DINIT"));
                jso.put("nome_emp", rs.getString("NOME_EMPRESA"));
                evnts.put(jso);
            } while (rs.next());
				
			s.close();
			rs.close();
		} catch (SQLException e) {
			System.out.println("ERROR: Event findThisWeek: SQL Exception.");
			//e.printStackTrace();
			return null;
		} catch (JSONException e) {
			//e.printStackTrace();
			System.out.println("ERROR: Event findThisWeek: JSON Exception.");
			return null;
		}
        return evnts;
    }

    /**
     * Procura informa��o do evento com o id = ide e retorna a sua informa��o detalhada.
     * formato informacao full: 
     * { event: {ide=�idx�, nome =�nomex�, desc = �descx�, onde=�ondex�, dinicio =�diniciox�, dfim = �dfimx�, contador = ��, nome_empresa = �nomeEmp�}}}
     * @param ide
     * @return
     */
    JSONObject getEventInfo(String ide) {
        JSONObject jso = new JSONObject();
        Statement s;
        try {

            s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT evento.username, evento.event_id, evento.nome, evento.descricao, evento.onde, to_char(dinicio, 'YYYY-MM-DD HH24:MI:SS') as dinit, to_char(dfim, 'YYYY-MM-DD HH24:MI:SS') as df, evento.marcacoes, empresas.nome_empresa, empresas.emp_id FROM evento, empresas WHERE empresas.username = evento.username AND event_id = " + ide);

            if (!rs.next()) {
                return jso;
            }

            jso.put("ide", rs.getString("EVENT_ID"));
            jso.put("nome", rs.getString("NOME"));
            jso.put("desc", (rs.getString("DESCRICAO") == null) ? "" : rs.getString("descricao"));
            jso.put("onde", rs.getString("ONDE"));
            jso.put("dinicio", rs.getString("DINIT"));
            jso.put("dfim", rs.getString("DF"));
            jso.put("contador", rs.getString("MARCACOES"));
            jso.put("nome_empresa", rs.getString("NOME_EMPRESA"));
            jso.put("idc", rs.getString("EMP_ID"));
            

        } catch (SQLException e) {
            //e.printStackTrace();
            System.out.println("ERROR: Event Info: SQL Exception.");
            return null;
        } catch (JSONException ex) {
            //Logger.getLogger(DBCompanies.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: Event Info: JSON Exception.");
            return null;
        }
        return jso;
    }

    
    /**
     * 
     * @param idc
     * @return
     *      -1 em caso de erro
     *      0 : sucesso
     *      1 : calendario nao existe
     *      2 : credenciais invalidas
     */
    
    int importEvent(String usernameEmp) {
    	String idc = EWServer.dbm.companies.getIDC(usernameEmp);
        JSONObject empInfo = EWServer.dbm.companies.get(idc, 2);
    	String username = "";
    	String password = ""; 
    	String nomeCalendario = "";
    	
		try {
			username = empInfo.getString("gc_username");
	    	password = empInfo.getString("gc_password");
	    	nomeCalendario = empInfo.getString("gc_nome");
		} catch (JSONException e1) {
			//e1.printStackTrace();
			return -1;
		}
		if(username.equals("") ||password.equals("") || nomeCalendario.equals("")){
			return -1;
		}
    	
    	String idCalendario = "";

    	try {
			URL feedUrlCalendarios = new URL("https://www.google.com/calendar/feeds/default");
			CalendarService myService = new CalendarService("exampleCo-exampleApp-1");
			myService.setUserCredentials(username, password);
			CalendarEventFeed myFeed = myService.getFeed(feedUrlCalendarios, CalendarEventFeed.class);
			
			
			for(int i = 0; i<myFeed.getEntries().size(); i++){
				if(myFeed.getEntries().get(i).getTitle().getPlainText().toLowerCase().equals(nomeCalendario.toLowerCase())){
					String[] tokens = myFeed.getEntries().get(i).getId().toString().split("/");
					idCalendario = tokens[tokens.length-1];
					break;
				}
			}
			
			if(idCalendario.equals("")) {
				System.out.println("Calendario do utilizador nao encontrado.");
				return 1; //calendario nao encontrado.
			}
			//data de hoje no formato certo
			String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
			String time[] = sdf.format(cal.getTime()).split(" ");
			String startTime = time[0]+"T"+time[1];

			URL feedUrl = new URL("https://www.google.com/calendar/feeds/"+idCalendario+"/private/full");
			//query para mostrar eventos a partir de hoje!
			CalendarQuery myQuery = new CalendarQuery(feedUrl);
			myQuery.setMinimumStartTime(DateTime.parseDateTime(startTime));
			//myFeed = myService.getFeed(feedUrl, CalendarEventFeed.class);
			CalendarEventFeed resultFeed = myService.query(myQuery, CalendarEventFeed.class);
			
			String idEvento = "";
			ArrayList<String> BDEventsID = findGcIds(usernameEmp);
			//percorrer os eventos
			for(int i = 0; i<resultFeed.getEntries().size(); i++){
				String[] tokens = resultFeed.getEntries().get(i).getId().toString().split("/");
				idEvento = tokens[tokens.length-1];
				//System.out.println("Idevento: "+BDEventsID.get(i));
				//ver se o idEvento existe no
				if(BDEventsID.contains(idEvento)){
					BDEventsID.remove(idEvento);
				}
			}
			//eliminar os eventos que ja nao estao no calendario
			for(int i = 0; i<BDEventsID.size(); i++){
				delEvento(BDEventsID.get(i));
				
			}
			ArrayList<String> BDEid = findGcIds(usernameEmp);
			//ver os eventos todos, se nao tiverem na base de dados adiciona, se tiverem e forem alterados-> altera, se tiver e nao for alterado nao faz nada
			for(int i = 0; i<resultFeed.getEntries().size(); i++){
				String[] tokens = resultFeed.getEntries().get(i).getId().toString().split("/");
				idEvento = tokens[tokens.length-1];

				//se nao contem adiciona
				if(!BDEid.contains(idEvento)){
					System.out.println("Nao tem evento, add.");
					String dataAlt = resultFeed.getEntries().get(i).getUpdated().toString();
					String nomeEvnt = resultFeed.getEntries().get(i).getTitle().getPlainText();
					String descEvnt="";
                                        
					if(resultFeed.getEntries().get(i).getPlainTextContent().equals("")) descEvnt = "Sem descricao.";
					else descEvnt = resultFeed.getEntries().get(i).getPlainTextContent();
                                        
					String dInicio = resultFeed.getEntries().get(i).getTimes().get(0).getStartTime().toString();//.split("T")[0]+" "+resultFeed.getEntries().get(i).getTimes().get(0).getStartTime().toString().split("T")[1].split(":")[0]+"-"+resultFeed.getEntries().get(i).getTimes().get(0).getStartTime().toString().split("T")[1].split(":")[1];
					String dFim = resultFeed.getEntries().get(i).getTimes().get(0).getEndTime().toString();//.split("T")[0]+ " "+ resultFeed.getEntries().get(i).getTimes().get(0).getEndTime().toString().split("T")[1].split(":")[0]+"-"+resultFeed.getEntries().get(i).getTimes().get(0).getEndTime().toString().split("T")[1].split(":")[1];					
					if(dInicio.contains("T")){
                                            dFim = resultFeed.getEntries().get(i).getTimes().get(0).getEndTime().toString().split("T")[0]+ " "+ resultFeed.getEntries().get(i).getTimes().get(0).getEndTime().toString().split("T")[1].split(":")[0]+"-"+resultFeed.getEntries().get(i).getTimes().get(0).getEndTime().toString().split("T")[1].split(":")[1];					
                                            dInicio = resultFeed.getEntries().get(i).getTimes().get(0).getStartTime().toString().split("T")[0]+" "+resultFeed.getEntries().get(i).getTimes().get(0).getStartTime().toString().split("T")[1].split(":")[0]+"-"+resultFeed.getEntries().get(i).getTimes().get(0).getStartTime().toString().split("T")[1].split(":")[1];
                                        }
                                        String onde = "";
                                        
					if(resultFeed.getEntries().get(i).getLocations().get(0).getValueString().equals(""))
                                            onde = "Sem local definido.";
					else
                                            onde =resultFeed.getEntries().get(i).getLocations().get(0).getValueString();
                                        
					addEvent(usernameEmp,idEvento, nomeEvnt, dataAlt, descEvnt, dInicio, dFim, onde);
					//adicionar a base de dados evento
				}else{// se contem
					if(resultFeed.getEntries().get(i).getUpdated().toString().equals(findDAlteracao(idEvento))){//se nao foi alterado
						System.out.println("Ja existe.");
						continue;
					}else{//se foi alterado->alterar
						System.out.println("Alterar.");
						String dataAlt = resultFeed.getEntries().get(i).getUpdated().toString();
						String nomeEvnt = resultFeed.getEntries().get(i).getTitle().getPlainText();
						String descEvnt="";
						if(resultFeed.getEntries().get(i).getPlainTextContent().equals("")) descEvnt = "Sem descricao.";
						else descEvnt = resultFeed.getEntries().get(i).getPlainTextContent();
                                                
                                                String dInicio = resultFeed.getEntries().get(i).getTimes().get(0).getStartTime().toString();//.split("T")[0]+" "+resultFeed.getEntries().get(i).getTimes().get(0).getStartTime().toString().split("T")[1].split(":")[0]+"-"+resultFeed.getEntries().get(i).getTimes().get(0).getStartTime().toString().split("T")[1].split(":")[1];
                                                String dFim = resultFeed.getEntries().get(i).getTimes().get(0).getEndTime().toString();//.split("T")[0]+ " "+ resultFeed.getEntries().get(i).getTimes().get(0).getEndTime().toString().split("T")[1].split(":")[0]+"-"+resultFeed.getEntries().get(i).getTimes().get(0).getEndTime().toString().split("T")[1].split(":")[1];					
                                                if(dInicio.contains("T")){
                                                    dFim = resultFeed.getEntries().get(i).getTimes().get(0).getEndTime().toString().split("T")[0]+ " "+ resultFeed.getEntries().get(i).getTimes().get(0).getEndTime().toString().split("T")[1].split(":")[0]+"-"+resultFeed.getEntries().get(i).getTimes().get(0).getEndTime().toString().split("T")[1].split(":")[1];					
                                                    dInicio = resultFeed.getEntries().get(i).getTimes().get(0).getStartTime().toString().split("T")[0]+" "+resultFeed.getEntries().get(i).getTimes().get(0).getStartTime().toString().split("T")[1].split(":")[0]+"-"+resultFeed.getEntries().get(i).getTimes().get(0).getStartTime().toString().split("T")[1].split(":")[1];
                                                }
                                                String onde = "";
                                                
						if(resultFeed.getEntries().get(i).getLocations().get(0).getValueString().equals("")) onde = "Sem local definido.";
						else onde =resultFeed.getEntries().get(i).getLocations().get(0).getValueString();
						updateEvent(usernameEmp,idEvento, nomeEvnt, dataAlt, descEvnt, dInicio, dFim, onde);
					}
				}
			}
			System.out.println("Import feito com sucesso.");
			
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException Import Exception");
			//e.printStackTrace();
			return -1;
		} catch (AuthenticationException e) {
			System.out.println("Authentication Import Exception");
			//e.printStackTrace();
			return 2;
		} catch (IOException e) {
			System.out.println("IO Import Exception");
			//e.printStackTrace();
			return -1;
		} catch (ServiceException e) {
			System.out.println("Service Import Exception");
			//e.printStackTrace();
			return -1;
		}
        return 0;
    }
    
        ArrayList<String> findGcIds (String username) {
        ArrayList<String> ids = new ArrayList<String>();
        try {
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT  gc_id FROM evento WHERE username = '" + username + "'");
            
            while(rs.next())
                ids.add(rs.getString("GC_ID"));
            
            s.close();
            rs.close();
            return ids;
        } catch (SQLException ex) {
        	System.out.println("Erro na BD findGCIds.");
           // Logger.getLogger(DBEvents.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
    }
    
    String findDAlteracao(String gc_id) {
        try {
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT dalteracao FROM evento WHERE gc_id = '" + gc_id + "'");
            
            if(!rs.next())
                return null;
            
            String dalteracao = rs.getString("DALTERACAO");
            
            s.close();
            rs.close();
            return dalteracao;
            
        } catch (SQLException ex) {
        	System.out.println("Erro na BD find.");
            //Logger.getLogger(DBEvents.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    boolean delEvento(String gc_id) {
        try {
        	System.out.println("1");
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("DELETE FROM evento WHERE gc_id = '" + gc_id + "'");
            System.out.println("2");
            s.close();
            rs.close();
            return true;
            
        } catch (SQLException ex) {
        	System.out.println("Erro na BD delete.");
            //Logger.getLogger(DBEvents.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    
    boolean addEvent(String username, String idEvento, String nomeEvnt, String dataAlt, String descEvnt, String dIni, String dFim, String onde){
        Statement s;
		try {
			s = con.createStatement();
			ResultSet rs = s.executeQuery("INSERT INTO evento(username, nome, descricao, onde, dinicio, dfim, gc_id, dalteracao) VALUES('"+username+"', '"+nomeEvnt+"','"+descEvnt+"','"+onde+"',to_date('"+dIni+"','YYYY-MM-DD HH24-MI' ),to_date('"+dFim+"','YYYY-MM-DD HH24-MI' ),'"+idEvento+"', '"+dataAlt+"') ");
			s.close();
		    rs.close();
		} catch (SQLException e) {
			System.out.println("Erro na BD adicionar.");
			return false;
			//e.printStackTrace();
		}
    	return true;
    }

    boolean updateEvent(String username, String idEvento, String nomeEvnt, String dataAlt, String descEvnt, String dIni, String dFim, String onde){
        Statement s;
        		try {
			s = con.createStatement();
			ResultSet rs = s.executeQuery("UPDATE evento SET nome = '"+nomeEvnt+"', descricao = '"+descEvnt+"', onde = '"+onde+"', dinicio = to_date('"+dIni+"','YYYY-MM-DD HH24-MI' ), dfim=to_date('"+dFim+"','YYYY-MM-DD HH24-MI' ), dalteracao = '"+dataAlt+"' WHERE gc_id = '"+idEvento+"'");
			s.close();
		    rs.close();
		} catch (SQLException e) {
			System.out.println("Erro na BD alterar.");
			return false;
			//e.printStackTrace();
		}
    	return true;
    }
}
