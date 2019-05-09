package com.zsf.interpreter.expressions.loop;

import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.expressions.NonTerminalExpression;
import com.zsf.interpreter.expressions.pos.PosExpression;
import com.zsf.interpreter.expressions.regex.Regex;
import com.zsf.interpreter.expressions.string.RegSubStringExpression;
import com.zsf.model.Match;

import java.util.List;

/**
 * Created by hasee on 2017/1/23.
 */
public class LoopExpression extends NonTerminalExpression {

    public static final String LINKING_MODE_CONCATENATE = "concat";

    /**
     * 要通过某种方式吧Loop的结果连接起来
     * 默认为concatenate连接字符串
     * 如果有需要也可以拓展出AddExpression或SbuExpression等
     */
    private String linkingMode;

    /**
     * baseExpression表示for的循环主体
     * 在IBM中就是SbuStr2(UppderToken,w)
     */
    private Expression baseExpression;

    // 因为for的index通常是一个等差数列，下面3个就代表了begin:stepsize:end
    // TODO: 2017/3/2 beginNode应该改成beginCount (他应该代表第一次出现，第二次出现而不是具体的位置)
    private int startCount = 0;
    private int stepSize = 1;
    private int endCount = 0;
    private int maxMatchesCount = 0;
    private int totalExpsCount = 0;

    public LoopExpression() {
        this.linkingMode = LINKING_MODE_CONCATENATE;
    }

    public LoopExpression(String linkingMode, Expression baseExpression, int startCount, int stepSize, int endCount, int maxMatchesCount, int totalExpsCount) {
        this.linkingMode = linkingMode;
        this.baseExpression = baseExpression;
        this.startCount = startCount;
        this.stepSize = stepSize;
        this.endCount = endCount;
        this.maxMatchesCount = maxMatchesCount;
        this.totalExpsCount = totalExpsCount;
    }

    @Override
    public String toString() {
        // Loop(concat(subStr2(UpperTok,0,1,3)))
        // TODO: 2017/1/23 Loop是否只适用于substr2？如果不是的话，这个toString还有待斟酌
        String ans = "";
        if (baseExpression == null) {
            System.out.println("Loop(error)");
        }
        if (baseExpression instanceof RegSubStringExpression) {
            if (endCount == PosExpression.END_POS) {
                ans = String.format("Loop(%s(%s(%s,%d,%d,%s)))", linkingMode,
                        "regSubStr", ((RegSubStringExpression) baseExpression).getRegex().getRegexName(),
                        startCount, stepSize, "END");
            } else {
                ans = String.format("Loop(%s(%s(%s,%d,%d,%d)))", linkingMode,
                        "regSubStr", ((RegSubStringExpression) baseExpression).getRegex().getRegexName(),
                        startCount, stepSize, endCount);
            }
        } else {
            ans = String.format("Loop(%s(%s))", linkingMode, baseExpression.toString());
        }
        return ans;
    }

    @Override
    public Expression deepClone() {
        return new LoopExpression(linkingMode, baseExpression, startCount, stepSize, endCount, maxMatchesCount, totalExpsCount);
    }

