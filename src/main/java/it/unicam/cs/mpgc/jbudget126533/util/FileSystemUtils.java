package it.unicam.cs.mpgc.jbudget126533.util;

import it.unicam.cs.mpgc.jbudget126533.model.FilePaths;
import it.unicam.cs.mpgc.jbudget126533.model.TimestampManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Classe di utilità per la gestione delle directory e dei file persistenti dell'applicazione.
 * <p>
 * Fornisce metodi per:
 * <ul>
 *     <li>Visualizzare i timestamp dei file di dati.</li>
 *     <li>Inizializzare la struttura delle directory necessarie.</li>
 *     <li>Creare file mancanti con contenuto iniziale vuoto.</li>
 *     <li>Creare backup dei file di dati.</li>
 *     <li>Verificare l'esistenza dei file richiesti.</li>
 * </ul>
 */
public class FileSystemUtils {

    /**
     * Mostra i timestamp di tutti i file di dati gestiti dall'applicazione.
     * <p>
     * I timestamp vengono letti tramite {@link TimestampManager} e stampati in formato
     * "dd/MM/yyyy HH:mm:ss".
     */
    public static void displayAllTimestamps() {
        System.out.println("\n=== TIMESTAMP DEI FILE ===");

        Map<String, LocalDateTime> timestamps = TimestampManager.getAllTimestamps();

        timestamps.forEach((filePath, timestamp) -> {
            String fileName = new File(filePath).getName();
            String formattedTime = timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            System.out.printf("%-25s: %s%n", fileName, formattedTime);
        });
    }

    /**
     * Inizializza la struttura delle directory necessarie per l'applicazione.
     * <p>
     * Crea la cartella principale dei dati e una sottocartella per i backup, se non esistono già.
     */
    public static void initializeDataDirectory() {
        File dataDir = new File(FilePaths.DATA_DIRECTORY);
        if (!dataDir.exists()) {
            if (dataDir.mkdirs()) {
                System.out.println("Directory dati creata: " + dataDir.getAbsolutePath());
            } else {
                System.err.println("Impossibile creare la directory dati: " + dataDir.getAbsolutePath());
            }
        }

        // Crea anche la directory backups
        File backupsDir = new File(FilePaths.DATA_DIRECTORY + "/backups");
        if (!backupsDir.exists()) {
            backupsDir.mkdirs();
        }
    }

    /**
     * Crea file vuoti per quelli mancanti.
     * <p>
     * I file creati vengono inizializzati con contenuto appropriato (es. "[]" per JSON vuoto)
     * e il loro timestamp viene aggiornato tramite {@link TimestampManager}.
     */
    public static void createMissingFiles() {
        String[] filesToCreate = {
                FilePaths.MOVEMENT_FILE,
                FilePaths.TAG_FILE,
                FilePaths.ALL_TAGS_FILE,
                FilePaths.BUDGET_FILE,
                FilePaths.SCHEDULED_FILE,
                FilePaths.AMORTIZATION_FILE
        };

        for (String filePath : filesToCreate) {
            File file = new File(filePath);
            if (!file.exists()) {
                try {
                    if (file.createNewFile()) {
                        // Inizializza con contenuto JSON vuoto
                        try (FileWriter writer = new FileWriter(file)) {
                            writer.write("[]");
                        }

                        // Aggiorna timestamp
                        TimestampManager.updateTimestamp(filePath);
                        System.out.println("File creato: " + file.getName());
                    }
                } catch (IOException e) {
                    System.err.println("Errore nella creazione del file " + filePath + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Crea un backup dei file di dati nell'apposita cartella "backups".
     * <p>
     * Ogni file viene copiato con un suffisso ".backup.<timestamp>" per permettere versioni multiple.
     */
    public static void createBackup() {
        try {
            Path backupDir = Path.of(FilePaths.DATA_DIRECTORY, "backups");
            Files.createDirectories(backupDir);

            List<String> filesToBackup = Arrays.asList(
                    FilePaths.MOVEMENT_FILE,
                    FilePaths.TAG_FILE,
                    FilePaths.ALL_TAGS_FILE,
                    FilePaths.BUDGET_FILE,
                    FilePaths.SCHEDULED_FILE,
                    FilePaths.AMORTIZATION_FILE
            );

            for (String filePath : filesToBackup) {
                File source = new File(filePath);
                if (source.exists()) {
                    Path target = backupDir.resolve(source.getName() + ".backup." + System.currentTimeMillis());
                    Files.copy(source.toPath(), target);
                }
            }

        } catch (IOException e) {
            System.err.println("Errore durante il backup: " + e.getMessage());
        }
    }

    /**
     * Verifica se tutti i file richiesti dall'applicazione esistono.
     */
    public static void checkAllFilesExist() {
        List<String> requiredFiles = Arrays.asList(
                FilePaths.MOVEMENT_FILE,
                FilePaths.TAG_FILE,
                FilePaths.ALL_TAGS_FILE,
                FilePaths.BUDGET_FILE,
                FilePaths.SCHEDULED_FILE,
                FilePaths.AMORTIZATION_FILE
        );

        requiredFiles.stream().allMatch(filePath -> new File(filePath).exists());
    }
}
