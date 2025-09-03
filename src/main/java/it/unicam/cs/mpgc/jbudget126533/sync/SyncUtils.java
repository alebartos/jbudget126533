package it.unicam.cs.mpgc.jbudget126533.sync;

import it.unicam.cs.mpgc.jbudget126533.model.FilePaths;
import it.unicam.cs.mpgc.jbudget126533.model.TimestampManager;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility per operazioni di sincronizzazione dei dati dell'applicazione JBudget.
 * <p>
 * Fornisce metodi per:
 * <ul>
 *     <li>Verificare modifiche locali non sincronizzate.</li>
 *     <li>Ottenere lo stato di sincronizzazione dei file.</li>
 *     <li>Creare backup pre-sincronizzazione.</li>
 *     <li>Calcolare statistiche dei dati per la sincronizzazione.</li>
 * </ul>
 * Questa classe non è istanziabile.
 */
public final class SyncUtils {

    /** Costruttore privato per impedire l'istanza */
    private SyncUtils() {
        throw new UnsupportedOperationException("Classe di utilità, non istanziabile");
    }

    /**
     * Verifica se ci sono modifiche locali non sincronizzate rispetto all'ultimo timestamp di sincronizzazione.
     *
     * @param lastSyncTime ultimo timestamp di sincronizzazione
     * @return true se ci sono modifiche non sincronizzate o se è la prima sincronizzazione
     */
    public static boolean hasUnsynchronizedChanges(LocalDateTime lastSyncTime) {
        if (lastSyncTime == null) {
            return true; // Prima sincronizzazione
        }

        String[] filesToCheck = {
                FilePaths.MOVEMENT_FILE,
                FilePaths.TAG_FILE,
                FilePaths.ALL_TAGS_FILE,
                FilePaths.BUDGET_FILE,
                FilePaths.SCHEDULED_FILE,
                FilePaths.AMORTIZATION_FILE
        };

        for (String filePath : filesToCheck) {
            if (TimestampManager.isModifiedAfter(filePath, lastSyncTime)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Restituisce lo stato di sincronizzazione di tutti i file gestiti dall'applicazione.
     *
     * @param lastSyncTime ultimo timestamp di sincronizzazione
     * @return mappa che associa il nome di ciascun file al relativo {@link SyncStatus}
     */
    public static Map<String, SyncStatus> getSyncStatus(LocalDateTime lastSyncTime) {
        Map<String, SyncStatus> status = new HashMap<>();

        String[] filesToCheck = {
                FilePaths.MOVEMENT_FILE,
                FilePaths.TAG_FILE,
                FilePaths.ALL_TAGS_FILE,
                FilePaths.BUDGET_FILE,
                FilePaths.SCHEDULED_FILE,
                FilePaths.AMORTIZATION_FILE
        };

        for (String filePath : filesToCheck) {
            String fileName = new File(filePath).getName();
            LocalDateTime lastModified = TimestampManager.readTimestamp(filePath);

            if (lastModified == null) {
                status.put(fileName, SyncStatus.NEVER_SYNCED);
            } else if (lastSyncTime == null || lastModified.isAfter(lastSyncTime)) {
                status.put(fileName, SyncStatus.UNSYNCED);
            } else {
                status.put(fileName, SyncStatus.SYNCED);
            }
        }

        return status;
    }

    /**
     * Crea un backup dei dati prima della sincronizzazione.
     *
     * @return percorso del file di backup creato, oppure null in caso di errore
     */
    public static String createPreSyncBackup() {
        try {
            String backupDir = FilePaths.DATA_DIRECTORY + "/backups/sync";
            new File(backupDir).mkdirs();

            // La logica di backup completo dovrebbe essere implementata qui
            return backupDir + "/pre_sync_backup_" + System.currentTimeMillis() + ".zip";

        } catch (Exception e) {
            System.err.println("Errore nella creazione del backup: " + e.getMessage());
            return null;
        }
    }

    /**
     * Calcola statistiche sui dati dell'applicazione, utili per la sincronizzazione.
     *
     * @return oggetto {@link SyncStatistics} con i conteggi dei dati
     */
    public static SyncStatistics calculateStatistics() {
        // Implementazione specifica per il calcolo delle statistiche
        return new SyncStatistics();
    }
}

/**
 * Enum che rappresenta lo stato di sincronizzazione di un file.
 */
enum SyncStatus {
    /** Il file è sincronizzato correttamente */
    SYNCED,

    /** Il file è stato modificato dopo l'ultima sincronizzazione */
    UNSYNCED,

    /** Il file non è mai stato sincronizzato */
    NEVER_SYNCED,

    /** Il file è in conflitto */
    CONFLICT
}

/**
 * Classe che contiene statistiche sui dati dell'applicazione per la sincronizzazione.
 * <p>
 * Include il conteggio di transazioni, budget, transazioni programmate, piani di ammortamento e tag.
 */
class SyncStatistics {
    private int transactionCount;
    private int budgetCount;
    private int scheduledTransactionCount;
    private int amortizationPlanCount;
    private int tagCount;

    /** Restituisce il numero di transazioni */
    public int getTransactionCount() { return transactionCount; }

    /** Imposta il numero di transazioni */
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

    /** Restituisce il numero di budget */
    public int getBudgetCount() { return budgetCount; }

    /** Imposta il numero di budget */
    public void setBudgetCount(int budgetCount) { this.budgetCount = budgetCount; }

    /** Restituisce il numero di transazioni programmate */
    public int getScheduledTransactionCount() { return scheduledTransactionCount; }

    /** Imposta il numero di transazioni programmate */
    public void setScheduledTransactionCount(int scheduledTransactionCount) { this.scheduledTransactionCount = scheduledTransactionCount; }

    /** Restituisce il numero di piani di ammortamento */
    public int getAmortizationPlanCount() { return amortizationPlanCount; }

    /** Imposta il numero di piani di ammortamento */
    public void setAmortizationPlanCount(int amortizationPlanCount) { this.amortizationPlanCount = amortizationPlanCount; }

    /** Restituisce il numero di tag */
    public int getTagCount() { return tagCount; }

    /** Imposta il numero di tag */
    public void setTagCount(int tagCount) { this.tagCount = tagCount; }
}
