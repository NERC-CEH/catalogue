define [
  'cs!views/editor/ObjectInputView'
  'tpl!templates/service-agreement/EndUserLicence.tpl'
], (ObjectInputView, template) -> ObjectInputView.extend

  template: template

  className: 'col-sm-3'

  events:
    'change .ogl': 'setOgl'
    'change .other': 'setOther'
    'change .value': 'setValue'
    'change .uri': 'setUri'

  initialize: (options) ->
    ObjectInputView.prototype.initialize.call @, options
    @$resourceConstraint = @$('#resourceConstraint')
    hasUri = @model.has('uri')
    hasValue = @model.has('value')

    if hasUri || hasValue
      if hasUri && @model.get('uri') == 'https://eidc.ceh.ac.uk/licences/OGL/plain'
        @$('input.ogl').prop('checked', true)
      else
        @$('input.other').prop('checked', true)
        @$resourceConstraint.removeClass('hidden')
        if hasValue
          @$('.value').val(@model.get 'value')
    else
      @$('input.ogl').prop('checked', true).change()

  setOgl: ->
    @$resourceConstraint.addClass('hidden')
    @model.set
      value: 'This resource is available under the terms of the Open Government Licence'
      code: 'license'
      uri: 'https://eidc.ceh.ac.uk/licences/OGL/plain'

  setOther: ->
    @$resourceConstraint.removeClass('hidden')
    @model.unset 'uri'
    @model.unset 'value'

  setValue:(event) ->
    @model.set
      code: 'license'
      value: event.target.value
