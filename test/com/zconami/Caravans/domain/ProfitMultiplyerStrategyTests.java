package com.zconami.Caravans.domain;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.AssertJUnit.assertEquals;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.zconami.Caravans.domain.Caravan.ProfitMultiplyerStrategy;
import com.zconami.Caravans.util.Utils;

@PrepareForTest(Utils.class)
public class ProfitMultiplyerStrategyTests extends PowerMockTestCase {

    @DataProvider
    public Object[][] testProfitMultiplyer() {
        return new Object[][] {
                new Object[] {
                        64,
                        10,
                        0.0001,
                        64
                }, new Object[] {
                        64,
                        10,
                        0.000035,
                        64
                }, new Object[] {
                        64,
                        100,
                        0.0001,
                        66
                }, new Object[] {
                        64,
                        100,
                        0.000035,
                        64
                }, new Object[] {
                        64,
                        500,
                        0.0001,
                        78
                }, new Object[] {
                        64,
                        500,
                        0.000035,
                        68
                }, new Object[] {
                        64,
                        1000,
                        0.0001,
                        97
                }, new Object[] {
                        64,
                        1000,
                        0.000035,
                        74
                }, new Object[] {
                        64,
                        1500,
                        0.0001,
                        119
                }, new Object[] {
                        64,
                        1500,
                        0.000035,
                        79
                }, new Object[] {
                        64,
                        2000,
                        0.0001,
                        147
                }, new Object[] {
                        64,
                        2000,
                        0.000035,
                        85
                }, new Object[] {
                        64,
                        2500,
                        0.0001,
                        181
                }, new Object[] {
                        64,
                        2500,
                        0.000035,
                        92
                }, new Object[] {
                        64,
                        3000,
                        0.0001,
                        222
                }, new Object[] {
                        64,
                        3000,
                        0.000035,
                        99
                }, new Object[] {
                        64,
                        3500,
                        0.0001,
                        274
                }, new Object[] {
                        64,
                        3500,
                        0.000035,
                        106
                }, new Object[] {
                        64,
                        4000,
                        0.0001,
                        337
                }, new Object[] {
                        64,
                        4000,
                        0.000035,
                        114
                }
        };
    }

    @Test(dataProvider = "testProfitMultiplyer")
    public void testProfitMultiplyer(long investment, double distance, double reward, int expectedReturn) {

        final FileConfiguration mockFileConfiguration = mock(FileConfiguration.class);

        mockStatic(Utils.class);
        when(Utils.getCaravansConfig()).thenReturn(mockFileConfiguration);

        when(mockFileConfiguration.getDouble("caravans.profitMultiplyer.PvEReward")).thenReturn(reward);
        when(mockFileConfiguration.getDouble("caravans.profitMultiplyer.PvPReward")).thenReturn(reward);

        final Region mockOrigin = mock(Region.class);
        final Location mockOriginCenter = mock(Location.class);
        when(mockOrigin.getCenter()).thenReturn(mockOriginCenter);

        final Region mockDestination = mock(Region.class);
        final Location mockDestinationCenter = mock(Location.class);
        when(mockDestination.getCenter()).thenReturn(mockDestinationCenter);

        when(mockDestinationCenter.distance(mockOriginCenter)).thenReturn(distance);

        final Caravan mockCaravan = mock(Caravan.class);
        when(mockCaravan.getInvestment()).thenReturn(investment);
        when(mockCaravan.getProfitStrategy()).thenReturn(ProfitMultiplyerStrategy.PvE);
        when(mockCaravan.getOrigin()).thenReturn(mockOrigin);
        when(mockCaravan.getReturn(mockDestination)).thenCallRealMethod();
        when(mockCaravan.getReturn(mockDestinationCenter)).thenCallRealMethod();

        final long calculatedReturn = mockCaravan.getReturn(mockDestination);
        assertEquals(expectedReturn, calculatedReturn);
    }

}
