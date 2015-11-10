package gr.panos.navalwars;

import android.util.Log;

import java.util.Random;

/**
 * Created by Panos on 6/23/2015.
 */
public class AIController {

    private GameBoard board;

    private int attackX;
    private int attackY;
    private int attackX_hit;
    private int attackY_hit;

    private static int MAX_TRIES_SHOOT= 20;

    private String[] ships_toAdd;

    //the 1st parameter is a string array that contains all the ships that will be added (Based on the names given)
    public AIController(int DimX,int DimY,String[] ships){
        board = new GameBoard(DimX,DimY);
        attackX_hit = -1;
        attackY_hit = -1;
        ships_toAdd = ships.clone();
    }



    //this is called at phase 1 , so that the AI can plan his ships (where to place them)
    //the parameter is a string array that contains all the ships that will be added (Based on the names given)
    //returns the position of the first string that doesn't exist as a ship name - if all ships are added -1 is returned
    private int planShips(String[] ships){
        return board.planPlayerShipsRandom(ships);
    }

    //returns true if success
    public boolean playMove(){

        if ( board.getPhaseStatus() == GameBoard.PHASE_STATUS_COMPLETED ) //if the method is accidentally called and the phase is done exit
            return false;
        else
        {
            if (board.getPhase() == GameBoard.PHASE_PLAN) //AI Planning phase
            {
                if ( planShips(ships_toAdd) != -1 )
                    return false;

            }
            else if (board.getPhase() == GameBoard.PHASE_PLAYER_ATTACK) {
                Random rand = new Random();
                int tries = 0;
                int dir;
                Log.d("AI", "AI thinking"); //if ships are sticked together AI will get confused at this version!
                board.logShotArray(GameBoard.ENEMY_BOARD);
                Log.d("AI", "Prev Attack at :" + attackX_hit + "," + attackY_hit);
                do {
                    //if there's a successful last hit and has valid value and there's at least one free tile nearby
                    if ( attackX_hit != -1 && attackY_hit != -1 && board.IsPosInBoard(attackX_hit,attackY_hit) &&
                            board.checkPosShotNearbyFree(attackX_hit,attackY_hit,GameBoard.ENEMY_BOARD))
                    {
                        //this is used to set a specific direction to attack based on the previous hit (if object)
                        if (board.checkPosShot(attackX_hit, attackY_hit, 0, GameBoard.ENEMY_BOARD) == GameBoard.SHOT_OBJECT&&
                                board.checkPosShot(attackX_hit, attackY_hit, 180, GameBoard.ENEMY_BOARD) == GameBoard.SHOT_EMPTY)
                        {
                            dir = 180;
                            Log.d("AI", "AI shoted right "+(attackX_hit+1)+","+attackY_hit);
                        } else if ( board.checkPosShot(attackX_hit, attackY_hit, 180, GameBoard.ENEMY_BOARD) == GameBoard.SHOT_OBJECT &&
                                board.checkPosShot(attackX_hit, attackY_hit, 0, GameBoard.ENEMY_BOARD) == GameBoard.SHOT_EMPTY)
                        {
                            dir = 0;
                            Log.d("AI", "AI shoted left "+(attackX_hit-1)+","+attackY_hit);
                        }
                        else if ( board.checkPosShot(attackX_hit, attackY_hit,90,GameBoard.ENEMY_BOARD) == GameBoard.SHOT_OBJECT &&
                                board.checkPosShot(attackX_hit, attackY_hit, 270, GameBoard.ENEMY_BOARD) == GameBoard.SHOT_EMPTY)
                        {
                            dir = 270;
                            Log.d("AI", "AI shoted up "+attackX_hit+","+(attackY_hit-1));
                        }
                        else if ( board.checkPosShot(attackX_hit, attackY_hit,270,GameBoard.ENEMY_BOARD) == GameBoard.SHOT_OBJECT &&
                            board.checkPosShot(attackX_hit, attackY_hit, 90, GameBoard.ENEMY_BOARD) == GameBoard.SHOT_EMPTY)
                        {
                            dir = 90;
                            Log.d("AI", "AI shoted down "+attackX_hit+","+(attackY_hit+1));
                        }
                        else
                            dir = rand.nextInt(4)*90; //get a random direction to attack next
                        switch (dir) {
                            case 0: //right
                                // if right hasn't been shot
                                if ( board.checkPosShot(attackX_hit, attackY_hit,dir,GameBoard.ENEMY_BOARD) == GameBoard.SHOT_EMPTY )
                                {
                                    attackX = attackX_hit + 1; //attack right
                                    attackY = attackY_hit;
                                    Log.d("AI", "AI will shoot right "+(attackX_hit+1)+","+attackY_hit);
                                }
                                break;
                            case 180: //left
                                // if left hasn't been shot
                                if ( board.checkPosShot(attackX_hit, attackY_hit,dir,GameBoard.ENEMY_BOARD) == GameBoard.SHOT_EMPTY )
                                {
                                    attackX = attackX_hit - 1; //attack left
                                    attackY = attackY_hit;
                                    Log.d("AI", "AI will shoot left "+(attackX_hit-1)+","+attackY_hit);
                                }
                                break;
                            case 90: //up
                                // if up hasn't been shot
                                if ( board.checkPosShot(attackX_hit, attackY_hit,dir,GameBoard.ENEMY_BOARD) == GameBoard.SHOT_EMPTY )
                                {
                                    attackX = attackX_hit;
                                    attackY = attackY_hit - 1; //attack up
                                    Log.d("AI", "AI will shoot up "+attackX_hit+","+(attackY_hit-1));
                                }
                                break;
                            case 270: //down
                                // if down hasn't been shot
                                if ( board.checkPosShot(attackX_hit, attackY_hit,dir,GameBoard.ENEMY_BOARD) == GameBoard.SHOT_EMPTY )
                                {
                                    attackX = attackX_hit;
                                    attackY = attackY_hit + 1; //attack down
                                    Log.d("AI", "AI will shoot down "+attackX_hit+","+(attackY_hit+1));
                                }
                                break;
                            default:
                                return false; //if for some reason the dir is different than 0-3
                        }
                    }
                    else //if the previous attack wasn't successful , get random coordinates
                    {
                        attackX = rand.nextInt(board.getDimX());
                        attackY = rand.nextInt(board.getDimY());
                        Log.d("AI", "AI will shoot random "+attackX+","+attackY);
                        attackX_hit = -1;
                        attackY_hit = -1;
                    }
                    tries++;
                } while ( board.getShots(GameBoard.ENEMY_BOARD)[attackY][attackX] != GameBoard.SHOT_EMPTY && tries<MAX_TRIES_SHOOT );
                if (MAX_TRIES_SHOOT <= tries ) {
                    Log.d("AI", "AI got crazy!");
                    return false;
                }
                board.setShot(attackX, attackY, GameBoard.SHOT_HIT, GameBoard.ENEMY_BOARD);
                Log.d("AI", "Attack at :" + attackX + "," + attackY);
                Log.d("AI", "Prev Attack at :" + attackX_hit + "," + attackY_hit);
            }
            else if (board.getPhase() == GameBoard.PHASE_ENEMY_ATTACK) {


            }
            board.setPhaseStatus(GameBoard.PHASE_STATUS_COMPLETED);
            return true;
        }
    }

    //these is used to update the x,y variables to the last successful hit coordinates
    public void updateLastHit(int x,int y)
    {
        attackX_hit = x;
        attackY_hit = y;
    }

    public GameBoard getBoard() {
        return board;
    }

    public void setBoard(GameBoard board) {
        this.board = board;
    }

    public int getAttackX() {
        return attackX;
    }

    public void setAttackX(int attackX) {
        this.attackX = attackX;
    }

    public int getAttackY() {
        return attackY;
    }

    public void setAttackY(int attackY) {
        this.attackY = attackY;
    }

    public int getAttackX_hit() {
        return attackX_hit;
    }

    public void setAttackX_hit(int attackX_hit) {
        this.attackX_hit = attackX_hit;
    }

    public int getAttackY_hit() {
        return attackY_hit;
    }

    public void setAttackY_hit(int attackY_hit) {
        this.attackY_hit = attackY_hit;
    }
}
