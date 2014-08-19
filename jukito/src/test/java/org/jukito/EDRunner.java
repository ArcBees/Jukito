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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Runner allows to run the all test methods in environment build upon different implementations
 * via dedicated Injectors per test run.
 */
public class EDRunner extends ParentRunner {
    private List<Runner> runnersList;

    public EDRunner(Class<?> testClass) throws Exception {
        super(testClass);
        runnersList = createJukitoRunners(testClass);
    }

    public List<Runner> createJukitoRunners(Class<?> testClass) throws Exception {
        List<Runner> result = new ArrayList<Runner>();

        for (Injector injector : calculateInjectors(testClass)) {
            JukitoRunner jukitoRunner = new JukitoRunner(testClass, injector);
            result.add(jukitoRunner);
        }

        return result;
    }

    protected List<Injector> calculateInjectors(Class<?> testClass)
            throws IllegalAccessException, InstantiationException {
        List<Injector> result = new ArrayList<Injector>();

        EnvironmentDependentModules environmentDependentModules
                = testClass.getAnnotation(EnvironmentDependentModules.class);

        if (environmentDependentModules == null) {
            throw new RuntimeException(EnvironmentDependentModules.class + " not found on test class");
        }

        for (Class edModuleClass : environmentDependentModules.value()) {
            Injector i = buildInjector(edModuleClass, testClass);
            result.add(i);
        }

        return result;
    }

    @Override
    protected List getChildren() {
        return runnersList;
    }

    @Override
    protected Description describeChild(Object child) {
        return ((JukitoRunner) child).getDescription();
    }

    @Override
    protected void runChild(Object child, RunNotifier notifier) {
        ((JukitoRunner) child).run(notifier);
    }

    private Injector buildInjector(Class<? extends Module> edModuleClazz, final Class<?> testClass)
            throws InstantiationException, IllegalAccessException {

        final Module environmentDependentModule = edModuleClazz.newInstance();

        final AbstractModule testModule = new AbstractModule() {
            @Override
            protected void configure() {
                for (Module declaredModule : getDeclaredModulesForTest(testClass)) {
                    install(declaredModule);
                }
                install(environmentDependentModule);
            }
        };

        JukitoModule finalModule = new JukitoModule() {
            @Override
            protected void configureTest() {
                install(testModule);
            }
        };

        BindingsCollector collector = new BindingsCollector(finalModule);
        collector.collectBindings();
        finalModule.setBindingsObserved(collector.getBindingsObserved());
        finalModule.setStaticInjectionPointsObserved(collector.getStaticInjectionPointsObserved());

        return Guice.createInjector(finalModule);
    }

    // TODO refactor to common user module discovery method (JukitoRunner uses similar code now)
    private Module[] getDeclaredModulesForTest(Class<?> testClass) {
        UseModules useModules = testClass.getAnnotation(UseModules.class);
        Class<? extends Module>[] moduleClasses = useModules.value();
        final Module[] modules = new Module[moduleClasses.length];
        for (int i = 0; i < modules.length; i++) {
            try {
                modules[i] = moduleClasses[i].newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return modules;
    }
}
