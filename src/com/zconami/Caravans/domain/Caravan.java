package com.zconami.Caravans.domain;

import static com.zconami.Caravans.util.Utils.getCaravansPlugin;
import static com.zconami.Caravans.util.Utils.getLogger;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.gestern.gringotts.AccountInventory;

import com.google.common.collect.Sets;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.zconami.Caravans.repository.BeneficiaryRepository;
import com.zconami.Caravans.repository.RegionRepository;
import com.zconami.Caravans.storage.DataKey;
import com.zconami.Caravans.util.NMSUtils;
import com.zconami.Caravans.util.ScoreboardUtils;

import net.minecraft.server.v1_8_R3.EntityHorse;

public class Caravan extends LinkedEntity<Horse, EntityHorse> {

    // ===================================
    // INNER CLASSES
    // ===================================

    public enum ProfitMultiplyerStrategy {
        PvE {
            @Override
            protected double calculate(Region origin, Region destination) {
                final double reward = getCaravansPlugin().getConfig().getDouble("caravans.profitMultiplyer.PvEReward");
                return distanceOverBlockDivisorByReward(origin, destination, reward);
            }
        },
        PvP {
            @Override
            protected double calculate(Region origin, Region destination) {
                final double reward = getCaravansPlugin().getConfig().getDouble("caravans.profitMultiplyer.PvPReward");
                return distanceOverBlockDivisorByReward(origin, destination, reward);
            }
        };

        private static double distanceOverBlockDivisorByReward(Region origin, Region destination, double reward) {
            final int blockDivisor = getCaravansPlugin().getConfig().getInt("caravans.profitMultiplyer.blockDivisor");
            final double distance = destination.getCenter().distance(origin.getCenter());
            return 1.0 + (Math.max(distance / blockDivisor, 1) * reward);
        }

        protected abstract double calculate(Region origin, Region destination);

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
                NMSUtils.setHorseSpeed(horse, 0.10000000149f);
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

    public static final String CARAVAN_STARTED = "caravanStarted";
    private boolean caravanStarted = false;

    public static final String BENEFICIARY = "beneficiary";
    private Beneficiary beneficiary;

    public static final String PROFIT_STRATEGY = "profitStrategy";
    private ProfitMultiplyerStrategy profitStrategy;

    public static final String ORIGIN = "origin";
    private Region origin;

    public static final String LOCATION_PUBLIC = "locationPublic";
    private boolean locationPublic = false;

    private final Faction faction;
    private final AccountInventory accountInventory;

    private RegionRepository regionRepository;
    private BeneficiaryRepository beneficiaryRepository;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public Caravan(Horse horse, DataKey extraData) {
        super(horse, extraData);
        this.accountInventory = new AccountInventory(horse.getInventory());
        this.faction = MPlayer.get(beneficiary.getBukkitEntity()).getFaction();
        getRepositories();
    }

    private Caravan(CaravanCreateParameters params) {
        super(params);
        this.beneficiary = params.getBeneficiary();
        this.faction = MPlayer.get(beneficiary.getBukkitEntity()).getFaction();
        this.origin = params.getOrigin();
        this.profitStrategy = params.getProfitStrategy();
        this.accountInventory = new AccountInventory(params.getBukkitEntity().getInventory());
        getRepositories();
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public static Caravan muleCaravan(Beneficiary beneficiary, Region origin) {
        getLogger().info("Creating mule caravan");

        final Location location = origin.getCenter();

        final World world = location.getWorld();
        final Horse horse = CreateStrategy.applyStrategy(Horse.Variant.MULE,
                (Horse) world.spawnEntity(location, EntityType.HORSE));

        final ProfitMultiplyerStrategy profitStrategy;

        final Set<Faction> onlineFactions = Sets.newHashSet();
        for (Faction faction : Bukkit.getOnlinePlayers().stream().map(player -> MPlayer.get(player).getFaction())
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

        final CaravanCreateParameters params = new CaravanCreateParameters(horse, beneficiary, origin, profitStrategy);

        return new Caravan(params);
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public long getInvestment() {
        return this.accountInventory.balance();
    }

    public long getReturn(Region destination) {
        return getInvestment() + (long) Math.floor(this.profitStrategy.calculate(origin, destination));
    }

    public Region getOrigin() {
        return origin;
    }

    public Faction getFaction() {
        return faction;
    }

    public boolean isCaravanStarted() {
        return this.caravanStarted;
    }

    public void caravanHasStarted() {
        if (this.caravanStarted == false) {
            this.caravanStarted = true;
            this.setDirty(true);
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

    // ===================================
    // PRIVATE METHODS
    // ===================================

    public void getRepositories() {
        this.beneficiaryRepository = getCaravansPlugin().getBeneficiaryRepository();
        this.regionRepository = getCaravansPlugin().getRegionRepository();
    }

    // ===================================
    // IMPLEMENTATION OF Entity
    // ===================================

    @Override
    public void remove() {
        super.remove();
        getBukkitEntity().eject();
        accountInventory.remove(getInvestment());
        getBukkitEntity().remove();
        ScoreboardUtils.stopScoreboard(this);
    }

    @Override
    public void writeData(DataKey dataKey) {
        dataKey.setBoolean(CARAVAN_STARTED, caravanStarted);

        dataKey.setString(BENEFICIARY, beneficiary.getKey());

        dataKey.setString(ORIGIN, origin.getKey());

        dataKey.setString(PROFIT_STRATEGY, profitStrategy.name());

        dataKey.setBoolean(LOCATION_PUBLIC, locationPublic);
    }

    @Override
    public void readData(DataKey dataKey) {
        this.caravanStarted = dataKey.getBoolean(CARAVAN_STARTED);

        final UUID ownerUUID = UUID.fromString(dataKey.getString(BENEFICIARY));
        final Player player = Bukkit.getPlayer(ownerUUID);
        this.beneficiary = beneficiaryRepository.find(player);

        final String originKey = dataKey.getString(ORIGIN);
        this.origin = regionRepository.find(originKey);

        final String profitStrategyName = dataKey.getString(PROFIT_STRATEGY);
        this.profitStrategy = ProfitMultiplyerStrategy.valueOf(profitStrategyName);

        this.locationPublic = dataKey.getBoolean(LOCATION_PUBLIC);
    }

    // ===================================
    // IMPLEMENTATION OF LinkedEntity
    // ===================================

    @Override
    public Class<Horse> getBukkitEntityType() {
        return Horse.class;
    }

}
