/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.jo3d;

import de.jose.comm.msg.MessageListener;
import de.jose.chess.Clock;
import de.jose.chess.Constants;
import de.jose.util.map.MapUtil;
import de.jose.view.ClockPanel;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Clock3d
        extends Switch
        implements MessageListener, Constants
{
 	private static final Color3f white3f = new Color3f(Color.white);
    private static final Color3f black3f = new Color3f(Color.black);

    protected static final Color TRANSPARENT = new Color(0,0,0,0);

    /** miliseconds per minute  */
    public static final long MILLIS_PER_MINUTE   = 1000*60;

    /** the chess engine clock    */
    private Clock theClock;

    /** static transform group */
    private TransformGroup staticTg;
    /** ...and their transform */
    private Transform3D staticTf;

    /** our state: 0,WHITE,BLACK */
    private int current;
    /** time (int milliseconds)  */
    private long whiteTime, blackTime;

    /** the dials   */
    private Plane whiteDial;
    private Plane blackDial;

    /** the knobs   */
    private TransformGroup leftKnob, rightKnob;
    private Transform3D leftKnobTf, rightKnobTf;
    /** knob height */
    private float knobHeight;

    private class Hand {
        /** static transform group    */
        TransformGroup staticTg;
        /** dynamic transform group */
        TransformGroup dynamicTg;
        /** dynamic transform */
        Transform3D dynamicTf;
        /** the shape */
        Shape3D shape;

        public void rotate(double angle) {
            dynamicTf.setIdentity();
            dynamicTf.rotZ(angle);
            dynamicTg.setTransform(dynamicTf);
        }
    }

    /** the hands   */
    private Hand[] hand = new Hand[4];

    /** "click" sound   */
    private PointSound clickSound;


    public Clock3d(JoCanvas3D canvas, Clock engineClock, Jo3DFileReader fileReader, PickBehavior pick)
        throws Exception
    {
        super();
        setCapability(Switch.ALLOW_SWITCH_WRITE);
        setCapability(Switch.ALLOW_SWITCH_READ);

        staticTf = new Transform3D();
        staticTg = new TransformGroup(staticTf);
        addChild(staticTg);

        theClock = engineClock;
        theClock.addMessageListener(this);

        current = 0;
        whiteTime = blackTime = 0;

        setupScene(canvas,fileReader,pick);
        updateScene();
    }

    public void show(boolean on)
    {
        int newSwitch = on ? Switch.CHILD_ALL : Switch.CHILD_NONE;
        if (newSwitch != getWhichChild())
            setWhichChild(newSwitch);
    }

    /**
     * sent by theClock
     */
    public void handleMessage(Object who, int what, Object data)
    {
        if (who==theClock) {
            long newWhiteTime = theClock.getWhiteTime();
            long newBlackTime = theClock.getBlackTime();
            int newCurrent = theClock.getCurrent();

            boolean forceRedraw =
                    ((whiteTime/MILLIS_PER_MINUTE)!=(newWhiteTime/MILLIS_PER_MINUTE)) ||
                    ((blackTime/MILLIS_PER_MINUTE)!=(newBlackTime/MILLIS_PER_MINUTE)) ||
                    (current!=newCurrent);

           if ((newCurrent != 0) && (current != newCurrent) && (clickSound !=null)) {
              //  play "click" sound
              clickSound.setEnable(true);
           }

           whiteTime = newWhiteTime;
           blackTime = newBlackTime;
           current = newCurrent;

           updateScene();
           if (forceRedraw) {
                //  force redraw only once a minute
                //  ViewController.drawOnce(?)  TODO
            }
        }
    }

    private void setupScene(JoCanvas3D canvas, Jo3DFileReader reader, PickBehavior pick)
        throws Exception
    {
         //  load geometry from disk file
        Shape3D[] outer = {
            reader.getShape("FRAME_LEFT",0),
            reader.getShape("FRAME_TOP",0),
            reader.getShape("FRAME_RIGHT",0),
            reader.getShape("FRAME_BOTTOM",0),
        };
        Shape3D inner = reader.getShape("INNER",0);
        Shape3D knob_left = reader.getShape("KNOB_LEFT",0);
        Shape3D knob_right = reader.getShape("KNOB_RIGHT",0);

        leftKnob = new TransformGroup(leftKnobTf = new Transform3D());
        leftKnob.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        leftKnob.addChild(knob_left);

        rightKnob = new TransformGroup(rightKnobTf = new Transform3D());
        rightKnob.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        rightKnob.addChild(knob_right);

        for (int i=0; i<4; i++)
            outer[i].setCollidable(false);
        inner.setCollidable(false);
        knob_left.setCollidable(false);
        knob_right.setCollidable(false);

        HashMap params = reader.getFileParam();
        Point3f dial_center = (Point3f)params.get("DIAL_CENTER");
        float dial_radius = MapUtil.get(params,"DIAL_RADIUS",0f);
        knobHeight = MapUtil.get(params,"KNOB_HEIGHT",0f);
        Point3f label_center = (Point3f)params.get("LABEL_CENTER");
        float label_width = MapUtil.get(params,"LABEL_WIDTH",0f);
        float label_height = MapUtil.get(params,"LABEL_HEIGHT",0f);

        whiteDial = new Plane(
                dial_center.x, dial_center.y-dial_radius, dial_center.z-dial_radius,
				dial_center.x, dial_center.y+dial_radius, dial_center.z+dial_radius,
                1f,0f,0f,1f,
                Plane.YZ_PLANE);

        blackDial = new Plane(
                dial_center.x, -dial_center.y-dial_radius, dial_center.z-dial_radius,
				dial_center.x, -dial_center.y+dial_radius, dial_center.z+dial_radius,
                1f,0f,0f,1f,
                Plane.YZ_PLANE);

        float[] minutef = ClockPanel.getMinuteHandCoords(2*dial_radius,0f);
        float[] hourf = ClockPanel.getHourHandCoords(2*dial_radius,0f);

        Shape3D hourHand = new FlatPolygon(hourf,false);
        Shape3D minuteHand = new FlatPolygon(minutef,false);

        Appearance appHand = Util3D.createAppearance();
        appHand.getMaterial().setDiffuseColor(black3f);
	    appHand.getMaterial().setAmbientColor(white3f);
	    appHand.getMaterial().setSpecularColor(white3f);
	    appHand.getMaterial().setLightingEnable(true);
        Util3D.setCullFace(appHand,PolygonAttributes.CULL_FRONT);

        hand[0] = createHand(hourHand,dial_center.x+10f,dial_center.y,dial_center.z, appHand);
        hand[1] = createHand(minuteHand,dial_center.x+10f,dial_center.y,dial_center.z, appHand);
        hand[2] = createHand(hourHand,dial_center.x+10f,-dial_center.y,dial_center.z, appHand);
        hand[3] = createHand(minuteHand,dial_center.x+10f,-dial_center.y,dial_center.z, appHand);

        //  setup appearances
        Appearance appOuter = createAppearance("wood03.jpg",false);
        Appearance appInner = createAppearance("wood30.jpg",true);
        Appearance appKnob = createAppearance("metal01.jpg",false);
        Appearance appDial = createDialAppearance(512, -10f);

        for (int i=0; i<4; i++)
            outer[i].setAppearance(appOuter);
        inner.setAppearance(appInner);
        knob_left.setAppearance(appKnob);
        knob_right.setAppearance(appKnob);
        whiteDial.setAppearance(appDial);
        blackDial.setAppearance(appDial);

        //  setup group
        whiteDial.setCollidable(false);
        blackDial.setCollidable(false);

        for (int i=0; i<4; i++)
            staticTg.addChild(outer[i]);
        staticTg.addChild(inner);
        staticTg.addChild(leftKnob);
        staticTg.addChild(rightKnob);
        staticTg.addChild(whiteDial);
        staticTg.addChild(blackDial);
        for (int i=0; i<4; i++) {
            hand[i].shape.setCollidable(false);
            staticTg.addChild(hand[i].staticTg);
        }

        for (int i=0; i<4; i++)
            pick.addOrbitHandle(outer[i]);
        pick.addZoomHandle(inner);

        //  setup sound
/*        MediaContainer sound = new MediaContainer("file://"+de.jose.Sound.getPath("Hit.wav"));
        clickSound = new PointSound();
        clickSound.setPosition(dial_center.x,0,dial_center.z);
        clickSound.setSoundData(sound);
        clickSound.setSchedulingBounds(new BoundingSphere(new Point3d(0,0,0), Double.MAX_VALUE));
        clickSound.setCapability(PointSound.ALLOW_ENABLE_WRITE);
        clickSound.setCapability(PointSound.ALLOW_RELEASE_WRITE);
        clickSound.setInitialGain(3.0f);
        clickSound.setLoop(0);
        clickSound.setContinuousEnable(false);
        staticTg.addChild(clickSound);
        canvas.getViewController().addSound(clickSound);
 */   }

    private void updateScene()
    {
        //  set knobs
        switch (current) {
        case WHITE:
                Util3D.setTranslation(leftKnobTf, 0,0,0);
                Util3D.setTranslation(rightKnobTf, 0,0,-knobHeight);
                break;
        case BLACK:
                Util3D.setTranslation(leftKnobTf, 0,0,-knobHeight);
                Util3D.setTranslation(rightKnobTf, 0,0,0);
                break;
        default:    //  neutral
                Util3D.setTranslation(leftKnobTf, 0,0,-knobHeight/2);
                Util3D.setTranslation(rightKnobTf, 0,0,-knobHeight/2);
                break;
        }
        leftKnob.setTransform(leftKnobTf);
        rightKnob.setTransform(rightKnobTf);

        //  set hands
        hand[0].rotate( + Math.PI/2 + (whiteTime%HOUR12)*2*Math.PI/HOUR12 );    //  white hours
        hand[1].rotate( - Math.PI/2 + (whiteTime%HOUR)*2*Math.PI/HOUR );        //  white minutes
        hand[2].rotate( + Math.PI/2 + (blackTime%HOUR12)*2*Math.PI/HOUR12 );    //  black hours
        hand[3].rotate( - Math.PI/2 + (blackTime%HOUR)*2*Math.PI/HOUR );        //  black minutes
    }

    private void playSound()
    {

    }

    private Appearance createAppearance(String texture, boolean frontal)
    {
        Appearance app = new Appearance();
        Material mat = new Material();
        mat.setDiffuseColor(white3f);
	    mat.setAmbientColor(white3f);
	    mat.setSpecularColor(white3f);
	    mat.setLightingEnable(true);

        Texture2D tex = TextureCache3D.getTexture(texture);
        app.setTexture(tex);

        double scale = 1000.0;
        TextureAttributes attr = new TextureAttributes();
        Transform3D tf = new Transform3D();
        Util3D.scale(tf, 0.005,0.010,1.0);
        attr.setTextureTransform(tf);

        TexCoordGeneration texgen = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
                                            TexCoordGeneration.TEXTURE_COORDINATE_2);
        if (!frontal) {
            Vector4f splane = new Vector4f(+1.0f, 0.0f,-1.0f, 0.0f);    //  tilt the projection plane a bit
            texgen.setPlaneS(splane);
        }
        app.setMaterial(mat);
        app.setTextureAttributes(attr);
        app.setTexCoordGeneration(texgen);
        return app;
    }

    private Appearance createDialAppearance(int size, float zoffset)
    {
        TransparencyAttributes transp = new TransparencyAttributes();
		transp.setTransparencyMode(TransparencyAttributes.FASTEST);
		transp.setTransparency(0.0f);		//	opaque
		//	we need flexible transparency based on the alpha component of the texture

		Appearance appearance = new Appearance();
		appearance.setTransparencyAttributes(transp);

		PolygonAttributes polyattr = new PolygonAttributes();
		polyattr.setPolygonOffset(zoffset);
		appearance.setPolygonAttributes(polyattr);

		Material m = new Material();
	    m.setDiffuseColor(white3f);
		m.setAmbientColor(white3f);
		m.setSpecularColor(white3f);
		m.setLightingEnable(true);
		appearance.setMaterial(m);

        Texture tex = createDialTexture(size);
        appearance.setTexture(tex);

		return appearance;
    }

    private Texture createDialTexture(int size)
    {
        BufferedImage bimg = new BufferedImage(size,size, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = (Graphics2D)bimg.getGraphics();
        //  fill with transparent background
        g2.setColor(TRANSPARENT);
        g2.fillRect(0,0, size,size);
        //  draw dial
        Rectangle box = new Rectangle(4,4,size-8,size-8);
        ClockPanel.drawAnalogBackground(g2, box, true);

        //  turn it into a texture
        ImageComponent2D i2d = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, bimg, false, true);
        Texture2D tex = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA, size,size);
        tex.setImage(0,i2d);
        return tex;
    }

    private Hand createHand(Shape3D shape, float cx, float cy, float cz, Appearance app)
    {
        Hand hnd = new Hand();

        hnd.shape = (Shape3D)shape.cloneTree(false);
        hnd.shape.setAppearance(app);

        //  dynamic Transform
        hnd.dynamicTf = new Transform3D();
        hnd.dynamicTg = new TransformGroup(hnd.dynamicTf);
        hnd.dynamicTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        hnd.dynamicTg.addChild(hnd.shape);

        //  "static" Transform
        Transform3D staticTf = new Transform3D();
        Util3D.translate(staticTf,cx,cy,cz);
        Util3D.rotY(staticTf,Math.PI/2);
        hnd.staticTg = new TransformGroup(staticTf);

        hnd.staticTg.addChild(hnd.dynamicTg);
        return hnd;
    }
}
