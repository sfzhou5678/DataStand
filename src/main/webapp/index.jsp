<%@ page pageEncoding="UTF-8" %>
<html>
<script src="http://handsontable.github.io/handsontable-ruleJS/lib/jquery/jquery-1.10.2.js"></script>
<script src="http://handsontable.github.io/handsontable-ruleJS/lib/handsontable/handsontable.full.js"></script>
<link rel="stylesheet" media="screen"
      href="http://handsontable.github.io/handsontable-ruleJS/lib/handsontable/handsontable.full.css">
<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/lodash/lodash.js"></script>
<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/underscore.string/underscore.string.js"></script>
<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/moment/moment.js"></script>
<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/numeral/numeral.js"></script>
<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/numericjs/numeric.js"></script>
<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/js-md5/md5.js"></script>
<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/jstat/jstat.js"></script>
<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/formulajs/formula.js"></script>
<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/js/parser.js"></script>
<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/js/ruleJS.js"></script>
<script src="http://handsontable.github.io/handsontable-ruleJS/lib/handsontable/handsontable.formula.js"></script>

<script type="text/javascript" src="../../resource/js/jquery-1.9.1.js"></script>
<style>
    .text-center {
        text-align: center;
    }

    .info h2 {
        font-size: 36px;
    }

    .info h3 {
        font-size: 24px;
    }

    .uploadBtn {
        margin: 0 auto;
        border: 0;
        background: #228B22;
        color: white;
        font-family: "Microsoft YaHei UI";
        font-size: 24px;
        padding: 10px 30px;
    }

    .uploadBtn:hover {
        cursor: pointer;
        background: #FF4040;
        transition: background-color 0.2s;
    }

    .images {
        position: relative;
        text-align: center
    }

    .img-center {
        position: absolute;
        left: 15%;
        top: 50px;
    }

    .img-left {
        position: absolute;
        left: 5%;
        top: 170px;
    }

    .img-right {
        position: absolute;
        left: 55%;
        top: 200px;
    }
</style>
<body>

<div class="container">
    <div class="contained"
         style=" text-align: center; padding-top:10px;background: url(resources/images/back.jpg) no-repeat; width: 100%; height: 760px;background-size:cover; ">
        <div class="info text-center" style="font-family: 'Microsoft YaHei UI'">
            <h2>面向数据处理的程序合成器</h2>
            <h3>运用样例编程技术，精准理解意图，毫秒级响应速度</h3>
            <h3>批量处理数据，结果实时反馈，一键导出成果</h3>
        </div>
        <form action="main/upload_file" style="display: none;" method="post" enctype="multipart/form-data">
            <input id="hidden-file-uploader" type="file" name="file" onchange="uploaderChanged()">
            <input id="submit-btn" type="submit" value="提交">
        </form>
        <div style="width:100%; text-align: center">
            <button class="uploadBtn" onclick='$("#hidden-file-uploader").click()'>选择文件</button>
        </div>
        <div class="images" style="width: 100%">
            <img class="img-left" style="text-align: center" alt="Overleaf editor" width="650px"
                 src="resources/images/2.png">
            <img class="img-right" alt="Overleaf universities portal" width="650px" src="resources/images/1.png">
            <img class="img-center" alt="Overleaf templates gallery" width="1000px" src="resources/images/4.png">
        </div>
    </div>
</div>

<script type="text/javascript">
    function uploaderChanged() {
        $("#submit-btn").click();
    }
</script>
</body>
</html>
