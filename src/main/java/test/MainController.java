package test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import com.zsf.flashextract.FlashExtract;
import com.zsf.flashextract.field.Field;
import com.zsf.flashextract.message.MessageContainer;
import com.zsf.flashextract.tools.Color;
import com.zsf.interpreter.StringProcessor;
import com.zsf.interpreter.expressions.Expression;
import com.zsf.interpreter.model.ExamplePair;
import com.zsf.interpreter.model.ExpressionGroup;
import com.zsf.interpreter.model.ResultMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
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

    private FlashExtract flashExtract;

    @RequestMapping("/fun")
    public String fun() {
        return "hello";
    }

    @RequestMapping(value = "/upload_file", method = RequestMethod.POST)
    public ModelAndView uploadFile(HttpServletRequest request,
                                   @RequestParam(value = "file", required = false) MultipartFile partFile) {
        String basePath = request.getSession().getServletContext().getRealPath("upload" + File.separator + "files");
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyyHHmmss");
        Date date = new Date(System.currentTimeMillis());

        if (partFile.isEmpty()) {
            // TODO: 2017/3/27 还是要换成ajax 给出错误提示(1. 未上传文件 2. 文件类型不对 3. 文件过大 20M+)
            return new ModelAndView("redirect:/");
        }
        String fileOriginalName = partFile.getOriginalFilename();
        String newFileName = dateFormat.format(date) + fileOriginalName.substring(fileOriginalName.lastIndexOf("."));
        File file = new File(basePath + File.separator + newFileName);

        //文件写入磁盘
        try {
            // TODO: 2017/3/19 可以加一个md5判断？
            partFile.transferTo(file);

            StringBuilder builder = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }
                inputDocument = builder.toString();
                inputDocument = StringEscapeUtils.unescapeHtml4(inputDocument);
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
            flashExtract = new FlashExtract(inputDocument);

            return new ModelAndView("handle_data", data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ModelAndView("redirect:/");
    }

    private MessageContainer curSelectField;

    @RequestMapping(value = "/select_region")
    @ResponseBody
    public MessageContainer selectRegion(int startPos, int endPos) {
        String selectedText = inputDocument.substring(startPos, endPos);
        flashExtract.selectField(curColor, startPos, endPos, selectedText);
        curSelectField = flashExtract.showSelectedFields();
//        showField(curSelectField);

        return curSelectField;
    }

    @RequestMapping(value = "/edit_header", method = RequestMethod.POST)
    @ResponseBody
    public List<String> editHeader(int colorNum, String newHeader) {
        System.out.println("editHeader" + colorNum);
        Color color = Color.getColor(colorNum);
        flashExtract.setRegionTitle(color, newHeader);

        List<String> titles = flashExtract.getMessageContainer().getTitles();
        return titles;
    }

    @RequestMapping(value = "/getColData", method = RequestMethod.POST)
    @ResponseBody
    public List<String> getColData(int colorNum) {
        Color color = Color.getColor(colorNum);
        List<String> datas = flashExtract.getDatasByColor(color);

        return datas;
    }

    @RequestMapping(value = "/learnByExamples", method = RequestMethod.POST)
    @ResponseBody
    public List<String> learnByExamples(int colorNum, String jsonExamplePairs) {
        Color color = Color.getColor(colorNum);
        Gson gson=new Gson();
        List<ExamplePair> examplePairs=gson.fromJson(jsonExamplePairs,new TypeToken<List<ExamplePair>>(){}.getType());

        StringProcessor stringProcessor = new StringProcessor();
        List<ResultMap> resultMaps = stringProcessor.generateExpressionsByExamples(examplePairs);
        ExpressionGroup expressionGroup = stringProcessor.selectTopKExps(resultMaps, 10);
        ExpressionGroup bestExpressions=flashExtract.sortExpsAccSceneByColor(color,expressionGroup,5);

        // 现在只需要fields中的text，但是未来可能会用到fileds中的pos信息，所以先保留下来
        List<String> previewDatas=flashExtract.extraFFByColor(color,bestExpressions.getExpressions().get(0));

        return previewDatas;
    }

    @RequestMapping(value = "/to_scv")
    public ResponseEntity<byte[]> tableToCsv(HttpServletRequest request) {
        try {
            String basePath = request.getSession().getServletContext().getRealPath("output" + File.separator + "csv");
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyyHHmmss");
            String fileName = basePath + File.separator + dateFormat.format(date) + ".csv";
            File dir = new File(basePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            fileOutputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            CsvWriter csvWriter = new CsvWriter(fileOutputStream, "utf-8", new CsvWriterSettings());
            csvWriter.writeHeaders(curSelectField.getTitles());
            csvWriter.writeRowsAndClose(curSelectField.getDataTables());

            File file = new File(fileName);
            String dfileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", dfileName);
            return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 0号颜色作为未来背景色，暂时不用 所以color的编号是从1(Blue)开始的
    private Color curColor = Color.BLUE;

    @RequestMapping(value = "/set_color", method = RequestMethod.POST)
    public void setColor(int color) {
        curColor = Color.getColor(color);
        System.out.println("setColor:" + curColor.toString());
    }

    private void showField(MessageContainer selectField) {
        System.out.println("+++++++++++++++++++++++++++++++++");
        for (Field field : selectField.getSelectedFields()) {
            System.out.println(String.format("%s,%s,%d,%d", field.getColor(), field.getText(), field.getBeginPos(), field.getEndPos()));
        }
        System.out.println("+++++++++++++++++++++++++++++++++");
    }
}
