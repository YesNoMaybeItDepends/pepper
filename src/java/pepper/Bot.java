package pepper;

import bwapi.BWClient;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;
import bwapi.Position;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class Bot extends DefaultBWListener {
    public BWClient bwClient;
    private IFn handler;
    
    public static void main(String[] args) {
        Bot bot = new Bot();
        bot.bwClient = new BWClient(bot);
        bot.bwClient.startGame();
    }

    @Override
    public void onStart() {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("pepper.core"));
        
        IFn initFn = Clojure.var("pepper.core", "java-init");
        initFn.invoke(bwClient);

        handler = Clojure.var("pepper.core", "handle-java-res!");
        handler.invoke("onStart");
    }

    @Override
    public void onEnd(boolean isWinner) {
        handler.invoke("onEnd", isWinner);
    }

    @Override
    public void onFrame() {
        handler.invoke("onFrame");
    }

    @Override
    public void onSendText(String text) {
        handler.invoke("onSendText", text);
    }

    @Override
    public void onReceiveText(Player player, String text) {
        handler.invoke("onReceiveText", player, text);
    }

    @Override
    public void onPlayerLeft(Player player) {
        handler.invoke("onPlayerLeft", player);
    }

    @Override
    public void onNukeDetect(Position target) {
        handler.invoke("onNukeDetect", target);
    }

    @Override
    public void onUnitDiscover(Unit unit) {
        handler.invoke("onUnitDiscover", unit);
    }

    @Override
    public void onUnitEvade(Unit unit) {
        handler.invoke("onUnitEvade", unit);
    }

    @Override
    public void onUnitShow(Unit unit) {
        handler.invoke("onUnitShow", unit);
    }

    @Override
    public void onUnitHide(Unit unit) {
        handler.invoke("onUnitHide", unit);
    }

    @Override
    public void onUnitCreate(Unit unit) {
        handler.invoke("onUnitCreate", unit);
    }

    @Override
    public void onUnitDestroy(Unit unit) {
        handler.invoke("onUnitDestroy", unit);
    }

    @Override
    public void onUnitMorph(Unit unit) {
        handler.invoke("onUnitMorph", unit);
    }

    @Override
    public void onUnitRenegade(Unit unit) {
        handler.invoke("onUnitRenegade", unit);
    }

    @Override
    public void onSaveGame(String gameName) {
        handler.invoke("onSaveGame", gameName);
    }

    @Override
    public void onUnitComplete(Unit unit) {
        handler.invoke("onUnitComplete", unit);
    }
    
    @Override
    public void onPlayerDropped(Player player) {
        handler.invoke("onPlayerDropped", player);
    }
}