package org.cosinus.streamer.ui.view;

import org.cosinus.swing.store.InMemoryApplicationStorage;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.cosinus.streamer.ui.view.PanelLocation.LEFT;
import static org.cosinus.streamer.ui.view.PanelLocation.RIGHT;
import static org.junit.Assert.assertEquals;

public class StreamerHistoryTest {

    private InMemoryApplicationStorage storage;

    private StreamerHistory history;

    @Before
    public void setUp() {
        storage = new InMemoryApplicationStorage();
        history = new StreamerHistory(storage, () -> 3);
    }

    @Test
    public void backWithoutPreviousPathIsEmpty() {
        assertEquals(Optional.empty(), history.back(LEFT));
        history.add(LEFT, "/a");
        assertEquals(Optional.empty(), history.back(LEFT));
    }

    @Test
    public void backReturnsPreviousPaths() {
        history.add(LEFT, "/a");
        history.add(LEFT, "/b");
        history.add(LEFT, "/c");

        assertEquals(Optional.of("/b"), history.back(LEFT));
        assertEquals(Optional.of("/a"), history.back(LEFT));
        assertEquals(Optional.empty(), history.back(LEFT));
        assertEquals(0, history.getPosition(LEFT));
    }

    @Test
    public void showingThePathReachedByGoingBackKeepsTheHistory() {
        history.add(LEFT, "/a");
        history.add(LEFT, "/b");
        history.back(LEFT);
        history.add(LEFT, "/a");

        assertEquals(List.of("/a", "/b"), history.getPaths(LEFT));
        assertEquals(0, history.getPosition(LEFT));
    }

    @Test
    public void addingAfterGoingBackDropsTheForwardPaths() {
        history.add(LEFT, "/a");
        history.add(LEFT, "/b");
        history.add(LEFT, "/c");
        history.back(LEFT);
        history.back(LEFT);
        history.add(LEFT, "/d");

        assertEquals(List.of("/a", "/d"), history.getPaths(LEFT));
        assertEquals(1, history.getPosition(LEFT));
        assertEquals(null, storage.getString("history." + LEFT + ".2"));
    }

    @Test
    public void historyIsLimitedToTheMaximumSize() {
        history.add(LEFT, "/a");
        history.add(LEFT, "/b");
        history.add(LEFT, "/c");
        history.add(LEFT, "/d");

        assertEquals(List.of("/b", "/c", "/d"), history.getPaths(LEFT));
        assertEquals(2, history.getPosition(LEFT));
    }

    @Test
    public void locationsHaveSeparateHistories() {
        history.add(LEFT, "/a");
        history.add(LEFT, "/b");
        history.add(RIGHT, "/x");

        assertEquals(List.of("/a", "/b"), history.getPaths(LEFT));
        assertEquals(List.of("/x"), history.getPaths(RIGHT));
        assertEquals(Optional.empty(), history.back(RIGHT));
    }

    @Test
    public void historyIsReloadedFromStorage() {
        history.add(LEFT, "/a");
        history.add(LEFT, "/b");

        StreamerHistory reloaded = new StreamerHistory(storage, () -> 3);
        assertEquals(Optional.of("/a"), reloaded.back(LEFT));
    }

    @Test
    public void forwardReturnsTheNextPathsAfterGoingBack() {
        history.add(LEFT, "/a");
        history.add(LEFT, "/b");
        history.add(LEFT, "/c");
        assertEquals(Optional.empty(), history.forward(LEFT));

        history.back(LEFT);
        history.back(LEFT);
        assertEquals(Optional.of("/b"), history.forward(LEFT));
        history.add(LEFT, "/b");
        assertEquals(Optional.of("/c"), history.forward(LEFT));
        assertEquals(Optional.empty(), history.forward(LEFT));
        assertEquals(List.of("/a", "/b", "/c"), history.getPaths(LEFT));
    }

    @Test
    public void forwardIsEmptyAfterAddingANewPath() {
        history.add(LEFT, "/a");
        history.add(LEFT, "/b");
        history.back(LEFT);
        history.add(LEFT, "/x");

        assertEquals(Optional.empty(), history.forward(LEFT));
        assertEquals(Optional.of("/a"), history.back(LEFT));
    }
}
