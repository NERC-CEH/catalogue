import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import ObservedPropertyView from './ObservedPropertyView'
import template from '../templates/Fileset'
import dropdownTemplate from '../templates/PredefinedParentDropdown'
import { fetchVariablesFromLegilo } from './LegiloFetcher'
import variablesTemplate from '../templates/LegiloVariables'
import LegiloView from './LegiloView'
import { variableOnSelect } from './LegiloEventHandler'
import { ObservedProperty } from '../models'

import ChildLargeView from './ChildLargeView'

export default ObjectInputView.extend({
  events: function () {
    return _.extend({}, ObjectInputView.prototype.events, {
      'click .dropdown-menu': 'addObservedProperty',
      'mousedown #addObservedPropertyBtn': 'updateZIndex',
      'hide.bs.dropdown #addObservedPropertyBtn': 'dropdownClose'
    })
  },

  initialize (options) {
    this.template = template
    this.dropdownTemplate = dropdownTemplate
    ObjectInputView.prototype.initialize.call(this, options)
    this.predefined = options.predefined
    _.chain(this.predefined)
      .keys()
      .each(item => this.$('ul.dropdown-menu').prepend(this.dropdownTemplate({ predefined: item })))

    this.zIndex = 1
    this.observedPropertyList = this.model.getObservedProperty()
    this.collection = this.createList(this.observedPropertyList, '.observedProperty', this.newObservedProperty)

    if (options.fetchVariablesButton) {
      this.legiloVariables = new LegiloView({
        collection: this.collection,
        model: options.parentModel,
        modelType: ObservedProperty,
        template: variablesTemplate,
        fetcher: fetchVariablesFromLegilo,
        fetchButton: this.$('.legilo-variables-btn'),
        result: this.$('.legilo-variables-view'),
        onSelect: variableOnSelect
      })
    }
  },

  addObservedProperty (event) {
    event.preventDefault()
    const value = $(event.target).text()
    this.observedPropertyList.add({ ...this.predefined[value], constraints: {} })
  },

  newObservedProperty (model, i) {
    const view = new ChildLargeView({
      model,
      index: i,
      ObjectInputView: ObservedPropertyView
    })
    view.$('.observedProperty').addClass('border border-1 p-3')
    return view
  },

  updateZIndex (event) {
    const currentFileset = this.$(event.target).closest('.dataentry').closest('.row')
    currentFileset.css({
      position: 'relative',
      'z-index': this.zIndex
    })
    this.zIndex++
  },

  dropdownClose (event) {
    const currentFileset = this.$(event.target).closest('.dataentry').closest('.row')
    currentFileset.css('position', '')
  }
})
