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

package de.jose;

import de.jose.store.RAMFile;
import de.jose.util.xml.XMLUtil;
import de.jose.util.SoftCache;
import org.w3c.dom.Element;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

public class Sound
{
	/**	maps keys to URLs	*/
	protected static HashMap keyMap = new HashMap();
	/**	maps URLs to actual Sound objects	*/
	protected static SoftCache cache = new SoftCache();

	protected static SoundThread gSoundThread;

	public static void initialize(Config cfg)
	{
		try {
			Vector sounds = cfg.getElements("sound");
			for (int i=0; i<sounds.size(); i++)
				try {
					Element elm = (Element)sounds.get(i);
					String key = elm.getAttribute("id");
					String name = XMLUtil.getTextValue(elm);
					File file = new File(Application.theWorkingDirectory,name);
					URL url = new URL("file",null,file.getAbsolutePath());
					keyMap.put(key,url);
				} catch (MalformedURLException murlex) {
					murlex.printStackTrace();
				}
		} catch (Throwable ex) {
			Application.error(ex);
			//  don't let errors in static initialisation go unnoticed.
		}

		try {
			gSoundThread = new SoundThread();
			gSoundThread.sleepInitial(3000);
			gSoundThread.load("sound.error");	//	preload
			gSoundThread.load("sound.mate");		//	preload
			gSoundThread.load("sound.draw");		//	preload
			gSoundThread.load("sound.notify");		//	preload
			gSoundThread.start();
		} catch (Throwable ex) {
			Application.warning(ex);
		} finally {
			if (gSoundThread==null)
				Application.warning("sound thread not initialised");
		}
	}

	protected RAMFile data;
	protected AudioFormat audioFormat;
	protected SourceDataLine dataLine;

	private Sound()
	{ }

	public Sound(String key) throws Exception
	{
		this(getURL(key));
		cache.put(key,this);
	}

	public Sound(URL url) throws Exception
	{
		this(getAudioInputStream(url));
		cache.put(url,this);
	}

	public Sound(AudioInputStream audioStream) throws Exception
	{
		audioFormat = audioStream.getFormat();

		data = new RAMFile(4096);
		data.copy(0L, audioStream,0, Integer.MAX_VALUE);

		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		if (!AudioSystem.isLineSupported(info))
			throw new IllegalArgumentException("audio type not supported: "+audioFormat);

		dataLine = (SourceDataLine)AudioSystem.getLine(info);
		dataLine.open(audioFormat);
		dataLine.close();
		/*	make sure the audio system is set up
			(so that there is no audible delay when actually playing the sound)
		*/
	}

	public static AudioInputStream getAudioInputStream(URL url)
	        throws IOException, UnsupportedAudioFileException
	{
		int k = url.getPath().lastIndexOf("!");
		if (k > 0)
		try {
			//  zip entry
			String entryPath = url.getPath().substring(k+1);
			url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath().substring(0,k));

			ZipInputStream zin = new ZipInputStream(url.openStream());
			for (ZipEntry zety = zin.getNextEntry(); zety != null; zety = zin.getNextEntry())
				if (zety.getName().equals(entryPath))
					return AudioSystem.getAudioInputStream(zin);

			zin.close();
			throw new FileNotFoundException(url.toExternalForm());

		} catch (MalformedURLException muex) {
			throw new IOException(muex.getMessage());
		}
		else
			return AudioSystem.getAudioInputStream(url);
	}

	public static URL getURL(String key)
	{
		return (URL)keyMap.get(key);
	}

	public static void play(String fileName)
	{
		getSoundThread().play(fileName);
	}

	public static void load(String fileName)
	{
		getSoundThread().load(fileName);
	}

	public static void play(URL audioFile)
	{
		getSoundThread().play(audioFile);
	}

	public static void load(URL audioURL)
	{
		getSoundThread().load(audioURL);
	}


	protected static SoundThread getSoundThread()
	{
		if (gSoundThread==null) {
			gSoundThread = new SoundThread();
			gSoundThread.start();
		}
		return gSoundThread;
	}

	public static Sound getSound(String key) throws Exception {
		Sound result = (Sound)cache.get(key);
		if (result==null) {
			URL url = getURL(key);
			result = (Sound)cache.get(url);
		}
		if (result==null)
			try {
				result = new Sound(key);
			} catch (IllegalArgumentException e) {
				System.err.println(e.getMessage());
				cache.put(key,new Sound());	// dummy placeholder
			}
		return result;
	}

	public static Sound getSound(URL audioFile)
	{
		Sound result = (Sound)cache.get(audioFile);
		if (result==null)
			try {
				result = new Sound(audioFile);
			} catch (Exception e) {
				Application.error(e);
				return null;
			}
		return result;
	}

	public void play() {
		if (dataLine==null)
			Toolkit.getDefaultToolkit().beep();	//	sound is not available on this platfom (Linux!?)
		else
			try {
				dataLine.open(audioFormat);
				dataLine.start();

				int position = 0;
				for (;;) {
					byte[] block = data.getRawData(position);
					int blockSize = data.getRawBlockSize(position);
					if (blockSize<=0) break;

					dataLine.write(block,0, blockSize);
					position += blockSize;
				}

				dataLine.drain();
				dataLine.close();
			} catch (Throwable ex) {
				Toolkit.getDefaultToolkit().beep();
				//  better than nothing at all ;-(
			}
	}

	static class SoundThread extends Thread
	{
		protected Vector play = new Vector();
		protected Vector load = new Vector();
		protected long waitUntil = 0L;

		SoundThread()
		{
			super("jose.sound-play");
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
		}

		public void play(Object snd) {
			play.add(snd);
			this.interrupt();
		}

		public void load(Object fileName) {
			load.add(fileName);
			this.interrupt();
		}

		public void sleepInitial(long millis)
		{
			waitUntil = System.currentTimeMillis()+millis;
		}

		public void run()
		{
			try {
				for (;;)
				try {
					long now = System.currentTimeMillis();
					try {
						if (now < waitUntil)
							Thread.sleep(waitUntil-now);
						else
							Thread.sleep(50000);
					} catch (InterruptedException iex) {
						now = System.currentTimeMillis();
						if ((now < waitUntil) && play.isEmpty()) continue;
					}

					while (!play.isEmpty()) {
						Object obj = play.remove(0);
						Sound snd = null;
						if (obj instanceof Sound)
							snd = (Sound)obj;
						else if (obj instanceof String)
							snd = getSound((String)obj);
						else if (obj instanceof URL)
							snd = getSound((URL)obj);

						if (snd!=null)
							snd.play();
						else
							Toolkit.getDefaultToolkit().beep();	//	better than nothing ;-)
					}
					while (!load.isEmpty()) {
						Object obj = load.remove(0);
						if (obj instanceof String)
							getSound((String)obj);
						else if (obj instanceof URL)
							getSound((URL)obj);
					}

				}
				catch (Throwable ex) {
					ex.printStackTrace();
				}
			} finally {
				if (gSoundThread==this) gSoundThread = null;
			}
		}
	}
}
