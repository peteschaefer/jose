package de.jose.web;

import java.io.*;
import java.nio.file.Files;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;

import de.jose.pgn.Collection;
import de.jose.task.GameSource;
import de.jose.task.GameTask;
import de.jose.task.io.ArchiveExport;
import de.jose.task.io.PGNExport;
import org.apache.commons.io.IOUtils;

@WebServlet(name = "DownloadServlet", value = "/download-servlet")
public class DownloadServlet extends HttpServlet {


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        WebApplication.open(getServletContext(), response);
        SessionUtil su = new SessionUtil(request, request.getSession());

        int CId = su.getInt("CId",1,false);
        String output_type = su.getString("out","pgn",false);

        try {
            exportCollection(CId,output_type, response);
        } catch (Exception e) {
            e.printStackTrace(response.getWriter());
        }
    }

    private void exportCollection(int CId, String output_type,
                                  HttpServletResponse response) throws Exception
    {
        Collection coll = Collection.readCollection(CId);
        int count = coll.GameCount;

        String ext;
        if (output_type.equals("pgn")) {
            if (count >= 30) {
                response.setContentType("application/zip");
                ext = "zip";
            }
            else {
                response.setContentType("text/pgn");
                ext = "pgn";
                //response.setContentType("application/zip");
                //  todo distinguish zip/plain text
            }
        } else if (output_type.equals("archive")) {
            response.setContentType("application/zip");
            ext = "jose";
        }
        else {
            throw new IllegalStateException(output_type);
        }

        File tempFile = File.createTempFile("download", "."+ext);
        String name = coll.Name+"."+ext;
        response.setHeader("Content-disposition", "attachment;filename=\""+name+"\"");

        GameTask task = null;
        if (output_type.equals("pgn")) {
            task = new PGNExport(tempFile,"utf-8");
        }
        else if (output_type.equals("archive")) {
            task = new ArchiveExport(tempFile);
        }

        GameSource source = GameSource.singleCollection(CId);

        task.setSource(source);
        task.start();
        task.join();

        long size = Files.size(tempFile.toPath());
        response.setContentLength((int)size);

        //  stream temp file to output
        IOUtils.copy(new FileInputStream(tempFile), response.getOutputStream());
    }

    public void destroy() {
    }
}