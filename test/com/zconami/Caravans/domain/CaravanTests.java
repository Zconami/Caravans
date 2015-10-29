package com.zconami.Caravans.domain;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.HorseInventory;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.massivecraft.factions.entity.Faction;
import com.zconami.Core.util.NMSUtils;
import com.zconami.Core.util.Utils;

import net.minecraft.server.v1_8_R3.EntityHorse;

@PrepareForTest({
        NMSUtils.class,
        Utils.class
})
@Test
public class CaravanTests extends BaseLinkedEntityTests {

    public void testCaravanCreate() {
        final Logger mockLogger = mock(Logger.class);

        mockStatic(Utils.class);
        when(Utils.getOnlinePlayers()).thenReturn(Lists.newArrayList());
        when(Utils.getLogger()).thenReturn(mockLogger);

        final HorseInventory mockHorseInventory = mock(HorseInventory.class);

        final Chunk mockChunk = mock(Chunk.class);

        final World mockWorld = mock(World.class);

        final Location mockLocation = mock(Location.class);
        when(mockLocation.getWorld()).thenReturn(mockWorld);
        when(mockLocation.getChunk()).thenReturn(mockChunk);

        final org.bukkit.entity.Horse mockHorse = mock(org.bukkit.entity.Horse.class);
        when(mockHorse.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockHorse.getInventory()).thenReturn(mockHorseInventory);
        when(mockHorse.getLocation()).thenReturn(mockLocation);

        when(mockWorld.spawnEntity(Mockito.any(Location.class), Mockito.any())).thenReturn(mockHorse);

        final EntityHorse mockNMSHorse = mock(EntityHorse.class);

        mockStatic(NMSUtils.class);
        when(NMSUtils.getMinecraftEntity(mockHorse)).thenReturn(mockNMSHorse);

        final Faction mockFaction = mock(Faction.class);

        final Beneficiary mockBeneficiary = mock(Beneficiary.class);
        when(mockBeneficiary.getFaction()).thenReturn(mockFaction);

        final Region mockOrigin = mock(Region.class);
        when(mockOrigin.getCenter()).thenReturn(mockLocation);

        final Caravan caravan = Caravan.muleCaravan(mockBeneficiary, mockOrigin);
        assertNotNull(caravan);

        Mockito.verify(mockWorld).spawnEntity(Mockito.any(), Mockito.any());

        Mockito.verify(mockHorse).setAdult();
        Mockito.verify(mockHorse).setVariant(Horse.Variant.MULE);
        Mockito.verify(mockHorse).setCustomName("§cCaravan");
        Mockito.verify(mockHorse).setCarryingChest(true);
        Mockito.verify(mockHorse).setTamed(true);
        Mockito.verify(mockHorse).setMaxHealth(200);
        Mockito.verify(mockHorse).setHealth(200);
        Mockito.verify(mockHorseInventory).setSaddle(Mockito.any());
    }

}
