package test;

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
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
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
        Map<String ,Object> data=new HashMap<String,Object>();
        data.put("inputDocument",inputDocument);
        return new ModelAndView("redirect:/t/main",data);
    }


    @RequestMapping(value = "/select_region")
    public void selectRegion(int startPos,int endPos,int color) {
        System.out.println(inputDocument.substring(startPos,endPos));
    }


}
