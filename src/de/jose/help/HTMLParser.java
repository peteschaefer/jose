/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Schäfer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.help;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DocumentParser;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

/**
 * a simple HTML parser
 *  that looks for <meta name="help"> tags
 *  and for <a name="topic">
 *  see TOCGenerator
 *
 * @author Peter Schäfer
 */

public class HTMLParser
        extends HTMLEditorKit.ParserCallback
{
    /** contains the document title */
    protected String        title;
    /** contains the meta tag   */
    protected String        helpReference;
    /** collects title text */
    protected StringBuffer  charData;
    /** holds the <a names> and the corrsponding text*/
    protected Vector        anchors;
    protected HashMap       anchorTitles;
    protected String        currentAnchor;

    public HTMLParser()
    {
    }

    public final void parse(File file)    throws IOException
    {
       Reader in = new BufferedReader(new FileReader(file),4096);
       parseDoc(in);
       in.close();
    }

    public final void parse(URL url)    throws IOException
    {
        InputStream sin = url.openStream();
        Reader in = new InputStreamReader(sin);
        parseDoc(in);
        in.close();
    }

    protected void parseDoc(Reader in)   throws IOException
    {
        title = null;
        charData = null;
        helpReference = null;
        anchors = new Vector();
        anchorTitles = new HashMap();
        currentAnchor = null;

        DocumentParser parser = new DocumentParser(DTD.getDTD("html"));
        parser.parse(in,this,true);
    }

    public String getTitle()
    {
        return title;
    }

    public String[] getAnchors()
    {
        if (anchors.isEmpty()) return null;
        String[] result = new String[anchors.size()];
        anchors.toArray(result);
        return result;
    }

    public String getAnchorTitle(String anchor)
    {
        return (String)anchorTitles.get(anchor);
    }

    public String getHelpReference()
    {
        return helpReference;
    }

    // --------------------------------------------------------------------------
    //  parser callbacks
    // --------------------------------------------------------------------------

    public void handleText(char[] data, int pos)
    {
        if (charData != null) {
            charData.append(data);
        }
    }

    public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos)
    {
        handleSimpleTag(tag,a,pos);
    }

    public void handleEndTag(HTML.Tag tag, int pos)
    {
        String tname = tag.toString();
        if ("title".equalsIgnoreCase(tname)) {
            title = charData.toString();
            title = title.trim();
        }
        if ("a".equalsIgnoreCase(tname))
            currentAnchor = null;

        charData = null;
    }

    public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet a, int pos)
    {
          String tname = tag.toString();
          if ("title".equalsIgnoreCase(tname) ||
              "h1".equalsIgnoreCase(tname) ||
              "h2".equalsIgnoreCase(tname) ||
              "h3".equalsIgnoreCase(tname) ||
              "h4".equalsIgnoreCase(tname))
          {
              if (charData==null)
                  charData = new StringBuffer();
              else {
                  //  find the first H element after an A
                  if (currentAnchor != null) {
                      String text = charData.toString();
                      text = text.trim();
                      anchorTitles.put(currentAnchor,text);
                      currentAnchor = null;
                  }
                  charData = null;
              }
          }

         if ("a".equalsIgnoreCase(tname)) {
            Object aname = a.getAttribute(HTML.Attribute.NAME);
            if (aname != null) {
                currentAnchor = aname.toString();
                anchors.add(currentAnchor);
            }
        }

        if ("meta".equalsIgnoreCase(tname)) {
            Object name = a.getAttribute(HTML.Attribute.NAME);
            Object content = a.getAttribute(HTML.Attribute.CONTENT);

            if (name!=null && content!=null && "help".equalsIgnoreCase(name.toString()))
                helpReference = content.toString();
        }
    }

    public void handleError(String errorMsg, int pos) {
    }

}
