package com.zsf.model;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.tool.WordSegmentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by hasee on 2017/2/5.
 */
public class ExampleCluster {
    private List<ExamplePair> examplePairs=new ArrayList<ExamplePair>();
    private ExpressionGroup usefulExpression=new ExpressionGroup();
    private int maxWordLength=0;

    public ExampleCluster(ExamplePair examplePair, ExpressionGroup usefulExpression) {
        examplePairs.add(examplePair);
        this.usefulExpression = usefulExpression;
    }

    public ExampleCluster(List<ExamplePair> examplePairs, ExpressionGroup usefulExpression) {
        this.examplePairs = examplePairs;
        this.usefulExpression = usefulExpression;
    }

    public void showDetails(boolean showExamples, boolean showExpressions) {
        if (showExamples){
            for (ExamplePair pair:examplePairs){
                System.out.println(String.format("Input= '%s' , Output='%s'",pair.getInputString(),pair.getOutputString()));
            }
        }
        if (showExpressions){
            for (Expression expression:usefulExpression.getExpressions()){
                System.out.println(expression.toString());
            }
        }
    }

    /**
     * 计算MaxWordLength,用于分词计算字符串相似度
     * 例: input="Electronics Store,40.74260751,-73.99270535,Tue Apr 03 18:08:57 +0800 2012"
     * 首先用分隔符(分隔符定义待定)拆分input，拆分后得到最长的词是-73.99270535 那么maxWordLength=12
     * @return
     */
    public void calculateMaxWordLength() {
        // TODO: 2017/2/6 这里的拆词还没有做,用哪种拆分方法需要看过数据才能确定。
        this.maxWordLength=12;
    }

    /**
     * 计算newInputString和当前partition的相似度
     * 计算和此partition中其他inputString相似度
     * 最后取中位数作为结果(可换)
     * @param newInputString
     * @return
     */
    public Double calculateSimilarity(String newInputString) {
        List<Double> scoreList=new ArrayList<Double>();
        for (ExamplePair pair:examplePairs){
            scoreList.add(WordSegmentation.calculateSimilarity(newInputString,pair.getInputString(),getMaxWordLength()));
        }

        if (scoreList.size()>0){
            return calculateMedian(scoreList);
        }else {
            return null;
        }
    }

    /**
     * 计算中位数
     * @param scoreList
     * @return
     */
    private Double calculateMedian(List<Double> scoreList) {
        Collections.sort(scoreList);
        int size=scoreList.size();
        if (size%2==0){
            return (scoreList.get(size/2-1)+scoreList.get(size/2))/2;
        }else {
            return scoreList.get(size/2);
        }
    }

    public int getMaxWordLength() {
        calculateMaxWordLength();
        return maxWordLength;
    }

    public List<ExamplePair> getExamplePairs() {
        return examplePairs;
    }

    public void setExamplePairs(List<ExamplePair> examplePairs) {
        this.examplePairs = examplePairs;
    }

    public ExpressionGroup getUsefulExpression() {
        return usefulExpression;
    }

    public void setUsefulExpression(ExpressionGroup usefulExpression) {
        this.usefulExpression = usefulExpression;
    }

    public void insert(ExampleCluster partition2) {

    }
}
