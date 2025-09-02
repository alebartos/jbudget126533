package it.unicam.cs.mpgc.jbudget126533.model;

import com.google.gson.*;
import java.lang.reflect.Type;

/**
 * Adapter personalizzato per la (de)serializzazione degli oggetti {@link ITag}
 * tramite la libreria Gson.
 * <p>
 * Questo adapter converte un tag in JSON e viceversa, gestendo correttamente
 * i riferimenti gerarchici (parent-child) senza generare loop infiniti.
 * </p>
 *
 * <h2>Esempio di serializzazione</h2>
 * Un oggetto {@link ITag} viene trasformato in un JSON come:
 * <pre>{@code
 * {
 *   "name": "Alimentari",
 *   "color": "#FF0000",
 *   "description": "Spese per il cibo",
 *   "parentName": "Spese"
 * }
 * }</pre>
 *
 * <h2>Esempio di deserializzazione</h2>
 * Quando viene letto un JSON di questo tipo, l'adapter:
 * <ul>
 *   <li>Controlla se il tag esiste già in {@link TagManager} (per evitare duplicati).</li>
 *   <li>Se esiste, restituisce il tag già registrato.</li>
 *   <li>Se non esiste, ne crea uno nuovo con i dati disponibili.</li>
 *   <li>Gestisce il collegamento al tag padre (se presente).</li>
 * </ul>
 */
public class ITagTypeAdapter implements JsonSerializer<ITag>, JsonDeserializer<ITag> {

    /**
     * Serializza un oggetto {@link ITag} in formato JSON.
     * <p>
     * Per evitare cicli infiniti nella gerarchia dei tag, viene salvato
     * solamente il nome del parent (se presente).
     * </p>
     *
     * @param tag       il tag da serializzare
     * @param typeOfSrc tipo della sorgente
     * @param context   contesto di serializzazione Gson
     * @return rappresentazione JSON del tag
     */
    @Override
    public JsonElement serialize(ITag tag, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", tag.getName());
        jsonObject.addProperty("color", tag.getColor());
        jsonObject.addProperty("description", tag.getDescription());

        // Serializza SOLO il nome del parent per evitare loop infiniti
        if (tag.getParent() != null) {
            jsonObject.addProperty("parentName", tag.getParent().getName());
        }

        return jsonObject;
    }

    /**
     * Deserializza un oggetto {@link ITag} da un JSON.
     * <p>
     * L'adapter cerca prima se il tag esiste già nel {@link TagManager}.
     * Se esiste, viene restituito quello. Altrimenti, viene creato un nuovo {@link Tag}.
     * </p>
     *
     * @param json     rappresentazione JSON del tag
     * @param typeOfT  tipo target da deserializzare
     * @param context  contesto di deserializzazione Gson
     * @return istanza di {@link ITag} corrispondente al JSON
     * @throws JsonParseException se il JSON non è valido o incompleto
     */
    @Override
    public ITag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        String tagName = jsonObject.get("name").getAsString();

        // Cerca se il tag esiste già nel TagManager
        ITag existingTag = TagManager.getTag(tagName);
        if (existingTag != null) {
            return existingTag; // Usa il tag esistente
        }

        // Se non esiste, crea un nuovo tag
        Tag tag = new Tag(tagName);

        if (jsonObject.has("color")) {
            tag.setColor(jsonObject.get("color").getAsString());
        }

        if (jsonObject.has("description")) {
            tag.setDescription(jsonObject.get("description").getAsString());
        }

        // Gestione del parent DOPO la creazione del tag
        if (jsonObject.has("parentName")) {
            String parentName = jsonObject.get("parentName").getAsString();
            ITag parent = TagManager.getTag(parentName);
            if (parent != null) {
                tag.setParent(parent);
            }
        }

        return tag;
    }
}
