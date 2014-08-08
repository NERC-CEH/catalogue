/*
  ____ _____ _   _    ____      _        _                           _
 / ___| ____| | | |  / ___|__ _| |_ __ _| | ___   __ _ _   _  ___   (_)___
| |   |  _| | |_| | | |   / _` | __/ _` | |/ _ \ / _` | | | |/ _ \  | / __|
| |___| |___|  _  | | |__| (_| | || (_| | | (_) | (_| | |_| |  __/_ | \__ \
 \____|_____|_| |_|  \____\__,_|\__\__,_|_|\___/ \__, |\__,_|\___(_)/ |___/
                                                 |___/            |__/

That got your attention. This is the require js file which configures the client
side scripts for the CEH Catalogue. If you can see this code in the browser it 
means that you are running in development mode. You will want to call grunt 
default to create an optimized version of this file.
*/
require.config({
  stubModules: ['cs'],
  shim: {
    'openlayers': { exports: 'OpenLayers'}
  },
  paths: {
    'cs' : '../vendor/require-cs/cs',
    'coffee-script': '../vendor/coffee-script/extras/coffee-script',
    'text' : '../vendor/requirejs-text/text',
    'jquery' : '../vendor/jquery/jquery',
    'underscore': '../vendor/underscore-amd/underscore',
    'backbone': '../vendor/backbone-amd/backbone',
    'openlayers': '../vendor/OpenLayers-custom',
    'proj4js' : '../vendor/proj4js/lib/proj4js-combined'
  },
  waitSeconds:1000
});

require(['cs!models/App', 'cs!views/AppView', 'cs!models/MetadataDocument'], function(App, AppView, MetadataDocument){
  app = new App();
  view = new AppView({model : app});
});