define [
  'underscore'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/Contact.tpl'
], (_, ObjectInputView, template) -> ObjectInputView.extend

  template: template

  render: ->
    ObjectInputView.prototype.render.apply @
    @$('select').val @model.get 'role'
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