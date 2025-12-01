package models;
public interface Model {
    public void getAll();
    public void getId(Integer id);
    public void updateRowId(Integer id, String[] newValues);
    public void deleteRowId(Integer id);
    public void insertNewRow(Integer id, String[] values);    
}