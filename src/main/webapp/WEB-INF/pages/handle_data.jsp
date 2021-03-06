<%--
  Created by IntelliJ IDEA.
  User: hp
  Date: 2016/10/1
  Time: 17:14
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>

<head>
    <title>Title</title>

        
    <%--<script src="http://handsontable.github.io/handsontable-ruleJS/lib/jquery/jquery-1.10.2.js"></script>--%>
    <%--<script src="http://handsontable.github.io/handsontable-ruleJS/lib/handsontable/handsontable.full.js"></script>--%>
    <%--<link rel="stylesheet" media="screen"--%>
          <%--href="http://handsontable.github.io/handsontable-ruleJS/lib/handsontable/handsontable.full.css">--%>
    <%--<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/lodash/lodash.js"></script>--%>
    <%--<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/underscore.string/underscore.string.js"></script>--%>
    <%--<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/moment/moment.js"></script>--%>
    <%--<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/numeral/numeral.js"></script>--%>
    <%--<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/numericjs/numeric.js"></script>--%>
    <%--<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/js-md5/md5.js"></script>--%>
    <%--<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/jstat/jstat.js"></script>--%>
    <%--<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/lib/formulajs/formula.js"></script>--%>
    <%--<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/js/parser.js"></script>--%>
    <%--<script src="http://handsontable.github.io/handsontable-ruleJS/lib/RuleJS/js/ruleJS.js"></script>--%>
    <%--<script src="http://handsontable.github.io/handsontable-ruleJS/lib/handsontable/handsontable.formula.js"></script>--%>

    <%--&lt;%&ndash;dialog相关&ndash;%&gt;--%>
    <%--<link rel="stylesheet" media="screen"--%>
          <%--href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap3-dialog/1.34.7/css/bootstrap-dialog.min.css">--%>
    <%--<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap3-dialog/1.34.7/js/bootstrap-dialog.min.js"></script>--%>

    <%--<link rel="stylesheet" href="http://cdn.static.runoob.com/libs/bootstrap/3.3.7/css/bootstrap.min.css">--%>
    <%--<script src="http://cdn.static.runoob.com/libs/bootstrap/3.3.7/js/bootstrap.min.js"></script>--%>


    <script type="text/javascript" src="../resources/js/jquery-1.9.1.js"></script>
    <%--table--%>
    <script src="../resources/js/handsontable.full.js"></script>
    <link rel="stylesheet" media="screen" href="../resources/css/handsontable.full.css">

    <%--dialog--%>
    <link rel="stylesheet" media="screen" href="../resources/css/bootstrap-dialog.min.css">
    <script src="../resources/js/bootstrap-dialog.min.js"></script>


    <script src="../resources/js/bootstrap.min.js"></script>
    <link rel="stylesheet" href="../resources/css/bootstrap.min.css">
    <link rel="stylesheet" href="../resources/css/AdminLTE.min.css">



    <style>
        .colorBtn {
            width: 40px;
            height: 40px;
            border-radius: 5px;
            background: #dddddd;
            float: left;
            margin: 0 2px;
        }

        .colorBtn:hover {
            cursor: pointer;
        }

        .colorBtnChecked {
            border: 2px solid red;
        }

        .confirmBtn {
            border-radius: 5px;
            height: 40px;
            width: 60px;
            background: #95f090;
            text-align: center;
            color: white;
            cursor: pointer;
            font-size: 20px;
        }
        .pre-document {
            border: 0;
        }
    </style>
</head>
<body>

