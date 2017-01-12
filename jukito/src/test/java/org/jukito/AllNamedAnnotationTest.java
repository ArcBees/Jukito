/**
 * Copyright 2013 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.jukito;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test that methods with some parameters annotated with {@literal @}{@link All} behave correctly.
 */
@RunWith(JukitoRunner.class)
public class AllNamedAnnotationTest {

    public static final String FIRST = "first";
    public static final String SECOND = "second";

    static class Module extends JukitoModule {
        @SuppressWarnings("unchecked")
        @Override
        protected void configureTest() {
            bindManyNamedInstances(String.class, FIRST, "A", "B");
            bindManyNamedInstances(String.class, SECOND, "C", "D");
            bindManyNamedInstances(TestDataInstance.class, FIRST, new TestDataInstance("A"),
                    new TestDataInstance("B"));
            bindManyNamedInstances(TestDataInstance.class, SECOND, new TestDataInstance("C"),
                    new TestDataInstance("D"));
            bindManyNamed(TestData.class, FIRST, TestDataA.class, TestDataB.class);
            bindManyNamed(TestData.class, SECOND, TestDataC.class, TestDataD.class);

            bindManyNamedInstances(Integer.class, null, 1, 2, 3, 5);
        }
    }

    interface TestData {
        String getData();
    }

    static class TestDataA implements TestData {
        public String getData() {
            return "A";
        }
    }

    static class TestDataB implements TestData {
        public String getData() {
            return "B";
        }
    }

    static class TestDataC implements TestData {
        public String getData() {
            return "C";
        }
    }

    static class TestDataD implements TestData {
        public String getData() {
            return "D";
        }
    }

    static class TestDataInstance {
        private final String data;

