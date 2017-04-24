package com.zsf.flashextract;

import com.zsf.common.UsefulRegex;
import com.zsf.flashextract.field.PlainField;
import com.zsf.flashextract.region.ColorRegion;
import com.zsf.flashextract.field.Field;
import com.zsf.flashextract.message.MessageContainer;
import com.zsf.flashextract.tools.Color;
import com.zsf.flashextract.tools.FieldComparator;
import com.zsf.interpreter.StringProcessor;
import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.model.ExamplePair;
import com.zsf.interpreter.model.ExamplePartition;
import com.zsf.interpreter.model.ExpressionGroup;
import com.zsf.interpreter.model.ResultMap;

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
        int lineIndex = colorRegion.calculateLineIndex(beginPos, endPos);
        for (ColorRegion region : colorRegionMap.values()) {
            if (region != colorRegion) {
                // 判断是否已经产生过selector了
                if (region.getNeedSelectLineIndex().contains(lineIndex)) {
                    // 判断当前区域是否在其他color的lineSelector之内
                    colorRegion.selectFieldByOuterSelector(lineIndex, beginPos, endPos, text, region.getCurLineSelector());
                    break;
                }
            }
        }
        colorRegion.selectField(lineIndex, beginPos, endPos, text);
    }

    public MessageContainer showSelectedFields() {
        List<PlainField> fieldList = new ArrayList<PlainField>();
        int maxRow = 0;
        for (ColorRegion colorRegion : colorRegionMap.values()) {
            for (PlainField field : colorRegion.getFieldsGenerated()) {
                // FIXME: 2017/4/10 maxRow是不是应该上移到for外面？
                maxRow = Math.max(maxRow, colorRegion.getFieldsGenerated().size());
                if (!fieldList.contains(field)) {
                    fieldList.add(field);
                }
            }
            for (PlainField field : colorRegion.getFieldsByUser()) {
                // FIXME: 2017/4/10 maxRow是不是应该上移到for外面？
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
        for (PlainField field : fieldList) {
            if (!colors.contains(field.getColor())) {
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
        for (PlainField field : fieldList) {
            int colIndex = colors.indexOf(field.getColor());

            if (colIndex <= lastColIndex && needAddRow) {
                curRow++;
            }
            lastColIndex = colIndex;

            // 为了应对FF系统，这里特地将
            // tableDatas[curRow][colIndex] = field.getText()
            // 改为
            // tableDatas[curRow][colIndex] = field.getEditedText()
            tableDatas[curRow][colIndex] = field.getEditedText();
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
            messageContainer.modifyTitle(color, title);
        }
    }

    public ExpressionGroup sortExpsAccSceneByColor(Color color, ExpressionGroup expressionGroup, int k) {
        ColorRegion colorRegion = colorRegionMap.get(color);
        if (colorRegion != null) {
            List<PlainField> fieldsGenerated = colorRegion.getFieldsGenerated();
            List<String> strings = new ArrayList<String>();
            for (Field field : fieldsGenerated) {
                strings.add(field.getText());
            }
            colorRegion.sortExpsAccordingScene(strings, expressionGroup);
            expressionGroup = expressionGroup.selecTopK(k);
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


    /**
     * 在CR上用ff产生的exp进行预览
     * @param color
     * @return
     */
    public List<String> previewExpOnCR(Color color, List<ExamplePair> examplePairs) {
        ColorRegion colorRegion = colorRegionMap.get(color);
        if (colorRegion != null) {
            StringProcessor stringProcessor = new StringProcessor();
            List<ResultMap> resultMaps = stringProcessor.generateExpressionsByExamples(examplePairs);
            List<ExpressionGroup> expressionGroups = stringProcessor.selectTopKExps(resultMaps, 10);
            List<ExamplePartition> partitions = stringProcessor.generatePartitions(expressionGroups, examplePairs);

            return colorRegion.previewExp(partitions);
        } else {
            return null;
        }
    }

    /**
     * 外部确认通过FF产生的某个表达式生效，就把这个EXP加入到对应的colorRegion中去
     *
     * @param color
     */
    public void confirmExtraExp(Color color) {
        ColorRegion colorRegion = colorRegionMap.get(color);
        if (colorRegion != null) {
            colorRegion.confirmExtraExp();
        }
    }
}
