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

import com.sun.j3d.utils.universe.SimpleUniverse;
import de.jose.chess.Clock;
import de.jose.chess.Move;
import de.jose.image.Surface;
import de.jose.image.TextureCache;
import de.jose.jo3d.ImageRoll;
import de.jose.jo3d.SplineKeyFrame;
import de.jose.jo3d.SplineOrbitBehavior;
import de.jose.jo3d.TextureCache3D;
import de.jose.pgn.Game;
import de.jose.profile.LayoutProfile;
import de.jose.profile.UserProfile;
import de.jose.util.file.FileUtil;
import de.jose.view.Animation;
import de.jose.view.BoardView3D;
import de.jose.view.JoPanel;
import de.jose.view.MainBoardPanel;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.Player;

import javax.media.j3d.Alpha;
import javax.media.j3d.Transform3D;
import javax.swing.*;
import javax.vecmath.Point3f;
import java.applet.AppletContext;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class FlyBy
        extends AbstractApplication
        implements WindowListener, AWTEventListener
{
    //-------------------------------------------------------------------------------
    //	constants
	//-------------------------------------------------------------------------------

    /** Duration (in seconds) of camera animation   */
    private static final double DURATION        = 25.0;
    /** Duration (in seconds) of move animation   */
    private static final double MOVE_DURATION    = 1.0;

	//-------------------------------------------------------------------------------
    //	variables
	//-------------------------------------------------------------------------------

	/**	use full screen window ? default is false	*/
	private boolean fullScreen	= false;
	private boolean changeDisplay = false;
	private int screenWidth		= 640;
	private int screenHeight	= 480;
	private int screenDepth		= 0;	//	= default
	private int screenRefresh	= 0;	//	= best available
    private boolean showClock = true;
	private boolean showDisplayModes = false;
    private URL gameText;
    private URL audio;
    private ImageRoll theCredits;
    /** MP3 player  */
    private Player thePlayer;
	private static Thread shutDownHook;
    protected JFrame theWindow;

    //-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	/**
	 * creates a new standalone object
	 *
	 * @param args
	 * @throws Exception
	 */
	public FlyBy(String[] args)
		throws Exception
	{
		super();

		logErrors = false;
		URL url = getResource("images/jose.gif");
		theIconImage = Toolkit.getDefaultToolkit().createImage(url);

        gameText = getResource("immortal.txt");
        audio = getResource("background.mp3");

		processArgs(args,0,args.length-1);
	}

	public void processArgs(String[] args, int from, int to)
		throws Exception
	{
		for (int i=from; i<=to; i++)
			if (args[i]==null)
				continue;
			else if (args[i].equalsIgnoreCase("-wd"))
				theWorkingDirectory = new File(args[++i]);
			else if (args[i].equalsIgnoreCase("-framerate"))
				showFrameRate = true;
			else if (args[i].equalsIgnoreCase("-fullscreen") || args[i].equalsIgnoreCase("-f"))
				fullScreen = true;
            else if (args[i].equalsIgnoreCase("-clock"))
				showClock = true;
            else if (args[i].equalsIgnoreCase("-noclock"))
				showClock = false;
            else if (args[i].equalsIgnoreCase("-game"))
                gameText = new URL("file",null,args[++i]);
            else if (args[i].equalsIgnoreCase("-audio"))
                audio = new URL("file",null,args[++i]);
            else if (args[i].equalsIgnoreCase("-dm"))
	            showDisplayModes = true;
			else {
				//	  "width x height @ bit-depth / refresh-rate"
				//		bit-depth and refresh rate are optional
				int k1 = args[i].indexOf("x");
				int k2 = args[i].indexOf("@");
				int k3 = args[i].indexOf("/");

				if (k1 >= 0) {
					screenWidth = Integer.parseInt(args[i].substring(0,k1));
					if (k2 >= 0)
						screenHeight = Integer.parseInt(args[i].substring(k1+1,k2));
					else
						screenHeight = Integer.parseInt(args[i].substring(k1+1));
				}
				if (k2 >= 0) {
					if (k3 >= 0) {
						screenDepth = Integer.parseInt(args[i].substring(k2+1,k3));
						screenRefresh = Integer.parseInt(args[i].substring(k3+1));
					}
					else
						screenDepth = Integer.parseInt(args[i].substring(k2+1));
				}
				changeDisplay = true;
			}
	}

	public void setup(GraphicsConfiguration gc)
		throws Exception
	{

		theAbstractApplication = this;
		theWorkingDirectory = null;

		theCommandDispatcher = new CommandDispatcher();
		theCommandDispatcher.addCommandListener(this);

		/*	 create a fake user profile */
		theUserProfile = createUserProfile();

		/* setup a game of chess */
		theGame = new Game(theUserProfile.getStyleContext(), "","",new Date(), START_POSITION,null);
        String text = FileUtil.readTextStream(gameText, "ISO-8859-1");
		theGame.parse(null, text);

		theClock = new Clock();
        theClock.reset(5*60*1000, 5*60*1000);

		LayoutProfile profile = new LayoutProfile("window.board");
		JoPanel.create(MainBoardPanel.class, profile, false,false);
		boardPanel().set3d(gc);
		boardPanel().init();

		/*	setup 3D board	*/
		BoardView3D v3d = boardPanel().get3dView();
		v3d.enablePicking(false);
        v3d.setClipDistance(0.005,3.0);

        /*  setup credits   */
        URL credUrl = getResource("images/credits.png");
        theCredits = new ImageRoll(credUrl,
                    new Point3f(-500f,+1000f,+1000f),
                    new Point3f(-500f,0f,+1000f),
                    new Dimension(1000,1000),boardPanel());
        v3d.addObject(theCredits);

		/**	start the game animation	*/
		animation = new Animation((long)(1000*MOVE_DURATION));

        /** start the camera animation  */
        InputStream csv = getResourceStream("flight.csv");
        if (csv!=null) {
            List keyFrames = SplineKeyFrame.read(csv, null);

            if (!keyFrames.isEmpty()) {
                Alpha alpha = new Alpha(-1, (long)(1000*DURATION));
                SplineOrbitBehavior orbit = new SplineOrbitBehavior(alpha,
                                    v3d.getViewTransformGroup(),
                                    new Transform3D(),
                                    SplineKeyFrame.toKeyFrames(keyFrames,0,keyFrames.size()));

                orbit.addListener(v3d.getGlobalClip());
                v3d.addBehavior(orbit);
                alpha.setStartTime(System.currentTimeMillis());
            }
        }

    }

	//-------------------------------------------------------------------------------
	//	extends Applet
	//-------------------------------------------------------------------------------

	public FlyBy()
	{
		super();
	}

	/**
	 * initialize an applet
	 */
	public void init()
	{
		isApplet = true;
        theAbstractApplication = null;

		try {
			if (!checkSystemRequirements(getAppletContext()))
				return;

            /** get applet arguments    */
            String param = getParameter("audio");
            if (param != null)
	            audio = new URL(getDocumentBase(),param);
			else
            	audio = getResource("background.mp3");

            param = getParameter("game");
            if (param != null)
	            gameText = new URL(getDocumentBase(),param);
			else
				gameText = getResource("immortal.txt");

			setup(null);

//          System.out.println(audio.toExternalForm());
//          System.out.println(gameText.toExternalForm());

			setLayout(new BorderLayout());
			this.add(boardPanel().get3dView(), BorderLayout.CENTER);

		} catch (Exception ex) {
			ex.printStackTrace();
            theAbstractApplication = null;
		}
	}

	public void destroy()
	{

	}

	public void start()
	{
        if (theAbstractApplication != null)
            try {
                boardPanel().get3dView().getCanvas().getView().startView();
                getAnimation().start(3000);
                startMp3Player();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
	}

	public void stop()
	{
        if (theAbstractApplication != null)
            try {
                boardPanel().get3dView().getCanvas().getView().stopView();
                removeAll();
            } catch (Throwable ex) {
                ex.printStackTrace();	//	who cares ?
            }
	}

    public String[][] getParameterInfo()
	{
        return new String[][] {
            { "game",   "url, text file",            "the chess game that will be animated" },
            { "audio",  "url, mp3 audio file",       "background auio file"    },
        };

    }


	//-------------------------------------------------------------------------------
	//	extends Application
	//-------------------------------------------------------------------------------

	/**
	 * open the standalone application in a separate window
	 */
	public void open()
		throws Exception
	{
		/*	setup window */
		GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
		theWindow = null;

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		Rectangle screen = gc.getBounds();

		if (showDisplayModes) showDisplayModes(gd);

		boolean isFullScreen = false;

		if (fullScreen) {
			//JWindow jwindow = new JWindow(null,gc);
			theWindow = new JFrame(null,gc);
			showLoadScreen(theWindow);
			theWindow.setUndecorated(true);
			theWindow.setBounds(screen.x,screen.y, screen.width,screen.height);

			boolean canFullScreen = Version.java14orLater && gd.isFullScreenSupported();
			if (canFullScreen) {
				//	full screen
				isFullScreen = true;
				gd.setFullScreenWindow(theWindow);
			}
		}
		else {
			//		windowed mode; don't modify display mode, just set size
			theWindow = new JFrame("jose - FlyBy");
			showLoadScreen(theWindow);
			theWindow.setIconImage(theIconImage);
			theWindow.setBounds(screen.x+(screen.width-screenWidth)/2, screen.y+(screen.height-screenHeight)/2,
			        		screenWidth, screenHeight);
		}

		theWindow.addWindowListener(this);
		Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);

		if (isFullScreen && changeDisplay) {
			boolean canChangeDisplay = Version.java14orLater && gd.isDisplayChangeSupported();
			if (canChangeDisplay) {
				//	note that changes to display mode may only work in fullscreen
				//	that's why the call to setFullScreenWindow was made first
				System.out.println("setting display mode "+toString(screenWidth,screenHeight,screenDepth,screenRefresh));
				theWindow.setBounds(0,0, screenWidth,screenHeight);
				setDisplayMode(gd, screenWidth,screenHeight,screenDepth,screenRefresh);
			}
		}

		setup(gc);

		theWindow.getContentPane().removeAll();
		theWindow.getContentPane().add(boardPanel().get3dView());
		theWindow.setVisible(true);

		getAnimation().start(3000);
        startMp3Player();
//        theCredits.start();
	}

	protected void showLoadScreen(JFrame window)
	{
		Label label = new Label("loading ...");
		label.setBounds(0,0, 100,40);
		window.getContentPane().add(label);
	}

	protected void startMp3Player()
		throws Exception
	{
		/**	setup mp3 player	*/
		InputStream audioStream;
		try {
			audioStream = audio.openStream();
		} catch (Exception ex) {
			//	can't help it ;-(
			return;
		}

        audioStream = new BufferedInputStream(audioStream, 64*1024);
//        audioStream = new DoubleBufferInputStream(audioStream, 32*1024);
        AudioDevice audioDev = FactoryRegistry.systemRegistry().createAudioDevice();
        thePlayer = new Player(audioStream,audioDev);
        //	this mehod will block for a very long time, let's start a new thread for it !
        Thread playerThread = new Thread() {
            public void run() {
                try {
                    thePlayer.play();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        playerThread.start();
	}


	private UserProfile createUserProfile()
		throws Exception
	{
		UserProfile prf = new UserProfile(false);
		prf.createMinimalStyles();

		prf.set("board.3d", 					Boolean.TRUE);
		prf.set("board.3d.model",				"flyby.j3ds");
		prf.set("board.3d.light.ambient",		new Color(0.1f,0.1f,0.1f));
		prf.set("board.3d.light.directional",	new Color(0.9f,0.9f,0.9f));
		prf.set("board.3d.surface.frame",		Surface.newTexture(66,45,0, "wood03.jpg"));
		prf.set("board.surface.light",			Surface.newTexture(0xee,0xee,0xee, "marble04.jpg"));
		prf.set("board.surface.dark",			Surface.newTexture(0x88, 0x88, 0x88, "marble13.jpg"));
		prf.set("board.surface.white",			Surface.newColor(Color.white, "wood30.jpg"));
		prf.set("board.surface.black",			Surface.newColor(Color.black, "wood22.jpg"));
		prf.set("board.surface.background",		Surface.newTexture(Color.white, "marble12.jpg"));

        prf.set("board.3d.clock",               Util.toBoolean(showClock));
		prf.set("board.3d.shadow",				Boolean.TRUE);
		prf.set("board.3d.reflection",			Boolean.TRUE);
        prf.set("board.3d.anisotropic",         Boolean.FALSE);
        prf.set("board.3d.fsaa",                Boolean.FALSE);

		prf.set("board.3d.camera.distance",		new Double(3.0));
		prf.set("board.3d.camera.latitude",		new Double(2*Math.PI/8));

		/**	tells BoardView3D to run continuously; that means the 3D render loop
		 * 	will eat up all of the CPU time (or rather: all of the GPU time)
		 */
		prf.set("board.3d.flyby",   			Boolean.TRUE);

		/**
		 * load texture images from jar file; store int TextureCache
		 */
		TextureCache.addFromResource("wood03.jpg",TextureCache.LEVEL_256, true);
		TextureCache.addFromResource("wood04.jpg",TextureCache.LEVEL_256, true);
		TextureCache.addFromResource("wood22.jpg",TextureCache.LEVEL_256, true);
        TextureCache.addFromResource("wood30.jpg",TextureCache.LEVEL_256, true);
		TextureCache.addFromResource("marble04.jpg",TextureCache.LEVEL_256, true);
		TextureCache.addFromResource("marble13.jpg",TextureCache.LEVEL_256, true);
		TextureCache.addFromResource("marble12.jpg",TextureCache.LEVEL_256, true);
        TextureCache.addFromResource("metal01.jpg",TextureCache.LEVEL_256, true);

		TextureCache3D.addDecalFromResource("images/textures/decals","p1.png", true, this);
		TextureCache3D.addDecalFromResource("images/textures/decals","b1.png", true, this);
		TextureCache3D.addDecalFromResource("images/textures/decals","n1.png", true, this);
		TextureCache3D.addDecalFromResource("images/textures/decals","r1.png", true, this);
		TextureCache3D.addDecalFromResource("images/textures/decals","q1.png", true, this);
		TextureCache3D.addDecalFromResource("images/textures/decals","k1.png", true, this);

		return prf;
	}

 	//-------------------------------------------------------------------------------
	//	interface CommandListener
	//-------------------------------------------------------------------------------

	public int numCommandChildren()
	{
		return 1;
	}
	public CommandListener getCommandChild(int i)
	{
		return boardPanel();
	}

	public void setupActionMap(Map map)
	{
		/**	there will be only one command
		 * 	issued by theAnimation in regular intervals
		 */

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) {
				Move mv = theGame.forward();

				if (mv==null) {
				    //  return to start
				    theGame.first();
				    boardPanel().get3dView().refresh(true);
				    //  roll the credits
				    theCredits.start();
				}
				else {
				    if (boardPanel() != null)
				        boardPanel().move(mv, getAnimation().getSpeed());
				}

				theClock.setCurrent(theGame.getPosition().movesNext());
			}
		};
		map.put("move.forward", action);
	}


	//-------------------------------------------------------------------------------
	//	interface WindowListener
	//-------------------------------------------------------------------------------

	public void windowOpened(WindowEvent e)
	{ }

	public void windowClosing(WindowEvent e)
	{
		/*	forward to CommandListener	*/
		shutDown();
	}

	public void windowClosed(WindowEvent e)
	{ }

	public void windowIconified(WindowEvent e)
	{ }

	public void windowDeiconified(WindowEvent e)
	{ }

	public void windowActivated(WindowEvent e)
	{ }


	public void windowDeactivated(WindowEvent e)
	{ }

	//-------------------------------------------------------------------------------
	//	interfac AWTEventListener
	//-------------------------------------------------------------------------------

    public void eventDispatched(AWTEvent evt)
	{
        if (evt.getID()==KeyEvent.KEY_PRESSED)
	    {
	        KeyEvent kevt = (KeyEvent)evt;
	        if (kevt.getKeyCode()==KeyEvent.VK_ESCAPE)
		        shutDown();
	        if (kevt.getKeyCode()==KeyEvent.VK_F12)
		        boardPanel().get3dView().capture(null);
        }

    }

	/**	return never	*/
	public void shutDown()
	{
		if (shutDownHook != null) {
			shutDownHook.start();
			Runtime.getRuntime().removeShutdownHook(shutDownHook);
		}

		if (getAnimation() != null)
			getAnimation().stop();
		if (boardPanel()!=null && boardPanel().get3dView() != null)
			boardPanel().get3dView().close();
		System.exit(1);
		//	will start ShutdowHook, too
	}

	//-------------------------------------------------------------------------------
    //	display mode utils
    //-------------------------------------------------------------------------------

    /**
     * @since 1.4
     */
	public static void setDisplayMode(GraphicsDevice gd, int width, int height, int depth, int refresh)
	{
//		showDisplayModes(gd);
	    DisplayMode current = gd.getDisplayMode();

		if (depth==0) depth = current.getBitDepth();	//	don't change current depth
		if (refresh==0 || refresh==DisplayMode.REFRESH_RATE_UNKNOWN)
			refresh = optimalRefreshRate(gd, width, height, depth);

		DisplayMode dm = new DisplayMode(width,height,depth,refresh);
		try {
			shutDownHook = new ShutdownThread(gd,gd.getDisplayMode());
			Runtime.getRuntime().addShutdownHook(shutDownHook);
			//	reset display mode on exit
			gd.setDisplayMode(dm);
		} catch (IllegalArgumentException iaex) {
			System.out.println("display mode "+toString(dm)+" is not allowed");
			System.out.println("available display modes:");
			showDisplayModes(gd);
		}
	}

	static class ShutdownThread extends Thread
	{
		private GraphicsDevice gd;
		private DisplayMode dm;

		ShutdownThread(GraphicsDevice device, DisplayMode mode)
		{
			gd = device;
			dm = mode;
		}

		public void run()
		{
			if (gd != null)
				gd.setDisplayMode(dm);
		}
	}

    /**
     * @since 1.4
     */
	public static int optimalRefreshRate(GraphicsDevice gd, int width, int height, int depth)
	{
		DisplayMode[] dm = gd.getDisplayModes();
		int result = 0;

		for (int i=0; i<dm.length; i++)
			if (dm[i].getWidth() >= width && dm[i].getHeight() >= height && dm[i].getBitDepth() >= depth)
			{
				int rate = dm[i].getRefreshRate();
				if (rate != DisplayMode.REFRESH_RATE_UNKNOWN && rate > result)
					result = rate;
			}
		if (result==0)
			result = DisplayMode.REFRESH_RATE_UNKNOWN;
		return result;
	}

    /**
     * @since 1.4
     */
	public static void showDisplayModes(GraphicsDevice gd)
	{
		DisplayMode[] dm = gd.getDisplayModes();
		for (int i=0; i<dm.length; i++)
			System.out.println(" "+toString(dm[i]));
	}

    /**
     * @since 1.4
     */
	public static String toString(DisplayMode dm)
	{
		return toString(dm.getWidth(),dm.getHeight(),dm.getBitDepth(),dm.getRefreshRate());
	}

	public static String toString(int width, int height, int depth, int rate)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(Integer.toString(width));
		buf.append('x');
		buf.append(Integer.toString(height));

		if (depth == DisplayMode.BIT_DEPTH_MULTI)
			buf.append("@multi");
		else {
			buf.append('@');
			buf.append(Integer.toString(depth));
		}

		if (rate != DisplayMode.REFRESH_RATE_UNKNOWN)
		{
			buf.append('/');
			buf.append(Integer.toString(rate));
		}

		return buf.toString();
	}


	public static boolean checkSystemRequirements(AppletContext context)
        throws MalformedURLException
	{
		/**	check minimum system requirements	*/
		if (Version.java.compareTo("1.3") < 0)
		{
            URL url = new URL("http://java.sun.com/j2se/downloads.html");
			JOptionPane.showMessageDialog(null,
			        "Java 1.3 or later required \n\n"+
			        " the latest Java Runtime Environment can be downloaded from \n"+
			        " "+url.toExternalForm(),
			        "Error", JOptionPane.ERROR_MESSAGE);
            if (context != null)
                context.showDocument(url);
			return false;
		}

		if (! Version.hasJava3d(false,false) || Version.getJava3dVersion(false).compareTo("1.3") < 0)
		{
            URL url = new URL("http://java.sun.com/products/java-media/3D/download.html");
			JOptionPane.showMessageDialog(null,
			        "Java3D 1.3 or later required \n\n"+
			        " Java3D can be downloaded from \n"+
			        " "+url.toExternalForm()+"\n\n"+
			        " Linux users should have a look at \n"+
			        " http://www.blackdown.org/java-linux/jdk1.2-status/java-3d-status.html",
			        "Error", JOptionPane.ERROR_MESSAGE);
            if (context != null)
                context.showDocument(url);
			return false;
		}

		return true;
	}

    //-------------------------------------------------------------------------------
	//	main entry point
	//-------------------------------------------------------------------------------

	public static void main(String[] args)
	{
		try {
			logErrors = false;
			System.setProperty("sun.java2d.noddraw","true");
			//	important for Fullscreen mode

			if (!checkSystemRequirements(null))
				System.exit(-1);

			new FlyBy(args).open();

		} catch (Throwable ex) {
			fatalError(ex,-1);
		}
	}

}
