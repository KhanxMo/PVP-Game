package me.dumfing.menus;
//FILENAME
//Aaron Li  4/28/2017
//EXPLAIN

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import me.dumfing.client.maingame.GameState;
import me.dumfing.client.maingame.MainGame;
import me.dumfing.gdxtools.MenuTools;
import me.dumfing.multiplayerTools.MultiplayerTools;

import java.util.HashMap;

import static me.dumfing.client.maingame.MainGame.*;


public class ServerBrowser extends Menu{
    private ServerBrowserList serverList;
    /**
     * Constructor for the menu
     *
     * @param bmfc    BitMapFontCache for drawing all text in the menu
     * @param manager
     * @param camera
     */
    public ServerBrowser(Array<BitmapFontCache> bmfc, AssetManager manager, OrthographicCamera camera) {
        super(bmfc, manager, camera);
    }

    /**
     * Initializes the ServerBrowser
     */
    public void init() {
        //super.init();
        super.setBackground(MainGame.background);
        float serverListHeight = 3000;
        serverList = new ServerBrowserList(Gdx.graphics.getWidth()-400, Gdx.graphics.getHeight() - serverListHeight, 400, serverListHeight, super.getFonts(), this.getManager());
        super.addMenuBox(serverList);
        addRefreshButton();
        addBackButton();
        addOfflineButton();
        MenuTools.TextField directConnect = new MenuTools.TextField(5,5,400,40, "Server IP");
        directConnect.setEnterAction(new MenuTools.OnEnter() {
            @Override
            public void action(String sIn) {
                MainGame.client.connectServerPlay(sIn);
                MainGame.state = GameState.State.CONNECTINGTOSERVER;
            }
        });
        super.addTextField(directConnect);
    }
    private void addBackButton(){
        MenuTools.Button bt = new MenuTools.Button(5,Gdx.graphics.getHeight()-65,60,60);
        bt.setCallback(new MenuTools.OnClick() {
            @Override
            public void action() {
                MainGame.state = GameState.State.MAINMENU;
            }
        });
        bt.setPressedTexture(MainGame.backPres);
        bt.setUnpressedTexture(MainGame.backUn);
        super.addButton(bt);
    }
    private void addRefreshButton(){
        MenuTools.Button refreshServers = new MenuTools.Button(Gdx.graphics.getWidth()-465,Gdx.graphics.getHeight()-65,60,60);
        refreshServers.setCallback(new MenuTools.OnClick() {
            @Override
            public void action() {
                if(!client.isFindingServers()) {
                    MainGame.client.pingServers();
                }
            }
        });
        refreshServers.setPressedTexture(MainGame.refreshPress);
        refreshServers.setUnpressedTexture(MainGame.refreshUn);
        super.addButton(refreshServers);

    }
    private void addOfflineButton(){
        MenuBox offlineButton = MenuTools.createLabelledButton(5, 60, 300, 60, "[WHITE]Offline", new MenuTools.OnClick() {
            @Override
            public void action() {
                MainGame.state = GameState.State.OFFLINEDEBUG;
            }
        },MainGame.bigButtonPress,MainGame.bigButtonUn,getFonts(),DAGGER20);
        super.addMenuBox(offlineButton);
    }
    public boolean scrolled(int amount) {
        serverList.onScroll(amount);
        return super.scrolled(amount);
    }
    public void populateServerList(HashMap<String, MultiplayerTools.ServerSummary> servers){
        this.serverList.refreshServers(servers);
    }
    public void draw(SpriteBatch sb, ShapeRenderer sr) {
        super.standardDraw(sb,sr);
        serverList.draw(sb,sr);
    }
    private class ServerBrowserList extends MenuBox{
        //private String[] pingAmounts = {"CYAN","GREEN","YELLOW","GOLD","ORANGE","RED"};
        private AssetManager assets;
        public ServerBrowserList(float x ,float y, float w, float h, Array<BitmapFontCache> fonts,AssetManager assets){
            super(x,y,w,h,fonts);
            this.assets = assets;
        }
        public void refreshServers(final HashMap<String, MultiplayerTools.ServerSummary> serverList){
            int btHeight = 60; //Height of all buttons to be added
            int bNum = 0;
            super.clearButtons();
            super.clearText();
            for(final String k : serverList.keySet()){
                float btPosY = bNum*btHeight;
                MultiplayerTools.ServerSummary svInfo = serverList.get(k);
                String tOut = String.format("%s%d|%d [%s]%5d",(svInfo.num>=svInfo.max?"[RED]":""),svInfo.num,svInfo.max,ratePing(svInfo.ping),svInfo.ping);
                MenuTools.Button bt= new MenuTools.Button(0,super.getRect().getHeight()-btPosY-btHeight,super.getRect().getWidth(),btHeight);
                MenuTools.QueueText sName = new MenuTools.QueueText(5,super.getRect().getHeight()-btPosY-btHeight/2+7,0,0);
                MenuTools.QueueText peopleLimit = new MenuTools.QueueText(super.getRect().getWidth()-MenuTools.textWidth(getFonts().get(DAGGER30).getFont(),tOut)-2,super.getRect().getHeight()-btPosY-btHeight/2+7, 0,0);
                peopleLimit.setFont(DAGGER30);
                sName.setFont(DAGGER30);
                peopleLimit.setText(tOut,super.getFontCaches());
                sName.setText(serverList.get(k).serverName,super.getFontCaches());
                bt.setPressedTexture(MainGame.bigButtonPress);//new TextureRegion((Texture)assets.get("4914003-galaxy-wallpaper-png.png")));
                bt.setUnpressedTexture(MainGame.bigButtonUn);//new TextureRegion((Texture)assets.get("Desktop.jpg")));
                bt.setCallback(new MenuTools.OnClick() {
                    @Override
                    public void action() {
                            System.out.println(k);
                            String svIP = k;
                            svIP = svIP.replace("/", "").substring(0, svIP.indexOf(":") - 1); // the received ip is in the form "/ip:port", we only need the ip part so we remove the / and the :port
                            System.out.println(svIP);
                            MainGame.client.connectServerPlay(svIP);
                            MainGame.state = GameState.State.CONNECTINGTOSERVER;

                    }
                });
                super.addQueueText(peopleLimit);
                super.addQueueText(sName);
                super.addButton(bt);
                bNum++;
            }
        }

        public void onScroll(int amount){
            System.out.println(this.getRect()+" "+amount);
            if(amount>0){
                if(this.getRect().getY()<0){// <= Gdx.graphics.getHeight()-this.getRect().getHeight()){
                    this.translate(0,5*amount);
                }
            }
            else{
                if(this.getRect().getY() > Gdx.graphics.getHeight()-this.getRect().getHeight()){
                    this.translate(0,5*amount);
                }
            }
        }
        public String ratePing(int ping){
            if(ping<=10){
                return "CYAN";
            }
            else if(ping <= 30){
                return "GREEN";
            }
            else if(ping <=50){
                return "YELLOW";
            }
            else if(ping<=100){
                return "GOLD";
            }
            else if(ping<=150){
                return"ORANGE";
            }
            else{
                return "RED";
            }
        }
    }
}
