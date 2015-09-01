/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.revapi;

import org.junit.Test;
import org.revapi.simple.SimpleElement;
import org.revapi.simple.SimpleElementForest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * @author Lukas Krejci
 * @since 0.3.3
 */
public class AnalysisTest {

    @Test
    public void testTransformCycleDetection() throws Exception {
        BiFunction<Element, Element, Report> diffAnalyzer = (o, n) -> Report.builder().withNew(n).withOld(o)
                .addProblem().withCode("code").done().build();

        Revapi r = Revapi.builder().withAnalyzers(new DummyAnalyzer(diffAnalyzer))
                .withTransforms(new CloningDifferenceTransform()).build();

        AnalysisContext ctx = AnalysisContext.builder().withNewAPI(API.of().build()).withOldAPI(API.of().build())
                .build();

        //should not throw exception
        r.analyze(ctx);
    }

    private static final class CloningDifferenceTransform implements DifferenceTransform<Element> {

        @Override
        public @Nonnull Pattern[] getDifferenceCodePatterns() {
            return new Pattern[]{Pattern.compile("code")};
        }

        @Override
        public @Nullable Difference transform(@Nullable Element oldElement, @Nullable Element newElement, @Nonnull Difference d) {
            return Difference.builder().withCode(d.code).withName(d.name).withDescription(d.description)
                    .addClassifications(d.classification).build();
        }

        @Override
        public void close() throws Exception {
        }

        @Override
        public @Nullable String[] getConfigurationRootPaths() {
            return null;
        }

        @Override
        public @Nullable Reader getJSONSchema(@Nonnull String configurationRootPath) {
            return null;
        }

        @Override
        public void initialize(@Nonnull AnalysisContext analysisContext) {
        }
    }

    private static final class DummyElement extends SimpleElement {

        private final API api;
        private final Archive archive;

        private DummyElement(API api, Archive archive) {
            this.api = api;
            this.archive = archive;
        }

        @Override
        public @Nonnull API getApi() {
            return api;
        }

        @Override
        public @Nullable Archive getArchive() {
            return archive;
        }

        @Override
        public int compareTo(Element o) {
            return 0;
        }
    }

    private static final class DummyArchive implements Archive {

        @Override
        public @Nonnull String getName() {
            return "Dummy Archive";
        }

        @Override
        public @Nonnull InputStream openStream() throws IOException {
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    private static final class DummyAnalyzer implements ApiAnalyzer {

        private final BiFunction<Element, Element, Report> differenceAnalyzer;

        private DummyAnalyzer(BiFunction<Element, Element, Report> differenceAnalyzer) {
            this.differenceAnalyzer = differenceAnalyzer;
        }

        @Override
        public @Nonnull ArchiveAnalyzer getArchiveAnalyzer(@Nonnull API api) {
            return new DummyArchiveAnalyzer(api);
        }

        @Override
        public @Nonnull DifferenceAnalyzer getDifferenceAnalyzer(@Nonnull ArchiveAnalyzer oldArchive,
                                                                 @Nonnull ArchiveAnalyzer newArchive) {
            return new DummyDifferenceAnalyzer(differenceAnalyzer);
        }

        @Override
        public void close() throws Exception {
        }

        @Override
        public @Nullable String[] getConfigurationRootPaths() {
            return null;
        }

        @Override
        public @Nullable Reader getJSONSchema(@Nonnull String configurationRootPath) {
            return null;
        }

        @Override
        public void initialize(@Nonnull AnalysisContext analysisContext) {
        }
    }

    private static final class DummyElementForest extends SimpleElementForest {

        DummyElementForest(@Nonnull API api) {
            super(api);
        }

        @SuppressWarnings("unchecked")
        @Override
        public @Nonnull SortedSet<DummyElement> getRoots() {
            return (SortedSet<DummyElement>) super.getRoots();
        }
    }

    private static final class DummyArchiveAnalyzer implements ArchiveAnalyzer {

        private final API api;

        private DummyArchiveAnalyzer(API api) {
            this.api = api;
        }

        @Override
        public @Nonnull ElementForest analyze() {
            DummyElementForest ret = new DummyElementForest(api);
            ret.getRoots().add(new DummyElement(api, new DummyArchive()));
            return ret;
        }
    }

    private static final class DummyDifferenceAnalyzer implements DifferenceAnalyzer {

        private final BiFunction<Element, Element, Report> reportingFunction;

        private DummyDifferenceAnalyzer(BiFunction<Element, Element, Report> reportingFunction) {
            this.reportingFunction = reportingFunction;
        }

        @Override
        public @Nonnull Comparator<? super Element> getCorrespondenceComparator() {
            return (o1, o2) -> 0;
        }

        @Override
        public void open() {
        }

        @Override
        public void beginAnalysis(@Nullable Element oldElement, @Nullable Element newElement) {
        }

        @Override
        public Report endAnalysis(@Nullable Element oldElement, @Nullable Element newElement) {
            return reportingFunction.apply(oldElement, newElement);
        }

        @Override
        public void close() throws Exception {
        }
    }
}
