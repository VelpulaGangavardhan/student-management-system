package com.studentmanagement.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rule-based grading logic, kept in one place so grade boundaries can be
 * changed without touching any service. Not machine learning - a simple,
 * explainable lookup table, exactly as the project spec asks for.
 */
public final class GradeCalculator {

    // LinkedHashMap preserves insertion order, so we can walk it top-down
    // and stop at the first threshold the percentage satisfies.
    private static final Map<Double, String> GRADE_BOUNDARIES = new LinkedHashMap<>();
    private static final double PASS_PERCENTAGE = 50.0;

    static {
        GRADE_BOUNDARIES.put(90.0, "A+");
        GRADE_BOUNDARIES.put(80.0, "A");
        GRADE_BOUNDARIES.put(70.0, "B");
        GRADE_BOUNDARIES.put(60.0, "C");
        GRADE_BOUNDARIES.put(50.0, "D");
        GRADE_BOUNDARIES.put(0.0, "F");
    }

    private GradeCalculator() {
    }

    public static double percentage(double marksObtained, double maximumMarks) {
        if (maximumMarks <= 0) {
            throw new IllegalArgumentException("Maximum marks must be greater than zero");
        }
        return Math.round((marksObtained / maximumMarks) * 10000.0) / 100.0; // 2 decimal places
    }

    public static String grade(double percentage) {
        for (Map.Entry<Double, String> entry : GRADE_BOUNDARIES.entrySet()) {
            if (percentage >= entry.getKey()) {
                return entry.getValue();
            }
        }
        return "F";
    }

    public static boolean isPass(double percentage) {
        return percentage >= PASS_PERCENTAGE;
    }

    /**
     * Rough 10-point GPA equivalent of a percentage, used for the
     * student-facing performance dashboard. percentage / 10, capped at 10.0.
     */
    public static double gpaEquivalent(double percentage) {
        return Math.min(10.0, Math.round((percentage / 10.0) * 100.0) / 100.0);
    }
}
