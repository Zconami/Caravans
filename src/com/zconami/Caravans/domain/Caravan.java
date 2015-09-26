package com.zconami.Caravans.domain;

import static com.zconami.Caravans.util.Utils.broadcastMessage;
import static com.zconami.Caravans.util.Utils.getCaravansConfig;
import static com.zconami.Caravans.util.Utils.getCaravansPlugin;
import static com.zconami.Caravans.util.Utils.getLogger;
import static com.zconami.Caravans.util.Utils.getOnlinePlayers;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;
import org.gestern.gringotts.AccountInventory;
import org.gestern.gringotts.Util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.zconami.Caravans.CaravansPlugin;
import com.zconami.Caravans.storage.DataKey;
import com.zconami.Caravans.util.DynmapUtils;
import com.zconami.Caravans.util.NMSUtils;
import com.zconami.Caravans.util.ScoreboardUtils;
import com.zconami.Caravans.util.Utils;

import net.minecraft.server.v1_8_R3.EntityHorse;

public class Caravan extends LinkedEntity<Horse, EntityHorse> {

    // ===================================
    // INNER CLASSES
    // ===================================

    public enum ProfitMultiplyerStrategy {
        PvE {
            @Override
            protected double calculateExponent(Region origin, Location destination) {
                final double reward = getCaravansConfig().getDouble("caravans.profitMultiplyer.PvEReward");
                return distanceByReward(origin, destination, reward);
            }
        },
        PvP {
            @Override
            protected double calculateExponent(Region origin, Location destination) {
                final double reward = getCaravansConfig().getDouble("caravans.profitMultiplyer.PvPReward");
                return distanceByReward(origin, destination, reward);
            }
        };

        private static double distanceByReward(Region origin, Location destination, double reward) {
            final double distance = destination.distance(origin.getCenter());
            return 1.0 + (distance * reward);
        }

        protected abstract double calculateExponent(Region origin, Location destination);

    }

    private enum CreateStrategy {
        MULE(Horse.Variant.MULE) {
            @Override
            protected Horse apply(Horse horse) {
                horse.setAdult();
                horse.setVariant(Horse.Variant.MULE);
                horse.setCustomName("§cCaravan");
                horse.setCarryingChest(true);
                horse.setTamed(true);
                horse.setMaxHealth(200);
                horse.setHealth(200);
                horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                NMSUtils.setHorseSpeed(horse, 0.14f);
                return horse;
            }
        };

        private Horse.Variant variant;

        CreateStrategy(Horse.Variant variant) {
            this.variant = variant;
        }

        protected abstract Horse apply(Horse horse);

        public static Horse applyStrategy(Horse.Variant variant, Horse horse) {
            for (CreateStrategy strategy : CreateStrategy.values()) {
                if (strategy.variant.equals(variant)) {
                    return strategy.apply(horse);
                }
            }
            return null;
        }

    }

    // ===================================
    // ATTRIBUTES
    // ===================================

    public static final String BENEFICIARY = "beneficiary";
    private Beneficiary beneficiary;

    public static final String PROFIT_STRATEGY = "profitStrategy";
    private ProfitMultiplyerStrategy profitStrategy;

    public static final String ORIGIN = "origin";
    private Region origin;

    public static final String LOCATION_PUBLIC = "locationPublic";
    private boolean locationPublic;

    public static final String INVESTMENT = "investment";
    private long investment;
    private AccountInventory caravanAccount;

    private final Faction faction;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public Caravan(Horse horse, DataKey extraData) {
        super(horse, extraData);
        this.faction = beneficiary.getFaction();
        synchronizeInvestment();
    }

