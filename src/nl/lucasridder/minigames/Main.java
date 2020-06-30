package nl.lucasridder.minigames;

import net.minecraft.server.v1_16_R1.IChatBaseComponent;
import net.minecraft.server.v1_16_R1.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.output.ByteArrayOutputStream;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener, PluginMessageListener {

    int all = 0;
    boolean lock;
    String lockreason;
    HashMap<Player, String> nextServer = new HashMap<>();
    HashMap<Player, String> CurrentGame = new HashMap<>();

    //check if block is in spawn
    public boolean checkSpawn(Location location) {
        double x = location.getBlock().getX();
        double z = location.getBlock().getZ();
        return x >= -43 && z >= -70 && x <= 35 && z <= 8;
    }

    //check paintball portal
    public boolean paintballPortal(Location location) {
        if(!location.getWorld().equals(Bukkit.getWorld("minigames"))) return false;
        double x = location.getBlock().getX();
        double z = location.getBlock().getZ();
        return x >= 12 && z == -31 && x <= 14;
    }

    //check bedwars portal
    public boolean bedwarsPortal(Location location) {
        if(!location.getWorld().equals(Bukkit.getWorld("minigames"))) return false;
        double x = location.getBlock().getX();
        double z = location.getBlock().getZ();
        return x >= -2 && z == -30 && x <= 3;
    }

    //check bedwars portal
    public boolean tntrunPortal(Location location) {
        if(!location.getWorld().equals(Bukkit.getWorld("minigames"))) return false;
        double x = location.getBlock().getX();
        double z = location.getBlock().getZ();
        return x >= -19 && z == -31 && x <= -16;
    }

    //spawn
    public void spawn(Player player) {
        try {
            int x = this.getConfig().getInt("spawn.x");
            int y = this.getConfig().getInt("spawn.y");
            int z = this.getConfig().getInt("spawn.z");
            float yaw = this.getConfig().getInt("spawn.yaw");
            float pitch = this.getConfig().getInt("spawn.pitch");
            Location loc = new Location(Bukkit.getWorld("minigames"), x, y, z, pitch, yaw);
            player.teleport(loc);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    //send server
    public void sendServer(String server, Player player) {
        player.sendMessage(ChatColor.DARK_GRAY + "Je wordt nu doorverbonden naar: " + ChatColor.GOLD + server);
        //BUNGEE
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("Connect");
            out.writeUTF(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
        nextServer.put(player, server);
    }

    //playercount
    public void playerCount() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("vps2.lucasridder.nl", 25565), 1000);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            out.write(0xFE);

            StringBuilder str = new StringBuilder();

            int b;
            while ((b = in.read()) != -1) {
                if (b != 0 && b > 16 && b != 255 && b != 23 && b != 24) {
                    str.append((char) b);
                }
            }

            String[] data = str.toString().split("§");
            this.all = Integer.parseInt(data[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //update scoreboard
    public void updateScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard b = manager.getNewScoreboard();

        String top = this.getConfig().getString("scoreboard.top");
        Objective o = b.registerNewObjective("Gold", "", ChatColor.BOLD + "" + ChatColor.BLUE + top);
        o.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score score6 = o.getScore(ChatColor.YELLOW + "");
        score6.setScore(6);

        Score score5 = o.getScore(ChatColor.YELLOW + "Welkom, " + ChatColor.GRAY + player.getName());
        score5.setScore(5);

        Score score4 = o.getScore(ChatColor.BOLD + "");
        score4.setScore(4);

        Score score3 = o.getScore(ChatColor.GOLD + "Totaal aantal spelers: " + ChatColor.RED + this.all);
        score3.setScore(3);

        int spelers = getServer().getOnlinePlayers().size();
        Score score2 = o.getScore(ChatColor.BLUE + " - Minigames: " + ChatColor.RED + spelers);
        score2.setScore(2);

        Score score1 = o.getScore("");
        score1.setScore(1);

        String name = this.getConfig().getString("scoreboard.name");
        Score score0 = o.getScore(ChatColor.BOLD + "" + ChatColor.GREEN + name);
        score0.setScore(0);

        player.setScoreboard(b);
    }

    //clear chat
    public void clearChat(Player player) {
        int x = 0;
        while (x < 20){
            player.sendMessage("");
            x = x + 1;
        }
    }

    //motd
    public void motd(Player player) {
        String name = player.getName();
        clearChat(player);
        player.sendMessage("  " + ChatColor.DARK_GRAY + "Welkom, " + ChatColor.GOLD + name + ChatColor.DARK_GRAY + " op de minigames server!");
        player.sendMessage("  " + ChatColor.BLUE + "Beschikbare andere servers: ");
        player.sendMessage("  " + ChatColor.GOLD + "/hub" + ChatColor.DARK_GRAY + ", " + ChatColor.GOLD + "/survival" + ChatColor.DARK_GRAY + " en " + ChatColor.GOLD + "/kitpvp");
        player.sendMessage("");
        player.sendMessage("");
    }

    //Start-up
    @Override
    public void onEnable() {
        //Bungeecord channel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        //register events
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        //config
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();

        //start counter
        new BukkitRunnable() {
            public void run() {
                if(getServer().getOnlinePlayers().size() != 0) {
                    playerCount();
                }

            }
        }.runTaskTimer(this, 20, 100);

        //enable complete
        System.out.println(ChatColor.GRAY + "Minigames plugin by LucasRidder " + ChatColor.GREEN + "Enabled");
    }

    //Power-down
    @Override
    public void onDisable() {
        // shut down plugin
        this.saveConfig();
        //stop scoreboard
        for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
            onlinePlayers.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        //disable complete
        System.out.println(ChatColor.GRAY + "Minigames plugin by LucasRidder " + ChatColor.GREEN + "Disabled");
    }

    //Join
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        //player info
        Player player = e.getPlayer();
        String name = player.getName();
        //lock
        if (!player.isOp()) {
            if (this.lock) {
                player.kickPlayer(ChatColor.GRAY + "De server is momenteel in lockdown vanwege:" + "\n" +
                        ChatColor.BLUE + this.lockreason + "\n" +
                        ChatColor.YELLOW + "Zie actuele status via: " + ChatColor.AQUA + "https://www.discord.gg/AzVCaQE");
                return;
            }
        }

        //join message
        if (player.hasPermission("minigames.admin")) {
            e.setJoinMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_GREEN + "+" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + name);
        } else if (!player.hasPlayedBefore()) {
            e.setJoinMessage(ChatColor.DARK_GRAY + "Welkom " + ChatColor.RESET + name);
        } else {
            e.setJoinMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_GREEN + "+" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + name);
        }

        //spawn loc
        spawn(player);
        //title
        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer.a("{\"text\":\"§aWelkom!\"}"), 20, 40, 20);
        PacketPlayOutTitle subtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a("{\"text\":\"§bOp de minigames server\"}"), 20, 40, 20);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(subtitle);

        //scoreboard
        new BukkitRunnable() {
            public void run() {
                if (!player.isOnline()) this.cancel();
                if (!CurrentGame.containsKey(player)) {
                    updateScoreboard(player);
                }
            }
        }.runTaskTimer(this, 20, 20);

        //set attack speed
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).setBaseValue(100);

        //motd message
        motd(player);
    }

    //Leave
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        //player info
        Player player = e.getPlayer();
        String name = player.getName();

        //leave game
        CurrentGame.remove(player);

        //bungee check
        String server = nextServer.get(player);
        if (server != null) {
            if (player.hasPermission("minigames.admin")) {
                e.setQuitMessage(ChatColor.RED + name + ChatColor.DARK_RED + " -> " + ChatColor.GRAY + server);
            } else {
                e.setQuitMessage(ChatColor.WHITE + name + ChatColor.DARK_RED + " -> " + ChatColor.GRAY + server);
            }
            nextServer.remove(player);
        } else {
            //leave message
            if (player.hasPermission("minigames.admin")) {
                e.setQuitMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "-" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + name);
            } else {
                e.setQuitMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "-" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET + name);
            }
        }
    }

    //commands
    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        //gamemode
        if(cmd.getName().equalsIgnoreCase("gamemode")) {
            if(sender.hasPermission("survival.admin")) {
                //check of sender speler is
                //te weinig argumenten
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.RED + "/gamemode (creative/survival/spectator/adventure)/(0/1/2/3) (speler)");
                    return true;
                }

                //teveel argumenten
                if (args.length > 2) {
                    sender.sendMessage(ChatColor.RED + "/gamemode (creative/survival/spectator/adventure)/(0/1/2/3) (speler)");
                    return true;
                }

                //goede aantal argumenten
                if (args.length == 1) {
                    //pak speler

                    if (args[0].equalsIgnoreCase("creative") | args[0].equalsIgnoreCase("1")) {
                        player.setGameMode(GameMode.CREATIVE);
                        sender.sendMessage(ChatColor.GREEN + "Gedaan.");
                        return true;
                    } else if (args[0].equalsIgnoreCase("survival") | args[0].equalsIgnoreCase("0")) {
                        player.setGameMode(GameMode.SURVIVAL);
                        sender.sendMessage(ChatColor.GREEN + "Gedaan.");
                        return true;
                    } else if (args[0].equalsIgnoreCase("spectator") | args[0].equalsIgnoreCase("3")) {
                        player.setGameMode(GameMode.SPECTATOR);
                        sender.sendMessage(ChatColor.GREEN + "Gedaan.");
                        return true;
                    } else if (args[0].equalsIgnoreCase("adventure") | args[0].equalsIgnoreCase("2")) {
                        player.setGameMode(GameMode.ADVENTURE);
                        sender.sendMessage(ChatColor.GREEN + "Gedaan.");
                        return true;
                    } else
                        sender.sendMessage(ChatColor.RED + "/gamemode (creative/survival/spectator/adventure)/(0/1/2/3) (speler)");

                }

                //andere speler
                if (args.length == 2) {
                    //pak speler
                    Player target = Bukkit.getServer().getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(ChatColor.RED + "Doel is niet online");
                    } else {

                        if (args[0].equalsIgnoreCase("creative") | args[0].equalsIgnoreCase("1")) {
                            target.setGameMode(GameMode.CREATIVE);
                            target.sendMessage(ChatColor.GREEN + "Gamemode aangepast.");
                            sender.sendMessage(ChatColor.GREEN + "Gedaan.");
                            return true;
                        } else if (args[0].equalsIgnoreCase("survival") | args[0].equalsIgnoreCase("0")) {
                            target.setGameMode(GameMode.SURVIVAL);
                            target.sendMessage(ChatColor.GREEN + "Gamemode aangepast.");
                            sender.sendMessage(ChatColor.GREEN + "Gedaan.");
                            return true;
                        } else if (args[0].equalsIgnoreCase("spectator") | args[0].equalsIgnoreCase("3")) {
                            target.setGameMode(GameMode.SPECTATOR);
                            target.sendMessage(ChatColor.GREEN + "Gamemode aangepast.");
                            sender.sendMessage(ChatColor.GREEN + "Gedaan.");
                            return true;
                        } else if (args[0].equalsIgnoreCase("adventure") | args[0].equalsIgnoreCase("2")) {
                            target.setGameMode(GameMode.ADVENTURE);
                            target.sendMessage(ChatColor.GREEN + "Gamemode aangepast.");
                            sender.sendMessage(ChatColor.GREEN + "Gedaan.");
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.RED + "/gamemode (creative/survival/spectator/adventure)/(0/1/2/3) (speler)");
                        }
                    }
                }
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "Geen toegang tot dit commando");
            }
        }

        //setspawn command
        if(cmd.getName().equalsIgnoreCase("setspawn")) {
            if(!(sender instanceof Player)) {
                //zeg het
                sender.sendMessage(ChatColor.RED + "Je bent geen speler");
                return true;
            } else {
                if(sender.isOp()) {
                    int x = player.getLocation().getBlockX();
                    int y = player.getLocation().getBlockY();
                    int z = player.getLocation().getBlockZ();
                    float yaw = player.getLocation().getYaw();
                    float pitch = player.getLocation().getPitch();
                    this.getConfig().set("spawn.x", x);
                    this.getConfig().set("spawn.y", y);
                    this.getConfig().set("spawn.z", z);
                    this.getConfig().set("spawn.yaw", yaw);
                    this.getConfig().set("spawn.pitch", pitch);
                    this.saveConfig();
                    sender.sendMessage(ChatColor.GREEN + "Spawn set");
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "Geen toegang tot dit commando");
                }
            }
        }

        //lobby
        if(cmd.getName().equalsIgnoreCase("lobby")) { sendServer("lobby", (Player) sender); }

        //survival
        if(cmd.getName().equalsIgnoreCase("survival")) { sendServer("survival", (Player) sender); }

        //fly
        if(cmd.getName().equalsIgnoreCase("fly")) {
            if (!sender.isOp()) {
                if (!(sender instanceof Player)) {
                    //zeg het
                    sender.sendMessage(ChatColor.RED + "Je bent geen speler");
                    return true;
                } else {
                    //zelf
                    if (args.length == 0) {

                        if (player.isFlying()) {
                            player.setFlying(false);
                            sender.sendMessage(ChatColor.GREEN + "Vliegen uitgeschakeld.");
                        } else {
                            player.setFlying(true);
                            sender.sendMessage(ChatColor.GREEN + "Vliegen ingeschakeld.");
                        }
                        sender.sendMessage(ChatColor.GREEN + "Vliegen ingeschakeld.");
                    }
                    //andere speler
                    if (args.length == 1) {
                        Player target = Bukkit.getServer().getPlayer(args[0]);
                        if (target == null) {
                            sender.sendMessage(ChatColor.RED + "Doel is niet online");
                        } else {
                            if (target.isFlying()) {
                                target.setFlying(false);
                                sender.sendMessage(ChatColor.GREEN + "Vliegen uitgeschakeld voor " + ChatColor.GOLD + args[0] + ChatColor.GREEN + ".");
                            } else {
                                target.setFlying(true);
                                sender.sendMessage(ChatColor.GREEN + "Vliegen ingeschakeld voor " + ChatColor.GOLD + args[0] + ChatColor.GREEN + ".");
                            }
                        }
                    }
                    //teveel argumenten
                    if(args.length >= 1) { sender.sendMessage(ChatColor.RED + "/fly (speler)"); }

                }
            }
        }

        //spawn/leave
        if(cmd.getName().equalsIgnoreCase("spawn")) {
            if (!(sender instanceof Player)) {
                //zeg het
                sender.sendMessage(ChatColor.RED + "Je bent geen speler");
                return true;
            }

            if(CurrentGame.containsKey(player)) {
                if (this.CurrentGame.containsValue("paintball")) {
                    player.sendMessage(ChatColor.GREEN + "Leaving paintball...");
                    player.performCommand("pb leave");
                }
                if (this.CurrentGame.containsValue("bedwars")) {
                    player.sendMessage(ChatColor.GREEN + "Leaving bedwars...");
                    player.performCommand("bw leave");
                }
                if (this.CurrentGame.containsValue("tntrun")) {
                    player.sendMessage(ChatColor.GREEN + "Leaving tntrun...");
                    player.performCommand("tr leave");
                }
                CurrentGame.remove(player);
            }

            //spawn loc
            sender.sendMessage(ChatColor.GREEN + "Aan het teleporteren...");
            spawn(player);
        }

        //lock
        if(cmd.getName().equalsIgnoreCase("lock")) {
            if(sender.hasPermission("survival.admin")) {
                if(!this.lock) {
                    lock = true;
                    if (args.length == 0) {
                        this.lockreason = "onbekend";
                        sender.sendMessage(ChatColor.GREEN + "Lockdown geactiveerd!");
                    } else {
                        this.lockreason = args[0];
                        sender.sendMessage(ChatColor.GREEN + "Lockdown geactiveerd!");
                        sender.sendMessage(ChatColor.GRAY + "Reden: " + ChatColor.GOLD + this.lockreason);
                    }
                    //kick all players
                    sender.sendMessage(ChatColor.GREEN + "Kicking all players...");
                    for(Player players : this.getServer().getOnlinePlayers()) {
                        if(!players.isOp()) {
                            players.kickPlayer(ChatColor.GRAY + "De server is momenteel in lockdown vanwege:" + "\n" +
                                    ChatColor.BLUE + this.lockreason + "\n" +
                                    ChatColor.YELLOW + "Zie actuele status via: " + ChatColor.AQUA + "https://www.discord.gg/AzVCaQE");
                        } else {
                            players.sendMessage(ChatColor.GREEN + "Lockdown geactiveerd | Kick Bypassed");
                        }
                    }
                    System.out.println("[HUB]" + ChatColor.DARK_RED + " Lockdown activated by " + ChatColor.GREEN + sender.getName() + ChatColor.RED + "!");
                } else {
                    this.lock = false;
                    sender.sendMessage(ChatColor.GREEN + "Lockdown opgeheven");
                }
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "Geen toegang tot dit commando");
            }
        }

        //motd
        if(cmd.getName().equalsIgnoreCase("motd")) {
            if(!(sender instanceof Player)) {
                //zeg het
                sender.sendMessage(ChatColor.RED + "Je bent geen speler");
                return true;
            } else {
                motd((Player) sender);
            }
        }

        //stop
        if(cmd.getName().equalsIgnoreCase("stop")) {
            if(sender.isOp()) {
                sender.sendMessage(ChatColor.GREEN + "Kicking all players...");
                if(this.getServer().getOnlinePlayers().size() != 0) {
                    for (Player players : this.getServer().getOnlinePlayers()) {
                        players.kickPlayer(ChatColor.GRAY + "De server wordt momenteel herstart" + "\n" +
                                ChatColor.BLUE + "wacht even met opnieuw joinen" + "\n" +
                                ChatColor.YELLOW + "Zie actuele status via: " + ChatColor.AQUA + "https://www.discord.gg/AzVCaQE");
                        System.out.println("[HUB]" + ChatColor.DARK_RED + " stopping server...");
                        Bukkit.shutdown();
                    }
                }
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "Geen toegang tot dit commando");
            }

        }
        return true;
    }

    //Commandblock
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        String message = e.getMessage();
        Player player = e.getPlayer();
        if (!player.isOp()) {
            //help
            if (message.contains("/pl")) {
                e.setCancelled(true);
            }
        }
    }

    //Chat
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String name = e.getPlayer().getName();
        String message = e.getMessage();
        if(player.isOp()) {
            e.setFormat(ChatColor.GOLD + name + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + message);
        } else {
            e.setFormat(ChatColor.GRAY + name + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + message);
        }
    }

    //signs
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (e.getClickedBlock().getState() instanceof Sign) {
            Sign sign = (Sign) e.getClickedBlock().getState();
            if (sign.getLine(1).contains("[Join]")) {
                CurrentGame.put(e.getPlayer(), "tntrun");
            }
            if (sign.getLine(1).contains("gestolen")) {
                CurrentGame.put(e.getPlayer(), "bedwars");
            }
        }
    }

    //checkportal
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Location location = player.getLocation();
        if(paintballPortal(location)) {
                if (this.CurrentGame.containsValue("paintball")) {
                    player.sendMessage(ChatColor.GREEN + "Leaving paintball...");
                    player.performCommand("pb leave");
                    spawn(player);
                    CurrentGame.remove(player);
                } else {
                    player.sendMessage(ChatColor.GREEN + "Joining paintball...");
                    spawn(player);
                    player.performCommand("pb join");
                    CurrentGame.put(player, "paintball");
                }
        }
        if(bedwarsPortal(location)) {
            if (this.CurrentGame.containsKey(player)) {
                player.sendMessage(ChatColor.GREEN + "Leaving bedwars...");
                spawn(player);
                CurrentGame.remove(player);
            }
        }
        if(tntrunPortal(location)) {
            if (this.CurrentGame.containsKey(player)) {
                player.sendMessage(ChatColor.GREEN + "Leaving tntrun...");
                spawn(player);
                CurrentGame.remove(player);
            }
        }
    }

    //No Damage
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        Location location = e.getEntity().getLocation();
        if(checkSpawn(location)) {
            e.setCancelled(true);
        }
    }

    //break
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Location location = e.getBlock().getLocation();
        Player player = e.getPlayer();
        if(checkSpawn(location)) {
            if(!player.isOp()) {
                e.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Hey!, je mag in de spawn niks veranderen!");
            }
        }
    }

    //Block place
    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Location location = e.getBlock().getLocation();
        Player player = e.getPlayer();
        if(checkSpawn(location)) {
            if(!player.isOp()) {
                e.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Hey!, je mag in de spawn niks veranderen!");
            }
        }
    }

    //No Damage
    @EventHandler
    public void onPvp(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof Player) {
            if(checkSpawn(e.getEntity().getLocation())) e.setCancelled(true);
        }
    }

    //dood
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        String name = e.getEntity().getName();
        e.setDeathMessage(ChatColor.GRAY + name + ChatColor.RESET + " is gestorven" + ChatColor.RED + "!");
    }

    //Weer
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e){ e.setCancelled(e.toWeatherState()); }

    //Listener bungee
    @SuppressWarnings("NullableProblems")
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            System.out.println(Arrays.toString(message));
        }
    }

}
