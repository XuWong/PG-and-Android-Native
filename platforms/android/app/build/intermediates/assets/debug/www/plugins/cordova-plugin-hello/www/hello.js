cordova.define("cordova-plugin-hello.hello", function(require, exports, module) {
var exec = require('cordova/exec');

exports.showConsole = function (arg0, success, error) {
    exec(success, error, 'callPlugin', 'actionShow', [arg0]);
};
exports.cmdConsole = function (arg0, success, error) {
    exec(success, error, 'callPlugin', 'cmd', [arg0]);
};
});
