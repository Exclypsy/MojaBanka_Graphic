package com.example.mojabanka_graficky.ui.admin;

import com.example.mojabanka_graficky.dao.AccountDao;
import com.example.mojabanka_graficky.model.Ucet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminDashboardController {
    @FXML private TableView<Ucet> accountsTable;

    private final AccountDao accountDao = new AccountDao();
    private final ObservableList<Ucet> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        reload();
    }

    private void reload() {
        try {
            data.setAll(accountDao.findAll());
            accountsTable.setItems(data);
        } catch (Exception e) { /* status bar */ }
    }

    @FXML
    private void onDelete() {
        Ucet sel = accountsTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try { accountDao.delete(sel.getId()); reload(); } catch (Exception ignored) {}
    }
}
