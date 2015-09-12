package com.zconami.Caravans;

import static com.zconami.Caravans.util.Utils.getLogger;

import java.util.List;
import java.util.stream.Collectors;

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
    // CONSTANTS
    // ===================================

    private static final int PAGE_SIZE = 5;

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
                Integer pageNumber;
                if (args.length > 1) {
                    try {
                        pageNumber = Integer.valueOf(args[1]);
                    } catch (NumberFormatException ex) {
                        pageNumber = Integer.valueOf(1);
                    }
                } else {
                    pageNumber = Integer.valueOf(1);
                }

                final List<Caravan> caravans = caravanRepository.all();
                if (caravans.isEmpty()) {
                    sender.sendMessage(
                            "No public caravans at the moment, their locations have to be broadcast before they'll show here");
                } else {
                    sender.sendMessage(makeHeader("PUBLICALLY KNOWN CARAVANS"));
                    sender.sendMessage(ChatColor.GOLD + " Use " + ChatColor.BLUE + "/c track <playerName>"
                            + ChatColor.GOLD + " to track location");
                    sender.sendMessage(ChatColor.GOLD + "");
                }

                final List<Caravan> currentPage;
                if (caravans.size() <= PAGE_SIZE) {
                    currentPage = caravans;
                } else {
                    final int pageStart = (pageNumber - 1) * PAGE_SIZE;
                    currentPage = caravans.subList(pageStart, pageStart + PAGE_SIZE);
                }

                for (Caravan caravan : currentPage.stream().filter(Caravan::locationBroadcasted)
                        .collect(Collectors.toList())) {
                    final StringBuilder builder = new StringBuilder();
                    builder.append(" " + ChatColor.WHITE + caravan.getBeneficiary().getName() + " (");
                    final Faction faction = caravan.getFaction();
                    if (faction != null) {
                        builder.append(faction.describeTo(MPlayer.get(sender)) + ChatColor.WHITE + ") ");
                    }
                    builder.append(ChatColor.GREEN + Util.format(caravan.getInvestment()));
                    sender.sendMessage(builder.toString());
                }

                if (!caravans.isEmpty()) {
                    sender.sendMessage(ChatColor.GOLD + "");
                    sender.sendMessage(
                            ChatColor.GOLD + " Page " + pageNumber + " of " + Math.max(caravans.size() / PAGE_SIZE, 1));
                }
                return true;
            } else if (secondary.equalsIgnoreCase("track") && args.length == 2 && sender instanceof Player) {

                final String playerName = args[1];
                final Beneficiary targetBeneficiary = beneficiaryRepository.findByName(playerName);
                if (targetBeneficiary == null) {
                    sender.sendMessage("Can't find that player, are you sure you typed it right?");
                    return true;
                }

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

    private String makeHeader(String text) {
        final StringBuilder stringBuilder = new StringBuilder(ChatColor.GOLD + "▀▀▀ " + text + " ");
        for (int i = 0; i < 40 - text.length(); i++) {
            stringBuilder.append("▀");
        }
        return stringBuilder.toString();
    }

}
