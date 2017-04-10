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

    <%--dialog相关--%>
    <link rel="stylesheet" media="screen"
          href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap3-dialog/1.34.7/css/bootstrap-dialog.min.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap3-dialog/1.34.7/js/bootstrap-dialog.min.js"></script>

    <link rel="stylesheet" href="http://cdn.static.runoob.com/libs/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="http://cdn.static.runoob.com/libs/bootstrap/3.3.7/js/bootstrap.min.js"></script>
</head>
<body>
<div id="color-div">
    <input id="btnReg" type="button" value="+" onclick="addColor()"/>
</div>
<div style="clear: both;"></div>
<div class="modal fade" id="ff-dialog" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"
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
                <button type="button" class="btn btn-primary" onclick="learn()">
                    提交更改
                </button>
            </div>
        </div>
    </div>
</div>
<textarea id="hidden-document-area" style="display: none;">${inputDocument}</textarea>
<pre id="pre-document" class="" style="overFlow-x: scroll ; border-width: 10px; height:500px;width: 1049.55px;"></pre>

<input style="float: left" type="button" value="选择" onclick="selectField()"/>

<a style=" color: #000000;    text-decoration: none;   " href="to_scv">导出CSV</a>


<div id="handsontable-container" style="float: left"></div>

<button id="ff-dialog-btn" style="display: none;" class="btn btn-primary btn-lg" data-toggle="modal"
        data-target="#ff-dialog">
    <!-- 模态框（Modal） -->
    <script>
    </script>
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
                        $.ajax({
                            url: "edit_header",
                            type: "POST",
                            data: {"colorNum": parseInt(tableColColors[curSelectedDataCol].color), "newHeader": "zzzz"},
                            success: function (data) {
                                tableHeaderTitle = data;
                                updateDataTable();
                            },
                            error: function () {
                                alert("请求失败，请稍候重试");
                            }
                        });
                    } else if (key === 'flashFill') {
                        // todo 弹出dialog 左边n个原始数据，右边是n个输入框，右上方是开始处理的按钮
                        // 点击处理后，要把index和输入的所有样例(及行数)传给后台，后台处理完后返回处理后的所有数据和表达式列表
                        $.ajax({
                            url: "getColData",
                            type: "POST",
                            data: {"colorNum": parseInt(tableColColors[curSelectedDataCol].color)},
                            success: function (data) {
//                                alert(data);
                                var dialogTableData = [];
                                for (var i = 0; i < data.length; i++) {
                                    dialogTableData.push([data[i], '→', ""]);
                                }
                                ffhtable.updateSettings({
                                    data: dialogTableData
                                });
                                $('#ff-dialog-btn').click();
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
        });

        function selectField() {
//        alert(se + "," + beginPos + "," + endPos);
            $.ajax({
                url: "select_region",
                type: "POST",
                data: {"startPos": beginPos, "endPos": endPos},
                success: function (data) {
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
            alert(examplePairs);
            $.ajax({
                url: "learnByExamples",
                type: "POST",
                data: {
                    "colorNum": parseInt(tableColColors[curSelectedDataCol].color),
                    "jsonExamplePairs": '[' + String(examplePairs) + ']'
                },
                success: function (data) {
                    var newffTableData=[];
                    for (var i=0;i<data.length;i++){
                        newffTableData.push([inputDatas[i],"→",data[i]]);
                    }
                    ffhtable.updateSettings({
                        data:newffTableData
                    });
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
    <script type="text/javascript">
        var colorCounter = 1;
        function addColor() {
            if (colorCounter >= colors.length) {
                alert("已达到颜色上限");
                return;
            }

            var colorDiv = document.getElementById("color-div");
            var newColorBtn = document.createElement("input");
            newColorBtn.type = "button";
            newColorBtn.value = "color" + (colorCounter++);
            colorDiv.appendChild(newColorBtn);
            newColorBtn.addEventListener("click", function () {
                var curColor = this.value.substring(5);
                $.ajax({
                    url: "set_color",
                    type: "POST",
                    data: {"color": curColor},
                    error: function () {
                    }
                });
            });
            newColorBtn.click();
        }
    </script>
</body>
</html>
