/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'cs!views/editor/SingleView'
], function(_, SingleView) { return SingleView.extend({

  initialize(options) {
    SingleView.prototype.initialize.call(this, options);
    (this.render)();

    const inputModel = new this.data.ModelType(this.model.get(this.data.modelAttribute));
    this.listenTo(inputModel, 'change', this.updateMetadataModel);
    this.listenTo(this.model, 'sync', function(model) {
      return inputModel.set(model.get(this.data.modelAttribute));
    });

    return new this.data.ObjectInputView(_.extend({}, this.data, {
      el: this.$('.dataentry'),
      model: inputModel
    }
    )
    );
  }
});
 });