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
public abstract class Unit {

    // Utile durante i calcoli.
    public static Integer getOppositeDirection(Integer adjacencyPosition) {
        return (adjacencyPosition + 4) % 8;
    }
    // Restituisce l'ID del giocatore proprietario di questa unità.
    public abstract int getPlayerID();
    // Restituisce vero se l'unità considera ostili unità di altri giocatori.
    public abstract boolean isCompetitive();
    // Determina se l'unità deve considerare ostili unità di altri giocatori.
    public abstract void setCompetitive(boolean val);
    // Imposta internamente lo stato dell'unità per il turno successivo.
    public abstract void computeNextTurn(Unit[] adjacentUnits);
    // Restituisce true se lo stato dell'unità cambierà il prossimo turno.
    public abstract boolean isStateChanged();
    // Transiziona l'unità allo stato del turno successivo.
    public abstract void update();
    // Imposta l'unità come morta nel turno attuale e nel prossimo.
    public abstract void kill();
    // Determina se l'unità può riprodursi verso una certa direzione.
    // Momentaneamente questo metodo dovrebbe restituire sempre true.
    public abstract boolean reproduce(int adjacencyPosition);
    // Determina se l'unità può attaccarne un'altra in base alla specie, all'ID
    // del giocatore proprietario ed alla direzione relativa in cui si trova.
    // La direzione è momentaneamente ignorata in ogni implementazione.
    public abstract boolean attack(int adjacencyPosition, Unit target);
    // Restituisce i dati di specie utilizzati durante l'inizializzazione.
    public abstract SpeciesData getSpeciesData();
    // Restituisce l'ID della specie a cui appartiene originalmente l'unità.
    public abstract int getActualSpeciesID();
    // Restituisce l'ID della specie a cui appartiene l'unità.
    public abstract int getSpeciesID();
    // Restituisce l'ID della specie a cui l'unità può dare vita durante la fase
    // di riproduzione.
    public abstract int getBornSpeciesID();
    // Restituisce un insieme di specie le cui unità sono considerate amichevoli.
    public abstract Set<Integer> getFriendlySpecies();
    // Restituisce un insieme di specie le cui unità sono considerate ostili.
    public abstract Set<Integer> getHostileSpecies();
    // Restituisce la regola da soddisfare durante la fase di sopravvivenza per
    // il numero di unità amichevoli
    public abstract RuleInterface<Integer> getFriendlyCountSelector();
    // Restituisce la regola da soddisfare durante la fase di sopravvivenza per
    // il numero di unità ostili
    public abstract RuleInterface<Integer> getHostileCountSelector();
    // Restituisce la regola da soddisfare durante la fase di riproduzione per
    // determinare se una nuova unità può nascere in una data cella.
    public abstract RuleInterface<Integer> getReproductionSelector();
    // Restituisce true se l'unità è viva.
    public abstract boolean isAlive();
    // Restituisce il numero di punti salute dell'unità.
    public abstract Integer getHealth();
    // Somma l'intero passato come argomento al numero di punti salute dell'unità.
    public abstract void incrementHealth(int increment);
    // Restituisce lo stato attuale dell'unità.
    public abstract State getCurrentState();
    // Restituisce lo stato dell'unità per il turno successivo.
    public abstract State getNextTurnState() throws GameLogicException;
    
    // Fa in modo che l'unità risulti viva dal prossimo turno in poi
    protected abstract void markAsNewborn();

}
