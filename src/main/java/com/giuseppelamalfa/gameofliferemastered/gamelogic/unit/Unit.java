/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.GameLogicException;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import java.awt.image.BufferedImageOp;
import java.util.Set;

/**
 *
 * @author glitchedcode
 */
public abstract interface Unit {

    public static Integer getOppositeDirection(Integer adjacencyPosition) {
        return (adjacencyPosition + 4) % 8;
    }

    public int getPlayerID();

    public boolean isCompetitive();

    public void setCompetitive(boolean val);

    public void computeNextTurn(Unit[] adjacentUnits);

    public void update();

    public void kill();

    public boolean reproduce(int adjacencyPosition);

    public boolean attack(int adjacencyPosition, Unit target);

    public int getSpeciesID();
    
    public int getBornSpeciesID();
    
    public String getTextureCode();
    
    public BufferedImageOp getFilter();

    public Set<Integer> getFriendlySpecies();

    public Set<Integer> getHostileSpecies();

    public RuleInterface<Integer> getFriendlyCountSelector();

    public RuleInterface<Integer> getHostileCountSelector();

    public RuleInterface<Integer> getReproductionSelector();

    public boolean isAlive();

    public Integer getHealth();

    public void incrementHealth(int increment);

    public State getCurrentState();

    public State getNextTurnState() throws GameLogicException;
}
