package model;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;

public interface Loader {
    public List<MapObject> getObjects() throws IOException, XMLStreamException;
    public boolean isLoadable();
}
