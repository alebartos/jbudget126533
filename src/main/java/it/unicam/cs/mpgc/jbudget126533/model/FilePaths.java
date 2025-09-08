package it.unicam.cs.mpgc.jbudget126533.model;

/**
 * Classe che contiene tutti i percorsi dei file persistenti dell'applicazione.
 * Centralizza la gestione dei path per facilitare la manutenzione e la consistenza.
 */
public final class FilePaths {

    // Directory principale per tutti i file di dati
    public static final String DATA_DIRECTORY = "Data";

    // File per le transazioni
    public static final String MOVEMENT_FILE = DATA_DIRECTORY + "/Movement.json";

    // File per i tag
    public static final String TAG_FILE = DATA_DIRECTORY + "/Tags.json";
    public static final String ALL_TAGS_FILE = DATA_DIRECTORY + "/AllTags.json";

    // File per i budget
    public static final String BUDGET_FILE = DATA_DIRECTORY + "/Budgets.json";

    // File per le transazioni programmate
    public static final String SCHEDULED_FILE = DATA_DIRECTORY + "/ScheduledTransactions.json";

    // File per i piani di ammortamento
    public static final String AMORTIZATION_FILE = DATA_DIRECTORY + "/AmortizationPlans.json";

    // Costruttore privato per prevenire l'istanziazione
    private FilePaths() {
        throw new UnsupportedOperationException("Questa è una classe di utilità e non può essere istanziata");
    }

    /**
     * Restituisce il nome del file senza il percorso della directory.
     *
     * @param fullPath percorso completo del file
     * @return nome del file senza directory
     */
    public static String getFileNameOnly(String fullPath) {
        return fullPath.replace(FilePaths.DATA_DIRECTORY + "/", "");
    }

    /**
     * Restituisce il percorso completo per un file nella directory dati.
     *
     * @param fileName nome del file
     * @return percorso completo
     */
    public static String getFullPath(String fileName) {
        return DATA_DIRECTORY + "/" + fileName;
    }

    /**
     * Restituisce il percorso completo per un file di backup.
     *
     * @param baseName nome base del file
     * @return percorso completo del file di backup
     */
    public static String getBackupPath(String baseName) {
        return DATA_DIRECTORY + "/backups/" + baseName + "_" + System.currentTimeMillis() + ".json";
    }

    /**
     * Restituisce il percorso completo per un file temporaneo.
     *
     * @param baseName nome base del file
     * @return percorso completo del file temporaneo
     */
    public static String getTempPath(String baseName) {
        return DATA_DIRECTORY + "/" + baseName + "_temp.json";
    }
}
