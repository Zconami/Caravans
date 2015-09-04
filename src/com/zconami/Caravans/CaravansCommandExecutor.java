package com.zconami.Caravans;

import static com.zconami.Caravans.util.Utils.getCaravansPlugin;
import static com.zconami.Caravans.util.Utils.getLogger;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.gestern.gringotts.Util;

import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.util.ScoreboardUtils;

public class CaravansCommandExecutor implements CommandExecutor {

    // ===================================
    // ATTRIBUTES
    // ===================================

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public CaravansCommandExecutor() {
    }

    // ===================================
    // IMPLEMENTATION OF CommandExecutor
    // ===================================

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        getLogger().info("Command executed: " + command.getName());
        if (command.getName().equalsIgnoreCase("caravan") && args.length > 0) {
            final String secondary = args[0];
            if (secondary.equalsIgnoreCase("help")) {
                sender.sendMessage("/caravan help - this help page");
                sender.sendMessage("/caravan list - lists active caravans with public locations");
                sender.sendMessage("/caravan track <beneficiaryName> - track caravan from list");
                return true;
            } else if (secondary.equalsIgnoreCase("list")) {
                final List<Caravan> caravans = getCaravansPlugin().DB.find(Caravan.class).where()
                        .eq(Caravan.CARAVAN_STARTED, true).eq(Caravan.LOCATION_PUBLIC, true).findList();
                for (Caravan caravan : caravans) {
                    sender.sendMessage(caravan.getBeneficiary().getBukkitEntity().getName() + ": " + ChatColor.GREEN
                            + Util.format(caravan.getInvestment()) + " ("
                            + caravan.getBukkitEntity().getLocation().getBlockX() + ", "
                            + caravan.getBukkitEntity().getLocation().getBlockZ() + ")");
                }
                return true;
            } else if (secondary.equalsIgnoreCase("track") && args.length == 2 && sender instanceof Player) {
                final String beneficiaryName = args[1];

                final Caravan selected = getCaravansPlugin().DB.find(Caravan.class).where()
                        .eq(Caravan.CARAVAN_STARTED, true).eq(Caravan.LOCATION_PUBLIC, true)
                        .eq(Caravan.BUKKIT_ENTITY_ID, Bukkit.getPlayer(beneficiaryName).getUniqueId()).findUnique();

                if (selected != null) {
                    ScoreboardUtils.showScoreboard((Player) sender, selected);
                    sender.sendMessage(
                            "Now tracking " + selected.getBeneficiary().getBukkitEntity().getName() + "'s caravan");
                } else {
                    sender.sendMessage("Could not find that caravan, check /caravan list again");
                }
                return true;
            }
        }
        return false;
    }

}
