package gr.panos.navalwars;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

/**
 * Created by Panos on 6/8/2015.
 */
public class GameShip extends BoardObject {


    protected static int SpriteSmallBoat =  R.drawable.ship_64x64;  // ship's sprite/image to be loaded from resources
    protected static int SpriteScoutShip =  R.drawable.ship_64x128;  // ship's sprite/image to be loaded from resources
    protected static int SpriteBigShip =  R.drawable.ship_64x192;  // ship's sprite/image to be loaded from resources

    private static String NAME_SMALL_BOAT = "Small Boat";
    private static String NAME_SCOUT_SHIP = "Scout Ship";
    private static String NAME_BIG_SHIP   = "Big Ship";

    public static int SHIP_SMALL_BOAT = 1;
    public static int SHIP_SCOUT_SHIP = 2;
    public static int SHIP_BIG_SHIP = 3;


    public GameShip(){
        setSmallBoat(0,0,0);//apply the attributes of the small boat - default
    }


    //this constructor will initialize a new ship given the selected coordinates ,rotation and type
    public GameShip(int x ,int y,int rot,int ship_type,String status){
        setShip(x,y,rot,ship_type,status);
    }

    // change the attributes of the object to match the ship_type
    public void setShip(int x ,int y,int rot,int ship_type,String status){
        if ( ship_type == SHIP_SMALL_BOAT )
            setSmallBoat(x,y,rot);
        else if ( ship_type == SHIP_SCOUT_SHIP )
            setScoutShip(x,y,rot);
        else if ( ship_type == SHIP_BIG_SHIP )
            setBigShip(x,y,rot);
        else
            setSmallBoat(x,y,rot); //default choice
        setStatus(status); //object's statues e.g. alive , destroyed , damaged..
    }


    public void setSmallBoat(int x,int y,int rot){
        setPosX(x);
        setPosY(y);
        setRotation(rot);
        setSize(1);
        setSprite(SpriteSmallBoat);
        setName(NAME_SMALL_BOAT);
        setType("default");
    }

    public void setScoutShip(int x,int y,int rot){
        setPosX(x);
        setPosY(y);
        setRotation(rot);
        setSize(2);
        setSprite(SpriteScoutShip);
        setName(NAME_SCOUT_SHIP);
        setType("default");
    }

    public void setBigShip(int x,int y,int rot){
        setPosX(x);
        setPosY(y);
        setRotation(rot);
        setSize(3);
        setSprite(SpriteBigShip);
        setName(NAME_BIG_SHIP);
        setType("default");
    }

    //This is used to match the given name with the appropriate ship
    public static int IdentifyShip(String name)
    {
        if (name.equals(NAME_SMALL_BOAT))
            return SHIP_SMALL_BOAT;
        else if (name.equals(NAME_SCOUT_SHIP))
            return SHIP_SCOUT_SHIP;
        else if (name.equals(NAME_BIG_SHIP))
            return SHIP_BIG_SHIP;
        else
            return -1;
    }

    //This is used to match the ship type with the appropriate name
    public static String IdentifyShip(int id)
    {
        if ( id == SHIP_SMALL_BOAT )
            return NAME_SMALL_BOAT;
        else if ( id == SHIP_SCOUT_SHIP )
            return NAME_SCOUT_SHIP;
        else if ( id == SHIP_BIG_SHIP )
            return NAME_BIG_SHIP;
        else
            return "";
    }

    public static int getShipSprite(String name)
    {
        if (name.equals(NAME_SMALL_BOAT))
            return SpriteSmallBoat;
        else if (name.equals(NAME_SCOUT_SHIP))
            return SpriteScoutShip;
        else if (name.equals(NAME_BIG_SHIP))
            return SpriteBigShip;
        else
            return -1;
    }

}
