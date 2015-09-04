package com.zconami.Caravans.domain;

import static com.zconami.Caravans.util.Utils.getCaravansPlugin;
import static com.zconami.Caravans.util.Utils.getLogger;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

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
import com.zconami.Caravans.util.NMSUtils;

import net.minecraft.server.v1_8_R3.EntityHorse;

@Entity
@Table(name = Caravan.TABLE)
public class Caravan extends LinkedEntity<Horse, EntityHorse> {

    // ===================================
    // CONSTANTS
    // ===================================

    public static final String TABLE = TABLE_PREFIX + "caravan";

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
    @Column(nullable = false)
    private boolean caravanStarted = false;

    public static final String BENEFICIARY = "beneficiaryId";
    @Column(nullable = false)
    private UUID beneficiaryId;
    @Transient
    private Beneficiary beneficiary;

    public static final String PROFIT_STRATEGY = "profitStrategy";
    private ProfitMultiplyerStrategy profitStrategy;

    public static final String ORIGIN = "originId";
    @Column(nullable = false)
    private UUID originId;
    @Transient
    private Region origin;

    public static final String LOCATION_PUBLIC = "locationPublic";
    @Column(nullable = false)
    private boolean locationPublic = false;

    @Transient
    private Faction faction;

    @Transient
    private AccountInventory accountInventory;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public Caravan() {
        setTransientProperties();
    }

    private Caravan(CaravanCreateParameters params) {
        super(params);
        this.beneficiary = params.getBeneficiary();
        this.beneficiaryId = beneficiary.getId();

        this.origin = params.getOrigin();
        this.originId = origin.getId();

        this.profitStrategy = params.getProfitStrategy();
        setTransientProperties();
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
        final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        for (Player onlinePlayer : onlinePlayers) {
            final Faction playersFaction = MPlayer.get(onlinePlayer).getFaction();
            if (!playersFaction.getId().equals(Factions.ID_NONE) && !playersFaction.getId().equals(Factions.ID_SAFEZONE)
                    && !playersFaction.getId().equals(Factions.ID_WARZONE)) {
                onlineFactions.add(playersFaction);
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
        }
    }

    public boolean isLocationPublic() {
        return locationPublic;
    }

    public void locationIsPublic() {
        if (this.locationPublic == false) {
            this.locationPublic = true;
        }
    }

    public ProfitMultiplyerStrategy getProfitStrategy() {
        return profitStrategy;
    }

    public void setProfitStrategy(ProfitMultiplyerStrategy profitStrategy) {
        this.profitStrategy = profitStrategy;
    }

    public void setCaravanStarted(boolean caravanStarted) {
        this.caravanStarted = caravanStarted;
    }

    public void setBeneficiary(Beneficiary beneficiary) {
        this.beneficiary = beneficiary;
        setTransientFaction();
    }

    public void setOrigin(Region origin) {
        this.origin = origin;
    }

    public void setLocationPublic(boolean locationPublic) {
        this.locationPublic = locationPublic;
    }

    public UUID getBeneficiaryId() {
        return beneficiaryId;
    }

    public UUID getOriginId() {
        return originId;
    }

    public void setBeneficiaryId(UUID beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
        setTransientBeneficiary();
    }

    public void setOriginId(UUID originId) {
        this.originId = originId;
        setTransientOrigin();
    }

    // ===================================
    // IMPLEMENTATION OF LinkedEntity
    // ===================================

    @Override
    public Class<Horse> getBukkitEntityType() {
        return Horse.class;
    }

    @Override
    public void setTransientFromBukkitEntity() {
        setTransientAccountInventory();
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private void setTransientProperties() {
        setTransientFaction();
        setTransientAccountInventory();
        setTransientOrigin();
        setTransientBeneficiary();
    }

    private void setTransientFaction() {
        if (beneficiary != null) {
            this.faction = MPlayer.get(beneficiary.getBukkitEntity()).getFaction();
        }
    }

    private void setTransientAccountInventory() {
        if (this.getBukkitEntity() != null) {
            this.accountInventory = new AccountInventory(this.getBukkitEntity().getInventory());
        }
    }

    private void setTransientOrigin() {
        if (this.originId != null) {
            setOrigin(getCaravansPlugin().DB.find(Region.class).where().eq(Region.ID, originId).findUnique());
        }
    }

    private void setTransientBeneficiary() {
        if (this.beneficiaryId != null) {
            setBeneficiary(getCaravansPlugin().DB.find(Beneficiary.class).where().eq(Beneficiary.ID, beneficiaryId)
                    .findUnique());
        }
    }

}
