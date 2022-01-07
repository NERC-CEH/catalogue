/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * DS103: Rewrite code to no longer use __guard__, or convert again using --optional-chaining
 * DS207: Consider shorter variations of null checks
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'jquery',
  'backbone',
  'tpl!templates/Message.tpl'
], function(_, $, Backbone, template) { return Backbone.View.extend({
  el: '#message-panel',

  defaultMessage: 'A requested resource failed to load.',

  events: {
    "click span.fas": "dismissMessage"
  },

  initialize() {
    this.listenTo(this.model, 'error', this.appendError);
    return this.listenTo(this.model, 'info', this.appendInfo);
  },

  /*
  Handle the error event.
  */
  appendError(model, res){
    let message;
    (this.$el.show)();

    if (_.isString(model)) {
      message = model;
    }

    return this.$el.append(template({
      type: 'danger',
      message: message || __guard__(res != null ? res.responseJSON : undefined, x => x.message) || this.defaultMessage
    })
    );
  },

  /*
  Handle the info event.
  */
  appendInfo(message){
    (this.$el.show)();

    return this.$el.append(template({
      type: 'info',
      message
    })
    );
  },

  dismissMessage(evt) { 
    ($(evt.target).parent('.alert').remove)();
    if (this.$el.children().length === 0) { return (this.$el.hide)(); }
  }
});
 });
function __guard__(value, transform) {
  return (typeof value !== 'undefined' && value !== null) ? transform(value) : undefined;
}