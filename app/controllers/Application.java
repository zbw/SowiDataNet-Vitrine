package controllers;


import actions.ContextAction;
import model.Institution;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import utils.Discovery;

import javax.inject.Inject;

public class Application extends Controller {

    //public static String baseRestUrl = "http://dspace.vm:8080/rest";

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


        Promise<String> xmlPromise = Discovery.getXML(ws, inst, null, null);
        Promise<String> facetsPromise = Discovery.facetBox(xmlPromise, inst);
        //Promise<String> facetsPromise2 = Discovery.facetBox(xmlPromise, handle);
        Promise<Result> result =
                Promise.sequence(facetsPromise).map(components -> {
                  return ok(views.html.institution.render(components.get(0),inst, SearchApplication.getCommunity(inst)));
                });
        return result;
    }



}
