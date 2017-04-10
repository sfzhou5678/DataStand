package com.zsf.flashextract.message;

import com.zsf.flashextract.field.Field;
import com.zsf.flashextract.tools.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * 与前段通信用的Message对象
 * Created by zsf on 2017/3/19.
 */
public class MessageContainer {
    private List<Field> selectedFields;
    private List<Color> colors;
    private List<String> titles;
    private String[][] dataTables;

    public MessageContainer(List<Field> selectedFields, List<Color> colors, List<String> titles, String[][] dataTables) {
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

    public void modifyTitle(Color color, String title) {
        int index=colors.indexOf(color);
        if (index>=0){
            titles.set(index,title);
        }
    }

    public  List<String> getDatasByColor(Color color) {
        int index=colors.indexOf(color);
        if (index>=0){
            List<String> datas=new ArrayList<String>();
            for (int i=0;i<dataTables.length;i++){
                datas.add(dataTables[i][index]);
            }
            return datas;
        }else {
            return null;
        }
    }
}
