package com.zconami.Caravans;

import static com.zconami.Caravans.util.Utils.getLogger;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.gestern.gringotts.Util;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.zconami.Caravans.domain.Beneficiary;
import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.repository.BeneficiaryRepository;
import com.zconami.Caravans.repository.CaravanRepository;
import com.zconami.Caravans.repository.RegionRepository;
import com.zconami.Caravans.util.ScoreboardUtils;

public class CaravansCommandExecutor implements CommandExecutor {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final RegionRepository regionRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final CaravanRepository caravanRepository;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public CaravansCommandExecutor(RegionRepository regionRepository, BeneficiaryRepository beneficiaryRepository,
            CaravanRepository caravanRepository) {
        this.regionRepository = regionRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.caravanRepository = caravanRepository;
    }

    // ===================================
    // IMPLEMENTATION OF CommandExecutor
    // ===================================

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        getLogger().info("Command executed: " + command.getName());
        if (command.getName().equalsIgnoreCase("c") && args.length > 0) {
            final String secondary = args[0];
            if (secondary.equalsIgnoreCase("list")) {
                final List<Caravan> caravans = caravanRepository.all();
                if (caravans.isEmpty()) {
                    sender.sendMessage("No pulic caravans, has their location been broadcast yet?");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "===≕ Publically Known Caravans ≔==");
                }

                for (Caravan caravan : caravans.stream()
                        .filter(caravan -> caravan.isCaravanStarted() && caravan.isLocationPublic())
                        .collect(Collectors.toList())) {
                    final StringBuilder builder = new StringBuilder();
                    builder.append(caravan.getBeneficiary().getBukkitEntity().getName() + " (");
                    final Faction faction = caravan.getFaction();
                    if (faction != null) {
                        builder.append(faction.describeTo(MPlayer.get(sender)) + ChatColor.WHITE + ") ");
                    }
                    builder.append(ChatColor.GREEN + Util.format(caravan.getInvestment()));
                    sender.sendMessage(builder.toString());
                }
                return true;
            } else if (secondary.equalsIgnoreCase("track") && args.length == 2 && sender instanceof Player) {

                final String playerName = args[1];
                final Player player = Bukkit.getPlayer(playerName);
                if (player == null) {
                    sender.sendMessage("Can't find that player, are you sure you typed it right?");
                }

                final Beneficiary targetBeneficiary = beneficiaryRepository.find(player);
                final Caravan targetCaravan = caravanRepository.findByBeneficiary(targetBeneficiary);
                if (targetCaravan.isCaravanStarted() && targetCaravan.isLocationPublic()) {
                    ScoreboardUtils.showScoreboard((Player) sender, targetCaravan);
                    sender.sendMessage("Now tracking " + playerName + "'s caravan");
                } else {
                    sender.sendMessage("Could not find that caravan, has their location been broadcast yet?");
                }
                return true;
            } else {
                sender.sendMessage("/c help - this help page");
                sender.sendMessage("/c list - lists active caravans with public locations");
                sender.sendMessage("/c track <playerName> - track player's caravan");
                return true;
            }
        }
        return false;
    }

}
