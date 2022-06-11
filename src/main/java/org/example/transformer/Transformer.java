package org.example.transformer;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

public abstract class Transformer {
    public abstract HashMap<File, byte[]> transform(HashMap<File, byte[]> bytes);
}
