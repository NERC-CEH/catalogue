import EditorView from './EditorView'
import SelectView from './SelectView'

export var CatalogueView = EditorView.extend({

  initialize () {
    this.sections = [{
      label: 'One',
      title: 'Catalogue',
      views: [
        new SelectView({
          model: this.model,
          modelAttribute: 'value',
          label: 'Catalogue',
          options: this.model.options,
          helpText: '<p>CatalogueApp</p>'
        })
      ]
    }
    ]
    return EditorView.prototype.initialize.apply(this)
  }
})
