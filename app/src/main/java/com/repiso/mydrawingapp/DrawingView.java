package com.repiso.mydrawingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {

    //trazo del dibujo
    private Path drawPath;
    //dibujo y pincel del lienzo
    private Paint drawPaint, canvasPaint;
    //color inicial
    private int paintColor = 0xFF660000;
    //lienzo
    private Canvas drawCanvas;
    //mapa de bits del lienzo
    private Bitmap canvasBitmap;
    //talla de los pinceles
    private float brushSize, lastBrushSize;
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

    private void setupDrawing(){

        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;
        //Trazo y pincel
        drawPath = new Path();
        drawPaint = new Paint();
        //color inicial
        drawPaint.setColor(paintColor);

        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        drawPaint.setStrokeWidth(brushSize);

        canvasPaint = new Paint(Paint.DITHER_FLAG);


    }

    /**
     * Estable el tamaño de la vista
     * @param w Current width of this view.
     * @param h Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        this.drawCanvas = new Canvas(canvasBitmap);
    }

    /**
     * Permite que la clase funcione como una vista de dibujo
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    /**
     * Detecta los movimientos táctiles sobre la pantalla
     * @param event The motion event.
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }

        //Fuerza la ejecución del método
        invalidate();
        return true;
    }


    /**
     * Establece el color
     * @param newColor
     */
    public void setColor(String newColor){
        invalidate();
        this.paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    /**
     * Establece el tamaño del pincel
     * @param newSize
     */
    public void setBrushSize(float newSize){
     float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        this.brushSize=pixelAmount;
        this.drawPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize){
        this.lastBrushSize=lastSize;
    }
    public float getLastBrushSize(){
        return lastBrushSize;
    }


    /**
     * Establece el borrador
     * @param isErase
     */
    public void setErase(boolean isErase){
        this.erase=isErase;
        if(erase) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        else{
            drawPaint.setXfermode(null);}
    }

    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

}
