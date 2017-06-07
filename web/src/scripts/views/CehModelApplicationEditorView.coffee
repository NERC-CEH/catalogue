define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/KeywordView'
  'cs!views/editor/SelectView'
  'cs!views/editor/ReferenceView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/DataInfoView'
], (
  EditorView,
  InputView,
  TextareaView,
  ParentView,
  ParentStringView,
  KeywordView,
  SelectView,
  ReferenceView
  SingleObjectView
  DataInfoView
) -> EditorView.extend

  initialize: ->

    @model.set('type', 'modelApplication') unless @model.has('type')

    @sections = [
      label: 'Project Info'
      title: 'Project Info'
      views: [

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Project title'
          helpText: """
                    <p>Title of project</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'projectObjectives'
          label: 'Project objectives'
          rows: 17
          helpText: """
                    <p>Brief description of the main objectives</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Project description'
          rows: 17
          helpText: """
                    <p>Longer description of project incl. why models were used to answer the science question, assumptions made, key outputs</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'keywords'
          label: 'Keywords'
          ObjectInputView: KeywordView
          helpText: """
                    <p>5-10 keywords to enable searching for the project</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'projectCompletionDate'
          label: 'Project completion date'
          helpText: """
                    <p>Project end date</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'projectWebsite'
          label: 'Project website'
          helpText: """
                    <p>Public-facing website if available e.g. http://www.ceh.ac.uk/our-science/projects/upscape</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'funderDetails'
          label: 'Funder details'
          rows: 3
          helpText: """
                    <p>Funder details, including grant number if appropriate</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'contactName'
          label: 'Contact name'
          helpText: """
                    <p>Name of CEH PI/project representative</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'contactNameEmail'
          label: 'Contact name email'
          helpText: """
                    <p>Email of CEH PI/project representative e.g. someone@ceh.ac.uk</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'multipleModelsUsed'
          label: 'Multiple models used?'
          rows: 7
          helpText: """
                    <p>Were multiple models used in the project? If so, which ones?</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'multipleModelLinkages'
          label: 'Multiple model linkages'
          rows: 7
          helpText: """
                    <p>If multiple models were used how was this done e.g. chained, independent runs, comparisons, ensemble</p>
                    """

      ]
    ,
      label: 'References'
      title: 'References'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'references'
          label: 'References'
          ObjectInputView: ReferenceView
          multiline: true
          helpText: """
                    <p>Citation - Add publication citation here</p>
                    <p>DOI - DOI link for the citation e.g. http://dx.doi.org/10.1179/2042349715Y.0000000010</p>
                    <p>NORA - NORA links of the citation e.g. http://nora.nerc.ac.uk/513147/</p>
                    """
      ]
    ,
      label: 'Model Info'
      title: 'Model Info'
      views: [

      ]
    ,
      label: 'Data Info'
      title: 'Data Info'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'inputData'
          label: 'Input Data'
          ObjectInputView: DataInfoView
          multiline: true
          helpText: """
                    <p>Citation - Add publication citation here</p>
                    <p>DOI - DOI link for the citation e.g. http://dx.doi.org/10.1179/2042349715Y.0000000010</p>
                    <p>NORA - NORA links of the citation e.g. http://nora.nerc.ac.uk/513147/</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @