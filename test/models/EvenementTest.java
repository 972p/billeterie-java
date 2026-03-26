package models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EvenementTest {

    @Test
    public void testEvenementConstructorAndGetters() {
        Evenement evt = new Evenement(1, "Concert", "Short desc", "Long desc", 120, "FR", 12);
        
        assertEquals(1, evt.getId());
        assertEquals("Concert", evt.getTitre());
        assertEquals("Short desc", evt.getDescriptionCourte());
        assertEquals("Long desc", evt.getDescriptionLongue());
        assertEquals(120, evt.getDuree());
        assertEquals("FR", evt.getLangue());
        assertEquals(12, evt.getAgeMin());
    }
}
