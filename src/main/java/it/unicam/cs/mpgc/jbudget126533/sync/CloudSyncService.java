package it.unicam.cs.mpgc.jbudget126533.sync;

import java.time.LocalDateTime;

/**
 * Interfaccia per servizi di sincronizzazione cloud.
 */
public interface CloudSyncService {

    /**
     * Autentica l'utente sul servizio cloud.
     */
    boolean authenticate(String username, String password);

    /**
     * Carica il pacchetto di sincronizzazione sul cloud.
     */
    boolean uploadSyncPackage(SyncPackage syncPackage);

    /**
     * Scarica il pacchetto di sincronizzazione dal cloud.
     */
    SyncPackage downloadSyncPackage();

    /**
     * Ottiene l'ultimo timestamp di sincronizzazione.
     */
    LocalDateTime getLastSyncTime();

    /**
     * Verifica la connessione al servizio cloud.
     */
    boolean checkConnection();
}
