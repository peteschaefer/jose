<%@ page language="java" %>
<%@ page import="de.jose.Application"%>
<%@ page import="de.jose.pgn.Collection"%>
<%@ page import="de.jose.web.SessionUtil"%>
<%@ page import="de.jose.db.JoConnection"%>
<%@ page import="de.jose.db.JoPreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="de.jose.pgn.PgnUtil"%>
<%@ page import="de.jose.pgn.PgnDate"%>
<%@ page import="java.util.Date"%>
<%@ page import="de.jose.db.JoStatement"%>
<%@ page import="de.jose.web.WebApplication"%>
<%@ page import="de.jose.pgn.SearchRecord"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="de.jose.db.ParamStatement"%>
<%@ page import="de.jose.view.ListPanel"%>
<%@ page import="de.jose.util.map.IntHashSet"%>
<%@ page import="de.jose.Language"%>
<%@ page import="de.jose.Util"%>
<%@ page import="de.jose.util.StringUtil"%>

<%!
	protected boolean setStringField(SessionUtil su, SearchRecord search, String fieldName)
	{
		String oldValue = (String)search.getField(fieldName);
		if (oldValue==null)
			oldValue="";
		else
			oldValue = oldValue.trim();
		String newValue = su.getString(fieldName,oldValue,true);
		if (newValue==null)
			newValue="";
		else
			newValue = newValue.trim();

		search.setField(fieldName,newValue);

		return ! oldValue.equals(newValue);
	}
%>

