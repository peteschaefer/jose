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

package de.jose.devtools;

import com.sun.j3d.loaders.Scene;
import de.jose.Application;
import de.jose.util.xml.XMLUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.util.file.FileUtil;
import ncsa.j3d.loaders.ModelLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * @author peter.schaefer
 * @author $Author: $
 * @version "$Revision:  $","$Date:  $"
 */
public class Jo3DAssembler
{
	//	current file version
	public static final int fileVersion = 100;

	//	control document (XML)
	private Document control;
	//	base directory
	private File baseDir;

	private Jo3DFileWriter writer;
	//	global transform
	private Transform3D globaltf;
	//	file-wise transform
	private Transform3D filetf;
	//	shape-wise transform
	private Transform3D shapetf;
	//	global LOD threshholds
	private float[] globallod;
	//	file-wise LOD threshholds
	private float[] filelod;
    //  global crease angle
    private double globalcrease;
    //  file-wise crease angle
    private double filecrease;

	//	diagostic output
	private PrintStream stdout;

	public Jo3DAssembler(File controlFile)
		throws Exception
	{
		this (XMLUtil.parse(controlFile), controlFile.getParentFile());
	}

	public Jo3DAssembler(Document controlDoc, File dir) throws IOException
	{
		control = controlDoc;
		writer = new Jo3DFileWriter();

		if (dir==null) dir = Application.getWorkingDirectory();
		baseDir = dir;
		globaltf = new Transform3D();
		filetf = new Transform3D();
		shapetf = new Transform3D();

		globallod = new float[12];
		filelod = new float[12];

        globalcrease = 44.0/180*Math.PI;
        filecrease = -1;

		stdout = System.err;
	}

	public void writeFile(File output, boolean streamed)
		throws IOException
	{
        if (streamed)
            writer.writeStream(output);
        else
            writer.writeFile(output);
	}

	public void writeZip(File output)
		throws IOException
	{
		writer.writeZip(output);
	}

	public void printStats()
	{
		writer.printStats();
	}

	public void process()
		throws IOException
	{
		Element doc = control.getDocumentElement();
		for (Node nd = doc.getFirstChild(); nd != null; nd = nd.getNextSibling())
		{
			if (! (nd instanceof Element)) continue;

			Element el = (Element)nd;
            processParams(el,writer.getFileParams());
			processTransform(el,globaltf);
			processLOD(el,globallod);
            globalcrease = processCrease(el,globalcrease);

			String tag = el.getTagName();

			if (tag.equalsIgnoreCase("title"))
			{
				String text = el.getFirstChild().getNodeValue();
				String lang = el.getAttribute("lang");
				writer.setTitle(text,lang);
			}

			if (tag.equalsIgnoreCase("author"))
				writer.setAuthor(el.getFirstChild().getNodeValue());

			if (tag.equalsIgnoreCase("by_reference"))
				writer.setGeometryByReference(booleanValue(el));
			if (tag.equalsIgnoreCase("interleaved"))
				writer.setInterleavedGeometry(booleanValue(el));
			if (tag.equalsIgnoreCase("strips"))
				writer.setStripify(booleanValue(el));
			if (tag.equalsIgnoreCase("normals"))
				writer.setGenerateNormals(booleanValue(el));

			if (tag.equalsIgnoreCase("file"))
				processFile(el);
		}
	}

    private void processParams(Element nd, HashMap params)
    {
        String tag = nd.getTagName();
        Object value = null;

        if (tag.equalsIgnoreCase("param")) {
            //  String parameter
            value = nd.getFirstChild().getNodeValue();
        }
        else if (tag.equalsIgnoreCase("point")) {
            //  point parameter
            value = point3fValue(nd);
        }
        else if (tag.equalsIgnoreCase("float")) {
            //  floating point parameter
            value = new Float(floatValue(nd));
        }
        else
            return;

        String key = nd.getAttribute("name");
        params.put(key,value);
    }

