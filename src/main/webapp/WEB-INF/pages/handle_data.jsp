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

    <script type="text/javascript" src="../../../resource/js/jquery-1.9.1.js"></script>
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

        function initPreDocument() {
//            alert("sdaf");
            <%--alert(${inputDocument});--%>
//            textarea.innerHTML="SDAsadfsdafdsafdsf";
        }
    </script>
</head>
<body onload="initPreDocument()">
<div id="color-div">
    <input id="btnReg" type="button" value="+" onclick="addColor()"/>
</div>
<div style="clear: both;"></div>
<textarea id="hidden-document-area" style="display: none;">${inputDocument}</textarea>
<pre id="pre-document" class="" style="overFlow-x: scroll ; border-width: 10px; height:500px;width: 1049.55px;"></pre>

<input style="float: left" type="button" value="选择" onclick="selectField()"/>

<div id="handsontable-code" style="float: left"></div>

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

    function decodeHtml(s) {
        return (typeof s != "string") ? s :
                s.replace(this.REGX_HTML_DECODE,
                        function ($0, $1) {
                            var c = this.HTML_ENCODE[$0]; // 尝试查表
                            if (c === undefined) {
                                // Maybe is Entity Number
                                if (!isNaN($1)) {
                                    c = String.fromCharCode(($1 == 160) ? 32 : $1);
                                } else {
                                    // Not Entity Number
                                    c = $0;
                                }
                            }
                            return c;
                        });
    }

    var textarea = document.getElementById("pre-document");

    var inputDocument = "";
    var colors = ["#87cbff", "#95f090", "#EEAD0E"];

    $(document).ready(function () {
        <%--alert(encodeHtml(${inputDocument}));--%>
        <%--textarea.innerHTML=encodeHtml(${inputDocument});--%>

        // 下面是table相关的，暂时屏蔽
        var data1 = [
            ["", "Ford", "Volvo", "Toyota", "Honda"],
            ["2016", 10, 11, 12, 13],
            ["2017", 20, 11, 14, 13],
            ["2018", 30, 15, 12, 13]
        ];

        function negativeValueRenderer(instance, td, row, col, prop, value, cellProperties) {
            Handsontable.renderers.TextRenderer.apply(this, arguments);

            var escaped = Handsontable.helper.stringify(value),
                    newvalue;

            if (escaped.indexOf('return') === 0) {
                //计算列为只读
                //cellProperties.readOnly = true;
                td.style.background = '#EEE';
                newvalue = document.createElement('span');
                $.ajax({
                    //提交数据的类型 POST GET
                    type: "POST",
                    //提交的网址
                    url: "/services/CSEngine.ashx",
                    //提交的数据
                    data: {code: value, code2: escaped},
                    //返回数据的格式
                    datatype: "html",//"xml", "html", "script", "json", "jsonp", "text".
                    //在请求之前调用的函数
                    //beforeSend: function () { $("#msg").html("logining"); },
                    //成功返回之后调用的函数
                    success: function (data) {
                        // $("#msg").html(decodeURI(data));
                        newvalue.innerHTML = decodeURI(data);
                    },
                    //调用执行后调用的函数
                    complete: function (XMLHttpRequest, textStatus) {
                        //alert(XMLHttpRequest.responseText);
                        // alert(textStatus);
                        //HideLoading();
                    },
                    //调用出错执行的函数
                    error: function () {
                        //请求出错处理
                        // alert('error')
                    }
                });


                Handsontable.Dom.addEvent(newvalue, 'mousedown', function (e) {
                    e.preventDefault(); // prevent selection quirk
                });

                Handsontable.Dom.empty(td);
                td.appendChild(newvalue);
            }
            // if row contains negative number
            if (parseInt(value, 10) < 0) {
                // add class "negative"
                td.className = 'negative';
            }


        }

        //类似excel进行拖放，公式会变
        var container1 = $('#handsontable-code');
        Handsontable.renderers.registerRenderer('negativeValueRenderer', negativeValueRenderer);
        container1.handsontable({
            data: data1,
            minSpareRows: 1,
            colHeaders: true,
            rowHeaders: true,
            contextMenu: true,
            manualColumnResize: true,
            formulas: true,
            cells: function (row, col, prop) {
                var cellProperties = {};
                var escaped = Handsontable.helper.stringify(this.instance.getData()[row][col]);
                if (escaped.indexOf('return') === 0) {
                    cellProperties.renderer = "negativeValueRenderer";
                }


                return cellProperties;
            }
        });

        textarea.innerHTML = encodeHtml($("#hidden-document-area").val());
        inputDocument = $("#hidden-document-area").val();
    });

    var regionList;

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
            // 在处理当前region
//            style="background: "+colors[region.color];
            style = "background: " + colors[0];

            // 情况2. 普通的着色region
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

    function showRegions() {
        textarea.innerHTML = doSelect("jusettext", "", 0, inputDocument.length, regionList);
    }

    var beginPos, endPos;
    function selectField() {
        alert(se + "," + beginPos + "," + endPos);
        $.ajax({
            url: "select_region",
            type: "POST",
            data: {"startPos": beginPos, "endPos": endPos},
            success: function (data) {
                regionList = JSON.parse(data);
                showRegions();
            },
            error: function () {
                // TODO 提示
                alert("请求失败，请稍候重试");
            }
        });
        return false;
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
</body>
</html>
