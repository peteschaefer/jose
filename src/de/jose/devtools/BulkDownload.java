package de.jose.devtools;

import de.jose.util.file.FileUtil;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 
 */
public class BulkDownload
{
    static String baseURL = "http://chessclinic.kalandor.hu/jatszmak/";  // +"A04(1).zip"
	static String altURL = "http://chessclinic.kalandor.hu/jatszmak2/";  // +"A04.zip"
    static File targetDir = new File("F:/download");
	static String targetFile = "3m";
	static ZipOutputStream zout;
    static PrintWriter log;

    static boolean download1(String base, String code)
    {
        URLConnection conn = null;
        InputStream input = null;
        try {
            URL url = new URL(base+code+".zip");
            conn = url.openConnection();
            int size = conn.getContentLength();

            input = conn.getInputStream();

            if (FileUtil.copyStream(input, size, new File(targetDir,code+".zip")) < size) {
				log.println(" --- "+code+" incomplete ");
	            return false;
            }
			else {
	            log.println(" --- "+code+" OK --- ");
	            return true;
            }

        } catch (Exception e) {
            log.println(" --- "+code+" failed: "+e.getClass().getName()+" "+e.getLocalizedMessage());
            return false;
        } finally {
	        if (input!=null) try { input.close(); } catch (Exception ex) { };
        }
    }

    public static void main(String[] args)  throws Exception
    {
        for (int i=0; i<args.length; i++)
        {
            if (args[i].equalsIgnoreCase("-url"))
                baseURL = args[++i];
            else if (args[i].equalsIgnoreCase("-dir"))
                targetDir = new File(args[++i]);
            else if (args[i].equalsIgnoreCase("-file"))
                targetFile = args[++i];
        }

	    //  download();
	    merge();
    }

	public static boolean merge1(File file)
	{
		ZipInputStream zin = null;
		log.print(" --- add "+file.getName());
		try {

			FileInputStream fin = new FileInputStream(file);
			BufferedInputStream bin = new BufferedInputStream(fin,4096);
			zin = new ZipInputStream(bin);

			while (zin.getNextEntry()!=null) {
				zout.write('\n');
				zout.write('\n');
				FileUtil.copyStream(zin, zout);
			}

			log.println(" --- ");
			return true;

		} catch (Exception e) {
			log.println(" failed: "+e.getClass().getName()+" "+e.getLocalizedMessage());
			return false;
		} finally {
			if (zin!=null) try { zin.close(); } catch (Exception ex) { };
		}
	}

	public static void download()  throws Exception
	{
        log = new PrintWriter(new FileWriter(new File(targetDir,"download.log"),true),true);

        log.println(" ------------------------------ ");
        log.println(" --- start "+(new Date())+" --- ");
        log.println(" --- from "+baseURL+" --- ");

        for (char c='A'; c <= 'E'; c++) {
            for (int i=0; i<=99; i++) {
                String code = String.valueOf(c);
                if (i<10) code += "0";
                code += i;

                log.println(" --- "+code+" --- ");

                //  try A00.zip
                download1(altURL, code);
                for (int o=1; ; o++) {
                    if (!download1(baseURL, code+"("+o+")") &&
                        !download1(baseURL, code+o))
                        break;
                }
            }
        }
    }

	public static void merge() throws Exception
	{
		log = new PrintWriter(new FileWriter(new File(targetDir,"merge.log"),true),true);

		File target = new File(targetDir,targetFile+".zip");
		FileOutputStream fout = new FileOutputStream(target);
		BufferedOutputStream bout = new BufferedOutputStream(fout,4096);

		zout = new ZipOutputStream(bout);
		zout.setLevel(9);

		ZipEntry zety = new ZipEntry(targetFile+".pgn");
		zout.putNextEntry(zety);

		String[] files = targetDir.list();
		for (int i=0; i < files.length; i++)
			if (!files[i].equals(targetFile+".zip") && FileUtil.hasExtension(files[i],"zip"))
				merge1(new File(targetDir,files[i]));

		zout.close();
	}
}
