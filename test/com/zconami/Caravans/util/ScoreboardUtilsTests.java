package com.zconami.Caravans.util;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.util.RelationUtil;
import com.zconami.Caravans.domain.Beneficiary;
import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.mock.MockObjective;

@PrepareForTest({
        MPlayer.class,
        Utils.class,
        Rel.class,
        RelationUtil.class
})
public class ScoreboardUtilsTests extends PowerMockTestCase {

    @DataProvider
    public Object[][] testScoreboardDisplayName() {
        return new Object[][] {
                new Object[] {
                        "Player",
                        "Faction",
                        "Player (" + ChatColor.RED + "Faction" + ChatColor.WHITE + ")"
                }, new Object[] {
                        "Player1234567890123",
                        "Faction",
                        "Player1234567890123 (" + ChatColor.RED + "Fac..." + ChatColor.WHITE + ")"
                }, new Object[] {
                        "Player12345678901234",
                        "Faction",
                        "Player12345678901234 (" + ChatColor.RED + "Fa..." + ChatColor.WHITE + ")"
                }, new Object[] {
                        "Player123456789012345",
                        "Faction",
                        "Player123456789012345 (" + ChatColor.RED + "F..." + ChatColor.WHITE + ")"
                }, new Object[] {
                        "Player1234567890123456",
                        "Faction",
                        "Player1234567890123456"
                }, new Object[] {
                        "Player12345678901234567890123456",
                        "Faction",
                        "Player12345678901234567890123456"
                }, new Object[] {
                        "Player123456789012345678901234567",
                        "Faction123456789012",
                        "Player12345678901234567890123..."
                }, new Object[] {
                        "Player",
                        "Faction12345678901",
                        "Player (" + ChatColor.RED + "Faction12345678901" + ChatColor.WHITE + ")"
                }, new Object[] {
                        "Player",
                        "Faction123456789012",
                        "Player (" + ChatColor.RED + "Faction123456789..." + ChatColor.WHITE + ")"
                }, new Object[] {
                        "Player",
                        "Faction1234567890123456789012345",
                        "Player (" + ChatColor.RED + "Faction123456789..." + ChatColor.WHITE + ")"
                }, new Object[] {
                        "Player",
                        "Faction12345678901234567890123456",
                        "Player (" + ChatColor.RED + "Faction123456789..." + ChatColor.WHITE + ")"
                }
        };
    }

    @Test(dataProvider = "testScoreboardDisplayName")
    public void testScoreboardDisplayName(String beneficiaryName, String factionName, String expectedDisplayName) {

        final Rel mockRel = mock(Rel.class);
        when(mockRel.getColor()).thenReturn(ChatColor.RED);

        mockStatic(Rel.class);
        when(Rel.values()).thenReturn(new Rel[] {
                mockRel
        });

        final String caravanKey = UUID.randomUUID().toString();

        final Score mockScore = mock(Score.class);

        final MockObjective mockObjective = mock(MockObjective.class);
        when(mockObjective.getScore(Mockito.anyString())).thenReturn(mockScore);
        Mockito.doCallRealMethod().when(mockObjective).setDisplayName(Mockito.anyString());
        when(mockObjective.getDisplayName()).thenCallRealMethod();

        final Scoreboard mockScoreboard = mock(Scoreboard.class);
        when(mockScoreboard.registerNewObjective(caravanKey.substring(0, 15), "dummy")).thenReturn(mockObjective);
        when(mockScoreboard.getObjective(DisplaySlot.SIDEBAR)).thenReturn(mockObjective);

        final ScoreboardManager mockScoreboardManager = mock(ScoreboardManager.class);
        when(mockScoreboardManager.getNewScoreboard()).thenReturn(mockScoreboard);

        final BukkitScheduler mockScheduler = mock(BukkitScheduler.class);

        mockStatic(Utils.class);
        when(Utils.getScoreboardManager()).thenReturn(mockScoreboardManager);
        when(Utils.getGringottsNamePlural()).thenReturn("Emeralds");
        when(Utils.getScheduler()).thenReturn(mockScheduler);

        final Faction mockFaction = mock(Faction.class);
        when(mockFaction.getName()).thenReturn(factionName);

        final Beneficiary mockBeneficiary = mock(Beneficiary.class);
        when(mockBeneficiary.getName()).thenReturn(beneficiaryName);
        when(mockBeneficiary.getFaction()).thenReturn(mockFaction);

        final Caravan mockCaravan = mock(Caravan.class);
        when(mockCaravan.getFaction()).thenReturn(mockFaction);
        when(mockCaravan.getBeneficiary()).thenReturn(mockBeneficiary);
        when(mockCaravan.getKey()).thenReturn(caravanKey);

        final MPlayer mockMPlayer = mock(MPlayer.class);
        when(mockMPlayer.getFaction()).thenReturn(mockFaction);

        final Player mockPlayer = mock(Player.class);

        mockStatic(MPlayer.class);
        when(MPlayer.get(mockPlayer)).thenReturn(mockMPlayer);

        mockStatic(RelationUtil.class);
        when(RelationUtil.getRelationOfThatToMe(mockFaction, mockMPlayer)).thenReturn(mockRel);

        final Scoreboard createdScoreboard = ScoreboardUtils.showScoreboard(mockPlayer, mockCaravan);
        final String createdDisplayName = createdScoreboard.getObjective(DisplaySlot.SIDEBAR).getDisplayName();

        assertTrue(createdDisplayName.length() <= ScoreboardUtils.DISPLAY_NAME_MAX_LENGTH);
        assertEquals(expectedDisplayName, createdDisplayName);
    }

}
