<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="de.jose.web.WebApplication"%>
<%@ page import="de.jose.web.SessionUtil"%>
<%
	WebApplication.open(application,response);
	SessionUtil su = new SessionUtil(request,session);
%>
<html>
<head>
	<style>
		A { color:black; text-decoration: none; border-color: #e5e5e5; }
		body { background-color: #e5e5e5; }
		img { behavior: url(pngHack.htc ); }
		img.on { background-color: #c5c5c5; border: groove; background: ButtonHighlight; border-color: #e5e5e5; }
		img.off { background-color: #e5e5e5; border: ButtonShadow; border-color: #e5e5e5; }
		img.button, span.button { background-color: #e5e5e5; border: ridge; padding: 4px; }
	</style>
</head>
<body>
	<form method=post>
		<table width="100%" height="100%" celllpadding=0 celllspacing=0>
			<tr><td align="center">
				<a href="javascript:nextrow(-1)"><img src="nav/game.previous.cold.png" align="baseline"
				                                                    name="game.previous" border="0" width="32" height="36"
																	onmouseover="hover('game.previous',1)" onmouseout="hover('game.previous',2)"
				                                                    onmousedown="hover('game.previous',3)" onmouseup="hover('game.previous',4)"></a>
			</td></tr>
			<tr><td align="center">
				<a href="collection.jsp" target="_parent"><img src="nav/search.cold.png" align="baseline"
				                                               name="search" border="0" width="36" height="32"
				                                               onmouseover="hover('search',1)" onmouseout="hover('search',2)"
				                                               onmousedown="hover('search',3)" onmouseup="hover('search',4)"></a>
			</td></tr>
			<tr><td align="center">
				<a href="javascript:nextrow(+1)"><img src="nav/game.next.cold.png" align="baseline"
				                                                name="game.next" border="0" width="32" height="36"
																onmouseover="hover('game.next',1)" onmouseout="hover('game.next',2)"
				                                                onmousedown="hover('game.next',3)" onmouseup="hover('game.next',4)"></a>
			</td></tr>
			<tr><td height="100%">
			</td></tr>
			<tr><td>
				<a href="javascript:setoutput('xsl.dhtml')"><img src="html.png" name="html"
				                                                 alt="als HTML anzeigen"
				                                                 width="48" height="48"></a>
			</td></tr>
			<tr><td>
				<a href="javascript:setoutput('xsl.pdf')"><img src="pdf.png" name="pdf"
				                                               alt="als PDF anzeigen"
				                                               width="48" height="48"></a>
			</td></tr>
			<tr><td>
				<a href="javascript:setoutput('xsl.text')"><img src="txt.png" name="txt"
				                                                 alt="als Text anzeigen"
				                                                 width="48" height="48"></a>
			</td></tr>
			<tr><td>
				<a href="javascript:setoutput('export.pgn')"><img src="pgn.png" name="pgn"
				                                                  alt="als PGN-Datei anzeigen"
				                                                  width="48" height="48"></a>
			</td></tr>
		</table>
	</form>

	<script language="JavaScript">
		var current_row = <%=su.getInt("row",1,true)%>;
		var max_row = <%=su.getInt("count-results",-1,true)%>;
		var output = '<%=su.getString("out","xsl.dhtml",true)%>';

		function hover(name, state)
		{
			var image = document.images[name];

			if (!image.isEnabled) {
				image.src = "nav/"+name+".off.png";
				return;
			}

			switch (state)
			{
			case 1: //  mouse over
			case 4: //  mouse up
				image.src = "nav/"+name+".hot.png"; break;
			case 2: //  mouse out
				image.src = "nav/"+name+".cold.png"; break;
			case 3: //  mouse down
				image.src = "nav/"+name+".pressed.png"; break;
			}
		}

		function setoutput(output_type)
		{
			output = output_type;
			top.GameView.location.href = 'game.jsp?out='+output_type;

			document.images['html'].className ='off';
			document.images['pdf'].className='off';
			document.images['txt'].className='off';
			document.images['pgn'].className='off';

			if (output_type=='xsl.dhtml') document.images['html'].className='on';
			if (output_type=='xsl.pdf') document.images['pdf'].className='on';
			if (output_type=='xsl.text') document.images['txt'].className='on';
			if (output_type=='export.pgn') document.images['pgn'].className='on';
		}

		function nextrow(diff) { setrow(current_row+diff); }

		function setrow(new_row)
		{
			if (new_row < 0) new_row = 0;
			if (max_row >= 0 && new_row >= max_row) new_row = max_row-1;

			document.images['game.previous'].isEnabled = (new_row>1);
			document.images['search'].isEnabled = true;
			document.images['game.next'].isEnabled = (max_row<=0 || (new_row+1) < max_row);

			hover('game.previous',2);
			hover('search',2);
			hover('game.next',2);

			if (new_row==current_row) return;

			top.GameView.location.href = ('game.jsp?row='+new_row);

			current_row = new_row;
		}
	</script>

	<script language="JavaScript" defer>
		setrow(current_row);
		setoutput(output);
	</script>
</body>
</html>