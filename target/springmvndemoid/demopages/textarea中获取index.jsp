<%@ page pageEncoding="UTF-8" %>
<html>
<body>
<textarea id="content" cols="50" rows="5">AAAAAAA BBBBBBB AAAAAAA CCCCCCC</textarea>
<input type="button" id="wrapText" value="加粗"/>
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
        alert(selectionStart+','+selectionEnd);
//        textarea.value = textarea.value.substring(0, selectionStart)
//                + '<b>' + textarea.value.substring(selectionStart, selectionEnd)
//                + '</b>' + textarea.value.substring(selectionEnd);
    };
</script>
</body>
</html>
