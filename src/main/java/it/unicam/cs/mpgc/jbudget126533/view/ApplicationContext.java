package it.unicam.cs.mpgc.jbudget126533.view;

import it.unicam.cs.mpgc.jbudget126533.model.Ledger;
import it.unicam.cs.mpgc.jbudget126533.model.ITag;
import it.unicam.cs.mpgc.jbudget126533.model.TagManager;
import it.unicam.cs.mpgc.jbudget126533.model.TransactionManager;
import it.unicam.cs.mpgc.jbudget126533.sync.SyncManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ApplicationContext {
    private static Ledger ledger;
    private static SyncManager syncManager;
    private static NavigationService navigationService;
    private static final Map<String, Object> controllers = new ConcurrentHashMap<>();
    private static final ObservableList<ITag> selectedTags = FXCollections.observableArrayList();

    private ApplicationContext() {}

    public static void init() {
        if (ledger == null) {
            TagManager.loadAllTags();
            TagManager.loadSelectedTags();
            selectedTags.setAll(TagManager.getSelectedTagsList());
            ledger = new Ledger(new TransactionManager());
            ledger.read();
            ledger.updateBudgets();
        }
        if (syncManager == null) {
            syncManager = new it.unicam.cs.mpgc.jbudget126533.sync.SyncManager();
        }
    }

    public static ObservableList<ITag> selectedTags() { return selectedTags; }

    public static Ledger ledger() { return ledger; }

    public static SyncManager sync() { return syncManager; }

    public static void setNavigationService(NavigationService nav) { navigationService = nav; }

    public static NavigationService nav() { return navigationService; }

    public static void registerController(String key, Object controller) {
        controllers.put(key, controller);
    }

    public static void selectTag(ITag tag) {
        if (tag == null) return;
        if (!selectedTags.contains(tag)) {
            TagManager.selectTag(tag.getName());
            selectedTags.add(tag);
        }
    }
    public static void deselectTag(ITag tag) {
        if (tag == null) return;
        if (selectedTags.remove(tag)) {
            TagManager.deselectTag(tag.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getController(String key, Class<T> type) {
        Object c = controllers.get(key);
        return (c != null && type.isInstance(c)) ? (T) c : null;
    }
}
