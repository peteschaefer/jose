<%@ page language="java" pageEncoding="UTF-8" %><%@
		page import="de.jose.Application,de.jose.web.SessionUtil"%><%@
		page import="de.jose.export.ExportConfig,de.jose.export.ExportContext"%><%@
		page import="de.jose.task.GameSource"%><%@
		page import="de.jose.view.style.JoStyleContext"%><%@
		page import="de.jose.export.HtmlUtil"%><%@
		page import="de.jose.Version"%><%@
		page import="de.jose.task.io.XSLFOExport"%><%@
		page import="de.jose.task.io.PGNExport"%><%@
		page import="java.io.File"%><%@
		page import="de.jose.web.WebApplication"%><%@
		page import="de.jose.task.io.XMLExport" %><%@
        page import="de.jose.pgn.SearchRecord" %><%@
		page import="java.sql.ResultSet"%><%@
		page import="de.jose.db.JoConnection"%><%@
		page import="de.jose.db.ParamStatement"%><%@
		page import="de.jose.db.JoPreparedStatement"%><%

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
		%>
			<style>
				pre[wrap]  { white-space: pre-wrap; }  /*for Mozilla*/
				pre  { white-space: pre-wrap; word-wrap: break-word; }  /*for IE*/
			</style>
			<pre wrap><%

		XMLExport task = new XMLExport(expContext);
		task.setSilentTime(Integer.MAX_VALUE);
		task.run();     //  wait for task to complete

		%> </pre> <%
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
%>