package com.zsf;

import com.zsf.flashextract.FlashExtract;
import com.zsf.flashextract.field.Field;
import com.zsf.flashextract.tools.Color;

import java.util.List;

/**
 * Created by hasee on 2017/3/16.
 */
public class WebMain {

    public static void main(String[] args) {
        String inputDocument = "<div class=\"teacherdiv\">\n" +
                "                    <div style=\"position: relative;float:left;width:100px;height:140px;margin: 5px 5px\">\n" +
                "                        <a href=\"/public/tindex/30452\"><img src=\"/uploaded/filename/public/teacherportrait/30452.jpg?id=F856496028532ICUJO3\" style=\"width:100%;\"></a>\n" +
                "                    </div>\n" +
                "                    <div style=\"position: relative;float:left;width:220px;height:100%;padding-top: 20px;\">\n" +
                "                        姓名：<span class=\"name\">Ran Liu</span> <br> 职称：<span class=\"zc\">Associate Professor/Senior Engineer</span><br> 联系方式：<span class=\"lxfs\">ran.liu_cqu@qq.com</span><br> 主要研究方向:<span class=\"major\">Medical and stereo image processing; IC design; Biomedical Engineering</span><br>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "<div class=\"teacherdiv\">\n" +
                "                    <div style=\"position: relative;float:left;width:100px;height:140px;margin: 5px 5px\">\n" +
                "                        <a href=\"/public/tindex/30500\"><img src=\"/images/nophoto.jpg\" style=\"width:100%;\"></a>\n" +
                "                    </div>\n" +
                "                    <div style=\"position: relative;float:left;width:220px;height:100%;padding-top: 20px;\">\n" +
                "                        姓名：<span class=\"name\">陈波</span> <br> 职称：<span class=\"zc\"></span><br> 联系方式：<span class=\"lxfs\"></span><br> 主要研究方向:<span class=\"major\"></span><br>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "<div class=\"teacherdiv\">\n" +
                "                    <div style=\"position: relative;float:left;width:100px;height:140px;margin: 5px 5px\">\n" +
                "                        <a href=\"/public/tindex/06013\"><img src=\"/images/nophoto.jpg\" style=\"width:100%;\"></a>\n" +
                "                    </div>\n" +
                "                    <div style=\"position: relative;float:left;width:220px;height:100%;padding-top: 20px;\">\n" +
                "                        姓名：<span class=\"name\">陈自郁</span> <br> 职称：<span class=\"zc\">讲师</span><br> 联系方式：<span class=\"lxfs\">chenziyu@cqu.edu.cn</span><br> 主要研究方向:<span class=\"major\">群智能、图像处理和智能控制</span><br>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "<div class=\"teacherdiv\">\n" +
                "                    <div style=\"position: relative;float:left;width:100px;height:140px;margin: 5px 5px\">\n" +
                "                        <a href=\"/public/tindex/06167\"><img src=\"/uploaded/filename/public/teacherportrait/06167.jpg?id=F856496028533QU5XNG\" style=\"width:100%;\"></a>\n" +
                "                    </div>\n" +
                "                    <div style=\"position: relative;float:left;width:220px;height:100%;padding-top: 20px;\">\n" +
                "                        姓名：<span class=\"name\">但静培</span> <br> 职称：<span class=\"zc\">讲师</span><br> 联系方式：<span class=\"lxfs\"></span><br> 主要研究方向:<span class=\"major\">时间序列数据挖掘、计算智能、神经网络等</span><br>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "<div class=\"teacherdiv\">\n" +
                "                    <div style=\"position: relative;float:left;width:100px;height:140px;margin: 5px 5px\">\n" +
                "                        <a href=\"/public/tindex/30733\"><img src=\"/uploaded/filename/public/teacherportrait/30733.jpg?id=F856496028534QUAOIB\" style=\"width:100%;\"></a>\n" +
                "                    </div>\n" +
                "                    <div style=\"position: relative;float:left;width:220px;height:100%;padding-top: 20px;\">\n" +
                "                        姓名：<span class=\"name\">房斌</span> <br> 职称：<span class=\"zc\">教授　博士生导师</span><br> 联系方式：<span class=\"lxfs\"></span><br> 主要研究方向:<span class=\"major\">模式识别与图像处理</span><br>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "<div class=\"teacherdiv\">\n" +
                "                    <div style=\"position: relative;float:left;width:100px;height:140px;margin: 5px 5px\">\n" +
                "                        <a href=\"/public/tindex/30651\"><img src=\"/uploaded/filename/public/teacherportrait/30651.jpg?id=F856496028535AIQV0Y\" style=\"width:100%;\"></a>\n" +
                "                    </div>\n" +
                "                    <div style=\"position: relative;float:left;width:220px;height:100%;padding-top: 20px;\">\n" +
                "                        姓名：<span class=\"name\">葛亮</span> <br> 职称：<span class=\"zc\">副教授</span><br> 联系方式：<span class=\"lxfs\">geliang@cqu.edu.cn</span><br> 主要研究方向:<span class=\"major\">计算机视觉，数据挖据，Web应用技术</span><br>\n" +
                "                    </div>\n" +
                "                </div>";
        FlashExtract flashExtract=new FlashExtract(inputDocument);

//        List<Field> fieldList;
//        document.selectField(Color.BLUE,476,483,"Ran Liu");
//        fieldList=document.showSelectedFields();
//        showField(fieldList);
//
//        document.selectField(Color.BLUE,1197,1199,"陈波");
//        fieldList=document.showSelectedFields();
//        showField(fieldList);
//
//        document.selectField(Color.GREEN,516,551,"Associate Professor/Senior Engineer");
//        fieldList=document.showSelectedFields();
//        showField(fieldList);

        flashExtract.setRegionTitle(Color.BLUE,"姓名");
        flashExtract.setRegionTitle(Color.GREEN,"职称");

//        // FIXME: 2017/3/13 现在假设所有要提取的数据都处于同一行，不存在跨行的结构化数据
//        // 当region达到2个时，可以自动产生LineSelector并应用
//        if (flashExtract.needGenerateLineReions(color)){
//            // FIXME: 2017/3/14 这一整块的逻辑比较混乱，急需大规模重构
//            List<Regex> boolLineSelector=flashExtract.getLineSelector(color);
//            System.out.println(boolLineSelector);
//
//            // TODO: 2017/3/13 selector需要排序，排序后默认选择第一个并且应用
//            Regex curSelector=boolLineSelector.get(0);
//            // 然后根据selector选择LineRegion并且自动选择出所有应该小region(所有颜色)
//            flashExtract.selectRegionBySelector(curSelector);
//        }
//        int color3=3;
//        flashExtract.selectRegion(color3,5,214,284,"Medical and stereo image processing; IC design; Biomedical Engineering");
        // TODO 异常处理：选中的是needSelectesRegions以外的region就提示错误，忽略本次输入
    }

    private static void showField(List<Field> fieldList) {
        System.out.println("+++++++++++++++++++++++++++++++++");
        for (Field field:fieldList){
            System.out.println(String.format("%s,%s,%d,%d",field.getColor(),field.getText(),field.getBeginPos(),field.getEndPos()));
        }
        System.out.println("+++++++++++++++++++++++++++++++++");
    }
}
