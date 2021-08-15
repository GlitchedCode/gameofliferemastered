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
public interface Unit {

    // Utile durante i calcoli.
    public static Integer getOppositeDirection(Integer adjacencyPosition) {
        return (adjacencyPosition + 4) % 8;
    }
    // Restituisce l'ID del giocatore proprietario di questa unità.
    public int getPlayerID();
    // Restituisce vero se l'unità considera ostili unità di altri giocatori.
    public boolean isCompetitive();
    // Determina se l'unità deve considerare ostili unità di altri giocatori.
    public void setCompetitive(boolean val);
    // Imposta internamente lo stato dell'unità per il turno successivo.
    public void computeNextTurn(Unit[] adjacentUnits);
    // Transiziona l'unità allo stato del turno successivo.
    public void update();
    // Imposta l'unità come morta nel turno attuale e nel prossimo.
    public void kill();
    // Determina se l'unità può riprodursi verso una certa direzione.
    // Momentaneamente questo metodo dovrebbe restituire sempre true.
    public boolean reproduce(int adjacencyPosition);
    // Determina se l'unità può attaccarne un'altra in base alla specie, all'ID
    // del giocatore proprietario ed alla direzione relativa in cui si trova.
    // La direzione è momentaneamente ignorata in ogni implementazione.
    public boolean attack(int adjacencyPosition, Unit target);
    // Restituisce l'ID della specie a cui appartiene l'unità.
    public int getSpeciesID();
    // Restituisce l'ID della specie a cui l'unità può dare vita durante la fase
    // di riproduzione.
    public int getBornSpeciesID();
    // Restituisce il codice per la texture che rappresenta l'unità.
    public String getTextureCode();
    // Restituisce il filtro da applicare alla texture che rappresenta l'unità.
    public BufferedImageOp getFilter();
    // Restituisce un insieme di specie le cui unità sono considerate amichevoli.
    public Set<Integer> getFriendlySpecies();
    // Restituisce un insieme di specie le cui unità sono considerate ostili.
    public Set<Integer> getHostileSpecies();
    // Restituisce la regola da soddisfare durante la fase di sopravvivenza per
    // il numero di unità amichevoli
    public RuleInterface<Integer> getFriendlyCountSelector();
    // Restituisce la regola da soddisfare durante la fase di sopravvivenza per
    // il numero di unità ostili
    public RuleInterface<Integer> getHostileCountSelector();
    // Restituisce la regola da soddisfare durante la fase di riproduzione per
    // determinare se una nuova unità può nascere in una data cella.
    public RuleInterface<Integer> getReproductionSelector();
    // Restituisce true se l'unità è viva.
    public boolean isAlive();
    // Restituisce il numero di punti salute dell'unità.
    public Integer getHealth();
    // Somma l'intero passato come argomento al numero di punti salute dell'unità.
    public void incrementHealth(int increment);
    // Restituisce lo stato attuale dell'unità.
    public State getCurrentState();
    // Restituisce lo stato dell'unità per il turno successivo.
    public State getNextTurnState() throws GameLogicException;
}
