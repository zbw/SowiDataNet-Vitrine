package controllers;


import actions.ContextAction;
import com.fasterxml.jackson.databind.JsonNode;
import model.Community;
import model.Institution;
import model.Item;
import model.RestResponse;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
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




    @Inject WSClient ws;

    /**
     * handle als parameter immer mitschleifen so kann beliebiges dspace rep eingebunden werden
     * @return
     */
    @With(ContextAction.class)
    public F.Promise<Result> discover(String institute, String basehandle, String handle) {
        Institution inst = (Institution) ctx().args.get("institution");
        handle = inst.handle;
        String params = queryString();
        String query = request().getQueryString("query");
        return search(inst, query, params);
    }

    @With(ContextAction.class)
    public F.Promise<Result> search(String institution) {
        Institution inst = (Institution) ctx().args.get("institution");
        String query = request().getQueryString("query");
        return search(inst, query, null);
    }
    public F.Promise<Result> search(final Institution inst,final String query,final String params) {
        F.Promise<String> xmlPromise = Discovery.getXML(ws, inst, query, params);
        F.Promise<String> facetsPromise = Discovery.facetBox(xmlPromise, inst);
        F.Promise<String> resultsPromise = Discovery.resultList(xmlPromise, inst);

        F.Promise<Result> result =
                F.Promise.sequence(facetsPromise, resultsPromise).map(components -> {
                    return ok(views.html.results.render(components.get(0), components.get(1), inst, query, getCommunity(inst)));
                });
        return result;

    }

    public static Community getCommunity(Institution inst) {
        RestResponse response = Community.findInstitutionByHandle(inst);
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

    @With(ContextAction.class)
    public Result showItem(String institution, String community, String handle) {
        Institution inst = (Institution) ctx().args.get("institution");
        StringBuilder contentString = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
           String baseRestUrl = inst.prot+"://"+inst.host+":"+inst.port+"/rest/handle/" + inst.basehandle +"/" + handle + "?expand=metadata,bitstreams";
            conn = connectToURL (baseRestUrl);
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

            return ok(views.html.item.detail.render(item, inst,getCommunity(inst)));
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
        URL url = new URL(endpoint);

        if(url.getProtocol().contains("https")) {
            conn = (HttpsURLConnection) url.openConnection();
            Logger.info("https");
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");


        if (conn.getResponseCode() != 200) {
            throw new MalformedURLException("Non-200 response: " + conn.getResponseMessage() + " for: " + conn.getURL());
        }

        return conn;
    }

}
