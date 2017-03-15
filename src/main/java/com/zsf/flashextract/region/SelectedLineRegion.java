package com.zsf.flashextract.region;

import com.zsf.StringProcessor;
import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.NonTerminalExpression;
import com.zsf.interpreter.model.ExamplePair;
import com.zsf.interpreter.model.ExpressionGroup;
import com.zsf.interpreter.model.ResultMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 表示被某种规则选中的Region,
 * parent表示它的所属(如果是完整的line那么parent为null)
 * beginPos表示他在parent中的位置，endPos类似
 * text表示这个region的内容
 * color表示他被绘制上的颜色(如果是line的话则是一个特殊的框框)
 *
 * FIXME 现在这是一个简化模型，只针对提取出的Line而设置，而且假设Line中不存在嵌套结构
 *
 * Created by zsf on 2017/3/13.
 */
public class SelectedLineRegion extends Region {

    private int color;
    /**
     * lineIndex用于指明此行数据在document中的行书，是专门针对这个简化版的实现方案而设置的，完整版不应该存在
     */
    private int lineIndex;
    private HashMap<Integer,ExpressionGroup> childRegionsMap= new HashMap<Integer, ExpressionGroup>();

    public SelectedLineRegion(Region parentRegion, int beginPos, int endPos, String text, int color,int lineIndex) {
        super(parentRegion, beginPos, endPos, text);
        this.color = color;
        this.lineIndex=lineIndex;
    }

    public ExpressionGroup selectChildRegion(int color,String targetString){
        //调用FF input为本地text, output为target, 返回一组有效的Expressions

        StringProcessor stringProcessor=new StringProcessor();
        List<ExamplePair> examplePairs=new ArrayList<ExamplePair>();
        examplePairs.add(new ExamplePair(getText(),targetString));

        List<ResultMap> resultMaps=stringProcessor.generateExpressionsByExamples(examplePairs);
        ExpressionGroup expressionGroup=stringProcessor.selectTopKExps(resultMaps,10);

        return expressionGroup;
    }

    public void setColorfulRegionExpressions(int color, ExpressionGroup expressionGroup) {
        childRegionsMap.put(color,expressionGroup);
        // 上色(可能应该在外部操作)
        Expression expression=expressionGroup.getExpressions().get(0);
        if (expression instanceof NonTerminalExpression){
            System.out.println(((NonTerminalExpression) expression).interpret(getText()));
        }
    }

    public int getLineIndex() {
        return lineIndex;
    }

    public void setLineIndex(int lineIndex) {
        this.lineIndex = lineIndex;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
