import { ParentStringView } from '../views'
import { EditorView, InputView } from '../index'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'dataset') }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [
        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Title'
        }),
        new InputView({
          model: this.model,
          modelAttribute: 'description',
          label: 'Description'
        }),
        new InputView({
          model: this.model,
          modelAttribute: 'version',
          label: 'Version'
        }),
        new InputView({
          model: this.model,
          modelAttribute: 'masterUrl',
          label: 'Master URL'
        }),
        new ParentStringView({
          model: this.model,
          modelAttribute: 'owners',
          label: 'Owners'
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