	private void processTransform(Element nd, Transform3D tf)
	{
		String tag = nd.getTagName();

		if (tag.equalsIgnoreCase("offset"))
		{
			Point3f offset = point3fValue(nd);
			Transform3D aux = new Transform3D();
			aux.setTranslation(new Vector3f(-offset.x,-offset.y,-offset.z));
			tf.add(aux);
		}
		if (tag.equalsIgnoreCase("scale"))
		{
			Point3f scale = scaleValue(nd);
			Transform3D aux = new Transform3D();
			aux.setScale(new Vector3d(1d/scale.x,1d/scale.y,1d/scale.z));
			tf.mul(aux);
		}
		if (tag.equalsIgnoreCase("rotate"))
		{
			Point3f rot = point3fValue(nd);
			tf.rotX(-rot.x);
			tf.rotY(-rot.y);
			tf.rotZ(-rot.z);
		}
	}

	private void processLOD(Element nd, float[] lod)
	{
		String tag = nd.getTagName();
		if (tag.equalsIgnoreCase("threshhold"))
		{
			int level = intAttribute(nd,"lod");
			float value = floatValue(nd);
			lod[level] = value;
		}
	}

    private double processCrease(Element nd, double defaultValue)
    {
        String tag = nd.getTagName();
        if (tag.equalsIgnoreCase("crease"))
            return floatValue(nd)/180*Math.PI;
        else
            return defaultValue;
    }

	private void processFile(Element parent)
		throws IOException
	{
		String fileName = parent.getAttribute("name");

		//	load the file
		stdout.print("["+fileName);
		ModelLoader loader = new ModelLoader();
		Scene scene = loader.load(baseDir.getAbsolutePath()+File.separator+fileName);
		BranchGroup bg = scene.getSceneGroup();
		stdout.println("]");

		Enumeration shapes = bg.getAllChildren();

		filetf.setIdentity();
		for (Node nd = parent.getFirstChild(); nd != null; nd = nd.getNextSibling())
		{
			if (! (nd instanceof Element)) continue;

			Element el = (Element)nd;
            processParams(el,writer.getFileParams());
			processTransform(el,filetf);
			processLOD(el,filelod);
            filecrease = processCrease(el,filecrease);

			String tag = el.getTagName();

			if (tag.equalsIgnoreCase("shape"))
			{
				Shape3D shape = (Shape3D)shapes.nextElement();
				bg.removeChild(shape);
				processShape(el, shape);
			}
		}

	}

	private void processShape(Element parent, Shape3D shape)
	{
		String name = parent.getAttribute("name");
        int lod = intAttribute(parent,"lod");
		float lod_thresh = filelod[lod];

		HashMap tag = new HashMap();
        tag.put("name", name);
        tag.put("lod", new Integer(lod));

		if (lod_thresh==0f) lod_thresh = globallod[lod];

        double crease = filecrease;
        if (crease < 0.0) crease = globalcrease;

		shapetf.set(globaltf);
		shapetf.mul(filetf);

		for (Node nd = parent.getFirstChild(); nd != null; nd = nd.getNextSibling())
		{
			if (! (nd instanceof Element)) continue;

			Element el = (Element)nd;
            processParams(el,tag);
			processTransform(el,shapetf);
            crease = processCrease(el,crease);

			String tagname = el.getTagName();
			if (tagname.equalsIgnoreCase("threshhold"))
				lod_thresh = floatValue(el);
		}
        tag.put("lod_thresh", new Float(lod_thresh));

		//	insert the shape
		stdout.print("   ["+name+"/"+lod);
		writer.add(shape, shapetf, tag, crease);
        stdout.println("]");
	}

	private static boolean booleanValue(Element nd)
	{
		String stringValue = nd.getFirstChild().getNodeValue();
		return (stringValue!=null) &&
				(stringValue.equalsIgnoreCase("1") || stringValue.equalsIgnoreCase("true"));
	}

