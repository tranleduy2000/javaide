/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.lint;

import com.android.annotations.NonNull;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link Warning} represents a specific warning that a {@link LintClient}
 * has been told about. The context stores these as they are reported into a
 * list of warnings such that it can sort them all before presenting them all at
 * the end.
 */
public class Warning implements Comparable<Warning> {
    public final Issue issue;
    public final String message;
    public final Severity severity;
    public final Project project;
    public AndroidProject gradleProject;
    public Location location;
    public File file;
    public String path;
    public int line = -1;
    public int offset = -1;
    public String errorLine;
    public String fileContents;
    public Set<Variant> variants;

    public Warning(Issue issue, String message, Severity severity, Project project) {
        this.issue = issue;
        this.message = message;
        this.severity = severity;
        this.project = project;
    }

    // ---- Implements Comparable<Warning> ----
    @SuppressWarnings({"VariableNotUsedInsideIf", "ConstantConditions"})
    @Override
    public int compareTo(@NonNull Warning other) {
        // Sort by category, then by priority, then by id,
        // then by file, then by line
        int categoryDelta = issue.getCategory().compareTo(other.issue.getCategory());
        if (categoryDelta != 0) {
            return categoryDelta;
        }
        // DECREASING priority order
        int priorityDelta = other.issue.getPriority() - issue.getPriority();
        if (priorityDelta != 0) {
            return priorityDelta;
        }
        String id1 = issue.getId();
        String id2 = other.issue.getId();
        assert id1 != null;
        assert id2 != null;
        int idDelta = id1.compareTo(id2);
        if (idDelta != 0) {
            return idDelta;
        }
        if (file != null) {
            if (other.file != null) {
                int fileDelta = file.getName().compareTo(
                        other.file.getName());
                if (fileDelta != 0) {
                    return fileDelta;
                }
            } else {
                return -1;
            }
        } else if (other.file != null) {
            return 1;
        }
        if (line != other.line) {
            return line - other.line;
        }

        int delta = message.compareTo(other.message);
        if (delta != 0) {
            return delta;
        }

        if (file != null) {
            if (other.file != null) {
                delta = file.compareTo(other.file);
                if (delta != 0) {
                    return delta;
                }
            } else {
                return -1;
            }
        } else if (other.file != null) {
            return 1;
        }

        Location secondary1 = location != null ? location.getSecondary() : null;
        File secondaryFile1 = secondary1 != null ? secondary1.getFile() : null;
        Location secondary2 = other.location != null ? other.location.getSecondary() : null;
        File secondaryFile2 = secondary2 != null ? secondary2.getFile() : null;
        if (secondaryFile1 != null) {
            if (secondaryFile2 != null) {
                return secondaryFile1.compareTo(secondaryFile2);
            } else {
                return -1;
            }
        } else if (secondaryFile2 != null) {
            return 1;
        }

        // This handles the case where you have a huge XML document without hewlines,
        // such that all the errors end up on the same line.
        if (location != null && other.location != null &&
                location.getStart() != null && other.location.getStart() != null) {
                delta = location.getStart().getColumn() - other.location.getStart().getColumn();
            if (delta != 0) {
                return delta;
            }
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Warning warning = (Warning) o;

        if (line != warning.line) {
            return false;
        }
        if (file != null ? !file.equals(warning.file) : warning.file != null) {
            return false;
        }
        if (!issue.getCategory().equals(warning.issue.getCategory())) {
            return false;
        }
        if (issue.getPriority() != warning.issue.getPriority()) {
            return false;
        }
        if (!issue.getId().equals(warning.issue.getId())) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (!message.equals(warning.message)) {
            return false;
        }

        Location secondary1 = location != null ? location.getSecondary() : null;
        Location secondary2 = warning.location != null ? warning.location.getSecondary() : null;
        if (secondary1 != null) {
            if (secondary2 != null) {
                if (!Objects.equal(secondary1.getFile(), secondary2.getFile())) {
                    return false;
                }
            } else {
                return false;
            }
        } else //noinspection VariableNotUsedInsideIf
            if (secondary2 != null) {
            return false;
        }

        // This handles the case where you have a huge XML document without hewlines,
        // such that all the errors end up on the same line.
        //noinspection RedundantIfStatement
        if (location != null && warning.location != null &&
                location.getStart() != null && warning.location.getStart() != null &&
                location.getStart().getColumn() != warning.location.getStart().getColumn()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = message.hashCode();
        result = 31 * result + (file != null ? file.hashCode() : 0);
        return result;
    }

    public boolean isVariantSpecific() {
        return variants != null && variants.size() < gradleProject.getVariants().size();
    }

    public boolean includesMoreThanExcludes() {
        assert isVariantSpecific();
        int variantCount = variants.size();
        int allVariantCount = gradleProject.getVariants().size();
        return variantCount <= allVariantCount - variantCount;
    }

    public List<String> getIncludedVariantNames() {
        assert isVariantSpecific();
        List<String> names = new ArrayList<String>();
        if (variants != null) {
            for (Variant variant : variants) {
                names.add(variant.getName());
            }
        }
        Collections.sort(names);
        return names;
    }

    public List<String> getExcludedVariantNames() {
        assert isVariantSpecific();
        Collection<Variant> variants = gradleProject.getVariants();
        Set<String> allVariants = new HashSet<String>(variants.size());
        for (Variant variant : variants) {
            allVariants.add(variant.getName());
        }
        Set<String> included = new HashSet<String>(getIncludedVariantNames());
        Set<String> excluded = Sets.difference(allVariants, included);
        List<String> sorted = Lists.newArrayList(excluded);
        Collections.sort(sorted);
        return sorted;
    }

    @Override
    public String toString() {
        return "Warning{" +
                "issue=" + issue +
                ", message='" + message + '\'' +
                ", file=" + file +
                ", line=" + line +
                '}';
    }
}
