

package de.jose.sax;

import de.jose.sax.AbstractObjectReader;
import de.jose.pgn.Game;
import de.jose.pgn.PgnConstants;
import de.jose.task.*;
import de.jose.task.io.XMLExport;
import de.jose.util.ListUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.util.file.FileUtil;
import de.jose.util.print.PrintableDocument;
import de.jose.view.style.JoStyleContext;
import de.jose.view.style.JoFontConstants;
import de.jose.view.list.IDBTableModel;
import de.jose.chess.Position;
import de.jose.chess.StringMoveFormatter;
import de.jose.chess.Constants;
import de.jose.chess.EngUtil;
import de.jose.Application;
import de.jose.Language;
import de.jose.image.ImgUtil;
import de.jose.export.HtmlUtil;
import de.jose.export.ExportConfig;
import de.jose.export.ExportContext;
import de.jose.profile.UserProfile;
import de.jose.profile.FontEncoding;
import de.jose.db.JoConnection;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.text.StyleContext;
import javax.swing.text.StyleConstants;
import javax.swing.text.Style;
import java.io.IOException;
import java.io.File;
import java.util.*;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.awt.print.PageFormat;
import java.awt.*;

/**
 * XMLReader implementation for the Game class. This class is used to
 * generate SAX events from the ProjectTeam class.
 */
public class GameXMLReader extends CSSXMLReader implements GameHandler
{
    protected Game gm;
	protected GameExport export;
	protected Position pos;
	protected JoConnection dbConnection;

	public GameXMLReader(JoConnection connection, ExportContext context, GameExport exporter)
	{
		super(context);
		this.dbConnection = connection;
		this.export = exporter;
	}

	/**
	 * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
	 */
	public void parse(InputSource input) throws IOException,SAXException
	{
		if (input instanceof GameSource)
			parse((GameSource)input);
		else
			throw new SAXException("GameSource expected");
	}


	/**
	 * @throws org.xml.sax.SAXException In case of a problem during SAX event generation
	 */
	public void parse(GameSource games) throws IOException,SAXException
	{
//		if (games == null)
//			throw new NullPointerException("Parameter GameSource must not be null");
		if (handler == null)
			throw new IllegalStateException("ContentHandler not set");

		int transform = context.getOutput();
		//  TODO get cfg from XMLExporter, not directly from profile

//Start the document
		handler.setContext(this.context);
		handler.startDocument();
		handler.startElement("jose-export");

		//  context info
		PageFormat page = context.profile.getPageFormat(false);
		page = PrintableDocument.validPageFormat(page);
		//  page size
		toSAX(page,handler);

		//  figurine encodings
		saxFigurines(context.config,context.styles,handler,context.collateral);
		//  style info
		toSAX(context.styles,handler);
		//  user language
		handler.element("language", context.profile.getString("user.language"));
		//  for PDF: generate bookmarks ?
		handler.element("bookmarks", String.valueOf(context.profile.getBoolean("xsl.pdf.bookmarks",true)));

		//  custom options
		try {
			saxOptions(context.config,handler);
		} catch (Exception e) {
			Application.error(e);
		}

//Generate SAX events
		try {
			//  experimental: tournament pairing and crosstable
			if (games!=null && games.isCollection())
			{
				int CId = games.firstId();
				if (ExportConfig.getBooleanParam(context.config,"pairing-list",false))
				{
					new PairingList(CId).toSAX(handler,context,dbConnection);
				}
				if (ExportConfig.getBooleanParam(context.config,"cross-table",false))
				{
					new CrossTable(CId).toSAX(handler,context,dbConnection);
				}
			}

			if (games!=null) toSAX(games);
		} catch (Exception e) {
            e.printStackTrace();
			throw new IOException("Exception: "+e.getMessage());
		}

//End the document
		handler.endElement("jose-export");
		handler.endDocument();
	}

	protected int toSAX(GameSource source) throws Exception
	{
		if (source == null)
			throw new NullPointerException("Parameter source must not be null");
		if (handler == null)
			throw new IllegalStateException("ContentHandler not set");

		GameIterator gi = null;
		try {
			gi = GameIterator.newGameIterator(source, export.getConnection());

			while (gi.hasNext()) {
				gi.next(this);
				export.processedGames++;

				if (export.isAbortRequested()) break;
			}

		} finally {
			if (gi!=null) gi.close();
		}
		return export.processedGames;
	}

