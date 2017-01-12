/*
 * Copyright 2017 ArcBees Inc.
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

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.mockito.Mockito;

public class MockitoUsageValidator extends RunListener {
    private final RunNotifier notifier;

    public MockitoUsageValidator(RunNotifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void testFinished(org.junit.runner.Description description) throws Exception {
        super.testFinished(description);

        try {
            Mockito.validateMockitoUsage();
        } catch (Throwable t) {
            notifier.fireTestFailure(new Failure(description, t));
        }
    }
}
