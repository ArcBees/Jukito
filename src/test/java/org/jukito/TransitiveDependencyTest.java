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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

/**
 * Test that various form of automatic discovery of transitive dependencies work.
 * 
 * @author Philippe Beaudoin
 */
@RunWith(JukitoRunner.class)
public class TransitiveDependencyTest {

  interface SubCollaborator {
    void subCollaborate();
  }
  
  @TestEagerSingleton
  static class Collaborator {
    private final SubCollaborator subCollaborator;
    @Inject
    public Collaborator(SubCollaborator subCollaborator) {
      this.subCollaborator = subCollaborator;
    }
  }
    
  @TestEagerSingleton
  static class Leader {
    private final Collaborator collaborator;
    @Inject
    public Leader(Collaborator collaborator) {
      this.collaborator = collaborator;
    }
  }
  
  @Test
  public void test(Leader leader) {
    // THEN
    verify(leader.collaborator.subCollaborator, never()).subCollaborate();
  }

}
