package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;
import model.Community;
import model.Item;
import model.RestResponse;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Discovery;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class SearchApplication extends Controller {

    public static String baseRestUrl = "http://dspace.vm:8080/rest";
    public static String baseProt = ConfigFactory.load().getString("baseprot");
    public static String baseHost = ConfigFactory.load().getString("basehost");
    public static String basePort = ConfigFactory.load().getString("baseport");
    public static String basePath = ConfigFactory.load().getString("basepath");
    public static String baseHandle = ConfigFactory.load().getString("basehandle");

    @Inject WSClient ws;

    /**
     * handle als parameter immer mitschleifen so kann beliebiges dspace rep eingebunden werden
     * @return
     */

    public F.Promise<Result> discover(String basehandle, String handle) {
        String params = queryString();
        String query = request().getQueryString("query");
        return search(handle, query, params);
    }
    public F.Promise<Result> search() {
        String query = request().getQueryString("query");
        String handle = request().getQueryString("handle");
        return search(handle, query, null);
    }
    public F.Promise<Result> search(final String handle,final String query,final String params) {
        F.Promise<String> xmlPromise = Discovery.getXML(ws, handle, query, params);
        F.Promise<String> facetsPromise = Discovery.facetBox(xmlPromise, handle);
        F.Promise<String> resultsPromise = Discovery.resultList(xmlPromise, handle);

        F.Promise<Result> result =
                F.Promise.sequence(facetsPromise, resultsPromise).map(components -> {
                    return ok(views.html.results.render(components.get(0), components.get(1), handle, query,getCommunity(handle)));
                });
        return result;

    }

    public static Community getCommunity(String handle) {
        RestResponse response = Community.findByHandle(baseHandle+"/"+handle);
        if(response.modelObject instanceof Community) {
            return (Community) response.modelObject;
        } else {
            return new Community();
        }
    }


    private String queryString() {
        String result = "";
        Map<String, String[]> query = request().queryString();
        for (String key : query.keySet()) {
            for (String value : query.get(key)) {
                result += key+"="+ value+"&";
            }
        }

        return result;
    }

    public Result showItem(String community, String handle) {
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            conn = connectToURL ("handle/" + baseHandle +"/" + handle + "?expand=metadata,bitstreams");
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output;
            while ((output = reader.readLine()) != null) {
                contentString.append(output);
            }

            JsonNode node = Json.parse(contentString.toString());
            Item item = new Item();

            if (node.size() > 0) {
                item = Item.parseItemFromJSON(node);
            }

            return ok(views.html.item.detail.render(item, getCommunity(community)));
        } catch (MalformedURLException e) {
            return badRequest(e.getMessage());
        } catch (IOException e) {
            return internalServerError(e.getMessage());
        } finally {

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }

            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static HttpURLConnection connectToURL(String endpoint) throws IOException {


        // Now you can access an https URL without having the certificate in the truststore

        HttpURLConnection conn;
        URL url = new URL(baseRestUrl + "/" + endpoint);

        if(url.getProtocol().contains("https")) {
            conn = (HttpsURLConnection) url.openConnection();
            Logger.info("https");
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");


        if (conn.getResponseCode() != 200) {
            throw new MalformedURLException("Non-200 response: " + conn.getResponseMessage());
        }

        return conn;
    }

}
