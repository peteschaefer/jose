<%@ page language="java" %>
<%@ page import="de.jose.web.WebApplication"%>
<%@ page import="de.jose.web.SessionUtil"%>
<%
	WebApplication.open(application,response);
	SessionUtil su = new SessionUtil(request,session);

	int gid = su.getInt("GId",1,true);
	int row = su.getInt("row",1,true);

	su.set("GId",gid);
	su.set("row",row);
%>
<html>
<head>
	<title>jose Web Interface</title>
</head>
<frameset cols="80px, *">
<frame name="ToolBar" src="tool.jsp" frameborder=0 noresize scrolling=no>
<frame name="GameView" src="game.jsp" frameborder=0 scrolling=auto>
</frameset>

</html>