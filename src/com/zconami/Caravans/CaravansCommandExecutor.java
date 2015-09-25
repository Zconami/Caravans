package com.zconami.Caravans;

import static com.zconami.Caravans.util.Utils.getLogger;
import static com.zconami.Caravans.util.Utils.sendMessage;
import static com.zconami.Caravans.util.Utils.sendTable;

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
import com.zconami.Caravans.util.ItemCallback;
import com.zconami.Caravans.util.ScoreboardUtils;

public class CaravansCommandExecutor implements CommandExecutor {

    // ===================================
    // CONSTANTS
    // ===================================

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

                final List<Caravan> caravans = caravanRepository.all().stream().filter(Caravan::locationBroadcasted)
                        .collect(Collectors.toList());

                final String tableDescription = " Use " + ChatColor.BLUE + "/c track <playerName>" + ChatColor.GOLD
                        + " to track location";
                sendTable(sender, pageNumber.intValue(), "PUBLICALLY KNOWN CARAVANS", tableDescription, caravans,
                        new ItemCallback<Caravan>() {
                            @Override
                            public String itemEntry(Caravan caravan) {
                                final StringBuilder builder = new StringBuilder();
                                builder.append(ChatColor.WHITE + caravan.getBeneficiary().getName() + " (");
                                final Faction faction = caravan.getFaction();
                                if (faction != null) {
                                    builder.append(faction.describeTo(MPlayer.get(sender)) + ChatColor.WHITE + ") ");
                                }
                                builder.append(ChatColor.GREEN + Util.format(caravan.getInvestment()));
                                return builder.toString();
                            }
                        });
                return true;
            } else if (secondary.equalsIgnoreCase("track") && args.length == 2 && sender instanceof Player) {

                final String playerName = args[1];
                final Beneficiary targetBeneficiary = beneficiaryRepository.findByName(playerName);
                if (targetBeneficiary == null) {
                    sendMessage(sender, "Can't find that player, are you sure you typed it right?");
                    return true;
                }

                final Caravan targetCaravan = caravanRepository.findByBeneficiary(targetBeneficiary);
                if (targetCaravan.isLocationPublic()) {
                    ScoreboardUtils.showScoreboard((Player) sender, targetCaravan);
                    sendMessage(sender, "Now tracking " + playerName + "'s caravan");
                } else {
                    sendMessage(sender, "Could not find that caravan, has their location been broadcast yet?");
                }
                return true;
            } else {
                sendMessage(sender, "/c help - this help page");
                sendMessage(sender, "/c list - lists active caravans with public locations");
                sendMessage(sender, "/c track <playerName> - track player's caravan");
                return true;
            }
        }
        return false;
    }
}
