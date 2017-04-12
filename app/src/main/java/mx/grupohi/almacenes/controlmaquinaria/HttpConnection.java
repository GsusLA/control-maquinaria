package mx.grupohi.almacenes.controlmaquinaria;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;


/**
 *  Clase que hace las conexiones a la API para la descarga de información
 */

class HttpConnection {


    /**
     * Creado por JFEsquivel on 07/10/2016.
     *
     *  Metodo post para el inicio de sesión a la aplicación
     */
    static JSONObject POST(URL url, ContentValues values) throws JSONException {

        String response = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write(Util.      getQuery(values));
            bw.flush();

            int statusCode = conn.getResponseCode();
            Log.i("Status Code", String.valueOf(statusCode));
            if(statusCode != 200){
                return new JSONObject("{\n" +
                        "  \"error\": \"invalid_credentials\",\n" +
                        "  \"code\": 401\n" +
                        "}");
            }

            InputStream is = conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line, toAppend;

            toAppend = br.readLine() + "\n";
            sb.append(toAppend);
            while ((line = br.readLine()) != null) {
                toAppend = line + "\n";
                sb.append(toAppend);
            }
            is.close();
            response = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("response: "+response);
        return  new JSONObject(response);
    }

    /**
     * Metodo GET para sincronizar la maquinaria registrada en cada obra
     *
     * @param url Link en el cual se hace la peticion para la sincronización
     * @param params datos necesarios para la sincronización de la maquinaria.
     * @return JSONObject con los datos devueltos por el web service y que contien los datos
     * de la maquinaria disponible de cda obra
     * @throws IOException se muestra si existe un error de comunicación con el servicio web
     * @throws JSONException se muestra si existe un error de conversion a Json de los datos
     * recuperados
     */
    static JSONObject GET(URL url, ContentValues params) throws IOException, JSONException {
        String body = " ";

        try {

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", "Bearer " + params.get("token"));
            urlConnection.setRequestProperty("database_name", params.get("base").toString());
            urlConnection.setRequestProperty("id_obra", params.get("idObra").toString());

            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            String codigoRespuesta = Integer.toString(urlConnection.getResponseCode());
            if(codigoRespuesta.equals("200")){//Vemos si es 200 OK y leemos el cuerpo del mensaje.
                body = readStream(urlConnection.getInputStream());
            }else{
                body = "{\"error\":\""+codigoRespuesta+"\"}";
            }
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            body = e.toString(); //Error URL incorrecta
        } catch (SocketTimeoutException e){
            body = e.toString(); //Error: Finalizado el timeout esperando la respuesta del servidor.
        } catch (Exception e) {
            body = e.toString();//Error diferente a los anteriores.
        }
        return new JSONObject(body);
    }

    /**
     * Metodo GET para el cierre se sesión
     * @param url Link en el cual se hace la peticion para la sincronización
     * @param token llave con la cual se hace referencia ala sesión que se va a cerrar
     * @return JSONObject con los datos de respuesta del cierre de sesión.
     * @throws IOException se muestra si existe un error de comunicación con el servicio web
     * @throws JSONException se muestra si existe un error de conversion a Json de los datos
     */
    static JSONObject LOGOUT(URL url, String token) throws IOException, JSONException {
        String body = " ";

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", "Bearer " + token);

            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            String codigoRespuesta = Integer.toString(urlConnection.getResponseCode());
            if(codigoRespuesta.equals("200")){//Vemos si es 200 OK y leemos el cuerpo del mensaje.
                body = readStream(urlConnection.getInputStream());
            }else{
                body = "{\"message\":\"error\",\"status_code\":"+codigoRespuesta+"}";
            }
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            body = e.toString(); //Error URL incorrecta
        } catch (SocketTimeoutException e){
            body = e.toString(); //Error: Finalizado el timeout esperando la respuesta del servidor.
        } catch (Exception e) {
            body = e.toString();//Error diferente a los anteriores.
        }
        return new JSONObject(body);
    }

    private static String readStream(InputStream in) throws IOException{

        BufferedReader r = null;
        r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        if(r != null){
            r.close();
        }
        in.close();
        return total.toString();
    }
}
