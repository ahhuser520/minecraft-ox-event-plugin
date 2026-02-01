package com.github.ahhuser.oxevent;

import com.github.ahhuser.oxevent.command.OXCommand;
import com.github.ahhuser.oxevent.listener.GameListener;
import com.github.ahhuser.oxevent.listener.SelectionListener;
import com.github.ahhuser.oxevent.manager.GameManager;
import com.github.ahhuser.oxevent.manager.PlayerManager;
import com.github.ahhuser.oxevent.manager.QuestionManager;
import com.github.ahhuser.oxevent.manager.RegionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class OXEventPlugin extends JavaPlugin {

    private static OXEventPlugin instance;

    private GameManager gameManager;
    private RegionManager regionManager;
    private QuestionManager questionManager;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize Managers
        this.regionManager = new RegionManager(this);
        this.questionManager = new QuestionManager(this);
        this.playerManager = new PlayerManager(this);
        this.gameManager = new GameManager(this);

        // Register Commands
        getCommand("ox").setExecutor(new OXCommand(this));

        // Register Listeners
        getServer().getPluginManager().registerEvents(new SelectionListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);

        getLogger().info("OX Event Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.endGame();
        }
        getLogger().info("OX Event Plugin has been disabled!");
    }

    public static OXEventPlugin getInstance() {
        return instance;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public QuestionManager getQuestionManager() {
        return questionManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
