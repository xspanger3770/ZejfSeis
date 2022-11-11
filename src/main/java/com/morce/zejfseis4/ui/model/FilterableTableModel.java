package com.morce.zejfseis4.ui.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public abstract class FilterableTableModel<E> extends AbstractTableModel {

    private static final long serialVersionUID = 1727941556193013022L;
	private final List<E> data;
    private final List<E> filteredData;

    public FilterableTableModel(List<E> data){
        this.data = data;
        this.filteredData = new ArrayList<E>(data);
        applyFilter();
    }

    public final void applyFilter(){
        this.filteredData.clear();
        this.filteredData.addAll(this.data.stream().filter(this::accept).toList());
        super.fireTableDataChanged();
    }

    public boolean accept(E entity){
        return true;
    }

    public void dataUpdated() {

    }

    @Override
    public int getRowCount() {
        return filteredData.size();
    }

    public void updateRow(E entity) {
        int rowIndex = filteredData.indexOf(entity);
        fireTableRowsUpdated(rowIndex, rowIndex);
        dataUpdated();
    }

    public void deleteRow(int rowIndex) {
        E entity = filteredData.get(rowIndex);
        filteredData.remove(entity);
        data.remove(entity);
        fireTableRowsDeleted(rowIndex, rowIndex);
        dataUpdated();
    }

    public void addRow(E entity) {
        int newRowIndex = filteredData.size();
        data.add(entity);
        filteredData.add(entity);
        fireTableRowsInserted(newRowIndex, newRowIndex);
        dataUpdated();
    }

    public E getEntity(int rowIndex) {
        return filteredData.get(rowIndex);
    }

    public List<E> getFilteredData() {
        return filteredData;
    }

    public List<E> getData() {
        return data;
    }
}