	private static float floatValue(Element nd)
	{
		String stringValue = nd.getFirstChild().getNodeValue();
		return Float.parseFloat(stringValue);
	}

	private static Point3f point3fValue(Element nd)
	{
		return point3fValue(nd.getFirstChild().getNodeValue(),0f);
	}

	private static Point3f point3fValue(String stringValue, float defaultValue)
	{
		Point3f result = new Point3f(defaultValue,defaultValue,defaultValue);
		StringTokenizer tok = new StringTokenizer(stringValue,",");

		if (tok.hasMoreTokens())
			result.x = Float.parseFloat(tok.nextToken());
		if (tok.hasMoreTokens())
			result.y = Float.parseFloat(tok.nextToken());
		if (tok.hasMoreTokens())
			result.z = Float.parseFloat(tok.nextToken());

		return result;
	}

	private static Point3f scaleValue(Element nd)
	{
		String stringValue = nd.getFirstChild().getNodeValue();
		if (stringValue.indexOf(",") < 0)
		{
			float scale = Float.parseFloat(stringValue);
			return new Point3f(scale,scale,scale);
		}
		else
			return point3fValue(stringValue,1f);
	}


	private static int intAttribute(Element nd, String name)
	{
		String stringValue = nd.getAttribute(name);
		if (stringValue==null || stringValue.length()==0)
			return 0;
		else
			return Integer.parseInt(stringValue);
	}


	public static void printHelp(PrintStream out)
	{
		out.println("java "+Jo3DAssembler.class.getName()+" <control file> [output file]");
		out.println(" examples: ");
		out.println("   chess                    reads from chess.xml, creates chess.jo3d ");
		out.println("   chess.xml chess.jo3d     reads from chess.xml, creates chess.jo3d ");
	}

	public static final void main(String[] args)
	{
		try {
			String controlFileName = null;
			String outputFileName = null;
			boolean zip = false;
            boolean stream = false;

			for (int i=0; i<args.length; i++)
				if ("-zip".equalsIgnoreCase(args[i]))
					zip = true;
                else if ("-stream".equalsIgnoreCase(args[i]))
                    stream = true;
                else if (controlFileName==null)
					controlFileName = args[i];
				else if (outputFileName==null)
					outputFileName = args[i];

			if (controlFileName==null)
			{
				printHelp(System.out);
				System.exit(-1);
			}

			if (outputFileName==null) {
                if (stream)
                    outputFileName = FileUtil.setExtension(controlFileName,"j3ds");
                else
                    outputFileName = FileUtil.setExtension(controlFileName,"j3df");
            }
			else {
				zip = zip || FileUtil.hasExtension(outputFileName,"zip");
                stream = stream || FileUtil.hasExtension(outputFileName,"j3ds");

                if (!FileUtil.hasExtension(outputFileName)) {
                    if (zip)
                        outputFileName = FileUtil.setExtension(outputFileName,"zip");
                    else if (stream)
                        outputFileName = FileUtil.setExtension(outputFileName,"j3ds");
                    else
                        outputFileName = FileUtil.setExtension(outputFileName,"j3df");
                }
            }

			if (!FileUtil.hasExtension(controlFileName))
				controlFileName = FileUtil.setExtension(controlFileName,"xml");

			File controlFile = new File(controlFileName);
			File outputFile = new File(outputFileName);

			if (!controlFile.exists())
			{
				printHelp(System.out);
				System.exit(-1);
			}

			Jo3DAssembler asm = new Jo3DAssembler(controlFile);
			asm.process();

            if (zip)
                asm.writeZip(outputFile);
            else if (stream)
                asm.writeFile(outputFile,true);
            else
			    asm.writeFile(outputFile,false);

			System.err.println(outputFileName+" created");
			asm.printStats();

		} catch (Throwable ex) {
			Application.error(ex);
		}
	}

} // class Jo3DFileUtil

/*
 * $Log: $
 *
 */

