var exec = require('cordova/exec');

exports.showConsole = function (arg0, success, error) {
    exec(success, error, 'callPlugin', 'actionShow', [arg0]);
};
