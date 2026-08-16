package com.studentmanagement.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GradeCalculatorTest {

    @Test
    void percentage_computesCorrectly() {
        assertEquals(78.0, GradeCalculator.percentage(78, 100));
        assertEquals(85.5, GradeCalculator.percentage(85.5, 100));
        assertEquals(50.0, GradeCalculator.percentage(25, 50));
    }

    @Test
    void percentage_rejectsZeroMaximum() {
        assertThrows(IllegalArgumentException.class, () -> GradeCalculator.percentage(10, 0));
    }

    @Test
    void grade_matchesSpecBoundaries() {
        assertEquals("A+", GradeCalculator.grade(95));
        assertEquals("A+", GradeCalculator.grade(90));
        assertEquals("A", GradeCalculator.grade(89.99));
        assertEquals("A", GradeCalculator.grade(80));
        assertEquals("B", GradeCalculator.grade(75));
        assertEquals("C", GradeCalculator.grade(65));
        assertEquals("D", GradeCalculator.grade(55));
        assertEquals("F", GradeCalculator.grade(49.99));
        assertEquals("F", GradeCalculator.grade(0));
    }

    @Test
    void isPass_boundaryIsFifty() {
        assertTrue(GradeCalculator.isPass(50.0));
        assertTrue(GradeCalculator.isPass(50.01));
        assertFalse(GradeCalculator.isPass(49.99));
    }

    @Test
    void gpaEquivalent_isCappedAtTen() {
        assertEquals(10.0, GradeCalculator.gpaEquivalent(100));
        assertEquals(8.5, GradeCalculator.gpaEquivalent(85));
    }
}
