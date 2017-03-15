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
            newColorBtn.addEventListener("click", function(){
                var curColor=this.value.substring(5);
                $.ajax({
                    url: "set_color",
                    type: "POST",
                    data: {"color":curColor},
                    error: function () {
                        alert("请求失败，请稍候重试");
                    }
                });
            });
        }
    </script>
</head>
<body>
<div id="color-div">
    <input id="btnReg" type="button" value="+" onclick="addColor()"/>
</div>
<div style="clear: both;"></div>
<textarea style="float: left" id="content" cols="100" rows="50">${inputDocument}</textarea>
<input style="float: left" type="button" id="wrapText" value="选择"/>


<div id="handsontable-code" style="float: left"></div>

<script type="text/javascript">
    $(document).ready(function () {

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

    });

</script>
<script type="text/javascript">
    var selectionStart, selectionEnd;
    var textarea = document.getElementById("content");

    document.onkeyup = document.onmouseup = function (event) {
        var userSelection;
        if (window.getSelection) { //现代浏览器
            userSelection = window.getSelection();
        } else if (document.selection) { //IE浏览器 考虑到Opera，应该放在后面
            userSelection = document.selection.createRange();
        }

        var getRangeIndex = function (selectionObject) {
            if (window.getSelection)
                return [textarea.selectionStart, textarea.selectionEnd];
            else { // 较老版本Safari!
                var range = document.selection.createRange();             //对选择的文字create Range
// var selectText          = range.text;                                //选中的文字
                var selectTextLength = range.text.length;                            //选中文字长度
                textarea.select();                                                      //textarea全选
//StartToStart、StartToEnd、EndToStart、EndToEnd
                range.setEndPoint("StartToStart", document.selection.createRange());    //指针移动到选中文字开始
                var selectTextPosition = range.text.length;                            //选中文字的结束位置
                range.collapse(false);                                                  //将插入点移动到当前范围的开始
                range.moveEnd("character", -selectTextLength);   //更改范围的结束位置，减去长度，字符开始位置，character不能改
                range.moveEnd("character", selectTextLength);   //再更改范围的结束位置，到字符结束位置
                range.select();                                                         //然后选中字符

//返回字符的开始和结束位置
                return [selectTextPosition - selectTextLength, selectTextPosition];
            }
        };

        var rangeIndex = getRangeIndex(userSelection);
        selectionStart = rangeIndex[0];
        selectionEnd = rangeIndex[1];
    };

    //加粗
    document.getElementById('wrapText').onclick = function () {
        alert(selectionStart + ',' + selectionEnd);
        $.ajax({
            url: "select_region",
            type: "POST",
            data: {"startPos": selectionStart, "endPos": selectionEnd},
            success: function (data) {
                alert("success");
            },
            error: function () {
                alert("请求失败，请稍候重试");
            }
        });
        return false;
    };
</script>
</body>
</html>
