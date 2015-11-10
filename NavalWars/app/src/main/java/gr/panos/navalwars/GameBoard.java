package gr.panos.navalwars;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.util.Random;
import java.util.Vector;


/**
 * Created by Panos on 6/8/2015.
 */
public class GameBoard{


    protected static int sizePixels = 64; //default pixels for size 1
    protected static int defaultBackgroundTile =  R.drawable.grid_square64x64; //default background tile
    protected static int defaultGuiTile =  R.drawable.gui_block; //default background tile
    protected static int defaultShotTile = R.drawable.shot; //default tile for shot
    protected static int defaultShotTargetTile = R.drawable.shot_target; //default tile for shot target - when the shot hits something


    //constants used to determine which board we want
    public static int PLAYER_BOARD = 0;
    public static int ENEMY_BOARD = 1;
    private static int MAX_TRIES_PLANSHIPS = 1000;

    public static String DEFAULT_BOARD_NAME = "Open Sea";

    private int BoardStatus; //BoardStatus determines what "board/canvas" is currently shown and handled - player's or his enemy

    private DrawBoard drawBoard;



    private int phase; //the phase the the game is currently in
    private int phaseStatus; //used to determine the phase's status - 1 = finished , 0 = not finished

    public static int PHASE_STATUS_COMPLETED = 1;
    public static int PHASE_STATUS_INCOMPLETE = 0;
    public static int PHASE_PLAN = 1 ; //phase 1 - player adds his ships
    public static int PHASE_PLAYER_ATTACK = 2 ; //phase 2 - player attacks the enemy's board
    public static int PHASE_ENEMY_ATTACK = 3 ; //phase 3 - enemy attacks the player's
    public static int PHASE_NULL = -1 ;

    public static char SHOT_HIT = 'h';
    public static char SHOT_EMPTY = 'e';
    public static char SHOT_OBJECT = 'o';


    private int dimX; // should be larger than 0
    private int dimY;  // should be larger than 0
    private Vector<BoardObject> playerBoard,enemyBoard;  //contains the objects of the board
    //enemyBoard not needed atm


    //this 2 dimentional array of size dimX*dimY will contain the shots that player fired (will shown at the enemy board)
    //it will contain a value 'h' (hit) if at the given position there's a shot/hit
    // 'e' value means no shot (empty)
    // 'o' (object) value means object exists there
    private char[][] shotsPlayer;
    private char[][] shotsEnemy;

    private String name;
    private String shipToAdd;//this variable holds the name of the ship to be added (so it can parsed to DrawBoard)
    private int shipToAddRotation; //this variable holds the rotation of the ship to be added

    public GameBoard(Context context , int _dimX , int _dimY,TextView tv1,TextView tv2){
        BoardStatus = PLAYER_BOARD;
        dimX = _dimX;
        dimY = _dimY;
        name = DEFAULT_BOARD_NAME;
        phase = PHASE_NULL;
        phaseStatus = PHASE_STATUS_INCOMPLETE;
        shipToAdd = "null";
        initBoards();
        drawBoard = new DrawBoard(context,this,tv1,tv2);
    }

    //this constructor since it doesn't have a Context , it's used for AI which doesn't need a Canvas
    public GameBoard(int _dimX , int _dimY){
        BoardStatus = PLAYER_BOARD;
        dimX = _dimX;
        dimY = _dimY;
        name = DEFAULT_BOARD_NAME;
        phase = PHASE_NULL;
        phaseStatus = PHASE_STATUS_INCOMPLETE;
        shipToAdd = "null";
        initBoards();
    }


    //initialize once
    private void initBoards(){
        playerBoard = new Vector();
        enemyBoard  = new Vector();
        shotsPlayer = new char[dimY][dimX];
        shotsEnemy = new char[dimY][dimX];

        //initialize values to false so we know these are "empty"
        for (int i=0 ; i< dimX ; i++) {
            for (int j = 0; j < dimY; j++) {
                shotsPlayer[i][j] = SHOT_EMPTY;
                shotsEnemy[i][j] = SHOT_EMPTY;
            }
        }

    }

    //places random ships
    //the parameter is a string array that contains all the ships that will be added (Based on the names given)
    //returns the position of the first string that doesn't exist as a ship name - if all ships are added -1 is returned
    public int planPlayerShipsRandom(String[] ships){
        Random r = new Random();
        GameShip gs;
        int x,y,rot,sid,tries=0;
        for (int i =0; i<ships.length ; i++)
        {
            gs = new GameShip();
            sid = gs.IdentifyShip(ships[i]);
            if ( sid == -1 ) //if a ship with the name ships[i] doesn't exist
                return i; //return the position of the name that doesn't exist
            do {
                x = r.nextInt(getDimX()) ;
                y = r.nextInt(getDimY()) ;
                rot = r.nextInt(4) * 90; // 0 - 270
                gs.setShip(x,y,rot,sid,BoardObject.BOARD_OBJECT_STATUS_ALIVE);
                tries++;
            }while ( addObject(gs,playerBoard) == false && tries<MAX_TRIES_PLANSHIPS);
        }
        return -1;
    }




