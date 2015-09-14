package com.zconami.Caravans.domain;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.Test;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;

@PrepareForTest(MPlayer.class)
@Test
public class BeneficiaryTests extends BaseEntityTests {

    public void testBeneficiaryCreate() {
        final Faction mockFaction = mock(Faction.class);

        final MPlayer mockMPlayer = mock(MPlayer.class);
        when(mockMPlayer.getFaction()).thenReturn(mockFaction);

        final Player mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn("Player");
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());

        mockStatic(MPlayer.class);
        when(MPlayer.get(mockPlayer)).thenReturn(mockMPlayer);

        final BeneficiaryCreateParameters params = new BeneficiaryCreateParameters(mockPlayer);
        final Beneficiary beneficiary = Beneficiary.create(params);
        assertNotNull(beneficiary);
    }

}
