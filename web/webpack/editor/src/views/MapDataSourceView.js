import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import MapReprojectionView from './MapReprojectionView'
import ChildView from './ChildView'
import MapFeaturesView from './MapFeaturesView'
import MapAttributeView from './MapAttributeView'
import template from '../templates/MapDataSource.tpl'
import $ from 'jquery'

export default ObjectInputView.extend({

  template,

  events () {
    return _.extend({}, ObjectInputView.prototype.events, {
      'click .addReprojection': 'addReprojection',
      'click .addAttribute': 'addAttribute',
      'click [styleMode]': 'updateStyleMode'
    }
    )
  },

  dataTypes: [
    { name: 'Polygon', value: 'POLYGON' },
    { name: 'Raster', value: 'RASTER' },
    { name: 'Point', value: 'POINT' },
    { name: 'Line', value: 'LINE' }
  ],

  initialize (options) {
    this.template = _.template(template)
    ObjectInputView.prototype.initialize.call(this, _.extend({}, options,
      { types: this.dataTypes })
    )

    this.reprojections = this.model.getRelatedCollection('reprojections')
    this.attributes = this.model.getAttributes()

    this.createList(this.reprojections, '.reprojections', this.newReprojection)
    this.createList(this.attributes, '.attributes', this.newAttribute)

    const mapFeaturesView = new MapFeaturesView({
      el: this.$('.features'),
      model: this.model.getRelated('features')
    })

    this.setStyleMode(this.model.stylingMode)

    this.listenTo(this.model, 'change:type', this.handlerByteTypeVisibility)

    // Set the radio button to the byteType of the model
    return (this.updateByteRadioButton)()
  },

  addReprojection () { return this.reprojections.add({}) },
  addAttribute () { return this.attributes.add({}) },

  newReprojection (model, i) {
    return new MapReprojectionView({
      model,
      disabled: this.data.disabled
    })
  },

  newAttribute (model, i) {
    return new ChildView({
      model,
      index: i,
      ObjectInputView: MapAttributeView,
      disabled: this.data.disabled
    })
  },

  updateStyleMode (e) { return this.setStyleMode($(e.target).attr('styleMode')) },

  setStyleMode (mode) {
    // Reset the state of all the styling buttons and update to the correct mode
    this.$('button[stylemode]').removeClass('btn-success').removeClass('active')
    this.$(`button[stylemode='${mode}']`).addClass('btn-success active')
    this.$('.styling-box').hide()
    this.$(`.styling-box.${mode}`).show()
    this.updateByteTypeVisibility(mode, this.model.get('type'))

    const attrBtn = this.$('.addAttribute').removeClass('disabled')
    if (mode === 'features') { attrBtn.addClass('disabled') }
    return this.model.setStylingMode(mode)
  },

  handlerByteTypeVisibility (e) { return this.updateByteTypeVisibility(e.stylingMode, e.attributes.type) },

  /*
  Update the bytetype radio button to match the model
  */
  updateByteRadioButton () {
    return $(this.$(`input[data-name='bytetype'][value='${this.model.attributes.bytetype}']`)[0]).attr('checked', 'checked')
  },

  /*
  Set the visibility of the byteType selector.  Only show it when the styling is 'attributes' and the type is 'RASTER'
  */
  updateByteTypeVisibility (stylingMode, type) {
    if ((stylingMode.toLowerCase() === 'attributes') && (type.toLowerCase() === 'raster')) { return this.$('.byte-box').show() } else { return this.$('.byte-box').hide() }
  }
})
