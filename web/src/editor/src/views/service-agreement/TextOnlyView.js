import _ from 'underscore'
import $ from 'jquery'
import SingleView from '../../SingleView'

export default SingleView.extend({

  className: 'component component--textonly',

  initialize (options) {
    this.template = _.template('<%= data.text %>')
    SingleView.prototype.initialize.call(this, options)
    this.render()
  },

  render () {
    SingleView.prototype.render.apply(this)
    const text = this.template({ data: _.extend({}, this.data, { value: this.model.get }) })
    // A question with no section label names the field it introduces itself (dri-one #462)
    if (this.data.labels && !this.data.label) {
      this.$('.dataentry').append($('<label>').attr('for', `input-${this.data.labels}`).html(text))
    } else {
      this.$('.dataentry').append(text)
    }
    return this
  }
})
