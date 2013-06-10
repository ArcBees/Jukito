package org.jukito;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Przemysław Gałązka
 * @since 10-06-2013
 */
@RunWith(JukitoRunner.class)
public class TestTestDescriptions {

    @Test
    @Description("some nice test description")
    public void testA() throws Exception {
        //-------------------- GIVEN -------------------------------------------------------------------


        //-------------------- WHEN --------------------------------------------------------------------


        //-------------------- THEN --------------------------------------------------------------------

    }

    @Test
    @Description("some nice ultra long test description, some nice ultra long test description," +
            "some nice ultra long test description, some nice ultra long test description")
    public void testB() throws Exception {
        //-------------------- GIVEN -------------------------------------------------------------------


        //-------------------- WHEN --------------------------------------------------------------------


        //-------------------- THEN --------------------------------------------------------------------
    }

    @Test
    public void testWithoutDescription() throws Exception {
        //-------------------- GIVEN -------------------------------------------------------------------


        //-------------------- WHEN --------------------------------------------------------------------


        //-------------------- THEN --------------------------------------------------------------------
    }
}
