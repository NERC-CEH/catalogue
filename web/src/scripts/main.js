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
  stubModules: ['cs', 'tpl'],
  shim: {
    'bootstrap':         { deps: ["jquery"]},
    'isInViewport':      { deps: ["jquery"]},
    'jquery-scrollsnap': { deps: ["jquery", "jquery-scrollstop"]},
    'openlayers':        { exports: 'OpenLayers'}
  },
  paths: {
    'bootstrap':         '../vendor/bootstrap/dist/js/bootstrap',
    'cs' :               '../vendor/require-cs/cs',
    'coffee-script':     '../vendor/coffee-script/extras/coffee-script',
    'tpl' :              '../vendor/requirejs-tpl/tpl',
    'text' :             '../vendor/requirejs-text/text',
    'isInViewport':      '../vendor/isInViewport/lib/isInViewport',
    'jquery' :           '../vendor/jquery/jquery',
    'jquery-deparam' :   '../vendor/jquery-deparam/jquery-deparam',
    'underscore':        '../vendor/underscore-amd/underscore',
    'backbone':          '../vendor/backbone-amd/backbone',
    'openlayers':        '../vendor/OpenLayers-custom',
    'jquery-ui':         '../vendor/jquery-ui/ui'
  },
  waitSeconds:1000
});

require(['cs!Main'], function(Main){ Main.initialize(); });