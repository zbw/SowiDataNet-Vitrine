package controllers;


import actions.ContextAction;
import com.fasterxml.jackson.databind.JsonNode;
import model.*;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
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
import java.util.*;

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
        return search(inst, query, params,  handle);
    }

    @With(ContextAction.class)
    public F.Promise<Result> search(String institution) {
        Institution inst = (Institution) ctx().args.get("institution");
        String params = queryString();
        String query = request().getQueryString("query");
        return search(inst, query, params,  null);
    }
    public F.Promise<Result> search1(final Institution inst,final String query,final String params, String handle) {
        F.Promise<String> xmlPromise = Discovery.getXML(ws, inst, params);
        F.Promise<String> facetsPromise = Discovery.facetBox(xmlPromise, inst);
        F.Promise<String> resultsPromise = Discovery.resultList(xmlPromise, inst);
        F.Promise<String> facetListPromise = Discovery.availableFacets(xmlPromise, inst);
        List<Filter> filters = getFiltered(params, inst, handle);
        F.Promise<Result> result =
                F.Promise.sequence(facetsPromise, resultsPromise, facetListPromise).map(components -> {
                    Logger.debug("filters: "+ filters.toString());
                    return ok(views.html.results.render(components.get(0), components.get(1),components.get(2),inst, query, getCommunity(inst), filters));
                });
        return result;

    }
    public F.Promise<Result> search(final Institution inst,final String query,final String params, String handle) {
        F.Promise<String> xmlPromise = Discovery.getXML(ws, inst, params);
        // for showing selected filters in the facet bar
        List<Filter> filters = getFiltered(params, inst, handle);
        //List<Filter> filters = new ArrayList<>();
                F.Promise<Result> result = F.Promise.sequence(
                Discovery.facetBox(xmlPromise, inst),
                Discovery.resultList(xmlPromise, inst),
                Discovery.availableFacets(xmlPromise, inst))
                .map( list -> {

                    return ok(views.html.results.render(list.get(0), list.get(1),list.get(2),  inst, query, getCommunity(inst), filters));
                });
        return result;
    }

    private  List<Filter> getFiltered(String params, Institution inst, String handle) {
        final Set<Map.Entry<String,String[]>> entries = request().queryString().entrySet();
        List<Filter> filters = new ArrayList<>();
        for (Map.Entry<String,String[]> entry : entries) {
            final String key = entry.getKey();
            final String value = Arrays.toString(entry.getValue());
            if (key.startsWith("filtertype")) {
                String post = key.substring(key.indexOf("filtertype") + 10);
                if (post==null) post="";
                String valuekey = key.replaceAll("type","");
                if (!request().getQueryString(valuekey).equals("")) {
                    String linkparams = params.replaceAll(key + "=(.*?)&", "");
                    linkparams = linkparams.replaceAll("filter_relational_operator" + post + "=(.*?)&", "");
                    linkparams = linkparams.replaceAll("filter" + post + "=(.*?)&", "");
                    Filter filter = new Filter();
                    filter.queryWithoutFilter = "/" + inst.id + "/discover/" + inst.basehandle + "/" + handle + "/?" + linkparams;
                    filter.typ = request().getQueryString(key);
                    filter.filter = request().getQueryString(valuekey);
                    filter.relationalOperator = request().getQueryString("filter_relational_operator" + post);
                    filters.add(filter);
                }
            }
        }
        return filters;
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
    public F.Promise<Result> showItem(String institution, String community, String handle) {
        Institution inst = (Institution) ctx().args.get("institution");
        StringBuilder contentString = new StringBuilder();
        String baseRestUrl = inst.prot+"://"+inst.host+":"+inst.port+"/rest/handle/" + inst.basehandle +"/" + handle + "?expand=metadata,bitstreams";
        WSRequest request = ws.url(baseRestUrl);
        String token = session("userToken");
        if (token!=null) {
            request.setHeader("rest-dspace-token", token);
        }
        F.Promise<WSResponse> promiseOfResponse = request.get();
        F.Promise<Result> promiseOfResult = promiseOfResponse.map(response  -> {
                                        if (response.getStatus() == 200) {
                                            JsonNode node = response.asJson();
                                            Item item = new Item();
                                            if (node.size() > 0) {
                                                item = Item.parseItemFromJSON(node, inst);
                                            }
                                            return ok(views.html.item.detail.render(item, inst, getCommunity(inst)));
                                        } else {
                                            flash("error" , "no rights");
                                            return ok(views.html.login.render(inst,null, "Login", "", "", ""));
                                        }
                                        });
        return promiseOfResult;
    }

    @With(ContextAction.class)
    public Result showItem1(String institution, String community, String handle) {
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
                item = Item.parseItemFromJSON(node, inst);
            }

            return ok(views.html.item.detail.render(item, inst,getCommunity(inst)));
        } catch (MalformedURLException e) {
            return ok(views.html.item.detail.render(null, inst,getCommunity(inst)));
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
        //String token = "ee151264-598c-4b43-a935-56703a16e8cd";
        //conn.setRequestProperty("rest-dspace-token",token);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        System.out.println(conn.getResponseCode());

        if (conn.getResponseCode() != 200) {
            throw new MalformedURLException("Non-200 response: " +conn.getResponseCode() + " msg: " + conn.getResponseMessage() + " for: " + conn.getURL());
        }

        return conn;
    }

}
