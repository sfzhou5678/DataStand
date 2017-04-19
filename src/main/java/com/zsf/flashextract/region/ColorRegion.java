package com.zsf.flashextract.region;

import com.zsf.interpreter.StringProcessor;
import com.zsf.flashextract.tools.RegexCommomTools;
import com.zsf.flashextract.FlashExtract;
import com.zsf.flashextract.field.Field;
import com.zsf.flashextract.field.LineField;
import com.zsf.flashextract.field.PlainField;
import com.zsf.flashextract.tools.Color;
import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.NonTerminalExpression;
import com.zsf.interpreter.expressions.regex.DynamicRegex;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.model.*;
import com.zsf.interpreter.tool.ExpScoreComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 每种颜色对应一个ColorRegion 属于比较高层的容器
 * Created by hasee on 2017/3/16.
 */
public class ColorRegion {
    private Color color;
    private String regionTitle = "unnamed";
    private String parentDocument;

    private List<PlainField> fieldsByUser = new ArrayList<PlainField>();
    private List<PlainField> fieldsGenerated = new ArrayList<PlainField>();

    private List<Integer> positiveLineIndex = new ArrayList<Integer>();
    private List<Integer> negativeLineIndex = new ArrayList<Integer>();
    private List<Integer> needSelectLineIndex = new ArrayList<Integer>();

    private List<LineField> lineFields = new ArrayList<LineField>();

    private List<Regex> lineSelectors = new ArrayList<Regex>();
    private Regex curLineSelector = null;

    private ExpressionGroup expressionGroup;
    private Expression curExpression;
    /**
     * extraExpressions指的是第一步提取出plainField之后又进行了n次FF进一步处理数据
     * 每一次的FF对应的exp会按顺序加入到extraExps中
     * <p>
     * 只要保留原始的lineFields和lineSelector，然后依次用curExpression、extraExpression中的表达式interpret就可以得到最终的text
     * (这么做是因为现在表达式还没有嵌套的功能)
     */
    private List<Expression> extraExpressions = new ArrayList<Expression>();

    public ColorRegion(Color color, String parentDocument) {
        this.color = color;
        this.parentDocument = parentDocument;

        lineFields = new ArrayList<LineField>();
        String[] splitedLines = parentDocument.split("\n");
        for (int i = 0; i < splitedLines.length; i++) {
            String line = splitedLines[i];
            int beginPos = RegexCommomTools.indexNOf(parentDocument, "\n", i) + 1;
            int endPos = beginPos + line.length();
            lineFields.add(new LineField(null, Color.DEFAULT, line, beginPos, endPos));
        }
    }

    /**
     * 当选择新的fileds时(>=2,若3个+则说明之前的selector不够好)，设置positive，negative、产生lineSelector等
     *
     * @param lineIndex
     * @param beginPos
     * @param endPos
     * @param text
     */
    public void selectField(int lineIndex, int beginPos, int endPos, String text) {
        PlainField field = new PlainField(lineFields.get(lineIndex), color, text, beginPos, endPos);
        if (!fieldsByUser.contains(field)) {
            fieldsByUser.add(field);
            addPositiveLineIndex(lineIndex);
        }

        // 手动选择了1,2两行，然后lineSelector选出了1-5，7-n行(符合要求的)，
        // 此时手动选择第6行时应该把3-5行也标记为positive
        if (this.curLineSelector != null) {
            for (int line : needSelectLineIndex) {
                if (line > lineIndex) {
                    break;
                } else {
                    addPositiveLineIndex(line);
                }
            }
        }

        if (needGenerateLineSelectors()) {
            if (doGenerateLineSelectors()) {
                generateLineFieldsByCurSelector();

                generateExpressionGroup();
                generatePlainFieldsByCurExp();
            } else {
                // FIXME 需要产生lineSelector，但是没能成功产生，需要返回一个错误提示
                System.out.println("doGenerateLineSelectors()失败");
            }
        }
    }