    @Override
    public String interpret(String inputString) {
        // TODO: 2017/3/2 改成count形式
        String ans = "";
        if (baseExpression instanceof RegSubStringExpression) {
            // TODO: 2017/2/16 ans+=的方式有问题，改成linkedExp
            Regex regex = ((RegSubStringExpression) baseExpression).getRegex();
            List<Match> matches = regex.doMatch(inputString);
            int endCountCopy = endCount < 0 ? endCount + matches.size()+1 : endCount;
            int startCountCopy = startCount < 0 ? startCount + matches.size()+1 : startCount;
            try {
                for (int i = startCountCopy - 1; i < endCountCopy; i += stepSize) {
                    ans += matches.get(i).getMatchedString();
                }
            } catch (IndexOutOfBoundsException e) {
                // 有些Loop不是通用的，比如Loop(1,1,4)有时候就会越界
                ans = null;
            }
        }
        return ans;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LoopExpression) {
            // 这里使用反射机制判断baseExp是否属于同个类，可能会影响运行速度？
            return ((LoopExpression) obj).getBaseExpression().getClass().equals(this.baseExpression.getClass()) &&
                    startCount == ((LoopExpression) obj).getStartCount() &&
                    endCount == ((LoopExpression) obj).getEndCount() &&
                    stepSize == ((LoopExpression) obj).getStepSize();
        }
        return false;
    }

    @Override
    public int deepth() {
        return 1;
    }

    @Override
    public double score() {
        return 0.15 + baseExpression.score() / 2 + totalExpsCount * 0.1;
    }


    public void addNode(Expression expression) {
        if (expression instanceof RegSubStringExpression) {
            int count = ((RegSubStringExpression) expression).getC();
            totalExpsCount++;
            if (totalExpsCount == 1) {
                startCount = count;
                this.maxMatchesCount = ((RegSubStringExpression) expression).getTotalC();
            }
            endCount = count;
            this.baseExpression = expression;
        } else if (expression instanceof LoopExpression) {
            int count = ((LoopExpression) expression).getEndCount();
            endCount = count;
            totalExpsCount += ((LoopExpression) expression).getTotalExpsCount();
            if (totalExpsCount == 1) {
                startCount = count;
            }
            this.baseExpression = ((LoopExpression) expression).getBaseExpression();
        }
        endCount = transformInteger(endCount, maxMatchesCount);
        startCount = transformInteger(startCount, maxMatchesCount);
        updateStepSize();

    }

    private void updateStepSize() {
        if (totalExpsCount == 1) {
            stepSize = 0;
        } else {
            int end = endCount < 0 ? endCount + (1 + maxMatchesCount) : endCount;
            int start = startCount < 0 ? startCount + (1 + maxMatchesCount) : startCount;
            stepSize = (end - start) / (totalExpsCount - 1);
        }
    }

    /**
     * 将序号count根据在total中所处的位置转换成正数或倒数形式
     *
     * @param count
     * @param total
     * @return
     */
    private int transformInteger(int count, int total) {
        if (count <= (total + 1) / 2) {
            return count;
        } else {
            return count - (total + 1);
        }
    }

    /**
     * 判断一个新的Exp是否合法，假设expression只可能是Loop或者substr2
     *
     * @param expression
     * @return
     */
    public boolean isLegalExpression(Expression expression) {
        // TODO 因为本程序中Loop是从前往后产生的，所以curStart一定要>=endCount(上一个结束的Node)
        // TODO: 2017/3/2 还有更多判断，后期处理
        if (!(expression instanceof LoopExpression || expression instanceof RegSubStringExpression)) {
            // 暂时假设新的exp必须是loop或者substr2
            return false;
        }
        if (baseExpression == null) {
            // FIXME: 2017/4/16 这段的意义忘了，可能会导致bug？
            if (expression instanceof LoopExpression) {
                baseExpression = ((LoopExpression) expression).getBaseExpression();
            } else if (expression instanceof RegSubStringExpression) {
                baseExpression = expression;
            }
        }
        if (expression instanceof RegSubStringExpression) {
            int count = ((RegSubStringExpression) expression).getC();
            if (count <= endCount) {
                return false;
            }
            return isSameExpressionInLoop(baseExpression, expression);
        } else if (expression instanceof LoopExpression) {
            return isSameExpressionInLoop(baseExpression, expression);
        }
        return true;
    }

    /**
     * 专门为Loop打造的same判别函数
     * <p>
     * same的定义：
     * constStr要求str相同
     * 普通Expression要求token相同
     * linkingExpression要求左右两边的普通Expression相同(如果linkingExpression左右均为LinkingExpression，)
     * <p>
     * FIXME: 假设所有baseExpression只可能是Substr2,expression只可能是substr2或者LoopExp
     *
     * @param baseExpression
     * @param expression
     * @return
     */
    private boolean isSameExpressionInLoop(Expression baseExpression, Expression expression) {
        if (expression instanceof LoopExpression) {
            return baseExpression.equals(((LoopExpression) expression).getBaseExpression());
        } else if (expression instanceof RegSubStringExpression) {
            return ((RegSubStringExpression) expression).loopEquals(baseExpression);
        }
        return false;
    }

    public int getTotalExpsCount() {
        return totalExpsCount;
    }

    public void setTotalExpsCount(int totalExpsCount) {
        this.totalExpsCount = totalExpsCount;
    }

    public Expression getBaseExpression() {
        return baseExpression;
    }

    public void setBaseExpression(Expression baseExpression) {
        this.baseExpression = baseExpression;
    }

    public String getLinkingMode() {
        return linkingMode;
    }

    public void setLinkingMode(String linkingMode) {
        this.linkingMode = linkingMode;
    }

    public static String getLinkingModeConcatenate() {
        return LINKING_MODE_CONCATENATE;
    }

    public int getStartCount() {
        return startCount;
    }

    public void setStartCount(int startCount) {
        this.startCount = startCount;
    }

    public int getEndCount() {
        return endCount;
    }

    public void setEndCount(int endCount) {
        this.endCount = endCount;
    }

    public int getStepSize() {
        return stepSize;
    }

    public void setStepSize(int stepSize) {
        this.stepSize = stepSize;
    }


}
