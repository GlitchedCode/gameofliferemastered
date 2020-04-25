/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.utils;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.imageio.ImageIO;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public class ImageManager{
    
    private final ClassLoader       loader = getClass().getClassLoader();
    private JSONObject              imageData;
    private boolean                 initialized = false;
    
    private String                                  imagePathTemplate;
    private final HashMap<String, Image>    imageMap = new HashMap<>();
    
    private int                     colorKey = 0xff222323;
    
    // Read the images.json file to get the data we need to store
    // and retrieve the images
    private boolean loadImageData(String path)
    {
        
        try
        {
            InputStream istream = loader.getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
            StringBuilder JSONStringBuilder = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
            {
                JSONStringBuilder.append(line);
            }
            imageData = new JSONObject(JSONStringBuilder.toString());
            imagePathTemplate = imageData.getString("pathTemplate");
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
            return false;
        }
        return true;
    }
    
    // Go through the JSONObject we read in loadImageData() and 
    // load all the images, then store them into imageDictionary
    private boolean loadImages()
    {
        JSONArray imageDataArray = imageData.getJSONArray("imageData");
        
        // To avoid having to write a long path every time I insert a new
        // tile, i just substitute the # character in the imagePathTemplate
        // string to get the correct path
        for (int c = 0; c < imageDataArray.length(); c++)
        {
            JSONObject current = imageDataArray.getJSONObject(c);
            String codeString = current.getString("code");
            String nameString = current.getString("name");
            
            String path = imagePathTemplate;
            path = path.replaceAll("#", codeString);
            InputStream imageStream = this.getClass().getClassLoader().getResourceAsStream(path);
            try
            {
                BufferedImage img = ImageIO.read(imageStream);
                ImageFilter filter = new RGBImageFilter()
                {
                    @Override
                    public final int filterRGB(int x, int y, int rgb)
                    {
                        if(rgb == colorKey)
                            return rgb & 0x00FFFFFF;
                        return rgb;
                    }
                };
                ImageProducer ip = new FilteredImageSource(img.getSource(), filter);
                imageMap.put(nameString, Toolkit.getDefaultToolkit().createImage(ip));
            }
            catch (IOException e)
            {
                System.err.println(e.toString());
                return false;
            }
        }
        return true;
    }
    
    public ImageManager(String path) 
    {

        initialized = loadImageData(path);
        if (initialized) 
        {
            initialized = initialized & loadImages();
        }
    }
    
    public boolean isInitialized() 
    {
        return initialized;
    }
    
    public Image getImage(String name)
    {
        return imageMap.get(name);
    }
}
