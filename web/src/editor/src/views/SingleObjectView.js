import _ from 'underscore'
import SingleView from '../SingleView'

export default SingleView.extend({

  initialize (options) {
    SingleView.prototype.initialize.call(this, options)
    // The control is rendered by the object view, so the section label can only name it if that
    // view says it renders input-<modelAttribute>
    this.labelsControl = Boolean(this.data.ObjectInputView.prototype.labelsControl)
    this.render()

    this.inputModel = new this.data.ModelType(this.model.get(this.data.modelAttribute))
    this.listenTo(this.inputModel, 'change', this.updateMetadataModel)
    this.listenTo(this.model, 'sync', function (model) {
      this.inputModel.set(model.get(this.data.modelAttribute))
    })
    /* eslint no-new: "off" */
    this.objectInputView = new this.data.ObjectInputView(_.extend({}, this.data, {
      el: this.$('.dataentry'),
      model: this.inputModel,
      // Templates build element ids from the index; an editor can hold several single-object fields
      // of the same type, so it has to be unique on the page
      index: this.inputModel.cid
    }))
  },

  show () {
    SingleView.prototype.show.apply(this)
    if (this.data.modelAttribute === 'boundingBox') {
      this.objectInputView.viewMap()
    }
  }
})
