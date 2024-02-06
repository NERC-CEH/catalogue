import EditorView from '../EditorView'
import InputView from '../InputView'
import { TextareaView } from '../views'

export default EditorView.extend({

  initialize () {
    this.sections = [{
      label: 'One',
      views: [
        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Title',
          helpText: `
<p>Title help</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          label: 'Description',
          rows: 17,
          helpText: `
<p>Description help</p>
`
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
