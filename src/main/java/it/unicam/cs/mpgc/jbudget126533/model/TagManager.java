package it.unicam.cs.mpgc.jbudget126533.model;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestore centrale dei tag dell'applicazione che utilizza IFileManagement per la persistenza.
 */
public class TagManager {

    private static final Map<String, ITag> allTags = new HashMap<>();
    private static final Set<String> selectedTags = new HashSet<>();
    private static IFileManagement fileManagement = new FileManagement();

    private static final String TAG_FILE = FilePaths.TAG_FILE;
    private static final String ALL_TAGS_FILE = FilePaths.ALL_TAGS_FILE;
    public static final String ALL_TAGS_FILE_PATH = FilePaths.ALL_TAGS_FILE;
    public static Map<String, ITag> getAllTagsMap() { return allTags; }
    public static Set<String> getSelectedTagsSet() { return selectedTags; }

    static {
        loadAllTags();        // Carica tutti i tag salvati
        loadSelectedTags();   // Carica tag selezionati
        if (allTags.isEmpty()) {
            initializeDefaultTags();
            saveAllTags();
        }
    }

    /**
     * Imposta un gestore di file personalizzato (utile per testing).
     */
    public static void setFileManagement(IFileManagement customFileManagement) {
        fileManagement = customFileManagement;
    }

    /**
     * Inizializza i tag di default, principali e sottocategorie.
     */
    private static void initializeDefaultTags() {
        ITag utilities = createTagInternal("Utenze", null);
        ITag transport = createTagInternal("Trasporti", null);
        ITag food = createTagInternal("Alimentari", null);
        ITag entertainment = createTagInternal("Svago", null);
        ITag health = createTagInternal("Salute", null);
        ITag income = createTagInternal("Entrate", null);

        // Sottocategorie
        createTagInternal("Luce", utilities);
        createTagInternal("Gas", utilities);
        createTagInternal("Acqua", utilities);
        createTagInternal("Internet", utilities);

        createTagInternal("Carburante", transport);
        createTagInternal("Manutenzione", transport);
        createTagInternal("Assicurazione", transport);

        createTagInternal("Supermercato", food);
        createTagInternal("Ristorante", food);

        createTagInternal("Cinema", entertainment);
        createTagInternal("Sport", entertainment);
        createTagInternal("Viaggi", entertainment);

        createTagInternal("Medico", health);
        createTagInternal("Farmaci", health);

        createTagInternal("Stipendio", income);
        createTagInternal("Bonus", income);
        createTagInternal("Investimenti", income);
    }

    /**
     * Crea un tag senza salvare immediatamente su file.
     */
    private static ITag createTagInternal(String name, ITag parent) {
        ITag tag = new Tag(name, parent);
        allTags.put(name, tag);
        return tag;
    }

    /**
     * Crea un nuovo tag e lo salva. Se esiste già, lo restituisce.
     */
    public static ITag createTag(String name, ITag parent) {
        if (allTags.containsKey(name)) return allTags.get(name);
        ITag tag = new Tag(name, parent);
        allTags.put(name, tag);
        saveAllTags();
        return tag;
    }

    public static ITag getTag(String name) { return allTags.get(name); }

    public static Map<String, ITag> getAllTags() { return new HashMap<>(allTags); }

    public static boolean tagExists(String name) { return allTags.containsKey(name); }

    // ----------------- gestione tag selezionati -----------------

    public static void selectTag(String name) {
        if (tagExists(name)) {
            selectedTags.add(name);
            saveSelectedTags();
        }
    }

    public static void deselectTag(String name) {
        selectedTags.remove(name);
        saveSelectedTags();
    }

    public static boolean isTagSelected(String name) { return selectedTags.contains(name); }

    public static List<ITag> getSelectedTagsList() {
        return selectedTags.stream()
                .map(TagManager::getTag)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ----------------- caricamento/salvataggio -----------------

    public static void loadSelectedTags() {
        try {
            Type type = new TypeToken<Set<String>>() {}.getType();
            Set<String> loaded = fileManagement.readObject(FilePaths.getFileNameOnly(FilePaths.TAG_FILE), type);
            if (loaded != null) selectedTags.addAll(loaded);
        } catch (Exception e) {
            System.err.println("Errore nel caricamento dei tag selezionati: " + e.getMessage());
        }
    }

    public static void saveSelectedTags() {
        try {
            fileManagement.writeObject(FilePaths.getFileNameOnly(FilePaths.TAG_FILE), selectedTags);
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio dei tag selezionati: " + e.getMessage());
        }
    }

    /**
     * Classe helper per la serializzazione della gerarchia dei tag.
     */
    private static class TagStructure {
        String name;
        String parentName;
        List<String> childrenNames;

        TagStructure(ITag tag) {
            this.name = tag.getName();
            this.parentName = (tag.getParent() != null) ? tag.getParent().getName() : null;
            this.childrenNames = tag.getChildren().stream()
                    .map(ITag::getName)
                    .collect(Collectors.toList());
        }
    }

    public static void loadAllTags() {
        try {
            Type type = new TypeToken<List<TagStructure>>() {}.getType();
            List<TagStructure> loadedStructures = fileManagement.readObject(
                    FilePaths.getFileNameOnly(FilePaths.ALL_TAGS_FILE), type);

            if (loadedStructures != null) {
                rebuildTagHierarchy(loadedStructures);
            } else {
                System.out.println("File AllTags.json non trovato, uso tag predefiniti");
            }

        } catch (Exception e) {
            System.err.println("Errore grave nel caricamento: " + e.getMessage());
            resetAllTags();
        }
    }

    private static void rebuildTagHierarchy(List<TagStructure> structures) {
        allTags.clear();

        // Prima passata: crea tutti i tag senza relazioni
        for (TagStructure ts : structures) {
            Tag tag = new Tag(ts.name);
            allTags.put(ts.name, tag);
        }

        // Seconda passata: ricostruisci le relazioni
        for (TagStructure ts : structures) {
            ITag currentTag = allTags.get(ts.name);
            if (ts.parentName != null) {
                ITag parent = allTags.get(ts.parentName);
                if (parent != null) {
                    currentTag.setParent(parent);
                }
            }
        }
    }

    public static void saveAllTags() {
        try {
            List<TagStructure> tagStructures = allTags.values().stream()
                    .map(TagStructure::new)
                    .collect(Collectors.toList());

            fileManagement.writeObject(FilePaths.getFileNameOnly(FilePaths.ALL_TAGS_FILE), tagStructures);
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio di tutti i tag: " + e.getMessage());
        }
    }

    /**
     * Reset completo dei tag selezionati e gerarchici, con creazione di default.
     */
    public static void resetAllTags() {
        try {
            // Usa IFileManagement per eliminare i file
            fileManagement.writeObject(ALL_TAGS_FILE, Collections.emptyList());
            fileManagement.writeObject(TAG_FILE, Collections.emptySet());

            allTags.clear();
            selectedTags.clear();
            initializeDefaultTags();
            saveAllTags();

            System.out.println("Tag resettati con successo");
        } catch (Exception e) {
            System.err.println("Errore nel reset dei tag: " + e.getMessage());
        }
    }
}