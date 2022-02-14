/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'backbone',
  'cs!views/upload/simple/MessageView'
], ($, Backbone, MessageView) => describe("MessageView", function() {
  let el = null;
  let collection = null;
  let model = null;
  let view = null;

  beforeEach(function() {
    el = $('ul').appendTo($('body'));
    spyOn(MessageView.prototype, 'remove').and.callThrough();
    model = new Backbone.Model({
      message: 'Hello World',
      type: 'info'
    });
    collection = new Backbone.Collection([model]);
    return view = new MessageView({
      el,
      model
    });
  });

  afterEach(() => el.remove());

  return it('removed when model is destroyed', function() {
    //when
    collection.pop();

    //then
    return expect(view.remove).toHaveBeenCalled();
  });
}));
