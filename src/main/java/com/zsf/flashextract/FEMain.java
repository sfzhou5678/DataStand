package com.zsf.flashextract;

import com.zsf.flashextract.model.FlashExtract;
import com.zsf.flashextract.region.Region;
import com.zsf.interpreter.expressions.regex.Regex;

import java.util.List;

/**
 * Created by zsf on 2017/2/26.
 */
public class FEMain {


    public static void main(String[] args) {
//        String inputDocument="<HTML>\n" +
//                "<body>\n" +
//                "<table>\n" +
//                "<tr><td>Name</td><td>Email</td><td>Office</td></tr>\n" +
//                "<tr><td>Russell Smith</td><td>Russell.Smith@contoso.com</td><td>London</td></tr>\n" +
//                "<tr><td>David Jones</td><td>David.Jones@contoso.com</td><td>Manchester</td></tr>\n" +
//                "<tr><td>John Cameron</td><td>John.Cameron@contoso.com</td><td>New York</td></tr>\n" +
//                "</table>\n" +
//                "</body>\n" +
//                "</HTML>";

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

        FlashExtract flashExtract=new FlashExtract();
        flashExtract.setInputDocument(inputDocument);

        int color=1;
        flashExtract.doSelectRegion(color,5,45,52,"Ran Liu");
        flashExtract.doSelectRegion(color,13,45,48,"陈波");

//        flashExtract.doSelectRegion(color,4,7,21,"Russell Smith");
//        flashExtract.doSelectRegion(color,5,7,19,"John Cameron");

        // FIXME: 2017/3/13 现在假设所有要提取的数据都处于同一行，不存在跨行的结构化数据
        // TODO: 2017/3/14 下面几个方法可以整合到一起
        // 当region达到2个时，可以自动产生LineSelector并应用
        if (flashExtract.needGenerateLineReions(color)){
            List<Regex> boolLineSelector=flashExtract.getLineSelector(color);
            System.out.println(boolLineSelector);
            int lineRegionColor=0;
            // TODO: 2017/3/13 selector的排序
            flashExtract.selectRegionBySelector(boolLineSelector.get(0),lineRegionColor);
            // TODO: 产生LineSelector之后，自动在LineRegion中根据提供的例子产生childRegion
            // FIXME: 2017/3/14 这一整块的逻辑比较混乱，急需大规模重构
            flashExtract.generateChildRegionsInLineRegions(color);
        }

//        showRegionNeedSelect(flashExtract.getDocumentRegions(), boolLineSelector);

        int color2=2;
        flashExtract.doSelectRegionInLineRegions(color2,5,86,121,"Associate Professor/Senior Engineer");

        int color3=3;
        // TODO if has lineSelector: call FF.extract() else doSelectRegion
        // FF.extract: 根据input(selectedTextRegion)和output(mouseSecletedRegion)产生program
        // 再对其他所有lineSelector选出的needSelectedsRegion使用program，并且标注
        flashExtract.doSelectRegionInLineRegions(color3,5,214,284,"Medical and stereo image processing; IC design; Biomedical Engineering");

        // TODO 异常处理：选中的是needSelectesRegions以外的region就提示错误，忽略本次输入
    }

    /**
     * 根据boolLineSelector对documentRegion进行筛选，输出(显示)需要显示的行
     * @param documentRegions
     * @param boolLineSelector
     */
    private static void showRegionNeedSelect(List<Region> documentRegions, List<Regex> boolLineSelector) {
        for (Regex selector:boolLineSelector){
            System.out.println("========="+"selector:"+selector.toString()+"=========");
            for (Region region:documentRegions){
                if (region.canMatch(selector)){
                    System.out.println(region.getText());
                }
            }
        }
    }

}
