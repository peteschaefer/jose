<%@ page language="java" %>
<%@ page import="de.jose.Version"%>
<%@ page import="java.sql.Connection"%>
<%@ page import="de.jose.db.JoConnection"%>
<%@ page import="de.jose.pgn.Collection"%>
<%@ page import="de.jose.db.JoPreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="de.jose.web.WebApplication"%>

<% WebApplication.open(application,response); %>

<html>
<head></head>

<body>
Hello from jose <%=Version.jose%> <br>
running on <%=Version.osName%> <%=Version.osVersion%> <%=Version.arch%>

<hr>

Working directory is: <%=WebApplication.theApplication.theWorkingDirectory%> <br>
Web directory is: <%=application.getRealPath("")%><br>
Request path is: <%=request.getRealPath("")%><br>

<hr>
Database is: <%=WebApplication.theApplication.theDatabaseId%>
<br>
on <%=WebApplication.theApplication.theDatabaseDirectory%>

<hr>

<table border=1>
	<tr>
		<th>Id</th>
		<th>Name</th>
		<th># of Games</th>
	</tr>
<%
	JoConnection conn = null;
	try {
		conn = JoConnection.get();
		JoPreparedStatement stm = conn.getPreparedStatement(
				"SELECT Id, Name, GameCount" +
				" FROM Collection WHERE GameCount > 0");
		stm.execute();
		while (stm.next())
		{
			%><tr>
			<td>
				<a href="collection.jsp?CId=<%=stm.getInt(1)%>"><%=stm.getInt(1)%></a>
			</td>
			<td><%=stm.getString(2)%></td>
			<td><%=stm.getInt(3)%></td>
			</tr><%
		}
	} catch (Throwable ex) {
			ex.printStackTrace(response.getWriter());
	} finally {
		JoConnection.release(conn);
	}

%>
</table>

</body>
</html>