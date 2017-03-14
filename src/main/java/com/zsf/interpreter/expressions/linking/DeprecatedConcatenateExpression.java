package com.zsf.interpreter.expressions.linking;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.NonTerminalExpression;
import com.zsf.interpreter.expressions.string.ConstStrExpression;
import com.zsf.interpreter.model.ExpressionGroup;

/**
 * Created by hasee on 2017/1/23.
 */
public class DeprecatedConcatenateExpression extends LinkingExpression {

    private Expression leftExp;
    private Expression rightExp;

    public DeprecatedConcatenateExpression(Expression leftExp, Expression rightExp) {
        this.leftExp = leftExp;
        this.rightExp = rightExp;
    }

    /**
     * 合并两个exp集合的工具函数
     * @param expressions1
     * @param expressions2
     * @return
     */
    public static ExpressionGroup concatenateExp(ExpressionGroup expressions1, ExpressionGroup expressions2) {
        ExpressionGroup linkedExpressions=new ExpressionGroup();
        for(Expression exp1:expressions1.getExpressions()){
            for (Expression exp2:expressions2.getExpressions()){
                if (exp1 instanceof ConstStrExpression && exp2 instanceof ConstStrExpression){
                    linkedExpressions.insert(new ConstStrExpression(((ConstStrExpression) exp1).getConstStr()+((ConstStrExpression) exp2).getConstStr()));
                }else {
                    linkedExpressions.insert(new DeprecatedConcatenateExpression(exp1,exp2));
                }
                // TODO: 2017/3/2 loop和其他值合併
            }
        }
        return linkedExpressions;
    }

    @Override
    public String toString() {
        return String.format("concat(%s,%s)",leftExp.toString(),rightExp.toString());
    }

    @Override
    public Expression deepClone() {
        return new DeprecatedConcatenateExpression(leftExp.deepClone(),rightExp.deepClone());
    }

    @Override
    public int deepth() {
        return leftExp.deepth()+rightExp.deepth();
    }

    @Override
    public String interpret(String inputString) {
        String ans="null";
        if (leftExp instanceof NonTerminalExpression && rightExp instanceof NonTerminalExpression){
            try {
                ans=((NonTerminalExpression) leftExp).interpret(inputString)+((NonTerminalExpression) rightExp).interpret(inputString);
            }catch (Exception e){
                return null;
            }
        }
        return ans;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeprecatedConcatenateExpression){
            return (leftExp.equals(((DeprecatedConcatenateExpression) obj).getLeftExp())&&rightExp.equals(((DeprecatedConcatenateExpression) obj).getRightExp()))
                    ||(leftExp.equals(((DeprecatedConcatenateExpression) obj).getRightExp())&&rightExp.equals(((DeprecatedConcatenateExpression) obj).leftExp));
        }
        return false;
    }

    @Override
    public double score() {
        // FIXME: 2017/3/2 concat(exp1,,concat(e2,e3))会导致score失真
        double score=(leftExp.score()+rightExp.score())/deepth();
        return score;
    }

    public Expression getLeftExp() {
        return leftExp;
    }

    public void setLeftExp(Expression leftExp) {
        this.leftExp = leftExp;
    }

    public Expression getRightExp() {
        return rightExp;
    }

    public void setRightExp(Expression rightExp) {
        this.rightExp = rightExp;
    }
}
