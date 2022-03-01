define [
  'jquery'
  'backbone'
  'tpl!templates/ClipboardCopy.tpl'
], 
($, Backbone, template) ->

  ##
  ## Add a <span class="clipboard-copy" data-selector="{my-selector}"></span>
  ## to any element to copy to clipboard.
  ## Replace {my-selector} with the selector for the element you want copied.
  ##

  copy = (selector) ->
      selection = self.getSelection()
      range = document.createRange()
      copyContent = document.querySelector selector
      
      if copyContent
        range.selectNode copyContent
        selection.empty()
        selection.addRange range
        document.execCommand 'copy'
        selection.empty()

  Backbone.View.extend

    events:
      'click': 'copyToClipboard'

    initialize: ->
      do @render

    render: -> 
      @$el.html template()
      @$('button').tooltip
        title: 'copied'
        trigger: 'click'
      @

    copyToClipboard: (event) ->
      copy $(event.currentTarget).data 'selector'
      self.clearTimeout @timeout
      @timeout = self.setTimeout (-> @$('button').tooltip 'hide'), 1000
