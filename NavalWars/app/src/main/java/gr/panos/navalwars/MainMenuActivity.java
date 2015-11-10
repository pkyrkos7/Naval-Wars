package gr.panos.navalwars;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainMenuActivity extends ActionBarActivity {


    public void playSound(int id)
    {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(),id);
        mp.start();
    }


    Button startGameButton,settingsButton,exitGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //<< this
        setContentView(R.layout.activity_main_menu);

        startGameButton = (Button)  findViewById(R.id.startGameButton);
        settingsButton = (Button)  findViewById(R.id.settingsButton);
        exitGameButton = (Button)  findViewById(R.id.exitGameButton);


        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(R.raw.cannon);
                Intent myIntent = new Intent(MainMenuActivity.this, GameActivity.class);
                startActivity(myIntent);
            }
        });

        exitGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(R.raw.cannon);
                MainMenuActivity.this.finish();
                System.exit(0);
            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
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
}
