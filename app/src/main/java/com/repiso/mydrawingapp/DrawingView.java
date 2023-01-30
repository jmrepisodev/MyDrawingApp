package com.repiso.mydrawingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Vista personalizada en la que se realizará el dibujo. Hereda de View.
 */
public class DrawingView extends View {

    //Trazo de dibujo
    private Path drawPath;
    //Pinceles
    private Paint drawPaint, canvasPaint;
    //Color inicial, color por defecto
    private int paintColor = 0xFF660000, paintAlpha = 255;
    //Lienzo o superficie de dibujo
    private Canvas drawCanvas;
    //La clase Canvas se almacena dentro de un mapa de bits,
    // con unas dimensiones determinadas y en una determinada posición de la pantalla
    private Bitmap canvasBitmap;
    //Tamaños de brocha
    private float brushSize, lastBrushSize;
    //Bandera borrador. Inicialmente el usuario está dibujando, no borrando
    private boolean erase=false;

    /**
     * Método constructor
     * @param context
     * @param attrs
     */
    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    /**
     * Establece la configuración del área de dibujo para la interacción con el usuario
     */
    private void setupDrawing(){

        //prepare for drawing and setup paint stroke properties
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;

        //Instanciamos el trazo y el pincel
        drawPath = new Path();
        drawPaint = new Paint();
        //Configuramos las propiedades iniciales del pincel
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        //Instanciamos el pincel del lienzo
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    /**
     * Tamaño asignado a la Vista
     * @param w Current width of this view.
     * @param h Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //Creamos un objeto Bitmap: dimensiones, formato de color
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //Definimos el lienzo o superficie de dibujo, a partir de un objeto Bitmap
        drawCanvas = new Canvas(canvasBitmap);

        //*Nota: Lo recomendable es obtener el Canvas a partir de View.onDraw() o SurfaceHolder
    }

    /**
     * Método principal: Dibuja la vista sobre el Canvas
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        //Dibuja una imagen o mapa de bits
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        //Asignamos el trazo y el pincel
        canvas.drawPath(drawPath, drawPaint);
    }

    /**
     * Registra los movimientos táctiles del usuario en la pantalla
     * @param event The motion event.
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        //respond to down, move and up events
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawPath.lineTo(touchX, touchY);
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }
        //Redibuja la vista
        invalidate();
        return true;

    }

    /**
     * Actualiza el color
     * @param newColor
     */
    public void setColor(String newColor){
        invalidate();
        //check whether color value or pattern name
        if(newColor.startsWith("#")){
            paintColor = Color.parseColor(newColor);
            drawPaint.setColor(paintColor);
            drawPaint.setShader(null);
        }
        else{
            //pattern
            int patternID = getResources().getIdentifier(
                    newColor, "drawable", "com.repiso.mydrawingapp");
            //decode
            Bitmap patternBMP = BitmapFactory.decodeResource(getResources(), patternID);
            //create shader
            BitmapShader patternBMPshader = new BitmapShader(patternBMP,
                    Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            //color and shader
            drawPaint.setColor(0xFFFFFFFF);
            drawPaint.setShader(patternBMPshader);
        }
    }

    //set brush size
    public void setBrushSize(float newSize){
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    //get and set last brush size
    public void setLastBrushSize(float lastSize){
        lastBrushSize=lastSize;
    }
    public float getLastBrushSize(){
        return lastBrushSize;
    }

    //set erase true or false
    public void setErase(boolean isErase){
        erase=isErase;
        //modifica el objeto Paint para borrar o para dibujar:
        if(erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else drawPaint.setXfermode(null);
    }

    /**
     * Comenzar nuevo dibujo
     */
    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    //return current alpha
    public int getPaintAlpha(){
        return Math.round((float)paintAlpha/255*100);
    }

    //set alpha
    public void setPaintAlpha(int newAlpha){
        paintAlpha=Math.round((float)newAlpha/100*255);
        drawPaint.setColor(paintColor);
        drawPaint.setAlpha(paintAlpha);
    }

    public void setPattern(String newPattern){
        invalidate();
        int patternID = getResources().getIdentifier(newPattern, "drawable", "com.repiso.mydrawingapp");
        Bitmap patternBMP = BitmapFactory.decodeResource(getResources(), patternID);
        BitmapShader patternBMPshader = new BitmapShader(patternBMP,
                Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        drawPaint.setColor(0xFFFFFFFF);
        drawPaint.setShader(patternBMPshader);
    }
}
