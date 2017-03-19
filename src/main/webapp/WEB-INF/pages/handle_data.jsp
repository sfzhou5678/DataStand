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
</head>
<body>
<div id="color-div">
    <input id="btnReg" type="button" value="+" onclick="addColor()"/>
</div>
<div style="clear: both;"></div>
<textarea id="hidden-document-area" style="display: none;">${inputDocument}</textarea>
<pre id="pre-document" class="" style="overFlow-x: scroll ; border-width: 10px; height:500px;width: 1049.55px;"></pre>

<input style="float: left" type="button" value="选择" onclick="selectField()"/>

<a style=" color: #000000;    text-decoration: none;   " href="to_scv">导出CSV</a>


<div id="handsontable-container" style="float: left"></div>

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
    var container = $('#handsontable-container');
    var colors = ["#dddddd", "#87cbff", "#95f090", "#EEAD0E"];

    var inputDocument = "";
    var regionList;
    var tableData = [];
    var tableHeaderTitle = [];
    var tableColColors = [];

    var beginPos, endPos;

    $(document).ready(function () {
        container.handsontable({
            data: tableData,
            colHeaders: true,
            rowHeaders: true,
            contextMenu: true,
            manualColumnResize: true,
            formulas: true
        });

        textarea.innerHTML = encodeHtml($("#hidden-document-area").val());
        inputDocument = $("#hidden-document-area").val();
    });

    function selectField() {
        alert(se + "," + beginPos + "," + endPos);
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
        container.handsontable({
            data: tableData,
            colHeaders: tableHeaderTitle,
            rowHeaders: true,
            contextMenu: true,
            manualColumnResize: true,
            formulas: true
        });
        setTableHeaderBgColor();
    }

    function setTableHeaderBgColor() {
        var theadTr = container.find(".htCore").find('thead > tr').eq(1);
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
    });
</script>
<script type="text/javascript">
    var colorCounter = 1;
    function addColor() {
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
                    alert("请求失败，请稍候重试");
                }
            });
        });
    }
</script>
</body>
</html>
