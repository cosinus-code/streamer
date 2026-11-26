package org.cosinus.streamer.ui.view.compare;

import org.cosinus.streamer.ui.view.compare.DiffRow.Range;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.cosinus.streamer.ui.view.compare.DiffRow.DiffType.CHANGED;
import static org.cosinus.streamer.ui.view.compare.DiffRow.DiffType.DELETED;
import static org.cosinus.streamer.ui.view.compare.DiffRow.DiffType.EQUAL;
import static org.cosinus.streamer.ui.view.compare.DiffRow.DiffType.INSERTED;
import static org.cosinus.streamer.ui.view.compare.DiffRow.NO_LINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompareContentHandlerTest {

    @Test
    public void emptyTextsHaveNoRows() {
        assertEquals(List.of(), CompareContentHandler.compare(List.of(), List.of()));
    }

    @Test
    public void equalTextsHaveOnlyEqualRows() {
        assertEquals(List.of(new DiffRow(EQUAL, 0, 0), new DiffRow(EQUAL, 1, 1)),
            CompareContentHandler.compare(List.of("a", "b"), List.of("a", "b")));
    }

    @Test
    public void insertedAndDeletedLinesAreAligned() {
        assertEquals(List.of(
                new DiffRow(EQUAL, 0, 0),
                new DiffRow(DELETED, 1, NO_LINE),
                new DiffRow(EQUAL, 2, 1),
                new DiffRow(INSERTED, NO_LINE, 2),
                new DiffRow(EQUAL, 3, 3)),
            CompareContentHandler.compare(List.of("a", "b", "c", "d"), List.of("a", "c", "x", "d")));
    }

    @Test
    public void replacedLinesArePairedAsChanged() {
        List<DiffRow> rows = CompareContentHandler.compare(List.of("a", "b", "c"), List.of("a", "x", "y", "c"));
        assertEquals(4, rows.size());
        assertEquals(new DiffRow(EQUAL, 0, 0), rows.get(0));
        assertEquals(CHANGED, rows.get(1).type());
        assertEquals(1, rows.get(1).leftLine());
        assertEquals(1, rows.get(1).rightLine());
        assertEquals(new DiffRow(INSERTED, NO_LINE, 2), rows.get(2));
        assertEquals(new DiffRow(EQUAL, 2, 3), rows.get(3));
    }

    @Test
    public void changedWordsAreHighlightedInChangedLines() {
        DiffRow row = CompareContentHandler.compare(List.of("int count = 10;"), List.of("long count = 20;")).get(0);
        assertEquals(CHANGED, row.type());
        assertEquals(List.of(new Range(0, 3), new Range(12, 14)), row.leftRanges());
        assertEquals(List.of(new Range(0, 4), new Range(13, 15)), row.rightRanges());
    }

    @Test
    public void insertedWordIsHighlightedOnlyInTheRightLine() {
        DiffRow row = CompareContentHandler.compare(List.of("a b"), List.of("a new b")).get(0);
        assertEquals(List.of(), row.leftRanges());
        assertEquals(1, row.rightRanges().size());
        Range range = row.rightRanges().get(0);
        String inserted = "a new b".substring(range.start(), range.end());
        assertTrue(inserted, List.of("new ", " new").contains(inserted));
    }

    @Test
    public void swappedRowsAreTheRowsOfTheSwappedTexts() {
        List<String> left = List.of("a", "b", "c x", "d");
        List<String> right = List.of("a", "c y", "d", "e");
        assertEquals(CompareContentHandler.compare(right, left),
            CompareContentHandler.compare(left, right).stream().map(DiffRow::swap).toList());
    }

    @Test
    public void oneSideEmpty() {
        assertEquals(List.of(new DiffRow(INSERTED, NO_LINE, 0), new DiffRow(INSERTED, NO_LINE, 1)),
            CompareContentHandler.compare(List.of(), List.of("a", "b")));
        assertEquals(List.of(new DiffRow(DELETED, 0, NO_LINE)),
            CompareContentHandler.compare(List.of("a"), List.of()));
    }

    @Test
    public void randomTextsAreAlignedWithAMinimalNumberOfDifferences() {
        Random random = new Random(42);
        for (int test = 0; test < 500; test++) {
            List<String> left = randomLines(random);
            List<String> right = randomLines(random);
            List<DiffRow> rows = CompareContentHandler.compare(left, right);

            List<String> leftFromRows = new ArrayList<>();
            List<String> rightFromRows = new ArrayList<>();
            int equal = 0;
            for (DiffRow row : rows) {
                if (row.hasLeftLine()) {
                    leftFromRows.add(left.get(row.leftLine()));
                }
                if (row.hasRightLine()) {
                    rightFromRows.add(right.get(row.rightLine()));
                }
                if (row.type() == EQUAL) {
                    equal++;
                    assertEquals(left.get(row.leftLine()), right.get(row.rightLine()));
                }
            }
            assertEquals(left, leftFromRows);
            assertEquals(right, rightFromRows);
            assertEquals(longestCommonSubsequence(left, right), equal);
            assertTrue(rows.size() <= left.size() + right.size());
        }
    }

    private List<String> randomLines(Random random) {
        int size = random.nextInt(12);
        List<String> lines = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            lines.add(String.valueOf((char) ('a' + random.nextInt(4))));
        }
        return lines;
    }

    private int longestCommonSubsequence(List<String> left, List<String> right) {
        int[][] lengths = new int[left.size() + 1][right.size() + 1];
        for (int i = 1; i <= left.size(); i++) {
            for (int j = 1; j <= right.size(); j++) {
                lengths[i][j] = left.get(i - 1).equals(right.get(j - 1)) ?
                    lengths[i - 1][j - 1] + 1 :
                    Math.max(lengths[i - 1][j], lengths[i][j - 1]);
            }
        }
        return lengths[left.size()][right.size()];
    }
}