    //sets a "shot" (with specified charater) in the given 2dim (based on which board is passed) array using the x,y position
    public void setShot(int x ,int y ,char shot,int board)
    {
        //x , y are passed inversed due to how the array is handled in java , so we have to invert their usage
        getShots(board)[y][x] = shot;
    }


    //Adds an object to the board , if the position is free
    //Returns true , if the object is succesfully added
    public boolean addObject(BoardObject _bObj,Vector<BoardObject> _board){
        if ( checkPosFree(_bObj,_board) == true ) //if the position is free
        {
            _board.add(_bObj);
            return true;
        }
        else
            return false;
    }

    //Adds an array of objects to the selected board
    //Returns an boolean array, that will contain true at the position of objects that were successfully added
    public boolean[] addObjects(BoardObject[] _bObj,Vector<BoardObject> _board){
        boolean[] _res = new boolean[_bObj.length];
        for (int i=0 ; i<_bObj.length ;i++)
            _res[i] = addObject(_bObj[i],_board);
        return  _res;
    }

    //Check if the position with x,y coordinates in the given board is free
    //Returns true if the position is free
    public boolean checkPosFree(int x,int y,Vector<BoardObject> _board){

        if ( IsPosInBoard(x,y) == false)
            return false; //exit if the given coordinates aren't inside the board
        //check all the objects from the board vector and see if any occupy this position
        for (int i=0 ; i<_board.size() ; i++)
        {
            if ( _board.get(i).checkPos(x,y) ) //if the i object exists at these coordinates , return false
                return false;
        }
        return true;
    }

    //Second Implementation
    //Check if the position of the object _b in the given board is free
    //Returns true if the position is free
    public boolean checkPosFree(BoardObject _b , Vector<BoardObject> _board){

        if ( IsPosInBoard(_b) == false)
            return false; //exit if the given object isn't inside the board
        //check all the objects from the board vector and see if any occupy the object's position
        for (int i=0 ; i<_board.size() ; i++)
        {
            if ( checkPosObjectOverride(_board.get(i) , _b) == true ) //if the i object occupy the same space as _b object
                return false;
        }
        return true;
    }


    //returns shot at the position in parameters
    //returns '*' for error
    //if direction is -1 it x,y position will be checked
    //otherwise depending on the direction the new position will checked
    public char checkPosShot(int x,int y,int dir,int board)
    {
        if ( IsPosInBoard(x,y) == false)
            return '*'; //exit if the given coordinates aren't inside the board

        char[][] _shotsArray = getShots(board);
        if ( dir == 0 && IsPosInBoard(x+1,y) )
        {
            return _shotsArray[y][x+1];
        }
        else if ( dir == 180 &&  IsPosInBoard(x-1,y) )
        {
            return _shotsArray[y][x-1];
        }
        else if ( dir == 90 &&  IsPosInBoard(x,y-1) )
        {
            return _shotsArray[y-1][x];
        }
        else if ( dir == 270 && IsPosInBoard(x,y+1) )
        {
            return _shotsArray[y+1][x];
        }
        else
        {
            return _shotsArray[y][x];
        }

    }


    public void logShotArray(int board)
    {
        String msg;
        char[][] _shotsArray = getShots(board);
        for (int i=0 ; i <dimX ; i++)
        {
            msg = "";
            for (int j=0 ; j<dimY ; j++)
                msg = " "+_shotsArray[i][j] + msg;
            Log.d("AI", msg);
        }

    }

    //returns true if there's at least one nearby free tile at the given x,y
    public boolean checkPosShotNearbyFree(int x,int y,int board)
    {
        char[][] _shotsArray = getShots(board);
        if ( IsPosInBoard(x+1,y) && _shotsArray[y][x+1] == SHOT_EMPTY )
            return true;
        else if ( IsPosInBoard(x-1,y) && _shotsArray[y][x-1] == SHOT_EMPTY )
            return true;
        else if ( IsPosInBoard(x,y-1) && _shotsArray[y-1][x] == SHOT_EMPTY )
            return true;
        else if ( IsPosInBoard(x,y+1) && _shotsArray[y+1][x] == SHOT_EMPTY )
            return true;
        else
            return false;
    }

    //returns true if all objects in the board have the specified status
    public boolean checkObjectsStatus(String status, Vector<BoardObject> _board)
    {
        for (int i=0 ; i<_board.size() ; i++)
        {
            if ( _board.get(i).checkStatus(status) == false )
                return false;
        }
        return  true;
    }


    //if _flag == true , it returns the object with the larger size
    //if _flag == false , it returns the object with the smaller size
    private BoardObject compareObjectSize(BoardObject _b1,BoardObject _b2,boolean _flag)
    {
        if ( _b1.getSize() > _b2.getSize() )
        {
            if ( _flag ) return _b1;
            else return _b2;
        }
        else
        {
            if ( _flag ) return _b2;
            else return _b1;
        }
    }

