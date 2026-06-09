package edu.group10.monopolydeal;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.*;
import org.junit.platform.launcher.listeners.*;

import java.io.PrintWriter;

/**
 * Standalone test runner — workaround for Gradle 9.x + Java 25 incompatibility.
 * Run via: {@code ./gradlew runTests}
 */
public final class TestSuiteRunner {

    public static void main(String[] args) {
        var summaryListener = new SummaryGeneratingListener();

        var request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        DiscoverySelectors.selectPackage("edu.group10.monopolydeal.backend.game"),
                        DiscoverySelectors.selectPackage("edu.group10.monopolydeal.backend.model.player"),
                        DiscoverySelectors.selectPackage("edu.group10.monopolydeal.backend.service"),
                        DiscoverySelectors.selectPackage("edu.group10.monopolydeal.backend.network.protocol"),
                        DiscoverySelectors.selectPackage("edu.group10.monopolydeal.common"))
                .filters(org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns(".*Test"))
                .build();

        var launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(summaryListener);

        launcher.execute(request);

        var summary = summaryListener.getSummary();

        System.out.println();
        System.out.println("================================================");
        System.out.println("           TEST EXECUTION SUMMARY");
        System.out.println("================================================");
        System.out.println("Tests found:     " + summary.getTestsFoundCount());
        System.out.println("Tests started:   " + summary.getTestsStartedCount());
        System.out.println("Tests succeeded: " + summary.getTestsSucceededCount());
        System.out.println("Tests failed:    " + summary.getTestsFailedCount());
        System.out.println("Tests skipped:   " + summary.getTestsSkippedCount());
        System.out.println("Time finished:   " + summary.getTimeFinished());
        System.out.println("================================================");

        summary.printTo(new PrintWriter(System.out, true));
        summary.printFailuresTo(new PrintWriter(System.out, true));

        // Exit with error code if any failures
        if (summary.getTestsFailedCount() > 0) {
            System.exit(1);
        }
    }
}