<section class="content">
    <div class="row">
        <section id="section-document" class="col-md-12 col-lg-7">
            <div class="box box-solid bg-aqua-gradient">
                <div class="box-header">
                    <i class="fa fa-calendar"></i>
                    <h3 class="box-title">操作区域</h3>
                </div>
                <!-- /.box-header -->
                <div class=" text-black">
                    <div id="color-div" style="margin: 5px 5px; float:left;">
                        <div class="colorBtn" style="background: #ce8483;  text-align:center;  color: white" onclick="addColor()">
                            <span>添加颜色</span>
                        </div>
                    </div>
                    <div id="btn-box" style="margin: 5px 5px; float:right; margin-right: 10px;">
                        <div id="confirmBtn" class="confirmBtn" style="float:left;"
                             onclick="selectField()">
                            <span style="height:40px; line-height:40px; ">选择</span>
                        </div>
                    </div>
                </div>
                <!-- /. color box & selectBtn-->
                <div class="box-footer text-black" style="padding: 2px">
                    <div class="row">
                        <div class="col-sm-12">
                            <textarea id="hidden-document-area" style="display: none;">${inputDocument}</textarea>
                            <pre id="pre-document" class="pre-document"
                                 style="margin-top: 5px; overFlow-x: scroll ; border-width: 10px; height:500px;"></pre>
                        </div>
                    </div>
                    <!-- /.row -->
                </div>
            </div>
        </section>
        <section id="section-datatable" class="col-md-12 col-lg-5">
            <div class="box box-solid bg-green-gradient">
                <div class="box-header">
                    <i class="fa fa-calendar"></i>
                    <h3 class="box-title">提取结果</h3>

                    <div class="pull-right box-tools">
                        <!--导出csv等按钮-->
                        <a  class="btn btn-success" style=" color: #ffffff;text-decoration: none;" href="to_scv">导出CSV</a>
                        </button>
                    </div>
                </div>
                <!-- /.box-header -->
                <!-- /.box-body -->
                <div class="box-footer text-black" style="padding: 2px">
                    <div class="row">
                        <div class="col-sm-12">
                            <div id="dtable-info" class="callout callout-warning" style="margin-top: 10px;">
                                <h3>请提取需要的数据</h3>
                                <p>左上方选择颜色，用鼠标选中要提取的数据，再点击右上方的提取按钮</p>
                            </div>
                            <div id="handsontable-container" style="height: 500px; overflow :auto ; display: none;"></div>
                        </div>
                    </div>
                    <!-- /.row -->
                </div>
            </div>
        </section>
    </div>
</section>

<div style="clear: both;"></div>

<!-- 模态框（Modal） -->
<div class="modal fade" id="ff-dialog" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog" style="width: 80%;">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" id="ff-dialog-close-btn" class="close" data-dismiss="modal"
                        aria-hidden="true">×
                </button>
                <h4 class="modal-title" id="myModalLabel">
                    批量修改
                </h4>
            </div>
            <div class="modal-body">
                <div id="ff-table-container"></div>
                <input type="button" value="学习" onclick="learn()">
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" onclick="confirmExtraExp()">
                    提交更改
                </button>
            </div>
        </div>
    </div>
</div>


<%--显示用的pre文本框--%>


<button id="ff-dialog-btn" style="display: none;" class="btn btn-primary btn-lg" data-toggle="modal"
        data-target="#ff-dialog"></button>
