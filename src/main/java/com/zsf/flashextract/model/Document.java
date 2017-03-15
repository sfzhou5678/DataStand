package com.zsf.flashextract.model;

import com.zsf.StringProcessor;
import com.zsf.flashextract.region.Region;
import com.zsf.flashextract.region.SelectedLineRegion;
import com.zsf.interpreter.expressions.regex.DynamicRegex;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.model.ExamplePair;
import com.zsf.interpreter.model.ExpressionGroup;
import com.zsf.interpreter.model.Match;
import com.zsf.interpreter.model.ResultMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zsf.interpreter.tool.StringTools.getCommonStr;
import static com.zsf.interpreter.tool.StringTools.getReversedStr;

/**
 * Created by zsf on 2017/3/13.
 */
public class Document {
    private String inputDocument;
    private List<Region> documentRegions = new ArrayList<Region>();
    private List<Regex> usefulRegex;
    private Map<Integer, List<Region>> colorfulRegions = new HashMap<Integer, List<Region>>();

    /**
     * 记录根据positiveExamples提取出的lineSelector
     */
    private List<Regex> lineSelector = new ArrayList<Regex>();
    /**
     * 当前使用的selector方案(可以选出selectedRegions)
     */
    private Regex curSelector = null;
    private List<SelectedLineRegion> selectedLineRegions = new ArrayList<SelectedLineRegion>();

    public Document(String inputDocument, List<Regex> usefulRegex) {
        this.usefulRegex = usefulRegex;
        setInputDocument(inputDocument);
    }

    public String getInputDocument() {
        return inputDocument;
    }

    public void setInputDocument(String inputDocument) {
        this.inputDocument = inputDocument;

        documentRegions = new ArrayList<Region>();
        String[] splitedLines = inputDocument.split("\n");
        for (String line : splitedLines) {
            documentRegions.add(new Region(null, 0, -1, line));
        }
    }

    /**
     * 外部调用此方法框选新的region，此方法判断之后分别分派给doSelectReion()或者doSelectRegionInLineRegions()进行操作
     *
     * @param color
     * @param lineIndex
     * @param beginPos
     * @param endPos
     * @param selectedText
     */
    public void selectRegion(int color, int lineIndex, int beginPos, int endPos, String selectedText) {
        SelectedLineRegion lineRegion = null;
        for (SelectedLineRegion someLineRegion : selectedLineRegions) {
            if (someLineRegion.getLineIndex() == lineIndex) {
                lineRegion = someLineRegion;
                break;
            }
        }
        if (lineRegion == null) {
            // 如果当前选中不在selectedLine之中,则认为是一次普通的select(即初始化选取)
            doSelectRegion(color, lineIndex, beginPos, endPos, selectedText);
        } else {
            doSelectRegionInLineRegions(lineRegion, color, lineIndex, beginPos, endPos, selectedText);
        }
    }

    /**
     * 在已经通过lineSelector选中的lineRegion中选择子region
     *
     * @param lineRegion
     * @param color
     * @param lineIndex
     * @param beginPos
     * @param endPos
     * @param selectedText
     */
    private void doSelectRegionInLineRegions(SelectedLineRegion lineRegion, int color, int lineIndex, int beginPos, int endPos, String selectedText) {
        // TODO: 2017/3/15 有必要的话可能要记录一下positive和negative？
        ExpressionGroup expressionGroup = null;
        if (lineRegion != null) {
            expressionGroup = lineRegion.selectChildRegion(color, selectedText);
        }
        if (expressionGroup != null) {
            for (SelectedLineRegion region : selectedLineRegions) {
                region.setColorfulRegionExpressions(color, expressionGroup);
            }
        }
    }

    /**
     * 在document全文中某个小region(非LineRegion)
     * @param color
     * @param lineIndex
     * @param beginPos
     * @param endPos
     * @param selectedText
     */
    private void doSelectRegion(int color, int lineIndex, int beginPos, int endPos, String selectedText) {
        List<Region> regions = colorfulRegions.get(color);
        // TODO: 2017/3/15 判断color是否合法(暂时跳过，当作不会遇到)

        if (regions == null) {
            regions = new ArrayList<Region>();
            colorfulRegions.put(color, regions);
        }
        // 这里加入的是带颜色的被选中的region，而不是普通region
        // FIXME: 2017/3/14 逻辑有点混乱
        regions.add(new SelectedLineRegion(documentRegions.get(lineIndex), beginPos, endPos, selectedText, color, lineIndex));
        // 直接将这一行设置为positiveLine
        addPositiveLineIndex(color, lineIndex);
    }

