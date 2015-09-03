package com.zconami.Caravans;

import static com.zconami.Caravans.util.Utils.getLogger;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.MPlayer;
import com.zconami.Caravans.domain.Beneficiary;
import com.zconami.Caravans.domain.BeneficiaryCreateParameters;
import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.domain.Region;
import com.zconami.Caravans.domain.RegionCreateParameters;
import com.zconami.Caravans.event.CaravanCreateEvent;
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
                sender.sendMessage("/caravan createCaravan <region> <beneficiary> - spawn a trade caravan");
                sender.sendMessage(
                        "/caravan createRegion <name> <radius> [isOrigin] [isDestination] - spawn a trade caravan");
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
            } else if (secondary.equalsIgnoreCase("createCaravan") && args.length >= 2 && sender instanceof Player) {

                if (sender instanceof Player) {
                    final Region origin = regionRepository.findByName(args[1]);
                    if (origin == null || !origin.isOrigin()) {
                        sender.sendMessage("Given region does not exist or is not a valid origin!");
                        return false;
                    }

                    final Player target = args.length == 1 ? (Player) sender : Bukkit.getPlayer(args[2]);
                    if (MPlayer.get(target).getFaction().equals(Factions.ID_NONE)) {
                        sender.sendMessage("Beneficiary must be in a faction to have a caravan!");
                        return false;
                    }

                    final Beneficiary beneficiary = findOrCreate(target);

                    final Caravan caravan = caravanRepository
                            .save(Caravan.muleCaravan(beneficiary, origin));
                    final CaravanCreateEvent caravanCreateEvent = new CaravanCreateEvent(caravan);
                    Bukkit.getServer().getPluginManager().callEvent(caravanCreateEvent);
                    return true;
                } else {
                    sender.sendMessage("Player only atm");
                    return false;
                }

            } else if (secondary.equalsIgnoreCase("createRegion") && args.length >= 3) {
                final String name = args[1];
                final int radius = Integer.valueOf(args[2]).intValue();
                final Location location = ((Player) sender).getLocation();
                final RegionCreateParameters params = new RegionCreateParameters(UUID.randomUUID().toString(), name,
                        location, radius);

                if (args.length == 4) {
                    final boolean isOrigin = Boolean.valueOf(args[3]).booleanValue();
                    params.setOrigin(isOrigin);
                }

                if (args.length == 5) {
                    final boolean isDestination = Boolean.valueOf(args[4]).booleanValue();
                    params.setDestination(isDestination);
                }

                regionRepository.save(Region.create(params));
                return true;
            }
        }
        return false;
    }

    private Beneficiary findOrCreate(Player player) {
        final Beneficiary existing = beneficiaryRepository.find(player);
        if (existing != null) {
            return existing;
        }
        final BeneficiaryCreateParameters params = new BeneficiaryCreateParameters(player);
        return beneficiaryRepository.save(Beneficiary.create(params));
    }

}
