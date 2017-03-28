package com.zsf.flashextract.message;

import com.zsf.flashextract.field.Field;
import com.zsf.flashextract.tools.Color;

import java.util.List;

/**
 * 和前端通信用的Message容器
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
