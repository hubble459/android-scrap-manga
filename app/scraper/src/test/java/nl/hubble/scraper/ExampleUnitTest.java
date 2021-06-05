package nl.hubble.scraper;

import org.junit.Test;

import nl.hubble.scraper.util.Utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void toTimeStringTest() {
        assertEquals("Just now", Utils.Parse.toTimeString(System.currentTimeMillis() - 900));
        assertEquals("2 minutes ago", Utils.Parse.toTimeString(System.currentTimeMillis() - 120000));
        assertEquals("40 minutes ago", Utils.Parse.toTimeString(System.currentTimeMillis() - 2400000));
        assertEquals("60 minutes ago", Utils.Parse.toTimeString(System.currentTimeMillis() - 3600000));
        assertEquals("2 hours ago", Utils.Parse.toTimeString(System.currentTimeMillis() - 7200000));
        assertEquals("24 hours ago", Utils.Parse.toTimeString(System.currentTimeMillis() - 86400000));
        assertEquals("2 days ago", Utils.Parse.toTimeString(System.currentTimeMillis() - 172800000));
        assertEquals("7 days ago", Utils.Parse.toTimeString(System.currentTimeMillis() - 604800000));
        assertNotEquals("8 days ago", Utils.Parse.toTimeString(System.currentTimeMillis() - 691200000));
    }

    @Test
    public void testConvertToNumber(){
        assertEquals(4.2, Utils.Parse.convertToNumber("A MIDDLE-AGED HERO, BETRAYED BY HIS FAVOURITE PUPIL, IS REVIVED AS HISTORY’S STRONGEST DEMON LORD Chapter 4.2"), 0.01);
        assertEquals(4.2, Utils.Parse.convertToNumber("Volume 2, Chapter 4.2"), 0.01);
        assertEquals(4.2, Utils.Parse.convertToNumber("Chapter 4.2 Volume 2"), 0.01);
        assertEquals(4.2, Utils.Parse.convertToNumber("Vol. 1.3 Chapter 4.2"), 0.01);
        assertEquals(4, Utils.Parse.convertToNumber("Chapter 4 the 2nd coming"), 0.01);
    }

    @Test
    public void testGetDate(){
        assertEquals(System.currentTimeMillis() - 3.6e6, Utils.Parse.toTime("1 hour ago"), 50);
        assertEquals(System.currentTimeMillis() - 7200000, Utils.Parse.toTime("2 hour ago"), 50);
        assertEquals(System.currentTimeMillis(), Utils.Parse.toTime("Just now"), 50);
    }
}