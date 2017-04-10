package com.zsf.flashextract;

import com.zsf.common.UsefulRegex;
import com.zsf.flashextract.region.ColorRegion;
import com.zsf.flashextract.field.Field;
import com.zsf.flashextract.message.MessageContainer;
import com.zsf.flashextract.tools.Color;
import com.zsf.flashextract.tools.FieldComparator;
import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.model.ExpressionGroup;

import java.util.*;

/**
 * 最高层的FE容器，包含输入文本document和所有ColorReiongs
 * Created by hasee on 2017/3/16.
 */
public class FlashExtract {
    private String document;
    private HashMap<Color, ColorRegion> colorRegionMap = new HashMap<Color, ColorRegion>();

    public static List<Regex> usefulRegex = UsefulRegex.getUsefulRegex();
    private MessageContainer messageContainer;

    public FlashExtract(String document) {
        this.document = document;
    }

    public void selectField(Color color, int beginPos, int endPos, String text) {
        ColorRegion colorRegion = colorRegionMap.get(color);
        if (colorRegion == null) {
            colorRegion = new ColorRegion(color, document);
            colorRegionMap.put(color, colorRegion);
        }
        // TODO: 2017/3/16 判断当前区域是否在其他color的lineSelector之内！！
        // TODO: 2017/3/17 判断是否已经产生过selector了
        int lineIndex = colorRegion.calculateLineIndex(beginPos, endPos);
        for (ColorRegion region : colorRegionMap.values()) {
            if (region != colorRegion) {
                // FIXME: 2017/3/27 如果补充选择positiveExample 现在会变成重复选择多次(带来n行NULL数据)！
                if (region.getNeedSelectLineIndex().contains(lineIndex)) {
                    colorRegion.selectFieldByOuterSelector(lineIndex, beginPos, endPos, text, region.getCurLineSelector());
                    break;
                }
            }
        }
        colorRegion.selectField(lineIndex, beginPos, endPos, text);
    }

    public MessageContainer showSelectedFields() {
        List<Field> fieldList = new ArrayList<Field>();
        int maxRow = 0;
        for (ColorRegion colorRegion : colorRegionMap.values()) {
            for (Field field : colorRegion.getFieldsGenerated()) {
                maxRow = Math.max(maxRow, colorRegion.getFieldsGenerated().size());
                if (!fieldList.contains(field)) {
                    fieldList.add(field);
                }
            }
            for (Field field : colorRegion.getFieldsByUser()) {
                maxRow = Math.max(maxRow, colorRegion.getFieldsByUser().size());
                if (!fieldList.contains(field)) {
                    fieldList.add(field);
                }
            }
        }
        // 按照beginPos从小到大sort
        Collections.sort(fieldList, new FieldComparator());

        // 产生color和title
        List<Color> colors = new ArrayList<Color>();
        List<String> titles = new ArrayList<String>();
        for (Field field : fieldList) {
            if (colors.contains(field.getColor())) {
                break;
            } else {
                colors.add(field.getColor());
                titles.add(getRegioinTitle(field.getColor()));
            }
        }

        // 产生tableDatas
        String[][] tableDatas = new String[maxRow][colors.size()];
        for (int i = 0; i < maxRow; i++) {
            for (int j = 0; j < colors.size(); j++) {
                tableDatas[i][j] = "NULL";
            }
        }
        int curRow = 0;
        int lastColIndex = -1;
        boolean needAddRow = false;
        for (Field field : fieldList) {
            int colIndex = colors.indexOf(field.getColor());

            if (colIndex <= lastColIndex && needAddRow) {
                curRow++;
            }
            lastColIndex = colIndex;

            tableDatas[curRow][colIndex] = field.getText();
            if ((colIndex == colors.size() - 1)) {
                curRow++;
                needAddRow = false;
            } else {
                needAddRow = true;
            }
        }
        messageContainer = new MessageContainer(fieldList, colors, titles, tableDatas);
        return messageContainer;
    }

    public String getRegioinTitle(Color color) {
        ColorRegion colorRegion = colorRegionMap.get(color);
        if (colorRegion != null) {
            return colorRegion.getRegionTitle();
        } else {
            return "";
        }
    }

    public void setRegionTitle(Color color, String title) {
        ColorRegion colorRegion = colorRegionMap.get(color);
        if (colorRegion != null) {
            colorRegion.setRegionTitle(title);
            messageContainer.modifyTitle(color,title);
        }
    }

    public ExpressionGroup sortExpsAccSceneByColor(Color color, ExpressionGroup expressionGroup, int k) {
        ColorRegion colorRegion=colorRegionMap.get(color);
        if (colorRegion!=null){
            List<Field> fieldsGenerated=colorRegion.getFieldsGenerated();
            List<String> strings=new ArrayList<String>();
            for (Field field:fieldsGenerated){
                strings.add(field.getText());
            }
            colorRegion.sortExpsAccordingScene(strings,expressionGroup);
            expressionGroup=expressionGroup.selecTopK(k);
        }
        return expressionGroup;
    }

    public List<String> getDatasByColor(Color color) {
        return messageContainer.getDatasByColor(color);
    }

    public MessageContainer getMessageContainer() {
        return messageContainer;
    }

    public void setMessageContainer(MessageContainer messageContainer) {
        this.messageContainer = messageContainer;
    }


    public List<String> extraFFByColor(Color color, Expression expression) {
        ColorRegion colorRegion = colorRegionMap.get(color);
        if (colorRegion != null) {
            return colorRegion.extraFF(expression);
        }else {
            return null;
        }
    }
}
