/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Schäfer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.jo3d;

import com.sun.j3d.utils.scenegraph.io.ObjectNotLoadedException;
import com.sun.j3d.utils.scenegraph.io.SceneGraphFileReader;
import com.sun.j3d.utils.scenegraph.io.SceneGraphStreamReader;
import de.jose.util.file.FileUtil;
import de.jose.util.map.MapUtil;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Shape3D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class implements are own 3D format
 * conversion methods from standard formats are provided
 *
 *
 * @author peter.schaefer
 * @author $Author: $
 * @version "$Revision:  $","$Date:  $"
 */
public class Jo3DFileReader
{
	/**	contains info about the file	*/
	private HashMap ftag;
	/**	contains the shape objects */
	private BranchGroup bg;
	/**	maps String to Shaped objects */
	private HashMap nameMap;

	public Jo3DFileReader()
		throws IOException
	{
		nameMap = new HashMap();
	}

	public Jo3DFileReader(File file)
		throws IOException
	{
		this();
		if (FileUtil.hasExtension(file.getName(),"zip"))
			readZip(file);
        else if (FileUtil.hasExtension(file.getName(),"j3df"))
            readFile(file);
        else
            readStream(file);
	}

    public Jo3DFileReader(URL url)
        throws IOException
    {
        this();
		String fileName = FileUtil.getFileName(url);
        if (FileUtil.hasExtension(fileName,"zip"))
            readZip(url.openStream());
        else if (FileUtil.hasExtension(fileName,"j3df"))
            throw new IllegalArgumentException("can't read file from URL");
        else
            readStream(url.openStream());
    }

	public Jo3DFileReader(InputStream in)
		throws IOException
	{
		this();
		readStream(in);
	}

    public void readZip(File file)
		throws IOException
	{
        readZip(new FileInputStream(file));
    }

	public void readZip(InputStream in)
		throws IOException
	{
        ZipInputStream zin = new ZipInputStream(in);
		ZipEntry zety = zin.getNextEntry();
		readStream(zin);
		zin.close();
        in.close();
	}

	public void readFile(File file)
		throws IOException
	{
        SceneGraphFileReader reader = new SceneGraphFileReader(file);
        ftag = (HashMap)reader.readUserData();

        bg = reader.readBranchGraph(0)[0];
        bg.setUserData(ftag);
        reader.close();

        fillNameMap();
	}


    public void readStream(File file)
        throws IOException
    {
        FileInputStream in = new FileInputStream(file);
        readStream(in);
        in.close();
    }

	public void readStream(InputStream in)
		throws IOException
	{
        SceneGraphStreamReader reader = new SceneGraphStreamReader(in);

		nameMap.clear();

		bg = reader.readBranchGraph(null);
		ftag = (HashMap)bg.getUserData();
        in.close();

        fillNameMap();
    }

    private void fillNameMap()
        throws IOException
    {
		//	fill name map
		Enumeration en = getShapes();
		while (en.hasMoreElements())
		{
			Shape3D shape = (Shape3D)en.nextElement();
			HashMap tag = (HashMap)shape.getUserData();

			String name = (String)tag.get("name");
			int lod = MapUtil.get(tag,"lod",0);

			String key = name+"/"+lod;
			nameMap.put(key.toLowerCase(),shape);
		}
	}

	public Enumeration getShapes()
		throws IOException
	{
		return bg.getAllChildren();
	}

	public Shape3D getShape(String name, int lod)
		throws ObjectNotLoadedException
	{
		String key = name+"/"+lod;
		Shape3D result = (Shape3D)nameMap.get(key.toLowerCase());
		if (result==null)
			return null;

		if (result.getParent() == bg)
		{
			bg.removeChild(result);		//	detach from branch
			//	SceneGraphFileReader seems to have a tendency to insert null values into the geometry list
			//	let's get rid of them
			for (int i=result.numGeometries()-1; i>=0; i--)
				if (result.getGeometry(i)==null)
					result.removeGeometry(i);
		}
		return result;
	}

	public int getLOD(Shape3D shape)
	{
		HashMap tag = (HashMap)shape.getUserData();
		if (tag==null)
			return 0;
		else
			return MapUtil.get(tag,"lod",0);
	}

	public float getLODThreshhold(Shape3D shape)
	{
		HashMap tag = (HashMap)shape.getUserData();
		if (tag==null)
			return 0f;
		else
			return MapUtil.get(tag,"lod_thresh",0f);
	}

	public int getFileVersion()	 throws IOException
	{
		return MapUtil.get(ftag,"version",0);
	}

	public String getTitle(String lang)
		throws IOException
	{
		String result = (String)ftag.get("title."+lang);
		if (result == null) result = (String)ftag.get("title");
		return result;
	}

	public String getAuthor()
		throws IOException
	{
		return (String)ftag.get("author");
	}

    public HashMap getFileParam() {
        return ftag;
    }

    public HashMap getShapeParam(Shape3D shape) {
        return (HashMap)shape.getUserData();
    }


	public static String getDisplayName(File file, String language)
        throws IOException
    {
        if (FileUtil.hasExtension(file.getName(),"j3df")) {
            SceneGraphFileReader reader = new SceneGraphFileReader(file);
            HashMap tag = (HashMap)reader.readUserData();

            String result = (String)tag.get("title."+language);
            if (result==null)
                result = (String)tag.get("title");
            return result;
        }
        else {
            //  can't access file tag in streamed files
            return null;
        }
    }

} // class Jo3DFile


/*
 * $Log: $
 *
 */

