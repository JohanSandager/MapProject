package model;

import java.io.InputStream;

public abstract class ResourceLoader {
    public InputStream getResourceStream(String resName) {
        ClassLoader classLoader = getClass().getClassLoader(); 
        InputStream inputStream = classLoader.getResourceAsStream(resName); 

        if(inputStream == null) {
            throw new IllegalArgumentException("Invalid resource \"" + resName + "\"");
        }
        return inputStream; 
    }
}
