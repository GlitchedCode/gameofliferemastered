/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;
import com.giuseppelamalfa.gameofliferemastered.GridPanel;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;
/**
 *
 * @author glitchedcode
 */
public interface GridPanelInterface {
    
    boolean         isSimulationStarted();
    boolean         isSimulationRunning();
    
    Integer         getRowCount();
    Integer         getColumnCount();
    Integer         getSectorSideLength();
    Integer         getCurrentTurn();
    
    UnitInterface   getUnit(int row, int col);
    void            setUnit(int row, int col, UnitInterface unit);
    
    void            computeNextTurn() throws Exception;
    
    void            synchronize();
    void            initializeGridPanel(GridPanel panel);
}
