define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
 	'cs!views/editor/SelectView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/ParentView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/PredefinedParentView'
	'cs!views/editor/KeywordView'
  'cs!views/editor/ContactView'
  'cs!models/editor/Contact'
  'cs!models/editor/BoundingBox'
  'cs!views/editor/BoundingBoxView'
  'cs!views/editor/DatasetReferenceDateView'
  'cs!models/editor/MultipleDate'
  'cs!models/editor/InspireTheme'
  'cs!views/editor/InspireThemeView'
  'cs!views/editor/TemporalExtentView'
  'cs!views/editor/ResourceConstraintView'
  'cs!views/editor/ReviewView'
  'cs!views/editor/RelatedRecordView'

], (
  EditorView
  InputView
  TextareaView
  SelectView
  SingleObjectView
  ParentView
  ParentStringView
  PredefinedParentView
  KeywordView
  ContactView
  Contact
  BoundingBox
  BoundingBoxView
  DatasetReferenceDateView
  MultipleDate
  InspireTheme
  InspireThemeView
  TemporalExtentView
  ResourceConstraintView
  ReviewView
  RelatedRecordView
) -> EditorView.extend

  initialize: ->
    @model.set 'type', 'notebook' unless @model.has 'type'

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [
          new SelectView
            model: @model
            modelAttribute: 'type'
            label: 'Record type'
            options: [
              {value: 'notebook', label: 'Notebook'},
              {value: 'codeProject', label: 'Code project'},
              {value: 'codeSnippet', label: 'Code snippet'}
            ]

          new InputView
            model: @model
            modelAttribute: 'title'
            label: 'Title'
          
          new TextareaView
            model: @model
            modelAttribute: 'description'
            label: 'Description'
            rows: 5

          new InputView
            model: @model
            modelAttribute: 'masterUrl'
            label: 'Master URL'
          
          new InputView
            model: @model
            modelAttribute: 'primaryLanguage'
            label: 'Primary language'
            listAttribute: """
                  <option value='Python' />
                  <option value='R' />
                  """

          new InputView
            model: @model
            modelAttribute: 'secondaryLanguage'
            label: 'Secondary language'
            listAttribute: """
                  <option value='Python' />
                  <option value='R' />
                  """

          new SelectView
            model: @model
            modelAttribute: 'assetType'
            label: 'Type'
            options: [
              {value: '', label: ''},
              {value: 'Jupyter notebook', label: 'Jupyter notebook'},
              {value: 'Zeppelin notebook', label: 'Zeppelin notebook'},
              {value: 'RStudio project', label: 'RStudio project'},
              {value: 'RShiny app', label: 'RShiny app'}
            ]
            helpText: """
                      <p>(only relevant for notebooks)</p>
                      """

          new InputView
            model: @model
            modelAttribute: 'version'
            label: 'Version'

          new SingleObjectView
            model: @model
            modelAttribute: 'referenceDate'
            ModelType: MultipleDate
            label: 'Dates'
            ObjectInputView: DatasetReferenceDateView

        ]
      ,
        label: 'Inputs & Outputs'
        title: 'Inputs, outputs, packages and review'
        views: [        

          new ParentStringView
            model: @model
            modelAttribute: 'inputs'
            label: 'Inputs'

          new ParentStringView
            model: @model
            modelAttribute: 'outputs'
            label: 'Outputs'

          new ParentStringView
            model: @model
            modelAttribute: 'packages'
            label: 'Packages'
          
          new ParentView
            model: @model
            modelAttribute: 'review'
            label: 'Review'
            ObjectInputView: ReviewView
            multiline: true

        ]
      ,
        label: 'Licensing & Contacts'
        title: 'Licensing and contacts'
        views: [        

          new PredefinedParentView
            model: @model
            modelAttribute: 'useConstraints'
            label: 'Use constraints'
            ObjectInputView: ResourceConstraintView
            multiline: true
            predefined:
              'Licence - OGL':
                value: 'Open Government Licence v3'
                uri: 'https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/'
                code: 'license'
              'Licence - GNU GPLv3':
                value: 'GNU'
                uri: 'http://www.gnu.org/licenses/gpl-3.0.txt'
                code: 'license'
              'Licence - CC-BY':
                value: 'Attribution 4.0 International (CC BY 4.0)'
                uri: 'https://creativecommons.org/licenses/by/4.0/'
                code: 'license'
              'Licence - CC-BY-SA':
                value: 'Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)'
                uri: 'https://creativecommons.org/licenses/by-sa/4.0/'
                code: 'license'

          new PredefinedParentView
            model: @model
            ModelType: Contact
            modelAttribute: 'responsibleParties'
            label: 'Contacts'
            ObjectInputView: ContactView
            multiline: true
            predefined:
              'UKCEH owner':
                organisationName: 'UK Centre for Ecology & Hydrology'
                role: 'owner'
                email: 'enquiries@ceh.ac.uk'
                organisationIdentifier: 'https://ror.org/00pggkr55'
              'Other owner':
                role: 'owner'
         ]
      ,
        label: 'Classification'
        title: 'Classification'
        views: [        
          new PredefinedParentView
            model: @model
            modelAttribute: 'boundingBoxes'
            ModelType: BoundingBox
            label: 'Spatial extent'
            ObjectInputView: BoundingBoxView
            multiline: true
            predefined:
              'England':
                northBoundLatitude: 55.812
                eastBoundLongitude: 1.768
                southBoundLatitude: 49.864
                westBoundLongitude: -6.452
                extentName: 'England'
                extentUri: 'http://sws.geonames.org/6269131'
              'Great Britain':
                northBoundLatitude: 60.861
                eastBoundLongitude: 1.768
                southBoundLatitude: 49.864
                westBoundLongitude: -8.648
                extentName: 'Great Britain'
              'Northern Ireland':
                northBoundLatitude: 55.313
                eastBoundLongitude: -5.432
                southBoundLatitude: 54.022
                westBoundLongitude: -8.178
                extentName: 'Northern Ireland'
                extentUri: 'http://sws.geonames.org/2641364'
              Scotland:
                northBoundLatitude: 60.861
                eastBoundLongitude: -0.728
                southBoundLatitude: 54.634
                westBoundLongitude: -8.648
                extentName: 'Scotland'
                extentUri: 'http://sws.geonames.org/2638360'
              'United Kingdom':
                northBoundLatitude: 60.861
                eastBoundLongitude: 1.768
                southBoundLatitude: 49.864
                westBoundLongitude: -8.648
                extentName: 'United Kingdom'
                extentUri: 'http://sws.geonames.org/2635167'
              Wales:
                northBoundLatitude: 53.434
                eastBoundLongitude: -2.654
                southBoundLatitude: 51.375
                westBoundLongitude: -5.473
                extentName: 'Wales'
                extentUri: 'http://sws.geonames.org/2634895'
              World:
                northBoundLatitude: 90.00
                eastBoundLongitude: 180.00
                southBoundLatitude: -90.00
                westBoundLongitude: -180.00

          new ParentView
            model: @model
            modelAttribute: 'temporalExtents'
            ModelType: MultipleDate
            label: 'Temporal extent'
            ObjectInputView: TemporalExtentView

          new ParentView
            model: @model
            ModelType: InspireTheme
            modelAttribute: 'inspireThemes'
            label: 'INSPIRE theme'
            ObjectInputView: InspireThemeView

          new ParentView
            model: @model
            modelAttribute: 'keywords'
            label: 'Keywords'
            ObjectInputView: KeywordView

          new ParentView
            model: @model
            modelAttribute: 'relatedRecords'
            label: 'Related records'
            ObjectInputView: RelatedRecordView
            multiline: true

      ]
    ]

    EditorView.prototype.initialize.apply @