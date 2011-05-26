/**
 * Copyright 2010 ArcBees Inc.
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

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

/**
 * Tests that new Guice 3.0 assisted injection works in Jukito.
 *
 * @author Philippe Beaudoin
 */
@RunWith(JukitoRunner.class)
public class ReportWriterTest {

  /**
   * Guice test module.
   */
  static class Module extends JukitoModule {
    final Writer reportWriter = new StringWriter();

    // Overriding this method will cause a report to be generated.
    @Override public Writer getReportWriter() { return reportWriter; }

    @Override
    protected void configureTest() {
      bind(Writer.class).toInstance(reportWriter);
      bindNamedSpy(Resource4.class, "Spy").in(TestSingleton.class);
      forceMock(Resource6.class);
    }
  }

  @Singleton static class Resource1 { }
  interface Resource2 { }
  static class Resource3 {
    @Inject public Resource3(Resource4 resource4) { }
    @Inject @OneHundred Resource5 resource5;
    @Inject void setResource6(Resource6 resource6) { }
  }
  static class Resource4 { }
  interface Resource5 { }
  static class Resource6 { }

  @Inject Resource2 resource2;
  @Inject Resource3 resource3;

  @Test
  public void ensureReport(Writer reportWriter, Resource1 resource1) {
    Set<String> explicitBindings = findBlock("*** EXPLICIT BINDINGS ***", reportWriter.toString());
    Set<String> automaticBindings = findBlock("*** AUTOMATIC BINDINGS ***",
        reportWriter.toString());

    Set<String> e = new HashSet<String>();
    e.add("  Key[type=java.io.Writer, annotation=[none]] --> Instance of java.io.StringWriter ### In scope EagerSingleton");
    e.add("  Key[type=org.jukito.ReportWriterTest$Resource4, annotation=@org.jukito.JukitoInternal] --> Bound directly ### No scope");
    e.add("  Key[type=org.jukito.ReportWriterTest$Resource4, annotation=@com.google.inject.name.Named(value=Spy)] --> Instance of org.jukito.SpyProvider ### In scope org.jukito.TestSingleton");

    Set<String> a = new HashSet<String>();
    a.add("  Key[type=org.jukito.ReportWriterTest$Resource1, annotation=[none]] --> Bound directly ### In scope TestSingleton");
    a.add("  Key[type=org.jukito.ReportWriterTest$Resource2, annotation=[none]] --> Instance of org.jukito.MockProvider ### In scope TestSingleton");
    a.add("  Key[type=org.jukito.ReportWriterTest$Resource3, annotation=[none]] --> Bound directly ### In scope TestSingleton");
    a.add("  Key[type=org.jukito.ReportWriterTest$Resource4, annotation=[none]] --> Bound directly ### In scope TestSingleton");
    a.add("  Key[type=org.jukito.ReportWriterTest$Resource5, annotation=@org.jukito.OneHundred] --> Instance of org.jukito.MockProvider ### In scope TestSingleton");
    a.add("  Key[type=org.jukito.ReportWriterTest$Resource6, annotation=[none]] --> Instance of org.jukito.MockProvider ### In scope TestSingleton");

    assertEquals(e, explicitBindings);
    assertEquals(a, automaticBindings);
  }

  private Set<String> findBlock(String header, String text) {
    int start = text.indexOf(header) + header.length();
    int end = text.indexOf("\n\n", start);
    String block = text.substring(start + 1, end);
    String[] lines = block.split("\n");
    Set<String> result = new HashSet<String>(lines.length);
    result.addAll(Arrays.asList(lines));
    return result;
  }
}
