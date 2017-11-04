package ewserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class EventsHandler implements HttpHandler{

	@Override
	public void handle(HttpExchange he) throws IOException {
            
            if (he.getRequestMethod().toLowerCase().equals("post")) {
                handlePost(he);
            }  else if (he.getRequestMethod().toLowerCase().equals("get")) {
                handleGet(he);
            }  else {
                he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
                he.getResponseBody().close();
            }
	}

	/**
	 * get events?oper=companyevents&idc=IDC
	 * get events?oper=eventinfo&ide=X
	 * get events?oper=7dias
	 * get events?oper=7dias&comps=1&comps=2&....
	 * get events?oper=searchevent&dinicio=DI&dfim=DF&onde=ONDE&nome=NOME
	 * @param he
	 */
	private void handleGet(HttpExchange he) {
		
		String[] args = new String[0];
		if (he.getRequestURI().getQuery() != null) {
            args = he.getRequestURI().getQuery().split("&");
        }
		try {
			String oper = "", idc = "", ide = "", dinicio = "", dfim = "", onde = "", nome = "", empresa = "", descricao = "";
			JSONArray comps = new JSONArray();
			 for (int i = 0; i < args.length; i++) {
				 String[] tokens = args[i].split("=");
				 /*if(tokens.length!=2){
					 he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
					 send("", he);
					 return;
				 }*/
				 if(tokens[0].equals("oper")){
					 oper = tokens[1];
				 } else if(tokens[0].equals("idc")){
					 idc = tokens[1];
				 } else if(tokens[0].equals("ide")) {
					 ide = tokens[1];				 
				 } else if(tokens[0].equals("dinicio")) {
					 dinicio = tokens[1];
				 }  else if(tokens[0].equals("dfim")) {
					 dfim = tokens[1];
				 }  else if(tokens[0].equals("onde")) {
					 onde = tokens[1];
				 }  else if(tokens[0].equals("nome")) {
					 nome = tokens[1];
				 }  else if(tokens[0].equals("comps")){
					 comps.put(tokens[1]);
				 } else if(tokens[0].equals("empresa")) {
					 empresa = tokens[1];
				 }else if(tokens[0].equals("descricao")) {
					 descricao = tokens[1];
				 }
				 
			 }
			//faltam argumentos
	         if (oper.equals("")
	                 || (oper.equals("companyevents") && idc.equals(""))
	                 || (oper.equals("eventinfo") && ide.equals(""))
	                 || (oper.equals("searchevent") && (dinicio.equals("") && dfim.equals("") && onde.equals("") && nome.equals("") && empresa.equals("") && descricao.equals("")))) {
	             
				he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
	            send("", he);
	            return;
	         }
	         
	         
	         if(oper.equals("companyevents")){
	        	 JSONArray companyevents = EWServer.dbm.companies.getUpcomingEvents(idc);
	        	 JSONObject companyInfo = EWServer.dbm.companies.get(idc, 0); // modo short
	        	 JSONObject companyEvnts = new JSONObject();
	        	 companyEvnts.put("idc", idc);
	        	 companyEvnts.put("empresa", companyInfo.get("nome"));
	        	 companyEvnts.put("eventos", companyevents);
	        	 
	        	 if(companyevents == null || companyInfo == null){
	        		 he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
	        		 send("", he);
	        		 return;
	        	 }
	        	 else if(companyevents.length() == 0 || companyInfo.length()==0){
	        		 he.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, 0);
	        		 send("", he);
	        		 return;
	        	 }
	        	 else{ //sucesso
	        		 he.sendResponseHeaders(HttpURLConnection.HTTP_OK, companyEvnts.toString().length());
	        		 send(companyEvnts.toString(), he);
	        		 return;
	        	 }
	         } else if(oper.equals("eventinfo")){ //events?oper=eventinfo&ide=X
	        	 JSONObject eventInfo = EWServer.dbm.events.getEventInfo(ide);
	        	 
	        	 if(eventInfo == null){
	        		 he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
	        		 send("", he);
	        		 return;
	        	 }else if(eventInfo.length() == 0){
	        		 he.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, 0);
	        		 send("", he);
	        		 return;	        		 
	        	 }else{//sucesso
	        		 he.sendResponseHeaders(HttpURLConnection.HTTP_OK, eventInfo.toString().length());
	        		 send(eventInfo.toString(), he);
	        		 return;	        		 
	        	 }
	        	 
	         } else if(oper.equals("searchevent")){//oper=searchevent&dinicio=DI&dfim=DF&onde=ONDE&nome=NOME&descricao=DESC&empresa=EMP
	        	 JSONArray searchEvent =  EWServer.dbm.events.find(dinicio,dfim, onde, nome, empresa, descricao);
	        	 
	        	 if(searchEvent == null){
	        		 he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
	        		 send("", he);
	        		 return;
	        	 }else if(searchEvent.length() == 0){
	        		 he.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, 0);
	        		 send("", he);
	        		 return;	        		 
	        	 }else{//sucesso
	        		 he.sendResponseHeaders(HttpURLConnection.HTTP_OK, searchEvent.toString().length());
	        		 send(searchEvent.toString(), he);
	        		 return;	        		 
	        	 }
	         } else if(oper.equals("7dias")){//events?oper=7dias
	        	 JSONArray evnts = new JSONArray();
	        	 if(comps.length()==0){
	        		 evnts = EWServer.dbm.events.findThisWeek();
	        	 }else{
	        		 evnts = EWServer.dbm.events.findThisWeek(comps);
	        	 }
	        	 if(evnts == null){
	        		 he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
	        		 send("", he);
	        		 return;
	        	 }else if(evnts.length() == 0){
	        		 he.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, 0);
	        		 send("", he);
	        		 return;	        		 
	        	 }else{//sucesso
	        		 he.sendResponseHeaders(HttpURLConnection.HTTP_OK, evnts.toString().length());
	        		 send(evnts.toString(), he);
	        		 return;	        		 
	        	 }
	         }
   
		} catch (IOException e) {
			//  Auto-generated catch block
			System.out.println("Error: IOException handleGet.");
			//e.printStackTrace();
		} catch (JSONException e) {
			System.out.println("Error: JSONException handleGet.");
			//e.printStackTrace();
		}
		
	}

	
	/**
	 * POST /events
	 * {ide = IDE, oper = "check"} ou {ide = IDE, oper = "uncheck"} 
	 * POST /events
	 * HEADER: token = TOKEN
	 * BODY: {oper = "sync"}
	 * @param he
	 */
	private void handlePost(HttpExchange he) {
        
        try {
        	InputStream is = he.getRequestBody();
			JSONObject body = new JSONObject(read(is));
			
            
            String ide = null;
            String oper = null;
            if(body.has("ide"))
            	ide = body.getString("ide");
            if(body.has("oper"))
            	oper = body.getString("oper");
            
            if(oper == null){
	       		he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
	    		send("", he);
	    		return;
            }else if(oper.equals("")||oper.equals("")){
	       		he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
	    		send("", he);
	    		return;
            }else{
            	if(oper.equals("check")){
            		if(EWServer.dbm.events.check(ide)){
                        he.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                        send("", he);
                        return;
            		} else{
        	       		he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
        	    		send("", he);
        	    		return;
            		}
            	}else if(oper.equals("uncheck")){
            		if(EWServer.dbm.events.uncheck(ide)){
                        he.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                        send("", he);
                        return;
            		} else{
        	       		he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
        	    		send("", he);
        	    		return;
            		}
            	}else if(oper.equals("import")){
            		String token = he.getRequestHeaders().getFirst("token");
        			
        			//validar username token
                    String username = validate(token);
	                if(username == null) {
	                        he.sendResponseHeaders(403, 0);
	                        send("", he);
	                        return;
                    }
            		/**
            		 * TODO: sync
            		 */
                        int result = EWServer.dbm.events.importEvent(username);
            		if(result == 0){
                            //System.out.println("Import feito com sucesso.");
                            he.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                            send("", he);
                            return;
            		}
                        else if (result == 2) { //credenciais invalidas
                            he.sendResponseHeaders(HttpURLConnection.HTTP_CONFLICT, 0);
                            send("", he);
                            System.out.println("Erro no import: credenciais erradas.");
                            return;
                        }
                        else if (result == 1) { //calendario nao existe
                            he.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, 0);
                            send("", he);
                            System.out.println("Erro no import: calendario invalido.");
                            return;
                        }
                        else {
                            he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
                            send("", he);
                            System.out.println("Erro no import.");
                            return;
            		}
            	}
            	else{
    	       		he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
    	    		send("", he);
    	    		return;
            	}
            }
			
			
		} catch (JSONException e) {
			System.out.println("Error: JSONException handlePost.");
			//e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error: IOException handlePost.");
			//e.printStackTrace();
		}

	}
	
    private void send(String response, HttpExchange he) {
        try {
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(AccountsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }
    
    private String validate(String token) {


        //token não presente nos headers - access forbidden
        if (token == null) {
            return null;
        }

        return EWServer.dbm.sessions.getUsername(token);
    }

}
