/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ewserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Diogo
 */
public class CompaniesHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {

        if (he.getRequestMethod().toLowerCase().equals("post")) {
            handlePost(he);
        } else if (he.getRequestMethod().toLowerCase().equals("get")) {
            handleGet(he);
        } else {
            he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
            he.getResponseBody().close();
        }
    }

    /**
     * Handler do método POST.
     * Utilizado para actualizar info da conta
     * 
     * 200 OK: sucesso
     * 403 ACCESS FORBIDDEN: token nao presente no header / token inválida
     * 400 BAD REQUEST: body com formato incorrecto
     * @param he 
     */
    private void handlePost(HttpExchange he) {
        try {
            InputStream is = he.getRequestBody();
            JSONObject body = new JSONObject(read(is));

            String token = he.getRequestHeaders().getFirst("token");

            /*
            //token não presente nos headers - access forbidden
            if (token == null) {
                he.sendResponseHeaders(403, 0);
                send("", he);
                return;
            }

            //token inválida - access forbidden
            String username = EWServer.dbm.sessions.getUsername(token);
            if (username == null) {
                he.sendResponseHeaders(403, 0);
                send("", he);
                return;
            }*/
            
            //valida a token
            String username = validate(token);
            if(username == null) {
                he.sendResponseHeaders(403, 0);
                send("", he);
                return;
            }

            //em caso de sucesso...
            if (EWServer.dbm.companies.update(body, username)) {
                he.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                send("", he);
                return;
            } else {
                he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                send("", he);
                return;
            }


        } catch (IOException ex) {
            System.out.println("ERROR: Companies' Post Handler: IO Exception");
            //Logger.getLogger(CompaniesHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            System.out.println("ERROR: Companies' Post Handler: Received malformed JSON.");
            //Logger.getLogger(CompaniesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Handler do método GET
     * Utilizado para pesquisa de empresas, consulta da informação detalhada de um empresa ou
     * ver informação completa de uma conta (acesso restrito a' propria empresa)
     * @param he 
     */
    private void handleGet(HttpExchange he) {
        try {
            //parsing dos argumentos
            String[] args = new String[0];
            if (he.getRequestURI().getQuery() != null) {
                args = he.getRequestURI().getQuery().split("&");
            }

            String oper = "", idc = "", query = "";
            for (int i = 0; i < args.length; i++) {
                if (args[i].split("=")[0].toLowerCase().equals("q")) {
                    query = args[i].split("=")[1];
                } else if (args[i].split("=")[0].toLowerCase().equals("oper")) {
                    oper = args[i].split("=")[1];
                } else if (args[i].split("=")[0].toLowerCase().equals("idc")) {
                    idc = args[i].split("=")[1];
                }
            }

            //faltam argumentos
            if (oper.equals("")
                    || (oper.equals("viewcompany") && idc.equals(""))
                    || (oper.equals("searchcompany") && query.equals(""))) {
                he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                send("", he);
                return;
            }

            // OPER - VIEWCOMPANY
            if (oper.equals("viewcompany")) {

                //pesquisa info da empresa
                JSONObject company = EWServer.dbm.companies.get(idc, 1);

                //em caso de erro..
                if (company == null) {
                    he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
                    send("", he);
                    return;
                } //caso a empresa nao exista...
                else if (company.length() == 0) {
                    he.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, 0);
                    send("", he);
                    return;
                } //em caso de sucesso...
                else {
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, company.toString().length());
                    send(company.toString(), he);
                    return;
                }
            } // OPER - SEARCHCOMPANY
            else if (oper.equals("searchcompany")) {
                JSONArray companies = EWServer.dbm.companies.find(query);

                //em caso de erro..
                if (companies == null) {
                    he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
                    send("", he);
                    return;
                } //caso nao sejam encontrados resultados...
                else if (companies.length() == 0) {
                    he.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, 0);
                    send("", he);
                    return;
                } //em caso de sucesso...
                else {
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, companies.toString().length());
                    send(companies.toString(), he);
                    return;
                }
            } // OPER - GETCOMPANY
            else if (oper.equals("getcompany")) {
                String token = he.getRequestHeaders().getFirst("token");
                String username = validate(token);

                // token/sessao invalida
                if (username == null) {
                    he.sendResponseHeaders(403, 0);
                    send("", he);
                    return;
                }
                String company_idc = EWServer.dbm.companies.getIDC(username);
                
                JSONObject company = EWServer.dbm.companies.get(company_idc, 2);
                
                he.sendResponseHeaders(HttpURLConnection.HTTP_OK, company.toString().length());
                send(company.toString(), he);
                return;
                
            } // OPER -INVALIDO
            else {
                he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                send("", he);
                return;
            }

        } catch (IOException ex) {
            Logger.getLogger(CompaniesHandler.class.getName()).log(Level.SEVERE, null, ex);
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

    private void send(String response, HttpExchange he) {
        try {
            OutputStream os = he.getResponseBody();

            os.write(response.getBytes());
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(AccountsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Valida a token indicada, retornando o username correspondente a' sessao.
     * Caso a validação nao tenha sucesso (token nula ou sessao invalida).
     *  e' retornado o valor null.
     * 
     * @param token
     * @return 
     */
    private String validate(String token) {

        //token não presente nos headers - access forbidden
        if (token == null) {
            return null;
        }

        return EWServer.dbm.sessions.getUsername(token);
    }
}
