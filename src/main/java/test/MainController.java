package test;

import com.google.gson.Gson;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import com.zsf.flashextract.model.FlashExtract;
import com.zsf.flashextract.region.newregion.MainDocument;
import com.zsf.flashextract.region.newregion.field.Field;
import com.zsf.flashextract.region.newregion.field.PlainField;
import com.zsf.flashextract.region.newregion.message.MessageSelectField;
import com.zsf.flashextract.region.newregion.tools.Color;
import com.zsf.interpreter.expressions.regex.Regex;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hp on 2016/9/30.
 */
@Controller
@RequestMapping("/main")
public class MainController {

    private String inputDocument = "";

    private MainDocument document;

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
        document = new MainDocument(inputDocument);


        return new ModelAndView("handle_data", data);
    }

    private MessageSelectField curSelectField;

    @RequestMapping(value = "/select_region")
    @ResponseBody
    public MessageSelectField selectRegion(int startPos, int endPos) {
        String selectedText = inputDocument.substring(startPos, endPos);
        document.selectField(curColor, startPos, endPos, selectedText);
        curSelectField = document.showSelectedFields();
        showField(curSelectField);

        return curSelectField;
    }

    @RequestMapping(value = "/to_scv")
    public ResponseEntity<byte[]> tableToCsv(HttpServletRequest request) {
        try {
            String basePath=request.getSession().getServletContext().getRealPath("output\\csv");
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat dateFormat= new SimpleDateFormat("MMddyyyyHHmmss");
            String fileName = basePath+File.separator+dateFormat.format(date) + ".csv";
            File dir=new File(basePath);
            if (!dir.exists()){
                dir.mkdirs();
            }
            FileWriter fileWriter = new FileWriter(fileName);
            CsvWriter writer = new CsvWriter(fileWriter, new CsvWriterSettings());

            writer.writeHeaders(curSelectField.getTitles());
            writer.writeRowsAndClose(curSelectField.getDataTables());

            File file=new File(fileName);
            String dfileName = new String(fileName.getBytes("UTF-8"),"iso-8859-1");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", dfileName);
            return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Color curColor = Color.BLUE;

    @RequestMapping(value = "set_color", method = RequestMethod.POST)
    public void setColor(int color) {
        curColor = Color.getColor(color);
        System.out.println("setColor:" + curColor.toString());
    }

    private void showField(MessageSelectField selectField) {
        System.out.println("+++++++++++++++++++++++++++++++++");
        for (Field field : selectField.getSelectedFields()) {
            System.out.println(String.format("%s,%s,%d,%d", field.getColor(), field.getText(), field.getBeginPos(), field.getEndPos()));
        }
        System.out.println("+++++++++++++++++++++++++++++++++");
    }
}
