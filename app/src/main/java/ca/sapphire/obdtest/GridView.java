package ca.sapphire.obdtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.View;

/**
 * Created by Ashley on 27/06/15.
 *
 */
public class GridView extends View {
    private ShapeDrawable mDrawable;
    Paint paint = new Paint();

    int xGrid, yGrid;
    public int gridSize = 8;

    int element[][];
    int colorArray[];

    public GridView(Context context, int xGrid, int yGrid ) {
        super(context);

        this.xGrid = xGrid;
        this.yGrid = yGrid;

        int x = 10;
        int y = 10;
        int width = 300;
        int height = 50;

        element = new int[xGrid][yGrid];

        for (int i = 0; i < xGrid; i++) {
            for (int j = 0; j <yGrid ; j++) {
                element[i][j] = 0;
            }
        }

        element[0][0] = 0;
        element[1][0] = 1;
        element[2][0] = 2;
        element[3][0] = 3;
        element[4][0] = 4;
        element[5][0] = 5;
        element[6][0] = 6;
        element[7][0] = 7;
        element[8][0] = 8;
        element[9][0] = 9;


        colorArray = new int[10];

        setColorArray( GridView.VeryBright );

//        paint.setColor(Color.DKGRAY );

//        mDrawable = new ShapeDrawable(new OvalShape());
//        mDrawable.getPaint().setColor(0xff74AC23);
//        mDrawable.setBounds(x, y, x + width, y + height);
    }

    protected void onDraw(Canvas canvas) {
//        mDrawable.draw(canvas);
//        paint.setColor( Color.DKGRAY);
        paint.setColor(Color.BLACK);

        int xMin = 10;
        int yMin = 10;
        int xAt = xMin;
        int yAt = yMin;
        int xMax = xMin + (xGrid * (gridSize+1) ) + 1;
        int yMax = yMin + (yGrid * (gridSize+1) ) + 1;

        for (int i = 0; i <= xGrid; i++) {
            canvas.drawLine(xAt, yMin, xAt, yMax, paint);
            xAt += gridSize + 1;
        }

        for (int i = 0; i <= yGrid; i++) {
            canvas.drawLine(xMin, yAt, xMax, yAt, paint);
            yAt += gridSize + 1;
        }

        for (int i = 0; i < xGrid ; i++) {
            for (int j = 0; j < yGrid; j++) {
                    renderBlock( canvas, paint, i, j );
            }
        }
    }


    public final static float VeryBright = 0.9f;
    public final static float Bright = 0.75f;
    public final static float Medium = 0.5f;
    public final static float Dim = 0.33f;

    private final static int hueIndex = 0;
    private final static int satIndex = 1;
    private final static int lumIndex = 2;

    public void setColorArray( float brightness ) {


        float hsv[] = new float[] { 0.0f, 1.0f, 0.5f };

        hsv[lumIndex] = brightness;

        colorArray[0] = Color.DKGRAY;

        for (int i = 1; i < 10; i++) {
            hsv[hueIndex] = 240 - ( i * (240/8) );

            colorArray[i] = Color.HSVToColor( 0xff, hsv );
        }

//        colorArray[0] = Color.rgb(0x7f,0x00,0xff);
//        colorArray[1] = Color.rgb(0x00,0x00,0xff);
//        colorArray[2] = Color.rgb(0x00,0x7f,0xff);
//        colorArray[3] = Color.rgb(0x00,0xff,0xff);
//        colorArray[4] = Color.rgb(0x00,0xff,0x7f);
//        colorArray[5] = Color.rgb(0x00,0xff,0x00);
//        colorArray[6] = Color.rgb(0x7f,0xff,0x00);
//        colorArray[7] = Color.rgb(0xff,0xff,0x00);
//        colorArray[8] = Color.rgb(0xff,0x7f,0x00);
//        colorArray[9] = Color.rgb(0xff,0x00,0x00);


    }

/*  Eg: gridSize = 5
*                       |xxxxx|xxxxx|
*                       0123456789012
*   first grid line:    0
*   next grid line:     6   ( 0 + (gridSize + 1 )
*   start of element:   1   ( 0 + 1 )
*   end of element:     5   ( start + gridSize - 1 )
*   next start:         7   (
*   next end:           11
*
*   Important note:  Even though the documentation says the rectangle draws from start to end,
*                    it doesn't actually include the end pixel.  This has to be added.
*/

    public void renderBlock( Canvas canvas, Paint paint, int x, int y ) {
        int xs = x*(gridSize+1)+1;
        int ys = y*(gridSize+1)+1;
        int xe = xs+gridSize;
        int ye = ys+gridSize;

        xs += 10; xe+=10; ys +=10; ye+=10;

        int colIndex = element[x][y];
        if( colIndex >= 10 )
            colIndex = 9;

        paint.setColor( colorArray[colIndex] );

        canvas.drawRect( xs, ys, xe, ye, paint );
    }

    public void elementInc( int index ) {
        int x = index/xGrid;
        int y = index%xGrid;

        element[x][y]++;
    }

    public void decAll() {
        for (int i = 0; i < xGrid ; i++) {
            for (int j = 0; j < yGrid; j++) {
                if( element[i][j] > 0)
                    element[i][j]--;
            }
        }
    }

}

