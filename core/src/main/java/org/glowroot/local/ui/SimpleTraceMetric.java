/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.glowroot.local.ui;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.TreeTraverser;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.glowroot.markers.UsedByJsonBinding;

import static org.glowroot.common.ObjectMappers.checkNotNullItemsForProperty;
import static org.glowroot.common.ObjectMappers.checkRequiredProperty;
import static org.glowroot.common.ObjectMappers.nullToEmpty;

/**
 * @author Trask Stalnaker
 * @since 0.5
 */
@UsedByJsonBinding
public class SimpleTraceMetric {

    static final TreeTraverser<SimpleTraceMetric> TRAVERSER =
            new TreeTraverser<SimpleTraceMetric>() {
                @Override
                public Iterable<SimpleTraceMetric> children(SimpleTraceMetric root) {
                    return root.getNestedTraceMetrics();
                }
            };

    private final String name;
    // aggregation uses microseconds to avoid (unlikely) 292 year nanosecond rollover
    private long totalMicros;
    private long count;
    private final List<SimpleTraceMetric> nestedTraceMetrics;

    static SimpleTraceMetric createSyntheticRootNode() {
        return new SimpleTraceMetric("<multiple root nodes>", 0, 0,
                new ArrayList<SimpleTraceMetric>());
    }

    private SimpleTraceMetric(String name, long totalMicros, long count,
            List<SimpleTraceMetric> nestedTraceMetrics) {
        this.name = name;
        this.totalMicros = totalMicros;
        this.count = count;
        this.nestedTraceMetrics = nestedTraceMetrics;
    }

    void incrementCount(long num) {
        count += num;
    }

    void incrementTotalMicros(long num) {
        totalMicros += num;
    }

    public String getName() {
        return name;
    }

    public long getTotalMicros() {
        return totalMicros;
    }

    public long getCount() {
        return count;
    }

    public List<SimpleTraceMetric> getNestedTraceMetrics() {
        return nestedTraceMetrics;
    }

    @JsonCreator
    static SimpleTraceMetric readValue(
            @JsonProperty("name") @Nullable String name,
            @JsonProperty("totalMicros") @Nullable Long totalMicros,
            @JsonProperty("count") @Nullable Long count,
            @JsonProperty("nestedTraceMetrics") @Nullable List</*@Nullable*/SimpleTraceMetric> uncheckedNestedTraceMetrics)
            throws JsonMappingException {
        List<SimpleTraceMetric> nestedTraceMetrics =
                checkNotNullItemsForProperty(uncheckedNestedTraceMetrics, "nestedTraceMetrics");
        checkRequiredProperty(name, "name");
        checkRequiredProperty(totalMicros, "totalMicros");
        checkRequiredProperty(count, "count");
        return new SimpleTraceMetric(name, totalMicros, count, nullToEmpty(nestedTraceMetrics));
    }
}
