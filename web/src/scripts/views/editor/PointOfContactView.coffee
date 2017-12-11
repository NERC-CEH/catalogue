define [
  'underscore'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/PointOfContact.tpl'
], (_, ObjectInputView, template) -> ObjectInputView.extend

  template: template

  initialize: ->
    @model.set 'role', 'pointOfContact'
    ObjectInputView.prototype.initialize.apply @

  render: ->
    ObjectInputView.prototype.render.apply @
    @

  modify: (event) ->
    $target = $(event.target)
    name = $target.data('name')
    value = $target.val()

    if _.contains(['deliveryPoint', 'city', 'administrativeArea', 'country', 'postalCode'], name)
      address = _.clone @model.get 'address'
      if value
        address[name] = value
        @model.set 'address', address
      else
        address = _.omit address, name
        @model.set 'address', address
    else
      if value
        @model.set name, value
      else
        @model.unset name