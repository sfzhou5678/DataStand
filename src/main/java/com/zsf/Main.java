package com.zsf;

import com.zsf.interpreter.StringProcessor;
import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.NonTerminalExpression;
import com.zsf.interpreter.expressions.pos.PosExpression;
import com.zsf.interpreter.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static ExpressionGroup getValidExpressions(List<ExamplePair> examplePairs, ExpressionGroup tmpConcatedExps) {
        ExpressionGroup expressionGroup = new ExpressionGroup();
        for (Expression exp : tmpConcatedExps.getExpressions()) {
            if (exp instanceof NonTerminalExpression) {
                boolean needAddIn = true;
                for (ExamplePair examplePair : examplePairs) {
                    String ans = ((NonTerminalExpression) exp).interpret(examplePair.getInputString());
                    if (!examplePair.getOutputString().contains(ans)) {
                        needAddIn = false;
                        break;
                    }
                }
                if (needAddIn) {
                    expressionGroup.insert(exp);
                }
            }
        }
        return expressionGroup;
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
//                result.add(new StartRegPos(match.getRegex(),match.getCount()));
//            }else if ((match.getMatchedIndex()+match.getMatchedString().length())==k){
//                result.add(new EndRegPos(match.getRegex(),match.getCount()));
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

    // region # 暂时不需要





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
        int partitionIndex = StringProcessor.lookupPartitionIndex(newInput, partitions);

        System.out.println("==========所属partition=" + partitionIndex + " ==========");
        ExamplePartition partition = partitions.get(partitionIndex);

        ExpressionGroup topNExpression = StringProcessor.getTopNExpressions(partition, newInput, 5);

        return topNExpression;
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
                System.out.println(((NonTerminalExpression) expression).interpret(v.getInputString()) + " , " + expression.toString());

//                if (v.getTargetString().equals(((NonTerminalExpression) expression).interpret(v.getInputString())))
            }
        }
    }

    // endregion
    private static List<ExamplePair> getExamplePairs() {

        // 对于提取IBM形式的句子，最后W的规模大致为3*(len(o))^2
        // 其他的subStr问题W的规模会小很多

        // FIXME 当前concatExp算法为指数型函数，一旦output中item数(比如用逗号隔开)增加以及每个item的长度变长，计算时间会爆炸增长。
        // FIXME: 2017/2/3 初步估计每个item延长一位会让concatResExp耗时翻倍，每增加一个item，就会导致concatResExp耗时乘以n倍
        List<ExamplePair> examplePairs = new ArrayList<ExamplePair>();
        // region # success
        // 提取结构化数据能力
//        examplePairs.add(new ExamplePair("Electronics Store,40.74260751,-73.99270535,Tue Apr 03 18:08:57 +0800 2012", "Electronics Store,Apr 03,18:08:57"));
//        examplePairs.add(new ExamplePair("Airport,40.77446436,-73.86970997,Sun Jul 15 14:51:15 +0800 2012", "Airport,Jul 15,14:51:15"));
//        examplePairs.add(new ExamplePair("Bridge,Tue Apr 03 18:00:25 +0800 2012", "Bridge,Apr 03"));
//        examplePairs.add(new ExamplePair("Arts & Crafts Store,40.71981038,-74.00258103,Tue Apr 03 18:00:09 +0800 2012", "Arts & Crafts Store,Apr 03,Tue"));
//
//        examplePairs.add(new ExamplePair("Wed Jul 11 11:17:44 +0800 2012,40.23213,German Restaurant", "German Restaurant,Jul 11"));
//        examplePairs.add(new ExamplePair("40.7451638,-73.98251878,Tue Apr 03 18:02:41 +0800 2012,Medical Center", "Medical Center,Apr 03"));

//        examplePairs.add(new ExamplePair("2016-09","2016-09"));
//        examplePairs.add(new ExamplePair("2016-12","2016-12"));

//        examplePairs.add(new ExamplePair("1-1","2017-1-1"));
//        examplePairs.add(new ExamplePair("08:16","2017-4-17 08:16"));


        // 单个较长output
//        examplePairs.add(new ExamplePair("Electronics Store,40.74260751,-73.99270535,Tue Apr 03 18:08:57 +0800 2012", "Electronics Store,Apr 03,Tue"));

        // 初级Loop能力
//        examplePairs.add(new ExamplePair("Hello World Zsf the Program Synthesis Electronics Airport Hello World Zsf the Program Synthesis Electronics Airport","HWZPSEAHWZPSEA"));
//        examplePairs.add(new ExamplePair("Hello World Zsf the Program Synthesis Electronics Airport Bridge","HWZPSEAB"));
//        examplePairs.add(new ExamplePair("the Association for the Advancement of Artificial Intelligence","AAAI"));
//        examplePairs.add(new ExamplePair("Association for Computing Machinery", "ACM"));
//        examplePairs.add(new ExamplePair("Shanghai Jiao Tong University", "SJTU"));

        examplePairs.add(new ExamplePair("ran.liu_cqu@qq.com", "qq.com"));
        examplePairs.add(new ExamplePair("lijia@cqu.edu.cn", "cqu.edu.cn"));
        examplePairs.add(new ExamplePair("15688888888", "手机"));
        examplePairs.add(new ExamplePair("", "缺失"));
        examplePairs.add(new ExamplePair("wqw AT cqu DOT edu DOT cn", "特殊"));

//        examplePairs.add(new ExamplePair("[252166]:2011-12-20,Tuesday", "252166,2011-12-20,Tuesday"));

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
//        testPairs.add(new ValidationPair("Coffee Shop,40.73340972,-74.00285648,Wed Jul 13 12:27:07 +0800 2012", "Coffee Shop,Jul 13"));
//        testPairs.add(new ValidationPair("Bridge,43,-73,Tue Apr 03 18:00:25 +0800 2012", "Coffee Shop,Jul 13"));

        testPairs.add(new ValidationPair("guoping@cqu.edu.cn","cqu.edu.cn"));
        testPairs.add(new ValidationPair("lijia@cqu.edu.cn","cqu.edu.cn"));
        testPairs.add(new ValidationPair("13320218299","手机"));
        testPairs.add(new ValidationPair("","缺失"));
        testPairs.add(new ValidationPair("wqw AT cqu DOT edu DOT cn","特殊"));


//        testPairs.add(new ValidationPair("guoping@cqu.edu.cn",""));
//        testPairs.add(new ValidationPair("guoping@cqu.edu.cn",""));
//
//        testPairs.add(new ValidationPair("40.69990191,,Sat Nov 17 20:36:26 +0800,Food & Drink Shop", "Food & Drink Shop,Nov 17"));
//        testPairs.add(new ValidationPair("40.74218831,-73.98792419,Park,Wed Jul 11 11:42:00 +0800 2012", "Park,Jul 11"));
//        testPairs.add(new ValidationPair("2125-11-02 23:50:00", "Park,Jul 11"));
//        testPairs.add(new ValidationPair("3-25", "2017-3"));
//        testPairs.add(new ValidationPair("08:19", "2017-4"));
//        testPairs.add(new ValidationPair("2016-07", "2016-07"));
//        testPairs.add(new ValidationPair("IRVING ST / 7TH AV","null"));
//        testPairs.add(new ValidationPair("1500 Block of CALIFORNIA ST","1500"));


//        // 初级Loop
//        testPairs.add(new ValidationPair("Foundation of Software Engineering","FSE"));
//        testPairs.add(new ValidationPair("European Software Engineering Conference","ESEC"));
//        testPairs.add(new ValidationPair("International Conference on Software Engineering","ICSE"));
//        // endregion
//
//
//        // region # error
//        testPairs.add(new ValidationPair("姓名：<span class=\"name\">Ran Liu</span> <br> 职称：<span class=\"zc\">Associate Professor/Senior Engineer</span><br> 联系方式：<span class=\"lxfs\">ran.liu_cqu@qq.com</span><br> 主要研究方向:<span class=\"major\">Medical and stereo image processing; IC design; Biomedical Engineering</span><br>","Ran Liu,Associate Professor/Senior Engineer,Medical and stereo image processing; IC design; Biomedical Engineering"));
//        testPairs.add(new ValidationPair("姓名：<span class=\"name\">陈波</span> <br> 职称：<span class=\"zc\"></span><br> 联系方式：<span class=\"lxfs\"></span><br> 主要研究方向:<span class=\"major\"></span><br>", ""));
//        testPairs.add(new ValidationPair("                        姓名：<span class=\"name\">陈自郁</span> <br> 职称：<span class=\"zc\">讲师</span><br> 联系方式：<span class=\"lxfs\">chenziyu@cqu.edu.cn</span><br> 主要研究方向:<span class=\"major\">群智能、图像处理和智能控制</span><br>", "讲师"));
//        testPairs.add(new ValidationPair("                        姓名：<span class=\"name\">但静培</span> <br> 职称：<span class=\"zc\">讲师</span><br> 联系方式：<span class=\"lxfs\"></span><br> 主要研究方向:<span class=\"major\">时间序列数据挖掘、计算智能、神经网络等</span><br>", "讲师"));
//
//
//        // FIXME: 2017/2/16 错误原因初步判定为相似度(classifier)错误
//        testPairs.add(new ValidationPair("2014年3月23日","3"));
//        testPairs.add(new ValidationPair("9/23/2012","09"));

        // FIXME: 2017/2/16 未知错误，运行时很久没有结果，可能在哪里死循环了，需要debug
//        testPairs.add(new ValidationPair("1234-2345-23", "1234-2345-23"));
//        testPairs.add(new ValidationPair("1.3213.02", "1-3213-02"));

        // FIXME: 2017/2/21 去掉注释

//        testPairs.add(new ValidationPair("System.out.println(\"Hello World!\"); //测试代码","System.out.println(\"Hello World!\"); "));
//        testPairs.add(new ValidationPair("40.69990191,//,Sat Nov 17 20:36:26 +0800,Food & Drink Shop","Food & Drink Shop,Nov 17"));
//        testPairs.add(new ValidationPair("40.74218831,-73.9879//2419,Park,Wed Jul 11 11:42:00 +0800 2012","Park,Jul 11"));

        // endregion
        return testPairs;
    }

    public static void main(String[] args) {
        List<ExamplePair> examplePairs = getExamplePairs();
        List<ValidationPair> testPairs = getTestPairs();

        StringProcessor stringProcessor = new StringProcessor();

        List<ResultMap> resultMaps = stringProcessor.generateExpressionsByExamples(examplePairs);
        List<ExpressionGroup> expressionGroups = stringProcessor.selectTopKExps(resultMaps, 10);
        List<ExamplePartition> partitions = stringProcessor.generatePartitions(expressionGroups, examplePairs);

        int count=0;
        for (ExamplePartition partition : partitions) {
            System.out.println("============"+count+++"================");
            for (ExamplePair examplePair : partition.getExamplePairs()) {
                System.out.println(examplePair);
            }
            for (Expression expression : partition.getUsefulExpression().getExpressions()) {
                System.out.println(expression);
            }
        }
        handleNewInput(testPairs, partitions);
    }
}