<script type="text/javascript">

    this.REGX_HTML_ENCODE = /"|&|'|<|>|[\x00-\x20]|[\x7F-\xFF]|[\u0100-\u2700]/g;
    function encodeHtml(s) {
        return (typeof s != "string") ? s :
                s.replace(this.REGX_HTML_ENCODE,
                        function ($0) {
                            var c = $0.charCodeAt(0), r = ["&#"];
                            c = (c == 0x20) ? 0xA0 : c;
                            r.push(c);
                            r.push(";");
                            return r.join("");
                        });
    }

    var textarea = document.getElementById("pre-document");
    var container = document.getElementById("handsontable-container");
    var htable;
    // 0号颜色作为未来背景色，暂时不用 所以color的编号是从1开始的
    var colors = ["#dddddd", "#87cbff", "#95f090", "#EEAD0E", "#FF69B4", "#DEB887", "#9F79EE"];

    var inputDocument = "";
    var regionList;
    var tableData = [];
    var tableHeaderTitle = [];
    var tableColColors = [];

    var beginPos, endPos;

    htable = new Handsontable(container, {
        data: tableData,
        colHeaders: true,
        rowHeaders: true,
        contextMenu: true,
        manualColumnResize: true,
        formulas: true
    });

    var ffContainer = document.getElementById("ff-table-container");
    var ffhtable = new Handsontable(ffContainer, {
        data: tableData,
        colHeaders: true,
        rowHeaders: true,
        contextMenu: true,
        manualColumnResize: true,
        formulas: true,
        height: 400
    });

    var curSelectedDataCol;
    htable.updateSettings({
        contextMenu: {
            callback: function (key, options) {
                if (key === 'editName') {
                    var newTitle = prompt("请输入新标题:", tableHeaderTitle[curSelectedDataCol]);
                    if (newTitle != null) {
                        $.ajax({
                            url: "edit_header",
                            type: "POST",
                            data: {
                                "colorNum": parseInt(tableColColors[curSelectedDataCol].color),
                                "newHeader": newTitle
                            },
                            success: function (data) {
                                tableHeaderTitle = data;
                                updateDataTable();
//                                $('#ff-dialog-close-btn').click();
                            },
                            error: function () {
                                alert("请求失败，请稍候重试");
                            }
                        });
                    }
                } else if (key === 'flashFill') {
                    $.ajax({
                        url: "getColData",
                        type: "POST",
                        data: {"colorNum": parseInt(tableColColors[curSelectedDataCol].color)},
                        success: function (data) {
                            var dialogTableData = [];
                            for (var i = 0; i < data.length; i++) {
                                dialogTableData.push([data[i], '→', ""]);
                            }
                            ffhtable.updateSettings({
                                data: dialogTableData
                            });
                            $('#ff-dialog-btn').click();
//                                $('#ff-dialog').modal({backdrop: 'static', keyboard: false});
                        },
                        error: function () {
                            alert("请求失败，请稍候重试");
                        }
                    });
                }
                setTableHeaderBgColor();
            },
            items: {
                "editName": {name: '修改列名'},
                "hsep": "---------",
                "flashFill": {name: '批量编辑数据'}
            }
        }
    });

    $(document).ready(function () {
        textarea.innerHTML = encodeHtml($("#hidden-document-area").val());
        inputDocument = $("#hidden-document-area").val();

        addColor();
        adjustPreWidth();
    });


    window.onresize = function(){
      updateDataTable();
        adjustPreWidth()
    };

    function selectField() {
//        alert(se + "," + beginPos + "," + endPos);
        $.ajax({
            url: "select_region",
            type: "POST",
            data: {"startPos": beginPos, "endPos": endPos},
            success: function (data) {
                var htable=document.getElementById("handsontable-container");
                var tableInfo=document.getElementById("dtable-info");

                htable.style.display = "block";
                tableInfo.style.display='none';

                regionList = data.selectedFields;

                tableData = data.dataTables;
                tableHeaderTitle = data.titles;
                tableColColors = data.colors;
                showRegions();
                updateDataTable();
            },
            error: function () {
                // TODO 提示
                alert("请求失败，请稍候重试");
            }
        });
        return false;
    }

    /**
     *  点击ffDialog中的"学习"后调用此方法，将用户输入的数据根据行index组成examplePairs传递给后台学习
     *  后台学完后返回一组(3-5个)最佳expression以及第一个expression解释之后获得的结果。
     *  将结果用不同的颜色显示在第三列输入栏，右侧要给出后台返回的exp，用户可以进行选择
     *
     *  用户还可以手工补充更多样例
     */
    function learn() {
        var examplePairs = [];
        var inputDatas = ffhtable.getDataAtCol(0);
        var outputDatas = ffhtable.getDataAtCol(2);
        for (var i = 0; i < outputDatas.length; i++) {
            data = outputDatas[i];
            if (data != "") {
                examplePairs.push('{"inputString":"' + inputDatas[i] + '","outputString":"' + outputDatas[i] + '"}');
            }
        }
        $.ajax({
            url: "learnByExamples",
            type: "POST",
            data: {
                "colorNum": parseInt(tableColColors[curSelectedDataCol].color),
                "jsonExamplePairs": '[' + String(examplePairs) + ']'
            },
            success: function (data) {
                var newffTableData = [];
                for (var i = 0; i < data.length; i++) {
                    newffTableData.push([inputDatas[i], "→", data[i]]);
                }
                ffhtable.updateSettings({
                    data: newffTableData
                });
            },
            error: function () {
                alert("请求失败，请稍候重试");
            }
        });
    }

    /**
     * 在弹出ffDialog中点击'确认修改' 此时向后台传输请求，成功的话则关闭dialog并且更新datatable
     */
    function confirmExtraExp() {
        $.ajax({
            url: "confirmExtraExp",
            type: "POST",
            data: {
                "colorNum": parseInt(tableColColors[curSelectedDataCol].color)
            },
            success: function (data) {
                tableData = data.dataTables;
                updateDataTable();
            },
            error: function () {
                alert("请求失败，请稍候重试");
            }
        });
    }
    function showRegions() {
        textarea.innerHTML = doSelect("jusettext", "", 0, inputDocument.length, regionList);
    }

    /**
     * select就是替换掉pre的内容
     *
     * 替换规则根据regionList的index确定。由<没选中的>+<选中的>一段一段拼接而成
     */
    function doSelect(parentClasses, parentStyle, parentBeginPos, parentEndPos, childRegions) {
        // fixme 可能还要看情况加一个parentText？
        var innerHtml = "";
        var lastBeginPos = parentBeginPos;
        // 假设regionList的pos都是有序的
        function generateSpan(classes, style, text, beginPos, endPos) {
            var span = "<span class=\"" + classes + "\" style=\"" + style + "\" data-start=\"" + beginPos + "\" data-end=\"" + endPos + "\">" + text + "</span>";
            return span;
        }

        for (var index in childRegions) {
            var region = childRegions[index];
            // 先处理这个region之前的内容
            if (region.beginPos != lastBeginPos) {
                var baseSpan = generateSpan(parentClasses, parentStyle, encodeHtml(inputDocument.substr(lastBeginPos, region.beginPos - lastBeginPos)),
                        lastBeginPos, region.beginPos);
                innerHtml += baseSpan;
            }
            // 再处理当前region
            style = "background: " + colors[region.color.color];
            var curSpan = generateSpan("justtext", style,
                    encodeHtml(inputDocument.substr(region.beginPos, (region.endPos - region.beginPos))),
                    region.beginPos, region.endPos);
            innerHtml += curSpan;
            lastBeginPos = region.endPos;
        }
        var lastSpan = generateSpan("justtext", parentStyle,
                encodeHtml(inputDocument.substr(lastBeginPos, parentEndPos - lastBeginPos)),
                lastBeginPos, inputDocument.length);
        innerHtml += lastSpan;

        return innerHtml;
    }

    function updateDataTable() {
        if(tableData.length>0){
            htable.updateSettings({
                data: tableData,
                colHeaders: tableHeaderTitle
            });
            $('#handsontable-container thead tr th').mousedown(function (e) {
                if (e.which == 3) {
                    curSelectedDataCol = $(this).index() - 1;
                }
            });
            setTableHeaderBgColor();
        }
    }

    function setTableHeaderBgColor() {
        var table = $('#handsontable-container');
        var theadTr = table.find(".htCore").find('thead > tr').eq(1);
        for (var i = 1; i <= tableColColors.length; i++) {
            theadTr.find("th").eq(i).css('background', colors[parseInt(tableColColors[i - 1].color)]);
        }
    }
