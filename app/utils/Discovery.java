package utils;

import com.typesafe.config.ConfigFactory;
import model.Institution;
import play.Play;
import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;


/**
 * Created by Ott Konstantin on 26.10.2015.
 */
public class Discovery extends Controller {





    public static String baseRestUrl = "http://dspace.vm:8080/rest";
    public static String baseProt = ConfigFactory.load().getString("baseprot");
    public static String baseHost = ConfigFactory.load().getString("basehost");
    public static String basePort = ConfigFactory.load().getString("baseport");
    public static String basePath = ConfigFactory.load().getString("basepath");
    public static String baseHandle = ConfigFactory.load().getString("basehandle");

    public Discovery() {}

    public static F.Promise<String> getXML(WSClient ws,String handle, String query, String params) {
        String port = "";
        if (basePort!=null && !basePort.equals("")) {
            port = ":" + basePort;
        }
        String querypar = "";
        if (params != null) {
            querypar += params;
        }
        if (query != null) {
            querypar += "query="+ URLEncoder.encode(query)+ "&";
        }

        querypar += "XML";
        String url = baseProt+"://"+baseHost+port+"/"+basePath+"/handle/" + baseHandle +"/"+handle+"/discover";
        F.Promise<WSResponse> wsPromise = ws.url(url).setQueryString(querypar).get();
        F.Promise<String> xml = wsPromise.map(response -> {

            // got some problems with this namespace. So I remove it...
            //Document document = response.asXml();
            //Source xml = new DOMSource(document);
            String resultstring = response.getBody();
            resultstring = resultstring.replaceFirst("<document xmlns=\"http://di.tamu.edu/DRI/1.0/\" xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" version=\"1.1\">", "<document xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\">");
            System.out.println("getXML");
            return resultstring;
        });
        return xml;
    }

    public static F.Promise<String> facetBox(F.Promise<String> xmlPromise, Institution inst) {
        String handle = inst.handle;
        F.Promise<String> facetbox = xmlPromise.map(xml -> {
            Source xmlsource = new StreamSource(new StringReader(xml));
            String body = "";
            Source xslt = new StreamSource(Play.application().classloader().getResourceAsStream("xslt/discovery_facet.xsl"));
            TransformerFactory transFact = TransformerFactory.newInstance();
            Transformer trans = transFact.newTransformer(xslt);
            trans.setParameter("lang", "de");
            trans.setParameter("path", "./conf/xslt/") ;
            trans.setParameter("institut.id", inst.id) ;
            trans.setParameter("handle", inst.handle) ;
            trans.setParameter("basehandle", baseHandle) ;
            StringWriter writer = new StringWriter();
            StreamResult s_result = new StreamResult(writer);
            trans.transform(xmlsource,s_result);
            body = writer.toString();
            return body;
        });
        return facetbox;
    }


    public static F.Promise<String> resultList(F.Promise<String> xmlPromise, Institution inst) {
        F.Promise<String> resultListPromise = xmlPromise.map(xml -> {
            Source xmlsource = new StreamSource(new StringReader(xml));
            String resultListBody = "";
            Source xslt = new StreamSource(Play.application().classloader().getResourceAsStream("xslt/discovery_results.xsl"));
            TransformerFactory transFact = TransformerFactory.newInstance();
            Transformer trans = transFact.newTransformer(xslt);
            trans.setParameter("lang", "de");
            trans.setParameter("path", "./conf/xslt/") ;
            trans.setParameter("handle", inst.handle) ;
            trans.setParameter("institut.id", inst.id) ;
            trans.setParameter("basehandle", baseHandle) ;
            StringWriter writer = new StringWriter();
            StreamResult s_result = new StreamResult(writer);
            trans.transform(xmlsource,s_result);
            resultListBody = writer.toString();
            return resultListBody;
        });
        return resultListPromise;
    }
}
