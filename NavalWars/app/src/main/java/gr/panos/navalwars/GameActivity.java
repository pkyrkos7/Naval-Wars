package gr.panos.navalwars;


import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class GameActivity extends AppCompatActivity {

    private static String BUTTON_TEXT_ENEMY_BOARD = "Enemy Board";
    private static String BUTTON_TEXT_MY_BOARD =    "Your Board ";
    private static String BUTTON_TEXT_ADD_SHIP = "Add Ship";
    private static String BUTTON_TEXT_SHOOT = "Shoot";
    private static String TEXT_SWITCH_TO = "Switch to";


    private static int SND_SHOT =  R.raw.cannon;
    private static int SND_SHOT_OBJECT = R.raw.cannon_hit_wood;
    private static int SND_WATER_SPLASH = R.raw.water_splash;

    private static int MUSIC_BATTLE01 = R.raw.battle01;

    private static String MESSAGE_VICTORY = "Congratulations you have won!";
    private static String MESSAGE_DEFEAT = "I am sorry but your opponent has bested you!";

    private static int BOARD_GAME_DIMS = 10;

    private List<String> listSpinnerShips;

    private GameBoard boardPlayer;
    private ArrayAdapter<String> addShipAdapter;

    private AIController enemyAI;
    private boolean player_turn; //is true if the it's players turn  - used to control who plays

    MediaPlayer mPlayer;

    Spinner addShipSpinner;
    Button leftButton01, rotateShipButton , randomPlanButton;


    Button swapBoardButton;

    RelativeLayout rl;
    TextView tv1,tv2;
    FrameLayout boardFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //<< this
        setContentView(R.layout.activity_game);

        InitViewComponents(); //this will make the proper assignments for the objects in view , such as buttons

        initRoom();
        playMusic(MUSIC_BATTLE01, true);
        playPhase();

        new Thread() //this thread is responsible for checking when a phase has been completed
        {
            public void run() {
                while (true) {

                    if ( IsPhaseCompleted() )
                    {
                        //set phase status to incompleted
                        setPhasesStatus(GameBoard.PHASE_STATUS_INCOMPLETE);
                        nextPhase();
                        playPhase();
                    }
                }
            }
        }.start();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPlayer.pause();
    }


    @Override
    public void onResume() {
        super.onResume();
        mPlayer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlayer.stop();
        mPlayer = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_game);

        } else {
            setContentView(R.layout.activity_game);
        }
    }


    private void InitViewComponents(){

        addShipSpinner  = (Spinner)  findViewById(R.id.addShipSpinner);

        addShipSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if ( listSpinnerShips.size() > 0 )
                    boardPlayer.setShipToAdd(addShipSpinner.getSelectedItem().toString()); //update variable
                boardPlayer.getDrawBoard().invalidate();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        boardFrameLayout = (FrameLayout)  findViewById(R.id.boardFrameLayout);

        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);

        rl  = (RelativeLayout)  findViewById(R.id.rl);

        leftButton01 = (Button)  findViewById(R.id.addShipButton);
        rotateShipButton = (Button)  findViewById(R.id.rotateShipButton);
        swapBoardButton = (Button)  findViewById(R.id.swapBoardButton);
        randomPlanButton = (Button)  findViewById(R.id.randomPlanButton);

        swapBoardButton.setText(TEXT_SWITCH_TO + " \n" + BUTTON_TEXT_ENEMY_BOARD);

    }

    private void initRoom(){

        //This part creates ships that will be added in the spinner list as well be passed to the AI controller
        String [] shipNames = new String[3]; //multiple of 3
        //amount of ships in the spinner list
        //Add three of each type
        for (int i=0;i<shipNames.length;i++)
        {
            if (i<shipNames.length/3)
                shipNames[i] = GameShip.IdentifyShip(GameShip.SHIP_SMALL_BOAT);
            else if (i<2*shipNames.length/3)
                shipNames[i] = GameShip.IdentifyShip(GameShip.SHIP_SCOUT_SHIP);
            else if (i<3*shipNames.length/3)
                shipNames[i] = GameShip.IdentifyShip(GameShip.SHIP_BIG_SHIP);

        }

        addSpinnerShips(shipNames);

        boardPlayer = new GameBoard(this,BOARD_GAME_DIMS,BOARD_GAME_DIMS,tv1,tv2);

        boardFrameLayout.removeAllViews();
        boardFrameLayout.addView(boardPlayer.getDrawBoard(),0);

        //create the AI now
        enemyAI = new AIController(BOARD_GAME_DIMS,BOARD_GAME_DIMS,shipNames);

        setPhases(GameBoard.PHASE_PLAN);
        player_turn = true;

        //===========================


        leftButton01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftButton01Function();
            }
        });

        swapBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapBoardButtonFunction();
            }
        });

        rotateShipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //used to change the rotation of the ship to be added
                rotateShipButtonFunction();
            }
        });

        randomPlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //used to change the rotation of the ship to be added
                randomPlanButtonFunction();
            }
        });

    }

    public void playMusic(int id,boolean loop)
    {
        mPlayer = MediaPlayer.create(getApplicationContext(),id);
        mPlayer.setLooping(loop);
        mPlayer.start();
    }

    public void playSound(int id)
    {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(),id);
        mp.start();
    }


    public void addSpinnerShips(String[] names){
        //add to the Spinner the ship names
        listSpinnerShips =new ArrayList();

        for (int i=0;i<names.length;i++)
        {
            listSpinnerShips.add(names[i]);
        }
        addShipAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, listSpinnerShips);
        addShipSpinner.setAdapter(addShipAdapter);
    }

    public void removeSpinnerShip(int shipToRemove) {
        //if the spinner contains items
        if ( listSpinnerShips.size() > 0 ) {
           //Debug , show what ship we will remove from spinner
           // Toast.makeText(getApplicationContext(),addShipSpinner.getItemAtPosition(shipToRemove).toString(), Toast.LENGTH_SHORT).show();
            listSpinnerShips.remove(shipToRemove); //remove ship name from the list
            addShipAdapter.notifyDataSetChanged(); //update the adapter so that the spinner can change
            if (listSpinnerShips.size() > 0) //if there is at least one time in the spinner then set selection to the first element
                addShipSpinner.setSelection(0);
        }
        //if the spinner has no items -->This will only happen when the spinner is empty
        if ( listSpinnerShips.size() <= 0 ) {
            addShipSpinner.setEnabled(false); //disable the Spinner
            addShipSpinner.setVisibility(View.INVISIBLE);
            leftButton01.setEnabled(false);  //disable the Button that "handles" the Spinner
            rotateShipButton.setEnabled(false);
            rotateShipButton.setVisibility(View.INVISIBLE);
            leftButton01.setVisibility(View.INVISIBLE);
            boardPlayer.setShipToAdd("");
            randomPlanButton.setEnabled(false);
            randomPlanButton.setVisibility(View.INVISIBLE);
        }
    }




    public void swapBoardButtonFunction() {
        boardPlayer.invertBoardStatus(); //invert the board (player's - enemy)
        if ( boardPlayer.getBoardStatus() == boardPlayer.PLAYER_BOARD) //if player board is visible
        {
            swapBoardButton.setText(TEXT_SWITCH_TO+" \n"+BUTTON_TEXT_ENEMY_BOARD);
            if ( listSpinnerShips.size()>0 ) //if there are ships in the list enable
            {
                leftButton01.setText(BUTTON_TEXT_ADD_SHIP);
                leftButton01.setEnabled(true);
                rotateShipButton.setEnabled(true);
                rotateShipButton.setVisibility(View.VISIBLE);
                addShipSpinner.setEnabled(true);
                addShipSpinner.setVisibility(View.VISIBLE);
            }
            else
            {
                leftButton01.setEnabled(false);  //disable the Button that "handles" the Spinner
                rotateShipButton.setEnabled(false);
                rotateShipButton.setVisibility(View.INVISIBLE);
                leftButton01.setVisibility(View.INVISIBLE);
            }
        }
        else //if enemy board is visible
        {
            swapBoardButton.setText(TEXT_SWITCH_TO+" \n"+BUTTON_TEXT_MY_BOARD);

            leftButton01.setText(BUTTON_TEXT_SHOOT);
            leftButton01.setVisibility(View.VISIBLE);
            //if player's turn to shoot , enable this button
            if ( player_turn == true && boardPlayer.getPhase() == GameBoard.PHASE_PLAYER_ATTACK )
                leftButton01.setEnabled(true);
            else
                leftButton01.setEnabled(false);

            rotateShipButton.setEnabled(false);
            rotateShipButton.setVisibility(View.INVISIBLE);

            if ( listSpinnerShips.size()>0 ) {
                addShipSpinner.setEnabled(false);
                addShipSpinner.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void rotateShipButtonFunction() {
        boardPlayer.setShipToAddRotation((boardPlayer.getShipToAddRotation() + 90) % 360);
        //###DEBUG
        //  Toast.makeText(getApplicationContext(), "New Rotation : "+boardPlayer.getShipToAddRotation(), Toast.LENGTH_SHORT).show();
        boardPlayer.getDrawBoard().invalidate();//update canvas
    }

    public void leftButton01Function(){

        int tmp_x, tmp_y, tmp_rot, tmp_id;
        //used in both cases
        tmp_x = boardPlayer.getDrawBoard().getSelectedTileX();
        tmp_y = boardPlayer.getDrawBoard().getSelectedTileY();

        if (boardPlayer.getBoardStatus() == boardPlayer.PLAYER_BOARD) // in this case the button will add ships
        {
            GameShip tmp_ship;
            tmp_rot = boardPlayer.getShipToAddRotation();
            tmp_ship = new GameShip();
            tmp_id = tmp_ship.IdentifyShip(addShipSpinner.getSelectedItem().toString());

            if (tmp_id == -1) { //if the String from Spinner doesn't any ship type
                Toast.makeText(getApplicationContext(), "The selected ship doesn't exist in the database!", Toast.LENGTH_LONG).show();
            } else {
                //boardPlayer.setShipToAdd(addShipSpinner.getSelectedItem().toString()); //update variable
                tmp_ship.setShip(tmp_x, tmp_y, tmp_rot, tmp_id, BoardObject.BOARD_OBJECT_STATUS_ALIVE);
                //if object is successfully added
                if (boardPlayer.addObject(tmp_ship, boardPlayer.getBoard(boardPlayer.PLAYER_BOARD)) == true) {
                    //###DEBUG
                    //Toast.makeText(getApplicationContext(), "I am adding the ship " + tmp_ship.getName(), Toast.LENGTH_SHORT).show();
                    boardPlayer.getDrawBoard().invalidate();
                    removeSpinnerShip(addShipSpinner.getSelectedItemPosition()); //remove selected
                    if (listSpinnerShips.size() > 0)
                        boardPlayer.setShipToAdd(listSpinnerShips.get(0));
                    else
                        boardPlayer.setPhaseStatus(GameBoard.PHASE_STATUS_COMPLETED); //update phase
                }
                //Toast.makeText(getApplicationContext(), "Position not free!" + boardPlayer.getBoardStatus(), Toast.LENGTH_SHORT).show();
            }
        }
        else  // in this case the button will shoot since we are viewing the enemy board
        {
                // player's turn and proper phase and the position it's empty
                if ( player_turn == true && boardPlayer.getPhase() != GameBoard.PHASE_PLAN ) {
                    if ( boardPlayer.getShots(GameBoard.ENEMY_BOARD)[tmp_y][tmp_x] == GameBoard.SHOT_EMPTY )
                    {
                        boardPlayer.setShot(tmp_x,tmp_y,GameBoard.SHOT_HIT,GameBoard.ENEMY_BOARD);
                        addShot(tmp_x,tmp_y,false);
                        setPhasesStatus(GameBoard.PHASE_STATUS_COMPLETED);
                        boardPlayer.getDrawBoard().invalidate();
                        leftButton01.setEnabled(false);
                    }
                }
            //###DEBUG
            //Toast.makeText(getApplicationContext(), "I am shooting", Toast.LENGTH_SHORT).show();
        }
        //###DEBUG
        //Toast.makeText(getApplicationContext(), "Pos "+tmp_x+","+tmp_y, Toast.LENGTH_SHORT).show();
    }


    public void randomPlanButtonFunction(){

        String[] str = new String[1];
        while ( listSpinnerShips.size() > 0 ) {
            str[0] = addShipSpinner.getSelectedItem().toString();
            boardPlayer.planPlayerShipsRandom(str);
            removeSpinnerShip(addShipSpinner.getSelectedItemPosition()); //remove selected
        }
        boardPlayer.setPhaseStatus(GameBoard.PHASE_STATUS_COMPLETED); //update phase
        randomPlanButton.setEnabled(false);
        randomPlanButton.setVisibility(View.INVISIBLE);
        boardPlayer.getDrawBoard().invalidate();
    }

    //this function is responsible for updating the shot arrays in players
    private void addShot(int x,int y,boolean from_enemy)
    {
        char shot_type;
        if ( from_enemy == false ) { //player shooting
            //if an object exist in that position
            if (enemyAI.getBoard().getObject(x, y, enemyAI.getBoard().getBoard(GameBoard.PLAYER_BOARD)) != null) {
                enemyAI.getBoard().getObject(x, y, enemyAI.getBoard().getBoard(GameBoard.PLAYER_BOARD)).setPosStatus(x, y, BoardObject.BOARD_OBJECT_STATUS_DEAD);
                shot_type = GameBoard.SHOT_OBJECT;
                Log.d("Player","Player hits! At :"+x+","+y);
                playSound(SND_SHOT_OBJECT);
            } else {
                shot_type = GameBoard.SHOT_HIT;
                playSound(SND_SHOT);
            }

            boardPlayer.setShot(x,y,shot_type,GameBoard.ENEMY_BOARD); //update the player array as well
            enemyAI.getBoard().setShot(x,y,shot_type,GameBoard.PLAYER_BOARD); //update the AI array as well
        }
        else{ //enemy shooting
            //if an object exist in that position
            if (boardPlayer.getObject(x, y, boardPlayer.getBoard(GameBoard.PLAYER_BOARD)) != null ) {
                boardPlayer.getObject(x, y, boardPlayer.getBoard(GameBoard.PLAYER_BOARD)).setPosStatus(x, y, BoardObject.BOARD_OBJECT_STATUS_DEAD);
                shot_type = GameBoard.SHOT_OBJECT;

                enemyAI.updateLastHit(enemyAI.getAttackX(),enemyAI.getAttackY()); //update last valid hit variables for AI
                Log.d("AI", "AI hits! At :" + x + "," + y);

                final int _x,_y;
                _x = x;
                _y = y;
                Handler h = new Handler(Looper.getMainLooper());
                h.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "AI shot your " + boardPlayer.getObject(_x, _y, boardPlayer.getBoard(GameBoard.PLAYER_BOARD)).getName() + " at " + _y + "," + _x, Toast.LENGTH_LONG).show();
                    }
                });


                playSound(SND_SHOT_OBJECT);
            } else
                shot_type = GameBoard.SHOT_HIT;

            boardPlayer.setShot(x,y,shot_type,GameBoard.PLAYER_BOARD);//update the player array as well
            enemyAI.getBoard().setShot(x,y,shot_type,GameBoard.ENEMY_BOARD); //update the AI array as well
        }


        //Gets Interrupted !
        /*
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            public void run() {
                //wait 1 second
                new CountDownTimer(200, 50) {

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        playSound(SND_WATER_SPLASH);
                    }
                }.start();
            }
        });
        */



    }



    private void playPhase()
    {

        if (enemyAI.playMove() == false ) //if for some reason AI has stucked
            Log.d("LogPhase","Enemy AI not working!");//throw new RuntimeException("AI doesn't seem to work!");
        if ( boardPlayer.getPhase() == GameBoard.PHASE_PLAN )
        {
            //###DEBUG
            Log.d("LogPhase", "PHASE_SHIP_PLAN");
            // should be only used for debugging
            //boardPlayer.setEnemyBoard(enemyAI.getBoard().getBoard(GameBoard.PLAYER_BOARD)); //copies board
        }
        else if ( boardPlayer.getPhase() == GameBoard.PHASE_PLAYER_ATTACK )
        {
            //###DEBUG
            Log.d("LogPhase","PHASE_PLAYER_ATTACK");
        }
        else if ( boardPlayer.getPhase() == GameBoard.PHASE_ENEMY_ATTACK )
        {
            //###DEBUG
            Log.d("LogPhase","PHASE_ENEMY_ATTACK");

            //receive attack from enemy that is valid

            if ( boardPlayer.getShots(GameBoard.PLAYER_BOARD)[enemyAI.getAttackY()][enemyAI.getAttackX()] == GameBoard.SHOT_EMPTY )
            {
                addShot(enemyAI.getAttackX(),enemyAI.getAttackY(),true);
            }
            boardPlayer.setPhaseStatus(GameBoard.PHASE_STATUS_COMPLETED);
            Vibrator v = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
            v.vibrate(100); // Vibrate for 500 milliseconds



            //simple way to change UI
            boardFrameLayout.post(new Runnable() {
                public void run() {
                    /* the desired UI update */
                    swapBoardButtonFunction();//show your board
                }
            });
        }

        checkGameEnding();
    }

    //returns true if the phase currently running is finished for both players
    private boolean IsPhaseCompleted()
    {
        //###DEBUG
        /*
        if ( boardPlayer.getPhaseStatus() == GameBoard.PHASE_COMPLETED )
            Log.d("LogPhase","PLAYER PHASE COMPLETED");
        if ( enemyAI.getBoard().getPhaseStatus() == GameBoard.PHASE_COMPLETED )
            Log.d("LogPhase","ENEMY AI PHASE FINISHED");
        */
        if ( boardPlayer.getPhaseStatus() == GameBoard.PHASE_STATUS_COMPLETED &&
             enemyAI.getBoard().getPhaseStatus() == GameBoard.PHASE_STATUS_COMPLETED )
            return true;
        return false;
    }


    //set for both players (and global), the next phase
    private void nextPhase(){ //changes the current phase to the next one
    if ( boardPlayer.getPhase() != GameBoard.PHASE_PLAN) //if it's not the planning phase , change turn
        player_turn = !player_turn;

    if ( boardPlayer.getPhase() == GameBoard.PHASE_PLAN && enemyAI.getBoard().getPhase() == GameBoard.PHASE_PLAN)
    {//this should happen only once
        boardPlayer.setPhase(GameBoard.PHASE_PLAYER_ATTACK);
        enemyAI.getBoard().setPhase(GameBoard.PHASE_ENEMY_ATTACK);
    }
    else
    {
        boardPlayer.nextPhase();
        enemyAI.getBoard().nextPhase();
    }
        Log.d("LogPhase","Current phase : "+boardPlayer.getPhase());
        Log.d("LogPhase", "Current AI phase : " + enemyAI.getBoard().getPhase());
    }

    //set for both players (and global), the phase status to the given phase status
    private void setPhasesStatus(int status)
    {
        boardPlayer.setPhaseStatus(status);
        enemyAI.getBoard().setPhaseStatus(status);
    }

    //set for both players (and global), a specific phase
    private void setPhases(int phase)
    {
        boardPlayer.setPhase(phase);
        enemyAI.getBoard().setPhase(phase);
    }


    private void checkGameEnding(){

        if ( boardPlayer.getPhase() == GameBoard.PHASE_PLAN || enemyAI.getBoard().getPhase() == GameBoard.PHASE_PLAN )
            return;
        boolean flag = false;
        boolean victory = false;
        //if player has no "Alive" ships left
        if (boardPlayer.checkObjectsStatus(BoardObject.BOARD_OBJECT_STATUS_DEAD, boardPlayer.getBoard(GameBoard.PLAYER_BOARD)) == true ) {
            Log.d("Player","I am dead!");
            victory = false;
            flag = true;
        }
        else if ( enemyAI.getBoard().checkObjectsStatus(BoardObject.BOARD_OBJECT_STATUS_DEAD, enemyAI.getBoard().getBoard(GameBoard.PLAYER_BOARD)) == true ) {
            Log.d("AI","I am dead!");
            victory = true;
            flag = true;
        }
        if ( flag == true ) {
            Vibrator v = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
            v.vibrate(500); // Vibrate for 500 milliseconds
            Log.d("LogPhase","I am in ending");
            new showDialogFinishTask().execute(victory);
        }
    }

    boolean flag = false;
    private class showDialogFinishTask extends AsyncTask<Boolean, Void,Void> {

        boolean victory;


        protected Void doInBackground(Boolean... params) {
            victory = params[0];
            Log.d("LogPhase","I am in doInBackground");
            return null;
        }

        protected void onPreExecute(){
            Log.d("LogPhase","I am in Pre Execute!");

        }

        protected void onPostExecute(Void result) {
            if ( flag == false ) { //for some reason this runs twice if the flag is declared inside this class!
                Log.d("LogPhase","I am in Post Execute!");
                showDialogFinish(victory);
                flag = true;
            }
        }
    }

    private void showDialogFinish(boolean victory)
    {

        String message;
        if ( victory == true )
            message = MESSAGE_VICTORY;
        else
            message = MESSAGE_DEFEAT;

        final Dialog dialog = new Dialog(GameActivity.this);

        //setting custom layout to dialog
        dialog.setContentView(R.layout.dialog_finish);
        dialog.setTitle("Battle Ended!");

        //adding text dynamically
        TextView txt = (TextView) dialog.findViewById(R.id.textView);
        txt.setText(message);

        //adding button click event
        Button dismissButton = (Button) dialog.findViewById(R.id.button);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent myIntent = new Intent(GameActivity.this,MainMenuActivity.class);
                startActivity(myIntent);
                finish();
            }
        });
        if ( !dialog.isShowing() )
        {
            dialog.show();
        }
    }


}
