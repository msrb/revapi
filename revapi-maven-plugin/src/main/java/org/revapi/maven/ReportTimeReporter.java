package org.revapi.maven;

import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.revapi.AnalysisContext;
import org.revapi.CompatibilityType;
import org.revapi.Difference;
import org.revapi.DifferenceSeverity;
import org.revapi.Element;
import org.revapi.Report;
import org.revapi.Reporter;

/**
 * @author Lukas Krejci
 * @since 0.1
 */
final class ReportTimeReporter implements Reporter {

    private final DifferenceSeverity minSeverity;

    EnumMap<DifferenceSeverity, EnumMap<CompatibilityType, List<DifferenceReport>>> reportsBySeverity =
        new EnumMap<>(DifferenceSeverity.class);

    ReportTimeReporter(DifferenceSeverity minSeverity) {
        this.minSeverity = minSeverity;
    }

    @Override
    public void report(@Nonnull Report report) {
        for (Difference d : report.getDifferences()) {
            addDifference(report.getOldElement(), report.getNewElement(), d);
        }
    }

    @Override
    public void close() throws Exception {
    }

    @Nullable
    @Override
    public String[] getConfigurationRootPaths() {
        return null;
    }

    @Nullable
    @Override
    public Reader getJSONSchema(@Nonnull String configurationRootPath) {
        return null;
    }

    @Override
    public void initialize(@Nonnull AnalysisContext analysisContext) {
    }

    private void addDifference(Element oldElement, Element newElement, Difference difference) {
        for (Map.Entry<CompatibilityType, DifferenceSeverity> cls : difference.classification.entrySet()) {
            if (cls.getValue().compareTo(minSeverity) < 0) {
                continue;
            }

            EnumMap<CompatibilityType, List<DifferenceReport>> sevReports = reportsBySeverity.get(cls.getValue());
            if (sevReports == null) {
                sevReports = new EnumMap<>(CompatibilityType.class);
                reportsBySeverity.put(cls.getValue(), sevReports);
            }

            List<DifferenceReport> reps = sevReports.get(cls.getKey());
            if (reps == null) {
                reps = new ArrayList<>();
                sevReports.put(cls.getKey(), reps);
            }

            reps.add(new DifferenceReport(oldElement, newElement, difference));
        }
    }

    public static class DifferenceReport {
        final Element oldElement;
        final Element newElement;
        final Difference difference;

        private DifferenceReport(Element oldElement, Element newElement, Difference difference) {
            this.oldElement = oldElement;
            this.newElement = newElement;
            this.difference = difference;
        }
    }
}