    //Returns true if the given board objects , occupy the same position
    public boolean checkPosObjectOverride(BoardObject _b1,BoardObject _b2)
    {
        BoardObject b_l,b_s; //object large , object small
        b_l = compareObjectSize(_b1,_b2,true);
        b_s = compareObjectSize(_b1,_b2,false);

        //In the loop we put the smaller size object as limit
        //The reason for this is , so that the loop below won't go out of array bounds
        for (int i=0 ; i < b_s.getSize() ; i++) {
            if (b_l.checkPos(b_s.getPos()[i][0],b_s.getPos()[i][1]) == true  )
                return true;
        }
        return false;
    }

    //returns the object that is at these coordinates in the specified vector
    public BoardObject getObject(int x,int y, Vector<BoardObject> _board){

        BoardObject res_bo;//return object
        res_bo = null;
        for (int i=0 ; i<_board.size() ; i++ )
        {
            if (_board.get(i).checkPos(x,y)) //if i object exist at these coordinates , return it
            {
                res_bo = _board.get(i);
                break;
            }
        }
        return res_bo;
    }

    //### Used for DEBUGGING ###//
    public void LogBoardPos( Vector<BoardObject> _board)
    {
        for (int i=0 ; i<_board.size() ; i++ )
        {
            _board.get(i).LogShowPositions();
        }
    }


    //checks if the position given is inside the bounds/dimentions of the board
    //returns true if the position is inside the board , otherwise false
    public boolean IsPosInBoard(int x ,int y){
        if ( x < dimX && y < dimY && x >= 0 && y >= 0)
            return true;
        else
            return false;
    }

    //checks if the object given is inside the bounds/dimentions of the board
    //returns true if the object is inside the board , otherwise false
    public boolean IsPosInBoard(BoardObject _b){
        for (int i=0; i<_b.getSize() ; i++)
        {
            if ( IsPosInBoard(_b.getPos()[i][0],(_b.getPos()[i][1])) == false )
                return false;
        }
            return true;
    }



    //if selection is true , it will return the player's Board , else the enemy board
    public Vector getBoard(int sel)
    {
        if ( sel == PLAYER_BOARD)
            return playerBoard;
        else if ( sel == ENEMY_BOARD )
            return enemyBoard;
        else
            return null;
    }

    public void setEnemyBoard(Vector<BoardObject> enemyBoard) {
        this.enemyBoard = enemyBoard;
    }

    public void setPlayerBoard(Vector<BoardObject> playerBoard) {
        this.playerBoard = playerBoard;
    }


    public int getBoardStatus() { return BoardStatus; }

    public int getBoardStatusInverse(){
        if ( BoardStatus == PLAYER_BOARD)
            return ENEMY_BOARD;
        else
           return PLAYER_BOARD;
    }

    public void setBoardStatus(int boardStatus) {
        BoardStatus = boardStatus;
        drawBoard.invalidate();
    }

    public void invertBoardStatus()
    {
        if ( BoardStatus == PLAYER_BOARD)
            setBoardStatus(ENEMY_BOARD);
        else
            setBoardStatus(PLAYER_BOARD);
    }

    public int getDimX() {
        return dimX;
    }

    public void setDimX(int dimX) {
        this.dimX = dimX;
    }

    public int getDimY() {
        return dimY;
    }

    public void setDimY(int dimY) {
        this.dimY = dimY;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int PHASE) {
        this.phase = PHASE;
    }

    //changes the current phase to the next one
    //parameter my_turn if true , the next phase(after the first one) will be PHASE_PLAYER_ATTACK
    //in simple words is used to check the order (which attacks and which one receives the attack)
    public void nextPhase(){
        if ( phase == PHASE_PLAN )
            phase = PHASE_PLAYER_ATTACK;
        else if ( phase == PHASE_PLAYER_ATTACK )
            phase = PHASE_ENEMY_ATTACK;
        else if ( phase == PHASE_ENEMY_ATTACK )
            phase = PHASE_PLAYER_ATTACK;
        else
            phase = PHASE_NULL;
    }

    public int getPhaseStatus() {
        return phaseStatus;
    }

    public void setPhaseStatus(int phaseStatus) {
        this.phaseStatus = phaseStatus;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DrawBoard getDrawBoard() {
        return drawBoard;
    }

    public void setDrawBoard(DrawBoard drawBoard) {
        this.drawBoard = drawBoard;
    }

    public char[][] getShotsPlayer() {
        return shotsPlayer;
    }

    public void setShotsPlayer(char[][] shotsPlayer) {
        this.shotsPlayer = shotsPlayer;
    }

    public char[][] getShotsEnemy() {
        return shotsEnemy;
    }

    public void setShotsEnemy(char[][] shotsEnemy) {
        this.shotsEnemy = shotsEnemy;
    }


    public char[][] getShots(int board){

        if ( board == PLAYER_BOARD)
            return shotsEnemy;
        else
            return shotsPlayer;

    }

    public String getShipToAdd() {
        return shipToAdd;
    }

    public void setShipToAdd(String shipToAdd) {
        this.shipToAdd = shipToAdd;
    }

    public int getShipToAddRotation() {
        return shipToAddRotation;
    }

    public void setShipToAddRotation(int shipToAddRotation) {
        this.shipToAddRotation = shipToAddRotation;
    }
}
