package com.periodic.app;

import android.content.Context;
import android.graphics.*;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.*;
import java.util.*;

public final class PeriodicTableView extends View {
    public interface Listener { void onElement(int atomicNumber); }
    private final Listener listener;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    private final ScaleGestureDetector scaler;
    private final GestureDetector gestures;
    private final HashMap<Integer,RectF> hit=new HashMap<>();
    private float zoom=1.12f, panX=0f, yaw=0f, phase=0f;
    private int pressed=-1;

    public PeriodicTableView(Context c, Listener listener){
        super(c); this.listener=listener; setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        p.setTypeface(Typeface.create("sans",Typeface.BOLD));
        scaler=new ScaleGestureDetector(c,new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override public boolean onScale(ScaleGestureDetector d){zoom=Math.max(.82f,Math.min(2.7f,zoom*d.getScaleFactor()));invalidate();return true;}
        });
        gestures=new GestureDetector(c,new GestureDetector.SimpleOnGestureListener(){
            @Override public boolean onDown(MotionEvent e){return true;}
            @Override public boolean onScroll(MotionEvent a,MotionEvent b,float dx,float dy){
                panX=Math.max(-getWidth()*.32f,Math.min(getWidth()*.32f,panX-dx));
                yaw=Math.max(-13f,Math.min(13f,panX/Math.max(1f,getWidth())*30f)); invalidate(); return true;
            }
            @Override public boolean onDoubleTap(MotionEvent e){zoom=1.12f;panX=0;yaw=0;pressed=-1;invalidate();return true;}
            @Override public boolean onSingleTapUp(MotionEvent e){return tap(e.getX(),e.getY());}
        });
    }
    @Override public boolean onTouchEvent(MotionEvent e){scaler.onTouchEvent(e);gestures.onTouchEvent(e);return true;}
    private Matrix matrix(){
        float w=getWidth(),h=getHeight(); float base=Math.min(w/1520f,h/790f);
        Matrix m=new Matrix();m.postTranslate(-760,-395);m.postScale(base*zoom,base*zoom);
        // Side-to-side movement changes perspective without allowing roll, pitch, or underside views.
        float perspective=(float)Math.sin(Math.toRadians(yaw));
        m.postSkew(perspective*.055f,0f);m.postScale(1f-Math.abs(perspective)*.045f,1f,760,395);
        m.postTranslate(w/2+panX*.24f,h/2+10);return m;
    }
    private boolean tap(float x,float y){
        Matrix inv=new Matrix(); if(!matrix().invert(inv))return false; float[] q={x,y};inv.mapPoints(q);
        for(Map.Entry<Integer,RectF> en:hit.entrySet()) if(en.getValue().contains(q[0],q[1])){
            pressed=en.getKey();invalidate();performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            postDelayed(()->listener.onElement(en.getKey()),110);return true;
        }return false;
    }
    @Override protected void onDraw(Canvas c){super.onDraw(c); c.save();c.concat(matrix());drawTable(c);c.restore();drawLegend(c);phase=(phase+.55f)%360f;postInvalidateOnAnimation();}
    private void drawTable(Canvas c){
        hit.clear();float cw=79,ch=78,ox=50,oy=42;
        p.setStyle(Paint.Style.FILL);p.setShadowLayer(30,0,18,Color.argb(190,0,0,0));p.setColor(Color.argb(225,8,13,18));c.drawRoundRect(15,6,1505,760,30,30,p);p.clearShadowLayer();
        p.setShader(new LinearGradient(0,0,1520,760,new int[]{Color.rgb(28,39,45),Color.rgb(12,20,27),Color.rgb(32,42,47)},null,Shader.TileMode.CLAMP));c.drawRoundRect(22,13,1498,753,26,26,p);p.setShader(null);
        // Realistic laboratory display plane and restrained under-light.
        p.setShader(new RadialGradient(760,735,610,new int[]{Color.argb(85,66,154,183),Color.argb(20,32,87,104),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawRect(80,560,1440,770,p);p.setShader(null);
        for(Element e:ElementData.ALL){float x=ox+(e.group-1)*cw,y=oy+(e.period-1)*ch;RectF r=new RectF(x,y,x+72,y+70);hit.put(e.number,r);drawTile(c,e,r,e.number==pressed);}
        p.setTextAlign(Paint.Align.LEFT);p.setColor(Color.rgb(190,211,218));p.setTextSize(15);c.drawText("Lanthanides and actinides are displayed in their conventional detached rows.",54,735,p);
    }
    private void drawTile(Canvas c,Element e,RectF r,boolean active){
        float lift=active?-5:0,depth=active?10:7;RectF top=new RectF(r.left,r.top+lift,r.right,r.bottom+lift);
        int base=Visuals.categoryColor(e.category), side=Visuals.darken(base,.42f), edge=Visuals.darken(base,.62f), hi=Visuals.lighten(base,.20f);
        Path right=new Path();right.moveTo(top.right,top.top+5);right.lineTo(top.right+depth,top.top+depth);right.lineTo(top.right+depth,top.bottom+depth);right.lineTo(top.right,top.bottom);right.close();p.setColor(side);c.drawPath(right,p);
        Path low=new Path();low.moveTo(top.left+5,top.bottom);low.lineTo(top.right,top.bottom);low.lineTo(top.right+depth,top.bottom+depth);low.lineTo(top.left+depth,top.bottom+depth);low.close();p.setColor(edge);c.drawPath(low,p);
        p.setShadowLayer(active?18:6,0,active?6:4,active?Color.argb(210,120,205,235):Color.argb(130,0,0,0));
        p.setShader(new LinearGradient(top.left,top.top,top.right,top.bottom,new int[]{hi,base,Visuals.darken(base,.13f)},null,Shader.TileMode.CLAMP));c.drawRoundRect(top,6,6,p);p.setShader(null);p.clearShadowLayer();
        // Moonlight reflection and subtle machining marks.
        float sweep=((phase*1.15f+e.number*13)%125)-30;
        p.setShader(new LinearGradient(top.left+sweep,top.top,top.left+sweep+27,top.bottom,new int[]{Color.TRANSPARENT,Color.argb(48,245,252,255),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawRoundRect(top,6,6,p);p.setShader(null);
        p.setStrokeWidth(.55f);for(int i=0;i<7;i++){p.setColor(Color.argb(11+i%2*5,255,255,255));c.drawLine(top.left+5,top.top+8+i*8,top.right-5,top.top+8+i*8,p);}
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);p.setColor(edge);c.drawRoundRect(top,6,6,p);p.setStyle(Paint.Style.FILL);
        // Textbook-style layout: Z, symbol, name, mass.
        int ink=Color.rgb(238,244,244);p.setShadowLayer(2,0,2,Color.argb(190,0,0,0));p.setColor(ink);
        p.setTextAlign(Paint.Align.LEFT);p.setTextSize(10.5f);c.drawText(String.valueOf(e.number),top.left+6,top.top+13,p);
        p.setTextAlign(Paint.Align.CENTER);p.setTextSize(27);c.drawText(e.symbol,top.centerX(),top.top+38,p);
        p.setTextSize(8.4f);c.drawText(e.name,top.centerX(),top.top+51,p);
        p.setTextSize(8.0f);p.setColor(Color.rgb(218,229,229));c.drawText(e.mass,top.centerX(),top.top+63,p);p.clearShadowLayer();
    }
    private void drawLegend(Canvas c){
        String[] cats={"Alkali metal","Alkaline earth metal","Transition metal","Post-transition metal","Metalloid","Nonmetal","Halogen","Noble gas","Lanthanide","Actinide"};
        p.setColor(Color.argb(150,4,9,14));c.drawRoundRect(18,getHeight()-55,getWidth()-18,getHeight()-12,17,17,p);
        float x=34,y=getHeight()-31;p.setTextSize(10.5f);p.setTypeface(Typeface.create("sans",Typeface.NORMAL));
        for(String cat:cats){p.setColor(Visuals.categoryColor(cat));c.drawCircle(x,y-4,5.5f,p);p.setColor(Color.rgb(211,222,226));c.drawText(cat,x+10,y,p);x+=p.measureText(cat)+31;if(x>getWidth()-120)break;}
        p.setTypeface(Typeface.create("sans",Typeface.BOLD));
    }
}
