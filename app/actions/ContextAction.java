package actions;

import com.typesafe.config.ConfigFactory;
import model.Institution;
import play.Logger;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Created by Ott Konstantin on 15.12.2015.
 */
public class ContextAction extends play.mvc.Action.Simple {
    @Override
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        Logger.info("Calling action for " + ctx);
        Institution institution = new Institution();
        String institute = ctx.request().path();
        if (institute.length()>1) {
            institute = institute.substring(1);
            if (institute.indexOf("/") >= 0) {
                institute = institute.substring(0, institute.indexOf("/"));
            }

            institution.id = institute;
            institution.handle = ConfigFactory.load().getString(institute + ".handle");
            institution.name = ConfigFactory.load().getString(institute + ".name");

            institution.prot = ConfigFactory.load().getString(institute + ".prot");
            institution.host = ConfigFactory.load().getString(institute + ".host");
            institution.port = ConfigFactory.load().getString(institute + ".port");
            institution.basepath = ConfigFactory.load().getString(institute + ".basepath");
            institution.basehandle = ConfigFactory.load().getString(institute + ".basehandle");
            institution.metafields = ConfigFactory.load().getStringList(institute+".metadata");
        } else {
            institution.id = "";
            institution.handle = "27788";
            institution.name = "Sowidata Vitrine";

        }
        ctx.args.put("institution", institution);
        return delegate.call(ctx);
    }
}
