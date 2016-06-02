package utils;

import model.Facet;
import model.Filter;
import model.Institution;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import play.Logger;
import play.Play;
import play.libs.F;
import play.libs.XPath;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Ott Konstantin on 26.10.2015.
 */
public class Discovery extends Controller {

    public Discovery() {}

    public static F.Promise<String> getXML(WSClient ws,Institution inst, String params) {
        String port = "";
        if (inst.port!=null && !inst.port.equals("")) {
            port = ":" + inst.port;
        }

        if (params==null) {
            params = "XML";
        }   else {
            params += "XML";
        }
        //there has to be a filter param. Else the filterlist will not show
        params += "&submit=Los&filter_100=";
        String url = inst.prot+"://"+inst.host+port+"/"+inst.basepath+"/handle/" + inst.basehandle +"/"+inst.handle+"/discover";
        String cookie = session("JSESSIONID");
        WSRequest wsRequest =   ws.url(url);
        if (cookie!=null && !cookie.equals("")) {
            wsRequest =   ws.url(url +";jsessionid="+cookie);
            wsRequest.setHeader("Cookie", "JSESSIONID=" + cookie);
        }
        wsRequest.setQueryString(params);
        F.Promise<WSResponse> wsPromise = wsRequest.get();
        F.Promise<String> xml = wsPromise.map(response -> {

            // got some problems with this namespace. So I remove it...
            //Document document = response.asXml();
            //Source xml = new DOMSource(document);
            String resultstring = response.getBody();
            resultstring = resultstring.replaceFirst("<document xmlns=\"http://di.tamu.edu/DRI/1.0/\" xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" version=\"1.1\">", "<document xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\">");
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
            trans.setParameter("lang", ctx().lang().code());
            trans.setParameter("path", "./conf/xslt/") ;
            trans.setParameter("institut.id", inst.id) ;
            trans.setParameter("handle", inst.handle) ;
            trans.setParameter("basehandle", inst.basehandle) ;
            StringWriter writer = new StringWriter();
            StreamResult s_result = new StreamResult(writer);
            trans.transform(xmlsource,s_result);
            body = writer.toString();
            return body;
        });
        return facetbox;
    }

    public static F.Promise<String> availableFacets(F.Promise<String> xmlPromise, Institution inst) {
        String handle = inst.handle;
        F.Promise<String> facets = xmlPromise.map(xml ->{
            String facetbody = "";
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try
            {
                //get the available facets
                ArrayList<Facet> facetlist = new ArrayList<Facet>();
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse( new InputSource( new StringReader( xml ) ) );
                NodeList nodes = XPath.selectNodes("//cell/field[@id='aspect.discovery.SimpleSearch.field.filtertype_1']", doc);
                if (nodes.getLength() == 0 ) {
                    nodes = XPath.selectNodes("//cell/field[@id='aspect.discovery.SimpleSearch.field.filtertype_2']", doc);
                }
                if (nodes.getLength() > 0 ) {
                    Node node = nodes.item(0);
                    NodeList options = XPath.selectNodes("option", node);
                    for ( int i =0;i<options.getLength(); i++) {
                         String value = XPath.selectText("@returnValue",options.item(i));
                        facetlist.add(new Facet(value,"facetoption."+value));
                    }
                }
                // get the available operators
                NodeList operators = XPath.selectNodes("//cell/field[@id='aspect.discovery.SimpleSearch.field.filter_relational_operator_1']", doc);
                if (operators.getLength()==0) {
                    operators = XPath.selectNodes("//cell/field[@id='aspect.discovery.SimpleSearch.field.filter_relational_operator_2']", doc);
                }
                List<String> operatorlist = new ArrayList<String>();
                if (operators.getLength()>0) {
                    Node node = operators.item(0);
                    NodeList options = XPath.selectNodes("option", node);
                    for ( int i =0;i<options.getLength(); i++) {
                        String value = XPath.selectText("@returnValue", options.item(i));
                        operatorlist.add(value);
                    }
                }
                // get the checked facets
                NodeList hiddenfields = XPath.selectNodes("//div[@n='main-form']/p/field[starts-with(@n, 'filter')]", doc);
                Logger.debug("hf size: "+hiddenfields.getLength());
                List<Filter> filters = new ArrayList<Filter>();
                for ( int i =0;i<hiddenfields.getLength(); i++) {
                    Node field = hiddenfields.item(i);
                    String label =  XPath.selectText("@n", field);
                    String id = label.substring(label.lastIndexOf("_")+1);
                    label = label.substring(0,label.lastIndexOf("_"));
                    String value = XPath.selectText("value/text()", field);
                    Filter filter = new Filter();
                    filter.id = Integer.parseInt(id);


                    if (filters.indexOf(filter)>=0) {
                        int idx = filters.indexOf(filter);
                        filter = filters.get(idx);
                    } else {
                        filters.add(filter);
                    }
                    if (label.startsWith("filtertyp")) {
                        filter.typ = value;
                    } else if (label.startsWith("filter_rel")) {
                        filter.relationalOperator = value;
                    } else {
                        filter.filter = value;
                        if (filter.filter.equals("")) {
                            filters.remove(filter);
                        }
                    }
                }

                facetbody = views.html.allFacets.render(facetlist, operatorlist, filters).body();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return facetbody;
        });
        return facets;
    }

    public static F.Promise<String> resultList(F.Promise<String> xmlPromise, Institution inst) {
        F.Promise<String> resultListPromise = xmlPromise.map(xml -> {
            Source xmlsource = new StreamSource(new StringReader(xml));
            String resultListBody = "";
            Source xslt = new StreamSource(Play.application().classloader().getResourceAsStream("xslt/discovery_results.xsl"));
            TransformerFactory transFact =  net.sf.saxon.TransformerFactoryImpl.newInstance();
            Transformer trans = transFact.newTransformer(xslt);

            trans.setParameter("lang", ctx().lang().code());
            trans.setParameter("path", "./conf/xslt/") ;
            trans.setParameter("handle", inst.handle) ;
            trans.setParameter("institut.id", inst.id) ;
            trans.setParameter("basehandle", inst.basehandle) ;
            StringWriter writer = new StringWriter();
            StreamResult s_result = new StreamResult(writer);
            trans.transform(xmlsource,s_result);
            resultListBody = writer.toString();
            return resultListBody;
        });
        return resultListPromise;
    }
}
