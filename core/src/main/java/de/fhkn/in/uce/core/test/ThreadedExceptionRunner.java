/*
    Copyright (c) 2012 Thomas Zink, 

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.fhkn.in.uce.core.test;

import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * @author thomas zink, daniel maier
 */
public class ThreadedExceptionRunner extends BlockJUnit4ClassRunner {
	
	public ThreadedExceptionRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }
	
    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
    }
     
    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        EachTestNotifier eachNotifier= makeNotifier(method, notifier);
        if (method.getAnnotation(Ignore.class) != null) {
            runIgnored(eachNotifier);
        } else {
            runNotIgnored(method, eachNotifier);
        }
    }

    private void runNotIgnored(final FrameworkMethod method,
            final EachTestNotifier eachNotifier) {
        eachNotifier.fireTestStarted();
        ThreadedTestGroup g = new ThreadedTestGroup(eachNotifier, method.getName());
        Thread t = new Thread(g, method.getName()) {
            public void run() {
                try {
                    methodBlock(method).evaluate();
                } catch (AssumptionViolatedException e) {
                    eachNotifier.addFailedAssumption(e);
                } catch (Throwable e) {
                    eachNotifier.addFailure(e);
                } finally {
                    eachNotifier.fireTestFinished();
                }
            };
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runIgnored(EachTestNotifier eachNotifier) {
        eachNotifier.fireTestIgnored();
    }
    
    private EachTestNotifier makeNotifier(FrameworkMethod method,
            RunNotifier notifier) {
        Description description = describeChild(method);
        return new EachTestNotifier(notifier, description);
    }
}
