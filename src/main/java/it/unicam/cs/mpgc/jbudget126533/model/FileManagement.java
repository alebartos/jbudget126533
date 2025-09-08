package it.unicam.cs.mpgc.jbudget126533.model;

import com.google.gson.*;

import java.util.Collections;

import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione di {@link IFileManagement} che gestisce la persistenza
 * di transazioni e oggetti generici su file JSON.
 * <p>
 * La classe utilizza Gson per serializzare e deserializzare oggetti,
 * supporta {@link LocalDate} e {@link ITag}, e salva i dati nella cartella "Data".
 * Fornisce metodi per leggere e scrivere transazioni singole o oggetti generici,
 * gestendo anche file corrotti o mancanti tramite backup e ricreazione.
 */
public class FileManagement implements IFileManagement {

    private final File MOVEMENT_FILE = new File(FilePaths.MOVEMENT_FILE);
    private final Gson gson;

    public FileManagement() {
        try {
            new File(FilePaths.DATA_DIRECTORY).mkdirs();

            if (!MOVEMENT_FILE.exists()) {
                MOVEMENT_FILE.createNewFile();
                try (FileWriter writer = new FileWriter(MOVEMENT_FILE)) {
                    writer.write("[]");
                }
                // Aggiunge il timestamp al file appena creato
                TimestampManager.updateTimestamp(FilePaths.MOVEMENT_FILE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        gson = new GsonBuilder()
                .registerTypeAdapter(ITag.class, new ITagTypeAdapter())
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString()))
                .setPrettyPrinting()
                .create();
    }

    @Override
    public void write(ITransaction transaction) {
        try {
            List<ITransaction> allTransactions = read();
            allTransactions.add(transaction);

            File tempFile = new File(FilePaths.getTempPath("Movement"));
            try (FileWriter writer = new FileWriter(tempFile)) {
                gson.toJson(allTransactions, writer);
            }

            if (MOVEMENT_FILE.exists()) {
                MOVEMENT_FILE.delete();
            }
            tempFile.renameTo(MOVEMENT_FILE);

            // Aggiorna il timestamp nella prima riga del file
            TimestampManager.updateTimestamp(FilePaths.MOVEMENT_FILE);

        } catch (IOException e) {
            System.err.println("Errore di scrittura file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<ITransaction> read() {
        ArrayList<ITransaction> transactions = new ArrayList<>();

        try {
            // Legge il file saltando il timestamp
            String content = TimestampManager.readFileWithoutTimestamp(FilePaths.MOVEMENT_FILE);

            if (content.trim().isEmpty()) {
                return transactions;
            }

            content = content.trim();
            if (!content.startsWith("[") || !content.endsWith("]")) {
                recreateFile();
                return transactions;
            }

            Type transactionListType = new TypeToken<ArrayList<Transaction>>(){}.getType();
            List<Transaction> loadedTransactions = gson.fromJson(content, transactionListType);

            if (loadedTransactions != null) {
                transactions.addAll(loadedTransactions);
            }

        } catch (JsonSyntaxException e) {
            System.err.println("Errore nel parsing del file JSON: " + e.getMessage());
            recreateFile();
        } catch (Exception e) {
            System.err.println("Errore imprevisto: " + e.getMessage());
            e.printStackTrace();
            recreateFile();
        }

        return transactions;
    }

    @Override
    public void writeObject(String fileName, Object object) {
        try {
            // Usa il percorso completo
            String fullPath = FilePaths.getFullPath(fileName);
            File file = new File(fullPath);
            file.getParentFile().mkdirs();

            File tempFile = new File(FilePaths.getTempPath(fileName.replace(".json", "")));
            try (FileWriter writer = new FileWriter(tempFile)) {
                gson.toJson(object, writer);
            }

            if (file.exists()) {
                file.delete();
            }
            tempFile.renameTo(file);

            // Aggiorna il timestamp nella prima riga del file
            TimestampManager.updateTimestamp(fullPath);

        } catch (IOException e) {
            System.err.println("Errore di scrittura oggetto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public <T> T readObject(String fileName, Class<T> type) {
        return readObjectInternal(fileName, type);
    }

    @Override
    public <T> T readObject(String fileName, Type type) {
        return readObjectInternal(fileName, type);
    }

    private <T> T readObjectInternal(String fileName, Type type) {
        try {
            String fullPath = FilePaths.getFullPath(fileName);
            File file = new File(fullPath);
            if (!file.exists()) {
                return null;
            }

            // Legge il file saltando il timestamp
            String content = TimestampManager.readFileWithoutTimestamp(fullPath);
            if (content.trim().isEmpty()) {
                return null;
            }

            return gson.fromJson(content, type);

        } catch (IOException e) {
            System.err.println("Errore di lettura oggetto: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (JsonSyntaxException e) {
            System.err.println("Errore nel parsing JSON dell'oggetto: " + e.getMessage());
            writeObject(fileName, Collections.emptyList());
            return null;
        }
    }


    private void recreateFile() {
        try {
            File backupFile = new File(FilePaths.getBackupPath("Movement_corrupted"));
            if (MOVEMENT_FILE.exists()) {
                java.nio.file.Files.move(MOVEMENT_FILE.toPath(), backupFile.toPath());
            }

            try (FileWriter writer = new FileWriter(MOVEMENT_FILE)) {
                writer.write("[]");
            }
            // Aggiunge il timestamp al file ricreato
            TimestampManager.updateTimestamp(FilePaths.MOVEMENT_FILE);
        } catch (IOException ex) {
            System.err.println("Errore nella ricreazione del file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}