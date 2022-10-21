import _ from 'underscore'
import SingleView from '../SingleView'
export default SingleView.extend({

  initialize (options) {
    SingleView.prototype.initialize.call(this, options)
    this.render()

    const inputModel = new this.data.ModelType(this.model.get(this.data.modelAttribute))
    this.listenTo(inputModel, 'change', this.updateMetadataModel)
    this.listenTo(this.model, 'sync', function (model) {
      inputModel.set(model.get(this.data.modelAttribute))
    })

    const that = this
    $(document).ready(function () {
      new that.data.ObjectInputView(_.extend({}, that.data, {
        el: that.$('.dataentry'),
        model: inputModel
      }))
    })
  }
})
