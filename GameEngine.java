package com.example.sparrow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class GameEngine extends SurfaceView implements Runnable {
     final String TAG = "SPARROW";

    // game thread variables
    Thread gameThread = null;
     boolean gameIsRunning;

    // drawing variables
     Canvas canvas;
     Paint paintbrush;
     SurfaceHolder holder;

    // Screen resolution varaibles
     int screenWidth;
     int screenHeight;
     //drawing variables
    // player variables
    Bitmap playerImage;
    Rect playerHitbox;
     Point player;
     //cat variables
    Bitmap catImage;
   Point cat;
   //sparrow variables
    Bitmap sparrowImage;
    Point sparrow;
    //Cage variables
    Bitmap cageImage;
    Point cage;

    // VISIBLE GAME PLAY AREA
    // These variables are set in the constructor
    int VISIBLE_LEFT;
    int VISIBLE_TOP;
    int VISIBLE_RIGHT;
    int VISIBLE_BOTTOM;

    // SPRITES
    Square bullet;
    int SQUARE_WIDTH = 100;

    Square enemy;




    ArrayList<Square> bullets = new ArrayList<Square>();

    // GAME STATS
    int score = 0;
    int lives = 3;


    public GameEngine(Context context, int screenW, int screenH) {
        super(context);

        // intialize the drawing variables
        this.holder = this.getHolder();
        this.paintbrush = new Paint();

        // set screen height and width
        this.screenWidth = screenW;
        this.screenHeight = screenH;

        // setup visible game play area variables
        this.VISIBLE_LEFT = 20;
        this.VISIBLE_TOP = 10;
        this.VISIBLE_RIGHT = this.screenWidth - 20;
        this.VISIBLE_BOTTOM = (int) (this.screenHeight * 0.8);


        // initalize sprites

        this.playerImage = BitmapFactory.decodeResource(context.getResources(),R.drawable.player64);
        this.player = new Point();
        this.player.x = 100;
        this.player.y = 700;
// setup player hitbox

        this.playerHitbox = new Rect(
                this.player.x,
                this.player.y,
                this.player.x+this.playerImage.getWidth(),
                this.player.y + playerImage.getHeight());
        // setup cage
        this.cageImage = BitmapFactory.decodeResource(context.getResources(),R.drawable.box);
        this.cage = new Point();
        this.cage.x = 700;
        this.cage.y = 100;

// setuo sparrow
        this.sparrowImage = BitmapFactory.decodeResource(context.getResources(),R.drawable.bird64);
        this.sparrow = new Point();
        this.sparrow.x = 100;
        this.sparrow.y = 400;

        this.catImage = BitmapFactory.decodeResource(context.getResources(),R.drawable.cat64);
        this.cat = new Point();
        this.cat.x = 100;
        this.cat.y = 700;

        // Deal with user input

        }

        @Override
        public void run() {while (gameIsRunning == true) {
            this.updatePositions();
            this.redrawSprites();
            this.setFPS();
        }
        }


        public void pauseGame() {
            gameIsRunning = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                // Error
            }
        }

        public void resumeGame() {
            gameIsRunning = true;
            gameThread = new Thread(this);
            gameThread.start();
        }




    final int CAT_SPEED = 20;
    final int BIRD_SPEED = 10;
    final int CAGE_SPEED = 15;

    // Game Loop methods
    public void updatePositions() {
this.cat.x = this.cat.x + CAT_SPEED;
this.sparrow.x =this.sparrow.x +  BIRD_SPEED;
        Log.d(TAG,"Bullet position: " + this.bullet.getxPosition() + ", " + this.bullet.getyPosition());
        Log.d(TAG,"Enemy position: " + this.enemy.getxPosition() + ", " + this.enemy.getyPosition());

        //calculate the distance
        double a = this.enemy.getxPosition() - this.bullet.getxPosition();
        double b = this.enemy.getyPosition() - this.bullet.getyPosition();

        // d = sqrt(a^2 + b^2)

        double d = Math.sqrt((a * a) + (b * b));

        Log.d(TAG, "Distance to enemy: " + d);

        // 2. calculate xn and yn constants
        // (amount of x to move, amount of y to move)
        double xn = (a / d);
        double yn = (b / d);

        // 3. calculate new (x,y) coordinates
        int newX = this.bullet.getxPosition() + (int) (xn * 15);
        int newY = this.bullet.getyPosition() + (int) (yn * 15);
        this.bullet.setxPosition(newX);
        this.bullet.setyPosition(newY);

        Log.d(TAG,"----------");
    }





    public void outputVisibleArea() {
        Log.d(TAG, "DEBUG: The visible area of the screen is:");
        Log.d(TAG, "DEBUG: Maximum w,h = " + this.screenWidth +  "," + this.screenHeight);
        Log.d(TAG, "DEBUG: Visible w,h =" + VISIBLE_RIGHT + "," + VISIBLE_BOTTOM);
        Log.d(TAG, "-------------------------------------");
    }



    public void redrawSprites() {
        if (holder.getSurface().isValid()) {

            // initialize the canvas
            this.canvas = holder.lockCanvas();
            // --------------------------------

            // set the game's background color
            this.canvas.drawColor(Color.argb(255,255,255,255));


            // setup stroke style and width
            paintbrush.setStyle(Paint.Style.FILL);
            paintbrush.setStrokeWidth(8);

            // --------------------------------------------------------
            // draw boundaries of the visible space of app
            // --------------------------------------------------------
            paintbrush.setStyle(Paint.Style.STROKE);
            paintbrush.setColor(Color.argb(255, 0, 128, 0));

            canvas.drawRect(VISIBLE_LEFT, VISIBLE_TOP, VISIBLE_RIGHT, VISIBLE_BOTTOM, paintbrush);
            this.outputVisibleArea();

            // --------------------------------------------------------
            // draw player, sparrow and cat
            // --------------------------------------------------------

            //1. draw player
            canvas.drawBitmap(playerImage,this.player.x,this.player.y,paintbrush);
            //2. draw sparrow
            canvas.drawBitmap(sparrowImage,this.sparrow.x,this.sparrow.y,paintbrush);
            //3.  draw cat
            canvas.drawBitmap(catImage, this.cat.x,this.cat.y,paintbrush);
            // --------------------------------------------------------
            // draw hitbox on player
            // --------------------------------------------------------
            // 1. change the paintbrush settings so we can see the hitbox
            paintbrush.setColor(Color.BLUE);
            paintbrush.setStyle(Paint.Style.STROKE);
            paintbrush.setStrokeWidth(5);

            // 2. draw the hitbox
            canvas.drawRect(this.playerHitbox.left,
                    this.playerHitbox.top,
                    this.playerHitbox.right,
                    this.playerHitbox.bottom,
                    paintbrush
            );


            // --------------------------------------------------------
            // draw hitbox on player
            // --------------------------------------------------------
            paintbrush.setTextSize(60);
            paintbrush.setStrokeWidth(5);
            String screenInfo = "Screen size: (" + this.screenWidth + "," + this.screenHeight + ")";
            canvas.drawText(screenInfo, 10, 100, paintbrush);

            // --------------------------------
            holder.unlockCanvasAndPost(canvas);
        }

    }

    public void setFPS() {
        try {
            gameThread.sleep(17);
        }
        catch (InterruptedException e) {

        }
    }


    // Deal with user input
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_DOWN:
                break;
       }
        return true;
    }
}





