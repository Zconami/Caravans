package com.zconami.Caravans.mock;

import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class MockObjective implements Objective {

    private String displayName;

    @Override
    public String getCriteria() throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDisplayName() throws IllegalStateException {
        return displayName;
    }

    @Override
    public DisplaySlot getDisplaySlot() throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Score getScore(OfflinePlayer arg0) throws IllegalArgumentException, IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Score getScore(String arg0) throws IllegalArgumentException, IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Scoreboard getScoreboard() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isModifiable() throws IllegalStateException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setDisplayName(String arg0) throws IllegalStateException, IllegalArgumentException {
        this.displayName = arg0;
    }

    @Override
    public void setDisplaySlot(DisplaySlot arg0) throws IllegalStateException {
        // TODO Auto-generated method stub

    }

    @Override
    public void unregister() throws IllegalStateException {
        // TODO Auto-generated method stub

    }

}