        TestDataInstance(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

    /**
     * This class keeps track of what happens in all the tests run in this
     * class. It's used to make sure all expected tests are called.
     */
    private static class Bookkeeper {
        static List<String> namedStringsProcessed = new ArrayList<String>();
        static List<String> namedDataProcessed = new ArrayList<String>();
        static List<String> namedDataInstanceProcessed = new ArrayList<String>();

        static List<String> stringsProcessed = new ArrayList<String>();
        static List<String> dataProcessed = new ArrayList<String>();
        static List<String> dataInstanceProcessed = new ArrayList<String>();

        static List<Integer> integerProcessed = new ArrayList<Integer>();
    }

    @Test
    public void testAllWithNamedInstance(@All(FIRST) String string1, @All(SECOND) String string2) {
        Bookkeeper.namedStringsProcessed.add(string1 + string2);
    }

    @Test
    public void testAllWithNamedClass(@All(FIRST) TestData data1, @All(SECOND) TestData data2) {
        Bookkeeper.namedDataProcessed.add(data1.getData() + data2.getData());
    }

    @Test
    public void testAllWithNamedClassInstance(
            @All(FIRST) TestDataInstance data1, @All(SECOND) TestDataInstance data2) {
        Bookkeeper.namedDataInstanceProcessed.add(data1.getData() + data2.getData());
    }

    @Test
    public void testAllWithInstance(@All String string1, @All String string2) {
        Bookkeeper.stringsProcessed.add(string1 + string2);
    }

    @Test
    public void testAllWithClass(@All TestData data1, @All TestData data2) {
        Bookkeeper.dataProcessed.add(data1.getData() + data2.getData());
    }

    @Test
    public void testAllWithClassInstance(@All TestDataInstance data1, @All TestDataInstance data2) {
        Bookkeeper.dataInstanceProcessed.add(data1.getData() + data2.getData());
    }

    @Test
    public void testAllWithNullAsName(@All Integer i) {
        Bookkeeper.integerProcessed.add(i);
    }

    @AfterClass
    public static void checkBookkeeper() {
        assertTrue(Bookkeeper.namedStringsProcessed.contains("AC"));
        assertTrue(Bookkeeper.namedStringsProcessed.contains("AD"));
        assertTrue(Bookkeeper.namedStringsProcessed.contains("BC"));
        assertTrue(Bookkeeper.namedStringsProcessed.contains("BD"));
        assertEquals(4, Bookkeeper.namedStringsProcessed.size());

        assertTrue(Bookkeeper.namedDataProcessed.contains("AC"));
        assertTrue(Bookkeeper.namedDataProcessed.contains("AD"));
        assertTrue(Bookkeeper.namedDataProcessed.contains("BC"));
        assertTrue(Bookkeeper.namedDataProcessed.contains("BD"));
        assertEquals(4, Bookkeeper.namedDataProcessed.size());

        assertTrue(Bookkeeper.namedDataInstanceProcessed.contains("AC"));
        assertTrue(Bookkeeper.namedDataInstanceProcessed.contains("AD"));
        assertTrue(Bookkeeper.namedDataInstanceProcessed.contains("BC"));
        assertTrue(Bookkeeper.namedDataInstanceProcessed.contains("BD"));
        assertEquals(4, Bookkeeper.namedDataInstanceProcessed.size());

        assertTrue(Bookkeeper.stringsProcessed.contains("AA"));
        assertTrue(Bookkeeper.stringsProcessed.contains("AB"));
        assertTrue(Bookkeeper.stringsProcessed.contains("AC"));
        assertTrue(Bookkeeper.stringsProcessed.contains("AD"));
        assertTrue(Bookkeeper.stringsProcessed.contains("BA"));
        assertTrue(Bookkeeper.stringsProcessed.contains("BB"));
        assertTrue(Bookkeeper.stringsProcessed.contains("BC"));
        assertTrue(Bookkeeper.stringsProcessed.contains("BD"));
        assertTrue(Bookkeeper.stringsProcessed.contains("CA"));
        assertTrue(Bookkeeper.stringsProcessed.contains("CB"));
        assertTrue(Bookkeeper.stringsProcessed.contains("CC"));
        assertTrue(Bookkeeper.stringsProcessed.contains("CD"));
        assertTrue(Bookkeeper.stringsProcessed.contains("DA"));
        assertTrue(Bookkeeper.stringsProcessed.contains("DB"));
        assertTrue(Bookkeeper.stringsProcessed.contains("DC"));
        assertTrue(Bookkeeper.stringsProcessed.contains("DD"));
        assertEquals(16, Bookkeeper.stringsProcessed.size());

        assertTrue(Bookkeeper.dataProcessed.contains("AA"));
        assertTrue(Bookkeeper.dataProcessed.contains("AB"));
        assertTrue(Bookkeeper.dataProcessed.contains("AC"));
        assertTrue(Bookkeeper.dataProcessed.contains("AD"));
        assertTrue(Bookkeeper.dataProcessed.contains("BA"));
        assertTrue(Bookkeeper.dataProcessed.contains("BB"));
        assertTrue(Bookkeeper.dataProcessed.contains("BC"));
        assertTrue(Bookkeeper.dataProcessed.contains("BD"));
        assertTrue(Bookkeeper.dataProcessed.contains("CA"));
        assertTrue(Bookkeeper.dataProcessed.contains("CB"));
        assertTrue(Bookkeeper.dataProcessed.contains("CC"));
        assertTrue(Bookkeeper.dataProcessed.contains("CD"));
        assertTrue(Bookkeeper.dataProcessed.contains("DA"));
        assertTrue(Bookkeeper.dataProcessed.contains("DB"));
        assertTrue(Bookkeeper.dataProcessed.contains("DC"));
        assertTrue(Bookkeeper.dataProcessed.contains("DD"));
        assertEquals(16, Bookkeeper.dataProcessed.size());

        assertTrue(Bookkeeper.dataInstanceProcessed.contains("AA"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("AB"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("AC"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("AD"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("BA"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("BB"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("BC"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("BD"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("CA"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("CB"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("CC"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("CD"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("DA"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("DB"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("DC"));
        assertTrue(Bookkeeper.dataInstanceProcessed.contains("DD"));
        assertEquals(16, Bookkeeper.dataInstanceProcessed.size());

        assertTrue(Bookkeeper.integerProcessed.contains(1));
        assertTrue(Bookkeeper.integerProcessed.contains(2));
        assertTrue(Bookkeeper.integerProcessed.contains(3));
        assertTrue(Bookkeeper.integerProcessed.contains(5));
        assertEquals(4, Bookkeeper.integerProcessed.size());
    }
}
