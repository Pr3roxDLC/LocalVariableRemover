package org.example;

import org.example.transformer.Transformer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Obfuscator {
    private static ArrayList<Transformer> transformers = new ArrayList<>();

    public static void addTransformer(Transformer transformer){
        transformers.add(transformer);
    }

    public static HashMap<File, byte[]> transform(HashMap<File, byte[]> bytes){
        HashMap<File, byte[]> tempBytes = bytes;
        for (Transformer transformer : transformers) {
            tempBytes = transformer.transform(tempBytes);
        }
        return tempBytes;
    }

}
