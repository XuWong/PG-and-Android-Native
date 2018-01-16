cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/cordova-plugin-hello/www/hello.js",
        "id": "cordova-plugin-hello.hello",
        "pluginId": "cordova-plugin-hello",
        "clobbers": [
            "cordova.plugins"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "cordova-plugin-hello": "0.1.1",
    "cordova-plugin-whitelist": "1.3.3"
}
// BOTTOM OF METADATA
});