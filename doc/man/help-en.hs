<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN"
         "http://java.sun.com/products/javahelp/helpset_2_0.dtd">

<helpset version="2.0">
  <!-- title -->
  <title>jose - Help</title>

  <!-- maps -->
  <maps>
     <homeID>manual.main</homeID>
     <mapref location="map.jhm"/>
  </maps>

  	<!-- TOC -->
  	<view>
    	<name>TOC</name>
    	<label>Table of Contents</label>
    	<type>javax.help.TOCView</type>
    	<data>toc.xml</data>
  	</view>

	<!-- Search 	-->
	<view>
	  <name>Search</name>
	  <label>Search</label>
	  <type>javax.help.SearchView</type>
	  <data engine="com.sun.java.help.search.DefaultSearchEngine"> JavaHelpSearch </data>
	</view>

	<!--	Index 	-->
<!--
  <view>
    <name>Index</name>
    <label>Index</label>
    <type>javax.help.IndexView</type>
    <data>index-en.xml</data>
  </view>
-->
	<!--	Glossary	-->
<!--
	  <view>
		<name>Glossary</name>
		<label>Glossary</label>
		<type>javax.help.GlossaryView</type>
		<data>gloss-en.xml</data>
	  </view>
-->
	<!--	Favorites	-->
	  <view>
		<name>Favorites</name>
		<label>Favorites</label>
		<type>javax.help.FavoritesView</type>
	  </view>


	<presentation xml:lang="en" displayviews="true" default="true" displayviewimages="false">
		<name>MainWindow</name>
		<!--	size and location are stored in user preferences	-->
		<title> jose - Help </title>
		<toolbar>
			<helpaction>javax.help.BackAction</helpaction>
			<helpaction>javax.help.ForwardAction</helpaction>
			<helpaction image="img.book">javax.help.HomeAction</helpaction>

			<helpaction>javax.help.SeparatorAction</helpaction>

			<helpaction>javax.help.ReloadAction</helpaction>
			<helpaction>javax.help.FavoritesAction</helpaction>

			<helpaction>javax.help.SeparatorAction</helpaction>

			<helpaction>javax.help.PrintAction</helpaction>
			<helpaction>javax.help.PrintSetupAction</helpaction>

		</toolbar>
	</presentation>

	<!--	implementation details	-->
	<impl>
		<helpsetregistry helpbrokerclass="javax.help.DefaultHelpBroker"/>
		<!--viewerregistry viewertype="text/html" viewerclass="com.sun.java.help.impl.CustomKit"/-->
		<viewerregistry viewertype="text/html" viewerclass="de.jose.help.CustomKit"/>
	</impl>

</helpset>