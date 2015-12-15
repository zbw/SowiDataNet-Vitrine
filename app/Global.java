import play.GlobalSettings;
import play.api.mvc.Handler;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Http.Request;

import java.lang.reflect.Method;

/**
 * Created by Ott Konstantin on 15.12.2015.
 */
public class Global extends GlobalSettings {

    public Handler onRouteRequest(Http.RequestHeader request) {
        if (!request.path().startsWith("/assets")) {

        }
       return super.onRouteRequest(request);
    }

    public Action onRequest(Request request, Method actionMethod) {

        return super.onRequest(request, actionMethod);
    }
}
