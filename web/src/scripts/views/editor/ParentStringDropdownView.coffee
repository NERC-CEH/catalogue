define [
  'cs!views/editor/ParentStringView'
  'tpl!templates/editor/MultiStringDropdown.tpl'
], (ParentStringView, childTemplate) -> ParentStringView.extend

  childTemplate: childTemplate

  render: ->
    ParentStringView.prototype.render.apply @
    @$('select').val @model.get 'value'
    console.log #{@$('select').val}
    console.log 'done'
    @
