package hu.t3ns1on;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class T3ns1onPlugin extends JavaPlugin {

    private Location pointA; // pos1
    private Location pointB; // pos2
    private NPC lastNpc;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadPointsFromConfig();
        getLogger().info("t3ns1on enabled.");
    }

    @Override
    public void onDisable() {
        cleanupNpc();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // /setnpcpoints pos1|pos2
        if (cmd.getName().equalsIgnoreCase("setnpcpoints")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(ChatColor.RED + "Ezt csak játékos használhatja.");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage(ChatColor.YELLOW + "Használat: /setnpcpoints pos1 | pos2");
                return true;
            }

            if (args[0].equalsIgnoreCase("pos1")) {
                pointA = p.getLocation().clone();
                savePointsToConfig();
                p.sendMessage(ChatColor.GREEN + "Pos1 elmentve: " + formatLoc(pointA));
                return true;
            }

            if (args[0].equalsIgnoreCase("pos2")) {
                pointB = p.getLocation().clone();
                savePointsToConfig();
                p.sendMessage(ChatColor.GREEN + "Pos2 elmentve: " + formatLoc(pointB));
                return true;
            }

            sender.sendMessage(ChatColor.YELLOW + "Használat: /setnpcpoints pos1 | pos2");
            return true;
        }

        // /startnpc
        if (cmd.getName().equalsIgnoreCase("startnpc")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(ChatColor.RED + "Ezt csak játékos használhatja.");
                return true;
            }

            if (Bukkit.getPluginManager().getPlugin("Citizens") == null) {
                p.sendMessage(ChatColor.RED + "Citizens2 nincs fent / nem töltött be, pedig kell hozzá.");
                return true;
            }

            if (pointA == null || pointB == null) {
                p.sendMessage(ChatColor.RED + "Előbb állíts be két pontot: /setnpcpoints pos1 és /setnpcpoints pos2");
                return true;
            }

            if (pointA.getWorld() == null || pointB.getWorld() == null) {
                p.sendMessage(ChatColor.RED + "A pontok világa invalid.");
                return true;
            }

            if (!pointA.getWorld().equals(pointB.getWorld())) {
                p.sendMessage(ChatColor.RED + "A két pontnak ugyanabban a világban kell lennie.");
                return true;
            }

            // előző NPC takarítás
            cleanupNpc();

            String coloredName = ChatColor.translateAlternateColorCodes('&', "&cFafa");

            // PLAYER NPC (default Steve-szerű kinézet, ha nincs skin trait beállítva)
            NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, coloredName);
            lastNpc = npc;

            npc.spawn(pointA);

            // elindítjuk pos2-re
            npc.getNavigator().setTarget(pointB);

            p.sendMessage(ChatColor.GREEN + "NPC elindítva: " + coloredName);
            return true;
        }

        return false;
    }

    private void cleanupNpc() {
        if (lastNpc != null) {
            try {
                if (lastNpc.isSpawned()) lastNpc.despawn();
                lastNpc.destroy();
            } catch (Throwable ignored) {
            } finally {
                lastNpc = null;
            }
        }
    }

    private String formatLoc(Location l) {
        return ChatColor.GRAY + "[" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + "]";
    }

    private void savePointsToConfig() {
        Location ref = (pointA != null ? pointA : pointB);
        if (ref != null && ref.getWorld() != null) {
            getConfig().set("world", ref.getWorld().getUID().toString());
        }

        if (pointA != null) {
            getConfig().set("a.x", pointA.getX());
            getConfig().set("a.y", pointA.getY());
            getConfig().set("a.z", pointA.getZ());
        }

        if (pointB != null) {
            getConfig().set("b.x", pointB.getX());
            getConfig().set("b.y", pointB.getY());
            getConfig().set("b.z", pointB.getZ());
        }

        saveConfig();
    }

    private void loadPointsFromConfig() {
        String worldId = getConfig().getString("world", null);
        if (worldId == null) return;

        World w;
        try {
            w = Bukkit.getWorld(UUID.fromString(worldId));
        } catch (IllegalArgumentException e) {
            return;
        }
        if (w == null) return;

        if (getConfig().contains("a.x")) {
            pointA = new Location(
                    w,
                    getConfig().getDouble("a.x"),
                    getConfig().getDouble("a.y"),
                    getConfig().getDouble("a.z")
            );
        }

        if (getConfig().contains("b.x")) {
            pointB = new Location(
                    w,
                    getConfig().getDouble("b.x"),
                    getConfig().getDouble("b.y"),
                    getConfig().getDouble("b.z")
            );
        }
    }
}
