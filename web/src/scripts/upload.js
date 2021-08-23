/*
  Hubbub Upload code
*/
require.config({
  stubModules: ['cs', 'tpl'],
  shim: {
    'bootstrap':         { deps: ['jquery']},
    'backbone':          { deps: ['underscore', 'jquery'], exports: 'Backbone'},
    'dropzone':          { exports: 'Dropzone'}
  },
  paths: {
    'dropzone':               '../vendor/dropzone/dist/dropzone',
    'filesize':               '../vendor/filesize/lib/filesize',
    'bootstrap':              '../vendor/bootstrap/dist/js/bootstrap',
    'cs' :                    '../vendor/require-cs/cs',
    'coffee-script':          '../vendor/coffee-script/extras/coffee-script',
    'tpl' :                   '../vendor/requirejs-tpl/tpl',
    'text' :                  '../vendor/requirejs-text/text',
    'jquery' :                '../vendor/jquery/dist/jquery',
    'underscore':             '../vendor/underscore/underscore',
    'backbone':               '../vendor/backbone/backbone'
  },
  waitSeconds:1000
});
require(['cs!Upload'], function(Upload){ Upload.initialize(); });
