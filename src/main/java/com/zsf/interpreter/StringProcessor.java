package com.zsf.interpreter;

import com.zsf.common.UsefulRegex;
import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.NonTerminalExpression;
import com.zsf.interpreter.expressions.linking.ConcatenateExpression;
import com.zsf.interpreter.expressions.loop.LoopExpression;
import com.zsf.interpreter.expressions.pos.*;
import com.zsf.interpreter.expressions.regex.*;
import com.zsf.interpreter.expressions.string.ConstStrExpression;
import com.zsf.interpreter.expressions.string.RegSubStringExpression;
import com.zsf.interpreter.expressions.string.SubStringExpression;
import com.zsf.interpreter.model.*;
import com.zsf.interpreter.tool.ExpressionComparator;
import com.zsf.interpreter.tool.RunTimeMeasurer;
import javafx.util.Pair;

import java.util.*;
import java.util.regex.Matcher;

/**
 * Created by hasee on 2017/3/1.
 */
public class StringProcessor {
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
    public List<ResultMap> generateExpressionsByExamples(List<ExamplePair> examplePairs) {
        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        for (ExamplePair pair : examplePairs) {
            String input = pair.getInputString();
            String output = pair.getOutputString();

            ResultMap resultMap = generateStr(input, output);
            resultMaps.add(resultMap);
        }
        return resultMaps;
    }


    /**
     * generate阶段要调用的函数
     * 返回一个能够从input中生产output的expressions集合
     *
     * @param inputString
     * @param outputString
     */
    private ResultMap generateStr(String inputString, String outputString) {
        // 论文中记作W W指能产生outputString[i，j]的所有方法集合,包括constStr[s[i,j]]以及动态获得子串方法generateSubString().
        int len = outputString.length();
        ResultMap resultMap = new ResultMap(len, len);

        RunTimeMeasurer.startTiming();
        List<Match> matches = buildStringMatches(inputString);

        for (int i = 1; i <= len; i++) {
            for (int j = 0; i + j <= len; j++) {
                String subString = outputString.substring(j, j + i);

                ExpressionGroup expressionGroup = new ExpressionGroup();
                ExpressionGroup tmpExpressgionGroup = generateSubString(inputString, subString, matches);
                expressionGroup.insert(tmpExpressgionGroup);
                resultMap.setData(j, i + j, expressionGroup);
            }
        }
        RunTimeMeasurer.endTiming("generateSubString");

        // 上面的resultMap结合DAG就有了跳跃能力，而且无需存储中间结果(只需要存储跳跃边), 再resultMap的基础上再加上一些Loop语句就可实现全局搜索
        // DAG在选择答案时可以结合loss func+beam search极大减小搜索空间。
        generateLoop(outputString, resultMap);


        // TODO: 2017/3/1 返回的是一个DAG(或者就是resultMap)
        return resultMap;
    }

    /**
     * 从某个节点出发，搜寻所有后续路径上一样的exp来构成loop
     * <p>
     * Loop的基本条件：
     * 1. 层数>=2
     * <p>
     * 一样的判定：
     * 1. substr2的reg一样
     * 2. loop的baseExp一样
     * 3. concat的左右的baseExp全都一样(递归包含情况1和2)
     */
    private void generateLoop(String outputString, ResultMap resultMap) {
        RunTimeMeasurer.startTiming();
        memorizeLoopMap = new HashMap<Pair<Integer, Integer>, ExpressionGroup>();
        for (int start = 0; start < outputString.length(); start++) {
            for (int end = start + 1; end <= outputString.length(); end++) {
                // FIXME: 2017/4/24 整个doGenerateLoop()方法都不是很好，感觉应该改成前+后归并
                ExpressionGroup loopExpressions = doGenerateLoop(new LoopExpression(), start, end, resultMap);
                loopExpressions = deDuplicateLoopExps(loopExpressions);
                resultMap.getData(start, end).insert(loopExpressions);
            }
        }
        RunTimeMeasurer.endTiming("generateLoop");
    }

