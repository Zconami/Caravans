package com.zconami.Caravans.domain;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.testng.AssertJUnit.assertNotNull;

import org.bukkit.Location;
import org.testng.annotations.Test;

@Test
public class RegionTests extends BaseEntityTests {

    public void testRegionCreate() {

        final Location mockLocation = mock(Location.class);

        final RegionCreateParameters params = new RegionCreateParameters("key", "name", mockLocation, 5);
        final Region region = Region.create(params);
        assertNotNull(region);

    }

}
