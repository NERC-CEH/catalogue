define [
  'underscore'
  'cs!views/editor/ParentStringView'
  'tpl!templates/editor/MultiStringDropdown.tpl'
], (_, ParentStringView, childTemplate) -> ParentStringView.extend

  ###
    Edit a list of strings, the value of the string comes
    from the options of a dropdown list.
  ###

  childTemplate: childTemplate

  render: ->
    do @renderParent
    $attach = @$(".existing")
    _.each @array, (string, index) =>
      $attach.append @childTemplate data: _.extend {}, @data,
        index: index
      @$("#select#{@data.modelAttribute}#{index}").val string
    @
