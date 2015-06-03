define [
  'cs!views/editor/ChildView'
  'underscore'
  'tpl!templates/editor/ContactsItem.tpl'
  'cs!models/editor/Contact'
], (ChildView, _, template, Contact) -> ChildView.extend

  template: template
  ModelType: Contact
  className: 'component'

  render: ->
    ChildView.prototype.render.apply @
    @$('select').val @model.get 'role'

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