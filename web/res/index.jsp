<%@ page import="de.jose.web.WebApplication"%>
<%@ page import="de.jose.web.SessionUtil"%>
<%@ page import="de.jose.pgn.Collection"%>
<%@ page import="de.jose.db.JoConnection"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.io.IOException"%>
<%@ page import="de.jose.db.JoPreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="de.jose.Language"%>
<%@ page language="java" %>
<%@ taglib prefix="jose" tagdir="." %>
<%!
	public static int getDepth(String path)
	{
		int depth = 0;
		int i=-1;
		for (;;)
		{
			i = path.indexOf("/",i+1);
			if (i >= 0)
				depth++;
			else
				break;
		}
		return depth;
	}
%>
<html>
<head>
	<style>
		td,th,input { font-family: Arial,Helvetica,Sans-Serif; font-size: 14px; }
		TR.odd {  background-color: #e2e2ff;}
		TR.even {  background-color: #eeeeff; }
		TD { padding:2 4 2 4; white-space: nowrap; }
		TD.center { text-align: center; }
		TD.right { text-align: right; }
		TR.header, TR.footer {  background-color: #e5e5e5; padding: 8 4 8 4; }
		A { color:black; text-decoration: none; }
	</style>

</head>
<script language="JavaScript">
	var cids = new Array();

	function adjust_image(CId)
	{
		var image = document.images['x-'+CId];
		if (!image.canExpand)
			image.src = "nav/line_root.gif";
		else if (image.isExpanded)
			image.src = "nav/minus.gif";
		else
			image.src = "nav/plus.gif";

		var folder_image = document.images['f-'+CId];
		if (image.canExpand && image.isExpanded)
			folder_image.src = "nav/folder_open.gif";
		else
			folder_image.src = "nav/folder.gif";
	}

	function add_image(CId, nest_depth, canExpand, isExpanded)
	{
		cids[cids.length] = CId;
		var image = document.images['x-'+CId];
		image.nestDepth = nest_depth;
		image.canExpand = canExpand;
		image.isExpanded = isExpanded;
		adjust_image(CId);
	}

	function toggle(CId)
	{
		var image = document.images['x-'+CId];
		if (!image.canExpand)
			return;
		image.isExpanded = ! image.isExpanded;
		adjust_image(CId);
		showChildren(CId, image.isExpanded);
	}

	function showChildren(parentId, visible)
	{
		var parentImage = document.images['x-'+parentId];
		var i = cids.indexOf(parentId)+1;

		for ( ; i < cids.length; i++)
		{
			var childId = cids[i];
			var childImage = document.images['x-'+childId];
			if (childImage.nestDepth <= parentImage.nestDepth)
				break;

			if (childImage.nestDepth == (parentImage.nestDepth+1))
			{
				show(childId, visible);
				if (childImage.canExpand && childImage.isExpanded)
					showChildren(childId, visible);
			}
		}
	}

	function show(CId, visible)
	{
		var row = document.getElementById('r-'+CId);
		if (visible)
			row.style.display = '';
		else
			row.style.display = 'none';
	}

</script>

<body>
<table border=0 cellpadding=0 cellspacing=0>
	<tr class="header">
		<th colspan="2">&nbsp;</th>
	</tr>

<%
	WebApplication.open(application,response);
	SessionUtil su = new SessionUtil(request,session);

	JoConnection connection = null;
	try {
		connection = JoConnection.get();
		JoPreparedStatement pstm = connection.getPreparedStatement(
			"select Parent.Id, Parent.Path, Parent.Name, Parent.GameCount, COUNT(Child.Id)" +
			" from Collection Parent LEFT OUTER JOIN Collection Child ON Child.PId = Parent.Id" +
			" group by Parent.Id" +
			" order by Parent.Path");

		pstm.execute();
		ResultSet res = pstm.getResultSet();

		for (int line=1; res.next(); line++)
		{
			String row_style = "even";

			int CId = res.getInt(1);
			String path = res.getString(2);
			String name = res.getString(3);
			if (Collection.isSystem(CId))
				name = Language.get(name);

			int gameCount = res.getInt(4);
			boolean hasChildren = res.getInt(5) > 0;

			int depth = getDepth(path)-2;

			%>
			<tr class="<%=row_style%>" id="r-<%=CId%>">
				<td>
					<img src="nav/pixel.gif" height="16" width="<%=16*depth%>">
					<%
						if (gameCount > 0) {
							%>
					<img align=middle name="x-<%=CId%>" height="16" width="16">

					<a href="collection.jsp?CId=<%=CId%>"><img border=0 align=middle  name="f-<%=CId%>" height="16" width="16"></a>
					<a href="collection.jsp?CId=<%=CId%>"><%=name%></a>
							<%
						}
						else {
							%>
					<img align=middle name="x-<%=CId%>" height="16" width="16"onclick="toggle(<%=CId%>)">

					<img border=0 align=middle  name="f-<%=CId%>" height="16" width="16" onclick="toggle(<%=CId%>)">
					<%=name%>
							<%
						}
					%>
				</td>
				<td class="right">
					<%
						if (gameCount==1) {
						%><a href="collection.jsp?CId=<%=CId%>">(<%=gameCount%> Partie)</a><%
						}
						else if (gameCount > 0) {
							%><a href="collection.jsp?CId=<%=CId%>">(<%=gameCount%> Partien)</a><%
						}
					%>
				</td>
			</tr>
			<script language="JavaScript">
				add_image(<%=CId%>,<%=depth%>,<%=hasChildren%>,<%=hasChildren%>);
			</script>
			<%
		}
%>

	<tr class="footer">
		<th colspan="2">&nbsp;</th>
	</tr>
</table>
</body>
</html>
<%
	} finally {
		JoConnection.release(connection);
	}
%>
