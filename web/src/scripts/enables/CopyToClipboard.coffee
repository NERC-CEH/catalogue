define [
  'jquery'
  'zeroclipboard'
], ($, ZeroClipboard) -> $(document).ready ->
  ZeroClipboard.config({swfPath: "/static/vendor/zeroclipboard/dist/ZeroClipboard.swf"});
  new ZeroClipboard($(".clipboard"));