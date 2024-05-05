    /**
     * url  请求的地址
     * data 数据 json格式数据
     * async 默认值: true。如果需要发送同步请求，请将此选项设置为 false。
     * type 请求方式("POST" 或 "GET")， 默认为 "GET"
     * dataType 预期服务器返回的数据类型，常用的如：xml、html、json、text
     * successfn 成功回调函数
     * errorfn 失败回调函数
     */
    layui.$.inheritAjax=function(url, data, async, type, dataType,contentType, successfn, errorfn) {
        async = (async==null || async==="" || typeof(async)==="undefined")? "true" : async;
        type = (type==null || type==="" || typeof(type)==="undefined")? "post" : type;
        dataType = (dataType==null || dataType==="" || typeof(dataType)==="undefined")? "json" : dataType;
        data = (data==null || data==="" || typeof(data)==="undefined")? {"date": new Date().getTime()} : data;
        contentType = (contentType==null || contentType ==="" || typeof(contentType)==="undefined") ? "application/x-www-form-urlencoded;charset=UTF-8" : contentType;
        layui.$.ajax({
            type: type,
            async: async,
            data: data,
            url: url,
            contentType :contentType,
            dataType: dataType,
            success: function(d){
                successfn(d);
            },
            error: function(e){
                layer.msg(e)
            },
            complete : function(XMLHttpRequest,status){ //请求完成后最终执行参数
                if(status==='timeout'){//超时,status还有success,error等值的情况
                    layer.msg(e)
                }
            }
        });
    };
    layui.$.yunJiAjaxPostJson=function(url, data, successfn) {
        layui.$.inheritAjax(url,data,true,"POST",null,"application/json;charset=utf-8",successfn,layui.$.noop);
    };
    layui.$.yunJiAjaxPost=function(url, data, successfn) {
        layui.$.inheritAjax(url,data,true,"POST",null,null,successfn,layui.$.noop);
    };
    layui.$.yunJiAjaxGet=function(url, data, successfn) {
        layui.$.inheritAjax(url,data,true,"GET",null,null,successfn,layui.$.noop);
    };
    layui.$.yunJiAjaxGetSycn=function(url, data, successfn) {
        layui.$.inheritAjax(url,data,false,"GET",null,null,successfn,layui.$.noop);
    };
    layui.$.yunJiAjaxPostSycn=function(url, data, successfn) {
        layui.$.inheritAjax(url,data,false,"POST",null,null,successfn,layui.$.noop);
    };



