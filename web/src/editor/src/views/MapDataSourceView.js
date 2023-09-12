import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import MapReprojectionView from './MapReprojectionView'
import MapFeaturesView from './MapFeaturesView'
import MapAttributeView from './MapAttributeView'
import template from '../templates/MapDataSource.tpl'

import ChildView from './ChildView'

export default ObjectInputView.extend({

  events: function () {
    return _.extend({}, ObjectInputView.prototype.events, {
      'click .addReprojection': 'addReprojection',
      'click .addAttribute': 'addAttribute',
      'click [styleMode]': 'updateStyleMode'
    })
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
    // eslint-disable-next-line no-unused-vars
    const mapFeaturesView = new MapFeaturesView({
      el: this.$('.features'),
      model: this.model.getRelated('features')
    })

    this.setStyleMode(this.model.stylingMode)

    this.listenTo(this.model, 'change:type', this.handlerByteTypeVisibility)

    // Set the radio button to the byteType of the model
    this.updateByteRadioButton()
  },

  addReprojection () {
    this.reprojections.add({})
  },

  addAttribute () {
    this.attributes.add({})
  },

  newReprojection (model, i) {
    // eslint-disable-next-line no-unused-vars
    return new MapReprojectionView({
      model,
      disabled: this.data.disabled
    })
  },

  newAttribute (model, i) {
    // eslint-disable-next-line no-unused-vars
    return new ChildView({
      model,
      index: i,
      ObjectInputView: MapAttributeView,
      disabled: this.data.disabled
    })
  },

  updateStyleMode (e) {
    this.setStyleMode($(e.target).attr('styleMode'))
  },

  setStyleMode (mode) {
    // Reset the state of all the styling buttons and update to the correct mode
    this.$('button[stylemode]').removeClass('btn-success').removeClass('active')
    this.$(`button[stylemode='${mode}']`).addClass('btn-success active')
    this.$('.styling-box').hide()
    if (mode === 'features') {
      this.$('.styling-box.features').show()
      this.$('.addAttribute').addClass('disabled')
    }
    if (mode === 'attributes') {
      this.$('.styling-box.attributes').show()
      this.$('.addAttribute').removeClass('disabled')
    }
    this.updateByteTypeVisibility(mode, this.model.get('type'))
    this.model.setStylingMode(mode)
  },

  handlerByteTypeVisibility (e) { this.updateByteTypeVisibility(e.stylingMode, e.attributes.type) },

  /*
  Update the bytetype radio button to match the model
  */
  updateByteRadioButton () {
    $(this.$('input[data-name=\'bytetype\'][value=\'#{this.model.attributes.bytetype}\']')[0]).attr('checked', 'checked')
  },

  /*
  Set the visibility of the byteType selector.  Only show it when the styling is 'attributes' and the type is 'RASTER'
  */
  updateByteTypeVisibility (stylingMode, type) {
    if ((stylingMode.toLowerCase() === 'attributes') && (type.toLowerCase() === 'raster')) { this.$('.byte-box').show() } else { this.$('.byte-box').hide() }
  }
})
