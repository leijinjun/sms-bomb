let timeUtil = {
    dateFormat:(date,format)=>{
        let pad = function (num) {
            let norm = Math.floor(Math.abs(num));
            return (norm < 10 ? '0' : '') + norm;
        };
        let offset = -date.getTimezoneOffset();
        let timezone = ((offset >= 0 ? '+' : '-') + pad(offset / 60) + ":" + pad(offset % 60));
        var map = {
            "M": date.getMonth() + 1, //月份
            "d": date.getDate(), //日
            "h": date.getHours(), //小时
            "m": date.getMinutes(), //分
            "s": date.getSeconds(), //秒
            "q": Math.floor((date.getMonth() + 3) / 3), //季度
            "S": date.getMilliseconds(), //毫秒
            "Z": timezone,
        };

        format = format.replace(/([yMdhmsqSZ])+/g, function (all, t) {
            var v = map[t];
            if (v !== undefined) {
                if (all.length > 1) {
                    v = '0' + v;
                    v = v.substr(v.length - 2);
                }
                return v;
            } else if (t === 'y') {
                return (date.getFullYear() + '').substr(4 - all.length);
            }
            return all;
        });
        return format;
    },
    getBJTime:d=>{
        let bgDate = new Date(d);
        let tmpHours = d.getHours();
        //算得时区
        let time_zone = -d.getTimezoneOffset() / 60;
        //少于0的是西区 西区应该用时区绝对值加京八区 重新设置时间（西区时间比东区时间早 所以加时区间隔）
        if (time_zone < 0) {
            time_zone = Math.abs(time_zone) + 8;
            bgDate.setHours(tmpHours + time_zone);
        } else {
            //大于0的是东区  东区时间直接跟京八区相减
            time_zone -= 8;
            bgDate.setHours(tmpHours - time_zone);
        }
        return bgDate;
    }
};
let $http = {
    get:(url,data)=>{
        return new Promise(function(resolve, reject){
            $.ajax({url:url,cache:false,traditional:true,type:'GET',data:data,
                success:function(result){
                    resolve(result);
                },
                error: function (xhr, status, error) {
                    if (xhr.status == 404) {
                        reject("404:数据不存在");
                    } else if (xhr.status >= 500) {
                        reject(xhr.status+":服务器错误");
                    } else {
                        reject(xhr.status+":"+xhr.responseText);
                    }
                }
            })
        });
    },
    getSync:(url,data)=>{
        return new Promise(function(resolve, reject){
            $.ajax({url:url,cache:false,traditional:true,type:'GET',data:data,async:false,
                success:function(result){
                    resolve(result);
                },
                error:function(xhr,status,error){
                    reject(xhr.status+":"+xhr.responseText);
                }
            })
        });
    },
    postSync:(url,data,contentType)=>{
        return new Promise((resolve, reject) => {
            $.ajax({
                url:url,
                traditional:true,
                type:'POST',
                contentType:contentType?contentType:'application/x-www-form-urlencoded',
                data:data,
                async:false,
                success:function(result){
                    resolve(result);
                },
                error:function(xhr,status,error){
                    reject(xhr.status+":"+xhr.responseText);
                }
            })
        })
    },
    //contentType可选，默认application/x-www-form-urlencoded
    post:(url,data,contentType)=>{
        return new Promise((resolve, reject) => {
            $.ajax({
                url:url,
                traditional:true,
                type:'POST',
                contentType:contentType?contentType:'application/x-www-form-urlencoded',
                data:data,
                success:function(result){
                    resolve(result);
                },
                error:function(xhr,status,error){
                    reject(xhr.status+":"+xhr.responseText);
                }
            })
        })
    },
    postWithForm:function(url,data){return this.post(url,data,'application/x-www-form-urlencoded')},
    postWithJSON:function(url,data){return this.post(url,data,'application/json')}
};

const gft = {
    download:(document,target)=>{
        let eleLink = document.createElement('a');
        eleLink.style.display = 'none';
        eleLink.href = target;
        document.body.appendChild(eleLink);
        eleLink.click();
        document.body.removeChild(eleLink);
    },
    reset($form) {
        $form.find('select option').removeAttr('selected');
        $form.find(':text').removeAttr('value');
        $form.find(':password').removeAttr('value');
        $form.find(':file').removeAttr('value');
        $form.find('textarea').removeAttr('value');
        $form.find(':radio').removeAttr('checked');
        $form.find(':checkbox').removeAttr('checked');
    },
    alert:{
        message:function(type,tips,timeout){
            let idSelector = 'vue2du3d-alert';
            let containerTemplate = '<div id="' + idSelector + '" style="max-width: 190px;position: fixed;top:' +
                ' 5px;z-index:10000000;right: 0;margin: 0;padding: 2px;pointer-events: auto;"></div>';
            let template = '<div class="alert alert-{{type}} alert-dismissible fade show" role="alert">' +
                '<div class="content" style="word-wrap:break-word;word-break:break-all;">{{tips}}</div>'+
                '<button type="button" class="close" data-rand="{{rand}}" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button></div>';
            let rand = new Date().getTime().toString();
            template = template.replace('{{tips}}', tips).replace('{{rand}}', rand).replace("{{type}}", type);
            let $alert = $(document.body).find('#' + idSelector);
            if ($alert.length) {
                $alert.show().prepend(template);
            }else{
                $(document.body).prepend(containerTemplate);
                $(document.body).find('#' + idSelector).prepend(template);
            }
            if (timeout && timeout > 0) {
                setTimeout(function () {
                    $(document.body).find('#' + idSelector).find("button[data-rand='" + rand + "']").alert('close');
                }, timeout);
            }
        },
        success:function(tips, timeout){
            this.message('success', tips, timeout);
        },
        error:function(tips,timeOut){
            this.message('danger', tips, timeOut);
        }
    }
};