    private Map<Integer, List<Integer>> colorfulPositiveLineIndex = new HashMap<Integer, List<Integer>>();

    /**
     * 在doSelectRegion后调用，将当前选中行加入到positiveIndex中(默认一开始只有一种颜色)
     *
     * @param color
     * @param lineIndex
     */
    private void addPositiveLineIndex(int color, int lineIndex) {
        List<Integer> positiveLineIndex = colorfulPositiveLineIndex.get(color);
        if (positiveLineIndex == null) {
            positiveLineIndex = new ArrayList<Integer>();
            colorfulPositiveLineIndex.put(color, positiveLineIndex);
        }
        positiveLineIndex.add(lineIndex);
    }

    private Map<Integer, List<Integer>> colorfulNegativeLineIndex = new HashMap<Integer, List<Integer>>();

    private List<Integer> getNegativeLineIndex(int color) {
        generateNegativeLineIndex(color);
        return colorfulNegativeLineIndex.get(color);
    }

    private void generateNegativeLineIndex(int color) {
        List<Integer> positiveLineIndex = getPositiveLineIndex(color);
        List<Integer> negativeLineIndex = new ArrayList<Integer>();
        int max = 0;
        for (int index : positiveLineIndex) {
            max = Math.max(max, index);
        }
        for (int i = 0; i < max; i++) {
            if (!positiveLineIndex.contains(i)) {
                negativeLineIndex.add(i);
            }
        }
        colorfulNegativeLineIndex.put(color, negativeLineIndex);
    }

    /**
     * 在每次有新的input时就调用此方法，可以返回 各个pos上所有能够和input匹配的集合
     * 当generatePosition()需要时，直接根据match的pos(index)去查找使用，避免重复计算
     */
    private List<Match> buildStringMatches(String inputString) {
        List<Match> matches = new ArrayList<Match>();
        for (int i = 0; i < usefulRegex.size(); i++) {
            Regex regex = usefulRegex.get(i);
            List<Match> curMatcher = regex.doMatch(inputString);
            matches.addAll(curMatcher);
        }
        return matches;
    }

    /**
     * 现在假设所有的待提取数据都处于同一行，所以只有处理第一种颜色的时候会调用这个函数
     * <p>
     * 即由第一种颜色确定dataLines，后面的颜色都只在目标数据中运用FF
     *
     * @return
     */
    public List<Regex> getLineSelector(int color) {
        // FIXME: 2017/3/13 这里的代码还没有重构过，可能存在冗余
        List<Region> selectedRegion = colorfulRegions.get(color);
        addDynamicToken(selectedRegion);

        List<List<Regex>> startWithReges = new ArrayList<List<Regex>>();
        List<List<Regex>> endWithReges = new ArrayList<List<Regex>>();
        int curDeepth = 1;
        int maxDeepth = 3;
        for (Region region : selectedRegion) {
            List<Match> matches = buildStringMatches(region.getParentRegion().getText());
            startWithReges.add(buildStartWith(curDeepth, maxDeepth, matches, 0, new DynamicRegex("", "")));
            endWithReges.add(buildEndWith(curDeepth, maxDeepth, matches, region.getParentRegion().getText().length(), new DynamicRegex("", "")));
        }
        System.out.println("start with:");
        System.out.println(startWithReges.get(1));
        System.out.println("end with:");
        System.out.println(endWithReges);

        List<Regex> startWithLineSelector = deDuplication(startWithReges, true);
        List<Regex> endWithLineSelector = deDuplication(endWithReges, false);

        System.out.println(startWithLineSelector);
        System.out.println(endWithLineSelector);

        // 利用positive和negativeExamples对selectors进行筛选
        List<Regex> usefulLineSelector = new ArrayList<Regex>();
        usefulLineSelector.addAll(filterUsefulSelector(startWithLineSelector, documentRegions, getPositiveLineIndex(color), getNegativeLineIndex(color)));
        usefulLineSelector.addAll(filterUsefulSelector(endWithLineSelector, documentRegions, getPositiveLineIndex(color), getNegativeLineIndex(color)));

        this.lineSelector = usefulLineSelector;

        return usefulLineSelector;
    }

