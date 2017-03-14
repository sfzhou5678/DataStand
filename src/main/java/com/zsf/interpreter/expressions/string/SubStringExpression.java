package com.zsf.interpreter.expressions.string;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.pos.PosExpression;

/**
 * Created by hasee on 2017/1/22.
 */
public class SubStringExpression extends StringExpression {
    private PosExpression posExpression1;
    private PosExpression posExpression2;

    public SubStringExpression(PosExpression posExpression1, PosExpression posExpression2) {
        this.posExpression1 = posExpression1;
        this.posExpression2 = posExpression2;
    }

    @Override
    public String interpret(String inputString) {
        int len=inputString.length();

        int pos1=posExpression1.interpret(inputString);
        int pos2=posExpression2.interpret(inputString);
        // pos=-1就表示len-1位，如input="abcde", input.subStr(1,-1)=input.subStr(1,4)="bcd"
        // END_POS表示最后一位,如果如果碰到END_POS，就把pos转换为input.length
        pos1=pos1<0?pos1+len:pos1;
//        pos1=pos1==PosExpression.END_POS?len:pos1;
        pos2=pos2<0?pos2+len:pos2;
        pos2=pos2==PosExpression.END_POS?len:pos2;

        if (isIllegalPos(inputString,pos1,pos2)){
//            System.out.println("ILLEGAL_POS");
            return null;
        }else {
            String ans="";
            try {
                ans=inputString.substring(pos1,pos2);
            }catch (Exception e){
                // FIXME: 2017/2/3 例：Input="bc abcd" output="abc" 现在的算法有时会导致index=4(a) 连接到index=0(bc)上去
//                System.out.println("SubString发生未知错误");
                return null;
            }
            return ans;
        }
    }

    private boolean isIllegalPos(String inputString, int pos1, int pos2) {
        if (pos1<0||pos1>inputString.length() && pos2<0||pos2>inputString.length()){
            return true;
        }
        if (pos1>=pos2){
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("substr(%s,%s)", posExpression1.toString(), posExpression2.toString());
    }

    @Override
    public Expression deepClone() {
        return new SubStringExpression((PosExpression) posExpression1.deepClone(), (PosExpression) posExpression2.deepClone());
    }

    @Override
    public int deepth() {
        return 1;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SubStringExpression){
            return posExpression1.equals(((SubStringExpression) obj).getPosExpression1())
                    && posExpression2.equals(((SubStringExpression) obj).getPosExpression2());
        }
        return false;
    }

    public PosExpression getPosExpression1() {
        return posExpression1;
    }

    public void setPosExpression1(PosExpression posExpression1) {
        this.posExpression1 = posExpression1;
    }

    public PosExpression getPosExpression2() {
        return posExpression2;
    }

    public void setPosExpression2(PosExpression posExpression2) {
        this.posExpression2 = posExpression2;
    }


    @Override
    public double score() {
        double score=0.5+(posExpression1.score()+posExpression2.score())/2.0;
        return score;
    }
}
