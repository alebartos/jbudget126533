package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.ITag;
import it.unicam.cs.mpgc.jbudget126533.model.Tag;
import it.unicam.cs.mpgc.jbudget126533.model.TagManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestisce la gestione dei tag nell'interfaccia utente.
 * <p>
 * Le principali funzionalità includono:
 * <ul>
 *     <li>Creazione, selezione e rimozione dei tag</li>
 *     <li>Gestione della gerarchia dei tag tramite TreeView</li>
 *     <li>Aggiornamento dei tag disponibili per transazioni, budget, transazioni programmate e ammortamenti</li>
 *     <li>Importazione ed esportazione dei tag</li>
 * </ul>
 */
public class TagHandler {

    private final ListView<ITag> availableTagsListView;
    private final ListView<ITag> selectedTagsListView;
    private final TextField newTagTextField;
    private final TreeView<ITag> tagHierarchyTreeView;
    private final ListView<ITag> transactionTagsListView;
    private final ListView<ITag> budgetTagsListView;
    private final ListView<ITag> scheduledTagsListView;
    private final ListView<ITag> amortTags;

    private final Map<String, ITag> availableTags = new HashMap<>();

    /**
     * Costruttore.
     *
     * @param availableTagsListView      ListView dei tag disponibili
     * @param selectedTagsListView       ListView dei tag selezionati
     * @param newTagTextField            TextField per creare nuovi tag
     * @param tagHierarchyTreeView       TreeView della gerarchia dei tag
     * @param transactionTagsListView    ListView dei tag disponibili per transazioni
     * @param budgetTagsListView         ListView dei tag disponibili per budget
     * @param scheduledTagsListView      ListView dei tag disponibili per transazioni programmate
     * @param amortTags                  ListView dei tag disponibili per ammortamenti
     */
    public TagHandler(ListView<ITag> availableTagsListView, ListView<ITag> selectedTagsListView,
                      TextField newTagTextField, TreeView<ITag> tagHierarchyTreeView,
                      ListView<ITag> transactionTagsListView, ListView<ITag> budgetTagsListView,
                      ListView<ITag> scheduledTagsListView, ListView<ITag> amortTags) {
        this.availableTagsListView = availableTagsListView;
        this.selectedTagsListView = selectedTagsListView;
        this.newTagTextField = newTagTextField;
        this.tagHierarchyTreeView = tagHierarchyTreeView;
        this.transactionTagsListView = transactionTagsListView;
        this.budgetTagsListView = budgetTagsListView;
        this.scheduledTagsListView = scheduledTagsListView;
        this.amortTags = amortTags;

        initializeAvailableTags();
    }

    /**
     * Carica tutti i tag disponibili dal TagManager.
     */
    private void initializeAvailableTags() {
        availableTags.putAll(TagManager.getAllTags());
    }

