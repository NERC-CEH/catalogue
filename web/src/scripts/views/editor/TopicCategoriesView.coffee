define [
  'cs!views/editor/ParentView'
  'cs!models/editor/TopicCategory'
  'cs!views/editor/TopicCategoriesItemView'
  'tpl!templates/editor/TopicCategories.tpl'
], (ParentView, TopicCategory, TopicCategoriesItemView, template) -> ParentView.extend
  template:       template
  ModelType:      TopicCategory
  modelAttribute: 'topicCategories'
  ChildView:      TopicCategoriesItemView