</script>
<script type="text/javascript">
    var se;
    $("#pre-document").mouseup(function (e) {
        se = window.getSelection();
        var curSpan = se.focusNode.parentElement;
        dataStart = curSpan.getAttribute("data-start");
        if (!dataStart || typeof(dataStart) == "undefined") {
            dataStart = 0;
        }
        beginPos = parseInt(dataStart) + parseInt(se.anchorOffset);
        endPos = parseInt(dataStart) + parseInt(se.focusOffset);
        if (beginPos > endPos) {
            var t = beginPos;
            beginPos = endPos;
            endPos = t;
        }
    });
</script>
<script>
    function adjustPreWidth() {
        var containerWidth = document.body.clientWidth;

        var preDocument = document.getElementById("pre-document");
//        if (preDocument.scrollLeft + preDocument.clientWidth < preDocument.scrollWidth
//                || preDocument.clientWidth > containerWith) {
//            // 出现滚动条，意味着pre不够长，所以就放大
//            // 如果文本框太大超出了container的宽度，那么久调小
//            // FIXME 注意这里没处理滚动条拉倒最右的情况
//            preDocument.style.width = Math.min(preDocument.scrollWidth, containerWith - 10) + 5 + 'px';
//        }

        // 如果preDocument的滑动条窗口大于0.7个body.width 就修改clas
        var sectionDocument=document.getElementById("section-document");
        var sectionDatatable=document.getElementById("section-datatable");

//        alert(preDocument.scrollWidth+","+sectionDocument.clientWidth+","+document.body.clientWidth);
        if (preDocument.scrollWidth>1.5*sectionDocument.clientWidth){
            if(sectionDocument.clientWidth<0.95*document.body.clientWidth){
                sectionDocument.className="col-md-12 col-lg-12";
                sectionDatatable.className="col-md-12 col-lg-12";
            }
        }else if(preDocument.scrollWidth<sectionDocument.clientWidth){
            if(sectionDocument.clientWidth>0.95*document.body.clientWidth){
                sectionDocument.className="col-md-7 col-lg-7";
                sectionDatatable.className="col-md-5 col-lg-5";
            }
        }
    }
</script>
<script type="text/javascript">
    var colorCounter = 1;
    function addColor() {
        if (colorCounter >= colors.length) {
            alert("已达到颜色上限");
            return;
        }
        var colorDiv = document.getElementById("color-div");
        var newColorBtn = document.createElement("div");
        newColorBtn.style = "background: " + colors[colorCounter] + ";";
        newColorBtn.className = "colorBtn";
        newColorBtn.dataset.color_counter = (colorCounter++);
        colorDiv.appendChild(newColorBtn);
        newColorBtn.addEventListener("click", function () {
            var curColor = this.dataset.color_counter;
            $.ajax({
                url: "set_color",
                type: "POST",
                data: {"color": parseInt(curColor)},
                error: function () {
                }
            });
            // 更改"选择"按钮的背景颜色
            var confirmBtn=document.getElementById("confirmBtn");
            confirmBtn.style.background=colors[curColor];

            // 更新"当前选中"标志
            var colorDiv = document.getElementById("color-div");
            checkedBtn = colorDiv.getElementsByClassName("colorBtnChecked");
            for (var i = 0; i < checkedBtn.length; i++) {
                checkedBtn[i].className = "colorBtn";
            }
            this.className = "colorBtn colorBtnChecked";
        });
        newColorBtn.click();
    }
</script>
</body>
</html>