    /**
     * Inizializza la gestione dei tag.
     * <p>
     * Configura le ListView e la TreeView, aggiorna i tag selezionati e sincronizza
     * i tag disponibili per transazioni, budget, schedulazioni e ammortamenti.
     */
    public void initializeTagManagement() {
        TagManager.loadAllTags();
        availableTags.clear();
        availableTags.putAll(TagManager.getAllTags());
        TagManager.loadSelectedTags();

        if (availableTagsListView != null) {
            availableTagsListView.setItems(FXCollections.observableArrayList(availableTags.values()));
            availableTagsListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(ITag tag, boolean empty) {
                    super.updateItem(tag, empty);
                    setText(empty ? null : tag.getFullPath());
                }
            });
        }

        if (selectedTagsListView != null) {
            ObservableList<ITag> selectedTags = FXCollections.observableArrayList(TagManager.getSelectedTagsList());
            selectedTagsListView.setItems(selectedTags);
            selectedTagsListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(ITag tag, boolean empty) {
                    super.updateItem(tag, empty);
                    setText(empty ? null : tag.getName());
                }
            });
        }

        if (tagHierarchyTreeView != null) {
            TreeItem<ITag> root = new TreeItem<>(new Tag("Tutte le categorie"));
            buildTagTree(root, availableTags.values().stream()
                    .filter(tag -> tag.getParent() == null)
                    .collect(Collectors.toList()));
            tagHierarchyTreeView.setRoot(root);
            tagHierarchyTreeView.setShowRoot(true);
            expandAllTreeNodes(root);
        }

        updateAvailableTransactionTags();
    }

    /**
     * Espande ricorsivamente tutti i nodi di una TreeView.
     *
     * @param item nodo padre
     */
    private void expandAllTreeNodes(TreeItem<ITag> item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
            for (TreeItem<ITag> child : item.getChildren()) {
                expandAllTreeNodes(child);
            }
        }
    }

    /**
     * Crea un nuovo tag.
     *
     * @param event evento associato all'azione
     */
    public void createNewTag(ActionEvent event) {
        String tagName = newTagTextField.getText().trim();
        if (!tagName.isEmpty()) {
            if (availableTags.containsKey(tagName)) {
                showAlert(Alert.AlertType.WARNING, "Attenzione", "Il tag esiste già!");
                return;
            }

            ITag parent = null;
            TreeItem<ITag> selectedTreeItem = tagHierarchyTreeView.getSelectionModel().getSelectedItem();
            if (selectedTreeItem != null && !selectedTreeItem.getValue().getName().equals("Tutte le categorie")) {
                parent = selectedTreeItem.getValue();
            }

            ITag newTag = TagManager.createTag(tagName, parent);
            availableTags.put(tagName, newTag);

            newTagTextField.clear();
            initializeTagManagement();

            showAlert(Alert.AlertType.INFORMATION, "Successo", "Tag creato con successo!");
        }
    }

    /**
     * Rimuove il tag selezionato dalla lista dei tag selezionati.
     *
     * @param event evento associato all'azione
     */
    public void removeSelectedTag(ActionEvent event) {
        ITag selected = selectedTagsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedTagsListView.getItems().remove(selected);
            TagManager.deselectTag(selected.getName());
            updateAvailableTransactionTags();
        }
    }

    /**
     * Aggiunge un tag dai tag disponibili a quelli selezionati.
     *
     * @param event evento associato all'azione
     */
    public void addSelectedTag(ActionEvent event) {
        ITag selected = availableTagsListView.getSelectionModel().getSelectedItem();
        if (selected != null && !selectedTagsListView.getItems().contains(selected)) {
            selectedTagsListView.getItems().add(selected);
            TagManager.selectTag(selected.getName());
            updateAvailableTransactionTags();
        }
    }

    /**
     * Elimina il tag selezionato dai tag disponibili.
     * <p>
     * Nota: la gestione completa della rimozione in transazioni non è implementata.
     *
     * @param event evento associato all'azione
     */
    public void deleteSelectedTag(ActionEvent event) {
        ITag selected = availableTagsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showAlert(Alert.AlertType.WARNING, "Attenzione",
                    "Funzionalità non implementata completamente");
        }
    }

    /**
     * Ricarica tutti i tag dal TagManager.
     *
     * @param event evento associato all'azione
     */
    public void refreshAllTags(ActionEvent event) {
        TagManager.loadAllTags();
        availableTags.clear();
        availableTags.putAll(TagManager.getAllTags());
        initializeTagManagement();
        showAlert(Alert.AlertType.INFORMATION, "Aggiornamento", "Tutti i tag sono stati ricaricati!");
    }

    /**
     * Mostra un messaggio di esportazione dei tag.
     *
     * @param event evento associato all'azione
     */
    public void exportTags(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Esportazione",
                "I tag sono stati salvati automaticamente in: " + TagManager.ALL_TAGS_FILE_PATH);
    }

    /**
     * Importa i tag da file.
     *
     * @param event evento associato all'azione
     */
    public void importTags(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Importa Tag");
        alert.setHeaderText("Importare tag da file?");
        alert.setContentText("I tag esistenti verranno sostituiti con quelli dal file.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TagManager.loadAllTags();
            refreshAllTags(null);
            showAlert(Alert.AlertType.INFORMATION, "Importazione",
                    "Tag importati con successo da: " + TagManager.ALL_TAGS_FILE_PATH);
        }
    }

    /**
     * Aggiorna le ListView dei tag disponibili per transazioni, budget, schedulazioni e ammortamenti
     * basandosi sui tag selezionati.
     */
    public void updateAvailableTransactionTags() {
        if (selectedTagsListView != null) {
            ObservableList<ITag> availableTags = FXCollections.observableArrayList(selectedTagsListView.getItems());

            if (transactionTagsListView != null) transactionTagsListView.setItems(availableTags);
            if (amortTags != null) amortTags.setItems(availableTags);
            if (scheduledTagsListView != null) scheduledTagsListView.setItems(availableTags);
            if (budgetTagsListView != null) budgetTagsListView.setItems(availableTags);
        }
    }

    /**
     * Costruisce ricorsivamente l'albero dei tag nella TreeView.
     *
     * @param parent nodo padre
     * @param tags   lista dei tag figli
     */
    private void buildTagTree(TreeItem<ITag> parent, List<ITag> tags) {
        for (ITag tag : tags) {
            TreeItem<ITag> item = new TreeItem<>(tag);
            parent.getChildren().add(item);
            if (tag.hasChildren()) {
                buildTagTree(item, new ArrayList<>(tag.getChildren()));
            }
        }
    }

    /**
     * Mostra un alert grafico.
     *
     * @param type    tipo di alert
     * @param title   titolo dell'alert
     * @param message messaggio dell'alert
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
