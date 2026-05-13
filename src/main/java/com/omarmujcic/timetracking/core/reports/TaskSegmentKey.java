package com.omarmujcic.timetracking.core.reports;

import java.util.UUID;

record TaskSegmentKey(UUID taskId, String taskName, String projectName, String label) {
}
