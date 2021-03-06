/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ORG.oclc.os.SRW;

import gov.loc.www.zing.srw.ScanRequestType;
import gov.loc.www.zing.srw.SearchRetrieveRequestType;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.z3950.zing.cql.CQLTermNode;

/**
 *
 * @author levan
 */
public class SRWOpenSearchDatabase extends SRWDatabase {
    static Log log=LogFactory.getLog(SRWOpenSearchDatabase.class);
    private String author, contact, description, restrictions, schemaID, schemaLocation, schemaName, title;

    @Override
    public String getDatabaseInfo() {
        StringBuilder sb=new StringBuilder();
        sb.append("        <databaseInfo>\n");
        if(title!=null)
            sb.append("          <title>").append(title).append("</title>\n");
        if(description!=null)
            sb.append("          <description>").append(description).append("</description>\n");
        if(author!=null)
            sb.append("          <author>").append(author).append("</author>\n");
        if(contact!=null)
            sb.append("          <contact>").append(contact).append("</contact>\n");
        if(restrictions!=null)
            sb.append("          <restrictions>").append(restrictions).append("</restrictions>\n");
        sb.append("          </databaseInfo>\n");
        return sb.toString();
    }

    @Override
    public String getExtraResponseData(QueryResult result, SearchRetrieveRequestType request) {
        return null;
    }

    @Override
    public String getIndexInfo() {
        StringBuilder sb=new StringBuilder();
        sb.append("        <indexInfo>\n")
          .append("          <set identifier=\"info:srw/oai-context-set/1/oai-v1.0\"")
          .append(" name=\"oai\"/>\n")
          .append("          <index>\n")
          .append("            <title>cql.serverChoice</title>\n")
          .append("            <map>\n")
          .append("              <name set=\"oai\">serverChoice</name>\n")
          .append("              </map>\n")
          .append("            <configInfo>\n")
          .append("              <supports type='relation'>=</supports>\n")
          .append("              </configInfo>\n")
          .append("            </index>\n")
          .append("          </indexInfo>\n");
        return sb.toString();
    }

    @Override
    public QueryResult getQueryResult(String query, SearchRetrieveRequestType request) throws InstantiationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSchemaInfo() {
        StringBuilder sb=new StringBuilder();
        sb.append("        <schemaInfo>\n")
          .append("          <schema identifier=\"").append(schemaID).append("\"\n")
          .append("              location=\"").append(schemaLocation).append("\"\n")
          .append("              sort=\"false\" retrieve=\"true\" name=\"").append(schemaName).append("\">\n")
          .append("            <title>RDF</title>\n")
          .append("            </schema>\n")
          .append("          </schemaInfo>\n");
        return sb.toString();
    }

    protected String getString(String tagName, Element element) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes();

            if (subList != null && subList.getLength() > 0) {
                return subList.item(0).getNodeValue();
            }
        }

        return null;
    }

    @Override
    public TermList getTermList(CQLTermNode term, int position, int maxTerms, ScanRequestType request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(String dbname, String srwHome, String dbHome, String dbPropertiesFileName, Properties dbProperties, HttpServletRequest request) throws Exception {
        log.debug("entering SRWOpenSearchDatabase.init, dbname="+dbname);
        super.initDB(dbname,  srwHome, dbHome, dbPropertiesFileName, dbProperties);

        String urlStr=dbProperties.getProperty("SRWOpenSearchDatabase.OpenSearchDescriptionURL");
        author=dbProperties.getProperty("SRWOpenSearchDatabase.author");
        contact=dbProperties.getProperty("SRWOpenSearchDatabase.contact");
        description=dbProperties.getProperty("SRWOpenSearchDatabase.description");
        restrictions=dbProperties.getProperty("SRWOpenSearchDatabase.restrictions");
        title=dbProperties.getProperty("SRWOpenSearchDatabase.title");
        schemaName=dbProperties.getProperty("SRWOpenSearchDatabase.schemaName");
        schemaID=dbProperties.getProperty("SRWOpenSearchDatabase.schemaID");
        schemaLocation=dbProperties.getProperty("SRWOpenSearchDatabase.schemaLocation");
        URL url=new URL(urlStr);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb=new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            sb.append(inputLine).append('\n');
        in.close();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(sb.toString())));
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr;
        if(contact==null) {
            expr = xpath.compile("/OpenSearchDescription/Contact");
            contact=(String) expr.evaluate(document, XPathConstants.STRING);
        }
        if(description==null) {
            expr = xpath.compile("/OpenSearchDescription/Description");
            description=(String) expr.evaluate(document, XPathConstants.STRING);
        }
        if(restrictions==null) {
            expr = xpath.compile("/OpenSearchDescription/SyndicationRight");
            restrictions=(String) expr.evaluate(document, XPathConstants.STRING);
            if(restrictions!=null)
                restrictions="SynticationRight="+restrictions;
            expr = xpath.compile("/OpenSearchDescription/Attribution");
            String attribution=(String) expr.evaluate(document, XPathConstants.STRING);
            if(attribution!=null)
                if(restrictions!=null)
                    restrictions=restrictions+", Attribution="+attribution;
                else
                    restrictions="Attribution="+attribution;
        }
        if(title==null) {
            expr = xpath.compile("/OpenSearchDescription/LongName");
            title=(String) expr.evaluate(document, XPathConstants.STRING);
        }
        expr = xpath.compile("/OpenSearchDescription/Url");
        NodeList nl = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        NamedNodeMap attrs;
        String template, type;
        for(int i=0; i<nl.getLength(); i++) {
            Node n=nl.item(i);
            attrs = n.getAttributes();
            n=attrs.getNamedItem("template");
            template=n.getTextContent();
            n=attrs.getNamedItem("type");
            type=n.getTextContent();
            log.info("<Url type='"+type+"' template='"+template+"'/>");
        }
        log.debug("leaving SRWOpenSearchDatabase.init");
    }

    @Override
    public boolean supportsSort() {
        return false;
    }
    
}
