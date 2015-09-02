package com.zconami.Caravans.util;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.GenericAttributes;

public class NMSUtils {

    // ===================================
    // CONSTRUCTORS
    // ===================================

    private NMSUtils() {
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public static void setHorseSpeed(Horse horse, float speed) {
        EntityLiving handle = getHandle(horse);
        ((EntityHorse) handle).getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.10000000149011612D);
    }

    public static EntityLiving getHandle(LivingEntity entity) {
        return (EntityLiving) getHandle((org.bukkit.entity.Entity) entity);
    }

    public static Entity getHandle(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof CraftEntity))
            return null;
        return ((CraftEntity) entity).getHandle();
    }

}
