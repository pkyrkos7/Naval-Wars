package gr.panos.navalwars;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by Panos on 6/8/2015.
 */


//This is a general class that represent every object that is in the board
public class BoardObject {

    private int[][] pos; //contains the positions (all positions/tiles) of the object (using sets of coordinates 0,1 etc..
    private String[] posStatus; //contains the status for each of the above pairs

    private int posX; //object's x position (first tile)
    private int posY; //object's y position (first tile)

    private int rotation;  //direction it's facing - will be used when creating the board -default 0 (right)

    private int size; //object's size  - this should be consistent with the bitmap size (see sizePixels in GameBoard)- e.g. size = 1 ==> 64x64
    private String name; // object's name
    private String type; //what kind of object it is e.g. ship , mine , etc...
    private String status; //object's statues e.g. alive , destroyed , damaged..
    private int sprite; //the "image" that it will be used

    public static String BOARD_OBJECT_STATUS_ALIVE = "Undamaged";
    public static String BOARD_OBJECT_STATUS_DAMAGED = "Damaged";
    public static String BOARD_OBJECT_STATUS_DEAD = "Destroyed";
    public static String BOARD_OBJECT_STATUS_GHOST = "Ghost";

    public BoardObject(){

        posX = 0;
        posY = 0;
        rotation = 0;
        size = 1;
        name = "defaultObject";
        type = "Object";
        status = "Undefined";
        sprite = -1;
        pos = new int[size][2];
        posStatus = new String[size];
        CalcTiles();
    }


    public void setSprite(int _sprite)
    {
        sprite = _sprite;
    }

    //this calculates the tiles that are occupied by the object based on it's x,y,rotation and size
    protected void CalcTiles(){

        for (int i=0 ; i<size ; i++){
            if ( rotation == 0) {
                pos[i][0] = posX + i;
                pos[i][1] = posY;
            }
            else if ( rotation == 90 ) {
                pos[i][0] = posX;
                pos[i][1] = posY - i;
            }
            else if ( rotation == 180 ) {
                pos[i][0] = posX - i;
                pos[i][1] = posY;
            }
            else if ( rotation == 270 ) {
                pos[i][0] = posX;
                pos[i][1] = posY + i;
            }

            posStatus[i] = status;
        }
    }


    //returns true if the whole object(all tiles) has a specific status
    public boolean checkStatus(String _status){

        for ( int i=0 ; i<size ; i++){
            if ( posStatus[i].equals(_status) == false ) //if at least one tile hasn't the same status return false
                return false;
        }
        return true;
    }

    //this sets the specified status at the x,y coordinates of the object (specific tile for object)
    //returns true if success
    public boolean setPosStatus(int x,int y,String status) {
        //if position exists
        int tmp = getPos(x, y) ; //position in array
        if ( tmp >= 0 )
        {
            posStatus[tmp] = status;
            return true;
        }
        else
            return false;
    }

    //returns the status that exists at the x,y coordinates of the object
    //else return null
    public String getPosStatus(int x,int y) {
        //if position exists
        int tmp = getPos(x,y) ; //position in array
        if ( tmp >= 0 )
            return posStatus[tmp];
        else
            return null;
    }

    //returns true if the board object exists at these coordinates
    public boolean checkPos(int x ,int y){
        if ( getPos(x,y) >= 0 )
            return true;
        return false;
    }

    //returns the position in the array if the board object exists at these coordinates
    //else returns -1
    public int getPos(int x,int y)
    {
        for (int i=0 ; i<size ; i++) {
            if (pos[i][0] == x && pos[i][1] == y) {
                return i;
            }
        }
        return -1;

    }

    //### Used for DEBUGGING ###//
    public void LogShowPositions(){
        for (int i=0 ; i<size ; i++) {
            Log.d("DEBUG", "Object with name "+ getName() +" has positions: " + pos[i][0] + "," + pos[i][1]);
        }

    }


    //returns true if all object tiles are "destroyed"
    public boolean IsDestroyed(){
        return checkStatus("Destroyed");
    }



    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
        CalcTiles();
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
        CalcTiles();
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
        CalcTiles();
    }


    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        pos = new int[size][2];
        posStatus = new String[size];
        CalcTiles();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSprite() {
        return sprite;
    }

    public int[][] getPos() {return pos;}





}
