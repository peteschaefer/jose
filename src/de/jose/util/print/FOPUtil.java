package de.jose.util.print;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.fop.apps.*;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.render.awt.AWTRenderer;
import org.apache.fop.render.java2d.*;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class FOPUtil
{

    public static JoConsoleLogger gConsoleLogger = new JoConsoleLogger();
    public static FopFactory fopFactory;


    public static Fop newFop(String outputFormat, OutputStream outputStream) throws FOPException
    {
        Fop fop = fopFactory.newFop(outputFormat,outputStream);
        fop.getUserAgent().getEventBroadcaster().addEventListener(FOPUtil.gConsoleLogger);
        return fop;
    }

    public static Fop newPreviewFop() throws FOPException
    {
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        Fop fop = userAgent.newFop(MimeConstants.MIME_FOP_AWT_PREVIEW, new NullOutputStream());
        //PreviewRenderer rend = new PreviewRenderer(userAgent);
        //userAgent.setRendererOverride(rend);  //  does not work as expected :(
        userAgent.getEventBroadcaster().addEventListener(FOPUtil.gConsoleLogger);
        return fop;
    }

    public static AreaTreeHandler getAreaTreeHandler(Fop fop) throws FOPException {
        FOTreeBuilder fotb = (FOTreeBuilder) fop.getDefaultHandler();
        return (AreaTreeHandler) fotb.getEventHandler();	//	for debugging only
    }

    static {
        try {
            fopFactory = FopFactory.newInstance(new File("fop/fop.xconf"));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyFontInfo(FontInfo from, FontInfo to)
    {
        for(Map.Entry<FontTriplet, String> entry : from.getFontTriplets().entrySet())
        {
            FontTriplet triplet = entry.getKey();
            String fontName = entry.getValue();
            FontTriplet found = to.fontLookup(triplet.getName(),triplet.getStyle(), triplet.getWeight());
            if (found==null || found.getName().equals("any")) {//  was substituted
                to.addFontProperties(fontName, triplet);
                to.addMetrics(fontName,from.getMetricsFor(fontName));
            }
        }
    }

    public static class PreviewRenderer extends AWTRenderer
    {
        public PreviewRenderer(FOUserAgent userAgent) {
            super(userAgent,null,false,false);
        }
/*
        @Override
        public void setupFontInfo(FontInfo inFontInfo) {
            //Don't call super.setupFontInfo() here! Java2D needs a special font setup
            // create a temp Image to test font metrics on
            this.fontInfo = inFontInfo;
            final Java2DFontMetrics java2DFontMetrics = new Java2DFontMetrics();

            FontManager fontManager = userAgent.getFontManager();

            InternalResourceResolver resourceResolver = fontManager.getResourceResolver();
            FontCollection[] fontCollections = new FontCollection[] {
                    new Base14FontCollection(java2DFontMetrics),
                    new InstalledFontCollection(java2DFontMetrics),
                    new ConfiguredFontCollection(resourceResolver, getFontList(),
                            userAgent.isComplexScriptFeaturesEnabled())
            };
            fontManager.setup(getFontInfo(), fontCollections);
            //this.fontInfo = inFontInfo;
        }
 */
    }
}
