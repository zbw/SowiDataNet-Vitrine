package controllers;


import com.typesafe.config.ConfigFactory;
import model.Community;
import model.RestResponse;
import play.Logger;
import play.Play;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Discovery;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Application extends Controller {

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

    public Promise<Result> index() {
        return index("27788");
    }

    public Promise<Result> index(String handle) {


        Promise<String> xmlPromise = Discovery.getXML(ws, handle, null, null);
        Promise<String> facetsPromise = Discovery.facetBox(xmlPromise, handle);
        Promise<String> facetsPromise2 = Discovery.facetBox(xmlPromise, handle);
        Promise<Result> result =
                Promise.sequence(facetsPromise, facetsPromise2).map(components -> {
                   System.out.println("all fetched");
                    return ok(views.html.institution.render(components.get(0), components.get(1), handle,getCommunity(handle)));
                });
        return result;
    }

    public Community getCommunity(String handle) {
        RestResponse response = Community.findByHandle(baseHandle+"/"+handle);
        if(response.modelObject instanceof Community) {
            return (Community) response.modelObject;
        } else {
            return new Community();
        }
    }






}
