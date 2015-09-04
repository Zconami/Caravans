package com.zconami.Caravans;

import static com.zconami.Caravans.util.Utils.getLogger;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        if (command.getName().equalsIgnoreCase("caravan") && args.length > 0) {
            final String secondary = args[0];
            if (secondary.equalsIgnoreCase("help")) {
                sender.sendMessage("/caravan help - this help page");
                sender.sendMessage("/caravan list - lists active caravans with public locations");
                sender.sendMessage("/caravan track <caravanIndex> - track caravan from list");
                return true;
            } else if (secondary.equalsIgnoreCase("list")) {
                int index = 1;
                for (Caravan caravan : caravanRepository.all().stream()
                        .filter(caravan -> caravan.isCaravanStarted() && caravan.isLocationPublic())
                        .collect(Collectors.toList())) {
                    sender.sendMessage(index + ": " + caravan.getBeneficiary().getBukkitEntity().getName());
                    index++;
                }
                return true;
            } else if (secondary.equalsIgnoreCase("track") && args.length == 2 && sender instanceof Player) {
                final int index = Integer.valueOf(args[1]).intValue() - 1;
                final List<Caravan> caravans = caravanRepository.all().stream()
                        .filter(caravan -> caravan.isCaravanStarted() && caravan.isLocationPublic())
                        .collect(Collectors.toList());
                if (caravans.size() - 1 >= index) {
                    final Caravan selected = caravans.get(index);
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
