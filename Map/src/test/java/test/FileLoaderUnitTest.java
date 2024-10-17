package test;

import model.FileLoader;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class FileLoaderUnitTest {
    @Test void fileDoesNotExist() {
        Exception exception = assertThrows(FileNotFoundException.class, () -> {
            FileLoader fl = new FileLoader("./this_file_does_not_exist.osm");
        });
    }
    @Test void invalidInputFile() {
        //A file that is not either .osm or .osm.zip
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            FileLoader fl = new FileLoader("./invalid_file_path");
        });
    }
    @Test void emptyOSMFile() {
        //A valid file that is empty
        Exception exception = assertThrows(XMLStreamException.class, () -> {
            FileLoader fl = new FileLoader("./src/test/test-data/empty.osm");
        });
    }
    @Test void validSmallOSMFile() {
        //A valid OSM file with known amount of MapObjects
        try {
            FileLoader fl = new FileLoader("./src/test/test-data/thuro.osm");
            assertEquals(fl.getObjects().size(), 5014);
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test void validOSMNoMapObjects() {
        //A valid OSM file with only nodes
        try {
            FileLoader fl = new FileLoader("./src/test/test-data/noMapObjects.osm");
            assertEquals(fl.getObjects().size(), 0);
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test void hasOSMBounds() throws XMLStreamException, IOException {
        FileInputStream input = new FileInputStream("./src/test/test-data/thuro.osm");
        boolean hasBounds = false;
        var stream = XMLInputFactory.newInstance().createXMLStreamReader(new InputStreamReader(input));
        while (stream.hasNext()) {
            var tagKind = stream.next();
            if (tagKind == XMLStreamConstants.START_ELEMENT) {
                String name = stream.getLocalName();
                if (name.equals("bounds")) {
                    hasBounds = true;
                    break;
                }
            }
        }
        assertEquals(hasBounds, true);
    }

    @Test void noOSMBounds() throws XMLStreamException, IOException {
        FileInputStream input = new FileInputStream("./src/test/test-data/thuro-nobounds.osm");
        boolean hasBounds = false;
        var stream = XMLInputFactory.newInstance().createXMLStreamReader(new InputStreamReader(input));
        while (stream.hasNext()) {
            var tagKind = stream.next();
            if (tagKind == XMLStreamConstants.START_ELEMENT) {
                String name = stream.getLocalName();
                if (name.equals("bounds")) {
                    hasBounds = true;
                    break;
                }
            }
        }
        assertEquals(hasBounds, false);
    }

    /* @Test void allAddressesStored() {
        // All known addresses are loaded and stored
        try {
            FileLoader fl = new FileLoader("./src/test/test-data/thuro.osm");
            assertEquals(fl.getAddresses().size(), 2136);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    } */
}
