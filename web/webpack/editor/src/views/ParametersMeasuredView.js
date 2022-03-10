/*
 * decaffeinate suggestions:
 * DS101: Remove unnecessary use of Array.from
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/editor/ObjectInputViewForObjects',
  'tpl!templates/editor/ParametersMeasured.tpl'
], function(ObjectInputViewForObjects, template) { return ObjectInputViewForObjects.extend({

  template,

  render() {
    if (!Array.from(this.data).includes('name')) { 
      this.data.name = {};
    }

    if (!Array.from(this.data).includes('unitOfMeasure')) { 
      this.data.unitOfMeasure = {};
    }

    return ObjectInputViewForObjects.prototype.render.apply(this);
  }
});
 });
