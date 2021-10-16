/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;
import com.giuseppelamalfa.gameofliferemastered.simulation.SimulationCLIServer;
import com.giuseppelamalfa.gameofliferemastered.simulation.SimulationGUIServer;
import com.giuseppelamalfa.gameofliferemastered.simulation.SimulationInterface;
import com.giuseppelamalfa.gameofliferemastered.utils.ImageManager;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class GridPanelTest {
    
    ImageManager tileManager = new ImageManager("tiles.json");
    SimulationGUIServer simulation;

    public GridPanelTest() throws Exception{
        simulation = new SimulationGUIServer(50,70);
        simulation.reloadSpeciesConf("species/testSpecies.json");
    }
    
    GridPanel panel;
    
    @Before
    public void setUp() {
        panel = new GridPanel(tileManager);
        
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetUnitFilter(){
        
    }
    
}
