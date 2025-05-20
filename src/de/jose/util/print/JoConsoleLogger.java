package de.jose.util.print;

import de.jose.Application;
import de.jose.view.ConsolePanel;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventFormatter;
import org.apache.fop.events.EventListener;
import org.apache.fop.events.model.EventSeverity;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * insert an SVG image into a sax stream
 * <p>
 * public void insertSVG(Drawable draw, ContentHandler sax) throws ParserConfigurationException, SVGGraphics2DIOException, TransformerException
 * {
 * Document dom = drawSVG(draw);
 * XMLUtil.insertDOMintoSAX(dom,sax);
 * }
 */

public class JoConsoleLogger
        implements /*Logger,*/ ErrorListener, EventListener {
    protected String toString(Throwable throwable) {
/*
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.flush();
        return sw.toString();
*/
        return throwable.getLocalizedMessage();
    }

    protected void println(String style, String text) {
        try {
            if (ConsolePanel.theConsole != null)
                ConsolePanel.theConsole.println(style, text);
        } catch (IOException e) {
            Application.error(e);
        }
    }

    // ---------------------------------------------
    //      implements ErrorListener
    // ---------------------------------------------

    public void error(TransformerException exception) throws TransformerException {
        error(toString(exception));
//			exception.printStackTrace();
    }

    public void fatalError(TransformerException exception) throws TransformerException {
        fatalError(toString(exception));
//			exception.printStackTrace();
    }

    public void warning(TransformerException exception) throws TransformerException {
        warning(exception);
    }

    // ---------------------------------------------
    //      implements Logger
    // ---------------------------------------------

    public void debug(String s) {
        println("info", s);
    }

    public void info(String s) {
        println("output", s);
    }

    public void warn(String s) {
        println("error", s);
    }

    public void error(String s) {
        println("error", s);
    }

    public void fatalError(String s) {
        println("error", s);
    }

    public boolean isDebugEnabled() {
        return true;
    }

    public boolean isInfoEnabled() {
        return true;
    }

    public boolean isWarnEnabled() {
        return true;
    }

    public boolean isErrorEnabled() {
        return true;
    }

    public boolean isFatalErrorEnabled() {
        return true;
    }


    public void debug(String s, Throwable throwable) {
        debug(s);
        debug(toString(throwable));
    }

    public void info(String s, Throwable throwable) {
        info(s);
        info(toString(throwable));
    }

    public void warn(String s, Throwable throwable) {
        warn(s);
        warn(toString(throwable));
    }

    public void error(String s, Throwable throwable) {
        error(s);
        error(toString(throwable));
    }

    public void fatalError(String s, Throwable throwable) {
        fatalError(s);
        fatalError(toString(throwable));
    }

    @Override
    public void processEvent(Event event) {
        EventSeverity esev = event.getSeverity();
        String msg = EventFormatter.format(event);
        if (esev == EventSeverity.INFO)
            info(msg);
        if (esev == EventSeverity.WARN)
            warn(msg);
        if (esev == EventSeverity.ERROR)
            error(msg);
        if (esev == EventSeverity.FATAL)
            fatalError(msg);
    }

    /*public Logger getChildLogger(String s) {
        return null;
    }*/
}
