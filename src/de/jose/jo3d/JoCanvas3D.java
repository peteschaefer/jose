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

import com.sun.j3d.utils.timer.J3DTimer;
import de.jose.Application;
import de.jose.Command;
import de.jose.MessageListener;
import de.jose.view.BoardView;
import de.jose.image.ImgUtil;

import javax.media.j3d.*;
import javax.vecmath.Point3f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * JoCanvas3D extends Canvas3D
 * it adds a capability to store screen shots on disk
 * the paint method kicks off the view, if it is not currently running
 * (so that update events will be served during inactivity)
 *
 * @author Peter Schäfer
*/

public class JoCanvas3D extends Canvas3D
{
	/**	for starting and stopping the view in a controlled fashion */
	private ViewController viewCtrl;
	/**	frame counter */
	private int frameCount;
	/**	indicates that next frame should be captured	*/
    private boolean captureNext;
	/**	JPEG target file */
	private File captureFile;
	/** callback command after capture  */
	private MessageListener captureCallback;
	/**	JPEG quality parameter */
	private float quality;

	/**	record frame count ? */
	private boolean recordFrames;
	/**	frame start times */
	private long[][] timer;
	/**	next report */
	private long lastReport;

	/**	default front clip distance	*/
	public static final float	FRONT_CLIP_DISTANCE	= 0.05f;
	/**	default back clip distance
	 * 	for optimal z-buffer resolution, 100 < BACK_CLIP/FRONT_CLIP < 1000
	 * */
	public static final float	BACK_CLIP_DISTANCE	= 3.0f;

	public static final long NANOS_PER_SECOND = 1000000000L;

    public JoCanvas3D(GraphicsConfiguration gc, float imageQuality, boolean useViewController) {
		super(gc);
		captureNext = false;
		frameCount = 0;
		quality = imageQuality;
        if (useViewController) viewCtrl = new ViewController();
		timer = new long[400][3];
		lastReport = 0L;
    }


	public void setImageQuality(float imageQuality)
	{
		quality = imageQuality;
	}

	public int getFrameCount()						{ return frameCount; }

	public void setFrameRecord(boolean on)			{ recordFrames = on; }

	public final long[] times(int frameOffset)		{ return timer[(frameCount-frameOffset) % timer.length]; }

    public ViewController getViewController()       { return viewCtrl; }

	public void capture(File target)
	{
		captureNext = true;
		captureFile = target;
		captureCallback = null;
	}

	public void capture(MessageListener callbackTarget)
	{
		captureNext = true;
		captureFile = null;
		captureCallback = callbackTarget;
	}

	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
	}

	/**	called before rendering */
	public void preRender()
	{
		frameCount++;
		if (recordFrames) times(0)[0] = J3DTimer.getValue();
		if (viewCtrl!=null)
			viewCtrl.preRender();
	}

	/**	called after rendering */
	public void postRender()
	{
		if (recordFrames) times(0)[1] = J3DTimer.getValue();
		if (viewCtrl!=null)
			viewCtrl.postRender();
	}

	/**	called after swapping the buffers */
    public void postSwap()
	{
		if (recordFrames) {
			long nanos = J3DTimer.getValue();
			times(0)[2] = nanos;

			if (nanos >= (lastReport+NANOS_PER_SECOND))
				frameReport();
		}

		if(captureNext)
			capture();

		//	notify View Controller
		if (viewCtrl != null)
			viewCtrl.postSwap();
    }

	public final void startView(boolean immediate)
	{
		if (viewCtrl!=null)
			viewCtrl.startView(getView(),immediate);
	}

	public final void stopView(boolean immediate)
	{
		if (viewCtrl!=null)
			viewCtrl.stopView(getView(),immediate);
	}

    public final void doStopView(boolean immediate)
    {
	    if (viewCtrl!=null)
	    	viewCtrl.doStopView(getView(),immediate);
    }

	private void frameReport()
	{
		//	how many frames have been rendered in the previous second ?
		long current = times(0)[2];
		long renderTime = 0L;
		long swapTime = 0L;
		int count = 0;
		for (long[] t = times(count); (count < timer.length) && t[2] >= (current-NANOS_PER_SECOND); t = times(++count))
		{
			renderTime += t[1] - t[0];
			swapTime += t[2] - t[1];
		}

		System.out.print(count);
		if (count > 0)
			System.out.print(" ("+(renderTime*1000.0/(count*NANOS_PER_SECOND))+","+(swapTime*1000.0/(count*NANOS_PER_SECOND))+")");
		System.out.println();

		lastReport = current;
	}

	/**
	 * get the amount of free memory on the graphics card
	 * (int bytes)
	 */
	public int availableGraphicsMemory()
	{
		return getGraphicsConfiguration().getDevice().getAvailableAcceleratedMemory();
	}

	private void capture()
	{
		GraphicsContext3D  ctx = getGraphicsContext3D();
		// The raster components need all be set!
		int w = getWidth();
		int h = getHeight();
		Raster ras = new Raster(
		   new Point3f(-1.0f,-1.0f,-1.0f),
		   Raster.RASTER_COLOR,
		   0,0,
		   w,h,
		   new ImageComponent2D(ImageComponent.FORMAT_RGB,
		   new BufferedImage(w,h, BufferedImage.TYPE_INT_RGB)),
		   null);

		ctx.readRaster(ras);

		// Now strip out the image info
		BufferedImage img = ras.getImage().getImage();

		try {
			if (captureCallback!=null) {
				//  call back to command listener
				captureCallback.handleMessage(this, BoardView.MESSAGE_CAPTURE_IMAGE, img);
			}
			else {
				// or write to disk....
				if (captureFile==null)
					captureFile = new File("Capture"+frameCount+".jpg");

				ImgUtil.writeJpeg(img,captureFile,quality);
			}
		} catch ( IOException e ) {
			Application.error(e);
		} finally {
			captureNext = false;
			captureFile = null;
			captureCallback = null;
		}
	}

	public void paint(Graphics g)
	{
		boolean wasrunning = getView().isViewRunning();

		if (!wasrunning)
			startView(true);
		super.paint(g);

		if (!wasrunning)
			stopView(true);
	}
}
