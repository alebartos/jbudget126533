package it.unicam.cs.mpgc.jbudget126533.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestisce i timestamp di modifica inseriti direttamente nei file.
 */
public final class TimestampManager {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String TIMESTAMP_PREFIX = "// TIMESTAMP: ";

    private static final Map<String, LocalDateTime> timestampCache = new HashMap<>();

    private TimestampManager() {
        throw new UnsupportedOperationException("Classe di utilità, non istanziabile");
    }

    /**
     * Inserisce o aggiorna il timestamp nella prima riga di un file.
     *
     * @param filePath percorso del file
     */
    public static void updateTimestamp(String filePath) {
        LocalDateTime now = LocalDateTime.now();
        File file = new File(filePath);

        if (!file.exists()) {
            return;
        }

        try {
            // Legge tutto il contenuto del file
            String content = new String(Files.readAllBytes(file.toPath()));

            // Divide in righe
            String[] lines = content.split("\n");
            StringBuilder newContent = new StringBuilder();

            // Controlla se la prima riga è già un timestamp
            boolean hasTimestamp = lines.length > 0 && lines[0].startsWith(TIMESTAMP_PREFIX);

            // Aggiunge il nuovo timestamp come prima riga
            newContent.append(TIMESTAMP_PREFIX).append(now.format(TIMESTAMP_FORMATTER)).append("\n");

            // Aggiunge il resto del contenuto (saltando il vecchio timestamp se esiste)
            int startLine = hasTimestamp ? 1 : 0;
            for (int i = startLine; i < lines.length; i++) {
                newContent.append(lines[i]);
                if (i < lines.length - 1) {
                    newContent.append("\n");
                }
            }

            // Scrive il nuovo contenuto
            Files.write(
                    file.toPath(),
                    newContent.toString().getBytes(),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            timestampCache.put(filePath, now);
            System.out.println("Timestamp aggiornato per " + filePath + ": " + now.format(TIMESTAMP_FORMATTER));

        } catch (IOException e) {
            System.err.println("Errore nell'aggiornamento del timestamp per " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Legge il timestamp dalla prima riga di un file.
     *
     * @param filePath percorso del file
     * @return il timestamp dell'ultima modifica, o null se non esiste
     */
    public static LocalDateTime readTimestamp(String filePath) {
        // Controlla prima la cache
        if (timestampCache.containsKey(filePath)) {
            return timestampCache.get(filePath);
        }

        File file = new File(filePath);

        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();

            if (firstLine != null && firstLine.startsWith(TIMESTAMP_PREFIX)) {
                String timestampStr = firstLine.substring(TIMESTAMP_PREFIX.length()).trim();
                LocalDateTime timestamp = LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
                timestampCache.put(filePath, timestamp);
                return timestamp;
            }

        } catch (Exception e) {
            System.err.println("Errore nella lettura del timestamp per " + filePath + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Rimuove il timestamp da un file (utile per operazioni di pulizia).
     *
     * @param filePath percorso del file
     */
    public static void removeTimestamp(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            return;
        }

        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            String[] lines = content.split("\n");

            // Se la prima riga è un timestamp, la salta
            boolean hasTimestamp = lines.length > 0 && lines[0].startsWith(TIMESTAMP_PREFIX);

            if (hasTimestamp) {
                StringBuilder newContent = new StringBuilder();
                for (int i = 1; i < lines.length; i++) {
                    newContent.append(lines[i]);
                    if (i < lines.length - 1) {
                        newContent.append("\n");
                    }
                }

                Files.write(
                        file.toPath(),
                        newContent.toString().getBytes(),
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                );
            }

            timestampCache.remove(filePath);

        } catch (IOException e) {
            System.err.println("Errore nella rimozione del timestamp per " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Restituisce il timestamp formattato come stringa leggibile.
     *
     * @param filePath percorso del file principale
     * @return stringa formattata del timestamp, o "Mai modificato" se non esiste
     */
    public static String getFormattedTimestamp(String filePath) {
        LocalDateTime timestamp = readTimestamp(filePath);
        if (timestamp == null) {
            return "Mai modificato";
        }
        return timestamp.format(TIMESTAMP_FORMATTER);
    }

    /**
     * Verifica se un file è stato modificato dopo una certa data.
     *
     * @param filePath percorso del file
     * @param since data da verificare
     * @return true se il file è stato modificato dopo la data specificata
     */
    public static boolean isModifiedAfter(String filePath, LocalDateTime since) {
        LocalDateTime timestamp = readTimestamp(filePath);
        return timestamp != null && timestamp.isAfter(since);
    }

    /**
     * Carica tutti i timestamp nella cache all'avvio dell'applicazione.
     */
    public static void loadAllTimestamps() {
        String[] mainFiles = {
                FilePaths.MOVEMENT_FILE,
                FilePaths.TAG_FILE,
                FilePaths.ALL_TAGS_FILE,
                FilePaths.BUDGET_FILE,
                FilePaths.SCHEDULED_FILE,
                FilePaths.AMORTIZATION_FILE
        };

        for (String filePath : mainFiles) {
            readTimestamp(filePath); // Questo caricherà nella cache
        }
    }

    /**
     * Restituisce una mappa con tutti i timestamp correnti.
     *
     * @return mappa con i percorsi dei file e i relativi timestamp
     */
    public static Map<String, LocalDateTime> getAllTimestamps() {
        Map<String, LocalDateTime> allTimestamps = new HashMap<>();

        String[] mainFiles = {
                FilePaths.MOVEMENT_FILE,
                FilePaths.TAG_FILE,
                FilePaths.ALL_TAGS_FILE,
                FilePaths.BUDGET_FILE,
                FilePaths.SCHEDULED_FILE,
                FilePaths.AMORTIZATION_FILE
        };

        for (String filePath : mainFiles) {
            LocalDateTime timestamp = readTimestamp(filePath);
            if (timestamp != null) {
                allTimestamps.put(filePath, timestamp);
            }
        }

        return allTimestamps;
    }

    /**
     * Legge il contenuto di un file saltando la riga del timestamp.
     *
     * @param filePath percorso del file
     * @return contenuto del file senza la riga del timestamp
     */
    public static String readFileWithoutTimestamp(String filePath) throws IOException {
        File file = new File(filePath);

        if (!file.exists()) {
            return "";
        }

        String content = new String(Files.readAllBytes(file.toPath()));
        String[] lines = content.split("\n");

        // Se la prima riga è un timestamp, la salta
        boolean hasTimestamp = lines.length > 0 && lines[0].startsWith(TIMESTAMP_PREFIX);

        if (hasTimestamp) {
            StringBuilder contentWithoutTimestamp = new StringBuilder();
            for (int i = 1; i < lines.length; i++) {
                contentWithoutTimestamp.append(lines[i]);
                if (i < lines.length - 1) {
                    contentWithoutTimestamp.append("\n");
                }
            }
            return contentWithoutTimestamp.toString();
        }

        return content;
    }
}