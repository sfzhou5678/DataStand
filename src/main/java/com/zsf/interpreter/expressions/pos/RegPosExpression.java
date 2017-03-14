package com.zsf.interpreter.expressions.pos;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.model.Match;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/1/22.
 */
public class RegPosExpression extends PosExpression {

    private Regex r1;
    private Regex r2;
    private int c;

    public RegPosExpression(Regex r1, Regex r2, int c) {
        this.r1 = r1;
        this.r2 = r2;
        this.c = c;
    }

    @Override
    public String toString() {
        return String.format("regPos(%s,%s,%d)",r1.toString(),r2.toString(),c);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RegPosExpression){
            // FIXME: 2017/2/5 equal判定加上c可能会导致无法生成Loop，需要注意
            return r1.equals(((RegPosExpression) obj).getR1())&&r2.equals(((RegPosExpression) obj).getR2())&&c==((RegPosExpression) obj).getC();
        }
        return false;
    }

    /**
     * 要找到第c个，左边是r1，右边是r2的位置。
     * 如Input="Electronics Store"
     * 有Exp="regPos(UpperToken,LowerToken,1)"
     * 那么matches1能匹配到E和S
     * matchers2能匹配到lectronics和tore
     *
     * 第一个m1的matchedIndex=0，length=1 ，第一个m2的matchedIndex=1，所以这就是第一个匹配(也是答案)
     *
     * @param inputString
     * @return
     */
    @Override
    public int interpret(String inputString) {
        List<Match> matches1=r1.doMatch(inputString);
        List<Match> matches2=r2.doMatch(inputString);
        int count=0;
        int total=0;
        List<Integer> posList=new ArrayList<Integer>();
        for (Match m1: matches1){
            for (Match m2:matches2){
                int leftEndPos=m1.getMatchedIndex()+m1.getMatchedString().length();
                int rightBeginPos=m2.getMatchedIndex();
                if(leftEndPos<rightBeginPos){
                    break;
                }
                if (leftEndPos==rightBeginPos){
                    count++;
                    total++;
                    if (count>0&&count==c){
                        return rightBeginPos;
                    }
                    posList.add(rightBeginPos);
                }
            }
        }
        if (c<0){
            // 假设 input="Hello World Zsf"
            // 现有regPos(UpperToken,LowerToken,-3) ,total=3 所以regPos(UpperToken,LowerToken,-3)等价于regPos(UpperToken,LowerToken,1)
            // 即第一个左侧(左开)为大写，右侧(右闭)为小写的位置，即index=1
            int contraPos=c+(total);
            if (contraPos>=0&&contraPos<posList.size()){
                return posList.get(contraPos);
            }
        }
        return PosExpression.ILLEGAL_POS;
    }

    @Override
    public double score() {
        return 0.2+(r1.score()+r2.score())/2.0;
    }

    @Override
    public Expression deepClone() {
        return new RegPosExpression(r1,r2,c);
    }

    @Override
    public int deepth() {
        return 1;
    }

    public Regex getR1() {
        return r1;
    }

    public void setR1(Regex r1) {
        this.r1 = r1;
    }

    public Regex getR2() {
        return r2;
    }

    public void setR2(Regex r2) {
        this.r2 = r2;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }


}