	//  implements GameHandler
	public void handleObject(Game game)
	{
		try {
			game.toSAX(handler);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}

	public void handleRow(ResultSet res) throws SQLException, SAXException
	{
		if (gm==null) {
			gm = new Game();
			gm.setPosition(pos);
			gm.newTagNode(null,PgnConstants.TAG_FEN,null);
		}

		gm.toSAX(res, handler);
	}


	/**
	 * Generates SAX events for a GameSource object.
	 * @param source GameSource object to use
	 * @throws org.xml.sax.SAXException In case of a problem during SAX event generation
	 */
	protected int toSAX2(GameSource source) throws IOException,SAXException,SQLException
	{
		if (source == null)
			throw new NullPointerException("Parameter source must not be null");
		if (handler == null)
			throw new IllegalStateException("ContentHandler not set");

		int total = source.size();  //  might be < 0, unknown
		if (total < 0) export.setProgress(-1);

		int current = 0;

		//  TODO handle all types of GameSources
		//  TODO report progress, check for cancel

		if (source.isResultSet())
			return resultSetToSAX((IDBTableModel)source.data,current,total);

		if (source.isSingleCollection())
			return collectionToSAX(source.getId(),current,total);

		if (source.isCollectionArray())
			return collectionToSAX(source.getId(),current,total);

		if (source.isCollectionSelection()) {
			Iterator i = ListUtil.iterator(source.data);
			while (i.hasNext()) {
				int CId = ((Number)i.next()).intValue();
				current = collectionToSAX(CId,current,total);
			}
			return current;
		}

		if (source.data instanceof Game) {
			//   1 single game object
			((Game)source.data).toSAX(handler);
			return 1;
		}

		if (ListUtil.isIteratable(source.data))    {
			//  list of game objects or game ids
			Iterator i = ListUtil.iterator(source.data);
			while (i.hasNext()) {
				Object next = i.next();
				if (next instanceof Game) {
					((Game)next).toSAX(handler);
					current++;
				}
				else if (next instanceof Number) {
					if (gm==null) {
						gm = new Game();
						gm.setPosition(pos);
						gm.newTagNode(null,PgnConstants.TAG_FEN,null);
					}
					int GId = ((Number)next).intValue();
					gm.toSAX(dbConnection, GId, handler);
					current++;
				}

				if (export.isAbortRequested()) break;
				if (total >= 0 && (current%100)==0) export.setProgress((double)current/total);

			}
			return current;
		}

		throw new UnsupportedOperationException("unrecognized game source flavor");
	}

	protected int collectionToSAX(int CId, int current, int total)
	{
		//  TODO traverse collection
		//  read blockwise to reduce DB contention
		throw new UnsupportedOperationException();
		//return current;
	}

	protected int resultSetToSAX(IDBTableModel resultSet, int current, int total)
	{
		//  TODO traverse collection
		//  TODO read blockwise to reduce DB contention

		for (int row = 0; row < resultSet.getRowCount(); row++) {
			int GId = resultSet.getDBId(row);
			if (GId < 0) break; //  right ?

			gameToSAX(GId);
			current++;

			if (export.isAbortRequested()) break;
			if (total >= 0 && (current%100)==0) export.setProgress((double)current/total);
		}
		return current;
	}

	protected void gameToSAX(int GId) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Generates SAX events for a ProjectMember object.
	 * @param game Game object to use
	 * @throws org.xml.sax.SAXException In case of a problem during SAX event generation
	 * /
	protected Game createGame(int GId) throws IOException,SAXException
	{
		if (gm==null) {
			if (styles==null) styles = Application.theUserProfile.getStyleContext();
			if (pos==null) pos = new Position();
			gm = new Game(styles,pos);
		}
		try {
			gm.read(GId);
		} catch (Exception ex) {
			throw new IOException(ex.getLocalizedMessage());
		}
		return gm;
	}
	*/


	public void saxFigurines(Element cfg, JoStyleContext styles, JoContentHandler handler,File webDir) throws SAXException
	{

		//  figurine info
		handler.startElement("figurines");

		for (int i=0; ; i++) {
			Style figStyle = styles.getStyle("body.figurine."+i);
			if (figStyle==null) break;
			//  else:
			saxFigurine(webDir,JoFontConstants.getFontFamily(figStyle),
			        JoFontConstants.getFontSize(figStyle),i,handler); //  IMG figurines
		}

		//  image files for inline diagrams (pieces + border)
		Style inlineStyle = styles.getStyle("body.inline");
		handler.startElement("inline");
			handler.element("font", JoFontConstants.getFontFamily(inlineStyle));
			handler.element("pt-size", JoFontConstants.getFontSize(inlineStyle));
			handler.element("px-size", styles.getPixelSize(inlineStyle));
		handler.endElement("inline");

		//  images files for large dias (as used by DHTML)
		int diaSize = context.profile.getInt("xsl.dhtml.diasize",20);
		handler.startElement("dia");
			handler.element("font", JoFontConstants.getFontFamily(inlineStyle));
			handler.element("pt-size", diaSize);
			handler.element("px-size", styles.getPixelSize(diaSize));
		handler.endElement("dia");

		//  plain text figurines for current language
		handler.startElement("text");

			String langCode = Language.theLanguage.langCode;
			handler.element("lang", langCode);
			String[] pieceChars = StringMoveFormatter.parsePieceChars(Language.get("fig."+langCode, null));

			for (int p = Constants.PAWN; p <= Constants.KING; p++)
			{
				String piece = String.valueOf(EngUtil.lowerPieceCharacter(p));
				handler.keyValue("string", piece, pieceChars[p]);
			}
		handler.endElement("text");

		handler.endElement("figurines");
	}

	public void saxFigurine(File baseDir, String fontFamily, int fontSize, int nestLevel, JoContentHandler handler) throws SAXException
	{
		/**
		 * image figurines: print family name & font size (= file paths)
		 */
		int pxsize = context.styles.getPixelSize(fontSize);
		handler.startElement("fig");
			handler.element("level",nestLevel);
			handler.element("font",fontFamily);
			handler.element("pt-size",fontSize);
			handler.element("px-size", pxsize);

			for (int p = Constants.PAWN; p <= Constants.KING; p++)
			{
				String piece = String.valueOf(EngUtil.lowerPieceCharacter(p));
				File file = new File(baseDir, fontFamily+"/"+pxsize+"/"+piece+".png");
				Dimension imgSize = ImgUtil.getImageSize(file);
				if (imgSize==null) imgSize = new Dimension(pxsize,pxsize);

				handler.startElement("string");
					handler.element("key",piece);
					handler.element("value",FontEncoding.getFigurine(fontFamily,p));
					//  actual image size ?
					handler.element("width", imgSize.width);
					handler.element("height", imgSize.height);
				handler.endElement("string");
			}

		handler.endElement("fig");
	}

	public void saxOptions(Element cfg, JoContentHandler handler) throws SAXException
	{
		handler.startElement("options");

		switch (ExportConfig.getOutput(cfg)) {
		case ExportConfig.OUTPUT_HTML:
		case ExportConfig.OUTPUT_XML:
			//  standard HTML options
			//  figurine format ("tt" for TrueType, "img" for Image)
			String figs = context.profile.getString("xsl.html.figs","tt");
			//  standalone css ?
			boolean cssStandalone = context.profile.getBoolean("xsl.css.standalone");
			//  collateral dir (contains images & css)
			String collpath = (context.collateral!=null && context.target instanceof File) ?
			        FileUtil.getRelativePath(((File)context.target).getParentFile(),context.collateral,"/"):"";

			handler.keyValue("option", "xsl.html.figs",figs);
			handler.keyValue("option", "xsl.css.standalone", cssStandalone ? "true":"false");
			handler.keyValue("option", "xsl.html.img.dir", collpath);
			//  TODO use URL ?
			break;
		}

		//  custom options defined in style sheet:
		NodeList options = cfg.getElementsByTagName("jose:option");
		for (int i=0; i < options.getLength(); i++)
		{
			Element option = (Element)options.item(i);
			String key = XMLUtil.getChildValue(option,"jose:key");
			Object value = context.profile.get(key);
			if (value==null)
				value = XMLUtil.getChildValue(option,"jose:default");

			if (value != null)
				handler.keyValue("option",key,value.toString());
		}
		handler.endElement("options");
	}

	public static void toSAX(PageFormat page, JoContentHandler handler) throws SAXException
	{
		handler.startElement("page");
			handler.element("width", String.valueOf(page.getWidth()));
			handler.element("height", String.valueOf(page.getHeight()));
			handler.element("margin-left", String.valueOf(page.getImageableX()));
			handler.element("margin-top", String.valueOf(page.getImageableY()));
			handler.element("margin-bottom", String.valueOf(page.getHeight()-page.getImageableY()-page.getImageableHeight()));
			handler.element("margin-right", String.valueOf(page.getWidth()-page.getImageableX()-page.getImageableWidth()));
			handler.element("printable-width", String.valueOf(page.getImageableWidth()));
			handler.element("printable-height", String.valueOf(page.getImageableHeight()));
		handler.endElement("page");
	}

}