    private List<Regex> filterUsefulSelector(List<Regex> regices, List<Region> documentRegions,
                                             List<Integer> positiveLineIndex, List<Integer> negataiveLineIndex) {
        List<Regex> usefulLineSelector = new ArrayList<Regex>();

        for (Regex regex : regices) {
            boolean needAddIn = true;
            for (int index : positiveLineIndex) {
                Region region = documentRegions.get(index);
                if (!region.canMatch(regex)) {
                    needAddIn = false;
                    break;
                }
            }
            for (int index : negataiveLineIndex) {
                Region region = documentRegions.get(index);
                if (region.canMatch(regex)) {
                    needAddIn = false;
                    break;
                }
            }
            if (needAddIn) {
                usefulLineSelector.add(regex);
            }
        }
        return usefulLineSelector;
    }

    /**
     * 从当前新选择的区域出发，分别向左&向右匹配相同str作为dynamicToken添加到usefulRegex中
     *
     * @param targetLines
     */
    private void addDynamicToken(List<Region> targetLines) {
        // FIXME: 2017/3/13 不确定这个应该放在document还是FE里
        Region region = targetLines.get(0);

        // 左匹配
        String textBeforeSelected = region.getParentRegion().getText().substring(0, region.getBeginPos() + 1);
        String leftCommonStr = textBeforeSelected;
        System.out.println(textBeforeSelected);
        for (int i = 1; i < targetLines.size(); i++) {
            Region curRegion = targetLines.get(i);
            leftCommonStr = getCommonStr(getReversedStr(leftCommonStr),
                    getReversedStr(curRegion.getParentRegion().getText().substring(0, curRegion.getBeginPos() + 1)));
            leftCommonStr = getReversedStr(leftCommonStr);
            System.out.println("leftCommonStr:  " + leftCommonStr);
        }

        // 右匹配
        String textAfterSelected = region.getParentRegion().getText().substring(region.getEndPos());
        String rightCommonStr = textAfterSelected;
        System.out.println(textAfterSelected);
        for (int i = 1; i < targetLines.size(); i++) {
            Region curRegion = targetLines.get(i);
            rightCommonStr = getCommonStr(rightCommonStr,
                    curRegion.getParentRegion().getText().substring(curRegion.getEndPos()));
            System.out.println("rightCommonStr:  " + rightCommonStr);
        }

        Regex leftRegex = new DynamicRegex("DynamicTok(" + leftCommonStr + ")", leftCommonStr);
        Regex rightRegex = new DynamicRegex("DynamicTok(" + rightCommonStr + ")", rightCommonStr);

        usefulRegex.add(leftRegex);
        usefulRegex.add(rightRegex);
    }


    private List<Regex> deDuplication(List<List<Regex>> regexs, boolean isStartWith) {
        List<Regex> deDuplicatedList = new ArrayList<Regex>();

        List<Regex> baseRegexList = regexs.get(0);
        for (Regex baseRegex : baseRegexList) {
            boolean needAddIn = false;
            for (int j = 1; j < regexs.size(); j++) {
                List<Regex> regexList = regexs.get(j);

                for (Regex regex : regexList) {
                    if ((baseRegex.equals(regex))) {
                        needAddIn = true;
                        break;
                    }
                }
                if (!needAddIn) {
                    break;
                }
            }
            if (needAddIn) {
                if (isStartWith) {
                    baseRegex.setReg("^" + baseRegex.getReg());
                    baseRegex.setRegexName("startWith(" + baseRegex.getRegexName() + ")");
                } else {
                    baseRegex.setReg(baseRegex.getReg() + "$");
                    baseRegex.setRegexName("endWith(" + baseRegex.getRegexName() + ")");
                }
                deDuplicatedList.add(baseRegex);
            }
        }
        return deDuplicatedList;
    }

