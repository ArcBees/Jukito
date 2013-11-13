package org.jukito.samples;

import javax.inject.Inject;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.name.Named;

import static org.junit.Assert.assertEquals;

@RunWith(JukitoRunner.class)
@UseModules({StringModule1.class, StringModule2.class})
public class UseModulesTest {
    @Inject
    @Named("1")
    String string1;
    @Inject
    @Named("2")
    String string2;

    @Test
    public void useModulesWorks() {
        assertEquals("abc", string1);
        assertEquals("def", string2);
    }
}
