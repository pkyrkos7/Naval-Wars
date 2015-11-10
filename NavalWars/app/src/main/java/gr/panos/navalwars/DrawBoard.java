package gr.panos.navalwars;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.TextView;
import java.lang.Math;


import static java.lang.Math.*;

public class DrawBoard extends View{



    private int viewWidth;
    private int viewHeight;


    private Paint txtpaint,gridline_paint,seltile_paint,shot_paint;
    private int board_sizeX,board_sizeY;
    private TextView tv1,tv2;
    private int TILE_SIZE = 64;
    private boolean grid_enable = true;
    GameBoard board;


    private Bitmap bt_background,bt_gui;//= BitmapFactory.decodeResource(getResources(), R.drawable.grid_square64x64);
    private Bitmap shot,shot_target;
    private Bitmap ship_small;
    private Bitmap ship_medium;
    private Bitmap ship_big;



    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 3f;


    private static final int INVALID_POINTER_ID = -1;

    private float mPosX;
    private float mPosY;

    private int sel_x,sel_y;



    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = INVALID_POINTER_ID;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;


    public DrawBoard(Context context,GameBoard board , TextView tv1,TextView tv2) {
        super(context);
        this.tv1 = tv1;
        this.tv2 = tv2;
        setFocusable(true);
        this.board = board;

        board_sizeX = board.getDimX();
        board_sizeY = board.getDimY();


        bt_gui = BitmapFactory.decodeResource(getResources(),GameBoard.defaultGuiTile); //background tile bitmap
        bt_background = BitmapFactory.decodeResource(getResources(),GameBoard.defaultBackgroundTile); //background tile bitmap


        shot = BitmapFactory.decodeResource(getResources(), GameBoard.defaultShotTile);
        shot_target = BitmapFactory.decodeResource(getResources(), GameBoard.defaultShotTargetTile);

        ship_small = BitmapFactory.decodeResource(getResources(), GameShip.SpriteSmallBoat);
        ship_medium = BitmapFactory.decodeResource(getResources(), GameShip.SpriteScoutShip);
        ship_big = BitmapFactory.decodeResource(getResources(), GameShip.SpriteBigShip);

        //resize the background tile to match the specified one ( in case the image doesn't have the same size)
        bt_gui = getResizedBitmap(bt_gui, TILE_SIZE, TILE_SIZE);
        bt_background = getResizedBitmap(bt_background,TILE_SIZE,TILE_SIZE);
        shot = getResizedBitmap(shot,TILE_SIZE,TILE_SIZE);
        shot_target = getResizedBitmap(shot_target,TILE_SIZE,TILE_SIZE);
        ship_small  = getResizedBitmap(ship_small,TILE_SIZE,TILE_SIZE);
        ship_medium = getResizedBitmap(ship_medium,TILE_SIZE,2*TILE_SIZE);
        ship_big  = getResizedBitmap(ship_big,TILE_SIZE,3*TILE_SIZE);


        initPaints();


        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());


    }




    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {

                final float x = ev.getX();
                final float y = ev.getY();

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
       // String x = String.valueOf(getLastTouchX()) +" , "+ String.valueOf(getLastTouchY());



        //actual board coordinates start from x = <value>+TILE_SIZE  , y = <value>+TILE_SIZE
        int temp_x = Math.round( (ev.getX()-mPosX));
        int temp_y = Math.round( (ev.getY()-mPosY));

        if ( (! (temp_x<TILE_SIZE || temp_y<TILE_SIZE))
           && !( (temp_x>(board_sizeX+1)*TILE_SIZE || temp_y>(board_sizeY+1)*TILE_SIZE))
                ) //if user touched inside the board

        {

            sel_x = Math.round(ev.getX()-mPosX);
            sel_y = Math.round(ev.getY()-mPosY);
            sel_x -= TILE_SIZE;
            sel_y -= TILE_SIZE;
           // tv1.setText(sel_x + "," + sel_y);

            sel_x = sel_x / TILE_SIZE;
            sel_y = sel_y / TILE_SIZE;
            tv1.setText(sel_y + "," + sel_x);


            //SHIP NAME
            if (board.getObject(sel_x,sel_y,board.getBoard(board.getBoardStatus()))!=null )
            {
                Log.d("POS:", "Ship found at" + sel_x + "," + sel_y);
                tv2.setText(board.getObject(sel_x, sel_y,board.getBoard(board.getBoardStatus())).getName() + " " + sel_x + "," + sel_y);
            }
            else
                tv2.setText("None");
            //====================================
        }
        else { //this means user touched something outside the drawn board
            tv1.setText("UI");
            //tv2.setText("UI");
        }

        invalidate();
        return true;
    }






    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(MIN_ZOOM, Math.min(mScaleFactor, MAX_ZOOM));
            invalidate();
            return true;
        }
    }


    //The below code draws the game
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        Matrix matrix = new Matrix();

        canvas.save();
        //Log.d("DEBUG", "X: " + mPosX + " Y: " + mPosY);

        //Check if "camera" is above or below the room limits
        if (mPosX > 0) //768 = 64*12
            mPosX = 0;
        else if ( mPosX < -abs((TILE_SIZE*12)-viewWidth) )
                mPosX = -abs((TILE_SIZE*12)-viewWidth);
        if (mPosY > 0)
            mPosY = 0;
        else if ( mPosY < -abs((TILE_SIZE*12)-viewHeight) )
            mPosY = -abs((TILE_SIZE*12)-viewHeight);


        //disable scaling for now
        //canvas.scale(mScaleFactor, mScaleFactor); //zooming


        canvas.translate(mPosX, mPosY); //translation - "position"
        matrix.postTranslate( TILE_SIZE,TILE_SIZE); //move the matrix the TILE_SIZE pixels (because of UI around the board)


        drawBackground(canvas,matrix);
        drawShips(canvas,matrix);
        drawShots(canvas, matrix);
        if (board.getBoardStatus() == GameBoard.PLAYER_BOARD) //if player board is visible
            drawPlanShip(canvas, matrix);


        //------------------------------------
        if (grid_enable) //if the grid is enabled , draw it
            drawGrid(canvas);


        int left,top,right,bottom;
        left = TILE_SIZE + sel_x*TILE_SIZE;
        top = TILE_SIZE + sel_y*TILE_SIZE;
        right = 2*TILE_SIZE + sel_x*TILE_SIZE;
        bottom = 2*TILE_SIZE + sel_y*TILE_SIZE;

        //draw selected tile
        Rect rect=new Rect(left,top,right,bottom);
        canvas.drawRect(rect,seltile_paint);

        canvas.restore();

        //after restore so that the coordinates are not relative to the translation
        //after restore so that this is drawn at the top of everything
        canvas.drawCircle(getLastTouchX(),getLastTouchY(),10,txtpaint);

    }


    @Override
    protected void onMeasure(int widthMeasuredSpec, int heightMeasuredSpec) {

        int width = MeasureSpec.getSize(widthMeasuredSpec);
        int height = MeasureSpec.getSize(heightMeasuredSpec);
        setMeasuredDimension(width,height); //dimentions of the view
    }


    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
       viewWidth = xNew;
       viewHeight = yNew;

                        /*
                        these viewWidth and viewHeight variables
                        are the global int variables
                        that were declared above
                        */
    }


    private void initPaints(){

        //text paint
        txtpaint = new Paint(); //paint used for text
        txtpaint.setColor(Color.BLACK); //text color
        txtpaint.setTextAlign(Paint.Align.CENTER);
        txtpaint.setTextSize(TILE_SIZE/2); //text size
        txtpaint.setFakeBoldText(true); //text "bold"

        //gridline paint
        gridline_paint = new Paint(gridline_paint.ANTI_ALIAS_FLAG); //paint used for gridlines
        gridline_paint.setColor(Color.BLACK); //gridline color

        //text paint
        seltile_paint = new Paint(); //paint used for text
        seltile_paint.setColor(Color.GREEN); //text color
        seltile_paint.setAlpha(100);

        shot_paint = new Paint(); //paint used for text
        shot_paint.setColor(Color.RED); //text color
        shot_paint.setAlpha(100);
    }

    public float getLastTouchX()
    {
        return mLastTouchX;
    }

    public float getLastTouchY()
    {
        return mLastTouchY;
    }



    private void drawShips(Canvas canvas,Matrix matrix){
        Matrix matrix_temp = new Matrix();
        Bitmap spr;
        /*Draw Ships*/
        for (int i=0 ; i<board.getBoard(board.getBoardStatus()).size() ; i++ ) {

            GameShip gs_temp = (GameShip) board.getBoard(board.getBoardStatus()).get(i); //create a temporary ship
            spr = BitmapFactory.decodeResource(getResources(),gs_temp.getSprite()); //load the proper sprite/bitmap

            spr = getResizedBitmap(spr,TILE_SIZE,TILE_SIZE*gs_temp.getSize()); //resize the sprite

            matrix_temp.set(matrix); // matrix_temp will copy the state of matrix

            //rotate bitmap
            matrix_temp.postTranslate( -TILE_SIZE,-TILE_SIZE); //translate -TILE_SIZE because matrix is already translated
            matrix_temp.postRotate(-gs_temp.getRotation(),TILE_SIZE/2,TILE_SIZE/2);
            matrix_temp.postTranslate(TILE_SIZE,TILE_SIZE);

            matrix_temp.postTranslate(gs_temp.getPosX() * bt_background.getWidth(), gs_temp.getPosY() * bt_background.getHeight());// translate matrix to proper position
            canvas.drawBitmap(spr, matrix_temp, null); //draw ship/bitmap
        }
    }


    private void drawPlanShip(Canvas canvas,Matrix matrix){
        //This is used to show a transparent version of the ship to be added
        GameShip gs_temp = new GameShip();
        int tmp_id = gs_temp.IdentifyShip(board.getShipToAdd());
        if (tmp_id == -1) //if ship doesn't exist exit
            return;
        gs_temp.setShip(getSelectedTileX(), getSelectedTileY(),board.getShipToAddRotation(), tmp_id, BoardObject.BOARD_OBJECT_STATUS_GHOST);
        Matrix matrix_temp = new Matrix();
        Bitmap spr;

        spr = BitmapFactory.decodeResource(getResources(),gs_temp.getSprite()); //load the proper sprite/bitmap
        spr.setHasAlpha(true);
        spr = getTransparentBitmap(spr,125);

        spr = getResizedBitmap(spr,TILE_SIZE,TILE_SIZE*gs_temp.getSize()); //resize the sprite ,we know rotation is 0

        matrix_temp.set(matrix); // matrix_temp will copy the state of matrix

        //rotate bitmap
        matrix_temp.postTranslate( -TILE_SIZE,-TILE_SIZE); //translate -TILE_SIZE because matrix is already translated
        matrix_temp.postRotate(-gs_temp.getRotation(),TILE_SIZE/2,TILE_SIZE/2);
        matrix_temp.postTranslate(TILE_SIZE,TILE_SIZE);

        matrix_temp.postTranslate(gs_temp.getPosX()*TILE_SIZE,gs_temp.getPosY() * TILE_SIZE);// translate matrix to proper position
        canvas.drawBitmap(spr, matrix_temp, null); //draw ship/bitmap

    }

    //Draws the shots fire depending on which board is currently visible (player's or enemy)
    private void drawShots(Canvas canvas,Matrix matrix){


        int left,top,right,bottom;
        Bitmap tmp_shot;
        Matrix matrix_temp = new Matrix();
        for (int line=0 ; line < board_sizeX ; line++)
        {
            for (int col=0 ; col < board_sizeY ; col++)
            {
                // matrix_temp will "reset" the matrix state to the one that is was before entering the loops
                matrix_temp.set(matrix);
                if ( board.getShots(board.getBoardStatus())[line][col] != GameBoard.SHOT_EMPTY ) { //if a shot exists at these location

                    left = TILE_SIZE + col * TILE_SIZE;
                    top = TILE_SIZE + line * TILE_SIZE;
                    right = 2 * TILE_SIZE + col * TILE_SIZE;
                    bottom = 2 * TILE_SIZE + line * TILE_SIZE;
                    Rect rect = new Rect(left, top, right, bottom);
                    canvas.drawRect(rect, shot_paint); // transparent rect for shot
                    //if this tile has no object
                    if ( board.getShots(board.getBoardStatus())[line][col] == GameBoard.SHOT_HIT )
                        tmp_shot = shot;
                    else if ( board.getShots(board.getBoardStatus())[line][col] == GameBoard.SHOT_OBJECT )
                        tmp_shot = shot_target;
                    else
                        tmp_shot = shot;
                    matrix_temp.postTranslate(col * tmp_shot.getWidth(), line * tmp_shot.getHeight());// x,y
                    canvas.drawBitmap(tmp_shot, matrix_temp, null);



                }
            }
        }

    }

    //draws Backgrounds along with GUI and grid numbers
    private void drawBackground(Canvas canvas,Matrix matrix){

        Matrix matrix_temp = new Matrix();
        /*Draw Background */


        //draw corner gui blocks
        canvas.drawBitmap(bt_gui,matrix_temp, null); //top left
        matrix_temp.postTranslate((board.getDimX()+1)*bt_gui.getWidth(),0);// x,y
        canvas.drawBitmap(bt_gui,matrix_temp, null); //top right
        matrix_temp.postTranslate(0,(board.getDimY()+1)*bt_gui.getHeight());// x,y
        canvas.drawBitmap(bt_gui,matrix_temp, null);//bottom right
        matrix_temp.postTranslate(-(board.getDimX()+1)*bt_gui.getWidth(),0);// x,y
        canvas.drawBitmap(bt_gui,matrix_temp, null);//bottom left

        for (int line=0 ; line < board_sizeX ; line++)
        {


            //left gui blocks
            matrix_temp.set(matrix); //reset matrix
            matrix_temp.postTranslate(-bt_gui.getWidth(),line*bt_gui.getHeight());// x,y
            canvas.drawBitmap(bt_gui, matrix_temp, null);

            //right gui blocks
            matrix_temp.set(matrix); //reset matrix
            matrix_temp.postTranslate(board.getDimX()*bt_gui.getWidth(),line*bt_gui.getHeight());// x,y
            canvas.drawBitmap(bt_gui, matrix_temp, null);



            //draw grid's line numbers
            canvas.drawText(""+line, TILE_SIZE/2 ,TILE_SIZE*line+ 1.5f*TILE_SIZE,txtpaint);

            for (int col=0 ; col < board_sizeY ; col++)
            {

                //top gui blocks
                matrix_temp.set(matrix); //reset matrix
                matrix_temp.postTranslate(col * bt_gui.getWidth(),-bt_gui.getHeight());// x,y
                canvas.drawBitmap(bt_gui, matrix_temp, null);

                //bottom gui blocks
                matrix_temp.set(matrix); //reset matrix
                matrix_temp.postTranslate(col * bt_gui.getWidth(),board.getDimY()*bt_gui.getHeight());// x,y
                canvas.drawBitmap(bt_gui, matrix_temp, null);


                //draw grid's column numbers
                canvas.drawText(""+col,TILE_SIZE*col+ 1.5f*TILE_SIZE ,TILE_SIZE/2  ,txtpaint);

                // matrix_temp will "reset" the matrix state to the one that is was before entering the loops
                matrix_temp.set(matrix);
                matrix_temp.postTranslate(col * bt_background.getWidth(), line * bt_background.getHeight());// x,y
                //draw the background
                canvas.drawBitmap(bt_background, matrix_temp, null);
            }
        }
    }

    private void drawGrid(Canvas canvas){

        for (int line=0 ; line < board_sizeX+1 ; line++)
        {
            //draw grid's horizontal lines
            canvas.drawLine(TILE_SIZE, TILE_SIZE*line+TILE_SIZE, TILE_SIZE*board_sizeX+TILE_SIZE, TILE_SIZE*line+TILE_SIZE, gridline_paint);
            for (int col=0 ; col < board_sizeY+1 ; col++) {
                //draw grid's row lines
                canvas.drawLine(TILE_SIZE * col + TILE_SIZE, TILE_SIZE, TILE_SIZE * col + TILE_SIZE, TILE_SIZE * board_sizeY + TILE_SIZE, gridline_paint);
            }
        }
    }

    public int getSelectedTileX(){return sel_x;} //return the X of the currently selected tile
    public int getSelectedTileY(){return sel_y;} //return the Y of the currently selected tile

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

        int width = bm.getWidth();

        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;

        float scaleHeight = ((float) newHeight) / height;

// create a matrix for the manipulation

        Matrix matrix = new Matrix();

// resize the bit map

        matrix.postScale(scaleWidth, scaleHeight);

// recreate the new Bitmap

        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        return resizedBitmap;

    }

    //Used to rotate a bitmap ,if center is true it's rotated around it's center (e.g. ships)
    private Bitmap getRotatedBitmap(Bitmap source, float angle,boolean center)
    {
        Matrix matrix = new Matrix();
        if ( center == false )
            matrix.postRotate(angle);
        else
        {
         matrix.setTranslate(source.getWidth()/2, source.getHeight()/2); //center the source image
         matrix.postRotate(angle); //rotate the image
         matrix.postTranslate(-source.getWidth() / 2,-source.getHeight() / 2); //now move it to the center of your final image
        }
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    //Used to rotate a bitmap with pivot x,y
    private Bitmap getRotatedBitmap(Bitmap source, float angle,float x , float y)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle,x,y);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Bitmap getTransparentBitmap(Bitmap src, int value) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap transBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(transBitmap);
        canvas.drawARGB(0, 0, 0, 0);
        // config paint
        final Paint paint = new Paint();
        paint.setAlpha(value);
        canvas.drawBitmap(src, 0, 0, paint);
        return transBitmap;
    }




}