    /**
     * 当选择和其他color同行数据时会调用这个方法，通过别人提供的selector产生plainFields
     * <p>
     * 和普通selectField()唯一的区别就在于 不做doGenerateLineSelectors()
     *
     * @param lineIndex
     * @param beginPos
     * @param endPos
     * @param text
     * @param selector
     */
    public void selectFieldByOuterSelector(int lineIndex, int beginPos, int endPos, String text, Regex selector) {
        this.curLineSelector = selector;

        PlainField field = new PlainField(lineFields.get(lineIndex), color, text,beginPos, endPos);
        if (!fieldsByUser.contains(field)) {
            fieldsByUser.add(field);
            addPositiveLineIndex(lineIndex);
        }

        generateLineFieldsByCurSelector();

        generateExpressionGroup();
        generatePlainFieldsByCurExp();
    }

    private boolean needGenerateLineSelectors() {
        // 当选中fields>=2时需要产生selector
        // 即使是已经有selector了，如果又选择了新的field，就可以认为之前的selector不够好，要重新生成
        return fieldsByUser.size() >= 2;
    }

    /**
     * 在selectField()时，如果判断需要产生LineSelectors就会调用此方法，产生LineSelecotr,排序结果后设置curSelector
     */
    private boolean doGenerateLineSelectors() {
        RegexCommomTools.addDynamicToken(fieldsByUser, FlashExtract.usefulRegex);

        List<List<Regex>> startWithReges = new ArrayList<List<Regex>>();
        List<List<Regex>> endWithReges = new ArrayList<List<Regex>>();
        int curDeepth = 1;
        int maxDeepth = 3;

        for (int index : positiveLineIndex) {
            List<Match> matches = RegexCommomTools.buildStringMatches(lineFields.get(index).getText(), FlashExtract.usefulRegex);
            startWithReges.add(RegexCommomTools.buildStartWith(curDeepth, maxDeepth, matches, 0, new DynamicRegex("", "")));
            endWithReges.add(RegexCommomTools.buildEndWith(curDeepth, maxDeepth, matches, lineFields.get(index).getText().length(), new DynamicRegex("", "")));
        }
        System.out.println("start with:");
        System.out.println(startWithReges);
        System.out.println("end with:");
        System.out.println(endWithReges);


        // 在deDuplication()中分别为startWith和endWith加上了^和$
        List<Regex> startWithLineSelector = RegexCommomTools.deDuplication(startWithReges, true);
        List<Regex> endWithLineSelector = RegexCommomTools.deDuplication(endWithReges, false);

//        System.out.println(startWithLineSelector);
//        System.out.println(endWithLineSelector);

        // 利用positive和negativeExamples对selectors进行筛选
        List<Regex> usefulLineSelector = new ArrayList<Regex>();
        usefulLineSelector.addAll(RegexCommomTools.filterUsefulSelector(startWithLineSelector, lineFields, positiveLineIndex, getNegativeLineIndex()));
        usefulLineSelector.addAll(RegexCommomTools.filterUsefulSelector(endWithLineSelector, lineFields, positiveLineIndex, getNegativeLineIndex()));

        // TODO: 2017/3/16 lineSelector的ranking

        if (usefulLineSelector.size() > 0) {
            this.lineSelectors = usefulLineSelector;
            System.out.println(lineSelectors);
            this.curLineSelector = lineSelectors.get(0);
            return true;
        } else {
            // 没有产生合适的selector，此次generate失败
            return false;
        }
    }

    private void addPositiveLineIndex(int lineIndex) {
        if (!positiveLineIndex.contains(lineIndex)) {
            positiveLineIndex.add(lineIndex);
        }
    }

    /**
     * 根据当前的selector产生fileds，会在getFileds()之前调用
     */
    private void generateLineFieldsByCurSelector() {
        needSelectLineIndex = new ArrayList<Integer>();
        if (curLineSelector == null) {
            // FIXME: 2017/3/16 需要一个优雅的解决方案
            return;
        }
        for (int i = 0; i < lineFields.size(); i++) {
            if (lineFields.get(i).canMatch(curLineSelector)) {
                needSelectLineIndex.add(i);
            }
        }
    }

    private void generateExpressionGroup() {
        // FIXME: 2017/3/27 加入多example协同作用
        List<ExamplePair> examplePairs = new ArrayList<ExamplePair>();
        for (Field field : fieldsByUser) {
            examplePairs.add(new ExamplePair(field.getParentField().getText(), field.getText()));
        }
        StringProcessor stringProcessor = new StringProcessor();
        List<ResultMap> resultMaps = stringProcessor.generateExpressionsByExamples(examplePairs);
        List<ExpressionGroup> expressionGroups = stringProcessor.selectTopKExps(resultMaps, 10);

        this.expressionGroup = expressionGroups.get(0);
        if (expressionGroup != null && expressionGroup.getExpressions().size() > 0) {
            List<String> strings = new ArrayList<String>();
            for (int index : needSelectLineIndex) {
                strings.add(lineFields.get(index).getText());
            }
            sortExpsAccordingScene(strings, expressionGroup);

            curExpression = expressionGroup.getExpressions().get(0);
            System.out.println(curExpression.score());
        }
    }

