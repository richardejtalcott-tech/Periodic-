package com.periodic.app;

import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.OverScroller;
import java.util.*;

public class PeriodicTableView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;
    private final OverScroller scroller;
    private float scale=1f, panX=0,panY=0, tiltX=-8,tiltY=0,lastAngle=0,phase=0;
    private boolean rotating=false;
    private final HashMap<Integer,RectF> hit=new HashMap<>();
    private int selected=-1;

    public PeriodicTableView(Context c){
        super(c);
        p.setTypeface(Typeface.create("sans",Typeface.BOLD));
        scroller=new OverScroller(c);
        scaleDetector=new ScaleGestureDetector(c,new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            public boolean onScale(ScaleGestureDetector d){scale=Math.max(.5f,Math.min(4.2f,scale*d.getScaleFactor()));invalidate();return true;}
        });
        gestureDetector=new GestureDetector(c,new GestureDetector.SimpleOnGestureListener(){
            public boolean onDown(MotionEvent e){scroller.forceFinished(true);return true;}
            public boolean onScroll(MotionEvent a,MotionEvent b,float dx,float dy){if(b.getPointerCount()==1){panX-=dx;panY-=dy;}invalidate();return true;}
            public boolean onSingleTapUp(MotionEvent e){return tap(e.getX(),e.getY());}
            public boolean onDoubleTap(MotionEvent e){scale=1;panX=panY=tiltY=0;tiltX=-8;selected=-1;invalidate();return true;}
            public boolean onFling(MotionEvent e1,MotionEvent e2,float vx,float vy){scroller.fling((int)panX,(int)panY,(int)vx,(int)vy,-1800,1800,-1000,1000);postInvalidateOnAnimation();return true;}
        });
    }

    @Override public void computeScroll(){if(scroller.computeScrollOffset()){panX=scroller.getCurrX();panY=scroller.getCurrY();postInvalidateOnAnimation();}}
    @Override public boolean onTouchEvent(MotionEvent e){
        scaleDetector.onTouchEvent(e); gestureDetector.onTouchEvent(e);
        if(e.getPointerCount()==2){float a=(float)Math.toDegrees(Math.atan2(e.getY(1)-e.getY(0),e.getX(1)-e.getX(0)));if(rotating){float d=a-lastAngle;tiltY+=d;tiltX=Math.max(-22,Math.min(18,tiltX+(e.getY(0)+e.getY(1)-getHeight())*.0004f));invalidate();}lastAngle=a;rotating=true;}else rotating=false;
        return true;
    }

    private boolean tap(float x,float y){
        Matrix inv=new Matrix();buildMatrix().invert(inv);float[] pt={x,y};inv.mapPoints(pt);
        for(Map.Entry<Integer,RectF> en:hit.entrySet())if(en.getValue().contains(pt[0],pt[1])){
            selected=en.getKey();invalidate(); SoundFx.select();
            Intent i=new Intent(getContext(),ElementDetailActivity.class);i.putExtra("element",en.getKey());getContext().startActivity(i);return true;
        }
        return false;
    }

    private Matrix buildMatrix(){
        float w=getWidth(),h=getHeight();float base=Math.min(w/1530f,h/760f);
        Matrix m=new Matrix();m.postTranslate(-765,-380);m.postScale(base*scale,base*scale);
        m.postSkew((float)Math.sin(Math.toRadians(tiltY))*.095f,(float)Math.sin(Math.toRadians(tiltX))*.07f);
        m.postTranslate(w/2+panX,h/2+panY);return m;
    }

    @Override protected void onDraw(Canvas c){
        c.drawColor(Color.rgb(1,4,12));drawSpace(c);c.save();c.concat(buildMatrix());drawTable(c);c.restore();drawHud(c);phase+=.45f;postInvalidateOnAnimation();
    }

    private void drawSpace(Canvas c){
        float w=getWidth(),h=getHeight();p.setStyle(Paint.Style.FILL); float drift=(float)Math.sin(Math.toRadians(phase*.32f))*18f;
        // star cluster / nebula
        p.setShader(new RadialGradient(w*.22f+drift,h*.28f,w*.34f,new int[]{Color.argb(80,73,35,129),Color.argb(40,31,78,132),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
        p.setShader(new RadialGradient(w*.31f-drift*.45f,h*.39f,w*.24f,new int[]{Color.argb(42,212,97,160),Color.argb(18,70,120,190),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
        p.setShader(new RadialGradient(w*.64f-drift*.25f,h*.72f,w*.22f,new int[]{Color.argb(34,20,95,125),Color.argb(18,80,35,115),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
        for(int i=0;i<220;i++){float x=(i*157%1009)/1009f*w,y=(i*271%1013)/1013f*h;float tw=.55f+.45f*(float)Math.sin(Math.toRadians(phase*2+i*23));p.setColor(Color.argb((int)(45+150*tw),160+(i%3)*25,180+(i%2)*35,255));c.drawCircle(x,y,.7f+(i%4)*.42f,p);}
        drawSaturn(c,w*.83f+drift*.2f,h*.22f,Math.min(w,h)*.105f);
        // sparse dust field for depth
        for(int i=0;i<32;i++){float x=(i*313%1019)/1019f*w+drift*(i%3-1)*.25f,y=(i*421%1021)/1021f*h;p.setColor(Color.argb(20+(i%5)*8,120,170,255));c.drawCircle(x,y,3+(i%4)*2,p);}
    }

    private void drawSaturn(Canvas c,float x,float y,float r){
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.12f);p.setColor(Color.argb(80,199,180,130));c.save();c.rotate(-16,x,y);c.drawOval(x-r*1.65f,y-r*.42f,x+r*1.65f,y+r*.42f,p);c.restore();
        p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(x-r,y-r,x+r,y+r,new int[]{Color.argb(125,227,202,145),Color.argb(120,154,111,72),Color.argb(110,220,184,116)},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,r,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.03f);p.setColor(Color.argb(65,255,240,195));for(int i=-2;i<=2;i++)c.drawArc(x-r*.9f,y-r*.45f+i*r*.14f,x+r*.9f,y+r*.45f+i*r*.14f,188,164,false,p);
    }

    private void drawTable(Canvas c){
        hit.clear();float cw=78,ch=79,ox=63,oy=30; p.setTextAlign(Paint.Align.CENTER);
        p.setShadowLayer(34,18,24,Color.BLACK);p.setColor(Color.rgb(13,16,24));p.setStyle(Paint.Style.FILL);c.drawRoundRect(18,-12,1512,752,34,34,p);p.clearShadowLayer();
        p.setColor(Color.rgb(73,66,48));c.drawRoundRect(24,-6,1506,746,30,30,p);
        p.setColor(Color.rgb(25,28,38));c.drawRoundRect(28,-2,1502,742,28,28,p);
        p.setShader(new LinearGradient(0,0,1495,735,new int[]{Color.rgb(42,48,59),Color.rgb(24,28,38),Color.rgb(38,42,49)},null,Shader.TileMode.CLAMP));c.drawRoundRect(35,5,1495,735,24,24,p);p.setShader(null);
        // faint underglow
        p.setShader(new RadialGradient(760,715,620,new int[]{Color.argb(52,70,135,210),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawRect(100,540,1420,760,p);p.setShader(null);
        for(Element e:ElementData.ALL){int row=e.period,col=e.group;float x=ox+(col-1)*cw,y=oy+(row-1)*ch;RectF r=new RectF(x,y,x+72,y+71);hit.put(e.number,r);drawTile(c,e,r,e.number==selected);}
        p.setColor(Color.rgb(226,201,121));p.setTextSize(22);p.setTextAlign(Paint.Align.LEFT);c.drawText("PERIODIC v2  •  INTERACTIVE ELEMENT TABLE",63,718,p);
        p.setTextAlign(Paint.Align.RIGHT);p.setTextSize(16);c.drawText("Pinch to zoom  •  Drag to move  •  Twist to rotate  •  Double-tap to reset",1472,718,p);
    }

    private void drawTile(Canvas c,Element e,RectF r,boolean active){
        float depth=active?10f:7f;float lift=active?-5f:0f;RectF top=new RectF(r.left,r.top+lift,r.right,r.bottom+lift);
        int base=Visuals.categoryColor(e.category), dark=Visuals.darken(base,.38f), edge=Visuals.darken(base,.58f), light=Visuals.lighten(base,.25f);
        Path side=new Path();side.moveTo(top.right,top.top+5);side.lineTo(top.right+depth,top.top+depth);side.lineTo(top.right+depth,top.bottom+depth);side.lineTo(top.right,top.bottom);side.close();p.setColor(dark);p.setStyle(Paint.Style.FILL);c.drawPath(side,p);
        Path bottom=new Path();bottom.moveTo(top.left+5,top.bottom);bottom.lineTo(top.right,top.bottom);bottom.lineTo(top.right+depth,top.bottom+depth);bottom.lineTo(top.left+depth,top.bottom+depth);bottom.close();p.setColor(edge);c.drawPath(bottom,p);
        p.setShadowLayer(active?18:7,active?0:3,active?5:5,active?Color.argb(210,125,190,255):Color.argb(120,0,0,0));
        p.setShader(new LinearGradient(top.left,top.top,top.right,top.bottom,new int[]{light,base,Visuals.darken(base,.16f)},null,Shader.TileMode.CLAMP));c.drawRoundRect(top,7,7,p);p.setShader(null);p.clearShadowLayer();
        // matte moon reflection
        p.setShader(new LinearGradient(top.left,top.top,top.right,top.top+18,new int[]{Color.argb(105,255,249,221),Color.argb(24,255,255,255),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawRoundRect(new RectF(top.left+3,top.top+3,top.right-3,top.top+20),6,6,p);p.setShader(null);
        // fine brushed-metal grain and a slowly moving moonlight band
        p.setStrokeWidth(.65f);for(int g=0;g<9;g++){float yy=top.top+8+g*6.1f;p.setColor(Color.argb(12+(g%3)*5,255,255,255));c.drawLine(top.left+5,yy,top.right-5,yy,p);}
        float sweep=(phase*1.15f+e.number*17)%160f-45f;p.setShader(new LinearGradient(top.left+sweep,top.top,top.left+sweep+34,top.bottom,new int[]{Color.TRANSPARENT,Color.argb(42,255,255,230),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawRoundRect(top,7,7,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.2f);p.setColor(edge);c.drawRoundRect(top,7,7,p);p.setStyle(Paint.Style.FILL);
        if(e.number>92){p.setColor(Color.rgb(218,88,145));c.drawCircle(top.right-8,top.top+8,3.5f,p);}
        int text=Color.rgb(18,20,24);p.setShadowLayer(2.2f,0,-1,Color.argb(185,255,255,255));p.setColor(text);p.setTextAlign(Paint.Align.LEFT);p.setTextSize(11);c.drawText(String.valueOf(e.number),top.left+7,top.top+14,p);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(27);c.drawText(e.symbol,top.centerX(),top.top+40,p);p.setTextSize(8.8f);c.drawText(e.name,top.centerX(),top.top+53,p);p.setTextSize(8.2f);c.drawText(e.mass,top.centerX(),top.top+64,p);p.clearShadowLayer();
    }

    private void drawHud(Canvas c){
        p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(155,0,0,0));c.drawRoundRect(18,16,365,70,18,18,p);p.setTextAlign(Paint.Align.LEFT);p.setColor(Color.WHITE);p.setTextSize(23);c.drawText("Tap any element to explore it",36,49,p);
        if(selected>0){Element se=ElementData.byNumber(selected);if(se!=null){p.setTextAlign(Paint.Align.RIGHT);p.setColor(Color.argb(205,255,255,255));p.setTextSize(17);c.drawText(se.name+"  •  "+se.category,getWidth()-28,49,p);}}
        // category legend
        String[] cats={"Alkali metal","Transition metal","Metalloid","Nonmetal","Halogen","Noble gas","Lanthanide","Actinide"};
        float x=20,y=getHeight()-38;for(String cat:cats){p.setColor(Visuals.categoryColor(cat));c.drawCircle(x+7,y-5,7,p);p.setColor(Color.argb(220,230,235,245));p.setTextSize(12);c.drawText(cat,x+19,y,p);x+=p.measureText(cat)+45;if(x>getWidth()-170)break;}
    }
}
