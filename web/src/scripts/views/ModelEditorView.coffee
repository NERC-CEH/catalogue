define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/ParentStringView'
  ], (EditorView, InputView, ParentStringView) -> EditorView.extend
  
  initialize: ->
  
	@sections = [
      label: 'One'
      title:  'General information'
      views: [
		new InputView
			  model: @model
			  modelAttribute: 'title'
			  label: 'Title'
			  
		new ParentStringView
          model: @model
          modelAttribute: 'keyReferences'
          label: 'Key References'
		]
	]