/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.utils;

/**
 * DEPRECATED
 * @author glitchedcode
 */
public class UtilityFunctions {
    public static boolean isInArray(Object val, Object[] array)
    {
        for (Object element : array)
            if (val == element)
                return true;
        
        return false;
    }
}
