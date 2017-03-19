package com.zsf.flashextract.region.newregion.message;

import com.zsf.flashextract.region.newregion.field.Field;
import com.zsf.flashextract.region.newregion.tools.Color;

import java.util.List;

/**
 * Created by zsf on 2017/3/19.
 */
public class MessageSelectField {
    private List<Field> selectedFields;
    private List<Color> colors;
    private List<String> titles;
    private String[][] dataTables;

    public MessageSelectField(List<Field> selectedFields, List<Color> colors, List<String> titles, String[][] dataTables) {
        this.selectedFields = selectedFields;
        this.colors = colors;
        this.titles = titles;
        this.dataTables = dataTables;
    }

    public List<Field> getSelectedFields() {
        return selectedFields;
    }

    public List<Color> getColors() {
        return colors;
    }

    public List<String> getTitles() {
        return titles;
    }

    public String[][] getDataTables() {
        return dataTables;
    }
}
