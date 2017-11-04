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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import org.json.JSONArray;

/**
 *
 * @author Diogo
 */
public class AccountsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
    	
        if (he.getRequestMethod().toLowerCase().equals("post")) {
            handlePost(he);
        } else if (he.getRequestMethod().toLowerCase().equals("put")) {
            handlePut(he);
        } else if (he.getRequestMethod().toLowerCase().equals("get")) {
            handleGet(he);
        } else if (he.getRequestMethod().toLowerCase().equals("delete")) {
            handleDelete(he);
        }

    }

    /**
     * Handler do método POST : logins e reset de passwords
     * @param he 
     */
    private void handlePost(HttpExchange he) {
        InputStream is = he.getRequestBody();
        JSONObject body;

        try {
            body = new JSONObject(read(is));

            String oper = body.getString("oper");

            /**
             * LOGIN
             */
            if (oper.toLowerCase().equals("login")) {
                JSONObject account = body.getJSONObject("account");
                String username = account.getString("username");
                String pass = account.getString("pass");
                String token;
                String response = "";

                //conta válida
                if (EWServer.dbm.accounts.isValid(username, pass)) {

                    //gera token
                    int i = 0;
                    boolean success = false;

                    do {
                        token = new BigInteger(330, EWServer.random).toString(32);
                        success = EWServer.dbm.sessions.add(username, token);
                        i++;
                    } while (i <= 10 && !success);

                    //resposta - unexpected error
                    if (!success) {
                        he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);

                    } //resposta - sucesso
                    else {
                        response = "Token=" + token;

                        he.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());

                    }

                } //resposta 403 Access Forbidden - invalid username/password
                else {
                    he.sendResponseHeaders(403, response.length());
                }

                //envia resposta
                OutputStream os = he.getResponseBody();

                os.write(response.getBytes());
                os.close();
                return;

            } /**
             * RESET PASSWORD: acesso restrito a admins
             */
            else if (oper.toLowerCase().equals("resetpass")) {

                String token = he.getRequestHeaders().getFirst("token");
                String response = "";
                String admin_username = EWServer.dbm.sessions.getUsername(token);

                JSONObject account = body.getJSONObject("account");
                String username = account.getString("username");

                //session token inválida -> access forbidden
                if (username == null) {
                    he.sendResponseHeaders(403, response.length());
                } //verifica o tipo da conta de quem enviou a request
                else {
                    String tipo = EWServer.dbm.accounts.getType(admin_username);

                    //se não for um admin -> access forbidden
                    if (!tipo.equals("admin")) {
                        he.sendResponseHeaders(403, response.length());
                    } else {
                        //gera nova password (10 caracteres)
                        String pass = new BigInteger(50, EWServer.random).toString(32);

                        //em caso de sucesso..
                        if (EWServer.dbm.accounts.alterPass(username, pass)) {
                            JSONObject new_account = new JSONObject();
                            new_account.put("username", username);
                            new_account.put("pass", pass);
                            response += new_account.toString();
                            he.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
                        } // em caso de erro..
                        else {
                            he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, response.length());
                        }

                    }
                }

                OutputStream os = he.getResponseBody();

                os.write(response.getBytes());
                os.close();
                return;
            }


        } catch (IOException ex) {
            Logger.getLogger(AccountsHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccountsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Handler do método PUT
     * Usado para registos de novas contas - acesso restrito a admins
     *  403 ACCESS FORBIDDEN: token nao presente no header / token inválida / o user nao é um admin
     *  409 CONFLICT: já existe uma conta com o mesmo username
     *  200 OK: sucesso
     * @param he 
     */
    private void handlePut(HttpExchange he) {
        try {
            InputStream is = he.getRequestBody();
            JSONObject body = new JSONObject(read(is));
            String response = "";

            String token = he.getRequestHeaders().getFirst("token");

            //session token não presente - access forbidden
            if (token == null || token.equals("")) {
                he.sendResponseHeaders(403, response.length());
                send(response, he);
                return;
            }

            String admin_username = EWServer.dbm.sessions.getUsername(token);

            //session token inválida - access forbidden
            if (admin_username == null) {
                he.sendResponseHeaders(403, response.length());
                send(response, he);
                return;
            }

            //sessão nao pertence a um admin - access forbidden
            String tipo = EWServer.dbm.accounts.getType(admin_username);
            if (!tipo.equals("admin")) {
                he.sendResponseHeaders(403, response.length());
                send(response, he);
                return;
            }

            String pass = new BigInteger(50, EWServer.random).toString(32);
            body.put("pass", pass);

            //em caso de sucesso...
            if (EWServer.dbm.accounts.add(body)) {
                he.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
                response += body.toString();
                send(response, he);
                return;
            } //caso o username já exista -  409 CONFLICT
            else {
                he.sendResponseHeaders(HttpURLConnection.HTTP_CONFLICT, response.length());
                send(response, he);
                return;
            }


        } catch (JSONException ex) {
            Logger.getLogger(AccountsHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AccountsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    /**
     * Handler do método GET.
     * Usado para pesquisar contas - acesso restrito a admins - por nome da respectiva
     * empresa, pelo username ou sem parametros.
     * 
     * 200 OK
     * 400 BAD REQUEST: missing query
     * 403 ACCESS FORBIDDEN: token nao presente no header / token inválida / o user nao é um admin
     * 204 NO CONTENT: nao foram encontrados resultados
     * @param he 
     */
    private void handleGet(HttpExchange he) {
        try {
            InputStream is = he.getRequestBody();
            JSONObject body;
            String response = "";
            String oper = "";
            JSONArray results = new JSONArray();

            //processa token
            String token = he.getRequestHeaders().getFirst("token");

            //verifica validade da token
            if (!isAdmin(token)) {
                he.sendResponseHeaders(403, response.length());
                send(response, he);
                return;
            }

            //lê argumentos
            String[] args = new String[0];
            if (he.getRequestURI().getQuery() != null) {
                args = he.getRequestURI().getQuery().split("&");
            }
            String query = "";
            for (int i = 0; i < args.length; i++) {
                if (args[i].split("=")[0].toLowerCase().equals("q")) {
                    query = args[i].split("=")[1];
                }
                else if(args[i].split("=")[0].toLowerCase().equals("oper")) {
                    oper = args[i].split("=")[1];
                }
            }

            //se a query nao for especificada...
            if (query.equals("")) {
                results = EWServer.dbm.accounts.findByUsername(query);
            } //se a pesquisa for por nome...
            else if (oper.equals("by_name")) {
                results = EWServer.dbm.accounts.findByName(query);
            } //se a pesquisa for por username...
            else if (oper.equals("by_username")) {
                results = EWServer.dbm.accounts.findByUsername(query);
            } //se existir query, mas nao for indicado o oper
            else {
                he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, response.length());
                send(response, he);
                return;
            }

            //Retorna os resultados encontrados
            if (results.length() == 0) {
                he.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, 0);
                send("", he);
                return;
            } else {
                response = results.toString();
                he.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
                send(response, he);
                return;
            }

        } /*catch (JSONException ex) {
            Logger.getLogger(AccountsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }*/ catch (IOException ex) {
            Logger.getLogger(AccountsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handler do método DELETE.
     * Usado para apagar contas - acesso restrito a admins
     * 
     * 200 OK
     * 400 BAD REQUEST: missing query
     * 404 NOT FOUND: username indicado nao foi encontrado
     * 403 ACCESS FORBIDDEN: token nao presente no header / token inválida / o user nao é um admin
     * 500 INTERNAL ERROR: erro
     * @param he 
     */
    private void handleDelete(HttpExchange he) {
        try {
            //processa token
            String token = he.getRequestHeaders().getFirst("token");

            //verifica validade da token
            if (!isAdmin(token)) {
                he.sendResponseHeaders(403, 0);
                send("", he);
                return;
            }

            //lê argumentos
            String[] args = new String[0];
            if (he.getRequestURI().getQuery() != null) {
                args = he.getRequestURI().getQuery().split("&");
            } 
            
            String username = "";
            for (int i = 0; i < args.length; i++) {
                if (args[i].split("=")[0].toLowerCase().equals("user")) {
                    username = args[i].split("=")[1];
                    break;
                }
            }
            
            //se o username a apagar não for indicado - bad request
            if(username.equals("")) {
                he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                send("", he);
                return;
            }
            
            //username não encontrado
            if(!EWServer.dbm.accounts.exists(username)) {
                he.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
                send("", he);
                return;
            }
            
            //em caso de sucesso
            if(EWServer.dbm.accounts.delete(username)) {
                he.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                send("", he);
                return;
            }
            //em caso de erro
            else {
                he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
                send("", he);
                return;
            }
            


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

    private void send(String response, HttpExchange he) {
        try {
            OutputStream os = he.getResponseBody();

            os.write(response.getBytes());
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(AccountsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean isAdmin(String token) {

        //session token não presente
        if (token == null || token.equals("")) {
            return false;
        }

        String admin_username = EWServer.dbm.sessions.getUsername(token);

        //session token inválida
        if (admin_username == null) {
            return false;
        }

        //sessão nao pertence a um admin
        String tipo = EWServer.dbm.accounts.getType(admin_username);
        if (!tipo.equals("admin")) {
            return false;
        }

        return true;
    }
}
