package test;

import javafx.geometry.Point2D;
import model.Address;
import model.SerializablePoint2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AddressUnitTest {

    private SerializablePoint2D coordinates;
    private String municipality, city,  street, postCode, houseNumber;

    @BeforeEach void setup() {
        coordinates = new SerializablePoint2D(0.56 * 10.6644370,-55.0442640);
        municipality = "Svendborg";
        city = "Svendborg";
        street = "Søndervej";
        postCode = "5700";
        houseNumber = "10";
    }

    @Test void postcodeCanNotBeParsedToAnInteger() {
        assertThrows(IllegalArgumentException.class, () -> {
            Address address = new Address(municipality, "Odense", city, street, houseNumber);
        });
    }

    @Test void addressIsOutOfBounds() {

    }

}
