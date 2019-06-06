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
    private Thread gameThread = null;
    private volatile boolean gameIsRunning;

    // drawing variables
    private Canvas canvas;
    private Paint paintbrush;
    private SurfaceHolder holder;

    // Screen resolution varaibles
    private int screenWidth;
    private int screenHeight;

    // VISIBLE GAME PLAY AREA
    // These variables are set in the constructor
    int VISIBLE_LEFT;
    int VISIBLE_TOP;
    int VISIBLE_RIGHT;
    int VISIBLE_BOTTOM;


    // point
    Point cageCatcherPosition;
    // SPRITES
    Square bullet;
    int SQUARE_WIDTH = 50;


    CageCatcher cageCatcher;
    Sprite player;
    Sprite sparrow;
    Cat cat;






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

        this.player = new Sprite(this.getContext(), 100, 700, R.drawable.player64);
        this.sparrow = new Sprite(this.getContext(), 500, 200, R.drawable.bird64);
        this.cat = new Cat(context,1500, 700);
        this.cageCatcher = new CageCatcher(context, 1500, 100);
        this.bullet = new Square(context, 100, 800, SQUARE_WIDTH);

        cageCatcherPosition = new Point();
        cageCatcherPosition.x = 1500;
        cageCatcherPosition.y = 100;
    }

    @Override
    public void run() {
        while (gameIsRunning == true) {
            updateGame();    // updating positions of stuff
            redrawSprites(); // drawing the stuff
            controlFPS();
        }
    }
    // CAGE SPEED
    final int CAGE_SPEED = 20;

    // CAT SPEED
    final int CAT_SPEED = 20;
    // Sparrow speed
    final int SPARROW_SPEED = 10;


    boolean CageMovingRight = true;

    // Game Loop methods
    public void updateGame() {




// CAGE MOVING
        this.cageCatcher.setxPosition(this.cageCatcher.getxPosition() - CAGE_SPEED);




        // Cat moving
        this.cat.setxPosition(this.cat.getxPosition()-CAT_SPEED);


// Sparrow hitbox
        this.sparrow.setxPosition(this.sparrow.getxPosition()-SPARROW_SPEED);
        // cat hitbox
        this.cat.getHitbox().left = this.cat.getxPosition();
        this.cat.getHitbox().top = this.cat.getyPosition();
        this.cat.getHitbox().right = this.cat.getxPosition() + this.cat.getImage().getWidth();
        this.cat.getHitbox().bottom = this.cat.getyPosition() + this.cat.getImage().getHeight();

        // cage hitbox
        this.cageCatcher.getHitbox().left = this.cageCatcher.getxPosition();
        this.cageCatcher.getHitbox().top = this.cageCatcher.getyPosition();
        this.cageCatcher.getHitbox().right = this.cageCatcher.getxPosition() + this.cageCatcher.getImage().getWidth();
        this.cageCatcher.getHitbox().bottom = this.cageCatcher.getyPosition() + this.cageCatcher.getImage().getHeight();


// Cage back to position
        int backOfCage = this.cageCatcher.getxPosition() + this.cageCatcher.getImage().getWidth();
        if (backOfCage <= 0) {
            // restart him at original position
            this.cageCatcher.setxPosition(1500);
            this.cageCatcher.setyPosition(120);
        }


        // Cat back to position
        int backOfCat = this.cat.getxPosition() + this.cat.getImage().getWidth();
        if (backOfCat <= 0) {
            // restart him at original position
            this.cat.setxPosition(1500);
            this.cat.setyPosition(700);
        }


// making Bullet Move
        double a = this.cageCatcher.getxPosition() - this.bullet.getxPosition();
        double b = this.cageCatcher.getyPosition() - this.bullet.getyPosition();

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

        // 4. update the bullet hitbox position
        this.bullet.updateHitbox();


        // COLLISION DETECTION FOR BULLET
        // -----------------------------




        // R1: When bullet intersects the enemy, restart bullet position
        if (bullet.getHitbox().intersect(cageCatcher.getHitbox())) {




            // RESTART THE BULLET FROM INITIAL POSITION
            this.cageCatcher.setxPosition(this.cat.getxPosition());
            this.cageCatcher.setyPosition(this.cat.getyPosition());

            // RESTART THE HITBOX
            this.bullet.updateHitbox();
        }





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
            canvas = holder.lockCanvas();
            // --------------------------------

            // set the game's background color
            canvas.drawColor(Color.argb(255,255,255,255));

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
            // draw player , sparrow , cat and cage
            // --------------------------------------------------------

            // 1. player
            canvas.drawBitmap(this.player.getImage(), this.player.getxPosition(), this.player.getyPosition(), paintbrush);

            // 2. sparrow
            canvas.drawBitmap(this.sparrow.getImage(), this.sparrow.getxPosition(), this.sparrow.getyPosition(), paintbrush);

            //3. cat
            canvas.drawBitmap(this.cat.getImage(), this.cat.getxPosition(), this.cat.getyPosition(), paintbrush);


            //3. cage
            canvas.drawBitmap(this.cageCatcher.getImage(), this.cageCatcher.getxPosition(), this.cageCatcher.getyPosition(), paintbrush);
            // --------------------------------------------------------
            // draw hitbox on player
            // --------------------------------------------------------
            Rect r = player.getHitbox();
            paintbrush.setStyle(Paint.Style.STROKE);
            canvas.drawRect(r, paintbrush);


            // --------------------------------------------------------
            // draw hitbox on player
            // --------------------------------------------------------
            paintbrush.setTextSize(60);
            paintbrush.setStrokeWidth(5);
            String screenInfo = "Screen size: (" + this.screenWidth + "," + this.screenHeight + ")";
            canvas.drawText(screenInfo, 10, 100, paintbrush);


// Draw cat hitbox
            paintbrush.setColor(Color.RED);
            paintbrush.setTextSize(60);
            paintbrush.setStrokeWidth(5);
            canvas.drawRect(this.cat.getHitbox().left,
                    this.cat.getHitbox().top,
                    this.cat.getHitbox().right,
                    this.cat.getHitbox().bottom,
                    paintbrush
            );


            // Draw cage hitbox
            paintbrush.setColor(Color.RED);
            paintbrush.setTextSize(60);
            paintbrush.setStrokeWidth(5);
            canvas.drawRect(this.cageCatcher.getHitbox().left,
                    this.cageCatcher.getHitbox().top,
                    this.cageCatcher.getHitbox().right,
                    this.cageCatcher.getHitbox().bottom,
                    paintbrush
            );



            // draw bullet
            paintbrush.setColor(Color.BLACK);
            paintbrush.setStyle(Paint.Style.FILL);
            paintbrush.setStrokeWidth(8);
            canvas.drawRect(
                    this.bullet.getxPosition(),
                    this.bullet.getyPosition(),
                    this.bullet.getxPosition() + this.bullet.getWidth(),
                    this.bullet.getyPosition() + this.bullet.getWidth(),
                    paintbrush
            );

            // draw the bullet hitbox
            paintbrush.setColor(Color.RED);
            paintbrush.setStyle(Paint.Style.STROKE);
            canvas.drawRect(
                    this.bullet.getHitbox(),
                    paintbrush
            );
            // --------------------------------
            holder.unlockCanvasAndPost(canvas);






        }

    }

    public void controlFPS() {
        try {
            gameThread.sleep(17);
        }
        catch (InterruptedException e) {

        }
    }


    // Deal with user input
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int userAction = event.getActionMasked();
        //@TODO: What should happen when person touches the screen?
        if (userAction == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "Person tapped the screen");

            // @TODO: Write code so when person taps down, player MOVES UP!
            this.bullet.setyPosition(this.bullet.getyPosition() - 60);

            // update hitbox position
            this.bullet.getHitbox().left = this.bullet.getxPosition();
            this.player.getHitbox().top = this.player.getyPosition();
            this.player.getHitbox().right = this.player.getxPosition() + this.bullet.getWidth();
            this.bullet.getHitbox().bottom = this.bullet.getyPosition() ;
        }
        else if (userAction == MotionEvent.ACTION_UP) {
            Log.d(TAG, "Person lifted finger");
        }
        return true;
    }

    // Game status - pause & resume
    public void pauseGame() {
        gameIsRunning = false;
        try {
            gameThread.join();
        }
        catch (InterruptedException e) {

        }
    }
    public void  resumeGame() {
        gameIsRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

}