    /**
     * 获得符合所有examples的endWith语法
     *
     * @param curDeepth
     * @param maxDeepth
     * @param matches
     * @param endNode
     * @param lastRegex
     */
    private List<Regex> buildEndWith(int curDeepth, int maxDeepth,
                                     List<Match> matches, int endNode, Regex lastRegex) {
        if (curDeepth > maxDeepth) {
            return null;
        }
        String connector = "+";
        if (curDeepth == 1) {
            connector = "";
        }
        List<Regex> regexList = new ArrayList<Regex>();
        for (int i = matches.size() - 1; i >= 0; i--) {
            Match match = matches.get(i);
            if ((match.getMatchedIndex() + match.getMatchedString().length()) == endNode) {

                Regex curRegex = new DynamicRegex(match.getRegex().getRegexName() + connector + lastRegex.getRegexName(),
                        match.getRegex().getReg() + lastRegex.getReg());
                regexList.add(curRegex);
                List<Regex> curList = buildEndWith(curDeepth + 1, maxDeepth,
                        matches, match.getMatchedIndex(),
                        curRegex);
                if (curList != null) {
                    regexList.addAll(curList);
                }
            }
        }
        return regexList;
    }

    /**
     * 获得符合所有examples的startWith语法
     *
     * @param curDeepth
     * @param maxDeepth
     * @param matches
     * @param beginNode
     * @param lastRegex
     */
    private List<Regex> buildStartWith(int curDeepth, int maxDeepth,
                                       List<Match> matches, int beginNode, Regex lastRegex) {
        if (curDeepth > maxDeepth) {
            return null;
        }
        String connector = "+";
        if (curDeepth == 1) {
            connector = "";
        }
        List<Regex> regexList = new ArrayList<Regex>();

        for (int i = 0; i < matches.size(); i++) {
            Match match = matches.get(i);
            if (match.getMatchedIndex() == beginNode) {
                Regex curRegex = new DynamicRegex(lastRegex.getRegexName() + connector + match.getRegex().getRegexName(),
                        lastRegex.getReg() + match.getRegex().getReg());
                regexList.add(curRegex);

                List<Regex> curList = buildStartWith(curDeepth + 1, maxDeepth,
                        matches, match.getMatchedIndex() + match.getMatchedString().length(),
                        curRegex);
                if (curList != null) {
                    regexList.addAll(curList);
                }
            }
        }
        return regexList;
    }

    public List<Integer> getPositiveLineIndex(int color) {
        return colorfulPositiveLineIndex.get(color);
    }

    public List<Region> getDocumentRegions() {
        return documentRegions;
    }

    /**
     * 用某个selector(Regex)选出当前document中符合条件的region，并将他们标注为selectedRegions&返回给外部
     *
     * @param selector
     * @param color
     * @return
     */
    public List<SelectedLineRegion> selectRegionsBySelector(Regex selector, int color) {
        this.curSelector = selector;
        this.selectedLineRegions = new ArrayList<SelectedLineRegion>();
        for (int i = 0; i < documentRegions.size(); i++) {
            Region region = documentRegions.get(i);
            if (region.canMatch(selector)) {
                selectedLineRegions.add(new SelectedLineRegion(region.getParentRegion(),
                        region.getBeginPos(), region.getEndPos(), region.getText(), color, i));
            }
        }
        return selectedLineRegions;
    }

    /**
     * 产生LineSelector之后，自动在LineRegion中根据提供的例子产生childRegion
     *
     * 可以处理乱序选择的问题：比如蓝色先选择了一个，然后绿色在相应数据行内选择了两次
     * 那么标记所有绿色数据的同时也会标记蓝色数据
     */
    public void generateChildRegionsInLineRegions() {
        for (int color : colorfulRegions.keySet()) {
            List<Region> regions = colorfulRegions.get(color);
            List<ExamplePair> examplePairs = new ArrayList<ExamplePair>();
            for (Region region : regions) {
                examplePairs.add(new ExamplePair(region.getParentRegion().getText(), region.getText()));
            }

            StringProcessor stringProcessor = new StringProcessor();
            List<ResultMap> resultMaps = stringProcessor.generateExpressionsByExamples(examplePairs);
            ExpressionGroup expressionGroup = stringProcessor.selectTopKExps(resultMaps, 10);

            if (expressionGroup != null) {
                for (SelectedLineRegion lineRegion : selectedLineRegions) {
                    lineRegion.setColorfulRegionExpressions(color, expressionGroup);
                }
            }
        }
    }

    /**
     * 当某种color的个数大于等于2时，就可以产生lineRegion了(简化处理)
     *
     * @param color
     * @return
     */
    public boolean needGenerateLineReions(int color) {
        return colorfulRegions.get(color).size() >= 2 && curSelector == null;
    }
}