    private ExpressionGroup deDuplicateLoopExps(ExpressionGroup loopExpressions) {
        ExpressionGroup eg = new ExpressionGroup();
        for (Expression exp : loopExpressions.getExpressions()) {
            if (!(exp instanceof LoopExpression)) {
                // 要求exp必须是loop，而且size必须大于等于2
                continue;
            }
            if (((LoopExpression) exp).getTotalExpsCount() < 2) {
                continue;
            }
            boolean needAdd = true;
            for (Expression e : eg.getExpressions()) {
                try {
                    if (e.equals(exp)) {
                        needAdd = false;
                        break;
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
            if (needAdd) {
                eg.insert(exp);
            }
        }
        return eg;
    }

    /**
     * 用于Loop的记忆化搜索的map
     */
    private HashMap<Pair<Integer, Integer>, ExpressionGroup> memorizeLoopMap = new HashMap<Pair<Integer, Integer>, ExpressionGroup>();

    /**
     *
     * @param baseLoopExpression 目前只是用来做Loop类型的标注？？
     * @param start
     * @param end
     * @param resultMap
     * @return
     */
    private ExpressionGroup doGenerateLoop(LoopExpression baseLoopExpression, int start, int end, ResultMap resultMap) {
        ExpressionGroup expressionGroup = memorizeLoopMap.get(new Pair<Integer, Integer>(start, end));
        if (expressionGroup != null) {
            return expressionGroup;
        }
        ExpressionGroup validLoopExpressionGroup = new ExpressionGroup();
        if (start + 1 == end) {
            ExpressionGroup curExpressions = resultMap.getData(start, end).deepClone();

            for (Expression expression : curExpressions.getExpressions()) {
                if (baseLoopExpression.isLegalExpression(expression)) {
                    // 注意这里产生的LoopExpression的size只有1，通常他们是其他表达式服务
                    // 比如1->3 + 3->5 + 5->6 这是5->6就是一个单节点的Loop，而整个1->6是一个3节点的Loop
                    // 对于单节点的LoopExp会在deDuplicateLoopExps()中被过滤掉
                    LoopExpression loopExpression = new LoopExpression();
                    loopExpression.addNode(expression);
                    validLoopExpressionGroup.insert(loopExpression);
                }
            }
            memorizeLoopMap.put(new Pair<Integer, Integer>(start, end), validLoopExpressionGroup);
            return validLoopExpressionGroup;
        }
        // FIXME: 2017/4/24 这段处理方法不好，以后改
        for (int j = start + 1; j < end; j++) {
            ExpressionGroup prefixEG = resultMap.getData(start, j).deepClone();
            for (Expression prefixExp : prefixEG.getExpressions()) {
                if (baseLoopExpression.isLegalExpression(prefixExp)) {
                    LoopExpression loopExpression = new LoopExpression();
                    loopExpression.addNode(prefixExp);

                    ExpressionGroup postfixLoopEG = doGenerateLoop(loopExpression, j, end, resultMap);
                    for (Expression postfixLoopExp : postfixLoopEG.getExpressions()) {
                        LoopExpression newLoopExpression = (LoopExpression) loopExpression.deepClone();
                        newLoopExpression.addNode(postfixLoopExp);
                        validLoopExpressionGroup.insert(newLoopExpression);
                    }
                }
            }
        }
        memorizeLoopMap.put(new Pair<Integer, Integer>(start, end), validLoopExpressionGroup);
        return validLoopExpressionGroup;
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
    private ExpressionGroup generateSubString(String inputString, String targetString, List<Match> matches) {
        ExpressionGroup result = new ExpressionGroup();

        ExpressionGroup regSubstrExpressions = generateRegSubStr(inputString, targetString);
        result.insert(regSubstrExpressions);

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
        if (needBeAddedIn(targetString, inputString)) {
            result.insert(new ConstStrExpression(targetString));
        }
        return result;
    }

    /**
     * 产生RegSubStr(r,c)
     * <p>
     * 这种截取子串的方法不需要通过r1和r2夹逼确定位置，有时会取得比substr更好的效果
     *
     * @param inputString
     * @param targetString
     * @return
     */
    private ExpressionGroup generateRegSubStr(String inputString, String targetString) {
        ExpressionGroup res = new ExpressionGroup();
        for (int i = 0; i < usefulRegex.size(); i++) {
            Regex regex = usefulRegex.get(i);
            Matcher matcher = regex.getPattern().matcher(inputString);
            int count = 0;
            ExpressionGroup curRegExpressions = new ExpressionGroup();
            while (matcher.find()) {
                count++;
                if (matcher.group().equals(targetString)) {
                    curRegExpressions.insert(new RegSubStringExpression(regex, count));
                }
            }
            for (Expression expression : curRegExpressions.getExpressions()) {
                if (expression instanceof RegSubStringExpression) {
                    ((RegSubStringExpression) expression).setTotalC(count);
                }
            }
            res.insert(curRegExpressions);
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
    private List<PosExpression> generatePos(String inputString, int k, List<Match> matches) {
        List<PosExpression> result = new ArrayList<PosExpression>();
        // 首先把k这个位置(正向数底k个，逆向数第-(inputString.length()-k)个)加到res中
        if (k == 0) {
            result.add(new AbsPosExpression(PosExpression.START_POS));
        }
        if (k == inputString.length()) {
            result.add(new AbsPosExpression(PosExpression.END_POS));
        }

        // 找到MatchPos形式的pos表达式
        for (Match match : matches) {
            if (match.getMatchedIndex() == k) {
                result.add(new StartRegPos(match.getRegex(), match.getCount(), match.getMaxCount()));
            } else if ((match.getMatchedIndex() + match.getMatchedString().length()) == k) {
                result.add(new EndRegPos(match.getRegex(), match.getCount(), match.getMaxCount()));
            }
        }

        /**
         * 新方法：
         * TODO 2017/2/15 重构代码
         */
        for (int k1 = k - 1; k1 >= 0; k1--) {
            for (int m1 = 0; m1 < matches.size(); m1++) {
                Match match1 = matches.get(m1);
                // TODO 确定r1，把if改成TokenSeq形式，要能根据match的起点和终点进行跳跃
                if (match1.getMatchedIndex() == k1 && (match1.getMatchedIndex() + match1.getMatchedString().length()) == k) {
                    Regex r1 = match1.getRegex();
                    for (int k2 = k + 1; k2 <= inputString.length(); k2++) {
                        for (int m2 = 0; m2 < matches.size(); m2++) {
                            Match match2 = matches.get(m2);
                            // TODO 确定r2，把if改成TokenSeq形式，要能根据match的起点和终点进行跳跃
                            if (match2.getMatchedIndex() == k && (k + match2.getMatchedString().length()) == k2) {
                                Regex r2 = match2.getRegex();

                                // TODO: 2017/1/22 用更好的方法合并r1和r2
                                Regex r12 = new CombinedRegex("r12", r1.getReg() + r2.getReg());
                                List<Match> totalMatches = r12.doMatch(inputString);
                                int curOccur = -1;
                                String sk1k2 = inputString.substring(k1, k2);
                                for (int i = 0; i < totalMatches.size(); i++) {
                                    if (sk1k2.equals(totalMatches.get(i).getMatchedString())) {
                                        curOccur = i + 1;
                                        break;
                                    }
                                }
                                result.add(new RegPosExpression(r1, r2, curOccur));
                                result.add(new RegPosExpression(r1, r2, -(totalMatches.size() - curOccur + 1)));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }


    private List<Regex> usefulRegex = UsefulRegex.getUsefulRegex();


    /**
     * 在每次有新的input时就调用此方法，可以返回 各个pos上所有能够和input匹配的集合
     * 当generatePosition()需要时，直接根据match的pos(index)去查找使用，避免重复计算
     */
    private List<Match> buildStringMatches(String inputString) {
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
     * 判断一个constStr是否有必要加入到图中
     *
     * @param subString
     * @param inputString
     * @return
     */
    private boolean needBeAddedIn(String subString, String inputString) {
        // 如果是原字符串中存在的str，那么就不需要添加(可能会有特例，需要注意一下)
        // TODO: 2017/2/2 (在最终调整之前不修改这个，以防万一)字符串是否存在要修改一下 ，去掉subString的分隔符，然后用LSC比较subString是否全都出现过
        boolean existedString = inputString.indexOf(subString) >= 0;

        // 如果是分界符，那么就添加进去
        // FIXME: 2017/2/3 delimiterReg增大会导致答案急剧增多(比如在[-,]+时为10W个，增大到[-, ]+时可能就会有1000W个)，还不知道怎么解决
        String delimiterReg = "[-,]+";
        return !existedString || subString.matches(delimiterReg);
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
    private ExpressionGroup predictOutput(String newInput, List<ExamplePartition> partitions) {
//        int partitionIndex = lookupPartitionIndex(newInput, partitions);
//
//        System.out.println("==========所属partition=" + partitionIndex + " ==========");
//        ExamplePartition partition = partitions.get(partitionIndex);
//
//        ExpressionGroup topNExpression = getTopNExpressions(partition, newInput, 5);
//
//        return topNExpression;
        return null;
    }

    /**
     * 在得到partitions划分之后，依次处理每一个input并显示
     *
     * @param validationPairs
     */
    public void handleNewInput(List<ValidationPair> validationPairs, ExpressionGroup expressionGroup) {
        for (ValidationPair v : validationPairs) {
            displayOutput(v, expressionGroup);
        }
    }

    /**
     * 显示预测的结果
     * 根据需要可以切换
     *
     * @param v
     * @param topNExpression
     */
    private void displayOutput(ValidationPair v, ExpressionGroup topNExpression) {
        System.out.println("期望输出：" + v.getTargetString());
        for (Expression expression : topNExpression.getExpressions()) {
            if (expression instanceof NonTerminalExpression) {
//                if (v.getTargetString().equals(((NonTerminalExpression) expression).interpret(v.getInputString())))
                System.out.println(((NonTerminalExpression) expression).interpret(v.getInputString()) + " , " + expression.toString());
            }
        }
    }


    /**
     * 用于selectTopK记忆化搜索的Map
     */
    private Map<Pair<Integer, Integer>, ExpressionGroup> memorizeTopKMap = new HashMap<Pair<Integer, Integer>, ExpressionGroup>();

    public List<ExpressionGroup> selectTopKExps(List<ResultMap> resultMaps, int k) {
        if (resultMaps == null && resultMaps.size() <= 0) {
            return null;
        }
        RunTimeMeasurer.startTiming();
        List<ExpressionGroup> ansList = new ArrayList<ExpressionGroup>();
        for (ResultMap resultMap : resultMaps) {
            memorizeTopKMap = new HashMap<Pair<Integer, Integer>, ExpressionGroup>();
            ExpressionGroup g = searchDAG(resultMap, 0, resultMap.getCol(), k);
            ansList.add(g);
            for (Expression e : g.getExpressions()) {
                System.out.println(e.deepth() + "  " + e.score() + "  " + e.toString());
            }
        }
        RunTimeMeasurer.endTiming("selectTopKExps");
        return ansList;
    }


    /**
     * 应对IBM这种跳跃式的output，可以先用Concate把o[0],o[1],o[2]连接起来
     * 每次用beam search保留前k个答案
     * <p>
     * 算法思想：dfs
     */
    private ExpressionGroup searchDAG(ResultMap resultMap, int start, int end, int k) {
        if (start + 1 == end) {
            return resultMap.getData(start, end);
        }
        ExpressionGroup expressionGroup = memorizeTopKMap.get(new Pair<Integer, Integer>(start, end));
        if (expressionGroup != null) {
            return expressionGroup;
        }
        ExpressionGroup newExpressions = resultMap.getData(start, end).deepClone();

        for (int j = start + 1; j < end; j++) {
            // FIXME 如果改成prefixExpressionGroup=searchDAG(resultMap,start,j,k)的话运行效率会降低而且结果出错，原因未知
            ExpressionGroup prefixExpressionGroup = resultMap.getData(start, j);
            if (prefixExpressionGroup.size() > 0) {
                ExpressionGroup topKPrefixExpGroup = prefixExpressionGroup.selecTopK(k);

                ExpressionGroup topKPostfixExpreessionGroup = searchDAG(resultMap, j, end, k);
                ExpressionGroup concatedTotalExpGroup = ConcatenateExpression.concatenateExp(topKPrefixExpGroup, topKPostfixExpreessionGroup);

                newExpressions.insert(concatedTotalExpGroup.selecTopK(k));
                newExpressions = newExpressions.selecTopK(k);
            }
        }
        ExpressionGroup res = newExpressions.selecTopK(k);
        memorizeTopKMap.put(new Pair<Integer, Integer>(start, end), res);
        return res;
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
     * @param expressionGroups
     * @param examplePairs
     */
    public List<ExamplePartition> generatePartitions(List<ExpressionGroup> expressionGroups, List<ExamplePair> examplePairs) {
        // init
        RunTimeMeasurer.startTiming();
        List<ExamplePartition> partitions = new ArrayList<ExamplePartition>();
        for (int i = 0; i < examplePairs.size(); i++) {
            partitions.add(new ExamplePartition(examplePairs.get(i), expressionGroups.get(i)));
        }

        boolean needMerge = true;
        while (needMerge) {
            ExamplePartition partition1 = null;
            ExamplePartition partition2 = null;
            ExpressionGroup p12TheSameExpressions = null;

            int index1 = 0;
            int index2 = 0;
            needMerge = false;
            int max = 0;
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
                            String strInterpreted=((NonTerminalExpression) e2).interpret(pair.getInputString());
                            if (strInterpreted==null||!strInterpreted.equals(pair.getOutputString())){
                                isTwoExpSame = false;
                                break;
                            }
//                            if (!((NonTerminalExpression) e2).interpret(pair.getInputString()).equals(pair.getOutputString())) {
//
//                            }
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

    /**
     * 找到当前String应该所属的分类(取代了论文中的classifier)
     *
     * @param string
     * @param partitions
     * @return
     */
    public static int lookupPartitionIndex(String string, List<ExamplePartition> partitions) {
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
     * 找出rank得分最高的n个Expression，从前往后排序
     *
     * @param partition
     * @param testString
     * @param n          取出rank前n的结果
     * @return
     */
    public static ExpressionGroup getTopNExpressions(ExamplePartition partition, String testString, int n) {
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

}
