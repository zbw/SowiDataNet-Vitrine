package controllers;


import actions.ContextAction;
import com.typesafe.config.ConfigFactory;
import model.Community;
import model.Institution;
import model.RestResponse;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import utils.Discovery;

import javax.inject.Inject;

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
    @With(ContextAction.class)
    public Result start() {
        Institution inst = (Institution) ctx().args.get("institution");
        return ok(views.html.start.render(inst));
    }

    @With(ContextAction.class)
    public Promise<Result> index(String institute) {
        Institution inst = (Institution) ctx().args.get("institution");
        String handle = inst.handle;

        Promise<String> xmlPromise = Discovery.getXML(ws, handle, null, null);
        Promise<String> facetsPromise = Discovery.facetBox(xmlPromise, inst);
        //Promise<String> facetsPromise2 = Discovery.facetBox(xmlPromise, handle);
        Promise<Result> result =
                Promise.sequence(facetsPromise).map(components -> {
                  return ok(views.html.institution.render(components.get(0),inst, getCommunity(inst.handle)));
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
