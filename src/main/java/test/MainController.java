package test;

import com.zsf.flashextract.model.FlashExtract;
import com.zsf.interpreter.expressions.regex.Regex;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hp on 2016/9/30.
 */
@Controller
@RequestMapping("/main")
public class MainController {

    String inputDocument = "";
    List<Integer> positiveLineIndex = new ArrayList<Integer>();
    List<Integer> negataiveLineIndex = new ArrayList<Integer>();

    FlashExtract flashExtract;

    @RequestMapping("/fun")
    public String fun() {
        return "hello";
    }

    @RequestMapping(value = "/upload_file", method = RequestMethod.POST)
    public ModelAndView uploadFile(@RequestParam(value = "file", required = false) MultipartFile file) {
        CommonsMultipartFile cf = (CommonsMultipartFile) file;
        DiskFileItem fi = (DiskFileItem) cf.getFileItem();

        // FIXME: 2017/3/13 读取文件并存储到disk上，再记录到数据库中
        File f = new File("D:\\MyProjectsRepertory\\JAVA_project\\FlashExtract\\data\\teacher.txt");
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line + "\n");
//                System.out.println(line);
            }
            inputDocument = builder.toString();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("inputDocument", inputDocument);
        flashExtract = new FlashExtract();
        flashExtract.setInputDocument(inputDocument);

        return new ModelAndView("handle_data", data);
    }

    @RequestMapping(value = "/select_region")
    public void selectRegion(int startPos, int endPos) {
        String selectedText = inputDocument.substring(startPos, endPos);

        String textBeforeSelect = inputDocument.substring(0, startPos);
        int lineIndex = getLineIndex(textBeforeSelect);

        int lineBeginTag = textBeforeSelect.lastIndexOf("\n");
        if (lineBeginTag <= 0) {
            // 如果上文不存在/n换行符的话，那么要和下面的操作相互抵消
            lineBeginTag = -2;
        }
        // TODO: 2017/3/13 将结果记录到FE中，
        // +2是因为textBeforeSelect截取到'/n'之前，还不包含/n
        int lineStartPos = startPos - (lineBeginTag + 2);
        int lineEndPos = endPos - (lineBeginTag + 2);
        flashExtract.doSelectRegion(curColor, lineIndex, lineStartPos, lineEndPos, selectedText);

        if (flashExtract.needGenerateLineReions(curColor)) {
            List<Regex> boolLineSelector = flashExtract.getLineSelector(curColor);
            System.out.println(boolLineSelector);
            int lineRegionColor = 0;
            // TODO: 2017/3/13 selector的排序
            flashExtract.selectRegionBySelector(boolLineSelector.get(0), lineRegionColor);
            // TODO: 产生LineSelector之后，自动在LineRegion中根据提供的例子产生childRegion
            // FIXME: 2017/3/14 这一整块的逻辑比较混乱，急需大规模重构
            flashExtract.generateChildRegionsInLineRegions(curColor);
        }
    }

    private int curColor;
    @RequestMapping(value = "set_color", method = RequestMethod.POST)
    public void setColor(int color) {
        System.out.println("setColor"+color);
        curColor=color;
    }

    /**
     * 根据beginPos之前的区域中出现的/n次数来判断选中的是第几行
     *
     * @param textBeforeSelect
     * @return
     */
    private int getLineIndex(String textBeforeSelect) {
        int count = 0;
        int index = 0;
        while (true) {
            index = textBeforeSelect.indexOf("\n", index + 1);
            if (index > 0) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
}
