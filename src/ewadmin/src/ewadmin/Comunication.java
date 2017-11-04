package ewadmin;

import java.io.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Nuno Marques
 *
 */
public class Comunication {
	
	private static String logintoken;
	private static String loginserver;
	
	public static String Login(String user, String password, String ipserver) throws Exception {
		logintoken="";
		loginserver=ipserver;
        String url = loginserver + "/accounts";

        final HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
        HttpPost httpPost = new HttpPost(url);

        JSONObject jsonoper=new JSONObject();
        JSONObject jsonlogin=new JSONObject();
        jsonlogin.put("pass", password);
        jsonlogin.put("username", user);
        jsonoper.put("account", jsonlogin);
        jsonoper.put("oper", "login");
        String POSTText = jsonoper.toString();
        System.out.println(POSTText);

        StringEntity entity = new StringEntity(POSTText, "UTF-8");
        BasicHeader basicHeader = new BasicHeader(HTTP.CONTENT_TYPE, "application/json");
        httpPost.getParams().setBooleanParameter("http.protocol.expect-continue", false);
        entity.setContentType(basicHeader);
        httpPost.setEntity(entity);
        
        HttpResponse response = httpClient.execute(httpPost);
        InputStream instream = response.getEntity().getContent();
        int statusResponse=response.getStatusLine().getStatusCode();
        if(statusResponse!=200)
        	throw new Exception(Integer.toString(statusResponse));
        String buffer=read(instream);
        System.out.println(buffer);
        logintoken=buffer.substring(buffer.indexOf("=")+1);
		return Integer.toString(statusResponse);
	}

	public static String[][] listAccounts(String textSearch, String operation) throws Exception{

    	HttpResponse response=null;
    	HttpGet httpget=null;
        String url = loginserver + "/accounts";
        String textSearchtrimm = textSearch.replaceAll(" ", "%20");
        //System.out.println(textSearchtrimm);

        final HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
    	if(textSearch.equalsIgnoreCase(""))
    		httpget = new HttpGet(url);
    	else
    		httpget = new HttpGet(url+"?q="+textSearchtrimm+"&oper="+operation);

    	httpget.setHeader("token", getLogintoken());
        
        response = httpClient.execute(httpget);
        int statusResponse=response.getStatusLine().getStatusCode();
        if(statusResponse!=200)
        	throw new Exception(Integer.toString(statusResponse));
        InputStream instream = response.getEntity().getContent();
        String buffer=read(instream);
        System.out.println(buffer);
        
        JSONArray arrayReceve = new JSONArray(buffer);
        String[][] listReceve = new String[arrayReceve.length()][3];
        for(int i=0;i<arrayReceve.length();i++){
        	JSONObject objectAccount = arrayReceve.getJSONObject(i);
         	try {
				listReceve[i][0] = objectAccount.getString("username");
				try {
					listReceve[i][1] = objectAccount.getString("nome_emp");
				} catch (JSONException e) {
					listReceve[i][1]="";
				}
				try {
					listReceve[i][2] = objectAccount.getString("tipo_conta");
				} catch (JSONException e) {
					listReceve[i][2]="";
				}
			} catch (JSONException e1) {
	        	System.out.println("User error.");
			}
        }
        return listReceve;
	}
	
	public static String[] newAccount(String newUser) throws Exception{
    	HttpResponse response=null;
        String url = loginserver + "/accounts";

        final HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("token", getLogintoken());
        
        JSONObject jsonnewaccount=new JSONObject();
        jsonnewaccount.put("username", newUser);
        String GETText = jsonnewaccount.toString();
        System.out.println(GETText);
        
        StringEntity entity = new StringEntity(GETText, "UTF-8");
        BasicHeader basicHeader = new BasicHeader(HTTP.CONTENT_TYPE, "application/json");
        httpPut.getParams().setBooleanParameter("http.protocol.expect-continue", false);
        entity.setContentType(basicHeader);
        httpPut.setEntity(entity);
        response = httpClient.execute(httpPut);
        int statusResponse=response.getStatusLine().getStatusCode();
        if(statusResponse!=200)
        	throw new Exception(Integer.toString(statusResponse));
        InputStream instream = response.getEntity().getContent();
        String buffer=read(instream);
        System.out.println(buffer);
        
        String[] newAccountReceve = new String[2];
        JSONObject objectAccount = new JSONObject(buffer);
    	newAccountReceve[0] = objectAccount.getString("username");
    	newAccountReceve[1] = objectAccount.getString("pass");
        
        return newAccountReceve;
	}
	
	public static String[] resetPass(String user) throws Exception {
    	HttpResponse response=null;
        String url = loginserver + "/accounts";

        final HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("token", getLogintoken());
        
        JSONObject jsonoper=new JSONObject();
        JSONObject jsonreset=new JSONObject();
        jsonreset.put("username", user);
        jsonoper.put("account", jsonreset);
        jsonoper.put("oper", "resetpass");
        String POSTText = jsonoper.toString();
        System.out.println(POSTText);
        
        StringEntity entity = new StringEntity(POSTText, "UTF-8");
        BasicHeader basicHeader = new BasicHeader(HTTP.CONTENT_TYPE, "application/json");
        httpPost.getParams().setBooleanParameter("http.protocol.expect-continue", false);
        entity.setContentType(basicHeader);
        httpPost.setEntity(entity);
        response = httpClient.execute(httpPost);
        int statusResponse=response.getStatusLine().getStatusCode();
        if(statusResponse!=200)
        	throw new Exception(Integer.toString(statusResponse));
        InputStream instream = response.getEntity().getContent();
        String buffer=read(instream);
        System.out.println(buffer);
        
        String[] resetReceve = new String[2];
        JSONObject objectAccount = new JSONObject(buffer);
    	resetReceve[0] = objectAccount.getString("username");
    	resetReceve[1] = objectAccount.getString("pass");
        
        return resetReceve;
	}

	public static String deletAccount(String user) throws Exception {
    	HttpResponse response=null;
        String url = loginserver + "/accounts";
        String usertrimm = user.replaceAll(" ", "%20");
        
        final HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
        HttpDelete httpDelete = new HttpDelete(url+"?user="+usertrimm);
        httpDelete.setHeader("token", getLogintoken());

        response = httpClient.execute(httpDelete);
        int statusResponse=response.getStatusLine().getStatusCode();
        if(statusResponse!=200)
        	throw new Exception(Integer.toString(statusResponse));

		return Integer.toString(statusResponse);
   	}

    private static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 2000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }

	public void setLogintoken(String logintok) {
		logintoken = logintok;
	}

	public static String getLogintoken() {
		return logintoken;
	}

}
