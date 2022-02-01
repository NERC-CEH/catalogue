/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/EditorView',
  'cs!views/editor/SelectView'
  ], function(EditorView, SelectView) { return EditorView.extend({

  initialize() {

    this.sections = [{
      label: 'One',
      title:  'Catalogue',
      views: [
        new SelectView({
          model: this.model,
          modelAttribute: 'value',
          label: 'Catalogue',
          options: this.model.options,
          helpText: `\
<p>Catalogue</p>\
`
        })
      ]
    }
    ];
    return EditorView.prototype.initialize.apply(this);
  }
  });
 });
