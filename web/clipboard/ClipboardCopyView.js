/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'backbone',
  'tpl!templates/ClipboardCopy.tpl'
], 
function($, Backbone, template) {

  //#
  //# Add a <span class="clipboard-copy" data-selector="{my-selector}"></span>
  //# to any element to copy to clipboard.
  //# Replace {my-selector} with the selector for the element you want copied.
  //#

  const copy = function(selector) {
      const selection = self.getSelection();
      const range = document.createRange();
      const copyContent = document.querySelector(selector);
      
      if (copyContent) {
        range.selectNode(copyContent);
        selection.empty();
        selection.addRange(range);
        document.execCommand('copy');
        return selection.empty();
      }
    };

  return Backbone.View.extend({

    events: {
      'click': 'copyToClipboard'
    },

    initialize() {
      return (this.render)();
    },

    render() { 
      this.$el.html(template());
      this.$('button').tooltip({
        title: 'copied',
        trigger: 'click'
      });
      return this;
    },

    copyToClipboard(event) {
      copy($(event.currentTarget).data('selector'));
      self.clearTimeout(this.timeout);
      return this.timeout = self.setTimeout((function() { return this.$('button').tooltip('hide'); }), 1000);
    }
  });
});
