define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/KeywordView'
  'cs!views/editor/RelationshipView'
], (
  EditorView,
  InputView,
  TextareaView,
  ParentView,
  KeywordView,
  RelationshipView
) -> EditorView.extend

  initialize: ->

    @model.set('type', 'agent') unless @model.has('type')

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Name'
          helpText: """
                    <p>Name of agent</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'role'
          label: 'Role'
          helpText: """
                    <p>Role of agent</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'address'
          rows: 5
          label: 'Address'
          helpText: """
                    <p>Address of agent</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'keywords'
          label: 'Keywords'
          ObjectInputView: KeywordView
          helpText: """
                    <p>Keywords for discovery</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'relationships'
          label: 'Relationships'
          ObjectInputView: RelationshipView
          multiline: true
          options: [
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/associatedWith', label: 'AssociatedWith'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/creates', label: 'Creates'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/knows', label: 'Knows'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/responsibleFor', label: 'Responsible For'}
          ]
          helpText: """
                    <p>Relationships to other OSDP document types</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @