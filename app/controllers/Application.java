package controllers;


import actions.ContextAction;
import com.fasterxml.jackson.databind.JsonNode;
import model.Institution;
import models.User;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import utils.Discovery;

import javax.inject.Inject;
import java.util.List;

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


        Promise<String> xmlPromise = Discovery.getXML(ws, inst, null);
        Promise<String> facetsPromise = Discovery.facetBox(xmlPromise, inst);
        //Promise<String> facetsPromise2 = Discovery.facetBox(xmlPromise, handle);
        Promise<Result> result =
                Promise.sequence(facetsPromise).map(components -> {
                  return ok(views.html.institution.render(components.get(0),inst, SearchApplication.getCommunity(inst)));
                });
        return result;
    }

    @With(ContextAction.class)
    public Result loginForm(String institution) {
        Institution inst = (Institution) ctx().args.get("institution");
        User user = new User();
        user = user.getUserFromSession(session());
        if(user == null || user.email() == null) {
            return ok(views.html.login.render(inst,user, "Login", "", "", ""));
        } else {
            return redirect(routes.SearchApplication.start(null));
        }
    }

    @With(ContextAction.class)
    public Promise<Result> login(String institution) {
        Institution inst = (Institution) ctx().args.get("institution");
        String baseRestUrl = inst.prot+"://"+inst.host+":"+inst.port+"/rest/login";
        WSRequest request = ws.url(baseRestUrl);

        DynamicForm requestData = Form.form().bindFromRequest();
        String email = requestData.get("email");
        String password = requestData.get("password");
        //get the token for rest
        JsonNode json = Json.newObject()
                .put("email", email)
                .put("password", password);
        F.Promise<String> token = request.setHeader("Content-Type", "application/json").post(json).map(response -> {
            return response.getBody();
        });
        //get the cookie for discover
        String startUrl = inst.prot+"://"+inst.host+":"+inst.port+"/xmlui";
        WSRequest startRequest = ws.url(startUrl);
        F.Promise<String> cookie = startRequest.get().map(response -> {
            List cookies = response.getCookies();
            String body = response.getBody();
            if (cookies.size()>0) {
                String cook = response.getCookie("JSESSIONID").getValue();
                //login
                String loginUrl = inst.prot+"://"+inst.host+":"+inst.port+"/xmlui/password-login";
                WSRequest loginRequest = ws.url(loginUrl);
                loginRequest.setHeader("Cookie", "JSESSIONID=" + cook);
                F.Promise<Boolean> ok = loginRequest.setContentType("application/x-www-form-urlencoded").post("login_email="+email+"&login_password="+password).map(lresponse -> {
                    return true;
                });
                return cook;
            } else {
                return "";
            }
        });

        try {
            String t = token.get(1000L);
            session().clear();
            if (!t.equals("")) {
                session("userToken", t);
                String jsession = cookie.get(1000L);
                if (!jsession.equals("")) {
                    session("JSESSIONID", jsession);
                }
                flash("success", "Logged in: " + t);
            } else {
                session().clear();
                flash("error" , "error loggin in");
            }
        } catch(F.PromiseTimeoutException pte) {
            session().clear();
            return index(inst.id);
        }
        return index(inst.id);
    }

    @With(ContextAction.class)
    public Promise<Result> logout(String institution) {
        Institution inst = (Institution) ctx().args.get("institution");
        String token = session().get("userToken");
        if (token != null && !token.equals("")) {
            String baseRestUrl = inst.prot + "://" + inst.host + ":" + inst.port + "/rest/logout";
            WSRequest request = ws.url(baseRestUrl);
            JsonNode json = Json.newObject()
                    .put("rest-dspace-token", token);
            F.Promise<String> result = request.setHeader("Content-Type", "application/json").post(json).map(response -> {
                return response.getBody();
            });
            try {
                String t = result.get(1000L);
                session().clear();
                flash("success", "Logged out");
            } catch(F.PromiseTimeoutException pte) {
                session().clear();
                flash("error", "Logged out");
                return index(inst.id);
            }
        }
        return index(inst.id);
    }



}
