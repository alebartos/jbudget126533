package it.unicam.cs.mpgc.jbudget126533.view;

import it.unicam.cs.mpgc.jbudget126533.controller.TagHandler;
import it.unicam.cs.mpgc.jbudget126533.model.ITag;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class TagsTab implements Initializable {

    @FXML private ListView<ITag> availableTagsListView;
    @FXML private ListView<ITag> selectedTagsListView;
    @FXML private TextField newTagTextField;
    @FXML private TreeView<ITag> tagHierarchyTreeView;

    // sincronia con le altre tab che usano tag selezionati
    @FXML private ListView<ITag> transactionTagsListView; // può essere null in questo FXML
    @FXML private ListView<ITag> budgetTagsListView;      // può essere null in questo FXML
    @FXML private ListView<ITag> scheduledTagsListView;   // può essere null in questo FXML
    @FXML private ListView<ITag> amortTags;               // può essere null in questo FXML

    private TagHandler handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        handler = new TagHandler(availableTagsListView, selectedTagsListView, newTagTextField,
                tagHierarchyTreeView, null, null, null, null);
        handler.initializeTagManagement();

        // Sovrascrive gli items “locali” con la lista condivisa
        selectedTagsListView.setItems(ApplicationContext.selectedTags());
    }


    @FXML
    public void addSelectedTag(ActionEvent e) {
        ITag tag = availableTagsListView.getSelectionModel().getSelectedItem();
        ApplicationContext.selectTag(tag);
    }

    @FXML
    public void removeSelectedTag(ActionEvent e) {
        ITag tag = selectedTagsListView.getSelectionModel().getSelectedItem();
        ApplicationContext.deselectTag(tag);
    }

    @FXML public void createNewTag(javafx.event.ActionEvent e)      { handler.createNewTag(e); }
    @FXML public void refreshAllTags(javafx.event.ActionEvent e)    { handler.refreshAllTags(e); }
    @FXML public void exportTags(javafx.event.ActionEvent e)        { handler.exportTags(e); }
    @FXML public void importTags(javafx.event.ActionEvent e)        { handler.importTags(e); }
    @FXML public void deleteSelectedTag(javafx.event.ActionEvent e) { handler.deleteSelectedTag(e); }
}