<html>
<head>
	<title>jose Web Interface</title>
	<style>
		td,th,input { font-family: Arial,Helvetica,Sans-Serif; font-size: 14px; }
		TR.odd {  background-color: #e2e2ff;}
		TR.even {  background-color: #eeeeff; }
		TD { padding:2 4 2 4; white-space: nowrap; }
		TD.center { text-align: center; }
		TD.right { text-align: right; }
		TR.header, TR.footer {  background-color: #e5e5e5; padding: 8 4 8 4; }
		A { color:black; text-decoration: none; }
		SELECT.footer { background-color: #e5e5e5 }

		input.sbox-search {
			color: black; width: 120px; height: 18px; border: 0;
			background: white url(border-background.gif) repeat-x left top;
			padding-top: 1px; padding-left: 3px; padding-right: 3px; padding-bottom: 0px;
		}
		input.sbox-page {
			color: black; height: 17px; border: 0;
			background: white url(border-background.gif) repeat-x left top;
			padding: 2px; text-align: center; width: 24px;
		}

		select.sbox-rows {
			color: black; height: 17px; border: none;
			background: white url(border-background.gif) repeat-x left top;
			padding: 2px;
		}
	</style>
</head>
<%
	WebApplication.open(application,response);
	SessionUtil su = new SessionUtil(request,session);

	int count_results = su.getInt("count-results",-1,true);

	//  get search record
	SearchRecord search = (SearchRecord)su.get("search-record",true);
	if (search==null) {
		search = new SearchRecord();
		search.clear();
		search.collections = new IntHashSet();
		search.collections.add(1001);
		search.adapter = JoConnection.getAdapter();
		search.sortOrder = ListPanel.COL_IDX+1;   //  negative values sort descending
		su.set("search-record",search);
		count_results = -1;
	}

	//  get URL parameters: CId, Name, page, pagesize, sort
	int cid = su.getInt("CId",0,false);
	//  try name ?
	String name = su.getString("Name",false);
	if (cid <= 0 && name!=null)
		cid = Collection.getId(name,false);

	if (cid>0) {
		if (cid != su.getInt("CId",1001,true))  search.clear();
		search.collections.clear();
		search.collections.add(cid);
		count_results = -1;
	}
	else
		cid = su.getInt("CId",1001,true);
	su.set("CId",cid);

	search.sortOrder = su.getInt("sort",search.sortOrder,false);

	int pagenum = su.getInt("pagenum",1,true);
	int pagesize = su.getInt("pagesize",20,true);
	if (pagesize < 1) pagesize=1;
	if (pagesize > 500) pagesize=500;

	//  edit search record
	boolean big_search = su.getBoolean("big-search",false,true);
	if (su.wasSubmitted("more-search")) big_search=!big_search;
	su.set("big-search",big_search);

	boolean dirty = setStringField(su,search,"firstPlayerName");

	if (dirty) {
		//  if search was modified
		pagenum = 1;
		count_results = -1;
	}

	ArrayList input_errors = new ArrayList();
	SearchRecord work_search = (SearchRecord)search.clone();
	work_search.finish(input_errors);	//	check plausability
	su.set("work-search",work_search);

	//  estimate results
	if (count_results < 0) count_results = work_search.estimateResults();

	if (count_results >= 0) {
		int max_page = (count_results+pagesize-1)/pagesize;
		if (pagenum > max_page) pagenum = max_page;
	}
	if (pagenum<1) pagenum=1;

	su.set("pagenum",pagenum);
	su.set("pagesize",pagesize);

	//  make the query
	JoConnection conn = null;
	JoPreparedStatement stm = null;
	ResultSet res = null;

	try {
		conn = JoConnection.get();

		int row0 = (pagenum-1)*pagesize;
		int[] id_array = new int[pagesize+1];

		su.set("current-row0",row0);
		su.set("current-ids",id_array);

		ParamStatement pstm = work_search.makeDataStatement(row0,id_array.length);

		stm = pstm.execute(conn,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		res = stm.getResultSet();
%>

<body>

<script language="JavaScript">
	function submit(param,value)
	{
		var form = document.forms[1];
		form.elements[param].value = value;
		form.submit();
	}
	function frame(gid,row)
	{
		var form = document.forms[0];
		form.elements['GId'].value = gid;
		form.elements['row'].value = row;
		form.submit();
	}
</script>

<form method="post" action="frame.jsp">
	<input type="hidden" name="GId">
	<input type="hidden" name="row">
</form>

<form method="post">
<input type="hidden" name="sort" value="<%=search.sortOrder%>">

<table cellspacing="0">
	<tr class="header">
		<th>
			<a href="javascript:submit('sort',<%=search.toggleSortOrder(ListPanel.COL_IDX+1)%>)"><%=Language.get("column.game.index")%>&nbsp;<%
				if (search.sortOrder==+(ListPanel.COL_IDX+1)) {
					%><img src="down8.gif" title="aufsteigend sortiert" width=8 height=8 border=0><%
				}
				else if (search.sortOrder==-(ListPanel.COL_IDX+1)) {
					%><img src="up8.gif" title="absteigend sortiert" width=8 height=8 border=0><%
				}
		%></a>
		</th>
		<th>
			<a href="javascript:submit('sort',<%=search.toggleSortOrder(ListPanel.COL_WNAME+1)%>)"><%=Language.get("column.game.white.name")%>&nbsp;<%
				if (search.sortOrder==+(ListPanel.COL_WNAME+1)) {
					%><img src="down8.gif" title="aufsteigend sortiert" width=8 height=8 border=0><%
				}
				else if (search.sortOrder==-(ListPanel.COL_WNAME+1)) {
					%><img src="up8.gif" title="absteigend sortiert" width=8 height=8 border=0><%
				}
		%></a>
		</th>
		<th>
			<a href="javascript:submit('sort',<%=search.toggleSortOrder(ListPanel.COL_WELO+1)%>)">ELO&nbsp;<%
				if (search.sortOrder==+(ListPanel.COL_WELO+1)) {
					%><img src="down8.gif" title="aufsteigend sortiert" width=8 height=8 border=0><%
				}
				else if (search.sortOrder==-(ListPanel.COL_WELO+1)) {
					%><img src="up8.gif" title="absteigend sortiert" width=8 height=8 border=0><%
				}
		%></a>
		</th>
		<th>
			<a href="javascript:submit('sort',<%=search.toggleSortOrder(ListPanel.COL_BNAME+1)%>)"><%=Language.get("column.game.black.name")%>&nbsp;<%
				if (search.sortOrder==+(ListPanel.COL_BNAME+1)) {
					%><img src="down8.gif" title="aufsteigend sortiert" width=8 height=8 border=0><%
				}
				else if (search.sortOrder==-(ListPanel.COL_BNAME+1)) {
					%><img src="up8.gif" title="absteigend sortiert" width=8 height=8 border=0><%
				}
		%></a>
		</th>
		<th><a href="javascript:submit('sort',<%=search.toggleSortOrder(ListPanel.COL_BELO+1)%>)">ELO&nbsp;<%
				if (search.sortOrder==+(ListPanel.COL_BELO+1)) {
					%><img src="down8.gif" title="aufsteigend sortiert" width=8 height=8 border=0><%
				}
				else if (search.sortOrder==-(ListPanel.COL_BELO+1)) {
					%><img src="up8.gif" title="absteigend sortiert" width=8 height=8 border=0><%
				}
		%></a>
		</th>
		<th><a href="javascript:submit('sort',<%=search.toggleSortOrder(ListPanel.COL_RESULT+1)%>)"><%=Language.get("column.game.result")%>&nbsp;<%
				if (search.sortOrder==+(ListPanel.COL_RESULT+1)) {
					%><img src="down8.gif" title="aufsteigend sortiert" width=8 height=8 border=0><%
				}
				else if (search.sortOrder==-(ListPanel.COL_RESULT+1)) {
					%><img src="up8.gif" title="absteigend sortiert" width=8 height=8 border=0><%
				}
		%></a>
		</th>
		<th><a href="javascript:submit('sort',<%=search.toggleSortOrder(ListPanel.COL_EVENT+1)%>)"><%=Language.get("column.game.event")%>&nbsp;<%
				if (search.sortOrder==+(ListPanel.COL_EVENT+1)) {
					%><img src="down8.gif" title="aufsteigend sortiert" width=8 height=8 border=0><%
				}
				else if (search.sortOrder==-(ListPanel.COL_EVENT+1)) {
					%><img src="up8.gif" title="absteigend sortiert" width=8 height=8 border=0><%
				}
		%></a>
		</th>
		<th><a href="javascript:submit('sort',<%=search.toggleSortOrder(ListPanel.COL_SITE+1)%>)"><%=Language.get("column.game.site")%>&nbsp;<%
				if (search.sortOrder==+(ListPanel.COL_SITE+1)) {
					%><img src="down8.gif" title="aufsteigend sortiert" width=8 height=8 border=0><%
				}
				else if (search.sortOrder==-(ListPanel.COL_SITE+1)) {
					%><img src="up8.gif" title="absteigend sortiert" width=8 height=8 border=0><%
				}
		%></a>
		</th>
		<th><a href="javascript:submit('sort',<%=search.toggleSortOrder(ListPanel.COL_ROUND+1)%>)"><%=Language.get("column.game.round")%>&nbsp;<%
				if (search.sortOrder==+(ListPanel.COL_ROUND+1)) {
					%><img src="down8.gif" title="aufsteigend sortiert" width=8 height=8 border=0><%
				}
				else if (search.sortOrder==-(ListPanel.COL_ROUND+1)) {
					%><img src="up8.gif" title="absteigend sortiert" width=8 height=8 border=0><%
				}
		%></a>
		</th>
		<th><a href="javascript:submit('sort',<%=search.toggleSortOrder(ListPanel.COL_DATE+1)%>)"><%=Language.get("column.game.date")%>&nbsp;<%
				if (search.sortOrder==+(ListPanel.COL_DATE+1)) {
					%><img src="down8.gif" title="aufsteigend sortiert" width=8 height=8 border=0><%
				}
				else if (search.sortOrder==-(ListPanel.COL_DATE+1)) {
					%><img src="up8.gif" title="absteigend sortiert" width=8 height=8 border=0><%
				}
		%></a>
		</th>
		<th><a href="javascript:submit('sort',<%=search.toggleSortOrder(ListPanel.COL_OPENING+1)%>)"><%=Language.get("column.game.opening")%>&nbsp;<%
				if (search.sortOrder==+(ListPanel.COL_OPENING+1)) {
					%><img src="down8.gif" title="aufsteigend sortiert" width=8 height=8 border=0><%
				}
				else if (search.sortOrder==-(ListPanel.COL_OPENING+1)) {
					%><img src="up8.gif" title="absteigend sortiert" width=8 height=8 border=0><%
				}
		%></a>
	</th>
	</tr>

<%
		int row = (pagenum-1)*pagesize;
		int max_row = pagenum*pagesize;

		for ( ; (row < max_row) && res.next(); row++)
		{
			id_array[row-row0] = res.getInt(1);

			String row_style = ((row %2)==1) ? "odd":"even";
			int gid = res.getInt(1/*Game.Id*/);

			String white_title = res.getString(5/*WhiteTitle*/);
			String white_name = res.getString(4/*White.Name*/);
			if (white_title!=null) white_name = white_title+" "+white_name;

			String white_elo = res.getString(6/*WhiteELO*/);
			if (white_elo==null || white_elo.equals("0")) white_elo = "";

			String black_title = res.getString(8/*BlackTitle*/);
			String black_name = res.getString(7/*Black.Name*/);
			if (black_title!=null) black_name = black_title+" "+black_name;

			String black_elo = res.getString(9/*BlackELO*/);
			if (black_elo==null || black_elo.equals("0")) black_elo = "";

			String event_name = res.getString(11/*Event.Name*/);
			if (event_name==null) event_name = "";

			String site_name = res.getString(12/*Site.Name*/);
			if (site_name==null) site_name = "";

			String round = res.getString(13/*Round*/);
			String board = res.getString(14/*Board*/);
			if (round==null && board==null)
				round = "";
			else if (board==null)
				 ;
			else if (round==null)
				round = board;
			else
				round = round+" "+board;

			String eco = res.getString(16/*Game.ECO*/);
			String opening = res.getString(17/*Opening.Name*/);
			if (eco==null && opening==null)
				opening = "";
			else if (eco==null)
				;
			else if (opening==null)
				opening = eco;
			else
				opening = opening+" "+eco;

			Date date = res.getDate(15/*GameDate*/);
			String date_string = "";
			if (date!=null) {
				int dateFlags = res.getInt(18/*DateFlags*/);
				PgnDate pgnDate = new PgnDate(date,(short)(dateFlags & 0x00ff));
				date_string = pgnDate.toLocalDateString(true);
			}
%>

	<tr class="<%=row_style%>">
		<td class="right">
			<a href="javascript:frame(<%=gid%>,<%=row%>)" title="<%=black_name%>-<%=white_name%>">
			<%=res.getString(3/*Idx*/)%>
			</a>
		</td>
		<td>
			<a href="javascript:frame(<%=gid%>,<%=row%>)" title="<%=black_name%>-<%=white_name%>">
			<%=white_name%>
			</a>
		</td>
		<td class="right">
			<a href="javascript:frame(<%=gid%>,<%=row%>)" title="<%=black_name%>-<%=white_name%>">
			<%=white_elo%>
			</a>
		</td>
		<td>
			<a href="javascript:frame(<%=gid%>,<%=row%>)" title="<%=black_name%>-<%=white_name%>">
			<%=black_name%>
			</a>
		</td>
		<td class="right">
			<a href="javascript:frame(<%=gid%>,<%=row%>)" title="<%=black_name%>-<%=white_name%>">
			<%=black_elo%>
			</a>
		</td>
		<td class="center">
			<a href="javascript:frame(<%=gid%>,<%=row%>)" title="<%=black_name%>-<%=white_name%>">
			<%=PgnUtil.resultString(res.getInt(10/*Result*/))%>
			</a>
		</td>
		<td>
			<a href="javascript:frame(<%=gid%>,<%=row%>)" title="<%=black_name%>-<%=white_name%>">
			<%=event_name%>
			</a>
		</td>
		<td>
			<a href="javascript:frame(<%=gid%>,<%=row%>)" title="<%=black_name%>-<%=white_name%>">
			<%=site_name%>
			</a>
		</td>
		<td class="right">
			<a href="javascript:frame(<%=gid%>,<%=row%>)" title="<%=black_name%>-<%=white_name%>">
			<%=round%>
			</a>
		</td>
		<td class="right">
			<a href="javascript:frame(<%=gid%>,<%=row%>)" title="<%=black_name%>-<%=white_name%>">
			<%=date_string%>
			</a>
		</td>
		<td>
			<a href="javascript:frame(<%=gid%>,<%=row%>)" title="<%=black_name%>-<%=white_name%>">
			<%=opening%>
			</a>
		</td>
	</tr>
<%
	}

	res.close();

	if (row < max_row) {
		//  end of result set has been reached; count!
		count_results = row;
		//  print some empty lines
		for (int dummy_row=row; dummy_row < max_row; dummy_row++)
		{
			String row_style = ((dummy_row %2)==1) ? "odd":"even";
			%><tr class="<%=row_style%>"><td colspan=11>&nbsp;</td></tr><%
		}
	}

	su.set("count-results", count_results);

%>
	<tr class="footer">
		<td colspan=11>
			<table cellspacing=0 cellpadding=0><tr><td width="180px">
			<!--short search-->
			<input type=image name=ignore3 width=0 height=0>
			<img type=image name="more-search" src="border-search.gif" alt="" class="srchimgs" align="absbottom" border="0" height="18" width="16"
><% if (!big_search) {
			%><input name="firstPlayerName" class="sbox-search" tabindex=1
			     value="<%=search.firstPlayerName%>"><%
 } %><input type=image src="border-right.gif" alt="" name="ignore1" class="srchimgs"
			     align="absbottom" border="0" height="18" width="9">
		</td>
		<td>Seite&nbsp;
<%
	//  page navigation
		int max_page;
		if (count_results>=0) {
			//  number of pages is known
			max_page = (count_results+pagesize-1)/pagesize;
		}
		else {
			//  number of pages is not (yet) known
			max_page = -1;
		}

		if (pagenum <= 6)
		{
				for (int p=1; p < pagenum; p++) {
					%><a href="javascript:submit('pagenum',<%=p%>)"><%=p%></a>&nbsp;<%
				}
		}
		else
		{
			for (int p=1; p <= 3; p++) {
					%><a href="javascript:submit('pagenum',<%=p%>)"><%=p%></a>&nbsp;<%
			}
			%> ... <%
			for (int p=pagenum-2; p < pagenum; p++) {
					%><a href="javascript:submit('pagenum',<%=p%>)"><%=p%></a>&nbsp;<%
			}
		}

		if (pagenum > 1) {
			%><a href="javascript:submit('pagenum',<%=(pagenum-1)%>)">&nbsp;&nbsp;<img src="left8.gif"
		                                               border=0 title="vorherige Seite" align="baseline">&nbsp;&nbsp;</a><%
		}

		%><img src="border-left.gif" alt="" class="srchimgs" align="absbottom" border="0" height="18" width="9"
			><input class="sbox-page" name="pagenum" tabindex=2 value="<%=pagenum%>"
			><input type=image src="border-right.gif" alt="" class="srchimgs" name="ignore2"
				align="absbottom" border="0" height="18" width="9">&nbsp;<%

		if (max_page < 1 || pagenum < max_page) {
			%><a href="javascript:submit('pagenum',<%=(pagenum+1)%>)">&nbsp;&nbsp;<img src="right8.gif" border=0 title="nächste Seite"
		                                               align="baseline">&nbsp;&nbsp;</a>&nbsp;<%
		}

		if (max_page >= 1 && (pagenum+5) >= max_page)
		{
			for (int p=pagenum+1; p <= max_page; p++) {
					%><a href="javascript:submit('pagenum',<%=p%>)"><%=p%></a>&nbsp;<%
			}
		}
		else if (max_page >= 1)
		{
			for (int p=pagenum+1; p <= (pagenum+2); p++) {
					%><a href="javascript:submit('pagenum',<%=p%>)"><%=p%></a>&nbsp;<%
			}
			%> ... <a href="javascript:submit('pagenum',<%=max_page%>)"><%=max_page%></a>&nbsp;<%
		}
		else
		{
			//  max_page unknown, print just one more
			%><a href="javascript:submit('pagenum',<%=(pagenum+1)%>)"><%=(pagenum+1)%></a>&nbsp;...&nbsp;<%
		}
%>
		</td><td style="width: 1cm">&nbsp;</td><td>
			<img src="border-left.gif" alt="" class="srchimgs" align="absbottom" border="0" height="18" width="9"
			><input tabindex=3 name="pagesize" class="sbox-page" value="<%=pagesize%>"
			><input type=image src="border-right.gif" alt="" class="srchimgs" align="absbottom" border="0" height="18" width="9">
				Zeilen
			</td></tr></table>
		</td>
	</tr>
<%      if (big_search) { %>
	<tr class=footer><td colspan=11>
		<table>
			<tr><td>Player1</td><td><input name="firstPlayerName" value="<%=search.firstPlayerName%>"></td></tr>
		</table>
	</td></tr>
<%
		}
	} catch (Throwable ex) {
		ex.printStackTrace(response.getWriter());
	} finally {
//		if (stm!=null) stm.close();
		JoConnection.release(conn);
	}
%>
</table>
</form>

</body>
</html>