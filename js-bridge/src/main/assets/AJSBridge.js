try {
    (function(win) {
        if (ANDROID_NAME) {
            return;
        }

        console.log("ANDROID_NAME init begin");

        var android = {
            queue: [],
            callback: function() {
                var argumentArray = Array.prototype.slice.call(arguments, 0);
                var index = argumentArray.shift();
                var executeOnce = argumentArray.shift();
                this.queue[index].apply(this, argumentArray);
                if (executeOnce) {
                    delete this.queue[index]
                }
            }
        };

        android.debug = android.plus = android.showToast = function() {
            var androidArguments = Array.prototype.slice.call(arguments, 0);
            console.log(androidArguments);
            if (androidArguments.length < 1) {
                throw "ANDROID_NAME call error, message:miss method name"
            }
            var androidParamTypes = [];
            for (var h = 1; h < androidArguments.length; h++) {
                var paramValue = androidArguments[h];
                var paramType = typeof paramValue;
                androidParamTypes[androidParamTypes.length] = paramType;
                if (paramType == "function") {
                    var length = android.queue.length;
                    android.queue[length] = paramValue;
                    androidArguments[h] = length
                }
            }
            var timeNow = new Date().getTime();
            var methodName = androidArguments.shift();
            var javaReturnJson = prompt("ANDROID_FLAG", "ANDROID_FLAG:" + JSON.stringify({ obj: "ANDROID_NAME", method: methodName, types: androidParamTypes, args: androidArguments }));
            console.log("invoke " + methodName + ", time: " + (new Date().getTime() - timeNow));
            var javaResult = JSON.parse(javaReturnJson);
            console.log(javaResult);
            if (javaResult.code != 200) {
                throw "ANDROID_NAME call error, code:" + javaResult.code + ", message:" + javaResult.result
            }
            return javaResult.result
        };

        Object.getOwnPropertyNames(android).forEach(
            function(propertyName) {
                var callbackFunction = android[propertyName];
                if (typeof callbackFunction === "function") {
                    android[propertyName] = function() {
                        return callbackFunction.apply(android, [propertyName].concat(Array.prototype.slice.call(arguments, 0)));
                    }
                }
            });
        win.ANDROID_NAME = android;
        console.log("ANDROID_NAME init end")
    })(window)
} catch (e) {
    console.warn(e)
}