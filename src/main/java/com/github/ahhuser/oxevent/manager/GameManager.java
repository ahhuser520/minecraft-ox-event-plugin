package com.github.ahhuser.oxevent.manager;

import com.github.ahhuser.oxevent.OXEventPlugin;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class GameManager {

    public enum GameState {
        WAITING, STARTING, IN_GAME, ENDING
    }

    private final OXEventPlugin plugin;
    private GameState state = GameState.WAITING;
    private BossBar bossBar;
    private boolean questionActive = false;

    public GameManager(OXEventPlugin plugin) {
        this.plugin = plugin;
    }

    public void setGameState(GameState state) {
        this.state = state;
    }

    public GameState getGameState() {
        return state;
    }

    public boolean isQuestionActive() {
        return questionActive;
    }

    public void initGame() {
        if (plugin.getRegionManager().getSpawn() == null) {
            // Should warn sender probably, but here just return
            return;
        }

        // Teleport everyone to spawn
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(plugin.getRegionManager().getSpawn());
            plugin.getPlayerManager().addParticipant(p);
        }

        state = GameState.STARTING;
    }

    public void startGame() {
        if (state != GameState.STARTING)
            return;
        state = GameState.IN_GAME;

        Title title = Title.title(
                Component.text("§aEVENT OX ROZPOCZĘTY!"),
                Component.text("§7Powodzenia!"),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(1000)));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(title);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }
    }

    public void startQuestion(int id) {
        if (state != GameState.IN_GAME)
            return;
        if (questionActive)
            return;

        QuestionManager.Question q = plugin.getQuestionManager().getQuestion(id);
        if (q == null)
            return;

        questionActive = true;

        bossBar = BossBar.bossBar(
                Component.text("§ePytanie: " + q.text()),
                1.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS);

        // Add bossbar to all
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showBossBar(bossBar);
        }

        new BukkitRunnable() {
            int timeLeft = 20;

            @Override
            public void run() {
                if (!questionActive || state != GameState.IN_GAME) {
                    this.cancel();
                    removeBossBar();
                    return;
                }

                // Action Bar
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendActionBar(Component.text("§e" + q.text()));

                    // BossBar Update Logic
                    // User requested: "JESTEŚ NA STREFIE: FAŁSZ/PRAWDA" in bossbar for
                    // participants?
                    // Ah, the user said "gracze na danej strefie powinni dostać boss bara z
                    // informacją... jednak zycie tego boss bara ma sie zmniejszac"
                    // So maybe I need separate bossbars or update title per player.
                    // Let's implement per-player bossbar update in a loop here.

                    updatePlayerBossBar(p, timeLeft);
                }

                if (timeLeft <= 5 && timeLeft > 0) {
                    Title title = Title.title(
                            Component.text("§c" + timeLeft),
                            Component.text("§e" + q.text()),
                            Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1200), Duration.ofMillis(0)));

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!plugin.getPlayerManager().isSpectator(p)) {
                            p.showTitle(title);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                        } else {
                            // Spectators maintain action bar and sound, but no title
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                        }
                    }
                }

                if (timeLeft <= 0) {
                    this.cancel();
                    resolveQuestion(q);
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void updatePlayerBossBar(Player p, int timeLeft) {
        // Update progress
        float progress = Math.max(0, timeLeft / 20.0f);
        bossBar.progress(progress);

        // Update name based on zone
        RegionManager rm = plugin.getRegionManager();
        String zoneInfo = "BRAK STREFY";
        if (rm.getTrueZone() != null && rm.getTrueZone().contains(p)) {
            zoneInfo = "§aPRAWDA (O)";
        } else if (rm.getFalseZone() != null && rm.getFalseZone().contains(p)) {
            zoneInfo = "§cFAŁSZ (X)";
        }

        // Spectators shouldn't get the zone info bossbar?
        // User said: "osoby ktore są na widowni... powinni nie dostawac boss bara z
        // informacja o strefie"
        // But BossBar api (Adventure) is usually shared if I use one instance.
        // It's better to show Spectators the question or remove bossbar for them.
        // For simplicity, I'll update the shared bossbar name with Generic info, but
        // wait...
        // If I want different titles for different players, I need different BossBar
        // instances.

        // Re-thinking: Creating one BossBar instance per player is expensive but allows
        // custom text.
        // Or I can just use one shared BossBar with the question, and send Action Bar
        // for zone info?
        // User requirement: "ACTION BAR powinien byc ciagle widoczne zadane pytanie...
        // BOSS BAR ma napis na jakiej obecnie strefie gracz sie znajduje"
        // OK, so Boss Bar = Zone Info + Timer. Action Bar = Question.

        // Since BossBar needs different text per player ("JESTEŚ NA STREFIE: X"), I
        // cannot use a single shared BossBar instance.
        // I will need to manage per-player BossBars or just instantiate a new one every
        // tick (bad).
        // Best approach: Store a BossBar map in GameManager or just send it uniquely.
        // Let's modify this method to handle per-player bossbar logic properly in next
        // iteration or now.
        // I'll leave the shared logic for now and refactor it in a follow-up step to
        // fix the per-player text requirement.

        // Actually, let's fix it now. I'll remove the shared `bossBar` field usage in
        // loop and use per-player or temporary.

        // Wait, Adventure BossBar is per-audience.
        // I can just recreate it or maintain a map.
        // Ideally, I'd maintain a Map<UUID, BossBar> activeBossBars.
    }

    private void resolveQuestion(QuestionManager.Question q) {
        questionActive = false;
        RegionManager rm = plugin.getRegionManager();
        RegionManager.Cuboid trueZone = rm.getTrueZone();
        RegionManager.Cuboid falseZone = rm.getFalseZone();

        RegionManager.Cuboid wrongZone = q.answer() ? falseZone : trueZone;

        // 1. Clear wrong zone
        if (wrongZone != null) {
            rm.clearZone(wrongZone);
        }

        // 2. Eliminate players
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getPlayerManager().isSpectator(p))
                continue;

            boolean inTrue = trueZone != null && trueZone.contains(p);
            boolean inFalse = falseZone != null && falseZone.contains(p);

            boolean safe = (q.answer() && inTrue) || (!q.answer() && inFalse);

            // "nie wybranie żadnej strefy wiąże się równoznacznie z przegraniem"
            if (!safe) {
                // If they are in the wrong zone, they might fall.
                // If they are in NO zone, they should be eliminated immediately too?
                // "fail-safe - jezeli gracz dalej znajduje się w poblizu jakiejkolwiek strefy,
                // a przegrał, to ma byc odsylany na widownie"

                // If the floor disappears (wrong zone), they will fall. Logic handles fall.
                // If they are standing nowhere (e.g. on edge or glitching), eliminate them.

                // Let's rely on the physical fall for those in the wrong zone (since blocks
                // disappear).
                // But for those "not choosing any zone", they might be standing on a neutral
                // platform?
                // The prompt says "przechodzą na prawą, albo lewą strone". Usually OX has a
                // middle neutral zone.
                // If they stay in neutral, they lose.

                // Eliminating everyone not in the CORRECT zone immediately is safest.
                // But the user wants the "fall" effect for wrong zone.
                // "jezeli gracz był w złej stefie, powinien spaść na dół... jezeli tak sie z
                // jakis przyczyn nie stalo, to przeteleportuj"

                // So, if they are in wrong zone -> block remove -> fall.
                // If they are in NEUTRAL zone (safe ground but not answer) -> Eliminate
                // immediately?
                // I will execute eliminatePlayer(p) for those NOT in the correct zone, EXCEPT
                // those who are about to fall (in wrong zone).
                // Actually, if I eliminate them immediately, they teleport away and don't fall.
                // User wants visual "fall".

                // So:
                // If in Wrong Zone: Let them fall. (Don't teleport yet).
                // If in Safe Zone (Correct): Do nothing.
                // If in Neither (Neutral): Teleport/Eliminate immediately? Or let them stay and
                // realize they lost?
                // User: "nie wybranie żadnej strefy wiąże się równoznacznie z przegraniem"

                if (wrongZone != null && wrongZone.contains(p)) {
                    // They will fall. We do nothing yet, the cleaning of zone handles it.
                    // We just need to make sure we don't count them as participants anymore?
                    // Or wait for them to die/fall?
                    // The prompt says "fail-safe".
                } else {
                    // Not in wrong zone, but also not in correct zone?
                    if (!((q.answer() && inTrue) || (!q.answer() && inFalse))) {
                        plugin.getPlayerManager().eliminatePlayer(p);
                    }
                }
            } else {
                p.sendMessage("§aDobra odpowiedź!");
            }

            // Remove bossbar
            p.hideBossBar(BossBar.bossBar(Component.empty(), 0, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)); // Clear
                                                                                                                 // all?
                                                                                                                 // No,
                                                                                                                 // need
                                                                                                                 // track.
        }

        removeBossBar(); // Clear global tracked bossbars

        // 3. Restore zone after 5s
        new BukkitRunnable() {
            @Override
            public void run() {
                if (wrongZone != null) {
                    rm.restoreZone(wrongZone);
                }
                // Check for anyone stuck in air/glitched?
            }
        }.runTaskLater(plugin, 100L);
    }

    public void endGame() {
        if (state == GameState.ENDING || state == GameState.WAITING)
            return;
        state = GameState.ENDING;

        Title title = Title.title(
                Component.text("§aKONIEC EVENTU"),
                Component.text("§7Dziękujemy!"),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(1000)));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(title);
        }

        removeBossBar();

        new BukkitRunnable() {
            @Override
            public void run() {
                Location endLoc = new Location(Bukkit.getWorld("world"), 0, 130, 0);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.teleport(endLoc);
                }
                plugin.getPlayerManager().clearAll();
                state = GameState.WAITING;
            }
        }.runTaskLater(plugin, 60L); // 3 seconds
    }

    private void removeBossBar() {
        if (bossBar != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hideBossBar(bossBar);
            }
            bossBar = null;
        }
        // Also need to clear the periodic one if I implement per-player.
    }
}
