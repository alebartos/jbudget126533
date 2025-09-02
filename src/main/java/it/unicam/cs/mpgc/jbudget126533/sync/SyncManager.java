package it.unicam.cs.mpgc.jbudget126533.sync;

import it.unicam.cs.mpgc.jbudget126533.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unicam.cs.mpgc.jbudget126533.controller.Ledger;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestore per la sincronizzazione dei dati tra diverse istanze dell'applicazione.
 * Fornisce funzionalità per creare, applicare, esportare e importare pacchetti di sincronizzazione
 * contenenti tutti i dati dell'applicazione.
 */
public class SyncManager {

    private final Gson gson;
    private final FileManagement fileManagement;

    /**
     * Costruttore del gestore di sincronizzazione.
     * Inizializza il sistema di gestione file e il parser JSON.
     */
    public SyncManager() {
        this.fileManagement = new FileManagement();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Crea un pacchetto di sincronizzazione contenente tutti i dati dell'applicazione.
     * Include transazioni, budget, transazioni programmate, piani di ammortamento e tag.
     *
     * @return oggetto SyncPackage con tutti i dati serializzabili
     */
    public SyncPackage createSyncPackage() {
        SyncPackage syncPackage = new SyncPackage();
        syncPackage.setTimestamp(LocalDateTime.now());
        syncPackage.setDeviceId(getDeviceId());

        try {
            // Carica tutti i dati
            syncPackage.setTransactions(fileManagement.read());
            syncPackage.setBudgets(new ArrayList<>(new BudgetManager(
                    new BudgetManagement(),
                    new Ledger(new BudgetManagement()),
                    fileManagement
            ).getAllBudgets().values()));

            syncPackage.setScheduledTransactions(new ScheduledTransactionManager(
                    new Ledger(new BudgetManagement()),
                    fileManagement
            ).getScheduledTransactions());

            syncPackage.setAmortizationPlans(new AmortizationManager(fileManagement).getAmortizationPlans());

            // Carica i tag
            Map<String, ITag> allTags = TagManager.getAllTagsMap();
            syncPackage.setTags(new ArrayList<>(allTags.values()));
            syncPackage.setSelectedTags(new ArrayList<>(TagManager.getSelectedTagsList()));

        } catch (Exception e) {
            System.err.println("Errore nella creazione del pacchetto di sincronizzazione: " + e.getMessage());
        }

        return syncPackage;
    }

    /**
     * Applica un pacchetto di sincronizzazione ai dati locali.
     * Sincronizza tutti i componenti dell'applicazione in ordine appropriato.
     *
     * @param syncPackage pacchetto da applicare contenente i dati remoti
     * @param conflictResolution strategia per la risoluzione dei conflitti (LOCAL_WINS o REMOTE_WINS)
     * @return true se l'operazione è riuscita, false in caso di errore
     */
    public boolean applySyncPackage(SyncPackage syncPackage, ConflictResolutionStrategy conflictResolution) {
        try {
            // 1. Sincronizza i tag (prima perché sono referenziati da altri dati)
            syncTags(syncPackage.getTags(), syncPackage.getSelectedTags());

            // 2. Sincronizza le transazioni
            syncTransactions(syncPackage.getTransactions(), conflictResolution);

            // 3. Sincronizza i budget
            syncBudgets(syncPackage.getBudgets(), conflictResolution);

            // 4. Sincronizza le transazioni programmate
            syncScheduledTransactions(syncPackage.getScheduledTransactions(), conflictResolution);

            // 5. Sincronizza i piani di ammortamento
            syncAmortizationPlans(syncPackage.getAmortizationPlans(), conflictResolution);

            System.out.println("Sincronizzazione completata con successo");
            return true;

        } catch (Exception e) {
            System.err.println("Errore nell'applicazione del pacchetto di sincronizzazione: " + e.getMessage());
            return false;
        }
    }

    /**
     * Esporta il pacchetto di sincronizzazione su file in formato JSON.
     *
     * @param filePath percorso del file di destinazione dove salvare il pacchetto
     * @return true se l'esportazione è riuscita, false in caso di errore
     */
    public boolean exportSyncPackage(String filePath) {
        try {
            SyncPackage syncPackage = createSyncPackage();
            String json = gson.toJson(syncPackage);

            Files.write(Path.of(filePath), json.getBytes());
            System.out.println("Pacchetto di sincronizzazione esportato: " + filePath);
            return true;

        } catch (Exception e) {
            System.err.println("Errore nell'esportazione del pacchetto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Importa un pacchetto di sincronizzazione da file JSON.
     *
     * @param filePath percorso del file sorgente da cui importare il pacchetto
     * @param conflictResolution strategia per la risoluzione dei conflitti durante l'importazione
     * @return true se l'importazione è riuscita, false in caso di errore
     */
    public boolean importSyncPackage(String filePath, ConflictResolutionStrategy conflictResolution) {
        try {
            String json = new String(Files.readAllBytes(Path.of(filePath)));
            SyncPackage syncPackage = gson.fromJson(json, SyncPackage.class);

            return applySyncPackage(syncPackage, conflictResolution);

        } catch (Exception e) {
            System.err.println("Errore nell'importazione del pacchetto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Genera o recupera un ID univoco per il dispositivo.
     * Se esiste già un file con l'ID del dispositivo, lo recupera, altrimenti ne crea uno nuovo.
     *
     * @return stringa contenente l'ID univoco del dispositivo
     */
    private String getDeviceId() {
        try {
            // Usa l'ID della macchina o genera un UUID
            String deviceIdFile = FilePaths.DATA_DIRECTORY + "/device.id";
            File file = new File(deviceIdFile);

            if (file.exists()) {
                return new String(Files.readAllBytes(file.toPath())).trim();
            } else {
                String newDeviceId = UUID.randomUUID().toString();
                Files.write(file.toPath(), newDeviceId.getBytes());
                return newDeviceId;
            }
        } catch (Exception e) {
            return "device-" + UUID.randomUUID().toString();
        }
    }

    // ==================== METODI DI SINCRONIZZAZIONE SPECIFICI ====================

    /**
     * Sincronizza i tag tra i dati locali e quelli remoti.
     * Aggiunge i tag mancanti e sincronizza la lista dei tag selezionati.
     *
     * @param remoteTags lista dei tag dal pacchetto remoto
     * @param remoteSelectedTags lista dei tag selezionati dal pacchetto remoto
     */
    private void syncTags(List<ITag> remoteTags, List<ITag> remoteSelectedTags) {
        Map<String, ITag> localTags = TagManager.getAllTagsMap();

        // Aggiungi/aggiorna tag remoti
        for (ITag remoteTag : remoteTags) {
            if (!localTags.containsKey(remoteTag.getName())) {
                TagManager.createTag(remoteTag.getName(), remoteTag.getParent());
            }
        }

        // Sincronizza tag selezionati
        Set<String> selectedTagNames = remoteSelectedTags.stream()
                .map(ITag::getName)
                .collect(Collectors.toSet());

        TagManager.getSelectedTagsSet().clear();
        selectedTagNames.forEach(TagManager::selectTag);
    }

    /**
     * Sincronizza le transazioni tra i dati locali e quelli remoti.
     * Applica la strategia di risoluzione conflitti specificata.
     *
     * @param remoteTransactions lista delle transazioni dal pacchetto remoto
     * @param strategy strategia di risoluzione dei conflitti
     */
    private void syncTransactions(List<ITransaction> remoteTransactions, ConflictResolutionStrategy strategy) {
        List<ITransaction> localTransactions;
        try {
            localTransactions = fileManagement.read();
        } catch (IOException e) {
            localTransactions = new ArrayList<>();
        }

        Map<String, ITransaction> localMap = createTransactionMap(localTransactions);
        Map<String, ITransaction> remoteMap = createTransactionMap(remoteTransactions);

        List<ITransaction> mergedTransactions = mergeData(localMap, remoteMap, strategy);

        // Sostituisci tutte le transazioni
        try {
            File movementFile = new File(FilePaths.MOVEMENT_FILE);
            try (FileWriter writer = new FileWriter(movementFile)) {
                writer.write(gson.toJson(mergedTransactions));
            }
            TimestampManager.updateTimestamp(FilePaths.MOVEMENT_FILE);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio delle transazioni sincronizzate: " + e.getMessage());
        }
    }

    /**
     * Sincronizza i budget tra i dati locali e quelli remoti.
     *
     * @param remoteBudgets lista dei budget dal pacchetto remoto
     * @param strategy strategia di risoluzione dei conflitti
     */
    private void syncBudgets(List<Budget> remoteBudgets, ConflictResolutionStrategy strategy) {
        BudgetManager budgetManager = new BudgetManager(new BudgetManagement(), new Ledger(new BudgetManagement()), fileManagement);
        Map<String, Budget> localBudgets = budgetManager.getAllBudgets();
        Map<String, Budget> remoteBudgetMap = remoteBudgets.stream()
                .collect(Collectors.toMap(Budget::getCategory, b -> b));

        // Applica la strategia di merge
        for (Map.Entry<String, Budget> entry : remoteBudgetMap.entrySet()) {
            String category = entry.getKey();
            Budget remoteBudget = entry.getValue();

            if (!localBudgets.containsKey(category) ||
                    strategy == ConflictResolutionStrategy.REMOTE_WINS) {
                budgetManager.setBudget(
                        remoteBudget.getCategory(),
                        remoteBudget.getAllocatedAmount(),
                        remoteBudget.getStartDate(),
                        remoteBudget.getEndDate()
                );
            }
        }
    }

    /**
     * Sincronizza le transazioni programmate tra i dati locali e quelli remoti.
     *
     * @param remoteTransactions lista delle transazioni programmate dal pacchetto remoto
     * @param strategy strategia di risoluzione dei conflitti
     */
    private void syncScheduledTransactions(List<ScheduledTransaction> remoteTransactions, ConflictResolutionStrategy strategy) {
        ScheduledTransactionManager stManager = new ScheduledTransactionManager(new Ledger(new BudgetManagement()), fileManagement);
        List<ScheduledTransaction> localTransactions = stManager.getScheduledTransactions();

        Map<String, ScheduledTransaction> localMap = localTransactions.stream()
                .collect(Collectors.toMap(this::getScheduledTransactionKey, st -> st));

        Map<String, ScheduledTransaction> remoteMap = remoteTransactions.stream()
                .collect(Collectors.toMap(this::getScheduledTransactionKey, st -> st));

        List<ScheduledTransaction> merged = mergeScheduledTransactions(localMap, remoteMap, strategy);

        // Sostituisci tutte le transazioni programmate
        stManager.getScheduledTransactions().clear();
        stManager.getScheduledTransactions().addAll(merged);
        stManager.saveScheduledTransactions();
    }

    /**
     * Sincronizza i piani di ammortamento tra i dati locali e quelli remoti.
     *
     * @param remotePlans lista dei piani di ammortamento dal pacchetto remoto
     * @param strategy strategia di risoluzione dei conflitti
     */
    private void syncAmortizationPlans(List<AmortizationPlan> remotePlans, ConflictResolutionStrategy strategy) {
        AmortizationManager amortizationManager = new AmortizationManager(fileManagement);
        List<AmortizationPlan> localPlans = amortizationManager.getAmortizationPlans();

        Map<String, AmortizationPlan> localMap = localPlans.stream()
                .collect(Collectors.toMap(AmortizationPlan::getId, p -> p));

        Map<String, AmortizationPlan> remoteMap = remotePlans.stream()
                .collect(Collectors.toMap(AmortizationPlan::getId, p -> p));

        // Applica la strategia di merge
        for (Map.Entry<String, AmortizationPlan> entry : remoteMap.entrySet()) {
            String planId = entry.getKey();
            AmortizationPlan remotePlan = entry.getValue();

            if (!localMap.containsKey(planId) ||
                    strategy == ConflictResolutionStrategy.REMOTE_WINS) {
                // Elimina il piano esistente se presente
                if (localMap.containsKey(planId)) {
                    amortizationManager.deleteAmortizationPlan(planId);
                }

                // Ricrea il piano
                amortizationManager.createAmortizationPlan(
                        remotePlan.getDescription(),
                        remotePlan.getPrincipalAmount(),
                        remotePlan.getAnnualInterestRate(),
                        remotePlan.getNumberOfInstallments(),
                        remotePlan.getStartDate(),
                        remotePlan.getTags()
                );
            }
        }
    }

    // ==================== METODI DI UTILITY ====================

    /**
     * Crea una mappa di transazioni utilizzando una chiave univoca composta da data, utente e importo.
     *
     * @param transactions lista di transazioni da convertire in mappa
     * @return mappa di transazioni con chiavi univoche
     */
    private Map<String, ITransaction> createTransactionMap(List<ITransaction> transactions) {
        return transactions.stream()
                .collect(Collectors.toMap(
                        t -> t.getDate().toString() + "_" + t.getUser() + "_" + t.getMoney(),
                        t -> t
                ));
    }

    /**
     * Genera una chiave univoca per una transazione programmata.
     *
     * @param st transazione programmata per cui generare la chiave
     * @return stringa rappresentante la chiave univoca
     */
    private String getScheduledTransactionKey(ScheduledTransaction st) {
        return st.getDescription() + "_" + st.getAmount() + "_" + st.getRecurrence();
    }

    /**
     * Esegue il merge di due mappe di dati applicando la strategia di risoluzione conflitti specificata.
     *
     * @param <T> tipo dei dati da mergiare
     * @param localMap mappa dei dati locali
     * @param remoteMap mappa dei dati remoti
     * @param strategy strategia di risoluzione dei conflitti
     * @return lista contenente i dati mergiati
     */
    private <T> List<T> mergeData(Map<String, T> localMap, Map<String, T> remoteMap, ConflictResolutionStrategy strategy) {
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(localMap.keySet());
        allKeys.addAll(remoteMap.keySet());

        List<T> merged = new ArrayList<>();

        for (String key : allKeys) {
            if (localMap.containsKey(key) && remoteMap.containsKey(key)) {
                // Conflitto: applica la strategia
                if (strategy == ConflictResolutionStrategy.LOCAL_WINS) {
                    merged.add(localMap.get(key));
                } else {
                    merged.add(remoteMap.get(key));
                }
            } else if (localMap.containsKey(key)) {
                merged.add(localMap.get(key));
            } else {
                merged.add(remoteMap.get(key));
            }
        }

        return merged;
    }

    /**
     * Esegue il merge delle transazioni programmate applicando la strategia di risoluzione conflitti.
     *
     * @param localMap mappa delle transazioni programmate locali
     * @param remoteMap mappa delle transazioni programmate remote
     * @param strategy strategia di risoluzione dei conflitti
     * @return lista contenente le transazioni programmate mergiate
     */
    private List<ScheduledTransaction> mergeScheduledTransactions(
            Map<String, ScheduledTransaction> localMap,
            Map<String, ScheduledTransaction> remoteMap,
            ConflictResolutionStrategy strategy) {

        return mergeData(localMap, remoteMap, strategy);
    }
}