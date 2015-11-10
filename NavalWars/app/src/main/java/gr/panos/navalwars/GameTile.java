package gr.panos.navalwars;

import android.content.Context;
import android.graphics.BitmapFactory;

/**
 * Created by Panos on 6/8/2015.
 */

//this is a game tile e.g. ( land , sea  )
public class GameTile extends BoardObject {


    public GameTile(){
        setPosX(0);
        setPosY(0);
        setRotation(0);
        setSize(1);
        setSprite(-1);
        setName("Small Boat");
        setType("default");
    }






}
