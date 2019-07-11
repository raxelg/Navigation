package expandListView.Classes;

import java.util.ArrayList;

public class ExpandListGroup {

    private String Name;
    private ArrayList<ExpandListChild> Items;

    public ExpandListGroup(String Name) {
        this.Name = Name;
    }

    public String getName() {
        return Name;
    }

    public ArrayList<ExpandListChild> getItems() {
        return Items;
    }

    public void setItems(ArrayList<ExpandListChild> Items) {
        this.Items = Items;
    }

}