    /**
     * 结合所有exps应用到needSelectedLines上能选出结果的比例来更新权重并排序
     *
     * @param expressionGroup
     */
    public void sortExpsAccordingScene(List<String> strings, ExpressionGroup expressionGroup) {
        int totalLines = strings.size();
        for (Expression expression : expressionGroup.getExpressions()) {
            if (expression instanceof NonTerminalExpression) {
                int count = 0;
                for (String str : strings) {
                    if (((NonTerminalExpression) expression).interpret(str) != null) {
                        count++;
                    }
                }
                expression.setSceneWeight((double) count / totalLines);
            }
        }
        Collections.sort(expressionGroup.getExpressions(), new ExpScoreComparator());
    }


    /**
     * 只负责产生普通的plainFields,然后调用updateFieldByExtraExps更新plainFields的editedText
     */
    private void generatePlainFieldsByCurExp() {
        if (curExpression != null) {
            this.fieldsGenerated = new ArrayList<PlainField>();
            for (int lineIndex : needSelectLineIndex) {
                // TODO: 2017/3/16 去重复？
                this.fieldsGenerated.addAll(
                        lineFields.get(lineIndex).selectChildFieldByExp(curExpression, color));
            }
        }
    }

    public int calculateLineIndex(int beginPos, int endPos) {
        String textBeforeSelect = parentDocument.substring(0, beginPos);
        int count = 0;
        int index = 0;
        while (true) {
            index = textBeforeSelect.indexOf("\n", index + 1);
            if (index > 0) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    public String getRegionTitle() {
        return regionTitle;
    }

    public void setRegionTitle(String regionTitle) {
        this.regionTitle = regionTitle;
    }

    public List<PlainField> getFieldsGenerated() {
        return fieldsGenerated;
    }

    public Color getColor() {
        return color;
    }

    public String getParentDocument() {
        return parentDocument;
    }

    public List<Regex> getLineSelectors() {
        return lineSelectors;
    }

    public Regex getCurLineSelector() {
        return curLineSelector;
    }

    public List<Integer> getNeedSelectLineIndex() {
        return needSelectLineIndex;
    }

    public List<PlainField> getFieldsByUser() {
        return fieldsByUser;
    }

    public List<Integer> getNegativeLineIndex() {
        negativeLineIndex = new ArrayList<Integer>();
        int max = 0;
        for (int index : positiveLineIndex) {
            max = Math.max(max, index);
        }
        for (int i = 0; i < max; i++) {
            if (!positiveLineIndex.contains(i)) {
                negativeLineIndex.add(i);
            }
        }
        return negativeLineIndex;
    }

    /**
     * 将新的expression应用到plainField上(只返回预览效果，不保存结果)
     */
    public List<String> previewExp(List<ExamplePartition> partitions) {
        if (fieldsGenerated != null && fieldsGenerated.size() > 0) {
            List<String> previewDatas = new ArrayList<String>();
            for (PlainField field : fieldsGenerated) {
                // 下面这一块加入了partition，暂时未发现问题
                int partitionIndex = StringProcessor.lookupPartitionIndex(field.getEditedText(), partitions);
                ExamplePartition partition = partitions.get(partitionIndex);

                ExpressionGroup topNExpression = StringProcessor.getTopNExpressions(partition, field.getEditedText(), 5);
                Expression expression=topNExpression.getExpressions().get(0);

                if (expression instanceof NonTerminalExpression) {
                    String txt = ((NonTerminalExpression) expression).interpret(field.getEditedText());
                    if (txt != null) {
                        previewDatas.add(txt);
                        field.setUnConfirmedExtraExp(expression);
                    }
                }
            }
            return previewDatas;
        }
        return null;
    }


    /**
     * 添加通过FF产生的额外的表达式
     */
    public void confirmExtraExp() {
        for (PlainField field : fieldsGenerated) {
            field.confirmExtraExp();
        }
    }
}
