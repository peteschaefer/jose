package de.jose.export;

import org.xml.sax.SAXException;
import org.xml.sax.HandlerBase;
import org.xml.sax.AttributeList;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

import de.jose.util.xml.XMLUtil;

/**
 * Parse the header of an XSL file
 * look for an <jose:export> element.
 * Abort as soon as we hit an <xsl:template>
 */
public class XSLHeaderParser extends HandlerBase
{
    protected static SAXParserFactory saxFactory = SAXParserFactory.newInstance();

    public static Document getExportInfo(File file)
    {
        XSLHeaderParser handler = new XSLHeaderParser();
        try {

            SAXParser saxParser = saxFactory.newSAXParser();
            saxParser.parse(file,handler);

        } catch (SAXException saxex) {
            if (saxex.getMessage().equals("bail-out"))
                /* thrown on intention */ ;
            else
                return null;    //  this is a real error
        } catch (Exception e) {
            return null;
        }

        return handler.doc;
    }



    protected Document doc = null;

    protected Element root = null;
    protected Element current = null;

    public void startElement(String name, AttributeList attributes) throws SAXException
    {
        if  (name.equalsIgnoreCase("xsl:template")) //  we are finished
            throw new SAXException("bail-out");

        if (name.equalsIgnoreCase("jose:export"))
        {
            //  this is what we are looking for !
            try {

                if (doc==null) {
                    doc = XMLUtil.newDocument();
                    doc.appendChild(doc.createElement("export-config"));
                }

            } catch (ParserConfigurationException e) {
                throw new SAXException(e.getMessage());
            }

            root = current = XMLUtil.createElement(doc,name,attributes);

            doc.getDocumentElement().appendChild(current);
        }
        else if (current != null)
        {
            //   start a new child
            Element child = XMLUtil.createElement(doc,name,attributes);

            current.appendChild(child);
            current = child;
        }
        //  else: ignore
    }

    public void endElement(String name) throws SAXException
    {
        if (current==root)
        {
            root = current = null;
        }
        else if (current!=null)
        {
            current = (Element)current.getParentNode();
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException
    {
        if (current!=null) {
            current.appendChild(doc.createTextNode(new String(ch,start,length)));
        }
    }
}
