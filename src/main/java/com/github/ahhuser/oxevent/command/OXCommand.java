package com.github.ahhuser.oxevent.command;

import com.github.ahhuser.oxevent.OXEventPlugin;
import com.github.ahhuser.oxevent.manager.GameManager;
import com.github.ahhuser.oxevent.manager.RegionManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class OXCommand implements CommandExecutor {

    private final OXEventPlugin plugin;

    public OXCommand(OXEventPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission("ox.admin")) {
            sender.sendMessage("§cBrak uprawnien!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "createquestion":
                handleCreateQuestion(sender, args);
                break;
            case "init":
                plugin.getGameManager().initGame();
                sender.sendMessage("§aZainicjowano event (teleportacja graczy).");
                break;
            case "start":
                plugin.getGameManager().startGame();
                sender.sendMessage("§aWystartowano event!");
                break;
            case "end":
                plugin.getGameManager().endGame();
                sender.sendMessage("§aZakonczono event.");
                break;
            case "question":
                handleQuestion(sender, args);
                break;
            case "setspawn":
                if (sender instanceof Player p) {
                    plugin.getRegionManager().setSpawn(p.getLocation());
                    p.sendMessage("§cUstawiono spawn eventu.");
                }
                break;
            case "setwidownia":
                if (sender instanceof Player p) {
                    plugin.getRegionManager().setWidownia(p.getLocation());
                    p.sendMessage("§aUstawiono widownie.");
                }
                break;
            case "setteleport":
                handleSetZone(sender, "teleport");
                break;
            case "set":
                if (args.length > 1) {
                    handleSetZone(sender, args[1].toLowerCase());
                } else {
                    sender.sendMessage("§cUzycie: /ox set <o/x>");
                }
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleCreateQuestion(CommandSender sender, String[] args) {
        // /ox createquestion [id] [true/false] [question]
        if (args.length < 4) {
            sender.sendMessage("§cUzycie: /ox createquestion <id> <true/false> <tresc...>");
            return;
        }

        try {
            int id = Integer.parseInt(args[1]);
            boolean answer = Boolean.parseBoolean(args[2]); // Works for "true" and "każdy inny ciąg jako false", but
                                                            // user specifically said "true/false".
            // Better parsing implementation if strictness needed, but Boolean.parseBoolean
            // is standard java.

            // Join rest of args
            String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            plugin.getQuestionManager().createQuestion(id, answer, text);
            sender.sendMessage("§aUtworzono pytanie #" + id);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cID musi byc liczba!");
        }
    }

    private void handleQuestion(CommandSender sender, String[] args) {
        if (plugin.getGameManager().getGameState() != GameManager.GameState.IN_GAME) {
            sender.sendMessage("§cEvent nie jest w trakcie gry (/ox start)!");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUzycie: /ox question <id>");
            return;
        }

        try {
            int id = Integer.parseInt(args[1]);
            plugin.getGameManager().startQuestion(id);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cID musi byc liczba!");
        }
    }

    private void handleSetZone(CommandSender sender, String type) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cKomenda tylko dla graczy.");
            return;
        }

        RegionManager.Cuboid selection = plugin.getRegionManager().getSelection(p);
        if (selection == null) {
            p.sendMessage("§cMusisz zaznaczyc teren lopata (PPM/LPM)!");
            return;
        }

        switch (type) {
            case "o": // True
                plugin.getRegionManager().setTrueZone(selection);
                p.sendMessage("§aUstawiono strefę PRAWDA (O).");
                break;
            case "x": // False
                plugin.getRegionManager().setFalseZone(selection);
                p.sendMessage("§cUstawiono strefę FAŁSZ (X).");
                break;
            case "teleport":
                plugin.getRegionManager().setTeleportZone(selection);
                p.sendMessage("§aUstawiono strefę teleportacji (fail-safe).");
                break;
            default:
                p.sendMessage("§cNieznany typ strefy: " + type + " (uzyj o, x, lub setteleport)");
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e--- OX EVENT ---");
        sender.sendMessage("§6/ox createquestion <id> <true/false> <tekst>");
        sender.sendMessage("§6/ox init - teleportuje graczy na spawn");
        sender.sendMessage("§6/ox start - rozpoczyna gre");
        sender.sendMessage("§6/ox question <id> - zadaje pytanie");
        sender.sendMessage("§6/ox setspawn - ustawia spawn startowy");
        sender.sendMessage("§6/ox setwidownia - ustawia miejsce dla przegranych");
        sender.sendMessage("§6/ox set <o|x> - ustawia strefy prawda/falsz (zaznacz lopata)");
        sender.sendMessage("§6/ox setteleport - ustawia strefy fail-safe");
        sender.sendMessage("§6/ox end - konczy event");
    }
}
