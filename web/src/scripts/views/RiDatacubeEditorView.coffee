define [
	'cs!views/EditorView'
	'cs!views/editor/InputView'
	'cs!views/editor/TextareaView'
	'cs!views/editor/SingleObjectView'
	'cs!views/editor/SingleView'
	'cs!views/editor/SelectView'
	'cs!views/editor/ReadOnlyView'
	'cs!views/service-agreement/TextOnlyView'
	'cs!views/editor/ParentView'
	'cs!views/editor/ParentLargeView'
	'cs!views/editor/ParentStringView'
	'cs!views/editor/ParentStringTextboxView'
	'cs!views/editor/PredefinedParentView'
	'cs!views/editor/PredefinedParentLargeView'
	'cs!views/editor/KeywordView'
	'cs!models/editor/Contact'
	'cs!views/editor/ContactView'
	'cs!models/editor/BoundingBox'
	'cs!views/editor/BoundingBoxView'
	'cs!views/editor/RelatedRecordView'
	'cs!views/editor/RiOnlineLinkView'
	'cs!models/editor/RiChallenge'
	'cs!views/editor/RiChallengeView'

], (EditorView, InputView, TextareaView, SingleObjectView, SingleView, SelectView, ReadOnlyView, TextOnlyView, ParentView, ParentLargeView, ParentStringView, ParentStringTextboxView, PredefinedParentView, PredefinedParentLargeView, KeywordView, Contact, ContactView, BoundingBox, BoundingBoxView, RelatedRecordView, RiOnlineLinkView, RiChallenge, RiChallengeView) -> EditorView.extend

	initialize: ->
		@model.set('type', 'riDatacube') unless @model.has('type')

		@sections = [
			label: 'General'
			title: 'General'
			views: [

				new InputView
					model: @model
					modelAttribute: 'title'
					label: 'Name'
					helpText: """
                    	<p>Should reflect purpose (succinctly)</p><p>Should be consistent (within and across assets)</p>
                    """

				new TextOnlyView
					model: @model
					label: 'Purpose'
					text: """<p>Explain strategic relevance: What does it do? Why? Who cares?<br>Write in plain English and avoid (or define) acronyms.<br>Explain relevance to government policy agenda</p>
							"""

				new TextareaView
					model: @model
					modelAttribute: 'description'
					rows: 6

				new PredefinedParentView
					model: @model
					ModelType: Contact
					modelAttribute: 'owners'
					label: 'Owner'
					ObjectInputView: ContactView
					multiline: true
					predefined:
						'UKCEH Bangor':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Bangor'
						'UKCEH Edinburgh':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Edinburgh'
						'UKCEH Lancaster':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Lancaster'
						'UKCEH Wallingford':
							organisationName: 'UK Centre for Ecology & Hydrology'
							role: 'owner'
							organisationIdentifier: 'https://ror.org/00pggkr55'
							address:
                				city: 'Wallingford'

			]
		,  
			label: 'Categorisation'
			title: 'Categorisation'
			views: [

				new SelectView
					model: @model
					modelAttribute: 'infrastructureCategory'
					label: 'Infrastructure category'
					options: [
						{value: 'Instrumented sites', label: 'Instrumented sites'},
						{value: 'Periodic surveys', label: 'Periodic surveys'},
						{value: 'Wildlife monitoring schemes', label: 'Wildlife monitoring schemes'},
						{value: 'Discovery collections and archives', label: 'Discovery collections and archives'},
						{value: 'Mobile observing platforms', label: 'Mobile observing platforms'},
						{value: 'Controlled environment facilities (micro- or mesocosms)', label: 'Controlled environment facilities (micro- or mesocosms)'},
						{value: 'Field research facilities (macrocosms)', label: 'Field research facilities (macrocosms)'},
						{value: 'Legacy Experimental Platforms or sites', label: 'Legacy Experimental Platforms or sites'},
						{value: 'Analytical facilities', label: 'Analytical facilities'},
						{value: 'Test facilities', label: 'Test facilities'},
						{value: 'Digital computing platforms ', label: 'Digital computing platforms'},
						{value: 'Data and information', label: 'Data and information'},
						{value: 'Digital labs for data analytics', label: 'Digital labs for data analytics'},
						{value: 'UKCEH models', label: 'UKCEH models'},
						{value: 'Community models', label: 'Community models'}
					]

				new ParentView
					model: @model
					ModelType: RiChallenge
					modelAttribute: 'infrastructureChallenge'
					label: 'Challenge/goal'
					ObjectInputView: RiChallengeView

				new SelectView
					model: @model
					modelAttribute: 'scienceArea'
					label: 'Science area'
					options: [
						{value: 'Atmospheric Chemistry and Effects', label: 'Atmospheric Chemistry and Effects'},
						{value: 'Biodiversity', label: 'Biodiversity'},
						{value: 'Hydro-climate Risks', label: 'Hydro-climate Risks'},
						{value: 'Pollution', label: 'Pollution'},
						{value: 'Soils and Land Use', label: 'Soils and Land Use'},
						{value: 'Water Resources', label: 'Water Resources'}
					]

				new ParentView
					model: @model
					modelAttribute: 'keywords'
					label: 'Other keywords'
					ObjectInputView: KeywordView


			]
		,  
			label: 'Scale'
			title: 'Scale'
			views: [

				new TextareaView
					model: @model
					modelAttribute: 'locationText'
					label: 'Location'
				
				new SelectView
					model: @model
					modelAttribute: 'infrastructureScale'
					label: 'Scale'
					options: [
						{value: 'UK', label: 'UK-wide'},
						{value: 'Landscape or catchment', label: 'Landscape or catchment'},
						{value: 'Local area', label: 'Local area'}
					]

				new PredefinedParentView
					model: @model
					modelAttribute: 'boundingBoxes'
					ModelType: BoundingBox
					label: 'Spatial extent'
					ObjectInputView: BoundingBoxView
					multiline: true
					predefined:
						'Great Britain':
							northBoundLatitude: 60.861
							eastBoundLongitude: 1.768
							southBoundLatitude: 49.864
							westBoundLongitude: -8.648

			]
		,  
			label: 'Capability'
			title: 'Capability'
			views: [

				new TextareaView
					model: @model
					modelAttribute: 'capabilities'
					label: 'Capabilities'				
					helpText: """
                    	<p>Describe the facility, experimental design.  What is it equipped to do or measure?</p><p>Be informative for external partners and users</p>
                    """

				new TextareaView
					model: @model
					modelAttribute: 'lifecycle'
					label: 'Lifecycle'				

				new TextareaView
					model: @model
					modelAttribute: 'uniqueness'
					label: 'Uniqueness'				


			]
		,  
			label: 'Use'
			title: 'Use'
			views: [

				new TextareaView
					model: @model
					modelAttribute: 'partners'
					label: 'Partners'				
									
				new ParentStringView
					model: @model
					modelAttribute: 'users'
					label: 'Users'		

				new TextareaView
					model: @model
					modelAttribute: 'access'
					label: 'Access'				

				new TextareaView
					model: @model
					modelAttribute: 'userCosts'
					label: 'User costs'	

				new TextareaView
					model: @model
					modelAttribute: 'fundingSources'
					label: 'Funding sources'
					helpText: """
                    	<p>Include all funding sources</p><p>Be specific about NC awards/programmes</p>
                    """

			]
		,  
			label: 'Other'
			title: 'Other'
			views: [

				new ParentView
					model: @model
					modelAttribute: 'onlineResources'
					label: 'Online resources'
					ObjectInputView: RiOnlineLinkView
					multiline: true

				new ParentView
					model: @model
					modelAttribute: 'relatedRecords'
					label: 'Related records'
					ObjectInputView: RelatedRecordView
					multiline: true
				]
		]

		EditorView.prototype.initialize.apply @
