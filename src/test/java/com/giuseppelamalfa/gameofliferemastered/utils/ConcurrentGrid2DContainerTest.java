/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.utils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class ConcurrentGrid2DContainerTest {

    public ConcurrentGrid2DContainerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of hasDefaultValue method, of class ConcurrentGrid2DContainer.
     */
    @Test
    public void testDefaultValue() {
        System.out.println("hasDefaultValue");
        ConcurrentGrid2DContainer<Integer> withDefault = new ConcurrentGrid2DContainer<Integer>(10, 10, 1);
        ConcurrentGrid2DContainer<Integer> withoutDefault = new ConcurrentGrid2DContainer<Integer>(10, 10);

        assertEquals(true, withDefault.get(0, 0) == 1);
        assertEquals(true, withoutDefault.get(0, 0) == null);
    }

    /**
     * Test of resize method, of class ConcurrentGrid2DContainer.
     */
    @Test
    public void testResize() {
        System.out.println("resize");
        ConcurrentGrid2DContainer<Integer> instance = new ConcurrentGrid2DContainer<>(5, 5);
        assertEquals(true, instance.getRowCount() == 5 & instance.getColumnCount() == 5);
        instance.resize(10, 10);
        assertEquals(true, instance.getRowCount() == 10 & instance.getColumnCount() == 10);
    }

    /**
     * Test of get method, of class ConcurrentGrid2DContainer.
     */
    @Test
    public void testPutGet() {
        System.out.println("putGet");
        Object expResult = new Object();
        Object defaultValue = new Object();
        ConcurrentGrid2DContainer<Object> instance = new ConcurrentGrid2DContainer<>(5, 5, defaultValue);

        for (int i = 0; i < 5; i++) {
            instance.put(i, i, expResult);
            assertEquals(expResult, instance.get(i, i));
        }
        assertEquals(defaultValue, instance.get(3, 4));
    }

    /**
     * Test of remove method, of class ConcurrentGrid2DContainer.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        Object obj = new Object();
        Object defaultValue = new Object();
        ConcurrentGrid2DContainer<Object> instance = new ConcurrentGrid2DContainer<>(5, 5, defaultValue);

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                instance.put(r, c, obj);
            }
            instance.remove(r, r);
            assertEquals(defaultValue, instance.get(r, r));
        }

    }

    /**
     * Test of clear method, of class ConcurrentGrid2DContainer.
     */
    @Test
    public void testClear() {
        System.out.println("clear");
        Object obj = new Object();
        Object defaultValue = new Object();
        ConcurrentGrid2DContainer<Object> instance = new ConcurrentGrid2DContainer<>(5, 5, defaultValue);

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                instance.put(r, c, obj);
            }
        }

        instance.clear();

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                assertEquals(defaultValue, instance.get(r, c));
            }
        }
    }

}
