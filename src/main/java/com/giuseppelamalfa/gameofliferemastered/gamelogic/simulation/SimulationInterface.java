/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation;
import com.giuseppelamalfa.gameofliferemastered.GridPanel;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.requests.InvalidRequestException;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;
import java.io.IOException;
/**
 *
 * @author glitchedcode
 */
public interface SimulationInterface {
    
    boolean         isStarted();
    boolean         isRunning();
    boolean         isLocallyControlled();
    int             getLocalPlayerID();
    
    int             getRowCount();
    int             getColumnCount();
    void            resize(int rows, int cols);
    int             getSectorSideLength();
    int             getCurrentTurn();
    
    UnitInterface   getUnit(int row, int col);
    void            setUnit(int row, int col, UnitInterface unit);
    
    void            computeNextTurn() throws Exception;
    
    void            setRunning(boolean val);
    void            handleRequest(Object requestObject, int ID) throws IOException, InvalidRequestException;

    void            synchronize();
    void            initializeGridPanel(GridPanel panel);
    void            close();
}