    private Caravan(CaravanCreateParameters params) {
        super(params);
        this.beneficiary = params.getBeneficiary();
        this.faction = beneficiary.getFaction();
        this.origin = params.getOrigin();
        this.profitStrategy = params.getProfitStrategy();
        this.investment = params.getInvestment();

        synchronizeInvestment();
        caravanHasStarted();
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public static Caravan muleCaravan(Beneficiary beneficiary, Region origin, Inventory inventory) {
        Preconditions.checkNotNull(beneficiary);
        Preconditions.checkNotNull(origin);
        Preconditions.checkNotNull(inventory);

        getLogger().info("Creating mule caravan");

        final Block block = origin.getCenter().getBlock();
        Sign sign = (Sign) block.getState().getData();
        final Location location = block.getRelative(sign.getFacing()).getLocation();

        final World world = location.getWorld();
        final Horse horse = CreateStrategy.applyStrategy(Horse.Variant.MULE,
                (Horse) world.spawnEntity(location, EntityType.HORSE));

        final long investmentBalance = new AccountInventory(inventory).balance();

        final ProfitMultiplyerStrategy profitStrategy;

        final Set<Faction> onlineFactions = Sets.newHashSet();
        for (Faction faction : getOnlinePlayers().stream().map(player -> MPlayer.get(player).getFaction())
                .collect(Collectors.toList())) {
            if (!faction.getId().equals(Factions.ID_NONE) && !faction.getId().equals(Factions.ID_SAFEZONE)
                    && !faction.getId().equals(Factions.ID_WARZONE)) {
                onlineFactions.add(faction);
            }
        }
        if (onlineFactions.size() >= 2) {
            profitStrategy = ProfitMultiplyerStrategy.PvP;
        } else {
            profitStrategy = ProfitMultiplyerStrategy.PvE;
        }

        final CaravanCreateParameters params = new CaravanCreateParameters(horse, beneficiary, origin, profitStrategy,
                investmentBalance);

        return new Caravan(params);
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public long getInvestment() {
        return new AccountInventory(this.getBukkitEntity().getInventory()).balance();
    }

    public long getReturn(Region destination) {
        return getReturn(destination.getCenter());
    }

    public long getReturn(Location destination) {
        final double calculatedExpoent = getProfitStrategy().calculateExponent(getOrigin(), destination);
        final double calculatedReturn = Math.pow(getInvestment(), calculatedExpoent);
        return (long) Math.floor(calculatedReturn);
    }

    public Region getOrigin() {
        return origin;
    }

    public Faction getFaction() {
        return faction;
    }

    public void synchronizeInvestment() {
        this.caravanAccount = new AccountInventory(this.getBukkitEntity().getInventory());
        caravanAccount.remove(caravanAccount.balance());
        this.caravanAccount.add(investment);
    }

    public void caravanHasStarted() {

        final CaravansPlugin plugin = getCaravansPlugin();
        final boolean announceStart = getCaravansConfig().getBoolean("broadcasts.announceStart");
        final int announceLocationDelay = getCaravansConfig().getInt("broadcasts.announceLocationDelay");
        if (announceStart) {
            final String beneficiaryName = this.getBeneficiary().getName();
            StringBuilder announcementBuilder = new StringBuilder(String.format(
                    "A %s caravan with a cargo of %s worth §a%s§f has started for %s", this.getProfitStrategy().name(),
                    origin.getTypeOfGood(), Util.format(this.getInvestment()), beneficiaryName));
            if (announceLocationDelay <= 0) {
                final Location location = getBukkitEntity().getLocation();
                announcementBuilder.append(String.format(" @ %d,%d!", location.getBlockX(), location.getBlockZ()));
                ScoreboardUtils.setUpScoreboardCaravanTask(this);
                DynmapUtils.setupDynmapCaravanTask(this);
                locationIsPublic();
            } else {
                announcementBuilder.append("!");
                plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (getBukkitEntity().isValid()) {
                            final Location location = getBukkitEntity().getLocation();
                            broadcastMessage(String.format("The location of %s's caravan is %d,%d", beneficiaryName,
                                    location.getBlockX(), location.getBlockZ()));
                            ScoreboardUtils.setUpScoreboardCaravanTask(Caravan.this);
                            DynmapUtils.setupDynmapCaravanTask(Caravan.this);
                            locationIsPublic();
                        }
                    }
                }, Utils.ticksFromSeconds(announceLocationDelay));
            }
            broadcastMessage(announcementBuilder.toString());
        }

    }

    public boolean isLocationPublic() {
        return locationPublic;
    }

    public void locationIsPublic() {
        if (this.locationPublic == false) {
            this.locationPublic = true;
            this.setDirty(true);
        }
    }

    public ProfitMultiplyerStrategy getProfitStrategy() {
        return this.profitStrategy;
    }

    public boolean locationAwaitingBroadcast() {
        return !this.isLocationPublic();
    }

    public boolean locationBroadcasted() {
        return this.isLocationPublic();
    }

    public boolean hasPassenger() {
        return this.getBukkitEntity().getPassenger() != null;
    }

    public void dismount() {
        this.getBukkitEntity().eject();
    }

    // ===================================
    // IMPLEMENTATION OF Entity
    // ===================================

    @Override
    public void remove() {
        super.remove();
        ScoreboardUtils.stopScoreboard(this);
        DynmapUtils.stopCaravanDynmap(this);
        getBukkitEntity().eject();
        new AccountInventory(this.getBukkitEntity().getInventory()).remove(getInvestment());
        getBukkitEntity().remove();
    }

    @Override
    public void writeData(DataKey dataKey) {
        super.writeData(dataKey);
        dataKey.setString(BENEFICIARY, beneficiary.getKey());
        dataKey.setString(ORIGIN, origin.getKey());
        dataKey.setString(PROFIT_STRATEGY, profitStrategy.name());
        dataKey.setBoolean(LOCATION_PUBLIC, locationPublic);
        dataKey.setLong(INVESTMENT, getInvestment());
    }

    @Override
    public void readData(DataKey dataKey) {
        super.readData(dataKey);

        final UUID ownerUUID = UUID.fromString(dataKey.getString(BENEFICIARY));
        this.beneficiary = getCaravansPlugin().getBeneficiaryRepository().find(ownerUUID.toString());

        final String originKey = dataKey.getString(ORIGIN);
        this.origin = getCaravansPlugin().getRegionRepository().find(originKey);

        final String profitStrategyName = dataKey.getString(PROFIT_STRATEGY);
        this.profitStrategy = ProfitMultiplyerStrategy.valueOf(profitStrategyName);

        this.locationPublic = dataKey.getBoolean(LOCATION_PUBLIC);

        this.investment = dataKey.getLong(INVESTMENT);
    }

    // ===================================
    // IMPLEMENTATION OF LinkedEntity
    // ===================================

    @Override
    public Class<Horse> getBukkitEntityType() {
        return Horse.class;
    }

}
