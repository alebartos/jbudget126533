package it.unicam.cs.mpgc.jbudget126533.util;

import it.unicam.cs.mpgc.jbudget126533.model.ITag;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class FormValidator {

    public static boolean validateTags(List<ITag> tags, Consumer<String> errorHandler) {
        if (tags == null || tags.isEmpty()) {
            errorHandler.accept("Seleziona almeno un tag!");
            return false;
        }
        return true;
    }

    public static boolean validateStartDate(LocalDate startDate, Consumer<String> errorHandler) {
        if (startDate == null) {
            errorHandler.accept("Seleziona una data di inizio!");
            return false;
        }
        return true;
    }
}
