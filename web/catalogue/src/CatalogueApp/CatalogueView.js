import { EditorView, SelectView } from '../Editor'

export default EditorView.extend({

  initialize () {
    this.sections = [{
      label: 'One',
      title: 'Catalogue',
      views: [
        new SelectView({
          model: this.model,
          modelAttribute: 'value',
          label: 'Catalogue',
          options: this.model.get('options'),
          helpText: '<p>Catalogue</p>'
        })
      ]
    }
    ]
    return EditorView.prototype.initialize.apply(this)
  }
})
