package de.jose.util.file;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;

/**
 * used to read resources from a filesystem directory
 * (instead of from a .jar file)
 */
public class ResourceClassLoader extends ClassLoader
{
    private File root;

    public ResourceClassLoader(File root) {
        this.root = root;
    }

    public ResourceClassLoader(String root) {
        this.root = new File(root);
    }

    @Override
    protected URL findResource(String name) {
        File file = new File( root, name );
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        URL[] urls = new URL[] { findResource(name) };
        return Collections.enumeration(Arrays.asList(urls));
    }

    @Override
    public URL getResource(String name) {
        Objects.requireNonNull(name);
        return findResource(name);
    }

}
