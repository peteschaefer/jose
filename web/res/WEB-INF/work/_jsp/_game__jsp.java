/*
 * JSP generated by Resin-3.0.14 (built Tue, 05 Jul 2005 11:03:36 PDT)
 */

package _jsp;
import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import de.jose.Application;
import de.jose.web.SessionUtil;
import de.jose.export.ExportConfig;
import de.jose.export.ExportContext;
import de.jose.task.GameSource;
import de.jose.view.style.JoStyleContext;
import de.jose.export.HtmlUtil;
import de.jose.Version;
import de.jose.task.io.XSLFOExport;
import de.jose.task.io.PGNExport;
import java.io.File;
import de.jose.web.WebApplication;
import de.jose.task.io.XMLExport;
import de.jose.pgn.SearchRecord;
import java.sql.ResultSet;
import de.jose.db.JoConnection;
import de.jose.db.ParamStatement;
import de.jose.db.JoPreparedStatement;

public class _game__jsp extends com.caucho.jsp.JavaPage{
  private boolean _caucho_isDead;
  
  public void
  _jspService(javax.servlet.http.HttpServletRequest request,
              javax.servlet.http.HttpServletResponse response)
    throws java.io.IOException, javax.servlet.ServletException
  {
    javax.servlet.http.HttpSession session = request.getSession(true);
    com.caucho.server.webapp.Application _jsp_application = _caucho_getApplication();
    javax.servlet.ServletContext application = _jsp_application;
    com.caucho.jsp.PageContextImpl pageContext = com.caucho.jsp.QJspFactory.allocatePageContext(this, _jsp_application, request, response, null, session, 8192, true);
    javax.servlet.jsp.JspWriter out = pageContext.getOut();
    javax.servlet.ServletConfig config = getServletConfig();
    javax.servlet.Servlet page = this;
    response.setContentType("text/html");
    try {
      

	WebApplication.open(application,response);
	SessionUtil su = new SessionUtil(request,session);

	int gid = su.getInt("GId",1,true);

	int row = su.getInt("row",-1,false);
	if (row>=0) {
		//  next row requested
		int row0 = su.getInt("current-row0",-1,true);
		int[] id_array = (int[])su.get("current-ids",true);

		if (row0>=0 && id_array!=null && row>=row0 && row < (row0+id_array.length))
			gid = id_array[row-row0];
		else
		{
			SearchRecord search = (SearchRecord)su.get("work-search",true);
			JoConnection conn = null;
			try {
				conn = JoConnection.get();

				row0 = Math.max(row-10,0);
				id_array = new int[20];

				su.set("current-row0",row0);
				su.set("current-ids",id_array);

				ParamStatement pstm = search.makeIdStatement();
				pstm.setLimit(row0,id_array.length);

				JoPreparedStatement stm = pstm.execute(conn, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				ResultSet res = stm.getResultSet();
				for (int i=0; i < id_array.length && res.next(); i++)
					id_array[i] = res.getInt(1);
				res.close();

				gid = id_array[row-row0];
			} finally {
				JoConnection.release(conn);
			}
		}
	}

	su.set("GId",gid);

	String output_type = (String)su.get("out",true);
	if (output_type==null) output_type = "xsl.dhtml";
	su.set("out",output_type);

	ExportConfig expConfig = Application.theApplication.getExportConfig();

	ExportContext expContext = new ExportContext(); //  TODO create only one instance per session
	expContext.styles = (JoStyleContext)expContext.profile.getStyleContext();
	expContext.collateral = new File(application.getRealPath(""));

	expContext.source = GameSource.singleGame(gid);
	//  TODO we can print multiple games, too !

//	String xsl = "xsl.dhtml";       //  resp. "xsl.html", "xsl.pdf", "xsl.text"
	expContext.config = expConfig.getConfig(output_type);

	switch (expContext.getOutput())
	{
	case ExportConfig.OUTPUT_HTML:
//		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
//		response.setHeader("Content-disposition", "inline");
		expContext.target = response.getWriter();

		HtmlUtil.createCollateral(expContext);
		//  setup XML exporter with appropriate style sheet
		XMLExport xmltask = new XMLExport(expContext);
		xmltask.setSilentTime(Integer.MAX_VALUE);
		xmltask.run();     //  wait for task to complete
		break;

	case ExportConfig.OUTPUT_XML:
	case ExportConfig.OUTPUT_TEX:
	case ExportConfig.OUTPUT_TEXT:
//		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-disposition", "inline");
		expContext.target = response.getWriter();

		//  make sure the output text lines are wrapped
		
      out.write(_jsp_string0, 0, _jsp_string0.length);
      

		XMLExport task = new XMLExport(expContext);
		task.setSilentTime(Integer.MAX_VALUE);
		task.run();     //  wait for task to complete

		
      out.write(_jsp_string1, 0, _jsp_string1.length);
      
		break;
	case ExportConfig.OUTPUT_XSL_FO:
		response.setContentType("application/pdf");
		response.setHeader("Content-disposition", "inline");
		expContext.target = response.getOutputStream();

		//  setup XSL-FO exporter with appropriate style sheet
		Version.loadFop();
		XSLFOExport fotask = new XSLFOExport(expContext);
		fotask.setSilentTime(Integer.MAX_VALUE);
		fotask.run();   //  don't wait for task to complete
		break;
    case ExportConfig.OUTPUT_PGN:
	    response.setContentType("text/plain");
	    response.setCharacterEncoding("UTF-8");    //  PGN standard is ISO-8859-1. But we use UTF-8 !
	    response.setHeader("Content-disposition", "inline");
	    expContext.target = response.getWriter();

		PGNExport pgntask = new PGNExport(expContext.target);
	    pgntask.setSilentTime(Integer.MAX_VALUE);
		pgntask.setSource(expContext.source);
		pgntask.run();
		break;
	default:
		throw new IllegalArgumentException();   //  TODO
	}

    } catch (java.lang.Throwable _jsp_e) {
      pageContext.handlePageException(_jsp_e);
    } finally {
      com.caucho.jsp.QJspFactory.freePageContext(pageContext);
    }
  }

  private java.util.ArrayList _caucho_depends = new java.util.ArrayList();

  public java.util.ArrayList _caucho_getDependList()
  {
    return _caucho_depends;
  }

  public void _caucho_addDepend(com.caucho.make.PersistentDependency depend)
  {
    super._caucho_addDepend(depend);
    com.caucho.jsp.JavaPage.addDepend(_caucho_depends, depend);
  }

  public boolean _caucho_isModified()
  {
    if (_caucho_isDead)
      return true;
    if (com.caucho.util.CauchoSystem.getVersionId() != -8852640556211195023L)
      return true;
    for (int i = _caucho_depends.size() - 1; i >= 0; i--) {
      com.caucho.make.Dependency depend;
      depend = (com.caucho.make.Dependency) _caucho_depends.get(i);
      if (depend.isModified())
        return true;
    }
    return false;
  }

  public long _caucho_lastModified()
  {
    return 0;
  }

  public void destroy()
  {
      _caucho_isDead = true;
      super.destroy();
  }

  public void init(com.caucho.vfs.Path appDir)
    throws javax.servlet.ServletException
  {
    com.caucho.vfs.Path resinHome = com.caucho.util.CauchoSystem.getResinHome();
    com.caucho.vfs.MergePath mergePath = new com.caucho.vfs.MergePath();
    mergePath.addMergePath(appDir);
    mergePath.addMergePath(resinHome);
    com.caucho.loader.DynamicClassLoader loader;
    loader = (com.caucho.loader.DynamicClassLoader) getClass().getClassLoader();
    String resourcePath = loader.getResourcePathSpecificFirst();
    mergePath.addClassPath(resourcePath);
    com.caucho.vfs.Depend depend;
    depend = new com.caucho.vfs.Depend(appDir.lookup("game.jsp"), 3576347481723030977L, false);
    com.caucho.jsp.JavaPage.addDepend(_caucho_depends, depend);
  }

  private final static char []_jsp_string0;
  private final static char []_jsp_string1;
  static {
    _jsp_string0 = "\r\n			<style>\r\n				pre[wrap]  { white-space: pre-wrap; }  /*for Mozilla*/\r\n				pre  { white-space: pre-wrap; word-wrap: break-word; }  /*for IE*/\r\n			</style>\r\n			<pre wrap>".toCharArray();
    _jsp_string1 = " </pre> ".toCharArray();
  }
}
