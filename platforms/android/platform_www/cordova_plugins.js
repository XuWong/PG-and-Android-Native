cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
  {
    "id": "cordova-plugin-hello.hello",
    "file": "plugins/cordova-plugin-hello/www/hello.js",
    "pluginId": "cordova-plugin-hello",
    "clobbers": [
      "cordova.plugins"
    ]
  }
];
module.exports.metadata = 
// TOP OF METADATA
{
  "cordova-plugin-whitelist": "1.3.3",
  "cordova-plugin-hello": "0.1.1"
};
// BOTTOM OF METADATA
});