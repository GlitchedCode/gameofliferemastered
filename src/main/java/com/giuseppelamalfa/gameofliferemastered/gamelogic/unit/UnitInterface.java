/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.GameLogicException;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import java.util.Set;

/**
 *
 * @author glitchedcode
 */
public abstract interface UnitInterface {

    public static Integer getOppositeDirection(Integer adjacencyPosition) {
        return (adjacencyPosition + 4) % 8;
    }

    public int getPlayerID();

    public boolean isCompetitive();

    public void setCompetitive(boolean val);

    public void computeNextTurn(UnitInterface[] adjacentUnits);

    public void update();

    public void kill();

    public boolean reproduce(int adjacencyPosition);

    public boolean attack(int adjacencyPosition, UnitInterface target);

    public void independentAction();

    public int getSpeciesID();

    public Set<Integer> getFriendlySpecies();

    public Set<Integer> getHostileSpecies();

    public RuleInterface<Integer> getReproductionSelector();

    public boolean isAlive();

    public Integer getHealth();

    public void incrementHealth(int increment);

    public State getCurrentState();

    public State getNextTurnState() throws GameLogicException;
}
