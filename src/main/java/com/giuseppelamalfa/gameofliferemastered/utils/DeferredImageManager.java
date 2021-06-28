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
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public class DeferredImageManager extends ImageManager {

    public DeferredImageManager(String path) {
        super(path);
    }

    @Override
    protected boolean loadImages() {
        JSONArray imageDataArray = imageData.getJSONArray("imageData");

        // To avoid having to write a long path every time I insert a new
        // tile, i just substitute the # character in the imagePathTemplate
        // string to get the correct path
        for (int c = 0; c < imageDataArray.length(); c++) {
            JSONObject current = imageDataArray.getJSONObject(c);
            String codeString = current.getString("code");
            String nameString = current.getString("name");

            String path = imagePathTemplate;
            path = path.replaceAll("#", codeString);

            ImageData data = new ImageData();
            data.path = path;
            imageMap.put(nameString, data);
        }
        return true;
    }

    @Override
    public Image getImage(String name) {
        ImageData data = imageMap.get(name);
        if ( data.image == null ) {
            InputStream imageStream = this.getClass().getClassLoader().getResourceAsStream(data.path);
            try {
                BufferedImage img = ImageIO.read(imageStream);
                ImageFilter filter = new RGBImageFilter() {
                    @Override
                    public final int filterRGB(int x, int y, int rgb) {
                        if ( rgb == colorKey ) {
                            return rgb & 0x00FFFFFF;
                        }
                        return rgb;
                    }
                };
                ImageProducer ip = new FilteredImageSource(img.getSource(), filter);
                data.image = Toolkit.getDefaultToolkit().createImage(ip);
            }
            catch (IOException e) {
                System.err.println(e);
                return null;
            }
        }
        return data.image;
    }
}
