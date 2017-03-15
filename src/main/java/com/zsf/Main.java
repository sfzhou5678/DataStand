package com.zsf;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.NonTerminalExpression;
import com.zsf.interpreter.expressions.RegExpression;
import com.zsf.interpreter.expressions.linking.DeprecatedConcatenateExpression;
import com.zsf.interpreter.expressions.pos.PosExpression;
import com.zsf.interpreter.expressions.regex.EpicRegex;
import com.zsf.interpreter.expressions.regex.NormalRegex;
import com.zsf.interpreter.expressions.regex.RareRegex;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.expressions.string.ConstStrExpression;
import com.zsf.interpreter.expressions.string.SubString2Expression;
import com.zsf.interpreter.expressions.string.SubStringExpression;
import com.zsf.interpreter.model.*;
import com.zsf.interpreter.tool.ExpressionComparator;
import com.zsf.interpreter.tool.RunTimeMeasurer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class Main {

    /**
     * 根据I->O的examples，利用generateStr()+generatePatrition()...得到能够正确处理I->0转换的表达式
     * 整个过程类似中缀表达式求值：中缀表达式->后缀表达式->求值
     * 对应本程序的inputString->[★根据examples得到的expression★]->outputString
     * <p>
     * 难点：
     * 1. 如何找到能够正确映射I->0的表达式集合？
     * 2. 如何给这些表达式排序找出最优解？
     * <p>
     * generateExpressionByEaxmples求得expression之后返回这个表达式，之后的所有I利用这个E来求得O即可
     */
    private static List<ExpressionGroup> generateExpressionsByExamples(List<ExamplePair> examplePairs) {
        List<ExpressionGroup> expressionGroups = new ArrayList<ExpressionGroup>();
        for (ExamplePair pair : examplePairs) {
            String input = pair.getInputString();
            String output = pair.getOutputString();

            ExpressionGroup expressionGroup = generateStr(input, output,examplePairs);
            System.out.println(String.format("Input=%s  Output=%s", input, output));
            System.out.println(expressionGroup.size());
            for (Expression exp : expressionGroup.getExpressions()) {
                System.out.println(exp.toString());
            }
            if (expressionGroup != null) {
                expressionGroups.add(expressionGroup);
            }
        }
        return expressionGroups;
    }


    /**
     * generate阶段要调用的函数
     * 返回一个能够从input中生产output的expressions集合
     *  @param inputString
     * @param outputString
     * @param examplePairs
     */
    public static ExpressionGroup generateStr(String inputString, String outputString, List<ExamplePair> examplePairs) {
        // 论文中记作W W指能产生outputString[i，j]的所有方法集合,包括constStr[s[i,j]]以及动态获得子串方法generateSubString().
        int len = outputString.length();
        ResultMap resultMap = new ResultMap(len, len);

        RunTimeMeasurer.startTiming();
        List<Match> matches = buildStringMatches(inputString);

        for (int i = 1; i <= len; i++) {
            for (int j = 0; i + j <= len; j++) {
                String subString = outputString.substring(j, j + i);

                ExpressionGroup expressionGroup = new ExpressionGroup();
                expressionGroup.insert(generateSubString(inputString, subString, matches));
                if (needBeAddedIn(subString, inputString)) {
                    expressionGroup.insert(new ConstStrExpression(subString));
                }
                resultMap.setData(j, i + j, expressionGroup);
            }
        }
        RunTimeMeasurer.endTiming("generateSubString");

        // FIXME: 2017/2/3 此方法过于耗时，当item数和每个item的长度增加时，解会爆炸增长
        // FIXME: 2017/2/3 初步推测这和constStr过多有关
        RunTimeMeasurer.startTiming();
        resultMap.setData(0, len, generateJumpingExps(examplePairs,resultMap, 0, len));
        RunTimeMeasurer.endTiming("generateJumpingExps");

        RunTimeMeasurer.startTiming();
        // FIXME: 2017/1/25 BUG:如果类似IBMHW，输出为IBM,HW，其中IBM是一个Loop，HW是一个LOOP但是现在程序不能产生这种Loop
        // FIXME 原因应该是处在generateLoop的位置，应该和论文一样把他放到generateStr的每个循环中
        // FIXME: 2017/2/3 当前的方法也比较耗时(约为concatRes的20%)
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j <= len; j++) {
                resultMap.getData(i, j).insert(generateLoop(i, j, resultMap, len));
            }
        }
        RunTimeMeasurer.endTiming("generateLoop");

        ExpressionGroup usefulExpressions = resultMap.getData(0, len);
        return usefulExpressions;
    }


    /**
     * 新方法：应对IBM这种跳跃式的output，可以先用Concate把o[0],o[1],o[2]连接起来
     * 之后generateLoop中只要把表达式全都一样的concate合并成一个Loop即可。
     * <p>
     * 算法思想：dfs
     */
    private static ExpressionGroup generateJumpingExps(List<ExamplePair> examplePairs, ResultMap resultMap, int start, int end) {
        if (start + 1 == end) {
            return resultMap.getData(start, end);
        }
        ExpressionGroup newExpressions = resultMap.getData(start, end).deepClone();
        for (int j = start + 1; j < end; j++) {
            ExpressionGroup curExpressions = resultMap.getData(start, j);
            if (curExpressions.size() > 0) {
                ExpressionGroup tmpConcatedExps = DeprecatedConcatenateExpression.concatenateExp(curExpressions, generateJumpingExps(examplePairs,resultMap, j, end));
                newExpressions.insert(getValidExpressions(examplePairs,tmpConcatedExps));
            }
        }
        return newExpressions;
    }

    private static ExpressionGroup getValidExpressions(List<ExamplePair> examplePairs, ExpressionGroup tmpConcatedExps) {
        ExpressionGroup expressionGroup=new ExpressionGroup();
        for (Expression exp:tmpConcatedExps.getExpressions()){
            if (exp instanceof NonTerminalExpression){
                boolean needAddIn=true;
                for (ExamplePair examplePair:examplePairs){
                    String ans=((NonTerminalExpression) exp).interpret(examplePair.getInputString());
                    if (!examplePair.getOutputString().contains(ans)){
                        needAddIn=false;
                        break;
                    }
                }
                if (needAddIn){
                    expressionGroup.insert(exp);
                }
            }
        }
        return expressionGroup;
    }

    /**
     * 已经在concatResExp()中处理过跳跃性的res(如首字母提取)
     * generateLoop()中只要找到是拼接起来的，而且左右表达式一致的exp即可。
     */
    private static ExpressionGroup generateLoop(int passbyNode, int endNode, ResultMap resultMap, int outputLen) {
        // TODO: 2017/1/23 效率存在问题，output一旦变长，程序就运行不出来了

        ExpressionGroup outputExpressions = resultMap.getData(passbyNode, endNode);
        ExpressionGroup loopExpressions = new ExpressionGroup();
//        for (Expression exp : outputExpressions.getExpressions()) {
//            if (exp instanceof DeprecatedConcatenateExpression) {
//                // TODO: 2017/2/16 这里也许可以改为：1. 左做loop 2. 右做loop 3. 合并左右 ，这样可以解决类似“1 2 3 Bank Of China”->"1-2-3 BOC"之类的问题
//                if (isSameExpression(((DeprecatedConcatenateExpression) exp).getLeftExp(), ((DeprecatedConcatenateExpression) exp).getRightExp())) {
////                    System.out.println("same:");
////                    System.out.println(exp);
//                    LoopExpression loop = new LoopExpression(LoopExpression.LINKING_MODE_CONCATENATE, exp, passbyNode, endNode);
////                    System.out.println(loop.toString());
//                    loopExpressions.insert(loop);
//                    if (endNode == outputLen) {
//                        LoopExpression loopToEnd = new LoopExpression(LoopExpression.LINKING_MODE_CONCATENATE, exp, passbyNode, PosExpression.END_POS);
//                        loopExpressions.insert(loopToEnd);
//                    }
//                }
//            }
//        }
        return loopExpressions;
    }

    /**
     * 返回从intputString中得到target的所有方式
     * 如：
     * inputString=123-456-123,targetString=123
     * 那么就返回s[0：3]+s[-3：-1]...
     * <p>
     * 返回一组对subStr(s,p1,p2)方法的引用，其中p1,p2则是通过generatePos()得到。
     *
     * @param inputString  输入数据
     * @param targetString 要从intputString中截取的字符串
     * @param matches
     */
    public static ExpressionGroup generateSubString(String inputString, String targetString, List<Match> matches) {
        ExpressionGroup result = new ExpressionGroup();

        ExpressionGroup substr2Expressions = generateSubStr2(inputString, targetString);
        result.insert(substr2Expressions);

        int targetLen = targetString.length();
        for (int k = 0; k <= inputString.length() - targetLen; k++) {
            // 如果input中的某一段能够和target匹配(因为target定长，所以遍历input，每次抽取I中长度为targetLen的一段进行比较)，那么就把此时的posExpression添加到res中
            // TODO: 2017/1/22 这里可能也可以利用matches加速处理
            if (inputString.substring(k, k + targetLen).equals(targetString)) {
                List<PosExpression> res1 = generatePos(inputString, k, matches);
                List<PosExpression> res2 = generatePos(inputString, k + targetLen, matches);

                // 把找到的pos转换为subString
                for (PosExpression expression1 : res1) {
                    for (PosExpression expression2 : res2) {
                        result.insert(new SubStringExpression(expression1, expression2));
                    }
                }
                break;
            }
        }
        return result;
    }

    private static ExpressionGroup generateSubStr2(String inputString, String targetString) {
        ExpressionGroup res = new ExpressionGroup();
        for (int i = 0; i < usefulRegex.size(); i++) {
            Regex regex = usefulRegex.get(i);
            Matcher matcher = regex.getPattern().matcher(inputString);
            int count = 0;
            while (matcher.find()) {
                count++;
                if (matcher.group().equals(targetString)) {
                    res.insert(new SubString2Expression(regex, count));
                }
            }
        }
        return res;
    }

    /**
     * 返回一组能够取得相应位置的'表达式!'，如Pos(r1,r2,c),其中r是正则表达式(在这里是token)，c代表第c个匹配
     * <p>
     * 注：r=TokenSeq(T1,T2..Tn)表示Str要符合[T1]+[T2]+...[Tn]+这种形式
     * 如：r=TokenSea(num，letter)，那么str必须是123abc或1Abb这种形式才能和r匹配
     * <p>
     *
     * @param inputString
     * @param k
     * @param matches
     * @return 一个表示能够从input中截取出targetString的位置集合。
     * 如：input=“123-abc-456-zxc" target="abc" 那么一个有效的起点pos(即a的位置)=POS(hyphenTok(即‘-’),letterTok,1||-2)
     * 这个POS表示第一个或倒数第二个左侧为‘-’，右侧为字母的符号的位置
     */
    public static List<PosExpression> generatePos(String inputString, int k, List<Match> matches) {
        List<PosExpression> result = new ArrayList<PosExpression>();
//        // 首先把k这个位置(正向数底k个，逆向数第-(inputString.length()-k)个)加到res中
//        if (k == 0) {
//            result.add(new AbsPosExpression(k));
//        }
//        if (k == inputString.length()) {
//            result.add(new AbsPosExpression(PosExpression.END_POS));
//        }
//
//        for (Match match:matches){
//            if (match.getMatchedIndex()==k){
//                result.add(new MatchStartPos(match.getRegex(),match.getCount()));
//            }else if ((match.getMatchedIndex()+match.getMatchedString().length())==k){
//                result.add(new MatchEndPos(match.getRegex(),match.getCount()));
//            }
//        }
//
//        /**
//         * 新方法：
//         * TODO 重构代码
//         */
//        for (int k1 = k - 1; k1 >= 0; k1--) {
//            for (int m1 = 0; m1 < matches.size(); m1++) {
//                Match match1 = matches.get(m1);
//                // TODO 确定r1，把if改成TokenSeq形式，要能根据match的起点和终点进行跳跃
//                if (match1.getMatchedIndex() == k1 && (match1.getMatchedIndex() + match1.getMatchedString().length()) == k) {
//                    Regex r1 = match1.getRegex();
//                    for (int k2 = k + 1; k2 <= inputString.length(); k2++) {
//                        for (int m2 = 0; m2 < matches.size(); m2++) {
//                            Match match2 = matches.get(m2);
//                            // TODO 确定r2，把if改成TokenSeq形式，要能根据match的起点和终点进行跳跃
//                            if (match2.getMatchedIndex() == k && (k + match2.getMatchedString().length()) == k2) {
//                                Regex r2 = match2.getRegex();
//
//                                // TODO: 2017/1/22 用更好的方法合并r1和r2
//                                Regex r12 = new CombinedRegex("r12", r1.getReg() + r2.getReg());
//                                List<Match> totalMatches = r12.doMatch(inputString);
//                                int curOccur = -1;
//                                String sk1k2 = inputString.substring(k1, k2);
//                                for (int i = 0; i < totalMatches.size(); i++) {
//                                    if (sk1k2.equals(totalMatches.get(i).getMatchedString())) {
//                                        curOccur = i + 1;
//                                        break;
//                                    }
//                                }
//                                result.add(new RegPosExpression(r1, r2, curOccur));
//                                result.add(new RegPosExpression(r1, r2, -(totalMatches.size() - curOccur + 1)));
//                            }
//                        }
//                    }
//                }
//            }
//        }
        return result;
    }


    /**
     * 当有多个IOPair时，每个IOPair都会对应一组解，但是实际上很多例子属于同一类别
     * generatePartition就是为所有IOPairs做一个划分，将相同类别的例子归到同一类(然后配合Classifier就可以做switch了)
     * <p>
     * 基本思想：
     * 1. while所有pairs中存在某两个pair相互兼容(难点1.相互兼容的定义)
     * 2. 找到所有配对中CS(Compatibility Score)最高的一对(难点2.CS分的定义 难点3.快速求最高分的方法,可用模拟退火？)
     * 3. T=T-(原有两个pairs)+(pairs合并后的结果)(小难点.合并？)
     * <p>
     * 改进：
     * 1. 上面的基本思想是基于贪心的，可以改成启发式搜索
     * 2. 类似试卷分配，可以改成基于swap的模拟退火
     * <p>
     * 备注:
     * 如果当前的lookupPartition所用的str相似度方法靠谱的话，就可以把generatePartition改造成聚类算法
     *
     * @param expressionList
     * @param examplePairs
     */
    private static List<ExamplePartition> generatePartitions(List<ExpressionGroup> expressionList, List<ExamplePair> examplePairs) {
        // init
        RunTimeMeasurer.startTiming();
        List<ExamplePartition> partitions = new ArrayList<ExamplePartition>();
        for (int i = 0; i < examplePairs.size(); i++) {
            partitions.add(new ExamplePartition(examplePairs.get(i), expressionList.get(i)));
        }

        boolean needMerge = true;
        while (needMerge) {
            needMerge = false;
            int max = 0;
            // findPartitions
            ExamplePartition partition1 = null;
            ExamplePartition partition2 = null;
            ExpressionGroup p12TheSameExpressions = null;

            int index1 = 0;
            int index2 = 0;
            for (int i = 0; i < partitions.size(); i++) {
                for (int j = i + 1; j < partitions.size(); j++) {
                    ExpressionGroup theSameExpressions = findSameExps(partitions.get(i), partitions.get(j));
                    if (theSameExpressions.size() > max) {
                        max = theSameExpressions.size();
                        partition1 = partitions.get(i);
                        partition2 = partitions.get(j);
                        p12TheSameExpressions = theSameExpressions;
                        index1 = i;
                        index2 = j;
                        needMerge = true;
                    }
                }
            }

            // mergePartitions
            if (needMerge) {
                // TODO: 2017/2/5 假设存在某两个partition的sameExp.size()只有1，不知道此时还要不要进行合并(但是目前还没有遇见这种情况)。
                System.out.println(String.format("Merge %d and %d,max=%d", index1, index2, max));
                partitions.remove(partition1);
                partitions.remove(partition2);

                List<ExamplePair> pairs = new ArrayList<ExamplePair>();
                pairs.addAll(partition1.getExamplePairs());
                pairs.addAll(partition2.getExamplePairs());

                partitions.add(new ExamplePartition(pairs, p12TheSameExpressions));
            }
        }
        RunTimeMeasurer.endTiming("generatePartition");
        return partitions;
    }

    /**
     * 在generatePartition中使用
     * 用于找出两个partition可共用的expressionList
     * <p>
     * 当返回的theSameExpressions.size()>0 说明两个partition可以合并
     *
     * @param partition1
     * @param partition2
     * @return
     */
    private static ExpressionGroup findSameExps(ExamplePartition partition1, ExamplePartition partition2) {
        // FIXME: 2017/2/6 这个函数运行时间较长，根本原因应该还是partition中的expression过于庞大
        ExpressionGroup expressions1 = partition1.getUsefulExpression();
        ExpressionGroup expressions2 = partition2.getUsefulExpression();

        List<ExamplePair> pairs1 = partition1.getExamplePairs();
        List<ExamplePair> pairs2 = partition2.getExamplePairs();

        ExpressionGroup theSameExpressions = new ExpressionGroup();
        for (Expression e1 : expressions1.getExpressions()) {
            for (Expression e2 : expressions2.getExpressions()) {
                if (e1.equals(e2)) {
                    boolean isTwoExpSame = true;
                    if (e1 instanceof NonTerminalExpression && e2 instanceof NonTerminalExpression) {
                        for (ExamplePair pair : pairs1) {
                            if (!((NonTerminalExpression) e2).interpret(pair.getInputString()).equals(pair.getOutputString())) {
                                isTwoExpSame = false;
                                break;
                            }
                        }
                        if (isTwoExpSame) {
                            for (ExamplePair pair : pairs2) {
                                if (!((NonTerminalExpression) e1).interpret(pair.getInputString()).equals(pair.getOutputString())) {
                                    isTwoExpSame = false;
                                    break;
                                }
                            }
                        }
                        if (isTwoExpSame) {
                            theSameExpressions.insert(e1);
                        }
                    }
                }
            }
        }
        return theSameExpressions;
    }

    public static List<Regex> usefulRegex = initUsefulRegex();

    /**
     * 增加有效的token可以强化匹配能力
     * <p>
     * 但是每添加一个token，答案数就要乘以这个token能产生的结果数
     * token过多会导致结果爆炸增长(很容易伤几千万)
     *
     * @return
     */
    private static List<Regex> initUsefulRegex() {
        List<Regex> regexList = new ArrayList<Regex>();
        regexList.add(new NormalRegex("SimpleNumberTok", "[0-9]+"));
        regexList.add(new NormalRegex("DigitToken", "[-+]?(([0-9]+)([.]([0-9]+))?)"));
        regexList.add(new NormalRegex("LowerToken", "[a-z]+"));
        regexList.add(new NormalRegex("UpperToken", "[A-Z]+"));
        regexList.add(new NormalRegex("AlphaToken", "[a-zA-Z]+"));
//        regexList.add(new Regex("WordToken","[a-z\\sA-Z]+")); // 匹配单词的token，会导致结果爆炸增长几十万倍

        // TimeToken可匹配[12:15 | 10:26:59 PM| 22:01:15 aM]形式的时间数据
        regexList.add(new RareRegex("TimeToken", "(([0-1]?[0-9])|([2][0-3])):([0-5]?[0-9])(:([0-5]?[0-9]))?([ ]*[aApP][mM])?"));
        // YMDToken可匹配[10/03/1979 | 1-1-02 | 01.1.2003]形式的年月日数据
        regexList.add(new RareRegex("YMDToken", "([0]?[1-9]|[1|2][0-9]|[3][0|1])[./-]([0]?[1-9]|[1][0-2])[./-]([0-9]{4}|[0-9]{2})"));
        // YMDToken2可匹配[2004-04-30 | 2004-02-29],不匹配[2004-04-31 | 2004-02-30 | 2004-2-15 | 2004-5-7]
        regexList.add(new RareRegex("YMDToken2", "[0-9]{4}-(((0[13578]|(10|12))-(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)-(0[1-9]|[1-2][0-9]|30)))"));
        // TextDate可匹配[Apr 03 | February 28 | November 02] (PS:简化版，没处理日期的逻辑错误)
        regexList.add(new RareRegex("TextDate", "(Jan(uary)?|Feb(ruary)?|Ma(r(ch)?|y)|Apr(il)?|Jul(y)?|Ju((ly?)|(ne?))|Aug(ust)?|Oct(ober)?|(Sept|Nov|Dec)(ember)?)[ -]?(0[1-9]|[1-2][0-9]|3[01])"));
        regexList.add(new RareRegex("WhichDayToken", "(Mon|Tues|Fri|Sun)(day)?|Wed(nesday)?|(Thur|Tue)(sday)?|Sat(urday)?"));
//        regices.add(new Regex("AlphaNumToken", "[a-z A-Z 0-9]+"));

        // special tokens
        regexList.add(new EpicRegex("TestSymbolToken", "[-]+"));
        regexList.add(new EpicRegex("CommaToken", "[,]+"));
        regexList.add(new EpicRegex("<", "[<]+"));
        regexList.add(new EpicRegex(">", "[>]+"));
        regexList.add(new EpicRegex("/", "[/]+"));
//        regexList.add(new Regex("SpaceToken", "[ ]+")); // 加上之后就出不了结果？？
        // FIXME: 2017/2/5 如果开启这个SpTok在当前算法下会导致解过于庞大
//        regexList.add(new Regex("SpecialTokens","[ -+()\\[\\],.:]+"));

        return regexList;
    }

    /**
     * 在每次有新的input时就调用此方法，可以返回 各个pos上所有能够和input匹配的集合
     * 当generatePosition()需要时，直接根据match的pos(index)去查找使用，避免重复计算
     */
    private static List<Match> buildStringMatches(String inputString) {
        // TODO: 2017/2/5 加入match次数的能力
        // TODO: 2017/2/5 加入不match的能力
        List<Match> matches = new ArrayList<Match>();
        for (int i = 0; i < usefulRegex.size(); i++) {
            Regex regex = usefulRegex.get(i);
            List<Match> curMatcher = regex.doMatch(inputString);
            matches.addAll(curMatcher);
        }
        return matches;
    }

    /**
     * 注：r=TokenSeq(T1,T2..Tn)表示Str要符合[T1]+[T2]+...[Tn]+这种形式
     * 如：r=TokenSea(num，letter)，那么str必须是123abc或1Abb这种形式才能和r匹配
     * TokenSeq中可能存在的token，如多次123abc456zxc会表示成{numToken，letterToken，numtoken，letterToken}形势，
     * 上面例子得到的经过本方法去重复之后得到{numtoken，letterToken}
     * <p>
     * △ 但是：123abc456->{numToken，letterToken，numtoken}不可以变成{numToken，letterToken}
     * <p>
     * 不太理解，不知道是不是可以略去
     * <p>
     * 对于一个某次字符串s匹配，token1和token2会取得一样的效果，此时token1和token2就没有区别(indistinguishable)
     *
     * @param regExpression
     * @param inputString
     */
    public static void generateRegex(RegExpression regExpression, String inputString) {

    }

    /**
     * same的定义：
     * constStr要求str相同
     * 普通Expression要求token相同
     * linkingExpression要求左右两边的普通Expression相同(如果linkingExpression左右均为LinkingExpression，)
     * <p>
     * FIXME: 现在只做了substr2的equals
     *
     * @param leftExp
     * @param rightExp
     * @return
     */
    private static boolean isSameExpression(Expression leftExp, Expression rightExp) {
        // FIXME: 2017/2/5 现在(包括论文里)不能处理以下 这种LOOP:
        // FIXME concat(subStr2(SimpleNumberTok,1),concat(constStr(-),concat(subStr2(SimpleNumberTok,2),concat(constStr(-),subStr2(SimpleNumberTok,3)))))
        Expression left = leftExp.deepClone();
        Expression right = rightExp.deepClone();
        if (leftExp instanceof DeprecatedConcatenateExpression) {
            if (isSameExpression(((DeprecatedConcatenateExpression) leftExp).getLeftExp(),
                    ((DeprecatedConcatenateExpression) leftExp).getRightExp())) {
                while (((DeprecatedConcatenateExpression) leftExp).getLeftExp() instanceof DeprecatedConcatenateExpression) {
                    leftExp = ((DeprecatedConcatenateExpression) leftExp).getLeftExp();
                }
                left = ((DeprecatedConcatenateExpression) leftExp).getLeftExp().deepClone();
            } else {
                return false;
            }
        }
        if (rightExp instanceof DeprecatedConcatenateExpression) {
            if (isSameExpression(((DeprecatedConcatenateExpression) rightExp).getLeftExp(),
                    ((DeprecatedConcatenateExpression) rightExp).getRightExp())) {
                while (((DeprecatedConcatenateExpression) rightExp).getLeftExp() instanceof DeprecatedConcatenateExpression) {
                    rightExp = ((DeprecatedConcatenateExpression) rightExp).getLeftExp();
                }
                right = ((DeprecatedConcatenateExpression) rightExp).getLeftExp().deepClone();
            } else {
                return false;
            }
        }
        if (left instanceof SubString2Expression) {
            return ((SubString2Expression) left).loopEquals(right);
        } else {
            return false;
        }
    }


    private static boolean needBeAddedIn(String subString, String inputString) {
        // 如果是原字符串中存在的str，那么就不需要添加(可能会有特例，需要注意一下)
        // TODO: 2017/2/2 (在最终调整之前不修改这个，以防万一)字符串是否存在要修改一下 ，去掉subString的分隔符，然后用LSC比较subString是否全都出现过
        boolean existedString = inputString.indexOf(subString) >= 0;

        // 如果是分界符，那么就添加进去
        // FIXME: 2017/2/3 delimiterReg增大会导致答案急剧增多(比如在[-,]+时为10W个，增大到[-, ]+时可能就会有1000W个)，还不知道怎么解决
        String delimiterReg = "[-,]+";
        return !existedString || subString.matches(delimiterReg);
    }


    private static void verifyResult(ExpressionGroup resExps, String testString, String target, boolean needToString, int deepth) {
        try {
            FileWriter fileWriter = new FileWriter("C:\\Users\\hasee\\Desktop\\tempdata\\string-processor\\ans.txt");
            for (Expression exp : resExps.getExpressions()) {
//            if (exp instanceof LoopExpression)
                if (needToString)
                    System.out.println(String.valueOf(exp.deepth()) + " " + exp.toString());
                if (exp instanceof NonTerminalExpression) {
                    String result = ((NonTerminalExpression) exp).interpret(testString);
                    if (result == null) {
                        System.out.println("null");
                    } else if (exp.deepth() <= deepth) {
                        if (result.equals(target))
                            System.out.println(String.valueOf(exp.deepth()) + " " + exp.toString());
                    }
                }
                fileWriter.write(exp.toString());
                fileWriter.write("\n");
                fileWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showPartitions(List<ExamplePartition> partitions) {
        for (int i = 0; i < partitions.size(); i++) {
            System.out.println(String.format("Partition %d :", i));
            partitions.get(i).showDetails(true, false);
        }
    }

    /**
     * 找到当前String应该所属的分类(取代了论文中的classifier)
     *
     * @param string
     * @param partitions
     * @return
     */
    private static int lookupPartitionIndex(String string, List<ExamplePartition> partitions) {
        int index = -1;
        double maxScore = -1;
        for (int i = 0; i < partitions.size(); i++) {
            Double curScore = partitions.get(i).calculateSimilarity(string);
            if (curScore > maxScore) {
                maxScore = curScore;
                index = i;
            }
        }
        return index;
    }

    /**
     * 根据example得到partitions之后可以开始处理新的输入
     * 首先在partition找到newInput所属的分类
     * 然后再此分类中找出topN的表达式
     * <p>
     * 然后输出执行结果(以及用这个公式的概率)
     *
     * @param newInput
     * @param partitions
     */
    private static ExpressionGroup predictOutput(String newInput, List<ExamplePartition> partitions) {
        int partitionIndex = lookupPartitionIndex(newInput, partitions);

        System.out.println("==========所属partition=" + partitionIndex + " ==========");
        ExamplePartition partition = partitions.get(partitionIndex);

        ExpressionGroup topNExpression = getTopNExpressions(partition, newInput, 5000000);

        return topNExpression;
    }

    /**
     * 找出rank得分最高的n个Expression，从前往后排序
     * 【需要一个有效的rank】
     *
     * @param partition
     * @param testString
     * @param n          取出rank前n的结果
     * @return
     */
    private static ExpressionGroup getTopNExpressions(ExamplePartition partition, String testString, int n) {
        ExpressionGroup topN = new ExpressionGroup();
        // TODO: 2017/2/6 等待rank算法
        List<Expression> expressions = partition.getUsefulExpression().getExpressions();
        Collections.sort(expressions, new ExpressionComparator());
        int count = 1;
        for (Expression exp : expressions) {
            topN.insert(exp);
            if (count++ >= n) {
                break;
            }
        }
        return topN;
    }

    /**
     * 在得到partitions划分之后，依次处理每一个input并显示
     *
     * @param validationPairs
     * @param partitions
     */
    private static void handleNewInput(List<ValidationPair> validationPairs, List<ExamplePartition> partitions) {
        for (ValidationPair v : validationPairs) {
            ExpressionGroup topNExpression = predictOutput(v.getInputString(), partitions);
            displayOutput(v, topNExpression);
        }
    }

    /**
     * 显示预测的结果
     * 根据需要可以切换
     *
     * @param v
     * @param topNExpression
     */
    private static void displayOutput(ValidationPair v, ExpressionGroup topNExpression) {
        System.out.println("期望输出：" + v.getTargetString());
        for (Expression expression : topNExpression.getExpressions()) {
            if (expression instanceof NonTerminalExpression) {
                if (v.getTargetString().equals(((NonTerminalExpression) expression).interpret(v.getInputString())))
                    System.out.println(((NonTerminalExpression) expression).interpret(v.getInputString()) + " , " + expression.toString());
            }
        }
    }

    private static List<ExamplePair> getExamplePairs() {

        // 对于提取IBM形式的句子，最后W的规模大致为3*(len(o))^2
        // 其他的subStr问题W的规模会小很多

        // FIXME 当前concatExp算法为指数型函数，一旦output中item数(比如用逗号隔开)增加以及每个item的长度变长，计算时间会爆炸增长。
        // FIXME: 2017/2/3 初步估计每个item延长一位会让concatResExp耗时翻倍，每增加一个item，就会导致concatResExp耗时乘以n倍
        List<ExamplePair> examplePairs = new ArrayList<ExamplePair>();
        // region # success
        // 提取结构化数据能力
//        examplePairs.add(new ExamplePair("Electronics Store,40.74260751,-73.99270535,Tue Apr 03 18:08:57 +0800 2012", "Electronics Store,Apr 03"));
//        examplePairs.add(new ExamplePair("Airport,40.77446436,-73.86970997,Sun Jul 15 14:51:15 +0800 2012", "Airport,Jul 15"));
//        examplePairs.add(new ExamplePair("Bridge,Tue Apr 03 18:00:25 +0800 2012", "Bridge,Apr 03"));
//        examplePairs.add(new ExamplePair("Arts & Crafts Store,40.71981038,-74.00258103,Tue Apr 03 18:00:09 +0800 2012", "Arts & Crafts Store,Apr 03,Tue"));
//
//        examplePairs.add(new ExamplePair("Wed Jul 11 11:17:44 +0800 2012,40.23213,German Restaurant", "German Restaurant,Jul 11"));
//        examplePairs.add(new ExamplePair("40.7451638,-73.98251878,Tue Apr 03 18:02:41 +0800 2012,Medical Center", "Medical Center,Apr 03"));

        // 单个较长output
//        examplePairs.add(new ExamplePair("Electronics Store,40.74260751,-73.99270535,Tue Apr 03 18:08:57 +0800 2012", "Electronics Store,Apr 03,Tue"));

        // 初级Loop能力
//        examplePairs.add(new ExamplePair("Hello World Zsf the Program Synthesis Electronics Airport","HWZPSEA"));
//        examplePairs.add(new ExamplePair("Hello World Zsf the Program Synthesis Electronics Airport Bridge","HWZPSEAB"));

        // endregion

        examplePairs.add(new ExamplePair("                        姓名：<span class=\"name\">陈自郁</span> <br> 职称：<span class=\"zc\">讲师</span><br> 联系方式：<span class=\"lxfs\">chenziyu@cqu.edu.cn</span><br> 主要研究方向:<span class=\"major\">群智能、图像处理和智能控制</span><br>", "陈自郁,讲师,群智能、图像处理和智能控制"));
//        examplePairs.add(new ExamplePair("姓名：<span class=\"name\">Ran Liu</span> <br> 职称：<span class=\"zc\">Associate Professor/Senior Engineer</span><br> 联系方式：<span class=\"lxfs\">ran.liu_cqu@qq.com</span><br> 主要研究方向:<span class=\"major\">Medical and stereo image processing; IC design; Biomedical Engineering</span><br>","Ran Liu,Associate Professor/Senior Engineer,Medical and stereo image processing; IC design; Biomedical Engineering"));

        // region # error
        // FIXME: 2017/2/16 错误原因初步判定为相似度(classifier)错误
//        examplePairs.add(new ExamplePair("01/21/2001","01"));
//        examplePairs.add(new ExamplePair("2003-03-23","03"));

        // FIXME: 2017/2/16 未知错误，运行时很久没有结果，可能在哪里死循环了，需要debug
//        examplePairs.add(new ExamplePair("12-23-34","12-23-34"));
//        examplePairs.add(new ExamplePair("12.3.4","12-3-4"));
//        examplePairs.add(new ExamplePair("74-12","abc-74-12"));
//        examplePairs.add(new ExamplePair("(123)-84-122","123-84-122"));

        // FIXME: 2017/2/21 去掉注释
//        examplePairs.add(new ExamplePair("System.out.println(\"hello\");//hello", "System.out.println(\"hello\");"));
//        examplePairs.add(new ExamplePair("Hello World// Zsf the Program Synthesis","Hello World"));
        // endregion


        return examplePairs;
    }

    private static List<ValidationPair> getTestPairs() {
        List<ValidationPair> testPairs = new ArrayList<ValidationPair>();

        // region # success
        // 提取结构化数据
        testPairs.add(new ValidationPair("Coffee Shop,40.73340972,-74.00285648,Wed Jul 13 12:27:07 +0800 2012", "Coffee Shop,Jul 13"));
        testPairs.add(new ValidationPair("Bridge,43,-73,Tue Apr 03 18:00:25 +0800 2012", "Coffee Shop,Jul 13"));

        testPairs.add(new ValidationPair("40.69990191,,Sat Nov 17 20:36:26 +0800,Food & Drink Shop", "Food & Drink Shop,Nov 17"));
        testPairs.add(new ValidationPair("40.74218831,-73.98792419,Park,Wed Jul 11 11:42:00 +0800 2012", "Park,Jul 11"));

        // 初级Loop
        testPairs.add(new ValidationPair("Foundation of Software Engineering","FSE"));
        testPairs.add(new ValidationPair("European Software Engineering Conference","ESEC"));
        testPairs.add(new ValidationPair("International Conference on Software Engineering","ICSE"));
        // endregion


        // region # error
        testPairs.add(new ValidationPair("姓名：<span class=\"name\">Ran Liu</span> <br> 职称：<span class=\"zc\">Associate Professor/Senior Engineer</span><br> 联系方式：<span class=\"lxfs\">ran.liu_cqu@qq.com</span><br> 主要研究方向:<span class=\"major\">Medical and stereo image processing; IC design; Biomedical Engineering</span><br>","Ran Liu,Associate Professor/Senior Engineer,Medical and stereo image processing; IC design; Biomedical Engineering"));
        testPairs.add(new ValidationPair("姓名：<span class=\"name\">陈波</span> <br> 职称：<span class=\"zc\"></span><br> 联系方式：<span class=\"lxfs\"></span><br> 主要研究方向:<span class=\"major\"></span><br>", ""));
        testPairs.add(new ValidationPair("                        姓名：<span class=\"name\">陈自郁</span> <br> 职称：<span class=\"zc\">讲师</span><br> 联系方式：<span class=\"lxfs\">chenziyu@cqu.edu.cn</span><br> 主要研究方向:<span class=\"major\">群智能、图像处理和智能控制</span><br>", "讲师"));
        testPairs.add(new ValidationPair("                        姓名：<span class=\"name\">但静培</span> <br> 职称：<span class=\"zc\">讲师</span><br> 联系方式：<span class=\"lxfs\"></span><br> 主要研究方向:<span class=\"major\">时间序列数据挖掘、计算智能、神经网络等</span><br>", "讲师"));


        // FIXME: 2017/2/16 错误原因初步判定为相似度(classifier)错误
        testPairs.add(new ValidationPair("2014年3月23日","3"));
        testPairs.add(new ValidationPair("9/23/2012","09"));

        // FIXME: 2017/2/16 未知错误，运行时很久没有结果，可能在哪里死循环了，需要debug
//        testPairs.add(new ValidationPair("1234-2345-23", "1234-2345-23"));
//        testPairs.add(new ValidationPair("1.3213.02", "1-3213-02"));

        // FIXME: 2017/2/21 去掉注释
//        testPairs.add(new ValidationPair("testPairs.add(new ValidationPair(\"Foundation of Software Engineering\",\"FSE\")); //测试","Coffee Shop,Jul 13"));
//        testPairs.add(new ValidationPair("40.69990191,//,Sat Nov 17 20:36:26 +0800,Food & Drink Shop","Food & Drink Shop,Nov 17"));
//        testPairs.add(new ValidationPair("40.74218831,-73.9879//2419,Park,Wed Jul 11 11:42:00 +0800 2012","Park,Jul 11"));

        // endregion
        return testPairs;
    }

    public static void main(String[] args) {
        List<ExamplePair> examplePairs = getExamplePairs();
        List<ValidationPair> testPairs = getTestPairs();

        StringProcessor stringProcessor=new StringProcessor();

        List<ResultMap> resultMaps=stringProcessor.generateExpressionsByExamples(examplePairs);


//        List<ExamplePartition> partitions = generatePartitions(expressionList, examplePairs);
//        List<ExamplePartition> partitions=stringProcessor.generatePartitions(expressionList,examplePairs);
//        showPartitions(partitions);
        ExpressionGroup topKExps=stringProcessor.selectTopKExps(resultMaps,5);
        stringProcessor.handleNewInput(testPairs, topKExps);




        //        boolean needVerifyResult = false;
//        boolean needToString = false;
//        int deepth = 4;
//        if (needVerifyResult) {
//            System.out.println("--------------------------------------------");
//            for (ValidationPair v:validationPairs){
//                verifyResult(resExps, v.getInputString(), v.getTargetString(), needToString, deepth);
//            }
//            System.out.println("============================================\n");
//        }


    }
}
