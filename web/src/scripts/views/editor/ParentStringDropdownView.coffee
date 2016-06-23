define [
  'cs!views/editor/ParentStringView'
  'tpl!templates/editor/MultiStringDropdown.tpl'
], (ParentStringView, childTemplate) -> ParentStringView.extend

  childTemplate: childTemplate

  render: ->
    do @renderParent
    $attach = @$(".existing")
    _.each @array, (string, index) =>
      $attach.append @childTemplate data: _.extend {}, @data,
        index: index
      @$("#select#{@data.modelAttribute}#{index}").val string
    @
