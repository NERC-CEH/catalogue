"use strict";
(self["webpackChunkweb"] = self["webpackChunkweb"] || []).push([[10],{

/***/ 2010:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

// ESM COMPAT FLAG
__webpack_require__.r(__webpack_exports__);

// EXTERNAL MODULE: ./node_modules/jquery/dist/jquery.js
var jquery = __webpack_require__(9755);
var jquery_default = /*#__PURE__*/__webpack_require__.n(jquery);
// EXTERNAL MODULE: ./editor/src/EditorMetadata.js
var EditorMetadata = __webpack_require__(6813);
;// CONCATENATED MODULE: ./editor/src/LinkEditorMetadata.js

/* harmony default export */ const LinkEditorMetadata = (EditorMetadata/* default.extend */.Z.extend({
  validate: function validate(attrs) {
    return this;
  }
}));
// EXTERNAL MODULE: ./editor/src/EditorView.js + 1 modules
var EditorView = __webpack_require__(4003);
// EXTERNAL MODULE: ./editor/src/InputView.js + 1 modules
var InputView = __webpack_require__(8315);
// EXTERNAL MODULE: ./node_modules/underscore/modules/index-all.js
var index_all = __webpack_require__(8860);
// EXTERNAL MODULE: ./node_modules/backbone/backbone.js
var backbone = __webpack_require__(2316);
var backbone_default = /*#__PURE__*/__webpack_require__.n(backbone);
;// CONCATENATED MODULE: ./editor/src/templates/Validation.tpl
// Module
var code = "<div class=\"alert alert-danger validation\"> <h4><b>Something isn't right</b></h4> <div class=\"warnings text-danger\"> </div> </div> ";
// Exports
/* harmony default export */ const Validation = (code);
;// CONCATENATED MODULE: ./editor/src/views/ObjectInputView.js




/* harmony default export */ const ObjectInputView = (backbone_default().View.extend({
  events: {
    change: 'modify'
  },
  initialize: function initialize(options) {
    this.data = options;
    this.listenTo(this.model, 'remove', function () {
      return this.remove();
    });
    this.listenTo(this.model, 'change', function (model) {
      var _this = this;

      if (model.isValid()) {
        return this.$('>.validation').hide();
      } else {
        this.$('>.validation').show();
        jquery_default()('div.warnings', this.$('>.validation')).html('');
        return index_all/* default.each */.ZP.each(model.validationError, function (error) {
          return jquery_default()('div.warnings', _this.$('>.validation')).append(jquery_default()("<p>".concat(error.message, "</p>")));
        });
      }
    });
    return this.render();
  },
  render: function render() {
    this.$el.html(this.template({
      data: index_all/* default.extend */.ZP.extend({}, this.data, this.model.attributes)
    }));
    this.$el.append(index_all/* default.template */.ZP.template(Validation));
    return this;
  },
  modify: function modify(event) {
    var name = jquery_default()(event.target).data('name');
    var value = jquery_default()(event.target).val();

    if (!value) {
      this.model.unset(name);
    } else {
      this.model.set(name, value);
    }

    return false;
  },
  // disable bubbling

  /*
  Defines a sortable list view which is bound to a positionable collection.
  The supplied `view` callback function is required to generate a constructed
  child view element which will be renederd on to the list
  */
  createList: function createList(collection, selector, view) {
    var _this2 = this;

    var element = this.$(selector);

    var addView = function () {
      var newView = view.apply(this, arguments);
      return element.append(newView.el);
    }.bind(this);

    var resetView = function resetView() {
      element.empty();
      return collection.each(addView, _this2);
    };

    this.listenTo(collection, 'add', addView);
    this.listenTo(collection, 'reset', resetView);
    var pos = null;

    if (!(this.data.disabled === 'disabled')) {
      element.sortable({
        start: function start(event, ui) {
          pos = ui.item.index();
        },
        update: function update(event, ui) {
          collection.position(pos, ui.item.index());
        }
      });
    }

    resetView();
    return collection;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/Textarea.tpl
// Module
var Textarea_code = "<textarea data-name=\"<%= data.modelAttribute %>\" rows=\"<%= data.rows %>\" placeholder=\"<%= data.placeholderAttribute %>\" class=\"editor-textarea\" id=\"input-<%= data.modelAttribute %>\"><%= data.value %></textarea>";
// Exports
/* harmony default export */ const Textarea = (Textarea_code);
;// CONCATENATED MODULE: ./editor/src/views/TextareaView.js



/* harmony default export */ const TextareaView = (InputView/* default.extend */.Z.extend({
  template: index_all/* default.template */.ZP.template(Textarea)
}));
// EXTERNAL MODULE: ./editor/src/SingleView.js + 1 modules
var SingleView = __webpack_require__(2798);
;// CONCATENATED MODULE: ./editor/src/templates/Child.tpl
// Module
var Child_code = "<div class=\"col-sm-11 dataentry child\"></div> <div class=\"col-sm-1\"> <button class=\"editor-button-xs remove\" <%= data.disabled%> ><span class=\"fas fa-times\" aria-hidden=\"true\"></span></button> </div>";
// Exports
/* harmony default export */ const Child = (Child_code);
;// CONCATENATED MODULE: ./editor/src/views/ChildView.js



/* harmony default export */ const ChildView = (backbone_default().View.extend({
  className: 'row',
  events: {
    'click button.remove': 'delete'
  },
  initialize: function initialize(options) {
    this.childTemplate = index_all/* default.template */.ZP.template(Child);
    this.data = options;
    this.listenTo(this.model, 'remove', function () {
      this.remove();
    });
    this.index = this.model.collection.indexOf(this.model);
    this.render();
    var view = new this.data.ObjectInputView(index_all/* default.extend */.ZP.extend({}, this.data, {
      el: this.$('.dataentry'),
      model: this.model,
      index: this.index
    }));
  },
  render: function render() {
    this.$el.html(this.childTemplate({
      index: this.index,
      data: this.data
    }));
    return this;
  },
  "delete": function _delete() {
    this.model.collection.remove(this.model);
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/Parent.tpl
// Module
var Parent_code = "<div class=\"row\"> <div class=\"col-sm-3\"> <label> <%= data.label %> <% if(data.helpText) { %> <a data-toggle=\"collapse\" title=\"Click for help\" href=\"#help-<%= data.modelAttribute.replace('.', '\\.') %>\" data-parent=\"#editor\"><i class=\"fas fa-question-circle\"></i></a> <% } %> </label> <button class=\"editor-button add\" <%= data.disabled%>>Add <span class=\"fas fa-plus\" aria-hidden=\"true\"></span></button> <div id=\"help-<%= data.modelAttribute %>\" class=\"editor-help\"> <%= data.helpText %> </div> </div> <div class=\"col-sm-9\"> <div class=\"existing container-fluid\"></div> </div> </div>";
// Exports
/* harmony default export */ const Parent = (Parent_code);
;// CONCATENATED MODULE: ./editor/src/collections/Positionable.js

/* harmony default export */ const Positionable = (backbone_default().Collection.extend({
  /*
    Moves an existing element in the the collection from position index
    to newPosition. Any "position" listeners of this instance will be
    notified with the arguments:
      model - the model which moved
      collection - this Layers instance
      newPosition - the new position of the model
      oldPosition - the position the model was in
    */
  position: function position(index, newPosition) {
    var toMove = this.models.splice(index, 1)[0];
    this.models.splice(newPosition, 0, toMove);
    return this.trigger('position', toMove, this, newPosition, index);
  }
}));
;// CONCATENATED MODULE: ./editor/src/collections/index.js

;// CONCATENATED MODULE: ./editor/src/views/ParentView.js






/* harmony default export */ const ParentView = (SingleView/* default.extend */.Z.extend({
  events: {
    'click button.add': 'add'
  },
  initialize: function initialize(options) {
    var _this = this;

    if (typeof this.template === 'undefined') {
      this.template = index_all/* default.template */.ZP.template(Parent);
    }

    SingleView/* default.prototype.initialize.call */.Z.prototype.initialize.call(this, options);
    this.collection = new Positionable([], {
      model: this.data.ModelType
    });
    this.listenTo(this.collection, 'add', this.addOne);
    this.listenTo(this.collection, 'reset', this.addAll);
    this.listenTo(this.collection, 'add remove change position', this.updateModel);
    this.listenTo(this.model, 'sync', this.updateCollection);
    this.render();
    this.collection.reset(this.getModelData());

    if (this.data.multiline) {
      this.$el.addClass('multiline');
    }

    if (!(this.data.disabled === 'disabled')) {
      this.$('.existing').sortable({
        start: function start(event, ui) {
          _this._oldPosition = ui.item.index();
        },
        update: function update(event, ui) {
          _this.collection.position(_this._oldPosition, ui.item.index());
        }
      });
    }
  },
  render: function render() {
    this.$el.html(this.template({
      data: this.data
    }));
    return this;
  },
  addOne: function addOne(model) {
    var view = new ChildView(index_all/* default.extend */.ZP.extend({}, this.data, {
      model: model
    }));
    var that = this;
    jquery_default()(document).ready(function () {
      that.$('.existing').append(view.el);
    });
  },
  addAll: function addAll() {
    this.$('.existing').html('');
    this.collection.each(this.addOne, this);
  },
  add: function add() {
    this.collection.add(new this.data.ModelType());
  },
  getModelData: function getModelData() {
    var model = this.model.attributes;
    var path = this.data.modelAttribute.split('.');

    while (path.length >= 2) {
      model = model[path.shift()] || {};
    }

    return model[path[0]] || [];
  },
  updateModel: function updateModel() {
    var path = this.data.modelAttribute.split('.');
    var data = this.collection.toJSON();

    while (path.length > 0) {
      var oldData = data;
      data = {};
      data[path.pop()] = oldData;
    }

    this.model.set(data);
  },
  updateCollection: function updateCollection(model) {
    var _this2 = this;

    if (model.hasChanged(this.data.modelAttribute)) {
      var updated = model.get(this.data.modelAttribute);
      var collectionLength = this.collection.length; // Update existing models

      index_all/* default.chain */.ZP.chain(updated).first(collectionLength).each(function (update, index) {
        _this2.collection.at(index).set(update);
      }); // Add new models


      index_all/* default.chain */.ZP.chain(updated).rest(collectionLength).each(function (update) {
        _this2.collection.add(update);
      }); // Remove models not in updated


      this.collection.remove(this.collection.rest(updated.length));
    }
  },
  show: function show() {
    SingleView/* default.prototype.show.apply */.Z.prototype.show.apply(this);
    this.collection.trigger('visible');
  },
  hide: function hide() {
    SingleView/* default.prototype.hide.apply */.Z.prototype.hide.apply(this);
    this.collection.trigger('hidden');
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/MultiString.tpl
// Module
var MultiString_code = "<div class=\"row\" id=\"input<%= data.modelAttribute %><%= data.index %>\"> <div class=\"col-sm-11 dataentry\"> <input data-index=\"<%= data.index %>\" class=\"editor-input\" value=\"<%= data.value %>\" placeholder=\"<%= data.placeholderAttribute %>\" <%= data.disabled%> > </div> <div class=\"col-sm-1\"> <button data-index=\"<%= data.index %>\" class=\"editor-button-xs remove\" <%= data.disabled%>><i class=\"fas fa-times\"></i></button> </div> </div>";
// Exports
/* harmony default export */ const MultiString = (MultiString_code);
;// CONCATENATED MODULE: ./editor/src/templates/ChildLarge.tpl
// Module
var ChildLarge_code = "<div class=\"col-sm-11 dataentry child child-large\"></div> <div class=\"col-sm-1\"> <button class=\"editor-button-xs remove\" title=\"remove\" <%= data.disabled%> ><span class=\"fas fa-times\" aria-hidden=\"true\"></span></button> <button class=\"editor-button-xs showhide\" title=\"show/hide details\"><span class=\"fas fa-chevron-down\" aria-hidden=\"true\"></span></button> </div>";
// Exports
/* harmony default export */ const ChildLarge = (ChildLarge_code);
// EXTERNAL MODULE: ./node_modules/jquery-ui/ui/widgets/sortable.js
var sortable = __webpack_require__(2526);
;// CONCATENATED MODULE: ./editor/src/views/ParentStringView.js







/* harmony default export */ const ParentStringView = (SingleView/* default.extend */.Z.extend({
  events: {
    change: 'modify',
    'click .remove': 'removeChild',
    'click .add': 'addChild'
  },
  initialize: function initialize(options) {
    var _this = this;

    if (typeof this.template === 'undefined') {
      this.template = index_all/* default.template */.ZP.template(ChildLarge);
    }

    if (typeof this.childTemplate === 'undefined') {
      this.childTemplate = index_all/* default.template */.ZP.template(MultiString);
    }

    if (typeof this.parentTemplate === 'undefined') {
      this.parentTemplate = index_all/* default.template */.ZP.template(Parent);
    }

    SingleView/* default.prototype.initialize.call */.Z.prototype.initialize.call(this, options);
    this.array = this.model.has(this.data.modelAttribute) ? index_all/* default.clone */.ZP.clone(this.model.get(this.data.modelAttribute)) : [];
    this.render();
    this.$('.existing').sortable({
      start: function start(event, ui) {
        _this._oldPosition = ui.item.index();
      },
      update: function update(event, ui) {
        var toMove = _this.array.splice(_this._oldPosition, 1)[0];

        _this.array.splice(ui.item.index(), 0, toMove);

        _this.updateModel();
      }
    });
  },
  renderParent: function renderParent() {
    return this.$el.html(this.parentTemplate({
      data: this.data
    }));
  },
  render: function render() {
    var _this2 = this;

    this.renderParent();

    index_all/* default.each */.ZP.each(this.array, function (string, index) {
      return _this2.$('.existing').append(_this2.childTemplate({
        data: index_all/* default.extend */.ZP.extend({}, _this2.data, {
          index: index,
          value: string
        })
      }));
    });

    return this;
  },
  modify: function modify(event) {
    var $target = jquery_default()(event.target);
    var index = $target.data('index');
    var value = $target.val();
    this.array.splice(index, 1, value);
    this.updateModel();
  },
  removeChild: function removeChild(event) {
    event.preventDefault();
    var $target = jquery_default()(event.currentTarget);
    var index = $target.data('index');
    this.array.splice(index, 1);
    this.$("#input".concat(this.data.modelAttribute).concat(index)).remove();
    this.updateModel();
  },
  addChild: function addChild(event) {
    event.preventDefault();
    this.array.push('');
    var index = this.array.length - 1;
    this.$('.existing').append(this.childTemplate({
      data: index_all/* default.extend */.ZP.extend({}, this.data, {
        index: index
      })
    }));
    this.$("#input".concat(this.data.modelAttribute).concat(index, " input")).focus();
    this.updateModel();
  },
  updateModel: function updateModel() {
    this.model.set(this.data.modelAttribute, index_all/* default.clone */.ZP.clone(this.array));
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/Keyword.tpl
// Module
var Keyword_code = "<div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\" for=\"descriptiveKeyword<%= data.index %>Keyword<%= data.keywordIndex %>Value\">Value</label> </div> <div class=\"col-sm-11 col-lg-5\"> <input data-name=\"value\" class=\"editor-input\" id=\"descriptiveKeyword<%= data.index %>Keyword<%= data.keywordIndex %>Value\" value=\"<%= data.value %>\"> </div> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\" for=\"descriptiveKeyword<%= data.index %>Keyword<%= data.keywordIndex %>Uri\">Uri</label> </div> <div class=\"col-sm-11 col-lg-5\"> <input data-name=\"uri\" class=\"editor-input\" id=\"descriptiveKeyword<%= data.index %>Keyword<%= data.keywordIndex %>Uri\" value=\"<%= data.uri %>\"> </div> </div>";
// Exports
/* harmony default export */ const Keyword = (Keyword_code);
;// CONCATENATED MODULE: ./editor/src/views/KeywordView.js



/* harmony default export */ const KeywordView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(Keyword)
}));
;// CONCATENATED MODULE: ./editor/src/templates/Reference.tpl
// Module
var Reference_code = "<div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"reference<%= data.index %>Citation\">Citation</label> </div> <div class=\"col-sm-10\"> <textarea data-name=\"citation\" rows=\"5\" id=\"reference<%= data.index %>Citation\" class=\"editor-textarea\"><%= data.citation %></textarea> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"reference<%= data.index %>Doi\">DOI</label> </div> <div class=\"col-sm-10 col-md-4\"> <input data-name=\"doi\" id=\"reference<%= data.index %>Doi\" class=\"editor-input\" value=\"<%= data.doi %>\" placeholder=\"e.g. https://doi.org/10.5072/unique-id-1234\"> </div> <div class=\"col-sm-2\"> <label for=\"reference<%= data.index %>Nora\">NORA url</label> </div> <div class=\"col-sm-10 col-md-4\"> <input data-name=\"nora\" id=\"reference<%= data.index %>Nora\" class=\"editor-input\" value=\"<%= data.nora %>\" placeholder=\"e.g. http://nora.nerc.ac.uk/d/eprint/xxxxxx\"> </div> </div>";
// Exports
/* harmony default export */ const Reference = (Reference_code);
;// CONCATENATED MODULE: ./editor/src/views/ReferenceView.js



/* harmony default export */ const ReferenceView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(Reference)
}));
;// CONCATENATED MODULE: ./editor/src/views/SingleObjectView.js



/* harmony default export */ const SingleObjectView = (SingleView/* default.extend */.Z.extend({
  initialize: function initialize(options) {
    SingleView/* default.prototype.initialize.call */.Z.prototype.initialize.call(this, options);
    this.render();
    var inputModel = new this.data.ModelType(this.model.get(this.data.modelAttribute));
    this.listenTo(inputModel, 'change', this.updateMetadataModel);
    this.listenTo(this.model, 'sync', function (model) {
      inputModel.set(model.get(this.data.modelAttribute));
    });
    var that = this;
    jquery_default()(document).ready(function () {
      var view = new that.data.ObjectInputView(index_all/* default.extend */.ZP.extend({}, that.data, {
        el: that.$('.dataentry'),
        model: inputModel
      }));
    });
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/DataInfo.tpl
// Module
var DataInfo_code = "<div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"dataInfo<%= data.index %>VariableName\">Variable name</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"variableName\" id=\"dataInfo<%= data.index %>VariableName\" class=\"editor-input\" value=\"<%= data.variableName %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"dataInfo<%= data.index %>Units\">Units</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"units\" id=\"dataInfo<%= data.index %>Units\" class=\"editor-input\" value=\"<%= data.units %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"dataInfo<%= data.index %>FileFormat\">File format</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"fileFormat\" id=\"dataInfo<%= data.index %>FileFormat\" class=\"editor-input\" value=\"<%= data.fileFormat %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"dataInfo<%= data.index %>Url\">Url</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"url\" id=\"dataInfo<%= data.index %>Url\" class=\"editor-input\" value=\"<%= data.url %>\"> </div> </div>";
// Exports
/* harmony default export */ const DataInfo = (DataInfo_code);
;// CONCATENATED MODULE: ./editor/src/views/DataInfoView.js



/* harmony default export */ const DataInfoView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(DataInfo)
}));
;// CONCATENATED MODULE: ./editor/src/templates/ModelInfo.tpl
// Module
var ModelInfo_code = "<div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>Name\">Name</label> </div> <div class=\"col-sm-9 col-lg-9\"> <div class=\"input-group\"> <span class=\"input-group-addon\" id=\"sizing-addon2\">Search</span> <input data-name=\"name\" id=\"modelInfo<%= data.index %>Name\" class=\"form-control autocomplete\" value=\"<%= data.name %>\"> </div> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>Id\">Id</label> </div> <div class=\"col-sm-9 col-lg-9\"> <input data-name=\"id\" id=\"modelInfo<%= data.index %>Id\" class=\"editor-input identifier\" value=\"<%= data.id %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>Version\">Version</label> </div> <div class=\"col-sm-9 col-lg-9\"> <input data-name=\"version\" id=\"modelInfo<%= data.index %>Version\" class=\"editor-input\" value=\"<%= data.version %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>Rationale\">Rationale</label> </div> <div class=\"col-sm-9 col-lg-9\"> <textarea data-name=\"rationale\" rows=\"7\" id=\"modelInfo<%= data.index %>Rationale\" class=\"editor-textarea\"><%= data.rationale %></textarea> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>spatialExtentOfApplication\">Spatial extent of application</label> </div> <div class=\"col-sm-9 col-lg-9\"> <input list=\"modelInfo<%= data.index %>SpatialExtentOfApplicationList\" data-name=\"spatialExtentOfApplication\" id=\"modelInfo<%= data.index %>SpatialExtentOfApplication\" class=\"editor-input\"/> <datalist id=\"modelInfo<%= data.index %>SpatialExtentOfApplicationList\"> <option value=\"Plot\"/> <option value=\"Field\"/> <option value=\"Farm\"/> <option value=\"River reach\"/> <option value=\"Catchment\"/> <option value=\"Landscape\"/> <option value=\"Regional\"/> <option value=\"National\"/> <option value=\"Global\"/> </datalist> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>AvailableSpatialData\">Available spatial data</label> </div> <div class=\"col-sm-9 col-lg-9\"> <select data-name=\"availableSpatialData\" id=\"modelInfo<%= data.index %>AvailableSpatialData\" class=\"editor-input spatial-data\"> <option value=\"unknown\">Unknown</option> <option value=\"shapefile\">Shapefile</option> <option value=\"bounding box\">Bounding box</option> </select> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>SpatialResolutionOfApplication\">Spatial resolution of application</label> </div> <div class=\"col-sm-9 col-lg-9\"> <input data-name=\"spatialResolutionOfApplication\" id=\"modelInfo<%= data.index %>SpatialResolutionOfApplication\" class=\"editor-input\" value=\"<%= data.spatialResolutionOfApplication %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>TemporalExtentOfApplicationStartDate\">Temporal extent of application (start date)</label> </div> <div class=\"col-sm-9 col-lg-9\"> <input data-name=\"temporalExtentOfApplicationStartDate\" id=\"modelInfo<%= data.index %>TemporalExtentOfApplicationStartDate\" class=\"editor-input\" value=\"<%= data.temporalExtentOfApplicationStartDate %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>TemporalExtentOfApplicationEndDate\">Temporal extent of application (end date)</label> </div> <div class=\"col-sm-9 col-lg-9\"> <input data-name=\"temporalExtentOfApplicationEndDate\" id=\"modelInfo<%= data.index %>TemporalExtentOfApplicationEndDate\" class=\"editor-input\" value=\"<%= data.temporalExtentOfApplicationEndDate %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>TemporalResolutionOfApplication\">Temporal resolution of application</label> </div> <div class=\"col-sm-9 col-lg-9\"> <input data-name=\"temporalResolutionOfApplication\" id=\"modelInfo<%= data.index %>TemporalResolutionOfApplication\" class=\"editor-input\" value=\"<%= data.temporalResolutionOfApplication %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>CalibrationConditions\">Calibration conditions</label> </div> <div class=\"col-sm-9 col-lg-9\"> <textarea data-name=\"calibrationConditions\" rows=\"3\" id=\"modelInfo<%= data.index %>CalibrationConditions\" class=\"editor-textarea\"><%= data.calibrationConditions %></textarea> </div> </div>";
// Exports
/* harmony default export */ const ModelInfo = (ModelInfo_code);
;// CONCATENATED MODULE: ./editor/src/views/ModelInfoView.js




/* harmony default export */ const ModelInfoView = (ObjectInputView.extend({
  initialize: function initialize() {
    var _this = this;

    this.template = index_all/* default.template */.ZP.template(ModelInfo);
    ObjectInputView.prototype.initialize.apply(this);
    var catalogue = jquery_default()('html').data('catalogue');
    this.$('.autocomplete').autocomplete({
      minLength: 0,
      source: function source(request, response) {
        var query;
        var term = request.term.trim();

        if (index_all/* default.isEmpty */.ZP.isEmpty(term)) {
          query = "/".concat(catalogue, "/documents?term=documentType:CEH_MODEL");
        } else {
          query = "/".concat(catalogue, "/documents?term=documentType:CEH_MODEL AND ").concat(request.term);
        }

        return jquery_default().getJSON(query, function (data) {
          return response(index_all/* default.map */.ZP.map(data.results, function (d) {
            return {
              value: d.title,
              label: d.title,
              identifier: d.identifier
            };
          }));
        });
      }
    });
    return this.$('.autocomplete').on('autocompleteselect', function (event, ui) {
      _this.model.set('id', ui.item.identifier);

      return _this.$('.identifier').val(ui.item.identifier);
    });
  },
  render: function render() {
    ObjectInputView.prototype.render.apply(this);
    this.$('select.spatial-data').val(this.model.get('availableSpatialData'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/PredefinedParent.tpl
// Module
var PredefinedParent_code = "<div class=\"dropdown\"> <button class=\"editor-button\" data-bs-toggle=\"dropdown\" type=\"button\" id=\"dropdown<%= data.modelAttribute %>Menu\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"true\"> Add <span class=\"caret\"></span> </button> <ul class=\"dropdown-menu\" aria-labelledby=\"dropdown<%= data.modelAttribute %>Menu\"> <li><a href=\"#\">Custom</a></li> </ul> </div> ";
// Exports
/* harmony default export */ const PredefinedParent = (PredefinedParent_code);
;// CONCATENATED MODULE: ./editor/src/templates/PredefinedParentDropdown.tpl
// Module
var PredefinedParentDropdown_code = "<li><a href=\"#\"><%= predefined %></a></li> ";
// Exports
/* harmony default export */ const PredefinedParentDropdown = (PredefinedParentDropdown_code);
;// CONCATENATED MODULE: ./editor/src/views/PredefinedParentView.js





/* harmony default export */ const PredefinedParentView = (ParentView.extend({
  events: {
    'click .dropdown-menu': 'setPredefined'
  },
  render: function render() {
    var _this = this;

    this.predefinedTemplate = index_all/* default.template */.ZP.template(PredefinedParent);
    this.dropdownTemplate = index_all/* default.template */.ZP.template(PredefinedParentDropdown);
    ParentView.prototype.render.apply(this);
    this.$('button.add').replaceWith(this.predefinedTemplate({
      data: this.data
    }));
    this.$('button').prop(this.data.disabled, this.data.disabled);

    index_all/* default.chain */.ZP.chain(this.data.predefined).keys().each(function (item) {
      return _this.$('ul.dropdown-menu').append(_this.dropdownTemplate({
        predefined: item
      }));
    });

    return this;
  },
  setPredefined: function setPredefined(event) {
    event.preventDefault();
    var value = jquery_default()(event.target).text();
    var selected = {};

    if (value !== 'Custom') {
      selected = this.data.predefined[value];
    }

    this.collection.add(selected);
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/Contact.tpl
// Module
var Contact_code = "<div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Role\">Role</label> </div> <div class=\"col-sm-10\"> <select data-name=\"role\" class=\"editor-input role\" id=\"contacts0Role\"> <option value=\"\" selected=\"\">- Select Role -</option> <optgroup label=\"Frequently used\"> <option value=\"author\">Author</option> <option value=\"custodian\">Custodian</option> <option value=\"depositor\">Depositor</option> <option value=\"distributor\">Distributor</option> <option value=\"pointOfContact\">Point of contact</option> <option value=\"publisher\">Publisher</option> <option value=\"resourceProvider\">Resource provider</option> <option value=\"rightsHolder\">Rights holder</option> <option value=\"owner\">Senior Responsible Officer [SRO]</option> </optgroup> <optgroup label=\"Other\"> <option value=\"coAuthor\">Co-author</option> <option value=\"collaborator\">Collaborator</option> <option value=\"contributor\">Contributor</option> <option value=\"editor\">Editor</option> <option value=\"funder\">Funder</option> <option value=\"mediator\">Mediator</option> <option value=\"originator\">Originator</option> <option value=\"principalInvestigator\">Principal investigator</option> <option value=\"processor\">Processor</option> <option value=\"rightsHolder\">Rights holder</option> <option value=\"sponsor\">Sponsor</option> <option value=\"stakeholder\">Stakeholder</option> <option value=\"user\">User</option> </optgroup> </select> </div> </div> <div class=\"col-sm-10 col-sm-offset-2 hidden-xs\"><hr></div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Person\">Person</label> </div> <div class=\"col-sm-4\"> <input data-name=\"individualName\" class=\"editor-input\" id=\"contacts<%= data.index %>Person\" value=\"<%= data.individualName %>\"> </div> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"contacts<%= data.index %>nameIdentifier\">ORCID</label> </div> <div class=\"col-sm-5\"> <input data-name=\"nameIdentifier\" placeholder=\"https://orcid.org/0000-...\" class=\"editor-input\" id=\"contacts<%= data.index %>nameIdentifier\" value=\"<%= data.nameIdentifier %>\"> </div> </div> <div class=\"col-sm-10 col-sm-offset-2 hidden-xs\"><hr></div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Email\">Email</label> </div> <div class=\"col-sm-10\"> <input data-name=\"email\" class=\"editor-input\" id=\"contacts<%= data.index %>Email\" value=\"<%= data.email %>\"> </div> </div> <div class=\"col-sm-10 col-sm-offset-2 hidden-xs\"><hr></div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Organisation\">Organisation</label> </div> <div class=\"col-sm-10\"> <input data-name=\"organisationName\" class=\"editor-input\" id=\"contacts<%= data.index %>Organisation\" value=\"<%= data.organisationName %>\"> </div> </div> <div class=\"postalAddress\"> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Address\">Address</label> </div> <div class=\"col-sm-10\"> <input data-name=\"deliveryPoint\" class=\"editor-input\" id=\"contacts<%= data.index %>Address\" value=\"<%= data.address.deliveryPoint %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>City\">City/Town</label> </div> <div class=\"col-sm-5\"> <input data-name=\"city\" class=\"editor-input\" id=\"contacts<%= data.index %>City\" value=\"<%= data.address.city %>\"> </div> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"contacts<%= data.index %>County\">County</label> </div> <div class=\"col-sm-4\"> <input data-name=\"administrativeArea\" class=\"editor-input\" id=\"contacts<%= data.index %>County\" value=\"<%= data.address.administrativeArea %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Postcode\">Postcode</label> </div> <div class=\"col-sm-5\"> <input data-name=\"postalCode\" class=\"editor-input\" id=\"contacts<%= data.index %>Postcode\" value=\"<%= data.address.postalCode %>\"> </div> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Country\">Country</label> </div> <div class=\"col-sm-4\"> <input data-name=\"country\" class=\"editor-input\" id=\"contacts<%= data.index %>Country\" value=\"<%= data.address.country %>\"> </div> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>organisationIdentifier\">Organisation's RoR</label> </div> <div class=\"col-sm-10\"> <input data-name=\"organisationIdentifier\" class=\"editor-input\" id=\"contacts<%= data.index %>organisationIdentifier\" value=\"<%= data.organisationIdentifier %>\"> </div> </div> ";
// Exports
/* harmony default export */ const Contact = (Contact_code);
;// CONCATENATED MODULE: ./editor/src/views/ContactView.js




/* harmony default export */ const ContactView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(Contact);
    ObjectInputView.prototype.render.apply(this);
    this.$('select.role').val(this.model.get('role'));
    return this;
  },
  modify: function modify(event) {
    var $target = jquery_default()(event.target);
    var name = $target.data('name');
    var value = $target.val();

    if (index_all/* default.contains */.ZP.contains(['deliveryPoint', 'city', 'administrativeArea', 'country', 'postalCode'], name)) {
      var address = index_all/* default.clone */.ZP.clone(this.model.get('address'));

      if (value) {
        address[name] = value;
        return this.model.set('address', address);
      } else {
        address = index_all/* default.omit */.ZP.omit(address, name);
        return this.model.set('address', address);
      }
    } else {
      if (value) {
        return this.model.set(name, value);
      } else {
        return this.model.unset(name);
      }
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/Qa.tpl
// Module
var Qa_code = "<div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"qa<%= data.index %>Done\">Done</label> </div> <div class=\"col-sm-5\"> <select data-name=\"done\" id=\"qa<%= data.index %>Done\" class=\"editor-input\" value=\"<%= data.done %>\"> <option value=\"yes\">Yes</option> <option value=\"no\">No</option> <option value=\"unknown\">Unknown</option> </select> </div> <div class=\"col-sm-1\"> <label for=\"qa<%= data.index %>ModelVersion\">Model version</label> </div> <div class=\"col-sm-5\"> <input data-name=\"modelVersion\" id=\"qa<%= data.index %>ModelVersion\" class=\"editor-input\" value=\"<%= data.modelVersion %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"qa<%= data.index %>Owner\">Owner</label> </div> <div class=\"col-sm-5\"> <input data-name=\"owner\" id=\"qa<%= data.index %>Owner\" class=\"editor-input\" value=\"<%= data.owner %>\"> </div> <div class=\"col-sm-1\"> <label for=\"qa<%= data.index %>Date\">Date</label> </div> <div class=\"col-sm-5\"> <input type=\"date\" data-name=\"date\" id=\"qa<%= data.index %>Date\" class=\"editor-input\" value=\"<%= data.date %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"qa<%= data.index %>Note\">Note</label> </div> <div class=\"col-sm-11\"> <textarea data-name=\"note\" rows=\"3\" id=\"qa<%= data.index %>Note\" class=\"editor-textarea\"><%= data.note %></textarea> </div> </div>";
// Exports
/* harmony default export */ const Qa = (Qa_code);
;// CONCATENATED MODULE: ./editor/src/views/QaView.js



/* harmony default export */ const QaView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(Qa);
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('done'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/VersionHistory.tpl
// Module
var VersionHistory_code = "<div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"versionHistory<%= data.index %>Version\">Version</label> </div> <div class=\"col-sm-4 col-lg-4\"> <input data-name=\"version\" id=\"versionHistory<%= data.index %>Version\" class=\"editor-input\" value=\"<%= data.version %>\"> </div> <div class=\"col-sm-2 col-lg-2\"> <label for=\"versionHistory<%= data.index %>Date\">Date</label> </div> <div class=\"col-sm-4 col-lg-4\"> <input data-name=\"date\" type=\"date\" id=\"versionHistory<%= data.index %>Date\" class=\"editor-input\" value=\"<%= data.date %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"versionHistory<%= data.index %>Note\">Note</label> </div> <div class=\"col-sm-10 col-lg-10\"> <textarea rows=\"5\" data-name=\"note\" id=\"versionHistory<%= data.index %>Note\" class=\"editor-textarea\"><%= data.note %></textarea> </div> </div>";
// Exports
/* harmony default export */ const VersionHistory = (VersionHistory_code);
;// CONCATENATED MODULE: ./editor/src/views/VersionHistoryView.js



/* harmony default export */ const VersionHistoryView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(VersionHistory)
}));
;// CONCATENATED MODULE: ./editor/src/templates/ProjectUsage.tpl
// Module
var ProjectUsage_code = "<div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label for=\"projectUsage<%= data.index %>Project\">Project</label> </div> <div class=\"col-sm-5 col-lg-5\"> <input data-name=\"project\" id=\"projectUsage<%= data.index %>Project\" class=\"editor-input\" value=\"<%= data.project %>\"> </div> <div class=\"col-sm-1 col-lg-1\"> <label for=\"projectUsage<%= data.index %>Version\">Version</label> </div> <div class=\"col-sm-2 col-lg-2\"> <input data-name=\"version\" id=\"projectUsage<%= data.index %>Version\" class=\"editor-input\" value=\"<%= data.version %>\"> </div> <div class=\"col-sm-1 col-lg-1\"> <label for=\"projectUsage<%= data.index %>Date\">Date</label> </div> <div class=\"col-sm-2 col-lg-2\"> <input data-name=\"date\" type=\"date\" id=\"projectUsage<%= data.index %>Date\" class=\"editor-input\" value=\"<%= data.date %>\"> </div> </div>";
// Exports
/* harmony default export */ const ProjectUsage = (ProjectUsage_code);
;// CONCATENATED MODULE: ./editor/src/views/ProjectUsageView.js



/* harmony default export */ const ProjectUsageView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(ProjectUsage)
}));
;// CONCATENATED MODULE: ./editor/src/templates/ObservationCapability.tpl
// Module
var ObservationCapability_code = "<div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"observedCapabilities<%= data.index %>ObservingTime\">Observing Time</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"observingTime\" id=\"observedCapabilities<%= data.index %>ObservingTime\" class=\"editor-input\" value=\"<%= data.observingTime %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-12 col-lg-12\"> <label>Observed Property</label> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"observedCapabilities<%= data.index %>NameTitle\">Name: title</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"observedPropertyName.title\" id=\"observedCapabilities<%= data.index %>NameTitle\" class=\"editor-input\" value=\"<%= data.observedPropertyName.title %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"observedCapabilities<%= data.index %>NameHref\">Name: url</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"observedPropertyName.href\" id=\"observedCapabilities<%= data.index %>NameHref\" class=\"editor-input\" value=\"<%= data.observedPropertyName.href %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"observedCapabilities<%= data.index %>UnitOfMeasureTitle\">Unit of Measure: title</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"observedPropertyUnitOfMeasure.title\" id=\"observedCapabilities<%= data.index %>UnitOfMeasureTitle\" class=\"editor-input\" value=\"<%= data.observedPropertyUnitOfMeasure.title %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"observedCapabilities<%= data.index %>UnitOfMeasureHref\">Unit of Measure: url</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"observedPropertyUnitOfMeasure.href\" id=\"observedCapabilities<%= data.index %>UnitOfMeasureHref\" class=\"editor-input\" value=\"<%= data.observedPropertyUnitOfMeasure.href %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-12 col-lg-12\"> <label>Procedure</label> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"observedCapabilities<%= data.index %>ProcedureNameTitle\">Name: title</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"procedureName.title\" id=\"observedCapabilities<%= data.index %>ProcedureNameTitle\" class=\"editor-input\" value=\"<%= data.procedureName.title %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"observedCapabilities<%= data.index %>ProcedureNameHref\">Name: url</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"procedureName.href\" id=\"observedCapabilities<%= data.index %>ProcedureNameHref\" class=\"editor-input\" value=\"<%= data.procedureName.href %>\"> </div> </div>";
// Exports
/* harmony default export */ const ObservationCapability = (ObservationCapability_code);
;// CONCATENATED MODULE: ./editor/src/views/OnlineLinkView.js



/* harmony default export */ const OnlineLinkView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(ObservationCapability);
    return ObjectInputView.prototype.render.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/DataTypeSchemaSimple.tpl
// Module
var DataTypeSchemaSimple_code = "<div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"schema-name<%= data.index %>\">Field</label> </div> <div class=\"col-sm-5\"> <input data-name=\"name\" id=\"schema-name<%= data.index %>\" class=\"editor-input\" value=\"<%= data.name %>\" placeholder=\"name of field/column\"/> </div> <div class=\"col-sm-1\"> <label for=\"schema-type<%= data.index %>\">Type</label> </div> <div class=\"col-sm-4\"> <input list=\"typeList\" data-name=\"type\" id=\"schema-type<%= data.index %>\" class=\"editor-input\" value=\"<%= data.type %>\" placeholder=\"\"/> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"schema-title<%= data.index %>\">Title</label> </div> <div class=\"col-sm-10\"> <input data-name=\"title\" id=\"schema-title<%= data.index %>\" class=\"editor-input\" value=\"<%= data.title %>\" placeholder=\"A nicer human readable label for the field (optional)\"/> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"schema-description<%= data.index %>\">Description</label> </div> <div class=\"col-sm-10\"> <textarea data-name=\"description\" id=\"schema-description<%= data.index %>\" class=\"editor-textarea\" rows=\"2\"><%= data.description %></textarea> </div> </div> <datalist id=\"typeList\"> <option value=\"boolean\">True or false</option> <option value=\"date\">Date (without time)</option> <option value=\"datetime\">Date AND time</option> <option value=\"number\">Decimal number </option> <option value=\"email\">Email address</option> <option value=\"geopoint\">Geographic point (e.g. lon, lat)</option> <option value=\"integer\">Integer</option> <option value=\"string\">Text string</option> <option value=\"time\">Time</option> <option value=\"uri\">URI such as a web address or urn</option> <option value=\"uuid\">UUID/GUID</option> <option value=\"year\">Four digit year</option> <option value=\"yearmonth\">Year and month (e.g. 2015-07)</option> </datalist> ";
// Exports
/* harmony default export */ const DataTypeSchemaSimple = (DataTypeSchemaSimple_code);
;// CONCATENATED MODULE: ./editor/src/views/DataTypeSchemaSimpleView.js




/* harmony default export */ const DataTypeSchemaSimpleView = (ObjectInputView.extend({
  initialize: function initialize() {
    this.template = index_all/* default.template */.ZP.template(DataTypeSchemaSimple);
  },
  modify: function modify(event) {
    var $target = jquery_default()(event.target);
    var name = $target.data('name'); // CHECK THIS

    var value = $target.val();

    if (index_all/* default.contains */.ZP.contains(['maximum', 'minimum', 'maxLength', 'minLength', 'unique'], name)) {
      var constraints = index_all/* default.clone */.ZP.clone(this.model.get('constraints'));

      if (value) {
        constraints[name] = value;
        return this.model.set('constraints', constraints);
      } else {
        constraints = index_all/* default.omit */.ZP.omit(constraints, name);
        return this.model.set('constraints', constraints);
      }
    } else {
      if (value) {
        return this.model.set(name, value);
      } else {
        return this.model.unset(name);
      }
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/Funding.tpl
// Module
var Funding_code = "<div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"funding<%= data.index %>FunderName\">Funding body</label> </div> <div class=\"col-sm-10\"> <input placeholder=\"(e.g. NERC)\" data-name=\"funderName\" class=\"editor-input\" id=\"funding<%= data.index %>FunderName\" value=\"<%= data.funderName %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"funding<%= data.index %>FunderIdentifier\">Funding body ID</label> </div> <div class=\"col-sm-10\"> <input placeholder=\"(e.g.Crossref Funder ID or GRID)\" data-name=\"funderIdentifier\" class=\"editor-input\" id=\"funding<%= data.index %>FunderIdentifier\" value=\"<%= data.funderIdentifier %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"funding<%= data.index %>AwardTitle\">Award name</label> </div> <div class=\"col-sm-10\"> <input placeholder=\"(e.g. DURESS)\" data-name=\"awardTitle\" class=\"editor-input\" id=\"funding<%= data.index %>AwardTitle\" value=\"<%= data.awardTitle %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"funding<%= data.index %>AwardTitle\">Award reference</label> </div> <div class=\"col-sm-10\"> <input placeholder=\"(e.g. NE/J015105/1)\" data-name=\"awardNumber\" class=\"editor-input\" id=\"funding<%= data.index %>AwardNumber\" value=\"<%= data.awardNumber %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"funding<%= data.index %>AwardTitle\">Award URL</label> </div> <div class=\"col-sm-10\"> <input placeholder=\"(e.g. https://gtr.ukri.org/projects?ref=NE/J015644/1)\" data-name=\"awardURI\" class=\"editor-input\" id=\"funding<%= data.index %>AwardURI\" value=\"<%= data.awardURI %>\"> </div> </div>";
// Exports
/* harmony default export */ const Funding = (Funding_code);
;// CONCATENATED MODULE: ./editor/src/views/FundingView.js



/* harmony default export */ const FundingView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(Funding)
}));
;// CONCATENATED MODULE: ./editor/src/views/SupplementalView.js


/* harmony default export */ const SupplementalView = (ObjectInputView.extend({
  /* render: ->
    this.template = _template(template);
    ObjectInputView.prototype.render.apply @
    @$('select.function').val @model.get 'function'
    @
  */
}));
;// CONCATENATED MODULE: ./editor/src/templates/ServiceType.tpl
// Module
var ServiceType_code = "<div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label for=\"serviceType\">Type</label> </div> <div class=\"col-sm-11 col-lg-11\"> <select data-name=\"type\" class=\"editor-input\" id=\"serviceType\" <%= data.disabled%>> <option value=\"\">- Select Service Type -</option> <optgroup label=\"INSPIRE\"> <option value=\"discovery\">Discovery Service</option> <option value=\"download\">Download Service</option> <option value=\"invoke\">Invoke Service</option> <option value=\"other\">Other Service</option> <option value=\"transformation\">Transformation Service</option> <option value=\"view\">View Service</option> </optgroup> <optgroup label=\"OGC\"> <option value=\"WFS-G\">Gazetteer Service</option> <option value=\"SOS\">Sensor Observation Service</option> <option value=\"SPS\">Sensor Processing Service</option> <option value=\"CSW\">Web Catalog Service</option> <option value=\"WCS\">Web Coverage Service</option> <option value=\"WFS\">Web Feature Service</option> <option value=\"WFS-T\">Web Feature Service (transactional)</option> <option value=\"WMS\">Web Map Service</option> <option value=\"WMTS\">Web Map Tile Service</option> <option value=\"WNS\">Web Notification Service</option> <option value=\"WPOS\">Web Price &amp; Ordering Service</option> <option value=\"WPS\">Web Processing Service</option> <option value=\"WTS\">Web Terrain Service</option> </optgroup> </select> </div> </div>";
// Exports
/* harmony default export */ const ServiceType = (ServiceType_code);
;// CONCATENATED MODULE: ./editor/src/views/ServiceTypeView.js



/* harmony default export */ const ServiceTypeView = (ObjectInputView.extend({
  className: 'component component--servicetype visible',
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(ServiceType);
    ObjectInputView.prototype.render.apply(this);
    return this.$('select').val(this.model.get(this.data.modelAttribute));
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/AccessLimitation.js

/* harmony default export */ const AccessLimitation = (backbone_default().Model.extend({
  defaults: {
    value: null
  },
  uris: {
    'Registration is required to access this data': 'https://www.eidc.ac.uk/help/faq/registration',
    'no limitations to public access': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations',
    embargoed: 'https://www.eidc.ac.uk/help/faq/embargos',
    'public access limited according to Article 13(1)(a) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1a',
    'public access limited according to Article 13(1)(b) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1b',
    'public access limited according to Article 13(1)(c) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1c',
    'public access limited according to Article 13(1)(d) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1d',
    'public access limited according to Article 13(1)(e) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1e',
    'public access limited according to Article 13(1)(f) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1f',
    'public access limited according to Article 13(1)(g) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1g',
    'public access limited according to Article 13(1)(h) of the INSPIRE Directive': 'http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1h'
  },
  codes: {
    'Registration is required to access this data': 'Available',
    'no limitations to public access': 'Available',
    embargoed: 'Embargoed',
    'in-progress': 'In-progress',
    superseded: 'Superseded',
    withdrawn: 'Withdrawn',
    'To access this data, a licence needs to be negotiated with the provider and there may be a cost': 'Controlled',
    'public access limited according to Article 13(1)(a) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(b) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(c) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(d) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(e) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(f) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(g) of the INSPIRE Directive': 'Restricted',
    'public access limited according to Article 13(1)(h) of the INSPIRE Directive': 'Restricted'
  },
  descriptions: {
    'public access limited according to Article 13(1)(a) of the INSPIRE Directive': 'Access would adversely affect the confidentiality of the proceedings of public authorities, where such confidentiality is provided for by law.',
    'public access limited according to Article 13(1)(b) of the INSPIRE Directive': 'Access would adversely affect international relations, public security or national defence.',
    'public access limited according to Article 13(1)(c) of the INSPIRE Directive': 'Access would adversely affect the course of justice, the ability of any person to receive a fair trial or the ability of a public authority to conduct an enquiry of a criminal or disciplinary nature.',
    'public access limited according to Article 13(1)(d) of the INSPIRE Directive': 'Access would adversely affect the confidentiality of commercial or industrial information, where such confidentiality is provided for by national or Community law to protect a legitimate economic interest, including the public interest in maintaining statistical confidentiality and tax secrecy.',
    'public access limited according to Article 13(1)(e) of the INSPIRE Directive': 'Access would adversely affect intellectual property rights.',
    'public access limited according to Article 13(1)(f) of the INSPIRE Directive': 'Access would adversely affect the confidentiality of personal data and/or files relating to a natural person where that person has not consented to the disclosure of the information to the public, where such confidentiality is provided for by national or Community law.',
    'public access limited according to Article 13(1)(g) of the INSPIRE Directive': 'Accesss would adversely affect the interests or protection of any person who supplied the information requested on a voluntary basis without being under, or capable of being put under, a legal obligation to do so, unless that person has consented to the release of the information concerned.',
    'public access limited according to Article 13(1)(h) of the INSPIRE Directive': 'Access would adversely affect the protection of the environment to which such information relates.'
  },
  initialize: function initialize() {
    return this.on('change:value', this.updateLimitation);
  },
  updateLimitation: function updateLimitation(model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : null);
    this.set('code', this.codes[value] ? this.codes[value] : null);
    return this.set('description', this.descriptions[value] ? this.descriptions[value] : null);
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/Contact.js


/* harmony default export */ const models_Contact = (backbone_default().Model.extend({
  defaults: {
    address: {}
  },
  validate: function validate(attrs) {
    var emailRegEx = new RegExp("\n^[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$\n");
    var urlRegEx = new RegExp("\n^(?:(?:(?:https?|ftp):)?\\/\\/)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\xA1-\uFFFF0-9]-*)*[a-z\xA1-\uFFFF0-9]+)(?:\\.(?:[a-z\xA1-\uFFFF0-9]-*)*[a-z\xA1-\uFFFF0-9]+)*(?:\\.(?:[a-z\xA1-\uFFFF]{2,})).?)(?::\\d{2,5})?(?:[/?#]\\S*)?$\n");
    var orcidRegEx = new RegExp("\n^https?:\\/\\/orcid.org\\/(\\d{4}-){3}\\d{3}[\\dX]$\n");
    var rorRegEx = new RegExp("\n^https?:\\/\\/ror.org\\/\\w{8,10}$\n");
    var errors = [];
    var organisationName = attrs.organisationName;
    var organisationIdentifier = attrs.organisationIdentifier;
    var role = attrs.role;
    var email = attrs.email;
    var nameIdentifier = attrs.nameIdentifier;

    var isValidEmail = function isValidEmail(address) {
      return emailRegEx.test(address);
    };

    var isValidnameIdentifier = function isValidnameIdentifier(id) {
      return urlRegEx.test(id);
    };

    var isValidORCID = function isValidORCID(id) {
      return orcidRegEx.test(id);
    };

    var isValidROR = function isValidROR(id) {
      return rorRegEx.test(id);
    };

    if (email && !isValidEmail(email)) {
      errors.push({
        message: 'That email address is wrong'
      });
    }

    if (nameIdentifier && !isValidORCID(nameIdentifier)) {
      errors.push({
        message: "If that's supposed to be an ORCiD, it's not quite right!"
      });
    }

    if (nameIdentifier && !isValidnameIdentifier(nameIdentifier)) {
      errors.push({
        message: 'Are you using the <i>fully-qualified</i> name identifier. For example, ORCiDs should be entered as https://orcid.org/0000-1234-5678-999X <b>not</b> 0000-1234-5678-999X'
      });
    }

    if (organisationIdentifier && !isValidROR(organisationIdentifier)) {
      errors.push({
        message: "If that's supposed to be an ROR, it's not quite right!"
      });
    }

    if (!organisationName || !role) {
      errors.push({
        message: 'The organisation name and role are mandatory.'
      });
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
      return;
    } else {
      return errors;
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(warnings)) {
      return;
    } else {
      return warnings;
    }
  },
  toJSON: function toJSON() {
    if (index_all/* default.isEmpty */.ZP.isEmpty(this.get('address'))) {
      return index_all/* default.omit */.ZP.omit(this.attributes, 'address');
    } else {
      return index_all/* default.clone */.ZP.clone(this.attributes);
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/DataTypeSchema.js


/* harmony default export */ const DataTypeSchema = (backbone_default().Model.extend({
  defaults: {
    constraints: {}
  },
  toJSON: function toJSON() {
    if (index_all/* default.isEmpty */.ZP.isEmpty(this.get('constraints'))) {
      return index_all/* default.omit */.ZP.omit(this.attributes, 'constraints');
    } else {
      return index_all/* default.clone */.ZP.clone(this.attributes);
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/NestedModel.js


var NestedModel = backbone_default().Model.extend({
  /*
  Return a collection which is bound to an array attribute of the given model.
  The collection will by default create instances of `NestedModel` and be of
  type `Positionable`. Any changes to the returned collection will be reflected
  in the models specified attribute.
  */
  getRelatedCollection: function getRelatedCollection(attr, model, collection) {
    var _this = this;

    if (model == null) {
      model = NestedModel;
    }

    if (collection == null) {
      collection = Positionable;
    }

    collection = new (backbone_default()).Collection(this.get(attr), {
      model: model
    });
    this.listenTo(collection, 'add remove change position', function () {
      return _this.set(attr, collection.toJSON());
    });
    return collection;
  },

  /*
  Return a model representation for an attribute on this model. Any changes to
  the returned model will be automatically reflected on this models attribute.
  */
  getRelated: function getRelated(attr, model) {
    var _this2 = this;

    if (model == null) {
      model = NestedModel;
    }

    model = new (backbone_default()).Model(this.get(attr));
    this.listenTo(model, 'change', function () {
      return _this2.set(attr, model.toJSON());
    });
    return model;
  }
});
;// CONCATENATED MODULE: ./editor/src/models/DescriptiveKeyword.js

/* harmony default export */ const DescriptiveKeyword = (NestedModel.extend({
  defaults: {
    keywords: []
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/DistributionFormat.js


/* harmony default export */ const DistributionFormat = (backbone_default().Model.extend({
  validate: function validate(attrs) {
    var errors = [];
    var name = attrs.name;
    var type = attrs.type;
    var version = attrs.version;

    if (!version && (name || type)) {
      errors.push({
        message: "The version is mandatory - if it's not applicable, enter 'unknown'"
      });
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(errors)) {// return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
    } else {
      return errors;
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/Funding.js


/* harmony default export */ const models_Funding = (backbone_default().Model.extend({
  validate: function validate(attrs) {
    // from https://github.com/jzaefferer/jquery-validation/blob/043fb91da7dab0371fded5e2a9fa7ebe4c836210/src/core.js#L1180
    var urlRegEx = new RegExp("\n^(?:(?:(?:https?|ftp):)?\\/\\/)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\xA1-\uFFFF0-9]-*)*[a-z\xA1-\uFFFF0-9]+)(?:\\.(?:[a-z\xA1-\uFFFF0-9]-*)*[a-z\xA1-\uFFFF0-9]+)*(?:\\.(?:[a-z\xA1-\uFFFF]{2,})).?)(?::\\d{2,5})?(?:[/?#]\\S*)?$\n");

    var isValidUrl = function isValidUrl(url) {
      return urlRegEx.test(url);
    };

    var errors = [];

    if (!attrs.funderName) {
      errors.push({
        message: 'Funding body is mandatory'
      });
    }

    if (attrs.awardURI && !isValidUrl(attrs.awardURI)) {
      errors.push({
        message: "That URL doesn't look right!"
      });
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(errors)) {// return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
    } else {
      return errors;
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/InspireTheme.js

/* harmony default export */ const InspireTheme = (backbone_default().Model.extend({
  defaults: {
    theme: ''
  },
  uris: {
    Addresses: 'http://inspire.ec.europa.eu/theme/ad',
    'Administrative Units': 'http://inspire.ec.europa.eu/theme/au',
    'Agricultural and Aquaculture Facilities': 'http://inspire.ec.europa.eu/theme/af',
    'Area Management Restriction Regulation Zones and Reporting units': 'http://inspire.ec.europa.eu/theme/am',
    'Atmospheric Conditions': 'http://inspire.ec.europa.eu/theme/ac',
    'Bio-geographical Regions': 'http://inspire.ec.europa.eu/theme/br',
    Buildings: 'http://inspire.ec.europa.eu/theme/bu',
    'Cadastral Parcels': 'http://inspire.ec.europa.eu/theme/cp',
    'Coordinate reference systems': 'http://inspire.ec.europa.eu/theme/rs',
    Elevation: 'http://inspire.ec.europa.eu/theme/el',
    'Energy Resources': 'http://inspire.ec.europa.eu/theme/er',
    'Environmental Monitoring Facilities': 'http://inspire.ec.europa.eu/theme/ef',
    'Geographical grid systems': 'http://inspire.ec.europa.eu/theme/gg',
    'Geographical Names': 'http://inspire.ec.europa.eu/theme/gn',
    Geology: 'http://inspire.ec.europa.eu/theme/ge',
    'Habitats and Biotopes': 'http://inspire.ec.europa.eu/theme/hb',
    'Human Health and Safety': 'http://inspire.ec.europa.eu/theme/hh',
    Hydrography: 'http://inspire.ec.europa.eu/theme/hy',
    'Land Cover': 'http://inspire.ec.europa.eu/theme/lc',
    'Land Use': 'http://inspire.ec.europa.eu/theme/lu',
    'Meteorological geographical features': 'http://inspire.ec.europa.eu/theme/mf',
    'Mineral Resources': 'http://inspire.ec.europa.eu/theme/mr',
    'Natural Risk Zones': 'http://inspire.ec.europa.eu/theme/nz',
    'Oceanographic Geographical Features': 'http://inspire.ec.europa.eu/theme/of',
    Orthoimagery: 'http://inspire.ec.europa.eu/theme/oi',
    'Population Distribution - Demography': 'http://inspire.ec.europa.eu/theme/pd',
    'Production and Industrial Facilities': 'http://inspire.ec.europa.eu/theme/pf',
    'Protected Sites': 'http://inspire.ec.europa.eu/theme/ps',
    'Sea Regions': 'http://inspire.ec.europa.eu/theme/sr',
    Soil: 'http://inspire.ec.europa.eu/theme/so',
    'Species Distribution': 'http://inspire.ec.europa.eu/theme/sd',
    'Statistical Units': 'http://inspire.ec.europa.eu/theme/su',
    'Transport Networks': 'http://inspire.ec.europa.eu/theme/tn',
    'Utility and Governmental Services': 'http://inspire.ec.europa.eu/theme/us'
  },
  initialize: function initialize() {
    return this.on('change:theme', this.updateUri);
  },
  updateUri: function updateUri(model, theme) {
    return this.set('uri', this.uris[theme] ? this.uris[theme] : '');
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/MapAttribute.js

/* harmony default export */ const MapAttribute = (NestedModel.extend({
  idAttribute: 'name'
}));
;// CONCATENATED MODULE: ./editor/src/models/MapDataSource.js



/* harmony default export */ const MapDataSource = (NestedModel.extend({
  defaults: {
    type: 'POLYGON',
    epsgCode: 4326,
    bytetype: 'false',
    features: {
      style: {
        colour: '#000000'
      }
    }
  },
  initialize: function initialize() {
    NestedModel.prototype.initialize.apply(this, arguments); // Determine the current styling mode

    return this.stylingMode = index_all/* default.isEmpty */.ZP.isEmpty(this.attributes.attributes) ? 'features' : 'attributes';
  },

  /*
  Return a releated collection of the attributes element of this model
  */
  getAttributes: function getAttributes() {
    return this.getRelatedCollection('attributes', MapAttribute);
  },

  /*
  Update the styling mode and trigger a change event as either the features
  or attributes will now be hidden.
  */
  setStylingMode: function setStylingMode(mode) {
    this.stylingMode = mode;
    return this.trigger('change', this, {});
  },
  validate: function validate(attrs) {
    var errors = []; // Validate all of the min and max values of any defined buckets

    var numRegex = /^-?(?:\d+(?:\.\d+)?|\.\d+)$/;

    if (!index_all/* default.isEmpty */.ZP.isEmpty(attrs.attributes)) {
      if (index_all/* default.chain */.ZP.chain(attrs.attributes).pluck('buckets').flatten().select(function (n) {
        return n != null;
      }).map(function (b) {
        return [b.min, b.max];
      }).flatten().select(function (n) {
        return n != null;
      }).any(function (n) {
        return !numRegex.test(n);
      }).value()) {
        errors.push({
          message: 'Bucket values must be numbers'
        });
      }

      if (index_all/* default.chain */.ZP.chain(attrs.attributes).pluck('name').uniq().value().length !== attrs.attributes.length) {
        errors.push({
          message: 'Layer names must be unique'
        });
      }
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(errors)) {
      return undefined;
    } else {
      return errors;
    }
  },

  /*
  Depending on the stylingMode, either hide the `features` or `attributes`
  attributes.
  */
  toJSON: function toJSON() {
    var json = NestedModel.prototype.toJSON.apply(this, arguments);

    switch (this.stylingMode) {
      case 'features':
        return index_all/* default.omit */.ZP.omit(json, 'attributes');

      case 'attributes':
        return index_all/* default.omit */.ZP.omit(json, 'features');
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/MultipleDate.js


/* harmony default export */ const MultipleDate = (backbone_default().Model.extend({
  // N.B. Use in Views that only has date attributes
  validate: function validate(attrs) {
    var labels = {
      creationDate: 'Creation date',
      publicationDate: 'Publication date',
      revisionDate: 'Revision date',
      supersededDate: 'Date superseded',
      deprecatedDate: 'Date deprecated',
      releasedDate: 'Date released',
      begin: 'Begin',
      end: 'End'
    };
    var dateRegExp = new RegExp("\n^\n\\d{4}\n-\n\\d{2}\n-\n\\d{2}\n$\n");
    var errors = [];

    index_all/* default.chain */.ZP.chain(attrs).keys().each(function (key) {
      var dateString = attrs[key];

      if (!dateString.match(dateRegExp)) {
        errors.push({
          message: "".concat(labels[key], " is wrong. The date format is supposed to be yyyy-mm-dd")
        });
      }

      if (isNaN(Date.parse(dateString))) {
        return errors.push({
          message: "".concat(labels[key], " doesn't look like a date to me")
        });
      }
    });

    if (attrs.begin && attrs.end) {
      var begin = Date.parse(attrs.begin);
      var end = Date.parse(attrs.end);

      if (begin > end) {
        errors.push({
          message: 'Collection of this data finished before it started!'
        });
      }
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(errors)) {// return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
    } else {
      return errors;
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/OnlineResource.js


/* harmony default export */ const OnlineResource = (backbone_default().Model.extend({
  validate: function validate(attrs) {
    // from https://github.com/jzaefferer/jquery-validation/blob/043fb91da7dab0371fded5e2a9fa7ebe4c836210/src/core.js#L1180
    var urlRegEx = new RegExp("\n^(?:(?:(?:https?|ftp):)?\\/\\/)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\xA1-\uFFFF0-9]-*)*[a-z\xA1-\uFFFF0-9]+)(?:\\.(?:[a-z\xA1-\uFFFF0-9]-*)*[a-z\xA1-\uFFFF0-9]+)*(?:\\.(?:[a-z\xA1-\uFFFF]{2,})).?)(?::\\d{2,5})?(?:[/?#]\\S*)?$\n");

    var isValidUrl = function isValidUrl(url) {
      return urlRegEx.test(url);
    };

    var errors = [];

    if (!isValidUrl(attrs.url)) {
      errors.push({
        message: "That url isn't right."
      });
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(errors)) {// return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
    } else {
      return errors;
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/PointOfContact.js


/* harmony default export */ const PointOfContact = (models_Contact.extend({
  defaults: index_all/* default.extend */.ZP.extend({}, models_Contact.prototype.defaults, {
    role: 'pointOfContact'
  }),
  intialize: function intialize() {
    return models_Contact.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/Reference.js


/* harmony default export */ const models_Reference = (backbone_default().Model.extend({
  validate: function validate(attrs) {
    var urlRegEx = new RegExp("\n^(?:(?:(?:https?|ftp):)?\\/\\/)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\xA1-\uFFFF0-9]-*)*[a-z\xA1-\uFFFF0-9]+)(?:\\.(?:[a-z\xA1-\uFFFF0-9]-*)*[a-z\xA1-\uFFFF0-9]+)*(?:\\.(?:[a-z\xA1-\uFFFF]{2,})).?)(?::\\d{2,5})?(?:[/?#]\\S*)?$\n");
    var DOIRegEx = new RegExp("\n^https?:\\/\\/doi\\.org\\/10.\\d{4,9}\\/[-._;()/:A-Za-z0-9]+$\n");

    var isValidDOI = function isValidDOI(doi) {
      return DOIRegEx.test(doi);
    };

    var isValidURL = function isValidURL(nora) {
      return urlRegEx.test(nora);
    };

    var errors = [];

    if (attrs.doi && !isValidDOI(attrs.doi)) {
      errors.push({
        message: 'Invalid DOI url'
      });
    }

    if (attrs.nora && !isValidURL(attrs.nora)) {
      errors.push({
        message: 'Invalid NORA URL'
      });
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(errors)) {// return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
    } else {
      return errors;
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/ResourceType.js

/* harmony default export */ const ResourceType = (backbone_default().Model.extend({
  defaults: {
    value: ''
  },
  uris: {
    dataset: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/dataset',
    series: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/series',
    service: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/services'
  },
  initialize: function initialize() {
    this.on('change:value', this.updateUri);
  },
  updateUri: function updateUri(model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : '');
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/SaPhysicalState.js

/* harmony default export */ const SaPhysicalState = (backbone_default().Model.extend({
  defaults: {
    value: ''
  },
  uris: {
    'Air dried': 'http://vocab.ceh.ac.uk/esb#airdry',
    'Chemical extract': 'http://vocab.ceh.ac.uk/esb#chemicalExtract',
    'Chilled (refrigerated)': 'http://vocab.ceh.ac.uk/esb#chilled',
    'Fixed in formalin': 'http://vocab.ceh.ac.uk/esb#formalinFixed',
    'Formalin-Fixed Paraffin-Embedded (FFPE) tissue': 'http://vocab.ceh.ac.uk/esb#formalin-paraffin',
    'Freeze dried': 'http://vocab.ceh.ac.uk/esb#freezeDried',
    Fresh: 'http://vocab.ceh.ac.uk/esb#fresh',
    'Frozen (-198 degrees C)': 'http://vocab.ceh.ac.uk/esb#frozen-198',
    'Frozen (-20 degrees C)': 'http://vocab.ceh.ac.uk/esb#frozen-20',
    'Frozen (-80 degrees C)': 'http://vocab.ceh.ac.uk/esb#frozen-80',
    'Natural state': 'http://vocab.ceh.ac.uk/esb#naturalState',
    'Oven dry': 'http://vocab.ceh.ac.uk/esb#ovendry',
    Preserved: 'http://vocab.ceh.ac.uk/esb#preserved',
    'Preserved in alcohol': 'http://vocab.ceh.ac.uk/esb#preserved-in-alcohol',
    Slide: 'http://vocab.ceh.ac.uk/esb#slide',
    Taxidermy: 'http://vocab.ceh.ac.uk/esb#taxidermy',
    'Under liquid nitrogen': 'http://vocab.ceh.ac.uk/esb#liquidnitrogen'
  },
  initialize: function initialize() {
    return this.on('change:value', this.updateUri);
  },
  updateUri: function updateUri(model, value) {
    return this.set('uri', this.uris[value] ? this.uris[value] : '');
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/SaSpecimenType.js

/* harmony default export */ const SaSpecimenType = (backbone_default().Model.extend({
  defaults: {
    value: ''
  },
  uris: {
    Air: 'http://vocab.ceh.ac.uk/esb#air',
    DNA: 'http://vocab.ceh.ac.uk/esb#dna',
    Ectoparasite: 'http://vocab.ceh.ac.uk/esb#ectoparasite',
    Endoparasite: 'http://vocab.ceh.ac.uk/esb#endoparasite',
    Fossil: 'http://vocab.ceh.ac.uk/esb#fossil',
    'Fresh water': 'http://vocab.ceh.ac.uk/esb#freshWater',
    Gas: 'http://vocab.ceh.ac.uk/esb#gas',
    'Ice core': 'http://vocab.ceh.ac.uk/esb#iceCore',
    Pathogen: 'http://vocab.ceh.ac.uk/esb#pathogen',
    'Rain water': 'http://vocab.ceh.ac.uk/esb#rain',
    RNA: 'http://vocab.ceh.ac.uk/esb#rna',
    Rock: 'http://vocab.ceh.ac.uk/esb#rock',
    'Sea water': 'http://vocab.ceh.ac.uk/esb#seaWater',
    Sediment: 'http://vocab.ceh.ac.uk/esb#sediment',
    Seed: 'http://vocab.ceh.ac.uk/esb#seed',
    Soil: 'http://vocab.ceh.ac.uk/esb#soil',
    'Surface water': 'http://vocab.ceh.ac.uk/esb#surfaceWater',
    Vegetation: 'http://vocab.ceh.ac.uk/esb#Vegetation'
  },
  initialize: function initialize() {
    return this.on('change:value', this.updateUri);
  },
  updateUri: function updateUri(model, value) {
    return this.set('uri', this.uris[value] ? this.uris[value] : '');
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/SaTissue.js

/* harmony default export */ const SaTissue = (backbone_default().Model.extend({
  defaults: {
    value: ''
  },
  uris: {
    Bone: 'http://vocab.ceh.ac.uk/esb#bone',
    Brain: 'http://vocab.ceh.ac.uk/esb#brain',
    Egg: 'http://vocab.ceh.ac.uk/esb#egg',
    Fat: 'http://vocab.ceh.ac.uk/esb#fat ',
    Feather: 'http://vocab.ceh.ac.uk/esb#feather',
    Fur: 'http://vocab.ceh.ac.uk/esb#fur',
    'Gut contents': 'http://vocab.ceh.ac.uk/esb#stomachContents',
    Heart: 'http://vocab.ceh.ac.uk/esb#heart',
    'Homogenised whole sample': 'http://vocab.ceh.ac.uk/esb#Homogenised',
    Kidney: 'http://vocab.ceh.ac.uk/esb#kidney',
    Liver: 'http://vocab.ceh.ac.uk/esb#liver',
    Lung: 'http://vocab.ceh.ac.uk/esb#lung',
    'Lymph node': 'http://vocab.ceh.ac.uk/esb#lymphNode',
    Muscle: 'http://vocab.ceh.ac.uk/esb#muscle',
    'Nerve/spinal cord': 'http://vocab.ceh.ac.uk/esb#nerve-spinalcord',
    Plasma: 'http://vocab.ceh.ac.uk/esb#plasma',
    Serum: 'http://vocab.ceh.ac.uk/esb#serum',
    'Skin ': 'http://vocab.ceh.ac.uk/esb#skin',
    Spleen: 'http://vocab.ceh.ac.uk/esb#spleen',
    Trachea: 'http://vocab.ceh.ac.uk/esb#trachea',
    'Whole blood': 'http://vocab.ceh.ac.uk/esb#blood',
    'Whole body': 'http://vocab.ceh.ac.uk/esb#carcase'
  },
  initialize: function initialize() {
    return this.on('change:value', this.updateUri);
  },
  updateUri: function updateUri(model, value) {
    return this.set('uri', this.uris[value] ? this.uris[value] : '');
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/Service.js

/* harmony default export */ const Service = (backbone_default().Model.extend({
  defaults: {
    versions: [],
    coupledResources: [],
    containsOperations: []
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/SpatialResolution.js


/* harmony default export */ const SpatialResolution = (backbone_default().Model.extend({
  validate: function validate(attrs) {
    var errors = [];
    var equivalentScale = attrs.equivalentScale;
    var distance = attrs.distance;

    if (equivalentScale && distance) {
      errors.push({
        message: 'You can <b>EITHER</b> enter an Equivalent scale <b>OR</b> a Distance but not both.'
      });
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(errors)) {// return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
    } else {
      return errors;
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/Supplemental.js


/* harmony default export */ const Supplemental = (backbone_default().Model.extend({
  validate: function validate(attrs) {
    // from https://github.com/jzaefferer/jquery-validation/blob/043fb91da7dab0371fded5e2a9fa7ebe4c836210/src/core.js#L1180
    var urlRegEx = new RegExp("\n^(?:(?:(?:https?|ftp):)?\\/\\/)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\xA1-\uFFFF0-9]-*)*[a-z\xA1-\uFFFF0-9]+)(?:\\.(?:[a-z\xA1-\uFFFF0-9]-*)*[a-z\xA1-\uFFFF0-9]+)*(?:\\.(?:[a-z\xA1-\uFFFF]{2,})).?)(?::\\d{2,5})?(?:[/?#]\\S*)?$\n");

    var isValidUrl = function isValidUrl(url) {
      return urlRegEx.test(url);
    };

    var errors = [];

    if (attrs.url && !isValidUrl(attrs.url)) {
      errors.push({
        message: 'Are you sure that web address is correct?'
      });
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(errors)) {// return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
    } else {
      return errors;
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/TopicCategory.js

/* harmony default export */ const TopicCategory = (backbone_default().Model.extend({
  defaults: {
    value: ''
  },
  uris: {
    biota: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/biota',
    boundaries: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/boundaries',
    climatologyMeteorologyAtmosphere: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/climatologyMeteorologyAtmosphere',
    economy: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/economy',
    elevation: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/elevation',
    environment: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/environment',
    farming: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/farming',
    geoscientificInformation: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/geoscientificInformation',
    health: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/health',
    imageryBaseMapsEarthCover: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/imageryBaseMapsEarthCover',
    inlandWaters: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/inlandWaters',
    intelligenceMilitary: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/intelligenceMilitary',
    location: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/location',
    oceans: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/oceans',
    planningCadastre: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/planningCadastre',
    society: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/society',
    structure: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/structure',
    transportation: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/transportation',
    utilitiesCommunication: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/utilitiesCommunication'
  },
  initialize: function initialize() {
    return this.on('change:value', this.updateUri);
  },
  updateUri: function updateUri(model, value) {
    return this.set('uri', this.uris[value] ? this.uris[value] : '');
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/SaTaxa.js

/* harmony default export */ const SaTaxa = (backbone_default().Model.extend({
  defaults: {
    value: ''
  },
  uris: {
    Algae: 'http://vocab.ceh.ac.uk/esb#algae',
    Amphibian: 'http://vocab.ceh.ac.uk/esb#amphibian',
    Annelid: 'http://vocab.ceh.ac.uk/esb#annelid',
    Arthropod: 'http://vocab.ceh.ac.uk/esb#arthropod',
    Bacteria: 'http://vocab.ceh.ac.uk/esb#bacteria',
    Bird: 'http://vocab.ceh.ac.uk/esb#bird',
    Fern: 'http://vocab.ceh.ac.uk/esb#fern',
    Fish: 'http://vocab.ceh.ac.uk/esb#fish',
    Fungi: 'http://vocab.ceh.ac.uk/esb#fungi',
    Invertebrate: 'http://vocab.ceh.ac.uk/esb#invertebrate',
    Lichen: 'http://vocab.ceh.ac.uk/esb#lichen',
    Mammal: 'http://vocab.ceh.ac.uk/esb#mammal',
    Mollusc: 'http://vocab.ceh.ac.uk/esb#mollusc',
    'Moss or liverwort': 'http://vocab.ceh.ac.uk/esb#moss',
    Nematode: 'http://vocab.ceh.ac.uk/esb#nematode',
    Plankton: 'http://vocab.ceh.ac.uk/esb#plankton',
    Plant: 'http://vocab.ceh.ac.uk/esb#plant',
    Platyhelminthes: 'http://vocab.ceh.ac.uk/esb#platyhelminthes',
    Protozoa: 'http://vocab.ceh.ac.uk/esb#protozoa',
    Reptile: 'http://vocab.ceh.ac.uk/esb#reptile',
    Virus: 'http://vocab.ceh.ac.uk/esb#virus'
  },
  initialize: function initialize() {
    return this.on('change:value', this.updateUri);
  },
  updateUri: function updateUri(model, value) {
    return this.set('uri', this.uris[value] ? this.uris[value] : '');
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/service-agreement/Author.js


/* harmony default export */ const Author = (backbone_default().Model.extend({
  defaults: {
    address: {}
  },
  validate: function validate(attrs) {
    var emailRegEx = new RegExp("\n^[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$\n");
    var orcidRegEx = new RegExp("\n^https?:\\/\\/orcid.org\\/(\\d{4}-){3}\\d{3}[\\dX]$\n");
    var errors = [];
    var organisationName = attrs.organisationName;
    var email = attrs.email;
    var individualName = attrs.individualName;
    var nameIdentifier = attrs.nameIdentifier;

    var isValidEmail = function isValidEmail(address) {
      return emailRegEx.test(address);
    };

    var isValidORCID = function isValidORCID(id) {
      return orcidRegEx.test(id);
    };

    if (email && !isValidEmail(email)) {
      errors.push({
        message: 'That email address is invalid'
      });
    }

    if (nameIdentifier && !isValidORCID(nameIdentifier)) {
      errors.push({
        message: 'That ORCiD is invalid.  ORCiDs should be entered as https://orcid.org/0000-1234-5678-999X <b>not</b> 0000-1234-5678-999X'
      });
    }

    if (!organisationName || !individualName || !email) {
      errors.push({
        message: 'Author name, affiliation and email address are mandatory.'
      });
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
      return;
    } else {
      return errors;
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(warnings)) {
      return;
    } else {
      return warnings;
    }
  },
  toJSON: function toJSON() {
    if (index_all/* default.isEmpty */.ZP.isEmpty(this.get('address'))) {
      return index_all/* default.omit */.ZP.omit(this.attributes, 'address');
    } else {
      return index_all/* default.clone */.ZP.clone(this.attributes);
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/service-agreement/RightsHolder.js


/* harmony default export */ const RightsHolder = (backbone_default().Model.extend({
  defaults: {
    address: {}
  },
  validate: function validate(attrs) {
    var rorRegEx = new RegExp("\n^https?:\\/\\/ror.org\\/\\w{8,10}$\n");
    var errors = [];
    var organisationName = attrs.organisationName;
    var organisationIdentifier = attrs.organisationIdentifier;

    var isValidROR = function isValidROR(id) {
      return rorRegEx.test(id);
    };

    if (organisationIdentifier && !isValidROR(organisationIdentifier)) {
      errors.push({
        message: 'That RoR is invalid '
      });
    }

    if (!organisationName) {
      errors.push({
        message: 'Affiliation (organisation name) is  mandatory.'
      });
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
      return;
    } else {
      return errors;
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(warnings)) {
      return;
    } else {
      return warnings;
    }
  },
  toJSON: function toJSON() {
    if (index_all/* default.isEmpty */.ZP.isEmpty(this.get('address'))) {
      return index_all/* default.omit */.ZP.omit(this.attributes, 'address');
    } else {
      return index_all/* default.clone */.ZP.clone(this.attributes);
    }
  }
}));
// EXTERNAL MODULE: ./editor/src/index.js
var src = __webpack_require__(5456);
;// CONCATENATED MODULE: ./editor/src/models/service-agreement/ServiceAgreement.js


/* harmony default export */ const ServiceAgreement = (src/* EditorMetadata.extend */.o4.extend({
  initialize: function initialize(data, options) {
    src/* EditorMetadata.prototype.initialize.apply */.o4.prototype.initialize.apply(this);

    if ('id' in data) {
      this.id = data.id;
      this.parameters = '';
    } else {
      this.id = options.id;
      this.parameters = '?catalogue=eidc';
    }
  },
  urlRoot: function urlRoot() {
    return "/service-agreement/".concat(this.id).concat(this.parameters);
  },
  validate: function validate(attrs) {
    var errors = src/* EditorMetadata.prototype.validate.call */.o4.prototype.validate.call(this, attrs) || [];

    if ((attrs != null ? attrs.depositorContactDetails : undefined) == null) {
      errors.push('Depositor contact details are mandatory');
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(errors)) {// return nothing from Backbone.Model.validate because returning something signals a validation error.
    } else {
      return errors;
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/service-agreement/SupportingDoc.js

/* harmony default export */ const SupportingDoc = (backbone_default().Model.extend({
  defaults: {
    content: 'Collection/generation methods\nNature and Units of recorded values\nQuality control\nDetails of data structure\nExperimental design/Sampling regime\nFieldwork and laboratory instrumentation\nCalibration steps and values\nAnalytical methods\nAny other information useful to the interpretation of the data'
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/InfrastructureCategory.js

/* harmony default export */ const InfrastructureCategory = (backbone_default().Model.extend({
  defaults: {
    value: null
  },
  uris: {
    instrumentedSites: 'http://vocabs.ceh.ac.uk/ri/instrumentedSites',
    surveys: 'http://vocabs.ceh.ac.uk/ri/surveys',
    wildlifeSchemes: 'http://vocabs.ceh.ac.uk/ri/wildlifeSchemes',
    discoveryCollections: 'http://vocabs.ceh.ac.uk/ri/discoveryCollections',
    mobilePlatforms: 'http://vocabs.ceh.ac.uk/ri/mobilePlatforms',
    controlledPlatforms: 'http://vocabs.ceh.ac.uk/ri/controlledPlatforms',
    fieldPlatforms: 'http://vocabs.ceh.ac.uk/ri/fieldPlatforms',
    labsAnalysis: 'http://vocabs.ceh.ac.uk/ri/labsAnalysis',
    labsTest: 'http://vocabs.ceh.ac.uk/ri/labsTest'
  },
  classes: {
    instrumentedSites: 'Environmental observatories',
    surveys: 'Environmental observatories',
    wildlifeSchemes: 'Environmental observatories',
    discoveryCollections: 'Environmental observatories',
    mobilePlatforms: 'Environmental observatories',
    controlledPlatforms: 'Environmental experiment platforms',
    fieldPlatforms: 'Environmental experiment platforms',
    labsAnalysis: 'Environmental analysis',
    labsTest: 'Environmental analysis'
  },
  descriptions: {
    instrumentedSites: 'Instrumented sites',
    surveys: 'Surveys',
    wildlifeSchemes: 'Wildlife monitoring schemes',
    discoveryCollections: 'Discovery collections',
    mobilePlatforms: 'Mobile observing platforms',
    controlledPlatforms: 'Controlled environment platforms',
    fieldPlatforms: 'Field research platforms',
    labsAnalysis: 'Analysis labs',
    labsTest: 'Test labs'
  },
  initialize: function initialize() {
    return this.on('change:value', this.updateLimitation);
  },
  updateLimitation: function updateLimitation(model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : null);
    this.set('infrastructureClass', this.classes[value] ? this.classes[value] : null);
    return this.set('description', this.descriptions[value] ? this.descriptions[value] : null);
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/InfrastructureChallenge.js

/* harmony default export */ const InfrastructureChallenge = (backbone_default().Model.extend({
  defaults: {
    value: ''
  },
  uris: {
    Biodiversity: 'http://vocab.ceh.ac.uk/ri#Biodiversity',
    Pollution: 'http://vocab.ceh.ac.uk/ri#Pollution',
    'Climate change resilience': 'http://vocab.ceh.ac.uk/ri#Resilience',
    'Climate change mitigation': 'http://vocab.ceh.ac.uk/ri#Mitigation'
  },
  initialize: function initialize() {
    return this.on('change:value', this.updateUri);
  },
  updateUri: function updateUri(model, value) {
    return this.set('uri', this.uris[value] ? this.uris[value] : '');
  }
}));
;// CONCATENATED MODULE: ./editor/src/models/index.js





























;// CONCATENATED MODULE: ./editor/src/templates/ServiceOperation.tpl
// Module
var ServiceOperation_code = "<div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label class=\"control-label\" for=\"serviceOperation<%= data.index %>OperationName\">Operation Name</label> </div> <div class=\"col-sm-4 col-lg-4\"> <select data-name=\"operationName\" class=\"editor-input operationName\" id=\"serviceOperation<%= data.index %>OperationName\" <%= data.disabled%>> <option value=\"\">- Select Operation Name -</option> <option>Cancel</option> <option>Confirm</option> <option>DescribeCoverage</option> <option>DescribeFeatureType</option> <option>DescribeLayer</option> <option>DescribeProcess</option> <option>DescribeRecord</option> <option>DescribeResultAccess</option> <option>DescribeSensor</option> <option>DescribeTasking</option> <option>Execute</option> <option>GetCapabilities</option> <option>GetCoverage</option> <option>GetDomain</option> <option>GetFeasibility</option> <option>GetFeature</option> <option>GetFeatureInfo</option> <option>GetFeatureWithLock</option> <option>GetGMLObject</option> <option>GetLegendGraphic</option> <option>GetMap</option> <option>GetObservation</option> <option>GetPropertyValue</option> <option>GetRecordById</option> <option>GetRecords</option> <option>GetStatus</option> <option>GetTask</option> <option>GetTile</option> <option>Harvest</option> <option>LockFeature</option> <option>Reserve</option> <option>Submit</option> <option>Transaction</option> <option>Update</option> </select> </div> <div class=\"col-sm-2 col-lg-2\"> <label class=\"control-label\" for=\"serviceOperation<%= data.index %>Platform\">Platform</label> </div> <div class=\"col-sm-4 col-lg-4\"> <select data-name=\"platform\" class=\"editor-input platform\" id=\"serviceOperation<%= data.index %>Platform\" <%= data.disabled%>> <option value=\"\">- Select Platform -</option> <option>COM</option> <option>Corba</option> <option>HTTPGet</option> <option>HTTPPost</option> <option>HTTPSoap</option> <option>Java</option> <option>SQL</option> <option>WebService</option> <option>XML</option> </select> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label class=\"control-label\" for=\"serviceOperation<%= data.index %>Url\">URL</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"url\" class=\"editor-input\" id=\"serviceOperation<%= data.index %>Url\" value=\"<%= data.url %>\" <%= data.disabled%>> </div> </div>";
// Exports
/* harmony default export */ const ServiceOperation = (ServiceOperation_code);
;// CONCATENATED MODULE: ./editor/src/views/ServiceOperationView.js



/* harmony default export */ const ServiceOperationView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(ServiceOperation);
    ObjectInputView.prototype.render.apply(this);
    this.$('select.operationName').val(this.model.get('operationName'));
    return this.$('select.platform').val(this.model.get('platform'));
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/CoupledResource.tpl
// Module
var CoupledResource_code = "<div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label class=\"control-label\" for=\"coupledResource<%= data.index %>OperationName\">Operation Name</label> </div> <div class=\"col-sm-4 col-lg-4\"> <select data-name=\"operationName\" class=\"editor-input\" id=\"coupledResource<%= data.index %>OperationName\" <%= data.disabled%>> <option value=\"\">- Select Operation -</option> <option>COM</option> <option>Corba</option> <option>GetMap</option> <option>HTTPGet</option> <option>HTTPPost</option> <option>HTTPSoap</option> <option>Java</option> <option>SQL</option> <option>WebService</option> <option>XML</option> </select> </div> <div class=\"col-sm-2 col-lg-2\"> <label class=\"control-label\" for=\"coupledResource<%= data.index %>LayerName\">Layer Name</label> </div> <div class=\"col-sm-4 col-lg-4\"> <input data-name=\"layerName\" class=\"editor-input\" id=\"coupledResource<%= data.index %>LayerName\" value=\"<%= data.layerName %>\" <%= data.disabled%>> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label class=\"control-label\" for=\"coupledResource<%= data.index %>Identifier\">Identifier</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"identifier\" class=\"editor-input\" id=\"coupledResource<%= data.index %>Identifier\" value=\"<%= data.identifier %>\" <%= data.disabled%>> </div> </div>";
// Exports
/* harmony default export */ const CoupledResource = (CoupledResource_code);
;// CONCATENATED MODULE: ./editor/src/views/CoupledResourceView.js



/* harmony default export */ const CoupledResourceView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(CoupledResource);
    ObjectInputView.prototype.render.apply(this);
    return this.$('select').val(this.model.get('operationName'));
  }
}));
;// CONCATENATED MODULE: ./editor/src/views/ServiceView.js









/* harmony default export */ const ServiceView = (SingleView/* default.extend */.Z.extend({
  initialize: function initialize(options) {
    SingleView/* default.prototype.initialize.call */.Z.prototype.initialize.call(this, options);
    var service = new Service(this.model.get('service'));
    this.render();
    this.listenTo(service, 'change', function (model) {
      return this.updateMetadataModel(model.toJSON());
    });
    var that = this;
    jquery_default()(document).ready(function () {
      var typeView = new ServiceTypeView({
        model: service,
        modelAttribute: 'type',
        disabled: options.disabled
      });
      var serviceOperations = new ParentView({
        model: service,
        modelAttribute: 'containsOperations',
        ObjectInputView: ServiceOperationView,
        label: 'Operations',
        multiline: true,
        disabled: options.disabled
      });
      var coupledResources = new ParentView({
        model: service,
        modelAttribute: 'coupledResources',
        ObjectInputView: CoupledResourceView,
        label: 'Coupled Resources',
        multiline: true,
        disabled: options.disabled
      });
      var versions = new ParentStringView({
        model: service,
        modelAttribute: 'versions',
        label: 'Versions',
        disabled: options.disabled
      });

      index_all/* default.invoke */.ZP.invoke([serviceOperations, coupledResources, versions], 'show');

      return that.$('.dataentry').append(typeView.el, serviceOperations.el, coupledResources.el, versions.el);
    });
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/MapReprojection.tpl
// Module
var MapReprojection_code = "<div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">EPSG:</label> </div> <div class=\"col-sm-2 col-lg-2\"> <div class=\"input-group\"> <input data-name=\"epsgCode\" class=\"editor-input\" value=\"<%= data.epsgCode %>\" <%= data.disabled%>> <span class=\"input-group-btn\"> <button class=\"editor-button-xs remove\" type=\"button\" <%= data.disabled%>><span class=\"fas fa-times\" aria-hidden=\"true\"></span></button> </span> </div> </div> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Path</label> </div> <div class=\"col-sm-8 col-lg-8\"> <input data-name=\"path\" class=\"editor-input\" value=\"<%= data.path %>\" <%= data.disabled%>> </div> </div>";
// Exports
/* harmony default export */ const MapReprojection = (MapReprojection_code);
;// CONCATENATED MODULE: ./editor/src/views/MapReprojectionView.js



/* harmony default export */ const MapReprojectionView = (ObjectInputView.extend({
  initialize: function initialize() {
    this.template = index_all/* default.template */.ZP.template(MapReprojection);
  },
  events: index_all/* default.extend */.ZP.extend({}, ObjectInputView.prototype.events, {
    'click button.remove': 'delete'
  }),
  "delete": function _delete() {
    return this.model.collection.remove(this.model);
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/MapStyleSelector.tpl
// Module
var MapStyleSelector_code = "<div class=\"input-group\"> <input type=\"text\" class=\"form-control\" value=\"<%= data.type %>\" <%= data.disabled%>/> <div class=\"input-group-btn\"> <button type=\"button\" class=\"btn btn-default dropdown-toggle\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\" <%= data.disabled%>> <span class=\"selected icon\"></span> </button> <ul class=\"dropdown-menu dropdown-menu-right\"> <li><a href=\"#\" data-symbol=\"blank\">Polygon</a></li> <li role=\"separator\" class=\"divider\"></li> <% _.chain(data.symbols).each(function(s, id){%> <li><a href=\"#\" data-symbol=\"<%=id%>\"><span class=\"icon\"><%=s.icon%></span> <%=s.label%></a></li> <%})%> </ul> </div> </div>";
// Exports
/* harmony default export */ const MapStyleSelector = (MapStyleSelector_code);
// EXTERNAL MODULE: ./node_modules/bootstrap-colorpicker/dist/js/bootstrap-colorpicker.js
var bootstrap_colorpicker = __webpack_require__(8997);
;// CONCATENATED MODULE: ./editor/src/views/MapStyleSelectorView.js





/* harmony default export */ const MapStyleSelectorView = (ObjectInputView.extend({
  events: {
    changeColor: 'setColour',
    'click a[data-symbol]': 'setSymbol'
  },
  buttonColour: '#fff',
  defaultColour: '#fff',
  symbols: {
    circle: {
      icon: '⬤',
      label: 'Circle'
    },
    square: {
      icon: '⬛',
      label: 'Square'
    }
  },
  initialize: function initialize(options) {
    this.template = index_all/* default.template */.ZP.template(MapStyleSelector);
    ObjectInputView.prototype.initialize.call(this, index_all/* default.extend */.ZP.extend({}, options, {
      symbols: this.symbols
    }));
    this.update();
    this.$('input').colorpicker({
      format: 'hex'
    });
    return this.listenTo(this.model, 'change:colour change:symbol', this.update);
  },
  update: function update() {
    var color = this.model.get('colour');
    this.$('input').val(color);

    if (this.model.has('symbol')) {
      var symbol = this.model.get('symbol');
      this.$('.selected').html(this.symbols[symbol].icon);
      this.$('button').css({
        backgroundColor: this.buttonColour
      });
    } else {
      this.$('.selected').html('&nbsp;');
      this.$('button').css({
        backgroundColor: color
      });
    }

    return this.$('.icon').css({
      color: color
    });
  },
  setColour: function setColour() {
    return this.model.set('colour', this.$('input').val());
  },
  setSymbol: function setSymbol(e) {
    if (jquery_default()(e.target).data('symbol') !== 'blank') {
      return this.model.set('symbol', jquery_default()(e.target).data('symbol'));
    } else {
      return this.model.unset('symbol');
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/MapFeatures.tpl
// Module
var MapFeatures_code = "<div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Layer ID/Name</label> </div> <div class=\"col-sm-3 col-lg-3\"> <input data-name=\"name\" class=\"editor-input\" value=\"<%= data.name %>\"> </div> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Legend</label> </div> <div class=\"col-sm-3 col-lg-3\"> <input data-name=\"label\" class=\"editor-input\" value=\"<%= data.label %>\"> </div> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Style</label> </div> <div class=\"col-sm-3 col-lg-3 style-selector\"></div> </div>";
// Exports
/* harmony default export */ const MapFeatures = (MapFeatures_code);
;// CONCATENATED MODULE: ./editor/src/views/MapFeaturesView.js




/* harmony default export */ const MapFeaturesView = (ObjectInputView.extend({
  initialize: function initialize(options) {
    this.template = index_all/* default.template */.ZP.template(MapFeatures);
    ObjectInputView.prototype.initialize.call(this, options);
    return new MapStyleSelectorView({
      el: this.$('.style-selector'),
      model: this.model.getRelated('style')
    });
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/MapValue.tpl
// Module
var MapValue_code = "<div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Label</label> </div> <div class=\"col-sm-3 col-lg-3\"> <input data-name=\"label\" class=\"editor-input\" value=\"<%= data.label %>\" <%= data.disabled%>> </div> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Setting</label> </div> <div class=\"col-sm-3 col-lg-3\"> <input data-name=\"setting\" class=\"editor-input\" value=\"<%= data.setting %>\" <%= data.disabled%>> </div> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Style</label> </div> <div class=\"col-sm-3 col-lg-3 style-selector\"></div> </div>";
// Exports
/* harmony default export */ const MapValue = (MapValue_code);
;// CONCATENATED MODULE: ./editor/src/views/MapValueView.js




/* harmony default export */ const MapValueView = (ObjectInputView.extend({
  initialize: function initialize(options) {
    this.template = index_all/* default.template */.ZP.template(MapValue);
    ObjectInputView.prototype.initialize.call(this, options);
    return new MapStyleSelectorView({
      el: this.$('.style-selector'),
      model: this.model.getRelated('style'),
      disabled: options.disabled
    });
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/MapBucket.tpl
// Module
var MapBucket_code = "<div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Label</label> </div> <div class=\"col-sm-3 col-lg-3\"> <input data-name=\"label\" class=\"editor-input\" value=\"<%= data.label %>\" <%= data.disabled%>> </div> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Bucket</label> </div> <div class=\"col-sm-3 col-lg-3\"> <div class=\"input-group\"> <input data-name=\"min\" class=\"form-control editor-input\" value=\"<%= data.min %>\" placeholder=\"-∞\" <%= data.disabled%>> <span class=\"input-group-addon\">'&#60 𝑥 &#8804</span> <input data-name=\"max\" class=\"form-control editor-input\" value=\"<%= data.max %>\" placeholder=\"∞\" <%= data.disabled%>> </div> </div> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Style</label> </div> <div class=\"col-sm-3 col-lg-3 style-selector\"></div> </div>";
// Exports
/* harmony default export */ const MapBucket = (MapBucket_code);
;// CONCATENATED MODULE: ./editor/src/views/MapBucketView.js




/* harmony default export */ const MapBucketView = (ObjectInputView.extend({
  initialize: function initialize(options) {
    this.template = index_all/* default.template */.ZP.template(MapBucket);
    ObjectInputView.prototype.initialize.call(this, options);
    return new MapStyleSelectorView({
      el: this.$('.style-selector'),
      model: this.model.getRelated('style'),
      disabled: options.disabled
    });
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/MapAttribute.tpl
// Module
var MapAttribute_code = "<h4>Attribute Definition</h4> <div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Name</label> </div> <div class=\"col-sm-7 col-lg-7\"> <input data-name=\"id\" class=\"editor-input\" value=\"<%= data.id %>\" <%= data.disabled%>> </div> <div class=\"col-sm-2 col-lg-2\"> <label class=\"control-label\">Data type</label> </div> <div class=\"col-sm-2 col-lg-2\"> <select data-name=\"type\" class=\"editor-input\" <%= data.disabled%>> <% _.each(data.types, function(d) {%> <option value=\"<%=d.value%>\" <%= d.value==data.type ? 'selected=\"selected\"': '' %>><%=d.name%></option> <%});%> </select> </div> </div> <h4>Layer Definition</h4> <div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">ID</label> </div> <div class=\"col-sm-3 col-lg-3\"> <input data-name=\"name\" class=\"editor-input\" value=\"<%= data.name %>\" <%= data.disabled%>> </div> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Title</label> </div> <div class=\"col-sm-7 col-lg-7\"> <input data-name=\"label\" class=\"editor-input\" value=\"<%= data.label %>\" <%= data.disabled%>> </div> </div> <hr/> <h4>Values <button class=\"editor-button-xs addValue\" <%= data.disabled%>>Add <span class=\"fas fa-plus\"></span></button></h4> <div class=\"values\"></div> <hr/> <h4>Buckets <button class=\"editor-button-xs addBucket\" <%= data.disabled%>>Add <span class=\"fas fa-plus\"></span></button></h4> <div class=\"buckets\"></div>";
// Exports
/* harmony default export */ const templates_MapAttribute = (MapAttribute_code);
;// CONCATENATED MODULE: ./editor/src/views/MapAttributeView.js






/* harmony default export */ const MapAttributeView = (ObjectInputView.extend({
  defaultLegend: {
    style: {
      colour: '#000000'
    }
  },
  dataTypes: [{
    name: 'Text',
    value: 'TEXT'
  }, {
    name: 'Number',
    value: 'NUMBER'
  }],
  events: function events() {
    return index_all/* default.extend */.ZP.extend({}, ObjectInputView.prototype.events, {
      'click .addValue': 'addValue',
      'click .addBucket': 'addBucket'
    });
  },
  initialize: function initialize(options) {
    this.template = index_all/* default.template */.ZP.template(templates_MapAttribute);
    ObjectInputView.prototype.initialize.call(this, index_all/* default.extend */.ZP.extend({}, options, {
      types: this.dataTypes
    }));
    this.buckets = this.model.getRelatedCollection('buckets');
    this.values = this.model.getRelatedCollection('values');
    this.createList(this.buckets, '.buckets', this.newBucket);
    return this.createList(this.values, '.values', this.newValue);
  },
  addValue: function addValue() {
    return this.values.add(this.defaultLegend);
  },
  addBucket: function addBucket() {
    return this.buckets.add(this.defaultLegend);
  },
  newValue: function newValue(m) {
    return new ChildView({
      model: m,
      ObjectInputView: MapValueView,
      disabled: this.data.disabled
    });
  },
  newBucket: function newBucket(m) {
    return new ChildView({
      model: m,
      ObjectInputView: MapBucketView,
      disabled: this.data.disabled
    });
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/MapDataSource.tpl
// Module
var MapDataSource_code = "<div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">EPSG:</label> </div> <div class=\"col-sm-2 col-lg-2\"> <div class=\"input-group\"> <input data-name=\"epsgCode\" class=\"editor-input\" value=\"<%= data.epsgCode %>\" <%= data.disabled%>> <span class=\"input-group-btn\"> <button class=\"btn btn-default btn-sm addReprojection\" type=\"button\" <%= data.disabled%>><span class=\"fas fa-plus\" aria-hidden=\"true\"></span></button> </span> </div> </div> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Path</label> </div> <div class=\"col-sm-8 col-lg-8\"> <input data-name=\"path\" class=\"editor-input\" value=\"<%= data.path %>\" <%= data.disabled%>> </div> </div> <div class=\"reprojections\"></div> <div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Type</label> </div> <div class=\"col-sm-2 col-lg-2\"> <select data-name=\"type\" class=\"editor-input\" <%= data.disabled%>> <% _.each(data.types, function(d) {%> <option value=\"<%=d.value%>\" <%= _.isString(data.type) && d.value===data.type.toUpperCase() ? 'selected=\"selected\"': '' %>><%=d.name%></option> <%});%> </select> </div> <div class=\"col-sm-1 col-lg-1\"> <label class=\"control-label\">Styling</label> </div> <div class=\"col-sm-5 col-lg-5\"> <div class=\"btn-group\" role=\"group\"> <button type=\"button\" class=\"btn btn-sm btn-default\" styleMode=\"features\" <%= data.disabled%>>Simple</button> <button type=\"button\" class=\"btn btn-sm btn-default\" styleMode=\"attributes\" <%= data.disabled%>>Classification</button> </div> <button class=\"editor-button-xs addAttribute\" type=\"button\" <%= data.disabled%>>Define Attribute <span class=\"fas fa-plus\" aria-hidden=\"true\"></span></button> </div> <div class=\"col-sm-3 col-lg-3\"> <div class=\"byte-box\"> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label class=\"control-label\">Byte?</label>&nbsp; </div> <div class=\"col-sm-5 col-lg-5\"> <input data-name=\"bytetype\" type=\"radio\" name=\"bytetype\" value=\"true\" <%= data.disabled%>> Yes </div> <div class=\"col-sm-4 col-lg-4\"> <input data-name=\"bytetype\" type=\"radio\" name=\"bytetype\" value=\"false\" <%= data.disabled%>> No </div> </div> </div> </div> </div> <div class=\"styling-box features\"></div> <div class=\"styling-box attributes existing\"></div> ";
// Exports
/* harmony default export */ const templates_MapDataSource = (MapDataSource_code);
;// CONCATENATED MODULE: ./editor/src/views/MapDataSourceView.js








/* harmony default export */ const MapDataSourceView = (ObjectInputView.extend({
  events: function events() {
    return index_all/* default.extend */.ZP.extend({}, ObjectInputView.prototype.events, {
      'click .addReprojection': 'addReprojection',
      'click .addAttribute': 'addAttribute',
      'click [styleMode]': 'updateStyleMode'
    });
  },
  dataTypes: [{
    name: 'Polygon',
    value: 'POLYGON'
  }, {
    name: 'Raster',
    value: 'RASTER'
  }, {
    name: 'Point',
    value: 'POINT'
  }, {
    name: 'Line',
    value: 'LINE'
  }],
  initialize: function initialize(options) {
    this.template = index_all/* default.template */.ZP.template(templates_MapDataSource);
    ObjectInputView.prototype.initialize.call(this, index_all/* default.extend */.ZP.extend({}, options, {
      types: this.dataTypes
    }));
    this.reprojections = this.model.getRelatedCollection('reprojections');
    this.attributes = this.model.getAttributes();
    this.createList(this.reprojections, '.reprojections', this.newReprojection);
    this.createList(this.attributes, '.attributes', this.newAttribute);
    var mapFeaturesView = new MapFeaturesView({
      el: this.$('.features'),
      model: this.model.getRelated('features')
    });
    this.setStyleMode(this.model.stylingMode);
    this.listenTo(this.model, 'change:type', this.handlerByteTypeVisibility); // Set the radio button to the byteType of the model

    return this.updateByteRadioButton();
  },
  addReprojection: function addReprojection() {
    return this.reprojections.add({});
  },
  addAttribute: function addAttribute() {
    return this.attributes.add({});
  },
  newReprojection: function newReprojection(model, i) {
    return new MapReprojectionView({
      model: model,
      disabled: this.data.disabled
    });
  },
  newAttribute: function newAttribute(model, i) {
    return new ChildView({
      model: model,
      index: i,
      ObjectInputView: MapAttributeView,
      disabled: this.data.disabled
    });
  },
  updateStyleMode: function updateStyleMode(e) {
    return this.setStyleMode(jquery_default()(e.target).attr('styleMode'));
  },
  setStyleMode: function setStyleMode(mode) {
    // Reset the state of all the styling buttons and update to the correct mode
    this.$('button[stylemode]').removeClass('btn-success').removeClass('active');
    this.$("button[stylemode='".concat(mode, "']")).addClass('btn-success active');
    this.$('.styling-box').hide();
    this.$(".styling-box.".concat(mode)).show();
    this.updateByteTypeVisibility(mode, this.model.get('type'));
    var attrBtn = this.$('.addAttribute').removeClass('disabled');

    if (mode === 'features') {
      attrBtn.addClass('disabled');
    }

    return this.model.setStylingMode(mode);
  },
  handlerByteTypeVisibility: function handlerByteTypeVisibility(e) {
    return this.updateByteTypeVisibility(e.stylingMode, e.attributes.type);
  },

  /*
  Update the bytetype radio button to match the model
  */
  updateByteRadioButton: function updateByteRadioButton() {
    return jquery_default()(this.$("input[data-name='bytetype'][value='".concat(this.model.attributes.bytetype, "']"))[0]).attr('checked', 'checked');
  },

  /*
  Set the visibility of the byteType selector.  Only show it when the styling is 'attributes' and the type is 'RASTER'
  */
  updateByteTypeVisibility: function updateByteTypeVisibility(stylingMode, type) {
    if (stylingMode.toLowerCase() === 'attributes' && type.toLowerCase() === 'raster') {
      return this.$('.byte-box').show();
    } else {
      return this.$('.byte-box').hide();
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/RelatedRecord.tpl
// Module
var RelatedRecord_code = "<div class=\"row\"> <div class=\"col-sm-1\"> <label>Search</label> </div> <div class=\"col-sm-11 catalogueSearch\"> <input data-name=\"title\" value=\"<%= data.title %>\" id=\"relationship<%= data.index %>Name\" class=\"form-control autocomplete\" placeholder=\"Search the catalogue...\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-sm-offset-1\"> <label>Relationship</label> </div> <div class=\"col-sm-9\"> <select data-name=\"rel\" id=\"relationship<%= data.index %>Rel\" class=\"editor-input rel\"> <option value=\"https://vocabs.ceh.ac.uk/eidc#generates\">GENERATES&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a model generates a dataset)</option> <option value=\"https://vocabs.ceh.ac.uk/eidc#memberOf\">MEMBER OF&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a dataset is a member of a data collection)</option> <option value=\"https://vocabs.ceh.ac.uk/eidc#relatedTo\">RELATED TO</option> <option value=\"https://vocabs.ceh.ac.uk/eidc#supersedes\">SUPERSEDES</option> <option value=\"https://vocabs.ceh.ac.uk/eidc#uses\">USES&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a service uses a dataset)</option> </select> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-sm-offset-1\"> <label>Name</label> </div> <div class=\"col-sm-9\"> <input data-name=\"title\" id=\"relationship<%= data.index %>Title\" class=\"editor-input title\" value=\"<%= data.title %>\" autocomplete=\"off\" disabled=\"true\"> </div> </div> <div class=\"hidden\"> <input data-name=\"identifier\" id=\"relationship<%= data.index %>Identifier\" class=\"editor-input identifier\" value=\"<%= data.identifier %>\"> <input data-name=\"associationType\" id=\"relationship<%= data.index %>AssociationType\" class=\"editor-input associationType\" value=\"<%= data.associationType %>\"> </div> ";
// Exports
/* harmony default export */ const RelatedRecord = (RelatedRecord_code);
;// CONCATENATED MODULE: ./editor/src/views/RelatedRecordView.js




/* harmony default export */ const RelatedRecordView = (ObjectInputView.extend({
  initialize: function initialize() {
    var _this = this;

    this.template = index_all/* default.template */.ZP.template(RelatedRecord);
    ObjectInputView.prototype.initialize.apply(this);
    var catalogue = jquery_default()('html').data('catalogue');
    this.$('.autocomplete').autocomplete({
      minLength: 2,
      source: function source(request, response) {
        var query;
        var term = request.term.trim();

        if (index_all/* default.isEmpty */.ZP.isEmpty(term)) {
          query = "/".concat(catalogue, "/documents");
        } else {
          query = "/".concat(catalogue, "/documents?term=").concat(request.term);
        }

        return jquery_default().getJSON(query, function (data) {
          return response(index_all/* default.map */.ZP.map(data.results, function (d) {
            return {
              value: d.title,
              label: d.title,
              identifier: d.identifier,
              type: d.resourceType
            };
          }));
        });
      }
    });
    return this.$('.autocomplete').on('autocompleteselect', function (event, ui) {
      _this.model.set('identifier', ui.item.identifier);

      _this.$('.identifier').val(ui.item.identifier);

      _this.model.set('href', 'https://catalogue.ceh.ac.uk/id/' + ui.item.identifier);

      _this.$('.href').val('https://catalogue.ceh.ac.uk/id/' + ui.item.identifier);

      _this.model.set('associationType', ui.item.type);

      _this.$('.associationType').val(ui.item.type);

      _this.model.set('title', ui.item.label);

      return _this.$('.title').val(ui.item.label);
    });
  },
  render: function render() {
    ObjectInputView.prototype.render.apply(this);
    this.$('select.rel').val(this.model.get('rel'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/KeywordVocabulary.tpl
// Module
var KeywordVocabulary_code = "<div class=\"row\"> <div class=\"col-sm-12\"> <input class=\"form-control input-sm autocomplete\" placeholder=\"Search for keywords in selected vocabularies\"> </div> </div> <div class=\"row vocabularyPicker\"></div> <div class=\"row\"> <div class=\"col-sm-offset-1 col-sm-1\"> <label class=\"control-label\">Value</label> </div> <div class=\"col-sm-5\"> <input data-name=\"value\" class=\"editor-input value\" value=\"<%= data.value %>\"> </div> <div class=\"col-sm-1\"> <label>Uri</label> </div> <div class=\"col-sm-4\"> <input data-name=\"uri\" class=\"editor-input uri\" value=\"<%= data.uri %>\"> </div> </div> <hr> ";
// Exports
/* harmony default export */ const KeywordVocabulary = (KeywordVocabulary_code);
;// CONCATENATED MODULE: ./editor/src/templates/KeywordCheckbox.tpl
// Module
var KeywordCheckbox_code = "<label> <input type=\"checkbox\" <% if (toSearch) { %> checked=\"checked\" <% } %>> <%= name %> </label> ";
// Exports
/* harmony default export */ const KeywordCheckbox = (KeywordCheckbox_code);
;// CONCATENATED MODULE: ./editor/src/views/KeywordCheckboxView.js



/* harmony default export */ const KeywordCheckboxView = (backbone_default().View.extend({
  className: 'col-sm-3',
  events: {
    'change input': 'select'
  },
  initialize: function initialize() {
    this.template = index_all/* default.template */.ZP.template(KeywordCheckbox);
    this.listenTo(this.model, 'sync', this.remove);
    return this.listenTo(this.model, 'change', this.render);
  },
  select: function select() {
    var previous = this.model.get('toSearch');
    return this.model.set('toSearch', !previous);
  },
  render: function render() {
    this.$el.html(this.template(this.model.attributes));
    return this;
  }
}));
// EXTERNAL MODULE: ./node_modules/jquery-ui/ui/widgets/autocomplete.js
var autocomplete = __webpack_require__(2993);
;// CONCATENATED MODULE: ./editor/src/views/KeywordVocabularyView.js







/* harmony default export */ const KeywordVocabularyView = (ObjectInputView.extend({
  initialize: function initialize() {
    var _this = this;

    this.template = index_all/* default.template */.ZP.template(KeywordVocabulary);
    ObjectInputView.prototype.initialize.apply(this);
    this.vocabularies = new (backbone_default()).Collection();
    var catalogue = jquery_default()('html').data('catalogue');
    this.$vocabularies = this.$('.vocabularyPicker');
    this.listenTo(this.vocabularies, 'add', this.addOne);
    this.listenTo(this.vocabularies, 'reset', this.addAll);
    jquery_default().getJSON("/catalogues/".concat(catalogue), function (data) {
      return _this.vocabularies.reset(data.vocabularies);
    });
    this.$('.autocomplete').autocomplete({
      minLength: 2,
      source: function source(request, response) {
        var query;

        var vocab = index_all/* default.pluck */.ZP.pluck(_this.vocabularies.where({
          toSearch: true
        }), 'id');

        var term = request.term.trim();

        if (index_all/* default.isEmpty */.ZP.isEmpty(term)) {
          query = "/vocabulary/keywords?vocab=".concat(vocab);
        } else {
          query = "/vocabulary/keywords?query=".concat(request.term, "&vocab=").concat(vocab);
        }

        return jquery_default().getJSON(query, function (data) {
          return response(index_all/* default.map */.ZP.map(data, function (d) {
            return {
              value: d.label,
              label: "".concat(d.label, " (").concat(d.vocabId, ")"),
              url: d.url
            };
          }));
        });
      }
    });
    return this.$('.autocomplete').on('autocompleteselect', function (event, ui) {
      _this.model.set('value', ui.item.value);

      _this.$('.value').val(ui.item.value);

      _this.model.set('uri', ui.item.url);

      return _this.$('.uri').val(ui.item.url);
    });
  },
  addAll: function addAll() {
    return this.vocabularies.each(this.addOne, this);
  },
  addOne: function addOne(vocabulary) {
    vocabulary.set({
      toSearch: true
    });
    var view = new KeywordCheckboxView({
      model: vocabulary
    });
    return this.$vocabularies.append(view.render().el);
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/DescriptiveKeyword.tpl
// Module
var DescriptiveKeyword_code = "<div class=\"row\"> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"descriptiveKeyword<%= data.index %>type\">Type</label> </div> <div class=\"col-sm-11\"> <select data-name=\"type\" class=\"editor-input type\" id=\"descriptiveKeyword<%= data.index %>type\"> <option value=\"\" selected=\"selected\">- Select Type -</option> <option value=\"Catalogue topic\">Catalogue topic </option><option value=\"dataCentre\">Data Centre</option> <option value=\"discipline\">Discipline </option><option value=\"instrument\">Instrument</option> <option value=\"place\">Place </option><option value=\"project\">Project</option> <option value=\"stratum\">Stratum </option><option value=\"taxon\">Taxon</option> <option value=\"temporal\">Temporal </option><option value=\"theme\">Theme </option></select> </div> </div> <div class=\"keywords row col-sm-11 col-sm-offset-1\"></div> <div class=\"row\"> <div class=\"col-sm-11 col-sm-offset-1\"> <div class=\"keyword-add hidden\" id=\"catalogueTopic\"> <button class=\"editor-button dropdown-toggle\" type=\"button\" id=\"dropdownCehTopicMenu\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"true\"> Choose Catalogue topics <span class=\"caret\"></span> </button> <ul class=\"dropdown-menu predefined\" aria-labelledby=\"dropdownCehTopicMenu\"> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/1\">Agriculture</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/20\">Animal behaviour</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/2\">Biodiversity</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/4\">Climate and climate change</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/6\">Ecosystem services</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/5\">Environmental risk</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/7\">Environmental survey</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/19\">Evolutionary ecology</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/9\">Hydrology</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/10\">Invasive species</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/11\">Land cover</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/12\">Land use</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/18\">Mapping</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/13\">Modelling</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/3\">Phenology</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/14\">Pollinators</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/15\">Pollution</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/8\">Radioecology</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/17\">Soil</a></li> <li><a href=\"http://onto.nerc.ac.uk/CEHMD/topic/16\">Water quality</a></li> </ul> </div> <div class=\"keyword-add\"> <button class=\"editor-button add\">Add <span class=\"fas fa-plus\" aria-hidden=\"true\"></span></button> </div> </div> </div>";
// Exports
/* harmony default export */ const templates_DescriptiveKeyword = (DescriptiveKeyword_code);
;// CONCATENATED MODULE: ./editor/src/views/DescriptiveKeywordView.js






/* harmony default export */ const DescriptiveKeywordView = (ObjectInputView.extend({
  template: templates_DescriptiveKeyword,
  events: function events() {
    return index_all/* default.extend */.ZP.extend({}, ObjectInputView.prototype.events, {
      'click .add': function clickAdd() {
        return this.add();
      },
      'click .predefined': function clickPredefined(event) {
        return this.addPredefined(event);
      }
    });
  },
  initialize: function initialize(options) {
    this.template = index_all/* default.template */.ZP.template(templates_DescriptiveKeyword);
    ObjectInputView.prototype.initialize.call(this, options);
    this.render();
    var keywordType = this.model.get('type');

    if (keywordType != null) {
      // IE only supports .startsWith() in MS Edge (> version 11)
      if (keywordType.lastIndexOf('Catalogue topic', 0) === 0) {
        this.$('#catalogueTopic').removeClass('hidden');
        this.$('.add').addClass('hidden');
        this.$('select.type').attr('disabled', 'disabled');
      }
    }

    this.keywords = this.model.getRelatedCollection('keywords');
    this.createList(this.keywords, '.keywords', this.addOne);
    return this.$('input.date').datepicker({
      dateFormat: 'yy-mm-dd'
    });
  },
  render: function render() {
    ObjectInputView.prototype.render.apply(this);
    this.$('.type').val(this.model.get('type'));
    return this;
  },
  addOne: function addOne(model, keywordIndex) {
    this.data = index_all/* default.omit */.ZP.omit(this.data, 'el');
    return new ChildView(index_all/* default.extend */.ZP.extend({}, this.data, {
      model: model,
      keywordIndex: keywordIndex,
      ObjectInputView: KeywordVocabularyView
    }));
  },
  add: function add() {
    return this.keywords.add({});
  },
  addPredefined: function addPredefined(event) {
    event.preventDefault();
    var $target = this.$(event.target);
    return this.keywords.add({
      value: $target.text(),
      uri: $target.attr('href')
    });
  },
  modify: function modify(event) {
    var $target = jquery_default()(event.target);
    var name = $target.data('name');
    var value = $target.val();

    if (value) {
      return this.model.set(name, value);
    } else {
      return this.model.unset(name);
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/TopicCategory.tpl
// Module
var TopicCategory_code = "<select data-name=\"value\" class=\"editor-input\"> <option value=\"\" selected=\"selected\">- Add Topic Category -</option> <option value=\"biota\">Biota&nbsp;&nbsp;&nbsp;&nbsp;(Flora and/or fauna in natural environment)</option> <option value=\"boundaries\">Boundaries&nbsp;&nbsp;&nbsp;&nbsp;(Legal land descriptions)</option> <option value=\"climatologyMeteorologyAtmosphere\">Climatology / Meteorology / Atmosphere&nbsp;&nbsp;&nbsp;&nbsp;(Processes and phenomena of the atmosphere)</option> <option value=\"economy\">Economy&nbsp;&nbsp;&nbsp;&nbsp;(Economic activities, conditions and employment)</option> <option value=\"elevation\">Elevation&nbsp;&nbsp;&nbsp;&nbsp;(Height above or below sea level)</option> <option value=\"environment\">Environment&nbsp;&nbsp;&nbsp;&nbsp;(Environmental resources, protection and conservation)</option> <option value=\"farming\">Farming&nbsp;&nbsp;&nbsp;&nbsp;(Rearing of animals and/or cultivation of plants)</option> <option value=\"geoscientificInformation\">Geoscientific Information&nbsp;&nbsp;&nbsp;&nbsp;(Information pertaining to earth sciences)</option> <option value=\"health\">Health&nbsp;&nbsp;&nbsp;&nbsp;(Health, health services, human ecology, and safety)</option> <option value=\"imageryBaseMapsEarthCover\">Imagery / Base Maps / Earth Cover&nbsp;&nbsp;&nbsp;&nbsp;(Base maps)</option> <option value=\"inlandWaters\">Inland Waters&nbsp;&nbsp;&nbsp;&nbsp;(Inland water features, drainage systems and their characteristics)</option> <option value=\"intelligenceMilitary\">Intelligence / Military&nbsp;&nbsp;&nbsp;&nbsp;(Military bases, structures, activities)</option> <option value=\"location\">Location&nbsp;&nbsp;&nbsp;&nbsp;(Positional information and services)</option> <option value=\"oceans\">Oceans&nbsp;&nbsp;&nbsp;&nbsp;(Features and characteristics of salt water bodies (excluding inland waters))</option> <option value=\"planningCadastre\">Planning / Cadastre&nbsp;&nbsp;&nbsp;&nbsp;(Information used for appropriate actions for future use of the land)</option> <option value=\"society\">Society&nbsp;&nbsp;&nbsp;&nbsp;(Characteristics of society and cultures)</option> <option value=\"structure\">Structure&nbsp;&nbsp;&nbsp;&nbsp;(Man-made construction)</option> <option value=\"transportation\">Transportation&nbsp;&nbsp;&nbsp;&nbsp;(Means and aids for conveying persons and/or goods)</option> <option value=\"utilitiesCommunication\">Utilities / Communication&nbsp;&nbsp;&nbsp;&nbsp;(Energy, water and waste systems and communications infrastructure and services)</option> </select> ";
// Exports
/* harmony default export */ const templates_TopicCategory = (TopicCategory_code);
;// CONCATENATED MODULE: ./editor/src/views/TopicCategoryView.js



/* harmony default export */ const TopicCategoryView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(templates_TopicCategory);
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('value'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/TemporalExtent.tpl
// Module
var TemporalExtent_code = "<div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label for=\"temporalExtent<%= data.index %>Begin\">Begin</label> </div> <div class=\"col-sm-4 col-lg-4\"> <input data-name=\"begin\" id=\"temporalExtent<%= data.index %>Begin\" autocomplete=\"off\" class=\"editor-input\" value=\"<%= data.begin %>\"> </div> <div class=\"col-sm-1 col-lg-1\"> <label for=\"temporalExtent<%= data.index %>End\">End</label> </div> <div class=\"col-sm-4 col-lg-4\"> <input data-name=\"end\" id=\"temporalExtent<%= data.index %>End\" autocomplete=\"off\" class=\"editor-input\" value=\"<%= data.end %>\"> </div> </div> ";
// Exports
/* harmony default export */ const TemporalExtent = (TemporalExtent_code);
;// CONCATENATED MODULE: ./editor/src/views/TemporalExtentView.js



/* harmony default export */ const TemporalExtentView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(TemporalExtent);
    ObjectInputView.prototype.render.apply(this);
    this.$('input').datepicker({
      dateFormat: 'yy-mm-dd'
    });
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/DatasetReferenceDate.tpl
// Module
var DatasetReferenceDate_code = " <div class=\"col-md-1\"> <label for=\"input-creationDate\">Created</label><br> </div> <div class=\"col-md-2\"> <input data-name=\"creationDate\" id=\"input-creationDate\" class=\"editor-input\" autocomplete=\"off\" value=\"<%= data.creationDate %>\"> </div> <div class=\"col-md-1\"> <label for=\"input-publicationDate\">Published</label><br> </div> <div class=\"col-md-2\"> <input data-name=\"publicationDate\" id=\"input-publicationDate\" class=\"editor-input\" autocomplete=\"off\" value=\"<%= data.publicationDate %>\"> </div> <div class=\"col-md-1\"> <label for=\"input-releasedDate\">Release(d)</label><br> </div> <div class=\"col-md-2\"> <input data-name=\"releasedDate\" id=\"input-releasedDate\" class=\"editor-input\" autocomplete=\"off\" value=\"<%= data.releasedDate %>\"> </div> <div class=\"col-md-1\"> <label for=\"input-supersededDate\">Superseded</label><br> </div> <div class=\"col-md-2\"> <input data-name=\"supersededDate\" id=\"input-supersededDate\" class=\"editor-input\" autocomplete=\"off\" value=\"<%= data.supersededDate %>\"> </div> ";
// Exports
/* harmony default export */ const DatasetReferenceDate = (DatasetReferenceDate_code);
// EXTERNAL MODULE: ./node_modules/jquery-ui/ui/widgets/datepicker.js
var datepicker = __webpack_require__(4414);
;// CONCATENATED MODULE: ./editor/src/views/DatasetReferenceDateView.js




/* harmony default export */ const DatasetReferenceDateView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(DatasetReferenceDate);
    ObjectInputView.prototype.render.apply(this);
    this.$('input').datepicker({
      dateFormat: 'yy-mm-dd'
    });
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/AccessLimitation.tpl
// Module
var AccessLimitation_code = "<select data-name=\"value\" class=\"editor-input\" id=\"input-accessLimitation\" <%= data.disabled%>> <option value=\"\"> -- Choose an option -- </option> <optgroup label=\"Available\"> <option value=\"Registration is required to access this data\">FREELY AVAILABLE - but USERS MUST LOG IN to access it</option> <option value=\"no limitations to public access\">FREELY AVAILABLE and there is NO NEED TO LOG IN to access it (no limitations)</option> </optgroup> <optgroup label=\"Controlled\"> <option value=\"To access this data, a licence needs to be negotiated with the provider and there may be a cost\">CONTROLLED - To access this data, a bespoke licence needs to be negotiated and there may be a cost</option> </optgroup> <optgroup label=\"Unavailable\"> <option value=\"embargoed\">EMBARGOED - This resource is not yet available but a date has been set for its release</option> <option value=\"in-progress\">IN PROGRESS - This resource is not yet available as is still being completed</option> <option value=\"superseded\">SUPERSEDED - This resource has been withdrawn and has been replaced by an updated version</option> <option value=\"withdrawn\">WITHDRAWN - This resource has been withdrawn but has not been replaced</option> </optgroup> <optgroup label=\"Restricted\"> <option value=\"public access limited according to Article 13(1)(h) of the INSPIRE Directive\">ACCESS RESTRICTED as release would adversely affect the protection of the environment (e.g. the location of rare species)</option> <option value=\"public access limited according to Article 13(1)(f) of the INSPIRE Directive\">ACCESS RESTRICTED as it contains personal information</option> <option value=\"public access limited according to Article 13(1)(d) of the INSPIRE Directive\">ACCESS RESTRICTED for reasons of commercial confidentiality</option> <option value=\"public access limited according to Article 13(1)(e) of the INSPIRE Directive\">ACCESS RESTRICTED as release would adversely affect intellectual property rights</option> </optgroup> </select> ";
// Exports
/* harmony default export */ const templates_AccessLimitation = (AccessLimitation_code);
;// CONCATENATED MODULE: ./editor/src/views/AccessLimitationView.js



/* harmony default export */ const AccessLimitationView = (ObjectInputView.extend({
  initialize: function initialize() {
    this.template = index_all/* default.template */.ZP.template(templates_AccessLimitation);
    ObjectInputView.prototype.initialize.apply(this);
    this.listenTo(this.model, 'change:accessLimitation', function (model, value) {
      this.model.set('type', value.value);
    });
  },
  render: function render() {
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('value'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/ResourceType.tpl
// Module
var ResourceType_code = "<select data-name=\"value\" class=\"editor-input\" id=\"input-resourceType\"> <option value=\"\" selected=\"selected\">- Select Resource Type -</option> <optgroup label=\"EIDC resource types\" class=\"resourcetypes-eidc\"> <option value=\"dataset\">Dataset</option> <option value=\"nonGeographicDataset\">Dataset (non-geographic)</option> <option value=\"aggregate\">Data collection (aggregation)</option> <option value=\"application\">Model code (application)</option> <option value=\"signpost\">Signpost</option> <option value=\"service\">Web service</option> </optgroup> <optgroup label=\"INMS resource types\" class=\"resourcetypes-inms\"> <option value=\"dataset\">Dataset</option> <option value=\"thirdPartyDataset\">Third-party dataset</option> <option value=\"nonGeographicDataset\">Dataset (non-geographic)</option> <option value=\"aggregate\">Data collection (aggregation)</option> <option value=\"service\">Map (web service)</option> </optgroup> <optgroup label=\"ASSIST resource types\" class=\"resourcetypes-assist\"> <option value=\"dataset\">Dataset</option> <option value=\"thirdPartyDataset\">Third-party dataset</option> <option value=\"aggregate\">Data collection (aggregation)</option> </optgroup> <optgroup label=\"eLTER resource types\" class=\"resourcetypes-elter\"> <option value=\"dataset\">Dataset</option> <option value=\"nonGeographicDataset\">Dataset (non-geographic)</option> <option value=\"aggregate\">Data collection (aggregation)</option> <option value=\"application\">Model code (application)</option> <option value=\"signpost\">Signpost</option> <option value=\"service\">Web service</option> </optgroup> <optgroup label=\"All ISO 19115 resource types\" class=\"resourcetypes-iso19115\"> <option value=\"aggregate\">Aggregate</option> <option value=\"application\">Application</option> <option value=\"attribute\">Attribute</option> <option value=\"attributeType\">Attribute type</option> <option value=\"collection\">Collection</option> <option value=\"collectionHardware\">Collection hardware</option> <option value=\"collectionSession\">Collection session</option> <option value=\"coverage\">Coverage</option> <option value=\"dataset\">Dataset</option> <option value=\"dimensionGroup\">Dimension group</option> <option value=\"document\">Document</option> <option value=\"feature\">Feature</option> <option value=\"featureType\">Feature type</option> <option value=\"fieldSession\">Field session</option> <option value=\"initiative\">Initiative</option> <option value=\"metadata\">Metadata</option> <option value=\"model\">Model</option> <option value=\"nonGeographicDataset\">Non-geographic dataset</option> <option value=\"product\">Product</option> <option value=\"propertyType\">Property type</option> <option value=\"repository\">Repository</option> <option value=\"series\">Series</option> <option value=\"service\">Service</option> <option value=\"sample\">Sample</option> <option value=\"software\">Software</option> <option value=\"tile\">Tile</option> </optgroup> </select> ";
// Exports
/* harmony default export */ const templates_ResourceType = (ResourceType_code);
;// CONCATENATED MODULE: ./editor/src/views/ResourceTypeView.js



/* harmony default export */ const ResourceTypeView = (ObjectInputView.extend({
  initialize: function initialize() {
    this.template = index_all/* default.template */.ZP.template(templates_ResourceType);
    ObjectInputView.prototype.initialize.apply(this);
    this.listenTo(this.model, 'change:resourceType', function (model, value) {
      this.model.set('type', value.value);
    });
  },
  render: function render() {
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('value'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/DeimsSite.tpl
// Module
var DeimsSite_code = "<div class=\"row\"> <div class=\"col-sm-1\"> <label>Search</label> </div> <div class=\"col-sm-11 catalogueSearch\"> <input data-name=\"title\" value=\"<%= data.title %>\" id=\"deims<%= data.index %>Name\" class=\"form-control autocomplete\" placeholder=\"Search for DEIMS site...\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-sm-offset-1\"> <label>Name</label> </div> <div class=\"col-sm-9\"> <input data-name=\"title\" id=\"deims<%= data.index %>Title\" class=\"editor-input title\" value=\"<%= data.title %>\" autocomplete=\"off\" disabled=\"true\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-sm-offset-1\"> <label>Url</label> </div> <div class=\"col-sm-9\"> <input data-name=\"url\" id=\"deims<%= data.index %>Url\" class=\"editor-input url\" value=\"<%= data.url %>\" autocomplete=\"off\" disabled=\"true\"> </div> </div> <div class=\"hidden\"> <input data-name=\"id\" id=\"deims<%= data.index %>Id\" class=\"editor-input id\" value=\"<%= data.id %>\"> </div> ";
// Exports
/* harmony default export */ const DeimsSite = (DeimsSite_code);
;// CONCATENATED MODULE: ./editor/src/views/DeimsSiteView.js




/* harmony default export */ const DeimsSiteView = (ObjectInputView.extend({
  initialize: function initialize() {
    var _this = this;

    this.template = index_all/* default.template */.ZP.template(DeimsSite);
    ObjectInputView.prototype.initialize.apply(this);
    this.$('.autocomplete').autocomplete({
      minLength: 2,
      source: function source(request, response) {
        var query;
        var term = request.term.trim();

        if (index_all/* default.isEmpty */.ZP.isEmpty(term)) {
          query = '/vocabulary/deims';
        } else {
          query = "/vocabulary/deims?query=".concat(request.term);
        }

        return jquery_default().getJSON(query, function (data) {
          return response(index_all/* default.map */.ZP.map(data, function (d) {
            return {
              value: d.title,
              label: d.title,
              id: d.id,
              url: d.url
            };
          }));
        });
      }
    });
    return this.$('.autocomplete').on('autocompleteselect', function (event, ui) {
      _this.model.set('id', ui.item.id);

      _this.$('.id').val(ui.item.id);

      _this.model.set('title', ui.item.label);

      _this.$('.title').val(ui.item.label);

      _this.model.set('url', ui.item.url);

      return _this.$('.url').val(ui.item.url);
    });
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/ReadOnly.tpl
// Module
var ReadOnly_code = "<span data-name=\"<%= data.modelAttribute %>\" id=\"input-<%= data.modelAttribute %>\" class=\"readonly form-control-static\"><%= data.value %> </span>";
// Exports
/* harmony default export */ const ReadOnly = (ReadOnly_code);
;// CONCATENATED MODULE: ./editor/src/views/ReadOnlyView.js




/* harmony default export */ const ReadOnlyView = (SingleView/* default.extend */.Z.extend({
  events: {
    change: 'modify'
  },
  initialize: function initialize(options) {
    this.template = index_all/* default.template */.ZP.template(ReadOnly);
    SingleView/* default.prototype.initialize.call */.Z.prototype.initialize.call(this, options);
    this.render();
    return this.listenTo(this.model, "change:".concat(this.data.modelAttribute), this.render);
  },
  render: function render() {
    SingleView/* default.prototype.render.apply */.Z.prototype.render.apply(this);
    return this.$('.dataentry').append(this.template({
      data: index_all/* default.extend */.ZP.extend({}, this.data, {
        value: this.model.get(this.data.modelAttribute)
      })
    }));
  },
  modify: function modify(event) {
    var $target = jquery_default()(event.target);
    var name = $target.data('name');
    var value = $target.val();

    if (!value) {
      return this.model.unset(name);
    } else {
      return this.model.set(name, value);
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/InspireTheme.tpl
// Module
var InspireTheme_code = "<div class=\"row\"> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"inspireThemes<%= data.index %>Theme\">Theme</label> </div> <div class=\"col-sm-6\"> <select class=\"editor-input theme\" data-name=\"theme\" id=\"inspireThemes<%= data.index %>Theme\" class=\"editor-input\"> <option value=\"\" selected=\"selected\">Choose a theme</option> <option value=\"Addresses\">Addresses</option> <option value=\"Administrative Units\">Administrative Units</option> <option value=\"Agricultural and Aquaculture Facilities\">Agricultural and Aquaculture Facilities</option> <option value=\"Area Management Restriction Regulation Zones and Reporting units\">Area Management Restriction Regulation Zones and Reporting units</option> <option value=\"Atmospheric Conditions\">Atmospheric Conditions</option> <option value=\"Bio-geographical Regions\">Bio-geographical Regions</option> <option value=\"Buildings\">Buildings</option> <option value=\"Cadastral Parcels\">Cadastral Parcels</option> <option value=\"Coordinate reference systems\">Coordinate reference systems</option> <option value=\"Elevation\">Elevation</option> <option value=\"Energy Resources\">Energy Resources</option> <option value=\"Environmental Monitoring Facilities\">Environmental Monitoring Facilities</option> <option value=\"Geographical grid systems\">Geographical grid systems</option> <option value=\"Geographical Names\">Geographical Names</option> <option value=\"Geology\">Geology</option> <option value=\"Habitats and Biotopes\">Habitats and Biotopes</option> <option value=\"Human Health and Safety\">Human Health and Safety</option> <option value=\"Hydrography\">Hydrography</option> <option value=\"Land Cover\">Land Cover</option> <option value=\"Land Use\">Land Use</option> <option value=\"Meteorological geographical features\">Meteorological geographical features</option> <option value=\"Mineral Resources\">Mineral Resources</option> <option value=\"Natural Risk Zones\">Natural Risk Zones</option> <option value=\"Oceanographic Geographical Features\">Oceanographic Geographical Features</option> <option value=\"Orthoimagery\">Orthoimagery</option> <option value=\"Population Distribution - Demography\">Population Distribution - Demography</option> <option value=\"Production and Industrial Facilities\">Production and Industrial Facilities</option> <option value=\"Protected Sites\">Protected Sites</option> <option value=\"Sea Regions\">Sea Regions</option> <option value=\"Soil\">Soil</option> <option value=\"Species Distribution\">Species Distribution</option> <option value=\"Statistical Units\">Statistical Units</option> <option value=\"Transport Networks\">Transport Networks</option> <option value=\"Utility and Governmental Services\">Utility and Governmental Services</option> </select> </div> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"inspireThemes<%= data.index %>Conformity\">Conformity</label> </div> <div class=\"col-sm-3\"> <select class=\"editor-input conformity\" data-name=\"conformity\" id=\"inspireThemes<%= data.index %>Conformity\" class=\"editor-input\"> <option value=\"\" selected=\"selected\">Not evaluated</option> <option value=\"Conformant\">Conformant</option> <option value=\"Not conformant\">Not conformant</option> </select> </div> </div>";
// Exports
/* harmony default export */ const templates_InspireTheme = (InspireTheme_code);
;// CONCATENATED MODULE: ./editor/src/views/InspireThemeView.js



/* harmony default export */ const InspireThemeView = (ObjectInputView.extend({
  initialize: function initialize() {
    this.template = index_all/* default.template */.ZP.template(templates_InspireTheme);
  },
  render: function render() {
    ObjectInputView.prototype.render.apply(this);
    this.$('select.theme').val(this.model.get('theme'));
    this.$('select.conformity').val(this.model.get('conformity'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/Checkbox.tpl
// Module
var Checkbox_code = "<input data-name=\"<%= data.modelAttribute %>\" id=\"input-<%= data.modelAttribute %>\" value=\"<%= data.value %>\" type=\"checkbox\">";
// Exports
/* harmony default export */ const Checkbox = (Checkbox_code);
;// CONCATENATED MODULE: ./editor/src/views/CheckboxView.js




/* harmony default export */ const CheckboxView = (InputView/* default.extend */.Z.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(Checkbox);
    InputView/* default.prototype.render.apply */.Z.prototype.render.apply(this);
    this.$('[type="checkbox"]').prop('checked', this.model.get(this.data.modelAttribute));
    return this;
  },
  modify: function modify(event) {
    var $target = jquery_default()(event.target);
    var name = $target.data('name');
    var value = $target.prop('checked');
    return this.model.set(name, value);
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/OnlineResource.tpl
// Module
var OnlineResource_code = "<div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"onlineResource<%= data.index %>Name\">Name</label> </div> <div class=\"col-sm-6\"> <input data-name=\"name\" id=\"onlineResource<%= data.index %>Name\" class=\"editor-input\" value=\"<%= data.name %>\" <%= data.disabled%>> </div> <div class=\"col-sm-1\"> <label for=\"onlineResource<%= data.index %>Function\">Function</label> </div> <div class=\"col-sm-3\"> <select data-name=\"function\" id=\"onlineResource<%= data.index %>Function\" class=\"editor-input\" <%= data.disabled%>> <option value=\"\" selected=\"selected\">- Select Function -</option> <option value=\"browseGraphic\">Browse Graphic</option> <option value=\"download\">Download</option> <option value=\"information\">Information</option> <option value=\"offlineAccess\">Offline Access</option> <option value=\"order\">Order</option> <option value=\"search\">Search</option> </select> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"onlineResource<%= data.index %>Description\">Description</label> </div> <div class=\"col-sm-10 col-lg-10\"> <textarea rows=\"3\" data-name=\"description\" id=\"onlineResource<%= data.index %>Description\" class=\"editor-textarea\" <%= data.disabled%>><%= data.description %></textarea> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"onlineResource<%= data.index %>Url\">URL</label> </div> <div class=\"col-sm-10\"> <input data-name=\"url\" id=\"onlineResource<%= data.index %>Url\" class=\"editor-input\" value=\"<%= data.url %>\" <%= data.disabled%>> </div> </div> ";
// Exports
/* harmony default export */ const templates_OnlineResource = (OnlineResource_code);
;// CONCATENATED MODULE: ./editor/src/views/OnlineResourceView.js



/* harmony default export */ const OnlineResourceView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(templates_OnlineResource);
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('function'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/DistributionFormat.tpl
// Module
var DistributionFormat_code = "<div class=\"row\"> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"distributionFormat<%= data.index %>Name\">Name</label> </div> <div class=\"col-sm-4\"> <input data-name=\"name\" class=\"editor-input\" id=\"distributionFormat<%= data.index %>Name\" value=\"<%= data.name %>\"> </div> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"distributionFormat<%= data.index %>Type\">Type</label> </div> <div class=\"col-sm-3\"> <input data-name=\"type\" class=\"editor-input\" id=\"distributionFormat<%= data.index %>Type\" value=\"<%= data.type %>\"> </div> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"distributionFormat<%= data.index %>Version\">Version</label> </div> <div class=\"col-sm-2\"> <input data-name=\"version\" class=\"editor-input\" id=\"distributionFormat<%= data.index %>Version\" value=\"<%= data.version %>\"> </div> </div>";
// Exports
/* harmony default export */ const templates_DistributionFormat = (DistributionFormat_code);
;// CONCATENATED MODULE: ./editor/src/views/DistributionFormatView.js



/* harmony default export */ const DistributionFormatView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(templates_DistributionFormat)
}));
;// CONCATENATED MODULE: ./editor/src/templates/ResourceConstraint.tpl
// Module
var ResourceConstraint_code = "<div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Code\">Code</label> </div> <div class=\"col-sm-11 col-lg-11\"> <select data-name=\"code\" class=\"editor-input\" id=\"<%= data.modelAttribute %><%= data.index %>Code\"> <option value=\"\" selected=\"selected\">- Select Type -</option> <option value=\"copyright\">Copyright</option> <option value=\"intellectualPropertyRights\">Intellectual Property Rights</option> <option value=\"license\">License</option> <option value=\"otherRestrictions\">Other Restrictions</option> <option value=\"patent\">Patent</option> <option value=\"patentPending\">Patent Pending</option> <option value=\"restricted\">Restricted</option> <option value=\"trademark\">Trademark</option> </select> </div> </div> <div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Value\">Text</label> </div> <div class=\"col-sm-11 col-lg-11\"> <input data-name=\"value\" id=\"<%= data.modelAttribute %><%= data.index %>Value\" class=\"editor-input\" value=\"<%= data.value %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Uri\">URL</label> </div> <div class=\"col-sm-11 col-lg-11\"> <input data-name=\"uri\" id=\"<%= data.modelAttribute %><%= data.index %>Uri\" class=\"editor-input\" value=\"<%= data.uri %>\"> </div> </div>";
// Exports
/* harmony default export */ const ResourceConstraint = (ResourceConstraint_code);
;// CONCATENATED MODULE: ./editor/src/views/ResourceConstraintView.js



/* harmony default export */ const ResourceConstraintView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(ResourceConstraint);
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('code'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/ResourceIdentifier.tpl
// Module
var ResourceIdentifier_code = "<div class=\"col-sm-1 col-lg-1\"> <label for=\"resourceIdentifier<%= data.index %>Code\">Code</label> </div> <div class=\"col-sm-5 col-lg-5\"> <input data-name=\"code\" id=\"resourceIdentifier<%= data.index %>Code\" class=\"editor-input\" value=\"<%= data.code %>\" <%= data.disabled%>> </div> <div class=\"col-sm-2 col-lg-2\"> <label for=\"resourceIdentifier<%= data.index %>CodeSpace\">Codespace</label> </div> <div class=\"col-sm-2 col-lg-2\"> <input data-name=\"codeSpace\" id=\"resourceIdentifier<%= data.index %>CodeSpace\" class=\"editor-input\" value=\"<%= data.codeSpace %>\" <%= data.disabled%>> </div> <div class=\"col-sm-1 col-lg-1\"> <label for=\"resourceIdentifier<%= data.index %>Version\">Version</label> </div> <div class=\"col-sm-1 col-lg-1\"> <input data-name=\"version\" id=\"resourceIdentifier<%= data.index %>Version\" class=\"editor-input\" value=\"<%= data.version %>\" <%= data.disabled%>> </div>";
// Exports
/* harmony default export */ const ResourceIdentifier = (ResourceIdentifier_code);
;// CONCATENATED MODULE: ./editor/src/views/ResourceIdentifierView.js



/* harmony default export */ const ResourceIdentifierView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(ResourceIdentifier)
}));
;// CONCATENATED MODULE: ./editor/src/templates/SpatialReferenceSystem.tpl
// Module
var SpatialReferenceSystem_code = "<div class=\"col-sm-2\"> <label for=\"spatialReferenceSystem<%= data.index %>Title\">Title</label> </div> <div class=\"col-sm-4\"> <input data-name=\"title\" id=\"spatialReferenceSystem<%= data.index %>Title\" class=\"editor-input\" value=\"<%= data.title %>\"> </div> <div class=\"col-sm-2\"> <label for=\"spatialReferenceSystem<%= data.index %>Code\">Code</label> </div> <div class=\"col-sm-4\"> <input data-name=\"code\" id=\"spatialReferenceSystem<%= data.index %>Code\" class=\"editor-input\" value=\"<%= data.code %>\"> </div> ";
// Exports
/* harmony default export */ const SpatialReferenceSystem = (SpatialReferenceSystem_code);
;// CONCATENATED MODULE: ./editor/src/views/SpatialReferenceSystemView.js



/* harmony default export */ const SpatialReferenceSystemView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(SpatialReferenceSystem)
}));
;// CONCATENATED MODULE: ./editor/src/templates/SpatialRepresentationType.tpl
// Module
var SpatialRepresentationType_code = "<div class=\"row\" id=\"input<%= data.modelAttribute %><%= data.index %>\"> <div class=\"col-sm-11 dataentry\"> <select data-index=\"<%= data.index %>\" class=\"editor-input\"> <option value=\"\">- Select Spatial Representation Type -</option> <option value=\"grid\">Raster (grid)</option> <option value=\"textTable\">Tabular data (e.g. a spreadsheet)</option> <option value=\"tin\">Triangular Irregular Network</option> <option value=\"vector\">Vector (e.g. Shape file)</option> </select> </div> <div class=\"col-sm-1\"> <button data-index=\"<%= data.index %>\" class=\"editor-button-xs remove\"><i class=\"fas fa-times\"></i></button> </div> </div> ";
// Exports
/* harmony default export */ const SpatialRepresentationType = (SpatialRepresentationType_code);
;// CONCATENATED MODULE: ./editor/src/views/SpatialRepresentationTypeView.js



/* harmony default export */ const SpatialRepresentationTypeView = (ParentStringView.extend({
  render: function render() {
    var _this = this;

    this.childTemplate = index_all/* default.template */.ZP.template(SpatialRepresentationType);
    ParentStringView.prototype.render.apply(this);
    return index_all/* default.each */.ZP.each(this.array, function (string, index) {
      return _this.$("#input".concat(_this.data.modelAttribute).concat(index, " select")).val(string);
    });
  }
}));
;// CONCATENATED MODULE: ./editor/src/views/SpatialResolutionView.js



/* harmony default export */ const SpatialResolutionView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(SpatialReferenceSystem)
}));
;// CONCATENATED MODULE: ./editor/src/templates/DataTypeSchema.tpl
// Module
var DataTypeSchema_code = "<div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"schema-name<%= data.index %>\">Field</label> </div> <div class=\"col-sm-10\"> <input data-name=\"name\" id=\"schema-name<%= data.index %>\" class=\"editor-input\" value=\"<%= data.name %>\" placeholder=\"name of field/column\"/> </div> </div> <div class=\"extended hidden\" id=\"schemaDetail<%= data.index %>\"> <div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"schema-title<%= data.index %>\">Title</label> </div> <div class=\"col-sm-10\"> <input data-name=\"title\" id=\"schema-title<%= data.index %>\" class=\"editor-input\" value=\"<%= data.title %>\" placeholder=\"A nicer human readable label for the field (optional)\"/> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"schema-description<%= data.index %>\">Description</label> </div> <div class=\"col-sm-10\"> <textarea data-name=\"description\" id=\"schema-description<%= data.index %>\" class=\"editor-textarea\" rows=\"3\"><%= data.description %></textarea> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-xs-12\"> <label for=\"schema-type<%= data.index %>\">Type</label> </div> <div class=\"col-sm-4 col-xs-12\"> <input list=\"typeList\" data-name=\"type\" id=\"schema-type<%= data.index %>\" class=\"editor-input\" value=\"<%= data.type %>\" placeholder=\"\"/> </div> <div class=\"col-sm-2 col-xs-12\"> <div class=\"hidden-xs text-right\"> <label for=\"schema-format<%= data.index %>\">Format</label> </div> <div class=\"visible-xs-inline\"> <label for=\"schema-format<%= data.index %>\">Format</label> </div> </div> <div class=\"col-sm-4 col-xs-12\"> <input list=\"formatList\" data-name=\"format\" id=\"schema-format<%= data.index %>\" class=\"editor-input\" value=\"<%= data.format %>\" placeholder=\"recommended for dates and times\"/> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"schema-units<%= data.index %>\">Units</label> </div> <div class=\"col-sm-10\"> <input data-name=\"units\" id=\"schema-units<%= data.index %>\" class=\"editor-input\" value=\"<%= data.units %>\"/> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"schema-minimum<%= data.index %>\">Minimum value</label> </div> <div class=\"col-sm-4\"> <input data-name=\"minimum\" type=\"number\" id=\"schema-minimum<%= data.index %>\" class=\"editor-input\" value=\"<%= data.constraints.minimum %>\"/> </div> <div class=\"col-sm-2\"> <label for=\"schema-maximum<%= data.index %>\">Maximum value</label> </div> <div class=\"col-sm-4\"> <input data-name=\"maximum\" type=\"number\" id=\"schema-maximum<%= data.index %>\" class=\"editor-input\" value=\"<%= data.constraints.maximum %>\"/> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"schema-minLength<%= data.index %>\">Minimum length</label> </div> <div class=\"col-sm-4\"> <input data-name=\"minLength\" type=\"number\" step=\"1\" id=\"schema-minLength<%= data.index %>\" class=\"editor-input\" value=\"<%= data.constraints.minLength %>\"/> </div> <div class=\"col-sm-2\"> <label for=\"schema-maxLength<%= data.index %>\">Maximum length</label> </div> <div class=\"col-sm-4\"> <input data-name=\"maxLength\" type=\"number\" step=\"1\" id=\"schema-maxLength<%= data.index %>\" class=\"editor-input\" value=\"<%= data.constraints.maxLength %>\"/> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label>Unique</label> </div> <div class=\"col-sm-10\"> <label class=\"radio-inline\"> <input type=\"radio\" data-name=\"unique\" name=\"schema-unique<%= data.index %>\" value=\"true\" <% if (data.constraints.unique == true) { %> checked=\"checked\" <% } %> /> Yes </label> <label class=\"radio-inline\"> <input type=\"radio\" data-name=\"unique\" name=\"schema-unique<%= data.index %>\" value=\"false\" <% if (data.constraints.unique == false) { %> checked=\"checked\" <% } %> />No </label> </div> </div> </div> <datalist id=\"typeList\"> <option value=\"boolean\">True or false</option> <option value=\"date\">Date (without time)</option> <option value=\"datetime\">Date AND time</option> <option value=\"number\">Decimal number </option> <option value=\"email\">Email address</option> <option value=\"geopoint\">Geographic point (e.g. lon, lat)</option> <option value=\"integer\">Integer</option> <option value=\"string\">Text string</option> <option value=\"time\">Time</option> <option value=\"uri\">URI such as a web address or urn</option> <option value=\"uuid\">UUID/GUID</option> <option value=\"year\">Four digit year</option> <option value=\"yearmonth\">Year and month (e.g. 2015-07)</option> </datalist> <datalist id=\"formatList\"> <option value=\"YYYY\">Four digit year e.g. 2018</option> <option value=\"YYYY-MM\">Year and month e.g. 2018-12</option> <option value=\"YYYY-MM-DD\">ISO date e.g. 2018-12-25</option> <option value=\"HH:MM:SS\">ISO time e.g. 13:30:25</option> </datalist>";
// Exports
/* harmony default export */ const templates_DataTypeSchema = (DataTypeSchema_code);
;// CONCATENATED MODULE: ./editor/src/views/DataTypeSchemaView.js




/* harmony default export */ const DataTypeSchemaView = (ObjectInputView.extend({
  initialize: function initialize() {
    this.template = index_all/* default.template */.ZP.template(templates_DataTypeSchema);
  },
  modify: function modify(event) {
    var $target = jquery_default()(event.target);
    var name = $target.data('name'); // CHECK THIS

    var value = $target.val();

    if (index_all/* default.contains */.ZP.contains(['maximum', 'minimum', 'maxLength', 'minLength', 'unique'], name)) {
      var constraints = index_all/* default.clone */.ZP.clone(this.model.get('constraints'));

      if (value) {
        constraints[name] = value;
        return this.model.set('constraints', constraints);
      } else {
        constraints = index_all/* default.omit */.ZP.omit(constraints, name);
        return this.model.set('constraints', constraints);
      }
    } else {
      if (value) {
        return this.model.set(name, value);
      } else {
        return this.model.unset(name);
      }
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/views/ChildLargeView.js



/* harmony default export */ const ChildLargeView = (backbone_default().View.extend({
  className: 'row',
  events: {
    'click button.remove': 'delete',
    'click button.showhide': 'showhide'
  },
  initialize: function initialize(options) {
    this.template = index_all/* default.template */.ZP.template(ChildLarge);
    this.data = options;
    this.listenTo(this.model, 'remove', function () {
      return this.remove();
    });
    this.listenTo(this.model, 'showhide', function () {
      return this.showhide();
    });
    this.index = this.model.collection.indexOf(this.model);
    this.render();
    var view = new this.data.ObjectInputView(index_all/* default.extend */.ZP.extend({}, this.data, {
      el: this.$('.dataentry'),
      model: this.model,
      index: this.index
    }));
  },
  render: function render() {
    this.$el.html(this.template({
      index: this.index,
      data: this.data
    }));
    return this;
  },
  "delete": function _delete() {
    return this.model.collection.remove(this.model);
  },
  showhide: function showhide() {
    if (this.$('.extended').hasClass('hidden')) {
      this.$('.extended').removeClass('hidden');
      this.$('.showhide span').removeClass('fa-chevron-down');
      return this.$('.showhide span').addClass('fa-chevron-up');
    } else {
      this.$('.extended').addClass('hidden');
      this.$('.showhide span').removeClass('fa-chevron-up');
      return this.$('.showhide span').addClass('fa-chevron-down');
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/views/ParentLargeView.js





/* harmony default export */ const ParentLargeView = (SingleView/* default.extend */.Z.extend({
  events: {
    'click button.add': 'add'
  },
  initialize: function initialize(options) {
    var _this = this;

    this.template = index_all/* default.template */.ZP.template(Parent);
    SingleView/* default.prototype.initialize.call */.Z.prototype.initialize.call(this, options);
    this.collection = new Positionable([], {
      model: this.data.ModelType
    });
    this.listenTo(this.collection, 'add', this.addOne);
    this.listenTo(this.collection, 'reset', this.addAll);
    this.listenTo(this.collection, 'add remove change position', this.updateModel);
    this.listenTo(this.model, 'sync', this.updateCollection);
    this.render();
    this.collection.reset(this.getModelData());

    if (this.data.multiline) {
      this.$el.addClass('multiline');
    }

    if (!(this.data.disabled === 'disabled')) {
      return this.$('.existing').sortable({
        start: function start(event, ui) {
          _this._oldPosition = ui.item.index();
        },
        update: function update(event, ui) {
          return _this.collection.position(_this._oldPosition, ui.item.index());
        }
      });
    }
  },
  render: function render() {
    this.$el.html(this.template({
      data: this.data
    }));
    return this;
  },
  addOne: function addOne(model) {
    var view = new ChildLargeView(index_all/* default.extend */.ZP.extend({}, this.data, {
      model: model
    }));
    return this.$('.existing').append(view.el);
  },
  addAll: function addAll() {
    this.$('.existing').html('');
    return this.collection.each(this.addOne, this);
  },
  add: function add() {
    return this.collection.add(new this.data.ModelType());
  },
  getModelData: function getModelData() {
    var model = this.model.attributes;
    var path = this.data.modelAttribute.split('.');

    while (path.length >= 2) {
      model = model[path.shift()] || {};
    }

    return model[path[0]] || [];
  },
  updateModel: function updateModel() {
    var path = this.data.modelAttribute.split('.');
    var data = this.collection.toJSON();

    while (path.length > 0) {
      var oldData = data;
      data = {};
      data[path.pop()] = oldData;
    }

    return this.model.set(data);
  },
  updateCollection: function updateCollection(model) {
    var _this2 = this;

    if (model.hasChanged(this.data.modelAttribute)) {
      var updated = model.get(this.data.modelAttribute);
      var collectionLength = this.collection.length; // Update existing models

      index_all/* default.chain */.ZP.chain(updated).first(collectionLength).each(function (update, index) {
        return _this2.collection.at(index).set(update);
      }); // Add new models


      index_all/* default.chain */.ZP.chain(updated).rest(collectionLength).each(function (update) {
        return _this2.collection.add(update);
      }); // Remove models not in updated


      return this.collection.remove(this.collection.rest(updated.length));
    }
  },
  show: function show() {
    SingleView/* default.prototype.show.apply */.Z.prototype.show.apply(this);
    return this.collection.trigger('visible');
  },
  hide: function hide() {
    SingleView/* default.prototype.hide.apply */.Z.prototype.hide.apply(this);
    return this.collection.trigger('hidden');
  }
}));
;// CONCATENATED MODULE: ./editor/src/views/PredefinedParentLargeView.js





/* harmony default export */ const PredefinedParentLargeView = (ParentLargeView.extend({
  events: {
    'click .dropdown-menu': 'setPredefined'
  },
  render: function render() {
    ParentLargeView.prototype.render.apply(this);
    this.$('button.add').replaceWith(PredefinedParent({
      data: this.data
    }));
    this.$('button').prop(this.data.disabled, this.data.disabled);
    var $dropdown = this.$('ul.dropdown-menu');

    index_all/* default.chain */.ZP.chain(this.data.predefined).keys().each(function (item) {
      return $dropdown.append(PredefinedParentDropdown({
        predefined: item
      }));
    });

    return this;
  },
  setPredefined: function setPredefined(event) {
    event.preventDefault();
    var value = jquery_default()(event.target).text();
    var selected = {};

    if (value !== 'Custom') {
      selected = this.data.predefined[value];
    }

    return this.collection.add(selected);
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/DataLocation.tpl
// Module
var DataLocation_code = "<div class=\"datalocation\"> <div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Uid\">Unique ID</label> </div> <div class=\"col-sm-11\"> <input data-name=\"uid\" id=\"<%= data.modelAttribute %><%= data.index %>Uid\" class=\"editor-input\" value=\"<%= data.uid %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Name\">Name</label> </div> <div class=\"col-sm-11\"> <input data-name=\"name\" id=\"<%= data.modelAttribute %><%= data.index %>Name\" class=\"editor-input\" value=\"<%= data.name %>\" placeholder=\"e.g. Post-CAP\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Purpose\">Purpose</label> </div> <div class=\"col-sm-11\"> <input data-name=\"purpose\" id=\"<%= data.modelAttribute %><%= data.index %>Purpose\" class=\"editor-input\" value=\"<%= data.purpose %>\" placeholder=\"e.g. This datafile contains data for the Post-CAP scenario\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"<%= data.modelAttribute %><%= data.index %>FileLocation\">File location</label> </div> <div class=\"col-sm-11\"> <input data-name=\"fileLocation\" id=\"<%= data.modelAttribute %><%= data.index %>FileLocation\" class=\"editor-input\" value=\"<%= data.fileLocation %>\" placeholder=\"e.g. //server/folder/file.csv OR https://ishare.com/data/geodatabase.gdb\"> </div> </div> </div>";
// Exports
/* harmony default export */ const DataLocation = (DataLocation_code);
;// CONCATENATED MODULE: ./editor/src/views/DataLocationView.js



/* harmony default export */ const DataLocationView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(DataLocation)
}));
;// CONCATENATED MODULE: ./editor/src/templates/PointOfContact.tpl
// Module
var PointOfContact_code = "<div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Person\">Person</label> </div> <div class=\"col-sm-10\"> <input data-name=\"individualName\" class=\"editor-input\" id=\"contacts<%= data.index %>Person\" value=\"<%= data.individualName %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Organisation\">Organisation</label> </div> <div class=\"col-sm-10\"> <input data-name=\"organisationName\" class=\"editor-input\" id=\"contacts<%= data.index %>Organisation\" value=\"<%= data.organisationName %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Address\">Address</label> </div> <div class=\"col-sm-10\"> <input data-name=\"deliveryPoint\" class=\"editor-input\" id=\"contacts<%= data.index %>Address\" value=\"<%= data.address.deliveryPoint %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>City\">City/Town</label> </div> <div class=\"col-sm-5\"> <input data-name=\"city\" class=\"editor-input\" id=\"contacts<%= data.index %>City\" value=\"<%= data.address.city %>\"> </div> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"contacts<%= data.index %>County\">County</label> </div> <div class=\"col-sm-4\"> <input data-name=\"administrativeArea\" class=\"editor-input\" id=\"contacts<%= data.index %>County\" value=\"<%= data.address.administrativeArea %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Postcode\">Postcode</label> </div> <div class=\"col-sm-5\"> <input data-name=\"postalCode\" class=\"editor-input\" id=\"contacts<%= data.index %>Postcode\" value=\"<%= data.address.postalCode %>\"> </div> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Country\">Country</label> </div> <div class=\"col-sm-4\"> <input data-name=\"country\" class=\"editor-input\" id=\"contacts<%= data.index %>Country\" value=\"<%= data.address.country %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Phone\">Phone</label> </div> <div class=\"col-sm-5\"> <input data-name=\"phone\" class=\"editor-input\" id=\"contacts<%= data.index %>Phone\" value=\"<%= data.phone %>\"> </div> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Email\">Email</label> </div> <div class=\"col-sm-4\"> <input data-name=\"email\" class=\"editor-input\" id=\"contacts<%= data.index %>Email\" value=\"<%= data.email %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>nameIdentifier\">Identifier</label> </div> <div class=\"col-sm-10\"> <input data-name=\"nameIdentifier\" placeholder=\"(e.g. ORCiD or ISNI)\" class=\"editor-input\" id=\"contacts<%= data.index %>nameIdentifier\" value=\"<%= data.nameIdentifier %>\"> </div> </div>";
// Exports
/* harmony default export */ const templates_PointOfContact = (PointOfContact_code);
;// CONCATENATED MODULE: ./editor/src/views/PointOfContactView.js




/* harmony default export */ const PointOfContactView = (ObjectInputView.extend({
  initialize: function initialize(options) {
    this.template = index_all/* default.template */.ZP.template(templates_PointOfContact);
    return ObjectInputView.prototype.initialize.apply(this);
  },
  render: function render() {
    ObjectInputView.prototype.render.apply(this);
    return this;
  },
  modify: function modify(event) {
    var $target = jquery_default()(event.target);
    var name = $target.data('name');
    var value = $target.val();

    if (index_all/* default.contains */.ZP.contains(['deliveryPoint', 'city', 'administrativeArea', 'country', 'postalCode'], name)) {
      var address = index_all/* default.clone */.ZP.clone(this.model.get('address'));

      if (value) {
        address[name] = value;
        return this.model.set('address', address);
      } else {
        address = index_all/* default.omit */.ZP.omit(address, name);
        return this.model.set('address', address);
      }
    } else {
      if (value) {
        return this.model.set(name, value);
      } else {
        return this.model.unset(name);
      }
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/ProcessingStep.tpl
// Module
var ProcessingStep_code = "<div class=\"provenance\"> <div class=\"row\"> <div class=\"col-sm-12\"> <textarea data-name=\"step\" id=\"<%= data.modelAttribute %><%= data.index %>Step\" class=\"editor-textarea\" rows=\"5\" placeholder=\"Actions, processes etc. describing how the resource was processed/came into being\"><%= data.step %></textarea> </div> </div> </div>";
// Exports
/* harmony default export */ const ProcessingStep = (ProcessingStep_code);
;// CONCATENATED MODULE: ./editor/src/views/ProcessingStepView.js



/* harmony default export */ const ProcessingStepView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(ProcessingStep)
}));
;// CONCATENATED MODULE: ./editor/src/templates/ErammpModelOutput.tpl
// Module
var ErammpModelOutput_code = "<div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>InternalName\">Internal name</label> </div> <div class=\"col-sm-9\"> <input data-name=\"internalName\" id=\"<%= data.modelAttribute %><%= data.index %>InternalName\" class=\"editor-input\" value=\"<%= data.internalName %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Definition\">Definition</label> </div> <div class=\"col-sm-9\"> <input data-name=\"definition\" id=\"<%= data.modelAttribute %><%= data.index %>Definition\" class=\"editor-input\" value=\"<%= data.definition %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.datatype %><%= data.index %>Datatype\">Data type</label> </div> <div class=\"col-sm-9\"> <input data-name=\"datatype\" id=\"<%= data.modelAttribute %><%= data.index %>Datatype\" class=\"editor-input\" value=\"<%= data.datatype %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Unit\">Unit</label> </div> <div class=\"col-sm-9\"> <input data-name=\"unit\" id=\"<%= data.modelAttribute %><%= data.index %>Unit\" class=\"editor-input\" value=\"<%= data.unit %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>SpatialResolution\">Spatial resolution</label> </div> <div class=\"col-sm-9\"> <input data-name=\"spatialResolution\" list=\"spatialResolution\" id=\"<%= data.modelAttribute %><%= data.index %>SpatialResolution\" class=\"editor-input\" value=\"<%= data.spatialResolution %>\"> </div> </div><div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>TemporalResolution\">Temporal resolution</label> </div> <div class=\"col-sm-9\"> <input data-name=\"temporalResolution\" list=\"temporalResolution\" id=\"<%= data.modelAttribute %><%= data.index %>TemporalResolution\" class=\"editor-input\" value=\"<%= data.temporalResolution %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Reuse\">Reuse</label> </div> <div class=\"col-sm-9\"> <input data-name=\"reuse\" id=\"<%= data.modelAttribute %><%= data.index %>Reuse\" class=\"editor-input\" value=\"<%= data.reuse %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Displayed\">Displayed</label> </div> <div class=\"col-sm-9\"> <input data-name=\"displayed\" id=\"<%= data.modelAttribute %><%= data.index %>Displayed\" class=\"editor-input\" value=\"<%= data.displayed %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>DisplayFormat\">DisplayFormat</label> </div> <div class=\"col-sm-9\"> <input data-name=\"displayFormat\" list=\"displayFormat\" id=\"<%= data.modelAttribute %><%= data.index %>DisplayFormat\" class=\"editor-input\" value=\"<%= data.displayFormat %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Ipr\">IPR</label> </div> <div class=\"col-sm-9\"> <input data-name=\"ipr\" id=\"<%= data.modelAttribute %><%= data.index %>Ipr\" class=\"editor-input\" value=\"<%= data.ipr %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>AdditionalNotes\">Notes</label> </div> <div class=\"col-sm-9\"> <input data-name=\"additionalNotes\" id=\"<%= data.modelAttribute %><%= data.index %>AdditionalNotes\" class=\"editor-input\" value=\"<%= data.additionalNotes %>\"> </div> </div> <datalist id=\"spatialResolution\"> <option value=\"Grid\"> </option><option value=\"Polygon\"> </option><option value=\"Cluster\"> </option><option value=\"River basin\"> </option><option value=\"Catchment\"> </option><option value=\"NUTS2\"> </option><option value=\"Country\"> </option><option value=\"Global\"> </option></datalist> <datalist id=\"temporalResolution\"> <option value=\"Sub-daily\"> </option><option value=\"Daily\"> </option><option value=\"Monthly\"> </option><option value=\"Seasonal\"> </option><option value=\"Annual\"> </option></datalist> <datalist id=\"displayFormat\"> <option value=\"Map\"> </option><option value=\"Graph\"> </option><option value=\"Bar chart\"> </option><option value=\"Smiley\"> </option></datalist>";
// Exports
/* harmony default export */ const ErammpModelOutput = (ErammpModelOutput_code);
;// CONCATENATED MODULE: ./editor/src/views/ErammpModelOutputView.js



/* harmony default export */ const ErammpModelOutputView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(ErammpModelOutput)
}));
;// CONCATENATED MODULE: ./editor/src/templates/ErammpModelInput.tpl
// Module
var ErammpModelInput_code = "<div class=\"erammp-input\"> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>InternalName\">Internal name</label> </div> <div class=\"col-sm-9\"> <input data-name=\"internalName\" id=\"<%= data.modelAttribute %><%= data.index %>InternalName\" class=\"editor-input\" value=\"<%= data.internalName %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>ExternalName\">External name</label> </div> <div class=\"col-sm-9\"> <input data-name=\"externalName\" id=\"<%= data.modelAttribute %><%= data.index %>ExternalName\" class=\"editor-input\" value=\"<%= data.externalName %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Definition\">Definition</label> </div> <div class=\"col-sm-9\"> <input data-name=\"definition\" id=\"<%= data.modelAttribute %><%= data.index %>Definition\" class=\"editor-input\" value=\"<%= data.definition %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>InputSource\">Input source</label> </div> <div class=\"col-sm-9\"> <input data-name=\"inputSource\" id=\"<%= data.modelAttribute %><%= data.index %>InputSource\" class=\"editor-input\" value=\"<%= data.inputSource %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Unit\">Unit</label> </div> <div class=\"col-sm-9\"> <input data-name=\"unit\" id=\"<%= data.modelAttribute %><%= data.index %>Unit\" class=\"editor-input\" value=\"<%= data.unit %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>SpatialResolution\">Spatial resolution</label> </div> <div class=\"col-sm-9\"> <input data-name=\"spatialResolution\" id=\"<%= data.modelAttribute %><%= data.index %>SpatialResolution\" class=\"editor-input\" value=\"<%= data.spatialResolution %>\"> </div> </div><div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>TemporalResolution\">Temporal resolution</label> </div> <div class=\"col-sm-9\"> <input data-name=\"temporalResolution\" id=\"<%= data.modelAttribute %><%= data.index %>TemporalResolution\" class=\"editor-input\" value=\"<%= data.temporalResolution %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>ManagementPolicy\">Management policy</label> </div> <div class=\"col-sm-9\"> <input data-name=\"managementPolicy\" id=\"<%= data.modelAttribute %><%= data.index %>ManagementPolicy\" class=\"editor-input\" value=\"<%= data.managementPolicy %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Access\">Access</label> </div> <div class=\"col-sm-9\"> <input data-name=\"access\" id=\"<%= data.modelAttribute %><%= data.index %>Access\" class=\"editor-input\" value=\"<%= data.access %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Licensing\">Licensing</label> </div> <div class=\"col-sm-9\"> <input data-name=\"licensing\" id=\"<%= data.modelAttribute %><%= data.index %>Licensing\" class=\"editor-input\" value=\"<%= data.licensing %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>DataSource\">Data source</label> </div> <div class=\"col-sm-9\"> <input data-name=\"dataSource\" id=\"<%= data.modelAttribute %><%= data.index %>DataSource\" class=\"editor-input\" value=\"<%= data.dataSource %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3\"> <label for=\"<%= data.modelAttribute %><%= data.index %>AdditionalNotes\">Notes</label> </div> <div class=\"col-sm-9\"> <input data-name=\"additionalNotes\" id=\"<%= data.modelAttribute %><%= data.index %>AdditionalNotes\" class=\"editor-input\" value=\"<%= data.additionalNotes %>\"> </div> </div> </div>";
// Exports
/* harmony default export */ const ErammpModelInput = (ErammpModelInput_code);
;// CONCATENATED MODULE: ./editor/src/views/ErammpModelInputView.js



/* harmony default export */ const ErammpModelInputView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(ErammpModelInput)
}));
;// CONCATENATED MODULE: ./editor/src/templates/LinkDocument.tpl
// Module
var LinkDocument_code = "<h2> <a href=\"/documents/<%=identifier%>\" target=\"_new\"><%=title%></a> <small><span class=\"fas fa-external-link-alt\" aria-hidden=\"true\"></span></small> </h2> <div class=\"row\"> <div class=\"col-sm-10 col-lg-10\"><%=shortenedDescription%></div> <div class=\"col-sm-2 col-lg-2\"> <button class=\"btn btn-success\">Select</button> </div> </div> ";
// Exports
/* harmony default export */ const LinkDocument = (LinkDocument_code);
;// CONCATENATED MODULE: ./editor/src/views/LinkDocumentView.js



/* harmony default export */ const LinkDocumentView = (backbone_default().View.extend({
  events: {
    'click button': 'selected'
  },
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(LinkDocument);
    this.$el.html(this.template(this.model.attributes));
    return this;
  },
  selected: function selected(event) {
    return this.model.trigger('selected', this.model.get('identifier'));
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/LinkDocumentSelector.tpl
// Module
var LinkDocumentSelector_code = "<div class=\"form-inline\" style=\"margin-top:6px\"> <div class=\"form-group\"> <label for=\"linkedDocumentId\">Linked Document Identifier</label> <input data-name=\"<%= data.modelAttribute %>\" class=\"form-control\" readonly=\"readonly\" id=\"linkedDocumentId\" value=\"<%= data.value %>\"> </div> </div> <div class=\"form-inline\" style=\"margin-top:6px\"> <div class=\"form-group\"> <label for=\"catalogue\">Catalogue</label> <select id=\"catalogue\" class=\"form-control\"></select> </div> <div class=\"input-group\"> <input placeholder=\"Search the catalogue\" id=\"term\" type=\"text\" autocomplete=\"off\" class=\"form-control\" value=\"<%= data.term %>\"> <div class=\"input-group-btn\"> <button tabindex=\"-1\" class=\"btn btn-success\" type=\"button\" aria-label=\"Search\"> <span class=\"fas fa-search\"></span> </button> </div> </div> <hr> <div id=\"results\"></div></div>";
// Exports
/* harmony default export */ const LinkDocumentSelector = (LinkDocumentSelector_code);
;// CONCATENATED MODULE: ./editor/src/views/LinkDocumentSelectorView.js






/* harmony default export */ const LinkDocumentSelectorView = (InputView/* default.extend */.Z.extend({
  events: function events() {
    return index_all/* default.extend */.ZP.extend({}, InputView/* default.prototype.events */.Z.prototype.events, {
      'keyup #term': function keyupTerm() {
        return this.searchOnceComplete();
      },
      'change #term': function changeTerm() {
        return this.search();
      },
      'change #catalogue': function changeCatalogue() {
        return this.search();
      },
      'click button': function clickButton() {
        return this.search();
      }
    });
  },
  template: LinkDocumentSelector,
  optionTemplate: index_all/* default.template */.ZP.template('<option value="<%= id %>" <% if (id === data.catalogue) { %>selected<% } %>><%= title %></option>'),
  initialize: function initialize(options) {
    var _this = this;

    this.template = index_all/* default.template */.ZP.template(LinkDocumentSelector);
    var params;

    if (this.model.isNew()) {
      params = "catalogue=".concat(backbone_default().history.location.pathname.split('/')[1]);
    } else {
      params = "identifier=".concat(this.model.get('id'));
    }

    options.catalogue = 'eidc';
    this.searchOnceComplete = index_all/* default.debounce */.ZP.debounce(this.search, 500);
    this.results = new (backbone_default()).Collection();
    jquery_default().getJSON("/catalogues?".concat(params), function (catalogues) {
      _this.catalogues = catalogues;
      return InputView/* default.prototype.initialize.call */.Z.prototype.initialize.call(_this, options);
    });
    this.listenTo(this.results, 'selected', this.setSelected);
    return this.listenTo(this.results, 'reset', this.addAll);
  },
  render: function render() {
    var _this2 = this;

    InputView/* default.prototype.render.apply */.Z.prototype.render.apply(this);
    var $select = this.$('#catalogue');

    index_all/* default.each */.ZP.each(this.catalogues, function (catalogue) {
      return $select.append(_this2.optionTemplate(index_all/* default.extend */.ZP.extend({}, catalogue, {
        data: _this2.data
      })));
    });

    this.search();
    return this;
  },
  search: function search() {
    var _this3 = this;

    var searchUrl;
    this.data.catalogue = this.$('#catalogue').val();
    this.data.term = this.$('#term').val();

    if (this.data.term.length > 0) {
      searchUrl = "/".concat(this.data.catalogue, "/documents?term=state:published AND view:public AND NOT documentType:LINK_DOCUMENT AND ").concat(this.data.term);
    } else {
      searchUrl = "/".concat(this.data.catalogue, "/documents?term=state:published AND view:public AND NOT documentType:LINK_DOCUMENT");
    }

    return jquery_default().getJSON(searchUrl, function (data) {
      return _this3.results.reset(data.results);
    });
  },
  addOne: function addOne(result) {
    var view = new LinkDocumentView({
      model: result
    });
    return this.$('#results').append(view.render().el);
  },
  addAll: function addAll() {
    this.$('#results').html('');
    return this.results.each(this.addOne, this);
  },
  setSelected: function setSelected(identifier) {
    return this.model.set(this.data.modelAttribute, identifier);
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/ModelApplicationModel.tpl
// Module
var ModelApplicationModel_code = "<div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"modelApplicationModel<%= data.index %>Name\">Name</label> </div> <div class=\"col-sm-5\"> <input data-name=\"name\" id=\"modelApplicationModel<%= data.index %>Name\" class=\"editor-input\" value=\"<%= data.name %>\"> </div> <div class=\"col-sm-1\"> <label for=\"modelApplicationModel<%= data.index %>Version\">Version</label> </div> <div class=\"col-sm-5\"> <input data-name=\"version\" id=\"modelApplicationModel<%= data.index %>Version\" class=\"editor-input\" value=\"<%= data.version %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"modelApplicationModel<%= data.index %>PrimaryPurpose\">Primary Purpose</label> </div> <div class=\"col-sm-5\"> <input data-name=\"primaryPurpose\" id=\"modelApplicationModel<%= data.index %>PrimaryPurpose\" class=\"editor-input\" value=\"<%= data.primaryPurpose %>\"> </div> <div class=\"col-sm-1\"> <label for=\"modelApplicationModel<%= data.index %>ApplicationScale\">Application Scale</label> </div> <div class=\"col-sm-5\"> <select data-name=\"applicationScale\" id=\"modelApplicationModel<%= data.index %>ApplicationScale\" class=\"editor-input\"> <option value=\"plot\">plot</option> <option value=\"field\">field</option> <option value=\"farm\">farm</option> <option value=\"river reach\">river reach</option> <option value=\"catchment\">catchment</option> <option value=\"landscape\">landscape</option> <option value=\"regional\">regional</option> <option value=\"national\">national</option> </select> </div> </div> <div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"modelApplicationModel<%= data.index %>KeyOutputVariables\">Key Output Variables</label> </div> <div class=\"col-sm-5\"> <input data-name=\"keyOutputVariables\" id=\"modelApplicationModel<%= data.index %>KeyOutputVariables\" class=\"editor-input\" value=\"<%= data.keyOutputVariables %>\"> </div> <div class=\"col-sm-1\"> <label for=\"modelApplicationModel<%= data.index %>KeyInputVariables\">Key Input Variables</label> </div> <div class=\"col-sm-5\"> <input data-name=\"keyInputVariables\" id=\"modelApplicationModel<%= data.index %>KeyInputVariables\" class=\"editor-input\" value=\"<%= data.keyInputVariables %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"modelApplicationModel<%= data.index %>TemporalResolution\">Temporal Resolution</label> </div> <div class=\"col-sm-5\"> <input data-name=\"temporalResolution\" id=\"modelApplicationModel<%= data.index %>TemporalResolution\" class=\"editor-input\" value=\"<%= data.temporalResolution %>\"> </div> <div class=\"col-sm-1\"> <label for=\"modelApplicationModel<%= data.index %>SpatialResolution\">Spatial Resolution</label> </div> <div class=\"col-sm-5\"> <input data-name=\"spatialResolution\" id=\"modelApplicationModel<%= data.index %>SpatialResolution\" class=\"editor-input\" value=\"<%= data.spatialResolution %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"modelApplicationModel<%= data.index %>InputDataAvailableInDataCatalogue\">Input Data Available In Data Catalogue</label> </div> <div class=\"col-sm-5\"> <input data-name=\"inputDataAvailableInDataCatalogue\" id=\"modelApplicationModel<%= data.index %>InputDataAvailableInDataCatalogue\" class=\"editor-input\" value=\"<%= data.inputDataAvailableInDataCatalogue %>\"> </div> </div>";
// Exports
/* harmony default export */ const ModelApplicationModel = (ModelApplicationModel_code);
;// CONCATENATED MODULE: ./editor/src/views/ModelApplicationModelView.js



/* harmony default export */ const ModelApplicationModelView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(ModelApplicationModel);
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('applicationScale'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/Link.tpl
// Module
var Link_code = "<div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label for=\"<%= data.modelAttribute %><%= data.index %>URL\">URL</label> </div> <div class=\"col-sm-11 col-lg-11\"> <input data-name=\"href\" id=\"<%= data.modelAttribute %><%= data.index %>URL\" class=\"editor-input\" value=\"<%= data.href %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-1 col-lg-1\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Title\">Title</label> </div> <div class=\"col-sm-11 col-lg-11\"> <input data-name=\"title\" id=\"<%= data.modelAttribute %><%= data.index %>Title\" class=\"editor-input\" value=\"<%= data.title %>\"> </div> </div>";
// Exports
/* harmony default export */ const Link = (Link_code);
;// CONCATENATED MODULE: ./editor/src/views/LinkView.js



/* harmony default export */ const LinkView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(Link)
}));
;// CONCATENATED MODULE: ./editor/src/templates/ShortContact.tpl
// Module
var ShortContact_code = "<div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label class=\"control-label\" for=\"contactPerson\">Person</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"individualName\" class=\"editor-input\" id=\"contactPerson\" value=\"<%= data.individualName %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label class=\"control-label\" for=\"contactOrganisation\">Organisation</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"organisationName\" class=\"editor-input\" id=\"contactOrganisation\" value=\"<%= data.organisationName %>\"> </div> </div> ";
// Exports
/* harmony default export */ const ShortContact = (ShortContact_code);
;// CONCATENATED MODULE: ./editor/src/views/ShortContactView.js



/* harmony default export */ const ShortContactView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(ShortContact)
}));
;// CONCATENATED MODULE: ./editor/src/templates/Relationship.tpl
// Module
var Relationship_code = "<div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"relationship<%= data.index %>Relation\">Relation</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input list=\"relationship<%= data.index %>relationships\" data-name=\"relation\" id=\"relationship<%= data.index %>Relation\" class=\"editor-input\" value=\"<%= data.relation %>\"> <datalist id=\"relationship<%= data.index %>relationships\"></datalist> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"relationship<%= data.index %>Target\">Target</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"target\" id=\"relationship<%= data.index %>Target\" class=\"editor-input\" value=\"<%= data.target %>\"> </div> </div>";
// Exports
/* harmony default export */ const Relationship = (Relationship_code);
;// CONCATENATED MODULE: ./editor/src/views/RelationshipView.js



/* harmony default export */ const RelationshipView = (ObjectInputView.extend({
  optionTemplate: index_all/* default.template */.ZP.template('<option value="<%= value %>"><%= label %></option>'),
  initialize: function initialize(options) {
    this.template = index_all/* default.template */.ZP.template(Relationship);
    this.options = options.options;
    return ObjectInputView.prototype.initialize.call(this, options);
  },
  render: function render() {
    var _this = this;

    ObjectInputView.prototype.render.apply(this);
    var $list = this.$('datalist');
    this.options.forEach(function (option) {
      return $list.append(_this.optionTemplate(option));
    });
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/ModelQA.tpl
// Module
var ModelQA_code = "<div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"qa<%= data.index %>Category\">Category</label> </div> <div class=\"col-sm-5\"> <select data-name=\"category\" class=\"editor-input category\" id=\"qa<%= data.index %>Category\"> <option value=\"\" selected=\"selected\">-- Choose one --</option> <option value=\"developerTesting\">Developer testing</option> <option value=\"governance\">Governance</option> <option value=\"guidelinesAndChecklists\">Guidelines and checklists</option> <option value=\"internalModelAudit\">Model audit (internal)</option> <option value=\"externalModelAudit\">Model audit (external)</option> <option value=\"internalPeerReview\">Peer review (internal)</option> <option value=\"externalPeerReview\">Peer review (external)</option> <option value=\"periodicReview\">Periodic review</option> <option value=\"transparency\">Transparency</option> </select> </div> <div class=\"col-sm-1\"> <label for=\"qa<%= data.index %>Date\">Date</label> </div> <div class=\"col-sm-5\"> <input data-name=\"date\" type=\"date\" id=\"qa<%= data.index %>Date\" class=\"editor-input\" value=\"<%= data.date %>\"> </div> <div class=\"col-sm-1\"> <label for=\"qa<%= data.index %>Notes\">Notes</label> </div> <div class=\"col-sm-11\"> <textarea data-name=\"notes\" id=\"qa<%= data.index %>Notes\" class=\"editor-textarea\" rows=\"3\"><%= data.notes %></textarea> </div> </div> ";
// Exports
/* harmony default export */ const ModelQA = (ModelQA_code);
;// CONCATENATED MODULE: ./editor/src/views/ModelQAView.js



/* harmony default export */ const ModelQAView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(ModelQA);
    ObjectInputView.prototype.render.apply(this);
    this.$('select.category').val(this.model.get('category'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/ModelResolution.tpl
// Module
var ModelResolution_code = "<div class=\"row\"> <div class=\"col-sm-1\"> <label for=\"resolution<%= data.index %>Category\">Category</label> </div> <div class=\"col-sm-11\"> <select data-name=\"category\" class=\"editor-input category\" id=\"resolution<%= data.index %>Category\"> <option value=\"\" selected=\"selected\">-- Choose one --</option> <option value=\"temporal\">Temporal</option> <option value=\"vertical\">Vertical</option> <option value=\"horizontal\">Horizontal</option> </select> </div> <div class=\"col-sm-1\"> <label for=\"resolution<%= data.index %>Min\">Min</label> </div> <div class=\"col-sm-5\"> <input data-name=\"min\" id=\"resolution<%= data.index %>Min\" class=\"editor-input\" value=\"<%= data.min %>\"> </div> <div class=\"col-sm-1\"> <label for=\"resolution<%= data.index %>Max\">Max</label> </div> <div class=\"col-sm-5\"> <input data-name=\"max\" id=\"resolution<%= data.index %>Max\" class=\"editor-input\" value=\"<%= data.max %>\"> </div> </div> ";
// Exports
/* harmony default export */ const ModelResolution = (ModelResolution_code);
;// CONCATENATED MODULE: ./editor/src/views/ModelResolutionView.js



/* harmony default export */ const ModelResolutionView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(ModelResolution);
    ObjectInputView.prototype.render.apply(this);
    this.$('select.category').val(this.model.get('category'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/NercModelInfo.tpl
// Module
var NercModelInfo_code = "<div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>Name\">Name</label> </div> <div class=\"col-sm-9 col-lg-9\"> <div class=\"input-group\"> <span class=\"input-group-addon\" id=\"sizing-addon2\">Search</span> <input data-name=\"name\" id=\"modelInfo<%= data.index %>Name\" class=\"form-control autocomplete\" value=\"<%= data.name %>\"> </div> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>Id\">Id</label> </div> <div class=\"col-sm-9 col-lg-9\"> <input data-name=\"id\" id=\"modelInfo<%= data.index %>Id\" class=\"editor-input identifier\" value=\"<%= data.id %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>Notes\">Notes</label> </div> <div class=\"col-sm-9 col-lg-9\"> <textarea data-name=\"notes\" rows=\"7\" id=\"modelInfo<%= data.index %>Notes\" class=\"editor-textarea\"><%= data.notes %></textarea> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>spatialExtentOfApplication\">Spatial extent of application</label> </div> <div class=\"col-sm-9 col-lg-9\"> <input list=\"modelInfo<%= data.index %>SpatialExtentOfApplicationList\" data-name=\"spatialExtentOfApplication\" id=\"modelInfo<%= data.index %>SpatialExtentOfApplication\" class=\"editor-input\" value=\"<%= data.spatialExtentOfApplication %>\"/> <datalist id=\"modelInfo<%= data.index %>SpatialExtentOfApplicationList\"> <option value=\"Plot\"/> <option value=\"Field\"/> <option value=\"Farm\"/> <option value=\"River reach\"/> <option value=\"Catchment\"/> <option value=\"Landscape\"/> <option value=\"Regional\"/> <option value=\"National\"/> <option value=\"Global\"/> </datalist> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>AvailableSpatialData\">Available spatial data</label> </div> <div class=\"col-sm-9 col-lg-9\"> <select data-name=\"availableSpatialData\" id=\"modelInfo<%= data.index %>AvailableSpatialData\" class=\"editor-input spatial-data\"> <option value=\"unknown\">Unknown</option> <option value=\"shapefile\">Shapefile</option> <option value=\"bounding box\">Bounding box</option> </select> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>SpatialResolutionOfApplication\">Spatial resolution of application</label> </div> <div class=\"col-sm-9 col-lg-9\"> <input data-name=\"spatialResolutionOfApplication\" id=\"modelInfo<%= data.index %>SpatialResolutionOfApplication\" class=\"editor-input\" value=\"<%= data.spatialResolutionOfApplication %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>TemporalExtentOfApplicationStartDate\">Temporal extent of application (start date)</label> </div> <div class=\"col-sm-9 col-lg-9\"> <input data-name=\"temporalExtentOfApplicationStartDate\" id=\"modelInfo<%= data.index %>TemporalExtentOfApplicationStartDate\" class=\"editor-input\" value=\"<%= data.temporalExtentOfApplicationStartDate %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>TemporalExtentOfApplicationEndDate\">Temporal extent of application (end date)</label> </div> <div class=\"col-sm-9 col-lg-9\"> <input data-name=\"temporalExtentOfApplicationEndDate\" id=\"modelInfo<%= data.index %>TemporalExtentOfApplicationEndDate\" class=\"editor-input\" value=\"<%= data.temporalExtentOfApplicationEndDate %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>TemporalResolutionOfApplication\">Temporal resolution of application</label> </div> <div class=\"col-sm-9 col-lg-9\"> <input data-name=\"temporalResolutionOfApplication\" id=\"modelInfo<%= data.index %>TemporalResolutionOfApplication\" class=\"editor-input\" value=\"<%= data.temporalResolutionOfApplication %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-3 col-lg-3\"> <label for=\"modelInfo<%= data.index %>CalibrationConditions\">Calibration</label> </div> <div class=\"col-sm-9 col-lg-9\"> <textarea data-name=\"calibrationConditions\" rows=\"3\" id=\"modelInfo<%= data.index %>CalibrationConditions\" class=\"editor-textarea\"><%= data.calibrationConditions %></textarea> </div> </div>";
// Exports
/* harmony default export */ const NercModelInfo = (NercModelInfo_code);
;// CONCATENATED MODULE: ./editor/src/views/NercModelInfoView.js




/* harmony default export */ const NercModelInfoView = (ObjectInputView.extend({
  template: NercModelInfo,
  initialize: function initialize() {
    var _this = this;

    this.template = index_all/* default.template */.ZP.template(NercModelInfo);
    ObjectInputView.prototype.initialize.apply(this);
    var catalogue = jquery_default()('html').data('catalogue');
    this.$('.autocomplete').autocomplete({
      minLength: 0,
      source: function source(request, response) {
        var query;
        var term = request.term.trim();

        if (index_all/* default.isEmpty */.ZP.isEmpty(term)) {
          query = "/".concat(catalogue, "/documents?term=documentType:nerc-model");
        } else {
          query = "/".concat(catalogue, "/documents?term=documentType:nerc-model AND ").concat(request.term);
        }

        return jquery_default().getJSON(query, function (data) {
          return response(index_all/* default.map */.ZP.map(data.results, function (d) {
            return {
              value: d.title,
              label: d.title,
              identifier: d.identifier
            };
          }));
        });
      }
    });
    return this.$('.autocomplete').on('autocompleteselect', function (event, ui) {
      _this.model.set('id', ui.item.identifier);

      return _this.$('.identifier').val(ui.item.identifier);
    });
  },
  render: function render() {
    ObjectInputView.prototype.render.apply(this);
    this.$('select.spatial-data').val(this.model.get('availableSpatialData'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/ResourceMaintenance.tpl
// Module
var ResourceMaintenance_code = "<div class=\"col-sm-2 col-lg-2\"> <label for=\"resourceMaintenance<%= data.index %>Frequency\">Frequency of Update</label> </div> <div class=\"col-sm-3 col-lg-3\"> <select data-name=\"frequencyOfUpdate\" id=\"resourceMaintenance<%= data.index %>Frequency\" class=\"editor-input\"> <option value=\"\" selected=\"selected\">- Select Frequency of Update -</option> <optgroup label=\"Time Period\"> <option value=\"daily\">Daily</option> <option value=\"weekly\">Weekly</option> <option value=\"fortnightly\">Fortnightly</option> <option value=\"monthly\">Monthly</option> <option value=\"quarterly\">Quarterly</option> <option value=\"annually\">Annually</option> <option value=\"biannually\">Biannually</option> </optgroup> <optgroup label=\"Other\"> <option value=\"asNeeded\">As Needed</option> <option value=\"continual\">Continual</option> <option value=\"irregular\">Irregular</option> <option value=\"notPlanned\">Not Planned</option> <option value=\"unknown\">Unknown</option> </optgroup> </select> </div> <div class=\"col-sm-1 col-lg-1\"> <label for=\"resourceMaintenance<%= data.index %>Note\">Notes</label> </div> <div class=\"col-sm-6 col-lg-6\"> <textarea data-name=\"note\" rows=\"3\" id=\"resourceMaintenance<%= data.index %>Note\" class=\"editor-textarea\"><%= data.note %></textarea> </div> ";
// Exports
/* harmony default export */ const ResourceMaintenance = (ResourceMaintenance_code);
;// CONCATENATED MODULE: ./editor/src/views/ResourceMaintenanceView.js



/* harmony default export */ const ResourceMaintenanceView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(ResourceMaintenance);
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('frequencyOfUpdate'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/SupplementalEIDC.tpl
// Module
var SupplementalEIDC_code = "<div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"supplemental<%= data.index %>Function\">Type</label> </div> <div class=\"col-sm-10\"> <select data-name=\"function\" class=\"editor-input function\" id=\"supplemental<%= data.index %>Function\"> <option value=\"\" selected=\"selected\">-- Choose one --</option> <optgroup label=\"Citation\"> <option value=\"isReferencedBy\">REFERENCED IN. The data is referenced/cited in this article.</option> <option value=\"isSupplementTo\">SUPPLEMENT TO. The data underlies the findings in this article.</option> </optgroup> <optgroup label=\"Other\"> <option value=\"relatedArticle\">RELATED ARTICLE. An article (or grey literature) which is relevant but which DOESN'T cite this resource</option> <option value=\"relatedDataset\">RELATED DATASET. A dataset which is related but which is NOT hosted by EIDC)</option> <option value=\"website\">RELATED WEBSITE. (e.g. project website)</option> <option value=\"\">Miscellaneous</option> </optgroup> </select> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"supplemental<%= data.index %>Name\">Name</label> </div> <div class=\"col-sm-10\"> <input data-name=\"name\" class=\"editor-input\" id=\"supplemental<%= data.index %>Name\" value=\"<%= data.name %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"supplemental<%= data.index %>Description\">Description</label> </div> <div class=\"col-sm-10\"> <textarea data-name=\"description\" class=\"editor-textarea\" id=\"supplemental<%= data.index %>Description\" rows=\"4\"><%= data.description %></textarea> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"supplemental<%= data.index %>URL\">URL/DOI</label> </div> <div class=\"col-sm-10\"> <input data-name=\"url\" class=\"editor-input\" id=\"supplemental<%= data.index %>URL\" value=\"<%= data.url %>\"> </div> </div> ";
// Exports
/* harmony default export */ const SupplementalEIDC = (SupplementalEIDC_code);
;// CONCATENATED MODULE: ./editor/src/views/SupplementalEIDCView.js



/* harmony default export */ const SupplementalEIDCView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(SupplementalEIDC);
    ObjectInputView.prototype.render.apply(this);
    this.$('select.function').val(this.model.get('function'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/views/DataTypeProvenanceView.js




/* harmony default export */ const DataTypeProvenanceView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(DatasetReferenceDate);
    ObjectInputView.prototype.render.apply(this);
    this.$('input').datepicker({
      dateFormat: 'yy-mm-dd'
    });
    var parent = new ParentStringView({
      el: this.$('#provenanceContributors'),
      model: this.model,
      modelAttribute: 'contributors',
      label: 'Contributors'
    });
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/ParametersMeasured.tpl
// Module
var ParametersMeasured_code = "<div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"parametersMeasured<%= data.index %>NameTitle\">Name: title</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"name.title\" id=\"parametersMeasured<%= data.index %>NameTitle\" class=\"editor-input\" value=\"<%= data.name.title %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"parametersMeasured<%= data.index %>NameHref\">Name: url</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"name.href\" id=\"parametersMeasured<%= data.index %>NameHref\" class=\"editor-input\" value=\"<%= data.name.href %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"parametersMeasured<%= data.index %>UnitOfMeasureTitle\">Unit of Measure: title</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"unitOfMeasure.title\" id=\"parametersMeasured<%= data.index %>UnitOfMeasureTitle\" class=\"editor-input\" value=\"<%= data.unitOfMeasure.title %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"parametersMeasured<%= data.index %>UnitOfMeasureHref\">Unit of Measure: url</label> </div> <div class=\"col-sm-10 col-lg-10\"> <input data-name=\"unitOfMeasure.href\" id=\"parametersMeasured<%= data.index %>UnitOfMeasureHrefe\" class=\"editor-input\" value=\"<%= data.unitOfMeasure.href %>\"> </div> </div>";
// Exports
/* harmony default export */ const ParametersMeasured = (ParametersMeasured_code);
;// CONCATENATED MODULE: ./editor/src/views/ObjectInputViewForObjects.js
function _slicedToArray(arr, i) { return _arrayWithHoles(arr) || _iterableToArrayLimit(arr, i) || _unsupportedIterableToArray(arr, i) || _nonIterableRest(); }

function _nonIterableRest() { throw new TypeError("Invalid attempt to destructure non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); }

function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }

function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) { arr2[i] = arr[i]; } return arr2; }

function _iterableToArrayLimit(arr, i) { var _i = arr == null ? null : typeof Symbol !== "undefined" && arr[Symbol.iterator] || arr["@@iterator"]; if (_i == null) return; var _arr = []; var _n = true; var _d = false; var _s, _e; try { for (_i = _i.call(arr); !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"] != null) _i["return"](); } finally { if (_d) throw _e; } } return _arr; }

function _arrayWithHoles(arr) { if (Array.isArray(arr)) return arr; }




/* harmony default export */ const ObjectInputViewForObjects = (ObjectInputView.extend({
  // Copes with an object made up of objects
  // So, one further level of objects than ObjectInputView
  // The template <input data-name="objectName.attributeName" …
  modify: function modify(event) {
    var $target = jquery_default()(event.target);

    var _Array$from = Array.from($target.data('name').split('.')),
        _Array$from2 = _slicedToArray(_Array$from, 2),
        objectName = _Array$from2[0],
        attributeName = _Array$from2[1];

    var value = $target.val();

    this._setObject(objectName, attributeName, value);

    return false;
  },
  // disable bubbling
  _setObject: function _setObject(objectName, attributeName, value) {
    if (!value) {
      return this.model.unset(objectName);
    } else {
      if (!index_all/* default.isUndefined */.ZP.isUndefined(attributeName)) {
        var obj = index_all/* default.extend */.ZP.extend({}, this.model.get(objectName));

        obj[attributeName] = value;
        return this.model.set(objectName, obj);
      } else {
        return this.model.set(objectName, value);
      }
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/views/ParametersMeasuredView.js



/* harmony default export */ const ParametersMeasuredView = (ObjectInputViewForObjects.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(ParametersMeasured);

    if (!Array.from(this.data).includes('name')) {
      this.data.name = {};
    }

    if (!Array.from(this.data).includes('unitOfMeasure')) {
      this.data.unitOfMeasure = {};
    }

    return ObjectInputViewForObjects.prototype.render.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/views/ObservationCapabilityView.js



/* harmony default export */ const ObservationCapabilityView = (ObjectInputViewForObjects.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(ObservationCapability);

    if (!Array.from(this.data).includes('observedPropertyName')) {
      this.data.observedPropertyName = {};
    }

    if (!Array.from(this.data).includes('observedPropertyUnitOfMeasure')) {
      this.data.observedPropertyUnitOfMeasure = {};
    }

    if (!Array.from(this.data).includes('procedureName')) {
      this.data.procedureName = {};
    }

    return ObjectInputViewForObjects.prototype.render.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/SaTaxa.tpl
// Module
var SaTaxa_code = "<select data-name=\"value\" class=\"editor-input\"> <option value=\"\" selected=\"selected\">- Add taxon -</option> <option value=\"Algae\">Algae</option> <option value=\"Amphibian\">Amphibian</option> <option value=\"Annelid\">Annelid</option> <option value=\"Arthropod\">Arthropod</option> <option value=\"Bacteria\">Bacteria</option> <option value=\"Bird\">Bird</option> <option value=\"Fern\">Fern</option> <option value=\"Fish\">Fish</option> <option value=\"Fungi\">Fungi</option> <option value=\"Invertebrate\">Invertebrate</option> <option value=\"Lichen\">Lichen</option> <option value=\"Mammal\">Mammal</option> <option value=\"Mollusc\">Mollusc</option> <option value=\"Moss or liverwort\">Moss or liverwort</option> <option value=\"Nematode\">Nematode</option> <option value=\"Plankton\">Plankton</option> <option value=\"Plant\">Plant</option> <option value=\"Platyhelminthes\">Platyhelminthes</option> <option value=\"Protozoa\">Protozoa</option> <option value=\"Reptile\">Reptile</option> <option value=\"Virus\">Virus</option> </select> ";
// Exports
/* harmony default export */ const templates_SaTaxa = (SaTaxa_code);
;// CONCATENATED MODULE: ./editor/src/views/SaTaxaView.js



/* harmony default export */ const SaTaxaView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(templates_SaTaxa);
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('value'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/SaPhysicalState.tpl
// Module
var SaPhysicalState_code = "<select data-name=\"value\" class=\"editor-input\"> <option value=\"\" selected=\"selected\">- Add physical state -</option> <option value=\"Air dried\">Air dried</option> <option value=\"Chemical extract\">Chemical extract</option> <option value=\"Chilled (refrigerated)\">Chilled (refrigerated)</option> <option value=\"Fixed in formalin\">Fixed in formalin</option> <option value=\"Formalin-Fixed Paraffin-Embedded (FFPE) tissue\">Formalin-Fixed Paraffin-Embedded (FFPE) tissue</option> <option value=\"Freeze dried\">Freeze dried</option> <option value=\"Fresh\">Fresh</option> <option value=\"Frozen (-198 degrees C)\">Frozen (-198 degrees C)</option> <option value=\"Frozen (-20 degrees C)\">Frozen (-20 degrees C)</option> <option value=\"Frozen (-80 degrees C)\">Frozen (-80 degrees C)</option> <option value=\"Natural state\">Natural state</option> <option value=\"Oven dry\">Oven dry</option> <option value=\"Preserved\">Preserved</option> <option value=\"Preserved in alcohol\">Preserved in alcohol</option> <option value=\"Slide\">Slide</option> <option value=\"Taxidermy\">Taxidermy</option> <option value=\"Under liquid nitrogen\">Under liquid nitrogen</option> </select> ";
// Exports
/* harmony default export */ const templates_SaPhysicalState = (SaPhysicalState_code);
;// CONCATENATED MODULE: ./editor/src/views/SaPhysicalStateView.js



/* harmony default export */ const SaPhysicalStateView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(templates_SaPhysicalState);
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('value'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/SaTissue.tpl
// Module
var SaTissue_code = "<select data-name=\"value\" class=\"editor-input\"> <option value=\"\" selected=\"selected\">- Add tissue -</option> <option value=\"Bone\">Bone</option> <option value=\"Brain\">Brain</option> <option value=\"Egg\">Egg</option> <option value=\"Fat\">Fat</option> <option value=\"Feather\">Feather</option> <option value=\"Fur\">Fur</option> <option value=\"Gut contents\">Gut contents</option> <option value=\"Heart\">Heart</option> <option value=\"Homogenised whole sample\">Homogenised whole sample</option> <option value=\"Kidney\">Kidney</option> <option value=\"Liver\">Liver</option> <option value=\"Lung\">Lung</option> <option value=\"Lymph node\">Lymph node</option> <option value=\"Muscle\">Muscle</option> <option value=\"Nerve/spinal cord\">Nerve/spinal cord</option> <option value=\"Plasma\">Plasma</option> <option value=\"Serum\">Serum</option> <option value=\"Skin \">Skin </option> <option value=\"Spleen\">Spleen</option> <option value=\"Trachea\">Trachea</option> <option value=\"Whole blood\">Whole blood</option> <option value=\"Whole body\">Whole body</option> </select> ";
// Exports
/* harmony default export */ const templates_SaTissue = (SaTissue_code);
;// CONCATENATED MODULE: ./editor/src/views/SaTissueView.js



/* harmony default export */ const SaTissueView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(templates_SaTissue);
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('value'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/SaSpecimenType.tpl
// Module
var SaSpecimenType_code = "<select data-name=\"value\" class=\"editor-input\"> <option value=\"\" selected=\"selected\">- Add specimen type -</option> <option value=\"Air\">Air</option> <option value=\"DNA\">DNA</option> <option value=\"Ectoparasite\">Ectoparasite</option> <option value=\"Endoparasite\">Endoparasite</option> <option value=\"Fossil\">Fossil</option> <option value=\"Fresh water\">Fresh water</option> <option value=\"Gas\">Gas</option> <option value=\"Ice core\">Ice core</option> <option value=\"Pathogen\">Pathogen</option> <option value=\"Rain water\">Rain water</option> <option value=\"RNA\">RNA</option> <option value=\"Rock\">Rock</option> <option value=\"Sea water\">Sea water</option> <option value=\"Sediment\">Sediment</option> <option value=\"Seed\">Seed</option> <option value=\"Soil\">Soil</option> <option value=\"Surface water\">Surface water</option> <option value=\"Vegetation\">Vegetation</option> </select> ";
// Exports
/* harmony default export */ const templates_SaSpecimenType = (SaSpecimenType_code);
;// CONCATENATED MODULE: ./editor/src/views/SaSpecimenTypeView.js



/* harmony default export */ const SaSpecimenTypeView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(templates_SaSpecimenType);
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('value'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/service-agreement/Author.tpl
// Module
var Author_code = "<div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Name\">Name</label> </div> <div class=\"col-sm-4\"> <input data-name=\"individualName\" class=\"editor-input\" id=\"contacts<%= data.index %>Name\" value=\"<%= data.individualName %>\"> </div> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Email\">Email</label> </div> <div class=\"col-sm-4\"> <input data-name=\"email\" class=\"editor-input\" id=\"contacts<%= data.index %>Email\" value=\"<%= data.email %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Organisation\">Affiliation</label> </div> <div class=\"col-sm-4\"> <input data-name=\"organisationName\" class=\"editor-input\" id=\"contacts<%= data.index %>Organisation\" value=\"<%= data.organisationName %>\"> </div> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>nameIdentifier\">ORCID</label> </div> <div class=\"col-sm-4\"> <input data-name=\"nameIdentifier\" placeholder=\"https://orcid.org/0000-0000-0000-0000\" class=\"editor-input\" id=\"contacts<%= data.index %>nameIdentifier\" value=\"<%= data.nameIdentifier %>\"> </div> </div>";
// Exports
/* harmony default export */ const service_agreement_Author = (Author_code);
;// CONCATENATED MODULE: ./editor/src/views/service-agreement/AuthorView.js



/* harmony default export */ const AuthorView = (ObjectInputView.extend({
  modify: function modify(event) {
    this.template = index_all/* default.template */.ZP.template(service_agreement_Author);
    ObjectInputView.prototype.modify.call(this, event);
    return this.model.set('role', 'author');
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/service-agreement/Category.tpl
// Module
var Category_code = "<select data-name=\"value\" class=\"editor-input\"> <option value=\"Environmental data\">Environmental Data</option> <option value=\"Information product\">Information Product</option> </select> ";
// Exports
/* harmony default export */ const Category = (Category_code);
;// CONCATENATED MODULE: ./editor/src/views/service-agreement/CategoryView.js



/* harmony default export */ const CategoryView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(Category);
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('value'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/service-agreement/EndUserLicence.tpl
// Module
var EndUserLicence_code = "<div> <label class=\"radio-inline\"> <input class=\"ogl\" type=\"radio\" name=\"choice\"> Open Government Licence </label> <label class=\"radio-inline\"> <input class=\"other\" type=\"radio\" name=\"choice\"> Other </label> </div> <div class=\"resourceConstraint hidden\"> <textarea data-name=\"value\" class=\"value editor-textarea\" type=\"text\" rows=\"2\" placeholder=\"Please specify the licence\"></textarea> </div>";
// Exports
/* harmony default export */ const EndUserLicence = (EndUserLicence_code);
;// CONCATENATED MODULE: ./editor/src/views/service-agreement/EndUserLicenceView.js



/* harmony default export */ const EndUserLicenceView = (ObjectInputView.extend({
  events: {
    'change .ogl': 'setOgl',
    'change .other': 'setOther',
    'change .value': 'setValue',
    'change .uri': 'setUri'
  },
  initialize: function initialize(options) {
    this.template = index_all/* default.template */.ZP.template(EndUserLicence);
    ObjectInputView.prototype.initialize.call(this, options);
    this.$resourceConstraint = this.$('.resourceConstraint');
    var hasUri = this.model.has('uri');
    var hasValue = this.model.has('value');

    if (hasUri || hasValue) {
      if (hasUri && this.model.get('uri') === 'https://eidc.ceh.ac.uk/licences/OGL/plain') {
        return this.$('input.ogl').prop('checked', true);
      } else {
        this.$('input.other').prop('checked', true);
        this.$resourceConstraint.removeClass('hidden');

        if (hasValue) {
          return this.$('.value').val(this.model.get('value'));
        }
      }
    } else {
      return this.$('input.ogl').prop('checked', true).change();
    }
  },
  setOgl: function setOgl() {
    this.$resourceConstraint.addClass('hidden');
    return this.model.set({
      value: 'This resource is available under the terms of the Open Government Licence',
      code: 'license',
      uri: 'https://eidc.ceh.ac.uk/licences/OGL/plain'
    });
  },
  setOther: function setOther() {
    this.$resourceConstraint.removeClass('hidden');
    this.model.unset('uri');
    return this.model.unset('value');
  },
  setValue: function setValue(event) {
    return this.model.set({
      code: 'license',
      value: event.target.value
    });
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/service-agreement/File.tpl
// Module
var File_code = "<div class=\"row\"> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"files<%= data.index %>Name\">Name</label> </div> <div class=\"col-sm-3\"> <input data-name=\"name\" class=\"editor-input\" id=\"files<%= data.index %>Name\" value=\"<%= data.name %>\"> </div> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"files<%= data.index %>Format\">Format</label> </div> <div class=\"col-sm-3\"> <input data-name=\"format\" class=\"editor-input\" id=\"files<%= data.index %>Format\" value=\"<%= data.format %>\"> </div> <div class=\"col-sm-1\"> <label class=\"control-label\" for=\"files<%= data.index %>Size\">Size</label> </div> <div class=\"col-sm-3\"> <input data-name=\"size\" class=\"editor-input\" id=\"files<%= data.index %>Size\" value=\"<%= data.size %>\" placeholder=\"e.g. 32Mb or 0.4Gb\"> </div> </div>";
// Exports
/* harmony default export */ const File = (File_code);
;// CONCATENATED MODULE: ./editor/src/views/service-agreement/FileView.js



/* harmony default export */ const FileView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(File)
}));
;// CONCATENATED MODULE: ./editor/src/templates/service-agreement/RightsHolder.tpl
// Module
var RightsHolder_code = "<div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Organisation\">Affiliation</label> </div> <div class=\"col-sm-10\"> <input data-name=\"organisationName\" class=\"editor-input\" id=\"contacts<%= data.index %>Organisation\" value=\"<%= data.organisationName %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>organisationIdentifier\">Organisation's RoR</label> </div> <div class=\"col-sm-4\"> <input data-name=\"organisationIdentifier\" class=\"editor-input\" id=\"contacts<%= data.index %>organisationIdentifier\" value=\"<%= data.organisationIdentifier %>\"> </div> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"contacts<%= data.index %>Name\">Name</label> </div> <div class=\"col-sm-4\"> <input data-name=\"individualName\" class=\"editor-input\" id=\"contacts<%= data.index %>Name\" value=\"<%= data.individualName %>\"> </div> </div> ";
// Exports
/* harmony default export */ const service_agreement_RightsHolder = (RightsHolder_code);
;// CONCATENATED MODULE: ./editor/src/views/service-agreement/RightsHolderView.js



/* harmony default export */ const RightsHolderView = (ObjectInputView.extend({
  modify: function modify(event) {
    this.template = index_all/* default.template */.ZP.template(service_agreement_RightsHolder);
    ObjectInputView.prototype.modify.call(this, event);
    return this.model.set('role', 'rightsHolder');
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/service-agreement/SupportingDoc.tpl
// Module
var SupportingDoc_code = "<div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"supportingDocs<%= data.index %>Filename\">Filename</label> </div> <div class=\"col-sm-10\"> <input data-name=\"filename\" class=\"editor-input\" id=\"supportingDocs<%= data.index %>Filename\" value=\"<%= data.filename %>\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label class=\"control-label\" for=\"supportingDocs<%= data.index %>Content\">Content</label> </div> <div class=\"col-sm-10\"> <textarea data-name=\"content\" id=\"supportingDocs<%= data.index %>Content\" class=\"editor-textarea\" rows=\"5\"><%= data.content %></textarea> </div> </div>";
// Exports
/* harmony default export */ const service_agreement_SupportingDoc = (SupportingDoc_code);
;// CONCATENATED MODULE: ./editor/src/views/service-agreement/SupportingDocView.js



/* harmony default export */ const SupportingDocView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(service_agreement_SupportingDoc)
}));
;// CONCATENATED MODULE: ./editor/src/templates/service-agreement/TextOnly.tpl
// Module
var TextOnly_code = "<%= data.text %>";
// Exports
/* harmony default export */ const TextOnly = (TextOnly_code);
;// CONCATENATED MODULE: ./editor/src/views/service-agreement/TextOnlyView.js



/* harmony default export */ const TextOnlyView = (SingleView/* default.extend */.Z.extend({
  className: 'component component--textonly',
  initialize: function initialize(options) {
    this.template = index_all/* default.template */.ZP.template(TextOnly);
    SingleView/* default.prototype.initialize.call */.Z.prototype.initialize.call(this, options);
    return this.render();
  },
  render: function render() {
    SingleView/* default.prototype.render.apply */.Z.prototype.render.apply(this);
    return this.$('.dataentry').append(this.template({
      data: index_all/* default.extend */.ZP.extend({}, this.data, {
        value: this.model.get
      })
    }));
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/InfrastructureCategory.tpl
// Module
var InfrastructureCategory_code = "<select data-name=\"value\" class=\"editor-input\" id=\"input-infrastructureCategory\" <%= data.disabled%>> <option value=\"\"> -- Choose an option -- </option> <optgroup label=\"Environmental observatories\"> <option value=\"instrumentedSites\">Instrumented sites</option> <option value=\"surveys\">Surveys</option> <option value=\"wildlifeSchemes\">Wildlife monitoring schemes</option> <option value=\"discoveryCollections\">Discovery collections</option> <option value=\"mobilePlatforms\">Mobile observing platforms</option> </optgroup> <optgroup label=\"Environmental experiment platforms\"> <option value=\"controlledPlatforms\">Controlled environment platforms</option> <option value=\"fieldPlatforms\">Field research platforms</option> </optgroup> <optgroup label=\"Environmental analysis\"> <option value=\"labsAnalysis\">Analysis labs</option> <option value=\"labsTest\">Test labs</option> </optgroup> </select> ";
// Exports
/* harmony default export */ const templates_InfrastructureCategory = (InfrastructureCategory_code);
;// CONCATENATED MODULE: ./editor/src/views/InfrastructureCategoryView.js



/* harmony default export */ const InfrastructureCategoryView = (ObjectInputView.extend({
  initialize: function initialize() {
    this.template = index_all/* default.template */.ZP.template(templates_InfrastructureCategory);
    ObjectInputView.prototype.initialize.apply(this);
    return this.listenTo(this.model, 'change:infrastructureCategory', function (model, value) {
      return this.model.set('type', value.value);
    });
  },
  render: function render() {
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('value'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/InfrastructureChallenge.tpl
// Module
var InfrastructureChallenge_code = "<select data-name=\"value\" class=\"editor-input\"> <option value=\"\" selected=\"selected\">--</option> <option value=\"Climate change: mitigation\">Climate change: mitigation</option> <option value=\"Climate change: resilience\">Climate change: resilience</option> <option value=\"Pollution\">Pollution</option> <option value=\"Sustainable ecosystems: biodiversity net gain\">Sustainable ecosystems: biodiversity net gain</option> </select> ";
// Exports
/* harmony default export */ const templates_InfrastructureChallenge = (InfrastructureChallenge_code);
;// CONCATENATED MODULE: ./editor/src/views/InfrastructureChallengeView.js



/* harmony default export */ const InfrastructureChallengeView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(templates_InfrastructureChallenge);
    ObjectInputView.prototype.render.apply(this);
    this.$('select').val(this.model.get('value'));
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/InfrastructureOnlineLink.tpl
// Module
var InfrastructureOnlineLink_code = "<div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Name\">Name/description</label> </div> <div class=\"col-sm-10\"> <input data-name=\"name\" id=\"<%= data.modelAttribute %><%= data.index %>Name\" class=\"editor-input\" value=\"<%= data.name %>\" autocomplete=\"off\"> </div> </div> <div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"<%= data.modelAttribute %><%= data.index %>URL\">Address</label> </div> <div class=\"col-sm-10\"> <input data-name=\"url\" id=\"<%= data.modelAttribute %><%= data.index %>URL\" class=\"editor-input\" value=\"<%= data.url %>\" autocomplete=\"off\"> </div> </div><div class=\"row\"> <div class=\"col-sm-2\"> <label for=\"<%= data.modelAttribute %><%= data.index %>Function\">Purpose</label> </div> <div class=\"col-sm-10\"> <select data-name=\"function\" id=\"<%= data.modelAttribute %><%= data.index %>Function\" class=\"function editor-input\"> <option value=\"\" selected=\"selected\"> </option> <option value=\"website\">Website</option> <option value=\"image\">Image</option> </select> </div> </div>";
// Exports
/* harmony default export */ const InfrastructureOnlineLink = (InfrastructureOnlineLink_code);
;// CONCATENATED MODULE: ./editor/src/views/InfrastructureOnlineLinkView.js



/* harmony default export */ const InfrastructureOnlineLinkView = (ObjectInputView.extend({
  render: function render() {
    this.template = index_all/* default.template */.ZP.template(InfrastructureOnlineLink);
    ObjectInputView.prototype.render.apply(this);
    return this.$('select.function').val(this.model.get('function'));
  }
}));
;// CONCATENATED MODULE: ./editor/src/templates/Review.tpl
// Module
var Review_code = "<div class=\"row\"> <div class=\"col-xs-2 col-sm-1\"> <label for=\"<%= data.modelAttribute %><%= data.index %>_date\">Date</label> </div> <div class=\"col-xs-10 col-sm-4 col-md-3\"> <input data-name=\"reviewDate\" id=\"<%= data.modelAttribute %><%= data.index %>_date\" class=\"editor-input\" value=\"<%= data.reviewDate %>\" type=\"date\"> </div> <div class=\"col-xs-2 col-sm-2 col-md-1\"> <label for=\"<%= data.modelAttribute %><%= data.index %>_process\">Process</label> </div> <div class=\"col-xs-10 col-sm-5 col-md-7\"> <input data-name=\"reviewProcess\" id=\"<%= data.modelAttribute %><%= data.index %>_process\" class=\"editor-input\" value=\"<%= data.reviewProcess %>\"> </div> </div>";
// Exports
/* harmony default export */ const Review = (Review_code);
;// CONCATENATED MODULE: ./editor/src/views/ReviewView.js



/* harmony default export */ const ReviewView = (ObjectInputView.extend({
  template: index_all/* default.template */.ZP.template(Review)
}));
;// CONCATENATED MODULE: ./editor/src/views/index.js










































































;// CONCATENATED MODULE: ./editor/src/editors/MonitoringEditorView.js



/* harmony default export */ const MonitoringEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    this.sections = [{
      label: 'One',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title',
        helpText: "\n<p>Title help</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Description',
        rows: 17,
        helpText: "\n<p>Description help</p>\n"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
// EXTERNAL MODULE: ./editor/src/SelectView.js + 1 modules
var SelectView = __webpack_require__(8751);
;// CONCATENATED MODULE: ./editor/src/editors/ModelEditorView.js




/* harmony default export */ const ModelEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    this.sections = [{
      label: 'Common',
      title: 'Common information',
      views: [new SelectView/* default */.Z({
        model: this.model,
        modelAttribute: 'type',
        label: 'Type',
        options: [{
          value: 'modelApplication',
          label: 'Model use'
        }, {
          value: 'caseStudy',
          label: 'Case Study'
        }, {
          value: 'model',
          label: 'Model'
        }]
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Description',
        rows: 17
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'contact',
        label: 'Contact',
        ObjectInputView: ShortContactView
      }), new ParentView({
        model: this.model,
        modelAttribute: 'resourceIdentifiers',
        label: 'Resource Identifiers',
        ObjectInputView: ResourceIdentifierView,
        helpText: "<p>A unique string or number used to identify the resource.</p>\n<p> The codespace identifies the context in which the code is unique.</p>"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'relationships',
        label: 'Relationships',
        ObjectInputView: RelationshipView,
        multiline: true,
        options: [{
          value: 'http://purl.org/dc/terms/references',
          label: 'References'
        }]
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView
      })]
    }, {
      label: 'Model',
      title: 'Model only',
      views: [new ParentStringView({
        model: this.model,
        modelAttribute: 'keyReferences',
        label: 'Key References'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'version',
        label: 'Version'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'releaseDate',
        label: 'Release Date'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'license',
        label: 'License'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'operatingRequirements',
        label: 'Operating Requirements'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'applicationType',
        label: 'Application Type'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'userInterface',
        label: 'User Interface'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'supportAvailable',
        label: 'Support Available'
      }), new SelectView/* default */.Z({
        model: this.model,
        modelAttribute: 'applicationScale',
        label: 'Application Scale',
        options: [{
          value: 'Plot',
          label: 'Plot'
        }, {
          value: 'Field',
          label: 'Field'
        }, {
          value: 'Farm',
          label: 'Farm'
        }, {
          value: 'River reach',
          label: 'River reach'
        }, {
          value: 'Catchment',
          label: 'Catchment'
        }, {
          value: 'Landscape',
          label: 'Landscape'
        }, {
          value: 'Regional',
          label: 'Regional'
        }, {
          value: 'National',
          label: 'National'
        }, {
          value: 'Global',
          label: 'Global'
        }]
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'geographicalRestrictions',
        label: 'Geographical Restrictions'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'temporalResolution',
        label: 'Temporal Resolution'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'spatialResolution',
        label: 'Spatial Resolution'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'primaryPurpose',
        label: 'Primary Purpose'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'keyOutputVariables',
        label: 'Key Output Variables'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'keyInputVariables',
        label: 'Key Input Variables'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'calibrationRequired',
        label: 'Calibration Required'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'modelStructure',
        label: 'Model Structure'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'modelParameterisation',
        label: 'Model Parameterisation'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'inputDataAvailableOnCatalogue',
        label: 'Input Data Available on CaMMP Catalogue?'
      }), new ParentStringView({
        model: this.model,
        modelAttribute: 'inputData',
        label: 'Input Data'
      }), new ParentStringView({
        model: this.model,
        modelAttribute: 'outputData',
        label: 'Output Data'
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'documentation',
        label: 'Documentation',
        ObjectInputView: LinkView
      }), new CheckboxView({
        model: this.model,
        modelAttribute: 'developerTesting',
        label: 'Developer Testing'
      }), new CheckboxView({
        model: this.model,
        modelAttribute: 'internalPeerReview',
        label: 'Internal Peer Review'
      }), new CheckboxView({
        model: this.model,
        modelAttribute: 'externalPeerReview',
        label: 'External Peer Review'
      }), new CheckboxView({
        model: this.model,
        modelAttribute: 'useOfVersionControl',
        label: 'Use of Version Control'
      }), new CheckboxView({
        model: this.model,
        modelAttribute: 'internalModelAudit',
        label: 'Internal Model Audit'
      }), new CheckboxView({
        model: this.model,
        modelAttribute: 'externalModelAudit',
        label: 'External Model Audit'
      }), new CheckboxView({
        model: this.model,
        modelAttribute: 'qualityAssuranceGuidelinesAndChecklists',
        label: 'Quality Assurance Guidelines and Checklists'
      }), new CheckboxView({
        model: this.model,
        modelAttribute: 'governance',
        label: 'Governance'
      }), new CheckboxView({
        model: this.model,
        modelAttribute: 'transparency',
        label: 'Transparency'
      }), new CheckboxView({
        model: this.model,
        modelAttribute: 'periodicReview',
        label: 'Periodic Review'
      })]
    }, {
      label: 'Model use',
      title: 'Model use only',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'relevanceToCaMMP',
        label: 'Relevance to CaMMP'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'date',
        label: 'Date'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'studySite',
        label: 'Study Site'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'studyScale',
        label: 'Study Scale'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'objective',
        label: 'Objective'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'funderDetails',
        label: 'Funder Details'
      }), new ParentStringView({
        model: this.model,
        modelAttribute: 'inputData',
        label: 'Input Data'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'multipleModelsUsed',
        label: 'Multiple Models Used'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'multipleModelLinkages',
        label: 'Multiple Model Linkages'
      }), new ParentView({
        model: this.model,
        modelAttribute: 'models',
        label: 'Models Used',
        ObjectInputView: ModelApplicationModelView,
        multiline: true
      }), new SelectView/* default */.Z({
        model: this.model,
        modelAttribute: 'sensitivity',
        label: 'Sensitivity',
        options: [{
          value: 'strongly disagree',
          label: 'strongly disagree'
        }, {
          value: 'disagree',
          label: 'disagree'
        }, {
          value: 'neither agree nor disagree',
          label: 'neither agree nor disagree'
        }, {
          value: 'agree',
          label: 'agree'
        }, {
          value: 'strongly agree',
          label: 'strongly agree'
        }]
      }), new SelectView/* default */.Z({
        model: this.model,
        modelAttribute: 'uncertainty',
        label: 'Uncertainty',
        options: [{
          value: 'strongly disagree',
          label: 'strongly disagree'
        }, {
          value: 'disagree',
          label: 'disagree'
        }, {
          value: 'neither agree nor disagree',
          label: 'neither agree nor disagree'
        }, {
          value: 'agree',
          label: 'agree'
        }, {
          value: 'strongly agree',
          label: 'strongly agree'
        }]
      }), new SelectView/* default */.Z({
        model: this.model,
        modelAttribute: 'validation',
        label: 'Validation',
        options: [{
          value: 'strongly disagree',
          label: 'strongly disagree'
        }, {
          value: 'disagree',
          label: 'disagree'
        }, {
          value: 'neither agree nor disagree',
          label: 'neither agree nor disagree'
        }, {
          value: 'agree',
          label: 'agree'
        }, {
          value: 'strongly agree',
          label: 'strongly agree'
        }]
      }), new SelectView/* default */.Z({
        model: this.model,
        modelAttribute: 'modelEasyToUse',
        label: 'Model Easy to Use',
        options: [{
          value: 'strongly disagree',
          label: 'strongly disagree'
        }, {
          value: 'disagree',
          label: 'disagree'
        }, {
          value: 'neither agree nor disagree',
          label: 'neither agree nor disagree'
        }, {
          value: 'agree',
          label: 'agree'
        }, {
          value: 'strongly agree',
          label: 'strongly agree'
        }]
      }), new SelectView/* default */.Z({
        model: this.model,
        modelAttribute: 'userManualUseful',
        label: 'User Manual Useful',
        options: [{
          value: 'strongly disagree',
          label: 'strongly disagree'
        }, {
          value: 'disagree',
          label: 'disagree'
        }, {
          value: 'neither agree nor disagree',
          label: 'neither agree nor disagree'
        }, {
          value: 'agree',
          label: 'agree'
        }, {
          value: 'strongly agree',
          label: 'strongly agree'
        }]
      }), new SelectView/* default */.Z({
        model: this.model,
        modelAttribute: 'dataObtainable',
        label: 'Data Obtainable',
        options: [{
          value: 'strongly disagree',
          label: 'strongly disagree'
        }, {
          value: 'disagree',
          label: 'disagree'
        }, {
          value: 'neither agree nor disagree',
          label: 'neither agree nor disagree'
        }, {
          value: 'agree',
          label: 'agree'
        }, {
          value: 'strongly agree',
          label: 'strongly agree'
        }]
      }), new SelectView/* default */.Z({
        model: this.model,
        modelAttribute: 'modelUnderstandable',
        label: 'Model Understandable',
        options: [{
          value: 'strongly disagree',
          label: 'strongly disagree'
        }, {
          value: 'disagree',
          label: 'disagree'
        }, {
          value: 'neither agree nor disagree',
          label: 'neither agree nor disagree'
        }, {
          value: 'agree',
          label: 'agree'
        }, {
          value: 'strongly agree',
          label: 'strongly agree'
        }]
      })]
    }, {
      label: 'Case Study',
      title: 'Case Study only',
      views: [new SingleObjectView({
        model: this.model,
        modelAttribute: 'caseStudy',
        label: 'Case Study Link',
        ObjectInputView: LinkView
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
// EXTERNAL MODULE: ./node_modules/leaflet/dist/leaflet-src.js
var leaflet_src = __webpack_require__(5243);
var leaflet_src_default = /*#__PURE__*/__webpack_require__.n(leaflet_src);
// EXTERNAL MODULE: ./node_modules/leaflet-draw/dist/leaflet.draw.js
var leaflet_draw = __webpack_require__(1787);
;// CONCATENATED MODULE: ./editor/src/geometryMap/BoundingBox.tpl
// Module
var BoundingBox_code = "<head> <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet-draw@1.0.2/dist/leaflet.draw-src.css\"/> </head> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <label for=\"boundingBox<%= data.index %>WestBoundLongitude\">West Bounding Longitude</label> <input data-name=\"westBoundLongitude\" id=\"boundingBox<%= data.index %>WestBoundLongitude\" class=\"editor-input\" value=\"<%= data.westBoundLongitude %>\"> </div> <div class=\"col-sm-2 col-lg-2\"> <label for=\"boundingBox<%= data.index %>NorthBoundLatitude\">North Bounding Latitude</label> <input data-name=\"northBoundLatitude\" id=\"boundingBox<%= data.index %>NorthBoundLatitude\" class=\"editor-input\" value=\"<%= data.northBoundLatitude %>\"> <label for=\"boundingBox<%= data.index %>SouthBoundLatitude\">South Bounding Latitude</label> <input data-name=\"southBoundLatitude\" id=\"boundingBox<%= data.index %>SouthBoundLatitude\" class=\"editor-input\" value=\"<%= data.southBoundLatitude %>\"> </div> <div class=\"col-sm-2 col-lg-2\"> <label for=\"boundingBox<%= data.index %>EastBoundLongitude\">East Bounding Longitude</label> <input data-name=\"eastBoundLongitude\" id=\"boundingBox<%= data.index %>EastBoundLongitude\" class=\"editor-input\" value=\"<%= data.eastBoundLongitude %>\"> <br> <button id=\"update\" class=\"editor-button\" title=\"Show/Update map\"><span class=\"fas fa-globe\" aria-hidden=\"true\"></span></button> </div> <div class=\"col-sm-6 col-lg-6\"> <div class=\"row\"> <div class=\"map\" style=\"width:500px;height:500px\"></div> </div> <div class=\"row\"> <span class=\"extentName\"><%= data.extentName %></span> <span class=\"extentUri\"><%= data.extentUri %></span> </div> </div> </div> ";
// Exports
/* harmony default export */ const BoundingBox = (BoundingBox_code);
;// CONCATENATED MODULE: ./editor/src/geometryMap/BoundingBoxView.js







/* harmony default export */ const BoundingBoxView = (ObjectInputView.extend({
  events: {
    'click #update': 'viewMap'
  },
  initialize: function initialize() {
    this.template = index_all/* default.template */.ZP.template(BoundingBox);
    this.render();
    this.listenTo(this.model, 'change:westBoundLongitude', function (model, value) {
      return this.$('#boundingBoxWestBoundLongitude').val(value);
    });
    this.listenTo(this.model, 'change:southBoundLatitude', function (model, value) {
      return this.$('#boundingBoxSouthBoundLatitude').val(value);
    });
    this.listenTo(this.model, 'change:eastBoundLongitude', function (model, value) {
      return this.$('#boundingBoxEastBoundLongitude').val(value);
    });
    this.listenTo(this.model, 'change:northBoundLatitude', function (model, value) {
      return this.$('#boundingBoxNorthBoundLatitude').val(value);
    });
  },
  createMap: function createMap() {
    this.map = new (leaflet_src_default()).Map(this.$('.map')[0], {
      center: new (leaflet_src_default()).LatLng(51.513, -0.09),
      zoom: 4
    });
    this.drawnItems = leaflet_src_default().featureGroup();

    if (this.model.get('northBoundLatitude') && this.model.get('westBoundLongitude') && this.model.get('southBoundLatitude') && this.model.get('eastBoundLongitude')) {
      this.shapeDrawn = true;
      this.rectangle = leaflet_src_default().rectangle([[[this.model.get('northBoundLatitude'), this.model.get('westBoundLongitude')], [this.model.get('southBoundLatitude'), this.model.get('eastBoundLongitude')]]]);
      this.drawnItems.addLayer(this.rectangle);
      this.rectangle.editing.enable();
      this.drawControl = this.deleteToolbar();
    } else {
      this.drawControl = this.createToolbar();
      this.shapeDrawn = false;
    }

    if (this.shapeDrawn === true) {
      this.map.setView(this.rectangle.getBounds().getCenter(), 4);
    }

    this.drawnItems.addTo(this.map);
    var baseMaps = {
      Map: leaflet_src_default().tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }),
      Satellite: leaflet_src_default().tileLayer('http://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      })
    };
    leaflet_src_default().control.layers(baseMaps, {
      Drawlayer: this.drawnItems
    }, {
      position: 'topright',
      collapsed: false
    }).addTo(this.map);
    this.map.addControl(this.drawControl);
    baseMaps.Map.addTo(this.map);
    this.listenTo(this.map, (leaflet_src_default()).Draw.Event.CREATED, function (event) {
      if (this.shapeDrawn === false) {
        this.rectangle = event.layer;
        this.rectangle.editing.enable();
        this.drawnItems.addLayer(this.rectangle);
        this.shapeDrawn = true;
        this.map.removeControl(this.drawControl);
        this.drawControl = this.deleteToolbar();
        this.map.addControl(this.drawControl);
        this.model.setBounds(event.layer.getBounds());
      }
    });
    this.listenTo(this.map, (leaflet_src_default()).Draw.Event.DELETED, function () {
      this.shapeDrawn = false;
      this.map.removeControl(this.drawControl);
      this.drawControl = this.createToolbar();
      this.map.addControl(this.drawControl);
      this.model.clearBounds();
    });
    this.listenTo(this.map, (leaflet_src_default()).Draw.Event.EDITMOVE, function (event) {
      var layer = event.layer;
      this.model.setBounds(layer.getBounds());
    });
    this.listenTo(this.map, (leaflet_src_default()).Draw.Event.EDITRESIZE, function (event) {
      var layer = event.layer;
      this.model.setBounds(layer.getBounds());
    });
  },
  createToolbar: function createToolbar() {
    return new (leaflet_src_default()).Control.Draw({
      position: 'topleft',
      edit: {
        featureGroup: this.drawnItems,
        edit: false,
        remove: false
      },
      draw: {
        rectangle: true,
        polygon: false,
        polyline: false,
        marker: false,
        circle: false,
        circlemarker: false
      }
    });
  },
  deleteToolbar: function deleteToolbar() {
    return new (leaflet_src_default()).Control.Draw({
      position: 'topleft',
      edit: {
        featureGroup: this.drawnItems,
        edit: false,
        remove: true
      },
      draw: {
        rectangle: false,
        polygon: false,
        polyline: false,
        marker: false,
        circle: false,
        circlemarker: false
      }
    });
  },
  viewMap: function viewMap() {
    if (this.map) {
      this.map.off();
      this.map.remove();
    }

    this.createMap();
  },
  render: function render() {
    ObjectInputView.prototype.render.apply(this);
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/geometryMap/BoundingBox.js


/* harmony default export */ const geometryMap_BoundingBox = (backbone_default().Model.extend({
  hasBoundingBox: function hasBoundingBox() {
    return this.has('westBoundLongitude') && this.has('southBoundLatitude') && this.has('eastBoundLongitude') && this.has('northBoundLatitude');
  },
  getBoundingBox: function getBoundingBox() {
    return [[[this.get('northBoundLatitude'), this.get('westBoundLongitude')], [this.get('southBoundLatitude'), this.get('eastBoundLongitude')]]];
  },
  setBounds: function setBounds(bounds) {
    this.set('northBoundLatitude', bounds.getNorth().toFixed(3));
    this.set('eastBoundLongitude', bounds.getEast().toFixed(3));
    this.set('southBoundLatitude', bounds.getSouth().toFixed(3));
    this.set('westBoundLongitude', bounds.getWest().toFixed(3));
  },
  clearBounds: function clearBounds() {
    this.set('northBoundLatitude', null);
    this.set('eastBoundLongitude', null);
    this.set('southBoundLatitude', null);
    this.set('westBoundLongitude', null);
  },
  validate: function validate(attrs) {
    var labels = {
      westBoundLongitude: 'West Bounding Longitude',
      eastBoundLongitude: 'East Bounding Longitude',
      northBoundLatitude: 'North Bounding Latitude',
      southBoundLatitude: 'South Bounding Latitude'
    };
    var errors = [];

    var isStringANumber = function isStringANumber(input) {
      return (// coerce attribute to a number with + then check if operation produced NaN
        !isNaN(+input)
      );
    };

    var isGreater = function isGreater(first, second) {
      if (isStringANumber(first) && isStringANumber(second)) {
        first = parseFloat(first);
        second = parseFloat(second);
        return first > second;
      }
    };

    var isOutOfRange = function isOutOfRange(input, min, max) {
      if (isStringANumber(input)) {
        input = parseFloat(input);
        return !(input <= max && input >= min);
      }
    };

    index_all/* default.chain */.ZP.chain(attrs).keys().each(function (key) {
      if (!isStringANumber(attrs[key])) {
        return errors.push({
          message: "".concat(labels[key], " needs to be a number")
        });
      }
    });

    if (isGreater(attrs.westBoundLongitude, attrs.eastBoundLongitude)) {
      errors.push({
        message: 'West Bounding Longitude should be less the East Bounding Longitude'
      });
    }

    if (isGreater(attrs.southBoundLatitude, attrs.northBoundLatitude)) {
      errors.push({
        message: 'South Bounding Longitude should be less the North Bounding Longitude'
      });
    }

    if (isOutOfRange(attrs.westBoundLongitude, -180, 180)) {
      errors.push({
        message: 'West Bounding Longitude should be between -180&deg; and 180&deg;'
      });
    }

    if (isOutOfRange(attrs.eastBoundLongitude, -180, 180)) {
      errors.push({
        message: 'East Bounding Longitude should be between -180&deg; and 180&deg;'
      });
    }

    if (isOutOfRange(attrs.northBoundLatitude, -90, 90)) {
      errors.push({
        message: 'North Bounding Longitude should be between -90&deg; and 90&deg;'
      });
    }

    if (isOutOfRange(attrs.southBoundLatitude, -90, 90)) {
      errors.push({
        message: 'South Bounding Longitude should be between -90&deg; and 90&deg;'
      });
    }

    if (index_all/* default.isEmpty */.ZP.isEmpty(errors)) {// return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
    } else {
      return errors;
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/CehModelEditorView.js






/* harmony default export */ const CehModelEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'model');
    }

    this.sections = [{
      label: 'Basic info',
      title: 'Basic info',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title',
        helpText: "\n<p>Name of the model</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Model description',
        rows: 7,
        helpText: "\n<p>Longer description of model e.g. development history, use to answer science questions, overview of structure</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'primaryPurpose',
        label: 'Primary purpose',
        rows: 3,
        helpText: "\n<p>Short phrase to describe primary aim of model</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'modelType',
        label: 'Model type',
        listAttribute: "<option value='Unknown' />\n<option value='Deterministic' />\n<option value='Stochastic' />",
        helpText: "\n<p>Type which best fits the model</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'currentModelVersion',
        label: 'Current model version',
        placeholderAttribute: 'e.g. 2.5.10',
        helpText: "\n<p>Most recent release version (if applicable)</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'releaseDate',
        typeAttribute: 'date',
        label: 'Release date',
        placeholderAttribute: 'yyyy-mm-dd',
        helpText: "\n<p>Date of release of current model version (if applicable)</p>\n"
      }), new PredefinedParentView({
        model: this.model,
        ModelType: models_Contact,
        modelAttribute: 'responsibleParties',
        label: 'Contacts',
        ObjectInputView: ContactView,
        multiline: true,
        predefined: {
          'SRO - UKCEH': {
            organisationName: 'UK Centre for Ecology & Hydrology',
            role: 'owner',
            organisationIdentifier: 'https://ror.org/00pggkr55'
          }
        },
        helpText: "\n<p>You <b>must</b> include one Senior Responsible Officer (SRO) - the person who is the \"owner\" and primary contact for the model</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView,
        helpText: "\n<p>Keywords for model discovery e.g. rainfall; species distribution; nitrogen deposition; global circulation model</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'onlineResources',
        label: 'Online resources',
        ObjectInputView: OnlineLinkView,
        multiline: true,
        listAttribute: "<option value='code'>Location of the model code such as GitHub repository</option>\n<option value='documentation'>Online documentation describing how to use the model</option>\n<option value='website'/>\n<option value='browseGraphic'>Image to display on metadata record</option>",
        helpText: "<p>Websites and online resources to access and further descibe the model</p>\n<p>You should include the location of the model code repository e.g. https://github.com/NERC-CEH/...</p>\n<p><b>If your model is not currently under version control and you are unsure about how to achieve this please talk to your Informatics Liaison representative.</b></p>"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'licenseType',
        label: 'License',
        listAttribute: "<option value='unknown' />\n<option value='open' />\n<option value='non-open' />",
        helpText: "\n<p>License type (open or non-open) under which the model is distributed</p>\n"
      })]
    }, {
      label: 'Input variables',
      title: 'Input variables',
      views: [new PredefinedParentView({
        model: this.model,
        ModelType: DataTypeSchema,
        modelAttribute: 'inputVariables',
        multiline: true,
        label: 'Input variables',
        ObjectInputView: DataTypeSchemaSimpleView,
        predefined: {
          'Boolean (true/false)': {
            type: 'boolean'
          },
          Date: {
            type: 'date'
          },
          'Date & time': {
            type: 'datetime'
          },
          'Decimal number': {
            type: 'number'
          },
          'Geographic point': {
            type: 'geopoint'
          },
          Integer: {
            type: 'integer'
          },
          Text: {
            type: 'string'
          },
          Time: {
            type: 'time',
            format: 'hh:mm:ss'
          },
          URI: {
            type: 'string',
            format: 'uri'
          },
          UUID: {
            type: 'string',
            format: 'uuid'
          }
        }
      })]
    }, {
      label: 'Output variables',
      title: 'Output variables',
      views: [new PredefinedParentView({
        model: this.model,
        ModelType: DataTypeSchema,
        modelAttribute: 'outputVariables',
        multiline: true,
        label: 'Output variables',
        ObjectInputView: DataTypeSchemaSimpleView,
        predefined: {
          'Boolean (true/false)': {
            type: 'boolean'
          },
          Date: {
            type: 'date'
          },
          'Date & time': {
            type: 'datetime'
          },
          'Decimal number': {
            type: 'number'
          },
          'Geographic point': {
            type: 'geopoint'
          },
          Integer: {
            type: 'integer'
          },
          Text: {
            type: 'string'
          },
          Time: {
            type: 'time',
            format: 'hh:mm:ss'
          },
          URI: {
            type: 'string',
            format: 'uri'
          },
          UUID: {
            type: 'string',
            format: 'uuid'
          }
        }
      })]
    }, {
      label: 'Spatio-temporal',
      title: 'Spatio-temporal details',
      views: [new PredefinedParentView({
        model: this.model,
        modelAttribute: 'boundingBoxes',
        ModelType: geometryMap_BoundingBox,
        label: 'Spatial extent',
        ObjectInputView: BoundingBoxView,
        multiline: true,
        predefined: {
          England: {
            northBoundLatitude: 55.812,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -6.452,
            extentName: 'England',
            extentUri: 'http://sws.geonames.org/6269131'
          },
          'Great Britain': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648,
            extentName: 'Great Britain'
          },
          'Northern Ireland': {
            northBoundLatitude: 55.313,
            eastBoundLongitude: -5.432,
            southBoundLatitude: 54.022,
            westBoundLongitude: -8.178,
            extentName: 'Northern Ireland',
            extentUri: 'http://sws.geonames.org/2641364'
          },
          Scotland: {
            northBoundLatitude: 60.861,
            eastBoundLongitude: -0.728,
            southBoundLatitude: 54.634,
            westBoundLongitude: -8.648,
            extentName: 'Scotland',
            extentUri: 'http://sws.geonames.org/2638360'
          },
          'United Kingdom': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648,
            extentName: 'United Kingdom',
            extentUri: 'http://sws.geonames.org/2635167'
          },
          Wales: {
            northBoundLatitude: 53.434,
            eastBoundLongitude: -2.654,
            southBoundLatitude: 51.375,
            westBoundLongitude: -5.473,
            extentName: 'Wales',
            extentUri: 'http://sws.geonames.org/2634895'
          },
          World: {
            northBoundLatitude: 90.00,
            eastBoundLongitude: 180.00,
            southBoundLatitude: -90.00,
            westBoundLongitude: -180.00
          }
        },
        helpText: "    <p>A bounding box representing the limits of the data resource's study area.</p>\n    <p>If you do not wish to reveal the exact location publicly (for example, if locations are sensitive) it is recommended that you generalise the location.</p>    "
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'spatialDomain',
        label: 'Spatial domain',
        placeholderAttribute: 'e.g. Parameterised for UK only or global',
        listAttribute: "    <option value='UK' />\n    <option value='Global' />    ",
        helpText: "\n    <p>Is the model only applicable to certain areas?</p>\n    "
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'spatialResolution',
        label: 'Spatial resolution',
        placeholderAttribute: 'e.g. 1km2 or 5m2;',
        helpText: "\n    <p>Spatial resolution at which model works or at which model outputs are generated (if applicable)</p>\n    "
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'temporalResolutionMin',
        label: 'Temporal resolution (min)',
        placeholderAttribute: 'e.g. 1 second or 10 days',
        helpText: "\n    <p>Minimum time step supported by the model (if applicable) </p>\n    "
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'temporalResolutionMax',
        label: 'Temporal resolution (max)',
        placeholderAttribute: 'e.g. annual or decadal ',
        helpText: "\n    <p>Maximum time step supported by the model (if applicable) </p>\n    "
      })]
    }, {
      label: 'Technical info',
      title: 'Technical info',
      views: [new TextareaView({
        model: this.model,
        modelAttribute: 'modelCalibration',
        label: 'Model calibration',
        rows: 7,
        helpText: "\n<p>Does the model need calibration before running? If so, what needs to be supplied to do this? (if applicable)</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'language',
        label: 'Language',
        placeholderAttribute: 'e.g. Python 2.7, C++, R 3.6',
        helpText: "\n<p>Language in which the model is written.  You should include the release number if relevant</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'compiler',
        label: 'Compiler',
        placeholderAttribute: 'e.g. C++ compiler',
        helpText: "\n<p>Compiler required (if applicable)</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'operatingSystem',
        label: 'Operating system',
        helpText: "\n<p>Operating system typically used to run the model</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'systemMemory',
        label: 'System memory',
        helpText: "\n<p>Memory required to run code (if known)</p>\n"
      })]
    }, {
      label: 'Quality',
      title: 'Quality assurance',
      views: [new SingleObjectView({
        model: this.model,
        modelAttribute: 'developerTesting',
        label: 'Developer testing',
        ObjectInputView: QaView,
        helpText: "\n<p>Use of a range of developer tools including parallel build and analytical review or sense check</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'internalPeerReview',
        label: 'Internal peer review',
        ObjectInputView: QaView,
        helpText: "\n<p>Obtaining a critical evaluation from a third party independent of the development of the model but from within the same organisation</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'externalPeerReview',
        label: 'External peer review',
        ObjectInputView: QaView,
        helpText: "\n<p>Formal or informal engagement of a third party to conduct critical evaluation from outside the organisation in which the model is being developed</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'internalModelAudit',
        label: 'Internal model audit',
        ObjectInputView: QaView,
        helpText: "\n<p>Formal audit of a model within the organisation, perhaps involving use of internal audit functions</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'externalModelAudit',
        label: 'External model audit',
        ObjectInputView: QaView,
        helpText: "\n<p>Formal engagement of external professional to conduct a critical evaluation of the model, perhaps involving audit professionals</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'qaGuidelinesAndChecklists',
        label: 'Quality assurance guidelines & checklists',
        ObjectInputView: QaView,
        helpText: "\n<p>Model development refers to department\u2019s guidance or other documented QA processes (e.g. third party publications)</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'governance',
        label: 'Governance',
        ObjectInputView: QaView,
        helpText: "\n<p>At least one of planning, design and/or sign-off of model for use is referred to a more senior person.  There is a clear line of accountability for the model</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'transparency',
        label: 'Transparency',
        ObjectInputView: QaView,
        helpText: "\n<p>Model is placed in the wider domain for scrutiny, and/or results are published</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'periodicReview',
        label: 'Periodic review',
        ObjectInputView: QaView,
        helpText: "\n<p>Model is reviewed at intervals to ensure it remains fit for the intended purpose, if used on an ongoing basis</p>\n"
      })]
    }, {
      label: 'References',
      title: 'References',
      views: [new ParentView({
        model: this.model,
        ModelType: models_Reference,
        modelAttribute: 'references',
        label: 'References',
        ObjectInputView: ReferenceView,
        multiline: true
      })]
    }, {
      label: 'Version control',
      title: 'Version control history',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'versionHistories',
        label: 'Version control change notes',
        ObjectInputView: VersionHistoryView,
        multiline: true,
        helpText: "\n<p>Use a unique identifier for different versions of a model</p>\n"
      })]
    }, {
      label: 'Project use',
      title: 'Project use',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'projectUsages',
        label: 'Project usage',
        ObjectInputView: ProjectUsageView,
        helpText: "\n<p>Use of model in projects</p>\n"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/CehModelApplicationEditorView.js




/* harmony default export */ const CehModelApplicationEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'modelApplication');
    }

    this.sections = [{
      label: 'Project Info',
      title: 'Project Info',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Project title',
        helpText: "\n<p>Title of project</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'projectCode',
        label: 'Project code',
        helpText: "\n<p>RMS project code</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'projectObjectives',
        label: 'Project objectives',
        rows: 17,
        helpText: "\n<p>Brief description of the main objectives</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Project description',
        rows: 17,
        helpText: "\n<p>Longer description of project incl. why models were used to answer the science question, assumptions made, key outputs</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView,
        helpText: "\n<p>5-10 keywords to enable searching for the project</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'projectCompletionDate',
        label: 'Project completion date',
        helpText: "\n<p>Project end date</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'projectWebsite',
        label: 'Project website',
        helpText: "\n<p>Public-facing website if available e.g. http://www.ceh.ac.uk/our-science/projects/upscape</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'funderDetails',
        label: 'Funder details',
        rows: 3,
        helpText: "\n<p>Funder details, including grant number if appropriate</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'contactName',
        label: 'Contact name',
        helpText: "\n<p>Name of UKCEH PI/project representative</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'contactEmail',
        label: 'Contact email',
        helpText: "\n<p>Email of UKCEH PI/project representative e.g. someone@ceh.ac.uk</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'multipleModelsUsed',
        label: 'Multiple models used?',
        rows: 7,
        helpText: "\n<p>Were multiple models used in the project? If so, which ones?</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'multipleModelLinkages',
        label: 'Multiple model linkages',
        rows: 7,
        helpText: "\n<p>If multiple models were used how was this done e.g. chained, independent runs, comparisons, ensemble</p>\n"
      })]
    }, {
      label: 'References',
      title: 'References',
      views: [new ParentView({
        model: this.model,
        ModelType: models_Reference,
        modelAttribute: 'references',
        label: 'References',
        ObjectInputView: ReferenceView,
        multiline: true,
        helpText: "<p>Citation - Add publication citations here</p>\n<p>DOI - DOI link for the citation e.g. https://doi.org/10.1111/journal-id.1882</p>\n<p>NORA - NORA links of the citation e.g. http://nora.nerc.ac.uk/513147/</p>"
      })]
    }, {
      label: 'Model Info',
      title: 'Model Info',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'modelInfos',
        label: 'Model info',
        ObjectInputView: ModelInfoView,
        multiline: true,
        helpText: "<p>Models used in the project</p>\n<p>Name - Name of model (searches catalogue for matching models)\n<p>Version - Version of the model used for the application (not necessarily the current release version) e.g. v1.5.2 (if applicable)</p>\n<p>Rationale - Why was this model chosen for use in this project?</p>\n<p>Spatial extent of application - What spatial extent best describes the application?</p>\n<p>Available spatial data - Can the application be described by either a shapefile/polygon or bounding box coordinates?</p>\n<p>Spatial resolution of application - Spatial resolution at which model outputs were generated e.g. 1km\xB2; 5m\xB2 (if applicable)</p>\n<p>Temporal extent of application (start date) - Start date of application (if applicable)</p>\n<p>Temporal extent of application (end date) - End date of application (if applicable)</p>\n<p>Temporal resolution of application - Time step used in the model application e.g. 1s; annual (if applicable)</p>\n<p>Calibration conditions - How was the model calibrated (if applicable)?</p>"
      })]
    }, {
      label: 'Data Info',
      title: 'Data Info',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'inputData',
        label: 'Input Data',
        ObjectInputView: DataInfoView,
        multiline: true,
        helpText: "\n<p>Detailed description of input data including: variable name, units, file format, URL to data catalogue record for each input</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'outputData',
        label: 'Output Data',
        ObjectInputView: DataInfoView,
        multiline: true,
        helpText: "\n<p>Detailed description of model outputs including: variable name, units, file format, URL to data catalogue record for each output (or alternative location of model outputs from this application)</p>\n"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/LinkEditorView.js


/* harmony default export */ const LinkEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    this.sections = [{
      label: 'One',
      title: 'General information',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'additionalKeywords',
        label: 'Additional Keywords',
        ObjectInputView: KeywordVocabularyView,
        helpText: "\n<p>Keywords (preferably taken from a controlled vocabulary) categorising and describing the data resource.</p>\n"
      }), new LinkDocumentSelectorView({
        model: this.model,
        modelAttribute: 'linkedDocumentId',
        label: 'Identifier of linked Document',
        helpText: "\n<p>Metadata record linked to by this document.</p>\n"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/OsdpAgentEditorView.js



/* harmony default export */ const OsdpAgentEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'agent');
    }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Name',
        helpText: "\n<p>Name of agent</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'address',
        rows: 5,
        label: 'Address',
        helpText: "\n<p>Address of agent</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView,
        helpText: "\n<p>Keywords for discovery</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'relationships',
        label: 'Relationships',
        ObjectInputView: RelationshipView,
        multiline: true,
        options: [{
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/associatedWith',
          label: 'Associated with'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/creates',
          label: 'Creates'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/knows',
          label: 'Knows'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/responsibleFor',
          label: 'Responsible for'
        }],
        helpText: "\n<p>Relationships to other OSDP document types</p>\n"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/OsdpDatasetEditorView.js






/* harmony default export */ const OsdpDatasetEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'dataset');
    }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title',
        helpText: "\n<p>Name of dataset</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        rows: 13,
        label: 'Description',
        helpText: "\n<p>Description of dataset</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'format',
        label: 'Format',
        helpText: "\n<p>Format of dataset</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'version',
        label: 'Version',
        helpText: "\n<p>Version of dataset</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'parametersMeasured',
        label: 'Parameters Measured',
        ObjectInputView: ParametersMeasuredView,
        multiline: true,
        helpText: "\n<p></p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'referenceDate',
        ModelType: MultipleDate,
        label: 'Reference Date',
        ObjectInputView: DatasetReferenceDateView,
        helpText: "\n<p>Publication, creation & revision dates</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'temporalExtent',
        ModelType: MultipleDate,
        label: 'Temporal Extent',
        ObjectInputView: TemporalExtentView,
        helpText: "\n<p>Temporal Extent of dataset</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'access',
        label: 'Access',
        ObjectInputView: LinkView,
        helpText: "\n<p>Access to dataset</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'boundingBox',
        ModelType: geometryMap_BoundingBox,
        label: 'Bounding Box',
        ObjectInputView: BoundingBoxView,
        helpText: "\n        <p>Bounding Box of Dataset</p>\n        "
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView,
        helpText: "\n<p>Keywords for discovery</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'relationships',
        label: 'Relationships',
        ObjectInputView: RelationshipView,
        multiline: true,
        options: [{
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/cites',
          label: 'Cites'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/related',
          label: 'Related'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/supercedes',
          label: 'Supercedes'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/produces',
          label: 'Produces'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/uses',
          label: 'Uses'
        }],
        helpText: "\n<p>Relationships to other OSDP document types</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'resourceIdentifiers',
        label: 'Resource Identifiers',
        ObjectInputView: ResourceIdentifierView,
        helpText: "<p>A unique string or number used to identify the resource.</p>\n<p> The codespace identifies the context in which the code is unique.</p>"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/OsdpModelEditorView.js




/* harmony default export */ const OsdpModelEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'model');
    }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title',
        helpText: "\n<p>Name of model</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        rows: 13,
        label: 'Description',
        helpText: "\n<p>Description of model</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'format',
        label: 'Format',
        helpText: "\n<p>Format of model</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'version',
        label: 'Version',
        helpText: "\n<p>Version of model</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'referenceDate',
        ModelType: MultipleDate,
        label: 'Reference Date',
        ObjectInputView: DatasetReferenceDateView,
        helpText: "\n<p>Publication, creation & revision dates</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'temporalExtent',
        ModelType: MultipleDate,
        label: 'Temporal Extent',
        ObjectInputView: TemporalExtentView,
        helpText: "\n<p>Temporal Extent of model</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'access',
        label: 'Access',
        ObjectInputView: LinkView,
        helpText: "\n<p>Access to model</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView,
        helpText: "\n<p>Keywords for discovery</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'relationships',
        label: 'Relationships',
        ObjectInputView: RelationshipView,
        multiline: true,
        options: [{
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/cites',
          label: 'Cites'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/related',
          label: 'Related'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/supercedes',
          label: 'Supercedes'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/produces',
          label: 'Produces'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/uses',
          label: 'Uses'
        }],
        // eslint-disable-next-line no-multi-str
        helpText: '\
<p>Relationships to other OSDP document types</p>\
'
      }), new ParentView({
        model: this.model,
        modelAttribute: 'resourceIdentifiers',
        label: 'Resource Identifiers',
        ObjectInputView: ResourceIdentifierView,
        helpText: "<p>A unique string or number used to identify the resource.</p>\n<p> The codespace identifies the context in which the code is unique.</p>"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/OsdpSampleEditorView.js




/* harmony default export */ const OsdpSampleEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'sample');
    }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title',
        helpText: "\n<p>Name of model</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        rows: 13,
        label: 'Description',
        helpText: "\n<p>Description of model</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'medium',
        label: 'Medium',
        helpText: "\n<p>The medium of the sample being described</p>\n"
      }), //     new GeometryView
      //     model: @model
      // modelAttribute: 'geometry'
      // label: 'Geometry'
      // helpText: """
      // <p>Geometry of Sample</p>
      // """
      new SingleObjectView({
        model: this.model,
        modelAttribute: 'referenceDate',
        ModelType: MultipleDate,
        label: 'Reference Date',
        ObjectInputView: DatasetReferenceDateView,
        helpText: "\n<p>Publication, creation & revision dates</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'temporalExtent',
        ModelType: MultipleDate,
        label: 'Temporal Extent',
        ObjectInputView: TemporalExtentView,
        // eslint-disable-next-line no-multi-str
        helpText: '\
<p>Temporal Extent of model</p>\
'
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'access',
        label: 'Access',
        ObjectInputView: LinkView,
        helpText: "\n<p>Access to model</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView,
        helpText: "\n<p>Keywords for discovery</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'relationships',
        label: 'Relationships',
        ObjectInputView: RelationshipView,
        multiline: true,
        options: [{
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/cites',
          label: 'Cites'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/related',
          label: 'Related'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/supercedes',
          label: 'Supercedes'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/produces',
          label: 'Produces'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/uses',
          label: 'Uses'
        }],
        helpText: "\n<p>Relationships to other OSDP document types</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'resourceIdentifiers',
        label: 'Resource Identifiers',
        ObjectInputView: ResourceIdentifierView,
        helpText: "<p>A unique string or number used to identify the resource.</p>\n<p> The codespace identifies the context in which the code is unique.</p>"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/OsdpPublicationEditorView.js




/* harmony default export */ const OsdpPublicationEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'publication');
    }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title',
        helpText: "\n<p>Name of publication</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        rows: 13,
        label: 'Description',
        helpText: "\n<p>Description of publication</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'referenceDate',
        ModelType: MultipleDate,
        label: 'Reference Date',
        ObjectInputView: DatasetReferenceDateView,
        helpText: "\n<p>Publication, creation & revision dates</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'temporalExtent',
        ModelType: MultipleDate,
        label: 'Temporal Extent',
        ObjectInputView: TemporalExtentView,
        helpText: "\n<p>Temporal Extent of publication</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'access',
        label: 'Access',
        ObjectInputView: LinkView,
        helpText: "\n<p>Access to publication</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView,
        helpText: "\n<p>Keywords for discovery</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'relationships',
        label: 'Relationships',
        ObjectInputView: RelationshipView,
        multiline: true,
        options: [{
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/cites',
          label: 'Cites'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/related',
          label: 'Related'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/supercedes',
          label: 'Supercedes'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/produces',
          label: 'Produces'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/uses',
          label: 'Uses'
        }],
        helpText: "\n<p>Relationships to other OSDP document types</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'resourceIdentifiers',
        label: 'Resource Identifiers',
        ObjectInputView: ResourceIdentifierView,
        helpText: "<p>A unique string or number used to identify the resource.</p>\n<p> The codespace identifies the context in which the code is unique.</p>"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/OsdpMonitoringActivityEditorView.js






/* harmony default export */ const OsdpMonitoringActivityEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'monitoringActivity');
    }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Name',
        helpText: "\n<p>Name of Monitoring Activity</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        rows: 13,
        label: 'Description',
        helpText: "\n<p>Description of Monitoring Activity</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'temporalExtent',
        ModelType: MultipleDate,
        label: 'Temporal Extent',
        ObjectInputView: TemporalExtentView,
        helpText: "\n<p>Temporal Extent of Monitoring Activity</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'parametersMeasured',
        label: 'Parameters Measured',
        ObjectInputView: ParametersMeasuredView,
        multiline: true,
        helpText: "\n<p>Parameters measured as part of Monitoring Activity</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView,
        helpText: "\n<p>Keywords for discovery</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'boundingBox',
        ModelType: geometryMap_BoundingBox,
        label: 'Bounding Box',
        ObjectInputView: BoundingBoxView,
        helpText: "\n        <p>Bounding Box of Monitoring Activity</p>\n        "
      }), new ParentView({
        model: this.model,
        modelAttribute: 'relationships',
        label: 'Relationships',
        ObjectInputView: RelationshipView,
        multiline: true,
        options: [{
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/produces',
          label: 'Produces'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/setupFor',
          label: 'Setup for'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/uses',
          label: 'Uses'
        }],
        helpText: "\n<p>Relationships to other OSDP document types</p>\n"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/OsdpMonitoringProgrammeEditorView.js






/* harmony default export */ const OsdpMonitoringProgrammeEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'monitoringProgramme');
    }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title',
        helpText: "\n<p>Name of Monitoring Programme</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        rows: 13,
        label: 'Description',
        helpText: "\n<p>Description of Monitoring Programme</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'temporalExtent',
        ModelType: MultipleDate,
        label: 'Temporal Extent',
        ObjectInputView: TemporalExtentView,
        helpText: "\n<p>Temporal Extent of Monitoring Programme</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'boundingBox',
        ModelType: geometryMap_BoundingBox,
        label: 'Bounding Box',
        ObjectInputView: BoundingBoxView,
        helpText: "\n        <p>Bounding Box of Monitoring Programme</p>\n        "
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView,
        helpText: "\n<p>Keywords for discovery</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'relationships',
        label: 'Relationships',
        ObjectInputView: RelationshipView,
        multiline: true,
        options: [{
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/associatedWith',
          label: 'Associated with'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/owns',
          label: 'Owns'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/setupFor',
          label: 'Setup for'
        }],
        helpText: "\n<p>Relationships to other OSDP document types</p>\n"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/OsdpMonitoringFacilityEditorView.js






/* harmony default export */ const OsdpMonitoringFacilityEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'monitoringFacility');
    }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title',
        helpText: "\n<p>Name of Monitoring Facility</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'facilityType',
        label: 'Type',
        helpText: "\n<p>Type of Monitoring Facility</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        rows: 13,
        label: 'Description',
        helpText: "\n<p>Description of Monitoring Facility</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'temporalExtent',
        ModelType: MultipleDate,
        label: 'Temporal Extent',
        ObjectInputView: TemporalExtentView,
        helpText: "\n<p>Temporal Extent of Monitoring Facility</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView,
        helpText: "\n<p>Keywords for discovery</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'boundingBox',
        ModelType: geometryMap_BoundingBox,
        label: 'Bounding Box',
        ObjectInputView: BoundingBoxView,
        helpText: "\n        <p>Bounding Box of Monitoring Facility</p>\n        "
      }), //         new GeometryView({
      //           model: this.model,
      //           modelAttribute: 'geometry',
      //           label: 'Geometry',
      //           helpText: `
      // <p>Geometry of Monitoring Facility</p>
      // `
      //         }),
      new ParentView({
        model: this.model,
        modelAttribute: 'relationships',
        label: 'Relationships',
        ObjectInputView: RelationshipView,
        multiline: true,
        options: [{
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/partOf',
          label: 'Part of'
        }, {
          value: 'http://onto.nerc.ac.uk/CEHMD/rels/produces',
          label: 'Produces'
        }],
        helpText: "\n<p>Relationships to other OSDP document types</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'observationCapabilities',
        label: 'Observation Capabilities',
        ObjectInputView: ObservationCapabilityView,
        multiline: true,
        helpText: "\n<p>Observation capabilities of the Monitoring Facility</p>\n"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/SampleArchiveEditorView.js






/* harmony default export */ const SampleArchiveEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'sampleArchive');
    }

    this.sections = [{
      label: 'About',
      title: 'About the archive',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Description',
        rows: 7,
        helpText: "\n<p>An brief overview that gives details about the sample collection</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'website',
        label: 'Website address',
        helpText: "\n<p>The archive's website</p>\n"
      }), new ParentView({
        model: this.model,
        ModelType: PointOfContact,
        modelAttribute: 'archiveLocations',
        label: 'Archive locations',
        ObjectInputView: PointOfContactView,
        multiline: true,
        helpText: "\n<p>Location(s) of the archiving facility</p>\n"
      }), new ParentView({
        model: this.model,
        ModelType: PointOfContact,
        modelAttribute: 'archiveContacts',
        label: 'Contacts',
        ObjectInputView: PointOfContactView,
        multiline: true,
        helpText: "\n<p>Person/organisation to contact for more information about or access to the sample archive</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'lineage',
        label: 'Lineage',
        rows: 5,
        helpText: "\n<p>An overview of how samples are collected and any Quality Control or Quality Assurance</p>\n"
      })]
    }, {
      label: 'Availability and access',
      title: 'Availability and access',
      views: [new TextareaView({
        model: this.model,
        modelAttribute: 'availability',
        label: 'Availability',
        rows: 5,
        helpText: "\n<p>Information about how readily available the samples are, who is allowed to have access and how to gain access</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'accessRestrictions',
        label: 'Access restrictions',
        rows: 5,
        helpText: "\n<p>An overview of any restrictions that will apply (usually things like Intellectual Property Rights and Terms and Conditions)</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'storage',
        label: 'Storage',
        rows: 5,
        helpText: "\n<p>An overview of how the samples are stored to help a potential sample user understand what will be required if they request a sample</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'healthSafety',
        label: 'Health and safety',
        rows: 5,
        helpText: "\n<p>Any information users need to know regarding Health and Safety when they are considering using samples from the archive</p>\n"
      })]
    }, {
      label: 'Additional info',
      title: 'Additional information',
      views: [new PredefinedParentView({
        model: this.model,
        modelAttribute: 'funding',
        label: 'Funding',
        ObjectInputView: FundingView,
        multiline: true,
        helpText: "\n<p>Details of funding organisations</p>\n",
        predefined: {
          BBSRC: {
            funderName: 'Biotechnology and Biological Sciences Research Council',
            funderIdentifier: 'https://ror.org/00cwqg982'
          },
          Defra: {
            funderName: 'Department for Environment Food and Rural Affairs',
            funderIdentifier: 'https://ror.org/00tnppw48'
          },
          EPSRC: {
            funderName: 'Engineering and Physical Sciences Research Council',
            funderIdentifier: 'https://ror.org/0439y7842'
          },
          ESRC: {
            funderName: 'Economic and Social Research Council',
            funderIdentifier: 'https://ror.org/03n0ht308'
          },
          'Innovate UK': {
            funderName: 'Innovate UK',
            funderIdentifier: 'https://ror.org/05ar5fy68'
          },
          MRC: {
            funderName: 'Medical Research Council',
            funderIdentifier: 'https://ror.org/03x94j517'
          },
          NERC: {
            funderName: 'Natural Environment Research Council',
            funderIdentifier: 'https://ror.org/02b5d8509'
          },
          STFC: {
            funderName: 'Science and Technology Facilities Council',
            funderIdentifier: 'https://ror.org/057g20z61'
          }
        }
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'temporalExtent',
        ModelType: MultipleDate,
        label: 'Temporal extent',
        ObjectInputView: TemporalExtentView,
        helpText: "\n<p>Start date is mandatory and it represents the earliest sample in the archive - if not precisely known, it  may be approximated. End date is optional, if it is provided it represents the last sample in the archive.</p>\n"
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'boundingBoxes',
        ModelType: geometryMap_BoundingBox,
        label: 'Spatial coverage',
        ObjectInputView: BoundingBoxView,
        multiline: true,
        // These bounding box values were copied from terraCatalog
        predefined: {
          England: {
            northBoundLatitude: 55.812,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -6.452
          },
          'Great Britain': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648
          },
          'Northern Ireland': {
            northBoundLatitude: 55.313,
            eastBoundLongitude: -5.432,
            southBoundLatitude: 54.022,
            westBoundLongitude: -8.178
          },
          Scotland: {
            northBoundLatitude: 60.861,
            eastBoundLongitude: -0.728,
            southBoundLatitude: 54.634,
            westBoundLongitude: -8.648
          },
          'United Kingdom': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648
          },
          Wales: {
            northBoundLatitude: 53.434,
            eastBoundLongitude: -2.654,
            southBoundLatitude: 51.375,
            westBoundLongitude: -5.473
          },
          World: {
            northBoundLatitude: 90.00,
            eastBoundLongitude: 180.00,
            southBoundLatitude: -90.00,
            westBoundLongitude: -180.00
          }
        },
        helpText: "\n        <p>A bounding box showing the area that the archive covers. It will encompass the remit of the archive, which may be larger than that represented by the samples actually in the archive. It is represented by north, south, east and west in decimal degrees (WGS84).</p><p>If you don't know the bounding box values, click on the map to place a rectangle at the approximate location and adjust by dragging it or changing its shape using the handles on the rectangle.</p>\n        "
      }), new ParentView({
        model: this.model,
        modelAttribute: 'onlineResources',
        label: 'Additional Resources',
        ObjectInputView: OnlineLinkView,
        listAttribute: "<option value='Website' />\n<option value='browseGraphic' />",
        helpText: "\n<p>A list of websites that may be of use to the user</p>\n"
      })]
    }, {
      label: 'Classification',
      title: 'Cataloguing and classification',
      views: [new ParentView({
        model: this.model,
        ModelType: SaTaxa,
        modelAttribute: 'taxa',
        label: 'Taxa',
        ObjectInputView: SaTaxaView
      }), new ParentView({
        model: this.model,
        ModelType: SaPhysicalState,
        modelAttribute: 'physicalStates',
        label: 'Physical state',
        ObjectInputView: SaPhysicalStateView
      }), new ParentView({
        model: this.model,
        ModelType: SaSpecimenType,
        modelAttribute: 'specimenTypes',
        label: 'Specimen type',
        ObjectInputView: SaSpecimenTypeView
      }), new ParentView({
        model: this.model,
        ModelType: SaTissue,
        modelAttribute: 'tissues',
        label: 'Tissue',
        ObjectInputView: SaTissueView
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Other keywords',
        ObjectInputView: KeywordView,
        helpText: "\n<p>A list of words and phrases that help to identify and describe the archive - useful for improving search results</p>\n"
      })]
    }, {
      label: 'Metadata',
      title: 'Metadata',
      views: [new ParentView({
        model: this.model,
        ModelType: PointOfContact,
        modelAttribute: 'metadataContacts',
        label: 'Record owner',
        ObjectInputView: PointOfContactView,
        multiline: true,
        helpText: "\n<p>Person responsible for maintaining this metadata record</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'id',
        label: 'File identifier',
        readonly: true
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'uri',
        label: 'URL',
        readonly: true
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'metadataDate',
        label: 'Metadata date',
        readonly: true,
        helpText: "\n<p>Date and time the record was last updated (generated automatically when you save a record and not editable)</p>\n"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/ErammpModelEditorView.js






/* harmony default export */ const ErammpModelEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'erammpModel');
    }

    this.sections = [{
      label: 'General',
      title: 'General',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Name of model'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'version',
        label: 'Version'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Description',
        rows: 5,
        helpText: "\n<p>A short description that gives details about the model. A well-written description will ensure the record appears appropriately in searches.</p>\n"
      }), new ParentView({
        model: this.model,
        ModelType: models_Contact,
        modelAttribute: 'contacts',
        label: 'Contacts',
        ObjectInputView: ContactView,
        multiline: true,
        helpText: "\n<p>The contact(s) responsible for this model and who can be contacted if there are questions about it.  A <b>named</b> person is recommended</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'ipr',
        label: 'IPR',
        rows: 5,
        helpText: "\n<p>Are there any IPR issues associated with making internal variables and/or outputs from the models available?</p>\n"
      })]
    }, {
      label: 'Spatial',
      title: 'Spatial',
      views: [new CheckboxView({
        model: this.model,
        modelAttribute: 'spatiallyExplicit',
        label: 'The model is spatially explicit'
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'boundingBoxes',
        ModelType: geometryMap_BoundingBox,
        label: 'Spatial extents',
        ObjectInputView: BoundingBoxView,
        predefined: {
          'England (or England & Wales)': {
            northBoundLatitude: 55.812,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -6.452
          },
          'Northern Ireland': {
            northBoundLatitude: 55.313,
            eastBoundLongitude: -5.432,
            southBoundLatitude: 54.022,
            westBoundLongitude: -8.178
          },
          Scotland: {
            northBoundLatitude: 60.861,
            eastBoundLongitude: -0.728,
            southBoundLatitude: 54.634,
            westBoundLongitude: -8.648
          },
          'UK (or Great Britain)': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648
          },
          Wales: {
            northBoundLatitude: 53.434,
            eastBoundLongitude: -2.654,
            southBoundLatitude: 51.375,
            westBoundLongitude: -5.473
          },
          World: {
            northBoundLatitude: 90.00,
            eastBoundLongitude: 180.00,
            southBoundLatitude: -90.00,
            westBoundLongitude: -180.00
          }
        },
        multiline: true,
        helpText: "        <p>A bounding box showing the area that the archive covers. It will encompass the remit of the archive, which may be larger than that represented by the samples actually in the archive. It is represented by north, south, east and west in decimal degrees (WGS84).</p>\n        <p>Enter the values, or click on the map to draw a  rectangle at the approximate location.</p>        "
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'spatialResolution',
        label: 'Maximum spatial resolution',
        placeholderAttribute: 'Maximum  resolution at which you would be comfortable applying the model',
        helpText: "\n<p>What is the maximum spatial resolution (most detailed, smallest pixel size) at which you would be comfortable applying the model?</p>\n"
      }), new CheckboxView({
        model: this.model,
        modelAttribute: 'calibratedForWales',
        label: 'The model is calibrated for Wales'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'calibrationEffort',
        label: 'Calibration',
        rows: 3,
        placeholderAttribute: 'If the model is NOT currently calibrated for Wales, how much work would it be to calibrate it?'
      })]
    }, {
      label: 'Input variables',
      title: 'Input ariables',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'inputs',
        label: 'Inputs',
        ObjectInputView: ErammpModelInputView,
        multiline: true
      })]
    }, {
      label: 'Output variables',
      title: 'Output ariables',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'outputs',
        label: 'Outputs',
        ObjectInputView: ErammpModelOutputView,
        multiline: true
      })]
    }, {
      label: 'Other',
      title: 'Other',
      views: [new TextareaView({
        model: this.model,
        modelAttribute: 'modelApproach',
        label: 'Modelling approach',
        rows: 5,
        helpText: "\n<p>e.g. Statistical, process-based, etc.</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'runtimeTotal',
        label: 'Approximate runtime',
        placeholderAttribute: 'e.g. 10 minutes/45 seconds/several hours'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'runtimeWales',
        label: 'Approximate runtime for Wales',
        placeholderAttribute: 'e.g. 10 minutes/45 seconds/several hours'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'runtimeOptimisation',
        label: 'Optimisation of runtime',
        helpText: "\n<p>What work needs to be done to make a model that can be run in seconds rather than minutes? (how would you do it?)</p>\n"
      }), new ParentStringView({
        model: this.model,
        modelAttribute: 'outputFormats',
        label: 'Output formats',
        placeholderAttribute: 'e.g. NetCDF, dbf, csv, shp'
      }), new ParentStringView({
        model: this.model,
        modelAttribute: 'sectors',
        label: 'Sector',
        placeholderAttribute: 'e.g. Agriculture, Forestry'
      }), new ParentStringView({
        model: this.model,
        modelAttribute: 'programmingLanguages',
        label: 'Programming languages'
      }), new ParentStringView({
        model: this.model,
        modelAttribute: 'operatingSystems',
        label: 'Operating Systems'
      }), new ParentStringView({
        model: this.model,
        modelAttribute: 'timeSteps',
        label: 'Time steps',
        placeholderAttribute: 'e.g. daily, annual, decadal',
        helpText: "\n<p>Over what time step does the model produce outputs?</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'futureRun',
        label: 'Future distance',
        placeholderAttribute: 'e.g. 2020s, 2100? ',
        helpText: "\n<p>How far into the future would you feel comfortable running the model?</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'integrationExperience',
        label: 'Experience of model integration',
        rows: 5,
        helpText: "\n<p>How much experience has your team of integrating models?</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'integrationHistory',
        label: 'Integration of this model',
        rows: 5,
        helpText: "\n<p>Have you linked this model within an integrated system before?  If so how?</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView,
        helpText: "\n<p>A list of keywords that help to identify and describe the model - used to improve search results and filtering. A keyword may be an entry from a vocabulary (with a uri) or just plain text.</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'onlineResources',
        label: 'Additional Resources',
        ObjectInputView: OnlineLinkView,
        helpText: "\n<p>A list of links to additional resources that may be of use to the user. These are in the form of name: url pairs.</p>\n"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/NercModelEditorView.js






/* harmony default export */ const NercModelEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'nercModel');
    }

    this.sections = [{
      label: 'Basic info',
      title: 'Basic info',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Description',
        rows: 6,
        helpText: "\n<p>Longer description of model e.g. development history, use to answer science questions, overview of structure</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'purpose',
        label: 'Purpose',
        rows: 6
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'modelType',
        label: 'Model type',
        listAttribute: "<option value='Deterministic' />\n<option value='Stochastic' />"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'version',
        label: 'Version',
        placeholderAttribute: 'e.g. 2.5.10'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'releaseDate',
        typeAttribute: 'text',
        label: 'Release date'
      }), new PredefinedParentView({
        model: this.model,
        ModelType: models_Contact,
        modelAttribute: 'responsibleParties',
        label: 'Contacts',
        ObjectInputView: ContactView,
        multiline: true,
        predefined: {
          BAS: {
            role: 'pointOfContact',
            organisationName: 'British Antarctic Survey',
            email: 'information@bas.ac.uk',
            organisationIdentifier: 'https://ror.org/01rhff309'
          },
          BGS: {
            role: 'pointOfContact',
            organisationName: 'British Geological Survey',
            email: 'enquiries@bgs.ac.uk',
            organisationIdentifier: 'https://ror.org/04a7gbp98'
          },
          CEDA: {
            role: 'pointOfContact',
            organisationName: 'Centre for Environmental Data Analysis'
          },
          NOC: {
            role: 'pointOfContact',
            organisationName: 'National Oceanography Centre',
            organisationIdentifier: 'https://ror.org/00874hx02'
          },
          UKCEH: {
            role: 'pointOfContact',
            organisationName: 'UK Centre for Ecology & Hydrology',
            email: 'enquiries@ceh.ac.uk',
            organisationIdentifier: 'https://ror.org/00pggkr55'
          }
        }
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordVocabularyView,
        helpText: "\n<p>Keywords help with model discovery</p>\n"
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'funding',
        ModelType: models_Funding,
        multiline: true,
        label: 'Funding',
        ObjectInputView: FundingView,
        predefined: {
          BBSRC: {
            funderName: 'Biotechnology and Biological Sciences Research Council',
            funderIdentifier: 'https://ror.org/00cwqg982'
          },
          Defra: {
            funderName: 'Department for Environment Food and Rural Affairs',
            funderIdentifier: 'https://ror.org/00tnppw48'
          },
          EPSRC: {
            funderName: 'Engineering and Physical Sciences Research Council',
            funderIdentifier: 'https://ror.org/0439y7842'
          },
          ESRC: {
            funderName: 'Economic and Social Research Council',
            funderIdentifier: 'https://ror.org/03n0ht308'
          },
          'Innovate UK': {
            funderName: 'Innovate UK',
            funderIdentifier: 'https://ror.org/05ar5fy68'
          },
          MRC: {
            funderName: 'Medical Research Council',
            funderIdentifier: 'https://ror.org/03x94j517'
          },
          NERC: {
            funderName: 'Natural Environment Research Council',
            funderIdentifier: 'https://ror.org/02b5d8509'
          },
          STFC: {
            funderName: 'Science and Technology Facilities Council',
            funderIdentifier: 'https://ror.org/057g20z61'
          }
        },
        helpText: "<p>Include here details of any grants or awards that were used to generate this resource.</p>\n<p>If you include funding information, the Funding body is MANDATORY, other fields are useful but optional.</p>\n<p>Award URL is either the unique identifier for the award or sa link to the funder's  grant page (if it exists). It is <b>NOT</b> a link to a project website.</p>"
      })]
    }, {
      label: 'Access',
      title: 'Access',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'onlineResources',
        label: 'Online resources',
        ObjectInputView: OnlineLinkView,
        multiline: true,
        listAttribute: "<option value='code'>Link to location of the model code (e.g. GitHub repository)</option>\n<option value='documentation'>Link to documentation describing how to use the model</option>\n<option value='website'/>\n<option value='browseGraphic'>Image to display on metadata record</option>",
        helpText: "<p>Websites/online resources to access and further descibe the model</p>\n<p>You should include the location of the model code repository e.g. https://github.com/...</p>"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'licenseType',
        label: 'License',
        listAttribute: "<option value='open' />\n<option value='non-open' />",
        helpText: "\n<p>License type (open or non-open) under which the model is distributed</p>\n"
      })]
    }, {
      label: 'Technical',
      title: 'Technical information',
      views: [new TextareaView({
        model: this.model,
        modelAttribute: 'calibration',
        label: 'Model calibration',
        rows: 7,
        helpText: "\n<p>Does the model need calibration before running? If so, what needs to be supplied to do this? (if applicable)</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'configuration',
        label: 'Model configuration',
        rows: 7
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'language',
        label: 'Language',
        placeholderAttribute: 'e.g. Python 2.7, C++, R 3.6',
        helpText: "\n<p>Language in which the model is written.  You should include the release number if relevant</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'compiler',
        label: 'Compiler',
        placeholderAttribute: 'e.g. C++ compiler',
        helpText: "\n<p>Compiler required (if applicable)</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'operatingSystem',
        label: 'Operating system',
        helpText: "\n<p>Operating system typically used to run the model</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'systemMemory',
        label: 'System memory',
        helpText: "\n<p>Memory required to run code (if known)</p>\n"
      })]
    }, {
      label: 'Scale',
      title: 'Spatial and temporal scale',
      views: [new PredefinedParentView({
        model: this.model,
        modelAttribute: 'boundingBoxes',
        ModelType: geometryMap_BoundingBox,
        label: 'Spatial extent',
        ObjectInputView: BoundingBoxView,
        multiline: true,
        predefined: {
          England: {
            northBoundLatitude: 55.812,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -6.452,
            extentName: 'England',
            extentUri: 'http://sws.geonames.org/6269131'
          },
          'Great Britain': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648,
            extentName: 'Great Britain'
          },
          'Northern Ireland': {
            northBoundLatitude: 55.313,
            eastBoundLongitude: -5.432,
            southBoundLatitude: 54.022,
            westBoundLongitude: -8.178,
            extentName: 'Northern Ireland',
            extentUri: 'http://sws.geonames.org/2641364'
          },
          Scotland: {
            northBoundLatitude: 60.861,
            eastBoundLongitude: -0.728,
            southBoundLatitude: 54.634,
            westBoundLongitude: -8.648,
            extentName: 'Scotland',
            extentUri: 'http://sws.geonames.org/2638360'
          },
          'United Kingdom': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648,
            extentName: 'United Kingdom',
            extentUri: 'http://sws.geonames.org/2635167'
          },
          Wales: {
            northBoundLatitude: 53.434,
            eastBoundLongitude: -2.654,
            southBoundLatitude: 51.375,
            westBoundLongitude: -5.473,
            extentName: 'Wales',
            extentUri: 'http://sws.geonames.org/2634895'
          },
          World: {
            northBoundLatitude: 90.00,
            eastBoundLongitude: 180.00,
            southBoundLatitude: -90.00,
            westBoundLongitude: -180.00
          }
        },
        helpText: "    <p>A bounding box representing the limits of the data resource's study area.</p>\n    <p>If you do not wish to reveal the exact location publicly (for example, if locations are sensitive) it is recommended that you generalise the location.</p>    "
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'spatialDomain',
        label: 'Spatial domain',
        placeholderAttribute: 'e.g. Parameterised for UK only or global',
        listAttribute: "    <option value='UK' />\n    <option value='Global' />    ",
        helpText: "\n    <p>Is the model only applicable to certain areas?</p>\n    "
      }), new ParentView({
        model: this.model,
        modelAttribute: 'resolution',
        multiline: true,
        label: 'Resolution',
        ObjectInputView: ModelResolutionView
      })]
    }, {
      label: 'Quality',
      title: 'Quality',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'qa',
        multiline: true,
        label: 'Quality assurance',
        ObjectInputView: ModelQAView
      })]
    }, {
      label: 'References',
      title: 'References',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'references',
        ModelType: Supplemental,
        multiline: true,
        label: 'References',
        ObjectInputView: SupplementalView,
        helpText: "<p>You can add information not documented elsewhere here. This includes links to related papers, grey literature or websites.  For example:</p>\n<ul><li>papers that cite this resource</li><li>papers/reports that provide relevant supporting information but which do not cite this resource</li><li>project websites</li></ul>\n<p>When linking to published articles, please use DOIs whenever possible.</p>\n<p><small class='text-danger'><i class='fas fa-exclamation-triangle'> </i> NOTE: Some websites may be maintained for a limited period and may therefore soon become unavailable.</small></p>"
      })]
    }, {
      label: 'Input variables',
      title: 'Input variables',
      views: [new PredefinedParentView({
        model: this.model,
        ModelType: DataTypeSchema,
        modelAttribute: 'inputVariables',
        multiline: true,
        label: 'Input variables',
        ObjectInputView: DataTypeSchemaSimpleView,
        predefined: {
          'Boolean (true/false)': {
            type: 'boolean'
          },
          Date: {
            type: 'date'
          },
          'Date & time': {
            type: 'datetime'
          },
          'Decimal number': {
            type: 'number'
          },
          'Geographic point': {
            type: 'geopoint'
          },
          Integer: {
            type: 'integer'
          },
          Text: {
            type: 'string'
          },
          Time: {
            type: 'time',
            format: 'hh:mm:ss'
          },
          URI: {
            type: 'string',
            format: 'uri'
          },
          UUID: {
            type: 'string',
            format: 'uuid'
          }
        }
      })]
    }, {
      label: 'Output variables',
      title: 'Output variables',
      views: [new PredefinedParentView({
        model: this.model,
        ModelType: DataTypeSchema,
        modelAttribute: 'outputVariables',
        multiline: true,
        label: 'Output variables',
        ObjectInputView: DataTypeSchemaSimpleView,
        predefined: {
          'Boolean (true/false)': {
            type: 'boolean'
          },
          Date: {
            type: 'date'
          },
          'Date & time': {
            type: 'datetime'
          },
          'Decimal number': {
            type: 'number'
          },
          'Geographic point': {
            type: 'geopoint'
          },
          Integer: {
            type: 'integer'
          },
          Text: {
            type: 'string'
          },
          Time: {
            type: 'time',
            format: 'hh:mm:ss'
          },
          URI: {
            type: 'string',
            format: 'uri'
          },
          UUID: {
            type: 'string',
            format: 'uuid'
          }
        }
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/NercModelUseEditorView.js




/* harmony default export */ const NercModelUseEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'nercModelUse');
    }

    this.sections = [{
      label: 'General',
      title: 'General',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Description',
        rows: 6
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordVocabularyView
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'completionDate',
        label: 'Completion date'
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'responsibleParties',
        ModelType: models_Contact,
        multiline: true,
        label: 'Contacts',
        ObjectInputView: ContactView,
        predefined: {
          BAS: {
            organisationName: 'British Antarctic Survey',
            role: 'pointOfContact',
            email: 'information@bas.ac.uk',
            organisationIdentifier: 'https://ror.org/01rhff309'
          },
          BGS: {
            organisationName: 'British Geological Survey',
            role: 'pointOfContact',
            email: 'enquiries@bgs.ac.uk',
            organisationIdentifier: 'https://ror.org/04a7gbp98'
          },
          CEDA: {
            organisationName: 'Centre for Environmental Data Analysis',
            role: 'pointOfContact'
          },
          NOC: {
            organisationName: 'National Oceanography Centre',
            role: 'pointOfContact',
            organisationIdentifier: 'https://ror.org/00874hx02'
          },
          UKCEH: {
            organisationName: 'UK Centre for Ecology & Hydrology',
            role: 'pointOfContact',
            email: 'enquiries@ceh.ac.uk',
            organisationIdentifier: 'https://ror.org/00pggkr55'
          }
        }
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'funding',
        ModelType: models_Funding,
        multiline: true,
        label: 'Funding',
        ObjectInputView: FundingView,
        predefined: {
          BBSRC: {
            funderName: 'Biotechnology and Biological Sciences Research Council',
            funderIdentifier: 'https://ror.org/00cwqg982'
          },
          Defra: {
            funderName: 'Department for Environment Food and Rural Affairs',
            funderIdentifier: 'https://ror.org/00tnppw48'
          },
          EPSRC: {
            funderName: 'Engineering and Physical Sciences Research Council',
            funderIdentifier: 'https://ror.org/0439y7842'
          },
          ESRC: {
            funderName: 'Economic and Social Research Council',
            funderIdentifier: 'https://ror.org/03n0ht308'
          },
          'Innovate UK': {
            funderName: 'Innovate UK',
            funderIdentifier: 'https://ror.org/05ar5fy68'
          },
          MRC: {
            funderName: 'Medical Research Council',
            funderIdentifier: 'https://ror.org/03x94j517'
          },
          NERC: {
            funderName: 'Natural Environment Research Council',
            funderIdentifier: 'https://ror.org/02b5d8509'
          },
          STFC: {
            funderName: 'Science and Technology Facilities Council',
            funderIdentifier: 'https://ror.org/057g20z61'
          }
        }
      })]
    }, {
      label: 'Models',
      title: 'Models',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'modelInfos',
        label: 'Model info',
        ObjectInputView: NercModelInfoView,
        multiline: true,
        helpText: "<p>Models used in the project</p>\n<p>Name - Name of model (searches catalogue for matching models)\n<p>Spatial extent of application - What spatial extent best describes the application?</p>\n<p>Available spatial data - Can the application be described by either a shapefile/polygon or bounding box coordinates?</p>\n<p>Spatial resolution of application - Spatial resolution at which model outputs were generated e.g. 1km\xB2; 5m\xB2 (if applicable)</p>\n<p>Temporal extent of application (start date) - Start date of application (if applicable)</p>\n<p>Temporal extent of application (end date) - End date of application (if applicable)</p>\n<p>Temporal resolution of application - Time step used in the model application e.g. 1s; annual (if applicable)</p>\n<p>Calibration - How was the model calibrated (if applicable)?</p>"
      })]
    }, {
      label: 'Data',
      title: 'Data',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'inputData',
        label: 'Input Data',
        ObjectInputView: DataInfoView,
        multiline: true,
        helpText: "\n<p>Detailed description of input data including: variable name, units, file format, URL to data catalogue record for each input</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'outputData',
        label: 'Output Data',
        ObjectInputView: DataInfoView,
        multiline: true,
        helpText: "\n<p>Detailed description of model outputs including: variable name, units, file format, URL to data catalogue record for each output (or alternative location of model outputs from this application)</p>\n"
      })]
    }, {
      label: 'References & links',
      title: 'References',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'onlineResources',
        label: 'Online resources',
        ObjectInputView: OnlineLinkView,
        multiline: true,
        listAttribute: "<option value='website'/>\n<option value='browseGraphic'>Image to display on metadata record</option>"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'references',
        ModelType: Supplemental,
        multiline: true,
        label: 'References',
        ObjectInputView: SupplementalView,
        helpText: "<p>You can add information not documented elsewhere here. This includes links to related papers, grey literature or websites.  For example:</p>\n<ul><li>papers that cite this resource</li><li>papers/reports that provide relevant supporting information but which do not cite this resource</li><li>project websites</li></ul>\n<p>When linking to published articles, please use DOIs whenever possible.</p>\n<p><small class='text-danger'><i class='fas fa-exclamation-triangle'> </i> NOTE: Some websites may be maintained for a limited period and may therefore soon become unavailable.</small></p>"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/ErammpDatacubeEditorView.js







/* harmony default export */ const ErammpDatacubeEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'erammpDatacube');
    }

    this.sections = [{
      label: ' General ',
      title: 'General',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Data name'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'version',
        label: 'Version'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Description',
        rows: 3
      }), new SelectView/* default */.Z({
        model: this.model,
        modelAttribute: 'condition',
        label: 'Status',
        options: [{
          value: '',
          label: ''
        }, {
          value: 'Current',
          label: 'Current'
        }, {
          value: 'Draft',
          label: 'Draft'
        }, {
          value: 'Obsolete',
          label: 'Obsolete (DO NOT USE)'
        }]
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'dataFormat',
        label: 'Data format',
        placeholderAttribute: 'e.g. NetCDF, dbf, csv, shp'
      }), new PredefinedParentView({
        model: this.model,
        ModelType: PointOfContact,
        modelAttribute: 'provider',
        label: 'Data provider',
        ObjectInputView: PointOfContactView,
        helpText: "\n<p>The contact(s) responsible for this model and who can be contacted if there are questions about it.  A <b>named</b> person is recommended</p>\n",
        predefined: {
          ADAS: {
            organisationName: 'ADAS'
          },
          UKCEH: {
            organisationName: 'UK Centre for Ecology & Hydrology'
          },
          Cranfield: {
            organisationName: 'Cranfield'
          },
          'Forest Research': {
            organisationName: 'Forest Research'
          }
        }
      })]
    }, {
      label: 'Data access',
      title: 'Data access',
      views: [new PredefinedParentView({
        model: this.model,
        modelAttribute: 'accessConstraints',
        label: 'Access constraints',
        ObjectInputView: ResourceConstraintView,
        multiline: true,
        predefined: {
          'Restricted to ERAMMP team': {
            value: 'Access to this data is restricted to members of the ERAMMP project team',
            code: 'otherRestrictions'
          },
          'Restricted to named individuals': {
            value: 'Access to this data is restricted to the following named individuals: ',
            code: 'otherRestrictions'
          }
        },
        helpText: "\n<p>Describe any restrictions and legal prerequisites placed on <strong>access</strong> to this  data</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'useConstraints',
        label: 'Use constraints',
        ObjectInputView: ResourceConstraintView,
        multiline: true,
        helpText: "\n<p>Describe any restrictions and legal prerequisites placed on the <strong>use</strong> of a data resource once it has been accessed.</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'dataLocations',
        label: 'Data location',
        ObjectInputView: DataLocationView,
        multiline: true,
        helpText: "\n<p>Where is the data stored (eg SharePoint, SAN).  A direct path to the data is preferred.</p>\n"
      })]
    }, {
      label: 'Data structure',
      title: 'Data structure',
      views: [new PredefinedParentLargeView({
        model: this.model,
        ModelType: DataTypeSchema,
        modelAttribute: 'schema',
        multiline: true,
        label: 'Schema',
        ObjectInputView: DataTypeSchemaView,
        predefined: {
          'Boolean (true/false)': {
            type: 'boolean'
          },
          Date: {
            type: 'date',
            format: 'YYYY-MM-DD'
          },
          'Date & time': {
            type: 'datetime',
            format: 'YYYY-MM-DDThh:mm:ss'
          },
          'Decimal number': {
            type: 'number'
          },
          Email: {
            type: 'string',
            format: 'email'
          },
          'Geographic point': {
            type: 'geopoint',
            format: 'lon, lat'
          },
          Integer: {
            type: 'integer'
          },
          Text: {
            type: 'string'
          },
          Time: {
            type: 'time',
            format: 'hh:mm:ss'
          },
          URI: {
            type: 'string',
            format: 'uri'
          },
          UUID: {
            type: 'string',
            format: 'uuid'
          },
          Year: {
            type: 'year',
            format: 'YYYY'
          },
          'Year & month': {
            type: 'yearmonth',
            format: 'YYYY-MM'
          }
        }
      })]
    }, {
      label: 'Spatial',
      title: 'Spatial',
      views: [new PredefinedParentView({
        model: this.model,
        modelAttribute: 'spatialReferenceSystems',
        label: 'Spatial reference systems',
        ObjectInputView: SpatialReferenceSystemView,
        predefined: {
          'British National Grid': {
            code: 27700,
            codeSpace: 'urn:ogc:def:crs:EPSG'
          },
          'Latitude/longitude (WGS84)': {
            code: 4326,
            codeSpace: 'urn:ogc:def:crs:EPSG'
          },
          'Spherical mercator': {
            code: 3857,
            codeSpace: 'urn:ogc:def:crs:EPSG'
          }
        },
        // eslint-disable-next-line no-multi-str
        helpText: '\
<p>The spatial referencing system used by the data resource.</p>\
'
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'boundingBoxes',
        ModelType: geometryMap_BoundingBox,
        label: 'Spatial coverage',
        ObjectInputView: BoundingBoxView,
        multiline: true,
        predefined: {
          Wales: {
            northBoundLatitude: 53.434,
            eastBoundLongitude: -2.654,
            southBoundLatitude: 51.375,
            westBoundLongitude: -5.473
          },
          'England & Wales': {
            northBoundLatitude: 55.812,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -6.452
          },
          'UK (or Great Britain)': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648
          },
          World: {
            northBoundLatitude: 90.00,
            eastBoundLongitude: 180.00,
            southBoundLatitude: -90.00,
            westBoundLongitude: -180.00
          }
        },
        helpText: "        <p>A bounding box showing the area that the archive covers. It will encompass the remit of the archive, which may be larger than that represented by the samples actually in the archive. It is represented by north, south, east and west in decimal degrees (WGS84).</p>\n        <p>Enter the values, or click on the map to draw a  rectangle at the approximate location.</p>        "
      }), new SelectView/* default */.Z({
        model: this.model,
        modelAttribute: 'spatialRepresentationType',
        label: 'Spatial type',
        options: [{
          value: 'grid',
          label: 'Raster (grid)'
        }, {
          value: 'vector',
          label: 'Vector'
        }, {
          value: 'textTable',
          label: 'Tabular (e.g. spreadsheet, database table)'
        }]
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'spatialResolution',
        label: 'Spatial resolution'
      })]
    }, {
      label: 'Provenance',
      title: 'Provenance',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'processingSteps',
        label: 'Processing steps',
        ObjectInputView: ProcessingStepView
      })]
    }, {
      label: 'Metadata',
      title: 'Metadata',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView,
        helpText: "\n<p>A list of keywords that help to identify and describe the model - used to improve search results and filtering. A keyword may be an entry from a vocabulary (with a uri) or just plain text.</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'onlineResources',
        label: 'Additional links',
        ObjectInputView: OnlineLinkView,
        helpText: "\n<p>A list of links to additional resources that may be of use to the user.</p>\n"
      }), new ReadOnlyView({
        model: this.model,
        modelAttribute: 'id',
        label: 'Identifier'
      }), new ReadOnlyView({
        model: this.model,
        modelAttribute: 'uri',
        label: 'URL'
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/DataTypeEditorView.js




/* harmony default export */ const DataTypeEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    this.sections = [{
      label: 'General',
      title: '',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        rows: 5,
        label: 'Description'
      }), new PredefinedParentLargeView({
        model: this.model,
        ModelType: DataTypeSchema,
        modelAttribute: 'schema',
        multiline: true,
        label: 'Schema',
        ObjectInputView: DataTypeSchemaView,
        predefined: {
          'Boolean (true/false)': {
            type: 'boolean'
          },
          Date: {
            type: 'date',
            format: 'YYYY-MM-DD'
          },
          'Date & time': {
            type: 'datetime',
            format: 'YYYY-MM-DDThh:mm:ss'
          },
          'Decimal number': {
            type: 'number'
          },
          Email: {
            type: 'string',
            format: 'email'
          },
          'Geographic point': {
            type: 'geopoint',
            format: 'lon, lat'
          },
          Integer: {
            type: 'integer'
          },
          Text: {
            type: 'string'
          },
          Time: {
            type: 'time',
            format: 'hh:mm:ss'
          },
          URI: {
            type: 'string',
            format: 'uri'
          },
          UUID: {
            type: 'string',
            format: 'uuid'
          },
          Year: {
            type: 'year',
            format: 'YYYY'
          },
          'Year & month': {
            type: 'yearmonth',
            format: 'YYYY-MM'
          }
        }
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'provenance',
        label: 'Provenance',
        ObjectInputView: DataTypeProvenanceView
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/ElterEditorView.js







/* harmony default export */ const ElterEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    var disabled = jquery_default()(jquery_default()('body')[0]).data('edit-restricted');
    this.sections = [{
      label: 'General',
      title: '',
      views: [new ReadOnlyView({
        model: this.model,
        modelAttribute: 'id',
        label: 'File identifier'
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'resourceType',
        ModelType: ResourceType,
        label: 'Resource Type',
        ObjectInputView: ResourceTypeView
      }), new ParentView({
        model: this.model,
        modelAttribute: 'deimsSites',
        label: 'DEIMS sites',
        ObjectInputView: DeimsSiteView,
        multiline: true,
        helpText: "\n<p>DEIMS sites that have contributed to the dataset.</p>\n"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title',
        helpText: "<p>Provide a title that best describes that data resource. Include references to the subject, spatial and temporal aspects of the data resource.</p>\n<p>Only the leading letter and proper nouns of the title should be capitalised.  If it's necessary to include acronyms in the title, then include both the acronym (in parentheses) and the phrase/word from which it was formed. Acronyms should not include full-stops between each letter.</p>\n<p>If there are multiple titles or translations of titles (e.g. in Welsh), these should be added as alternative titles.</p>"
      }), new ParentStringView({
        model: this.model,
        modelAttribute: 'alternateTitles',
        label: 'Alternative titles',
        helpText: "<p>Alternative titles allow you to add multiple titles and non-English translations of titles (e.g. Welsh).</p>\n<p>Only the leading letter and proper nouns of titles should be capitalised. If the title includes acronyms, include both the acronym (in parentheses) and its definition. Acronyms should not include full-stops between each letter.</p>"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Description',
        rows: 12,
        helpText: "<p>The description should describe the data resource in question, NOT the project/activity which produced it.</p>\n<p>The description is an 'executive summary' that allows the reader to determine the relevance and usefulness of the resource.  The text should be concise but should contain sufficient detail to allow the reader to ascertain rapidly the scope and limitations of the resource.</p>\n<p>Write in plain English; in other words, write complete sentences rather than fragments.  It is recommended that the abstract is organised using the \"What, Where, When, How, Why, Who\" structure.</p>"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'accessLimitation',
        ModelType: AccessLimitation,
        label: 'Resource status',
        ObjectInputView: AccessLimitationView,
        helpText: "<p>Access status of resource.  For example, is the resource embargoed or are restrictions imposed for reasons of confidentiality or security.</p>\n<p><b>NOTE</b>: if access is Embargoed, you must also complete the <i>Release date</i>.</p>"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'datasetReferenceDate',
        ModelType: MultipleDate,
        label: 'Reference dates',
        ObjectInputView: DatasetReferenceDateView,
        helpText: "<p><b>Created</b> is the date on which the data resource was originally created.</p>\n<p><b>Published</b> is the date when this metadata record is made available publicly.</p>\n<p>For embargoed resources, <b>Release(d)</b> is the date on which the embargo was lifted <i class='text-red'><b>or is due to be lifted</b></i>.</p>\n<p><b>Superseded</b> is the date on which the resource was superseded by another resource (where relevant).</p>"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'temporalExtents',
        ModelType: MultipleDate,
        label: 'Temporal extent',
        ObjectInputView: TemporalExtentView,
        helpText: "\n<p>The time period(s) the data resource covers.  This is often the same as the data capture period but it need not be so.</p>\n"
      })]
    }, {
      label: 'Authors & contacts',
      title: 'Authors and other contacts',
      views: [new PredefinedParentView({
        model: this.model,
        ModelType: models_Contact,
        modelAttribute: 'responsibleParties',
        label: 'Contacts',
        ObjectInputView: ContactView,
        multiline: true,
        helpText: "<p>The names of authors should be in the format <code>Surname, First Initial. Second Initial.</code> For example <i>Brown, A.B.</i></p>\n<p>Role and organisation name are mandatory.</p>\n<p>The preferred identifier for individuals is an ORCiD.  You must enter the identifier as a <i>fully qualified</i> ID (e.g.  <b>https://orcid.org/1234-5678-0123-987X</b> rather than <b>1234-5678-0123-987X</b>).</p>"
      })]
    }, {
      label: 'Classification',
      title: 'Categories and keywords',
      views: [new ParentView({
        model: this.model,
        ModelType: TopicCategory,
        modelAttribute: 'topicCategories',
        label: 'Topic categories',
        ObjectInputView: TopicCategoryView,
        helpText: "<p>Please note these are very broad themes and should not be confused with EIDC science topics.</p>\n<p>Multiple topic categories are allowed - please include all that are pertinent.  For example, \"Estimates of topsoil invertebrates\" = Biota AND Environment AND Geoscientific Information.</p>"
      }), new PredefinedParentView({
        model: this.model,
        ModelType: DescriptiveKeyword,
        modelAttribute: 'descriptiveKeywords',
        label: 'Keywords',
        ObjectInputView: DescriptiveKeywordView,
        multiline: true,
        predefined: {
          'Catalogue topic': {
            type: 'Catalogue topic'
          }
        },
        helpText: "<p>Keywords (preferably taken from a controlled vocabulary) categorising and describing the data resource.</p>\n<p>Good quality keywords help to improve the efficiency of search, making it easier to find relevant records.</p>"
      }), new ParentView({
        model: this.model,
        ModelType: InspireTheme,
        modelAttribute: 'inspireThemes',
        label: 'INSPIRE theme',
        ObjectInputView: InspireThemeView,
        helpText: "<p>If the resource falls within the scope of an INSPIRE theme it must be declared here.</p>\n<p>Conformity is the degree to which the <i class='text-red'>data</i> conforms to the relevant INSPIRE data specification.</p>"
      }), new CheckboxView({
        model: this.model,
        modelAttribute: 'notGEMINI',
        label: 'Exclude from GEMINI obligations',
        helpText: "\n<p>Tick this box to exclude this resource from GEMINI/INSPIRE obligations.</p><p <b class='text-red'><span class='fas fa-exclamation-triangle'>&nbsp;</span> WARNING.  This should only be ticked if the data DOES NOT relate to an area where an EU Member State exercises jurisdictional rights</b>.</p>\n"
      })]
    }, {
      label: 'Distribution',
      title: 'Distribution ,licensing and constraints',
      views: [new PredefinedParentView({
        model: this.model,
        modelAttribute: 'onlineResources',
        ModelType: OnlineResource,
        label: 'Online availability',
        ObjectInputView: OnlineResourceView,
        multiline: true,
        predefined: {
          'Data package': {
            url: 'https://data-package.ceh.ac.uk/data/{fileIdentifier}',
            name: 'Download the data',
            description: 'Download a copy of this data',
            "function": 'download'
          },
          'Order manager data': {
            url: 'https://order-eidc.ceh.ac.uk/resources/{ORDER_REF}}/order',
            name: 'Download the data',
            description: 'Download a copy of this data',
            "function": 'order'
          },
          'Direct access data': {
            url: 'https://catalogue.ceh.ac.uk/datastore/eidchub/{fileIdentifier}',
            name: 'Download the data',
            description: 'Download a copy of this data',
            "function": 'download'
          },
          'Supporting documents': {
            url: 'https://data-package.ceh.ac.uk/sd/{fileIdentifier}.zip',
            name: 'Supporting information',
            description: 'Supporting information available to assist in re-use of this dataset',
            "function": 'information'
          }
        },
        helpText: "<p>Include addresses of web services used to access the data and supporting information.</p>\n<p>Other links such as project websites or papers should <b>NOT</b> be included here. You can add them to \"Additional information\"</p>",
        disabled: disabled
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'distributionFormats',
        ModelType: DistributionFormat,
        label: 'Formats',
        ObjectInputView: DistributionFormatView,
        predefined: {
          CSV: {
            name: 'Comma-separated values (CSV)',
            type: 'text/csv',
            version: 'unknown'
          },
          'NetCDF v4': {
            name: 'NetCDF',
            type: 'application/netcdf',
            version: '4'
          },
          Shapefile: {
            name: 'Shapefile',
            type: '',
            version: 'unknown'
          },
          TIFF: {
            name: 'TIFF',
            type: 'image/tiff',
            version: 'unknown'
          }
        },
        helpText: "<p><b>Type</b> is the machine-readable media type.  If you do not know it, leave it blank.</p>\n<p><b>Version</b> is mandatory; if it's not applicable, enter '<i>unknown</i>'</p>"
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'useConstraints',
        label: 'Use constraints',
        ObjectInputView: ResourceConstraintView,
        multiline: true,
        predefined: {
          'Licence - OGL': {
            value: 'This resource is available under the terms of the Open Government Licence',
            uri: 'https://eidc.ceh.ac.uk/licences/OGL/plain',
            code: 'license'
          }
        },
        helpText: "<p>Describe any restrictions and legal prerequisites placed on the <strong>use</strong> of a data resource once it has been accessed. For example:</p>\n<ul class=\"list-unstyled\">\n  <li>\"Licence conditions apply\"</li>\n  <li>\"If you reuse this data you must cite \u2026\"</li>\n  <li>\"Do not use for navigation purposes\"</li>\n</ul>\n<p>Where possible include a link to a document describing the terms and conditions.</p>\n<p>You MUST enter something even if there are no constraints. In the rare case that there are none, enter \"no conditions apply\".</p>"
      })]
    }, {
      label: 'ID & relationships',
      title: 'Identifiers and links to related resources',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'resourceIdentifiers',
        label: 'Resource identifiers',
        ObjectInputView: ResourceIdentifierView,
        helpText: "\n<p>A unique string or number used to identify the data resource. The codespace identifies the context in which the code is unique.</p>\n",
        disabled: disabled
      }), new ParentView({
        model: this.model,
        modelAttribute: 'relatedRecords',
        label: 'Related records',
        ObjectInputView: RelatedRecordView,
        multiline: true
      })]
    }, {
      label: 'Spatial',
      title: 'Spatial characteristics',
      views: [new PredefinedParentView({
        model: this.model,
        modelAttribute: 'boundingBoxes',
        ModelType: geometryMap_BoundingBox,
        label: 'Spatial extent',
        ObjectInputView: BoundingBoxView,
        multiline: true,
        predefined: {
          Austria: {
            northBoundLatitude: 49.021,
            eastBoundLongitude: 17.161,
            southBoundLatitude: 46.372,
            westBoundLongitude: 9.531,
            extentName: 'Austria'
          },
          Belgium: {
            northBoundLatitude: 51.505,
            eastBoundLongitude: 6.407,
            southBoundLatitude: 49.497,
            westBoundLongitude: 2.546,
            extentName: 'Belgium'
          },
          Bulgaria: {
            northBoundLatitude: 44.216,
            eastBoundLongitude: 28.607,
            southBoundLatitude: 41.236,
            westBoundLongitude: 22.357,
            extentName: 'Bulgaria'
          },
          'Czech Republic': {
            northBoundLatitude: 51.055,
            eastBoundLongitude: 18.859,
            southBoundLatitude: 48.552,
            westBoundLongitude: 12.092,
            extentName: 'Czech Republic'
          },
          Denmark: {
            northBoundLatitude: 57.752,
            eastBoundLongitude: 15.193,
            southBoundLatitude: 54.560,
            westBoundLongitude: 8.076,
            extentName: 'Denmark'
          },
          Finland: {
            northBoundLatitude: 70.092,
            eastBoundLongitude: 31.586,
            southBoundLatitude: 59.766,
            westBoundLongitude: 19.312,
            extentName: 'Finland'
          },
          France: {
            northBoundLatitude: 51.089,
            eastBoundLongitude: 8.233,
            southBoundLatitude: 42.333,
            westBoundLongitude: -4.795,
            extentName: 'France'
          },
          Germany: {
            northBoundLatitude: 55.058,
            eastBoundLongitude: 15.041,
            southBoundLatitude: 47.270,
            westBoundLongitude: 5.868,
            extentName: 'Germany'
          },
          Greece: {
            northBoundLatitude: 41.749,
            eastBoundLongitude: 29.645,
            southBoundLatitude: 34.802,
            westBoundLongitude: 19.374,
            extentName: 'Greece'
          },
          Hungary: {
            northBoundLatitude: 48.585,
            eastBoundLongitude: 22.896,
            southBoundLatitude: 45.738,
            westBoundLongitude: 16.114,
            extentName: 'Hungary'
          },
          Israel: {
            northBoundLatitude: 33.290,
            eastBoundLongitude: 35.684,
            southBoundLatitude: 29.493,
            westBoundLongitude: 34.269,
            extentName: 'Israel'
          },
          Italy: {
            northBoundLatitude: 47.92,
            eastBoundLongitude: 18.519,
            southBoundLatitude: 35.493,
            westBoundLongitude: 6.627,
            extentName: 'Italy'
          },
          Latvia: {
            northBoundLatitude: 58.084,
            eastBoundLongitude: 28.241,
            southBoundLatitude: 55.675,
            westBoundLongitude: 20.971,
            extentName: 'Latvia'
          },
          Norway: {
            northBoundLatitude: 71.184,
            eastBoundLongitude: 31.168,
            southBoundLatitude: 57.960,
            westBoundLongitude: 4.503,
            extentName: 'Norway'
          },
          Poland: {
            northBoundLatitude: 54.836,
            eastBoundLongitude: 24.145,
            southBoundLatitude: 49.003,
            westBoundLongitude: 14.123,
            extentName: 'Poland'
          },
          Portugal: {
            northBoundLatitude: 42.154,
            eastBoundLongitude: -6.189,
            southBoundLatitude: 36.970,
            westBoundLongitude: -9.500,
            extentName: 'Portugal'
          },
          Romania: {
            northBoundLatitude: 48.264,
            eastBoundLongitude: 29.713,
            southBoundLatitude: 43.620,
            westBoundLongitude: 20.264,
            extentName: 'Romania'
          },
          Serbia: {
            northBoundLatitude: 46.189,
            eastBoundLongitude: 23.006,
            southBoundLatitude: 41.858,
            westBoundLongitude: 18.849,
            extentName: 'Serbia'
          },
          Slovakia: {
            northBoundLatitude: 49.614,
            eastBoundLongitude: 22.567,
            southBoundLatitude: 47.731,
            westBoundLongitude: 16.834,
            extentName: 'Slovakia'
          },
          Slovenia: {
            northBoundLatitude: 46.876,
            eastBoundLongitude: 16.597,
            southBoundLatitude: 45.422,
            westBoundLongitude: 13.375,
            extentName: 'Slovenia'
          },
          Spain: {
            northBoundLatitude: 43.788,
            eastBoundLongitude: 3.321,
            southBoundLatitude: 36.008,
            westBoundLongitude: -9.298,
            extentName: 'Spain'
          },
          Sweden: {
            northBoundLatitude: 69.060,
            eastBoundLongitude: 24.167,
            southBoundLatitude: 55.338,
            westBoundLongitude: 10.966,
            extentName: 'Sweden'
          },
          Switzerland: {
            northBoundLatitude: 47.807,
            eastBoundLongitude: 10.492,
            southBoundLatitude: 45.818,
            westBoundLongitude: 5.956,
            extentName: 'Switzerland'
          },
          'United Kingdom': {
            northBoundLatitude: 60.86,
            eastBoundLongitude: 1.77,
            southBoundLatitude: 49.86,
            westBoundLongitude: -8.65,
            extentName: 'United Kingdom',
            extentUri: 'http://sws.geonames.org/2635167'
          },
          World: {
            northBoundLatitude: 90.0,
            eastBoundLongitude: 180.0,
            southBoundLatitude: -90.0,
            westBoundLongitude: -180.0
          }
        },
        helpText: "    <p>A bounding box representing the limits of the data resource's study area.</p>\n    <p>If you do not wish to reveal the exact location publicly (for example, if locations are sensitive) it is recommended that you generalise the location.</p>    "
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'spatialReferenceSystems',
        label: 'Spatial reference systems',
        ObjectInputView: SpatialReferenceSystemView,
        predefined: {
          'British National Grid (EPSG::27700)': {
            code: 'http://www.opengis.net/def/crs/EPSG/0/27700',
            title: 'OSGB 1936 / British National Grid'
          },
          'GB place names': {
            code: 'https://data.ordnancesurvey.co.uk/datasets/opennames',
            title: 'GB place names'
          },
          'GB postcodes': {
            code: 'https://data.ordnancesurvey.co.uk/datasets/os-linked-data',
            title: 'GB postcodes'
          },
          'Lat/long (WGS84) (EPSG::4326)': {
            code: 'http://www.opengis.net/def/crs/EPSG/0/4326',
            title: 'WGS 84'
          },
          'Web mercator (EPSG::3857)': {
            code: 'http://www.opengis.net/def/crs/EPSG/0/3857',
            title: 'WGS 84 / Pseudo-Mercator'
          }
        },
        helpText: "\n    <p>The spatial referencing system used within the data resource.  <strong>This is mandatory for datasets</strong>; if the dataset has no spatial component (e.g. if it is a laboratory study) the resource type \u2018non-geographic data\u2019 should be used instead.</p>\n    "
      }), new SpatialRepresentationTypeView({
        model: this.model,
        modelAttribute: 'spatialRepresentationTypes',
        label: 'Spatial Representation Types'
      }), new ParentView({
        model: this.model,
        modelAttribute: 'spatialResolutions',
        ModelType: SpatialResolution,
        label: 'Spatial resolution',
        ObjectInputView: SpatialResolutionView,
        helpText: "\n    <p>This is an indication of the level of spatial detail/accuracy. Enter a distance OR equivalent scale but not both. For most datasets, <i>distance</i> is more appropriate.</p>For gridded data, distance is the area of the ground (in metres) represented in each pixel. For point data, it is the degree of confidence in the point's location (e.g. for a point expressed as a six-figure grid reference, SN666781, the resolution would be 100m)</p>\n    "
      })]
    }, {
      label: 'Quality',
      title: 'Quality',
      views: [new TextareaView({
        model: this.model,
        modelAttribute: 'lineage',
        label: 'Lineage',
        rows: 15,
        helpText: "<p>Information about the source data used in the construction of this data resource.</p>\n<p>Quality assessments and enhancement processes applied to the data resource can also be noted and summarised here.</p>"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'reasonChanged',
        label: 'Reason for change',
        rows: 7,
        helpText: "\n<p>If this record is being retracted, the reasons for withdrawal or replacement should be explained here.</p>\n"
      })]
    }, {
      label: 'Supplemental',
      title: 'Additional information and funding',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'supplemental',
        ModelType: Supplemental,
        multiline: true,
        label: 'Additional information',
        ObjectInputView: SupplementalView,
        helpText: "<p>You can add information not documented elsewhere here. This includes links to related papers, grey literature or websites.  For example:</p>\n<ul><li>papers that cite this resource</li><li>papers/reports that provide relevant supporting information but which do not cite this resource</li><li>project websites</li></ul>\n<p>When linking to published articles, please use DOIs whenever possible.</p>\n<p><small class='text-danger'><i class='fas fa-exclamation-triangle'> </i> NOTE: Some websites may be maintained for a limited period and may therefore soon become unavailable.</small></p>"
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'funding',
        ModelType: models_Funding,
        multiline: true,
        label: 'Funding',
        ObjectInputView: FundingView,
        predefined: {
          BBSRC: {
            funderName: 'Biotechnology and Biological Sciences Research Council',
            funderIdentifier: 'https://ror.org/00cwqg982'
          },
          Defra: {
            funderName: 'Department for Environment Food and Rural Affairs',
            funderIdentifier: 'https://ror.org/00tnppw48'
          },
          EPSRC: {
            funderName: 'Engineering and Physical Sciences Research Council',
            funderIdentifier: 'https://ror.org/0439y7842'
          },
          ESRC: {
            funderName: 'Economic and Social Research Council',
            funderIdentifier: 'https://ror.org/03n0ht308'
          },
          'Innovate UK': {
            funderName: 'Innovate UK',
            funderIdentifier: 'https://ror.org/05ar5fy68'
          },
          MRC: {
            funderName: 'Medical Research Council',
            funderIdentifier: 'https://ror.org/03x94j517'
          },
          NERC: {
            funderName: 'Natural Environment Research Council',
            funderIdentifier: 'https://ror.org/02b5d8509'
          },
          STFC: {
            funderName: 'Science and Technology Facilities Council',
            funderIdentifier: 'https://ror.org/057g20z61'
          }
        },
        helpText: "<p>Include here details of any grants or awards that were used to generate this resource.</p>\n<p>If you include funding information, the Funding body is MANDATORY, other fields are useful but optional.</p>\n<p>Award URL is either the unique identifier for the award or sa link to the funder's  grant page (if it exists). It is <b>NOT</b> a link to a project website.</p>",
        disabled: disabled
      })]
    }, {
      label: 'Web service',
      title: 'Web service details',
      views: [new ServiceView({
        model: this.model,
        modelAttribute: 'service',
        ModelType: Service,
        label: 'Service',
        disabled: disabled
      }), new ParentView({
        model: this.model,
        modelAttribute: 'mapDataDefinition.data',
        ModelType: MapDataSource,
        multiline: true,
        label: 'Web map service',
        ObjectInputView: MapDataSourceView,
        helpText: "<p>Link this metadata record to an ingested geospatial file and create a WMS (<strong>https://catalogue.ceh.ac.uk/maps/{METADATA_ID}?request=getCapabilities&service=WMS</strong>). The supported formats are:</p>\n<ul>\n  <li>Shapefiles - Vector (ignore the .shp extension when specifying the path) </li>\n  <li>GeoTiff - Raster</li>\n</ul>\n<p>To maximise performance, it is generally best to provide reprojected variants of data sources in common EPSG codes.</p>\n<p>Vector datasets should be spatially indexed (using <a href=\"http://mapserver.org/utilities/shptree.html\">shptree</a>)</p>\n<p>Raster datasets should be provided with <a href=\"http://www.gdal.org/gdaladdo.html\">overviews</a>. GeoTiff supports internal overviews.</p>\n<p>The 'Byte?' option that appears for raster (GeoTiff) datasets is used to indicate whether the GeoTiff is a 'byte' or 'non-byte' datatype.\nThis is only needed if you configure 'Stylying=Classification' for your GeoTiff.</p>\n<p>Paths should be specified relative to the base of the datastore. e.g. <strong>5b3fcf9f-19d4-4ad3-a8bb-0a5ea02c857e/my_shapefile</strong></p>",
        disabled: disabled
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/ElterLinkedEditorView.js



/* harmony default export */ const ElterLinkedEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    this.model.set('title', 'This title will be replaced on a successful document retrieval');
    this.sections = [{
      label: 'General',
      title: '',
      views: [new ReadOnlyView({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title'
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'linkedDocumentUri',
        label: 'Linked document URL',
        helpText: "<p>For creating linked documents, add the URL here.</p>\n<p>This should be a link to a metadata document in JSON format.</p>"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'linkedDocumentType',
        label: 'Linked document type',
        helpText: "\n<p>Enter the type of the linked document, if applicable.</p>\n"
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/geometryMap/Geometry.tpl
// Module
var Geometry_code = "<head> <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet-draw@1.0.2/dist/leaflet.draw-src.css\"/> </head> <div class=\"row\"> <div class=\"col-sm-2 col-lg-2\"> <button id=\"update\" class=\"editor-button\" title=\"Show/Update map\"><span class=\"fas fa-globe\" aria-hidden=\"true\"></span></button> </div> <div class=\"col-sm-6 col-lg-6\"> <div class=\"row\"> <div class=\"map\" style=\"width:500px;height:500px\"></div> </div> </div> </div> ";
// Exports
/* harmony default export */ const Geometry = (Geometry_code);
;// CONCATENATED MODULE: ./editor/src/geometryMap/GeometryView.js







/* harmony default export */ const GeometryView = (ObjectInputView.extend({
  events: {
    'click #update': 'viewMap'
  },
  initialize: function initialize() {
    (leaflet_src_default()).Icon.Default.imagePath = 'https://unpkg.com/leaflet-draw@1.0.2/dist/images/'; // fix for leaflet draw image bug

    this.template = index_all/* default.template */.ZP.template(Geometry);
    this.render();
  },
  createMap: function createMap() {
    this.map = new (leaflet_src_default()).Map(this.$('.map')[0], {
      center: new (leaflet_src_default()).LatLng(51.513, -0.09),
      zoom: 4
    });
    this.drawnItems = leaflet_src_default().featureGroup();

    if (this.model.get('geometryString')) {
      var parsedJson = JSON.parse(this.model.get('geometryString'));
      this.drawButtons = false;
      this.geometry = leaflet_src_default().geoJson(parsedJson);
      this.drawnItems.addLayer(this.geometry);
      this.shapeDrawn = true;
    } else {
      this.drawButtons = true;
      this.shapeDrawn = false;
    }

    if (this.shapeDrawn === true) {
      this.map.setView(this.geometry.getBounds().getCenter(), 4);
    }

    this.drawControl = this.createToolbar();
    this.drawnItems.addTo(this.map);
    var baseMaps = {
      Map: leaflet_src_default().tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }),
      Satellite: leaflet_src_default().tileLayer('http://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      })
    };
    leaflet_src_default().control.layers(baseMaps, {
      drawlayer: this.drawnItems
    }, {
      position: 'topright',
      collapsed: false
    }).addTo(this.map);
    this.map.addControl(this.drawControl);
    baseMaps.Map.addTo(this.map);
    this.listenTo(this.map, (leaflet_src_default()).Draw.Event.CREATED, function (event) {
      var layer = event.layer;
      this.drawButtons = false;
      this.model.set('geometryString', JSON.stringify(layer.toGeoJSON()));
      this.map.removeControl(this.drawControl);
      this.drawControl = this.createToolbar();
      this.map.addControl(this.drawControl);
      this.drawnItems.addLayer(layer);
    });
    this.listenTo(this.map, (leaflet_src_default()).Draw.Event.DELETED, function () {
      this.model.set('geometryString', null);
      this.drawButtons = true;
      this.map.removeControl(this.drawControl);
      this.drawControl = this.createToolbar();
      this.map.addControl(this.drawControl);
    });
  },
  createToolbar: function createToolbar() {
    if (this.drawButtons === true) {
      this.deleteButton = false;
    } else {
      this.deleteButton = true;
    }

    return new (leaflet_src_default()).Control.Draw({
      position: 'topleft',
      edit: {
        featureGroup: this.drawnItems,
        edit: false,
        remove: this.deleteButton
      },
      draw: {
        rectangle: false,
        polygon: this.drawButtons,
        polyline: false,
        marker: this.drawButtons,
        circle: false,
        circlemarker: false
      }
    });
  },
  viewMap: function viewMap() {
    if (this.map) {
      this.map.off();
      this.map.remove();
    }

    this.createMap();
  },
  render: function render() {
    ObjectInputView.prototype.render.apply(this);
    return this;
  }
}));
;// CONCATENATED MODULE: ./editor/src/geometryMap/Geometry.js

/* harmony default export */ const geometryMap_Geometry = (backbone_default().Model.extend({
  hasGeometry: function hasGeometry() {
    return this.has('geometryString');
  },
  getGeometry: function getGeometry() {
    return this.get('geometryString');
  },
  setGeometry: function setGeometry(geometry) {
    this.set('geometryString', geometry);
  },
  clearGeometry: function clearGeometry() {
    this.set('geometryString', null);
  }
}));
;// CONCATENATED MODULE: ./editor/src/geometryMap/index.js




;// CONCATENATED MODULE: ./editor/src/editors/ServiceAgreementEditorView.js






/* harmony default export */ const ServiceAgreementEditorView = (src/* EditorView.extend */.tk.extend({
  initialize: function initialize() {
    this.delegate({
      'click #exitWithoutSaving': 'exit'
    });
    this.delegate({
      'click #editorExit': 'attemptExit'
    });
    this.sections = [{
      label: 'General',
      title: '',
      views: [new TextOnlyView({
        model: this.model,
        text: "<h1>EIDC service agreement</h1><p>For more information/guidance about this document see <a href='https://eidc.ac.uk/support/agreement' target='_blank' rel='noopener noreferrer'>https://eidc.ac.uk/deposit/agreement</a></p>"
      }), new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'depositReference',
        label: 'Deposit Reference'
      }), new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'depositorName',
        label: 'Depositor Name'
      }), new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'depositorContactDetails',
        label: "Depositor's contact details"
      }), new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'eidcName',
        label: 'EIDC contact name'
      }), new TextOnlyView({
        model: this.model,
        text: "<h3>Data identification and citation</h3>\n<p><strong>PLEASE NOTE: once the dataset is published, the title and authors cannot be changed</strong><p>"
      }), new TextOnlyView({
        model: this.model,
        label: 'Title',
        text: "<p>Provide a brief title that best describes the data resource, <strong>not</strong> the project or activity from which the data were derived. Include references to the subject, spatial and temporal aspects of the data resource. <a href='https://eidc.ac.uk/deposit/metadata/guidance' target='_blank' rel='noopener noreferrer' >Further guidance is available on our website</a>.</p>\n"
      }), new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'title'
      }), new TextOnlyView({
        model: this.model,
        label: 'Authors',
        text: "<p>List authors below in the order in which they will appear in the citation.</p>\n<p>Author's names must be in the format <code>Surname &laquo;comma&raquo; Initial(s)</code>. For example, <code>Smith, K.P.</code> <strong>not</strong> <code>Kim P. Smith</code></p>\n<p>Authors' details will be published in a public data catalogue and held in EIDC systems.  UK law requires us to inform all individuals listed that they are being proposed as an author.  We therefore require a current, valid email address (or phone number) for all living authors.  Those without valid contact details are not eligible for authorship.  Please see our <a href='http://eidc.ceh.ac.uk/policies/privacy' target='_blank' rel='noopener noreferrer'>Privacy Notice</a> for further information</p>"
      }), new ParentView({
        model: this.model,
        ModelType: Author,
        modelAttribute: 'authors',
        ObjectInputView: AuthorView,
        multiline: true
      })]
    }, {
      label: 'The Data',
      title: 'The Data',
      views: [new TextOnlyView({
        model: this.model,
        label: 'Data retention',
        text: "<p>The EIDC's policy is to assign a DOI to all deposited data; such data will be kept in perpetuity.  If a DOI is not required or there are any other exceptions to this policy, please state them below.</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'policyExceptions',
        rows: 3
      }), new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'fileNumber',
        label: 'Number of files to be deposited'
      }), new TextOnlyView({
        model: this.model,
        label: 'Files',
        text: "<p>List the files to be deposited below - filenames must not include any spaces or special characters</p>\n<p>If there are a too many files to list separately, you can specify a naming convention below <strong>instead</strong>. (If doing so, please also indicate the total size of the deposit (e.g. 500Gb).)</p>"
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'files',
        ObjectInputView: FileView,
        predefined: {
          CSV: {
            format: 'csv'
          },
          NetCDF: {
            format: 'NetCDF'
          }
        },
        multiline: true
      }), new TextareaView({
        model: this.model,
        label: 'Naming convention',
        modelAttribute: 'fileNamingConvention',
        rows: 5,
        helpText: "\n<p>Specify a naming convention <strong>only</strong> if there are too many files to list individually.  Please also indicate the total size of the deposit (e.g. 500Gb).</p>\n"
      }), new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'transferMethod',
        label: 'Transfer Method',
        listAttribute: "\n<option value='Upload via EIDC catalogue' />\n"
      }), new TextOnlyView({
        model: this.model,
        label: 'Data Category',
        text: "<p>If the data are wholly or partly funded by NERC, the data must be categorised as either <strong>Environmental Data</strong> or <strong>Information Product</strong>.</p><p>Environmental data are '<i>individual items or records ... obtained by measurement, observation or modelling of the natural world... including all necessary calibration and quality control. This includes data generated through complex systems, such as ... models, including the model code used to produce the data.</i>' </p><p>Information Products are '<i>created by adding a level of intellectual input that refines or adds value to data through interpretation and/or combination with other data</i>'.</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'dataCategory',
        ObjectInputView: CategoryView
      })]
    }, {
      label: 'Supporting documentation',
      title: 'Supporting documentation',
      views: [new TextOnlyView({
        model: this.model,
        label: 'Document(s) to be provided',
        text: "<p>Please provide the title and file extension of document(s) you will provide to enable re-use of the data (see <a href=\"https://eidc.ac.uk/deposit/supportingDocumentation\">https://eidc.ac.uk/deposit/supportingDocumentation</a>).</a>\n<p>Describe the content of the documentation to be supplied. Mandatory elements are:</p>\n<ul><li>Collection/generation methods</li><li>Nature and Units of recorded values</li><li>Quality control</li><li>Details of data structure</li></ul>\n<p>Required elements (if appropriate) include:</p>\n<ul><li>Experimental design/Sampling regime</li><li>Fieldwork and laboratory instrumentation</li><li>Calibration steps and values</li><li>Analytical methods</li><li>Any other information useful to the interpretation of the data</li></ul>"
      }), new ParentView({
        model: this.model,
        ModelType: SupportingDoc,
        label: 'Supporting documnents',
        modelAttribute: 'supportingDocs',
        ObjectInputView: SupportingDocView,
        multiline: true
      })]
    }, {
      label: 'Availability, access and licensing',
      title: 'Availability, access and licensing',
      views: [new TextOnlyView({
        model: this.model,
        label: 'End user licence',
        text: "\n<p>Please state under which licence the data will be made available. the vast majority of NERC-funded data are provided under the Open Government Licence. We recommend that you seek guidance from your institution and/or funding agency as to the appropriate licence.</p>\n"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'endUserLicence',
        ObjectInputView: EndUserLicenceView
      }), new ParentView({
        model: this.model,
        ModelType: RightsHolder,
        modelAttribute: 'ownersOfIpr',
        label: 'Owner of IPR',
        ObjectInputView: RightsHolderView,
        multiline: true
      }), new TextOnlyView({
        model: this.model,
        label: 'Availability',
        text: "<p>Depositors may request that access to the data be restricted for an agreed period (embargoed).  Embargoes and embargo periods may be subject to funder requirements. For NERC-funded research, a reasonable embargo period is considered to be a maximum of two years <strong>from the end of data collection.</strong></p>\n<p>If an embargo is required, please specify below.</p>"
      }), new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'availability',
        typeAttribute: 'date'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'useConstraints',
        label: 'Additional Use Constraints',
        rows: 3
      })]
    }, {
      label: 'Legislation & funding',
      title: 'Legislation & funding',
      views: [new TextOnlyView({
        model: this.model,
        label: 'Other policies/legislation',
        text: "<p>All environmental data deposited into the EIDC are subject to the requirements of the <a href='https://nerc.ukri.org/research/sites/environmental-data-service-eds/policy/' target='_blank' rel='noopener noreferrer'>NERC Data Policy.</a></p>\n<p>By depositing data, you confirm that the data is compliant with the provisions of UK data protection laws.</p>\n<p>Data and supporting documentation should not contain names, addresses or other personal information relating to 'identifiable natural persons'.  Discovery metadata (the catalogue record) may contain names and contact details of the authors of this data (<a href='https://eidc.ac.uk/policies/retentionPersonalData' target='_blank' rel='noopener noreferrer'>see our policy on retention and use of personal data</a>).</p>\n<p>If other policies/legislation applies, please specify below.</p>"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'otherPoliciesOrLegislation',
        rows: 5
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'funding',
        ModelType: models_Funding,
        multiline: true,
        label: 'Grants/awards used to generate this resource',
        ObjectInputView: FundingView,
        predefined: {
          NERC: {
            funderName: 'Natural Environment Research Council',
            funderIdentifier: 'https://ror.org/02b5d8509'
          },
          BBSRC: {
            funderName: 'Biotechnology and Biological Sciences Research Council',
            funderIdentifier: 'https://ror.org/00cwqg982'
          },
          Defra: {
            funderName: 'Department for Environment Food and Rural Affairs',
            funderIdentifier: 'https://ror.org/00tnppw48'
          },
          EPSRC: {
            funderName: 'Engineering and Physical Sciences Research Council',
            funderIdentifier: 'https://ror.org/0439y7842'
          },
          ESRC: {
            funderName: 'Economic and Social Research Council',
            funderIdentifier: 'https://ror.org/03n0ht308'
          },
          'Innovate UK': {
            funderName: 'Innovate UK',
            funderIdentifier: 'https://ror.org/05ar5fy68'
          },
          MRC: {
            funderName: 'Medical Research Council',
            funderIdentifier: 'https://ror.org/03x94j517'
          },
          STFC: {
            funderName: 'Science and Technology Facilities Council',
            funderIdentifier: 'https://ror.org/057g20z61'
          }
        }
      })]
    }, {
      label: 'Miscellaneous',
      title: 'Miscellaneous',
      views: [new TextOnlyView({
        model: this.model,
        label: 'Superseding existing data',
        text: "\n<p>If the data is superseding an existing dataset held by the EIDC, please specify and explain why it is to be replaced. Include details of any errors found.</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'supersededData',
        rows: 5
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'relatedDataHoldings',
        label: 'Related Data Holdings',
        rows: 5
      }), new TextOnlyView({
        model: this.model,
        label: 'Other info',
        text: "\n<p>If there is any other information you wish to provide, please include it below.</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'otherInfo',
        rows: 7
      })]
    }, {
      label: 'Discovery metadata',
      title: 'Discovery metadata',
      views: [new TextOnlyView({
        model: this.model,
        text: "<p>Data resources deposited with the EIDC have an entry in the EIDC data catalogue, enabling users to find and access them. Please provide the following information to help complete the catalogue record. <a href='https://eidc.ac.uk/deposit/metadata/guidance' target='_blank' rel='noopener noreferrer' >Further guidance is available on our website</a>.</p>\n<p><em>Please note, this information is not fixed and may be subject to change and improvement over time</em></p>"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Description (min 100 characters)',
        rows: 12,
        helpText: "<p>The description should describe the data resource in question, NOT the project/activity which produced it.</p>\n<p>The description is an 'executive summary' that allows the reader to determine the relevance and usefulness of the resource.  The text should be concise but should contain sufficient detail to allow the reader to ascertain rapidly the scope and limitations of the resource.</p>\n<p>Write in plain English; in other words, write complete sentences rather than fragments.  It is recommended that the abstract is organised using the \"What, Where, When, How, Why, Who\" structure.</p>"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'lineage',
        label: 'Lineage',
        rows: 10,
        helpText: "<p>Information about the source data used in the construction of this data resource.</p>\n<p>Quality assessments and enhancement processes applied to the data resource can also be noted and summarised here.</p>"
      }), new PredefinedParentView({
        model: this.model,
        ModelType: DescriptiveKeyword,
        modelAttribute: 'descriptiveKeywords',
        label: 'Keywords',
        ObjectInputView: DescriptiveKeywordView,
        multiline: true,
        predefined: {
          'Catalogue topic': {
            type: 'Catalogue topic'
          }
        },
        helpText: "<p>Keywords (preferably taken from a controlled vocabulary) categorising and describing the data resource.</p>\n<p>Good quality keywords help to improve the efficiency of search, making it easier to find relevant records.</p>"
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'areaOfStudy',
        ModelType: geometryMap_BoundingBox,
        label: 'Area of Study',
        ObjectInputView: BoundingBoxView,
        multiline: true,
        predefined: {
          England: {
            northBoundLatitude: 55.812,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -6.452,
            extentName: 'England',
            extentUri: 'http://sws.geonames.org/6269131'
          },
          'Great Britain': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648,
            extentName: 'Great Britain'
          },
          'Northern Ireland': {
            northBoundLatitude: 55.313,
            eastBoundLongitude: -5.432,
            southBoundLatitude: 54.022,
            westBoundLongitude: -8.178,
            extentName: 'Northern Ireland',
            extentUri: 'http://sws.geonames.org/2641364'
          },
          Scotland: {
            northBoundLatitude: 60.861,
            eastBoundLongitude: -0.728,
            southBoundLatitude: 54.634,
            westBoundLongitude: -8.648,
            extentName: 'Scotland',
            extentUri: 'http://sws.geonames.org/2638360'
          },
          'United Kingdom': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648,
            extentName: 'United Kingdom',
            extentUri: 'http://sws.geonames.org/2635167'
          },
          Wales: {
            northBoundLatitude: 53.434,
            eastBoundLongitude: -2.654,
            southBoundLatitude: 51.375,
            westBoundLongitude: -5.473,
            extentName: 'Wales',
            extentUri: 'http://sws.geonames.org/2634895'
          },
          World: {
            northBoundLatitude: 90.00,
            eastBoundLongitude: 180.00,
            southBoundLatitude: -90.00,
            westBoundLongitude: -180.00
          }
        }
      })]
    }];
    return src/* EditorView.prototype.initialize.apply */.tk.prototype.initialize.apply(this);
  },
  attemptExit: function attemptExit() {
    if (this.saveRequired) {
      this.$('#confirmExit').modal('show');
    } else {
      this.exit();
    }
  },
  exit: function exit() {
    this.$('#confirmExit').modal('hide');

    index_all/* default.invoke */.ZP.invoke(this.sections, 'remove');

    this.remove();

    if ((backbone_default()).history.location.pathname === "/documents/".concat(this.model.get('id'))) {
      return backbone_default().history.location.replace("/service-agreement/".concat(this.model.get('id')));
    } else {
      return backbone_default().history.location.reload();
    }
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/UkemsDocumentEditorView.js




/* harmony default export */ const UkemsDocumentEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'dataset');
    }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Description',
        rows: 12
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'datasetReferenceDate',
        ModelType: MultipleDate,
        label: 'Reference dates',
        ObjectInputView: DatasetReferenceDateView,
        helpText: "<p><b>Created</b> is the date on which the data resource was originally created.</p>\n<p><b>Published</b> is the date when this metadata record is made available publicly.</p>\n<p>For embargoed resources, <b>Release(d)</b> is the date on which the embargo was lifted <i class='text-red'><b>or is due to be lifted</b></i>.</p>\n<p><b>Superseded</b> is the date on which the resource was superseded by another resource (where relevant).</p>"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView
      })]
    }, {
      label: 'Time & Space',
      title: 'Time & Space',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'temporalExtents',
        ModelType: MultipleDate,
        label: 'Temporal extent',
        ObjectInputView: TemporalExtentView
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'spatialReferenceSystems',
        label: 'Spatial reference systems',
        ObjectInputView: SpatialReferenceSystemView,
        predefined: {
          'British National Grid (EPSG::27700)': {
            code: 'http://www.opengis.net/def/crs/EPSG/0/27700',
            title: 'OSGB 1936 / British National Grid'
          },
          'GB place names': {
            code: 'https://data.ordnancesurvey.co.uk/datasets/opennames',
            title: 'GB place names'
          },
          'GB postcodes': {
            code: 'https://data.ordnancesurvey.co.uk/datasets/os-linked-data',
            title: 'GB postcodes'
          },
          'Lat/long (WGS84) (EPSG::4326)': {
            code: 'http://www.opengis.net/def/crs/EPSG/0/4326',
            title: 'WGS 84'
          },
          'Web mercator (EPSG::3857)': {
            code: 'http://www.opengis.net/def/crs/EPSG/0/3857',
            title: 'WGS 84 / Pseudo-Mercator'
          }
        },
        helpText: "\n<p>The spatial referencing system used within the data resource.  <strong>This is mandatory for datasets</strong>; if the dataset has no spatial component (e.g. if it is a laboratory study) the resource type \u2018non-geographic data\u2019 should be used instead.</p>\n"
      }), new SpatialRepresentationTypeView({
        model: this.model,
        modelAttribute: 'spatialRepresentationTypes',
        label: 'Spatial Representation Types'
      }), new ParentView({
        model: this.model,
        modelAttribute: 'spatialResolutions',
        ModelType: SpatialResolution,
        label: 'Spatial resolution',
        ObjectInputView: SpatialResolutionView,
        helpText: "\n<p>This is an indication of the level of spatial detail/accuracy. Enter a distance OR equivalent scale but not both. For most datasets, <i>distance</i> is more appropriate.</p>For gridded data, distance is the area of the ground (in metres) represented in each pixel. For point data, it is the degree of confidence in the point's location (e.g. for a point expressed as a six-figure grid reference, SN666781, the resolution would be 100m)</p>\n"
      })]
    }, {
      label: 'Data formats',
      title: 'Data formats',
      views: [new PredefinedParentView({
        model: this.model,
        modelAttribute: 'distributionFormats',
        ModelType: DistributionFormat,
        label: 'Data files',
        ObjectInputView: DistributionFormatView,
        predefined: {
          CSV: {
            name: 'Comma-separated values (CSV)',
            type: 'text/csv',
            version: 'unknown'
          },
          'NetCDF v4': {
            name: 'NetCDF',
            type: 'application/netcdf',
            version: '4'
          }
        },
        helpText: "<p><b>Name</b> is the filename (including extension) and is mandatory.</p>\n<p><b>Type</b> is the machine-readable media type.  If you do not know it, leave it blank.</p>\n<p><b>Version</b> is mandatory; if it's not applicable, enter '<i>unknown</i>'</p>"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'supplemental',
        ModelType: Supplemental,
        multiline: true,
        label: 'Additional information',
        ObjectInputView: SupplementalView,
        helpText: "<p>You can add information not documented elsewhere here. This includes links to related papers, grey literature or websites.  For example:</p>\n<ul><li>papers that cite this resource</li><li>papers/reports that provide relevant supporting information but which do not cite this resource</li><li>project websites</li></ul>\n<p>When linking to published articles, please use DOIs whenever possible.</p>\n<p><small class='text-danger'><i class='fas fa-exclamation-triangle'> </i> NOTE: Some websites may be maintained for a limited period and may therefore soon become unavailable.</small></p>"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'units',
        label: 'Units'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'provenance',
        label: 'Provenance information',
        rows: 12
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/GeminiEditorView.js









/* harmony default export */ const GeminiEditorView = (EditorView/* default.extend */.Z.extend({
  initialize: function initialize() {
    var disabled = jquery_default()(jquery_default()('body')[0]).data('edit-restricted');
    this.sections = [{
      label: 'General',
      title: '',
      views: [new ReadOnlyView({
        model: this.model,
        modelAttribute: 'id',
        label: 'File identifier'
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'resourceType',
        ModelType: ResourceType,
        label: 'Resource Type',
        ObjectInputView: ResourceTypeView
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title',
        helpText: "<p>Provide a title that best describes that data resource. Include references to the subject, spatial and temporal aspects of the data resource.</p>\n<p>Only the leading letter and proper nouns of the title should be capitalised.  If it's necessary to include acronyms in the title, then include both the acronym (in parentheses) and the phrase/word from which it was formed. Acronyms should not include full-stops between each letter.</p>\n<p>If there are multiple titles or translations of titles (e.g. in Welsh), these should be added as alternative titles.</p>"
      }), new ParentStringView({
        model: this.model,
        modelAttribute: 'alternateTitles',
        label: 'Alternative titles',
        helpText: "<p>Alternative titles allow you to add multiple titles and non-English translations of titles (e.g. Welsh).</p>\n<p>Only the leading letter and proper nouns of titles should be capitalised. If the title includes acronyms, include both the acronym (in parentheses) and its definition. Acronyms should not include full-stops between each letter.</p>"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Description',
        rows: 12,
        helpText: "<p>The description should describe the data resource in question, NOT the project/activity which produced it.</p>\n<p>The description is an 'executive summary' that allows the reader to determine the relevance and usefulness of the resource.  The text should be concise but should contain sufficient detail to allow the reader to ascertain rapidly the scope and limitations of the resource.</p>\n<p>Write in plain English; in other words, write complete sentences rather than fragments.  It is recommended that the abstract is organised using the \"What, Where, When, How, Why, Who\" structure.</p>"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'accessLimitation',
        ModelType: AccessLimitation,
        label: 'Resource status',
        ObjectInputView: AccessLimitationView,
        helpText: "<p>Access status of resource.  For example, is the resource embargoed or are restrictions imposed for reasons of confidentiality or security.</p>\n<p><b>NOTE</b>: if access is Embargoed, you must also complete the <i>Release date</i>.</p>"
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'datasetReferenceDate',
        ModelType: MultipleDate,
        label: 'Reference dates',
        ObjectInputView: DatasetReferenceDateView,
        helpText: "<p><b>Created</b> is the date on which the data resource was originally created.</p>\n<p><b>Published</b> is the date when this metadata record is made available publicly.</p>\n<p>For embargoed resources, <b>Release(d)</b> is the date on which the embargo was lifted <i class='text-red'><b>or is due to be lifted</b></i>.</p>\n<p><b>Superseded</b> is the date on which the resource was superseded by another resource (where relevant).</p>"
      }), new InputView/* default */.Z({
        model: this.model,
        modelAttribute: 'version',
        typeAttribute: 'number',
        label: 'Version'
      }), new ParentView({
        model: this.model,
        modelAttribute: 'temporalExtents',
        ModelType: MultipleDate,
        label: 'Temporal extent',
        ObjectInputView: TemporalExtentView,
        helpText: "\n<p>The time period(s) the data resource covers.  This is often the same as the data capture period but it need not be so.</p>\n"
      })]
    }, {
      label: 'Authors & contacts',
      title: 'Authors and other contacts',
      views: [new PredefinedParentView({
        model: this.model,
        ModelType: models_Contact,
        modelAttribute: 'responsibleParties',
        label: 'Contacts',
        ObjectInputView: ContactView,
        multiline: true,
        predefined: {
          'Author - UKCEH': {
            organisationName: 'UK Centre for Ecology & Hydrology',
            role: 'author',
            email: 'enquiries@ceh.ac.uk',
            organisationIdentifier: 'https://ror.org/00pggkr55'
          },
          'Author - unaffiliated': {
            organisationName: 'Unaffiliated',
            role: 'author'
          },
          'Custodian - EIDC': {
            organisationName: 'NERC EDS Environmental Information Data Centre',
            role: 'custodian',
            email: 'info@eidc.ac.uk',
            organisationIdentifier: 'https://ror.org/04xw4m193'
          },
          'Point of contact - UKCEH Bangor': {
            organisationName: 'UK Centre for Ecology & Hydrology',
            role: 'pointOfContact',
            email: 'enquiries@ceh.ac.uk',
            organisationIdentifier: 'https://ror.org/00pggkr55',
            address: {
              deliveryPoint: 'Environment Centre Wales, Deiniol Road',
              postalCode: 'LL57 2UW',
              city: 'Bangor',
              administrativeArea: 'Gwynedd',
              country: 'United Kingdom'
            }
          },
          'Point of contact - UKCEH Edinburgh': {
            organisationName: 'UK Centre for Ecology & Hydrology',
            role: 'pointOfContact',
            email: 'enquiries@ceh.ac.uk',
            organisationIdentifier: 'https://ror.org/00pggkr55',
            address: {
              deliveryPoint: 'Bush Estate',
              postalCode: 'EH26 0QB',
              city: 'Penicuik',
              administrativeArea: 'Midlothian',
              country: 'United Kingdom'
            }
          },
          'Point of contact - UKCEH Lancaster': {
            organisationName: 'UK Centre for Ecology & Hydrology',
            role: 'pointOfContact',
            email: 'enquiries@ceh.ac.uk',
            organisationIdentifier: 'https://ror.org/00pggkr55',
            address: {
              deliveryPoint: 'Lancaster Environment Centre, Library Avenue, Bailrigg',
              postalCode: 'LA1 4AP',
              city: 'Lancaster',
              administrativeArea: 'Lancashire',
              country: 'United Kingdom'
            }
          },
          'Point of contact - UKCEH Wallingford': {
            organisationName: 'UK Centre for Ecology & Hydrology',
            role: 'pointOfContact',
            email: 'enquiries@ceh.ac.uk',
            organisationIdentifier: 'https://ror.org/00pggkr55',
            address: {
              deliveryPoint: 'Maclean Building, Benson Lane, Crowmarsh Gifford',
              postalCode: 'OX10 8BB',
              city: 'Wallingford',
              administrativeArea: 'Oxfordshire',
              country: 'United Kingdom'
            }
          },
          'Publisher - EIDC': {
            organisationName: 'NERC EDS Environmental Information Data Centre',
            role: 'publisher',
            email: 'info@eidc.ac.uk',
            organisationIdentifier: 'https://ror.org/04xw4m193'
          },
          'Rights holder - UKCEH': {
            organisationName: 'UK Centre for Ecology & Hydrology',
            role: 'rightsHolder',
            email: 'enquiries@ceh.ac.uk',
            organisationIdentifier: 'https://ror.org/00pggkr55'
          }
        },
        helpText: "<p>The names of authors should be in the format <code>Surname, First Initial. Second Initial.</code> For example <i>Brown, A.B.</i></p>\n<p>Role and organisation name are mandatory. If email address is blank it is assumed to be 'enquiries@ceh.ac.uk'.</p>\n<p>The preferred identifier for individuals is an ORCiD.  You must enter the identifier as a <i>fully qualified</i> ID (e.g.  <b>https://orcid.org/1234-5678-0123-987X</b> rather than <b>1234-5678-0123-987X</b>).</p>"
      })]
    }, {
      label: 'Classification',
      title: 'Categories and keywords',
      views: [new ParentView({
        model: this.model,
        ModelType: TopicCategory,
        modelAttribute: 'topicCategories',
        label: 'Topic categories',
        ObjectInputView: TopicCategoryView,
        helpText: "<p>Please note these are very broad themes and should not be confused with EIDC science topics.</p>\n<p>Multiple topic categories are allowed - please include all that are pertinent.  For example, \"Estimates of topsoil invertebrates\" = Biota AND Environment AND Geoscientific Information.</p>"
      }), new PredefinedParentView({
        model: this.model,
        ModelType: DescriptiveKeyword,
        modelAttribute: 'descriptiveKeywords',
        label: 'Keywords',
        ObjectInputView: DescriptiveKeywordView,
        multiline: true,
        predefined: {
          'Catalogue topic': {
            type: 'Catalogue topic'
          }
        },
        helpText: "<p>Keywords (preferably taken from a controlled vocabulary) categorising and describing the data resource.</p>\n<p>Good quality keywords help to improve the efficiency of search, making it easier to find relevant records.</p>"
      }), new ParentView({
        model: this.model,
        ModelType: InspireTheme,
        modelAttribute: 'inspireThemes',
        label: 'INSPIRE theme',
        ObjectInputView: InspireThemeView,
        helpText: "<p>If the resource falls within the scope of an INSPIRE theme it must be declared here.</p>\n<p>Conformity is the degree to which the <i class='text-red'>data</i> conforms to the relevant INSPIRE data specification.</p>"
      }), new CheckboxView({
        model: this.model,
        modelAttribute: 'notGEMINI',
        label: 'Exclude from GEMINI obligations',
        helpText: "\n<p>Tick this box to exclude this resource from GEMINI/INSPIRE obligations.</p><p <b class='text-red'><span class='fas fa-exclamation-triangle'>&nbsp;</span> WARNING.  This should only be ticked if the data DOES NOT relate to an area where an EU Member State exercises jurisdictional rights</b>.</p>\n"
      })]
    }, {
      label: 'Distribution',
      title: 'Distribution ,licensing and constraints',
      views: [new PredefinedParentView({
        model: this.model,
        modelAttribute: 'onlineResources',
        ModelType: OnlineResource,
        label: 'Online availability',
        ObjectInputView: OnlineResourceView,
        multiline: true,
        predefined: {
          'Data package': {
            url: 'https://data-package.ceh.ac.uk/data/{fileIdentifier}',
            name: 'Download the data',
            description: 'Download a copy of this data',
            "function": 'download'
          },
          'Order manager data': {
            url: 'https://order-eidc.ceh.ac.uk/resources/{ORDER_REF}}/order',
            name: 'Download the data',
            description: 'Download a copy of this data',
            "function": 'order'
          },
          'Direct access data': {
            url: 'https://catalogue.ceh.ac.uk/datastore/eidchub/{fileIdentifier}',
            name: 'Download the data',
            description: 'Download a copy of this data',
            "function": 'download'
          },
          'Supporting documents': {
            url: 'https://data-package.ceh.ac.uk/sd/{fileIdentifier}.zip',
            name: 'Supporting information',
            description: 'Supporting information available to assist in re-use of this dataset',
            "function": 'information'
          }
        },
        helpText: "<p>Include addresses of web services used to access the data and supporting information.</p>\n<p>Other links such as project websites or papers should <b>NOT</b> be included here. You can add them to \"Additional information\"</p>",
        disabled: disabled
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'distributionFormats',
        ModelType: DistributionFormat,
        label: 'Formats',
        ObjectInputView: DistributionFormatView,
        predefined: {
          CSV: {
            name: 'Comma-separated values (CSV)',
            type: 'text/csv',
            version: 'unknown'
          },
          'NetCDF v4': {
            name: 'NetCDF',
            type: 'application/netcdf',
            version: '4'
          },
          Shapefile: {
            name: 'Shapefile',
            type: '',
            version: 'unknown'
          },
          TIFF: {
            name: 'TIFF',
            type: 'image/tiff',
            version: 'unknown'
          }
        },
        helpText: "<p><b>Type</b> is the machine-readable media type.  If you do not know it, leave it blank.</p>\n<p><b>Version</b> is mandatory; if it's not applicable, enter '<i>unknown</i>'</p>"
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'useConstraints',
        label: 'Use constraints',
        ObjectInputView: ResourceConstraintView,
        multiline: true,
        predefined: {
          'Licence - OGL': {
            value: 'This resource is available under the terms of the Open Government Licence',
            uri: 'https://eidc.ceh.ac.uk/licences/OGL/plain',
            code: 'license'
          }
        },
        helpText: "<p>Describe any restrictions and legal prerequisites placed on the <strong>use</strong> of a data resource once it has been accessed. For example:</p>\n<ul class=\"list-unstyled\">\n  <li>\"Licence conditions apply\"</li>\n  <li>\"If you reuse this data you must cite \u2026\"</li>\n  <li>\"Do not use for navigation purposes\"</li>\n</ul>\n<p>Where possible include a link to a document describing the terms and conditions.</p>\n<p>You MUST enter something even if there are no constraints. In the rare case that there are none, enter \"no conditions apply\".</p>"
      }), new PredefinedParentView({
        model: this.model,
        ModelType: models_Contact,
        modelAttribute: 'distributorContacts',
        label: 'Distributor contact',
        ObjectInputView: ContactView,
        multiline: true,
        predefined: {
          EIDC: {
            organisationName: 'NERC EDS Environmental Information Data Centre',
            role: 'distributor',
            email: 'info@eidc.ac.uk',
            organisationIdentifier: 'https://ror.org/04xw4m193'
          },
          'EMBL-EBI': {
            organisationName: 'The European Bioinformatics Institute (EMBL-EBI)',
            role: 'distributor'
          },
          'Other distributor': {
            role: 'distributor'
          }
        },
        helpText: "\n<p>The organisation responsible for distributing the data resource</p>\n"
      })]
    }, {
      label: 'ID & relationships',
      title: 'Identifiers and links to related resources',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'resourceIdentifiers',
        label: 'Resource identifiers',
        ObjectInputView: ResourceIdentifierView,
        helpText: "\n<p>A unique string or number used to identify the data resource. The codespace identifies the context in which the code is unique.</p>\n",
        disabled: disabled
      }), new ParentView({
        model: this.model,
        modelAttribute: 'relatedRecords',
        label: 'Related records',
        ObjectInputView: RelatedRecordView,
        multiline: true,
        disabled: disabled
      })]
    }, {
      label: 'Spatial',
      title: 'Spatial characteristics',
      views: [new PredefinedParentView({
        model: this.model,
        modelAttribute: 'boundingBoxes',
        ModelType: geometryMap_BoundingBox,
        label: 'Spatial extent',
        ObjectInputView: BoundingBoxView,
        multiline: true,
        predefined: {
          England: {
            northBoundLatitude: 55.812,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -6.452,
            extentName: 'England',
            extentUri: 'http://sws.geonames.org/6269131'
          },
          'Great Britain': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648,
            extentName: 'Great Britain'
          },
          'Northern Ireland': {
            northBoundLatitude: 55.313,
            eastBoundLongitude: -5.432,
            southBoundLatitude: 54.022,
            westBoundLongitude: -8.178,
            extentName: 'Northern Ireland',
            extentUri: 'http://sws.geonames.org/2641364'
          },
          Scotland: {
            northBoundLatitude: 60.861,
            eastBoundLongitude: -0.728,
            southBoundLatitude: 54.634,
            westBoundLongitude: -8.648,
            extentName: 'Scotland',
            extentUri: 'http://sws.geonames.org/2638360'
          },
          'United Kingdom': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648,
            extentName: 'United Kingdom',
            extentUri: 'http://sws.geonames.org/2635167'
          },
          Wales: {
            northBoundLatitude: 53.434,
            eastBoundLongitude: -2.654,
            southBoundLatitude: 51.375,
            westBoundLongitude: -5.473,
            extentName: 'Wales',
            extentUri: 'http://sws.geonames.org/2634895'
          },
          World: {
            northBoundLatitude: 90.00,
            eastBoundLongitude: 180.00,
            southBoundLatitude: -90.00,
            westBoundLongitude: -180.00
          }
        },
        helpText: "    <p>A bounding box representing the limits of the data resource's study area.</p>\n    <p>If you do not wish to reveal the exact location publicly (for example, if locations are sensitive) it is recommended that you generalise the location.</p>    "
      }), new ParentView({
        model: this.model,
        modelAttribute: 'geometries',
        ModelType: geometryMap_Geometry,
        label: 'Spatial extent geometry',
        ObjectInputView: GeometryView,
        multiline: true,
        helpText: "    <p>A polygon representing the limits of the data resource's study area or a marker representing a specific study location.</p>\n    <p>If you do not wish to reveal the exact location publicly (for example, if locations are sensitive) it is recommended that you generalise the location.</p>    "
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'spatialReferenceSystems',
        label: 'Spatial reference systems',
        ObjectInputView: SpatialReferenceSystemView,
        predefined: {
          'British National Grid (EPSG::27700)': {
            code: 'http://www.opengis.net/def/crs/EPSG/0/27700',
            title: 'OSGB 1936 / British National Grid'
          },
          'GB place names': {
            code: 'https://data.ordnancesurvey.co.uk/datasets/opennames',
            title: 'GB place names'
          },
          'GB postcodes': {
            code: 'https://data.ordnancesurvey.co.uk/datasets/os-linked-data',
            title: 'GB postcodes'
          },
          'Lat/long (WGS84) (EPSG::4326)': {
            code: 'http://www.opengis.net/def/crs/EPSG/0/4326',
            title: 'WGS 84'
          },
          'Web mercator (EPSG::3857)': {
            code: 'http://www.opengis.net/def/crs/EPSG/0/3857',
            title: 'WGS 84 / Pseudo-Mercator'
          }
        },
        helpText: "\n    <p>The spatial referencing system used within the data resource.  <strong>This is mandatory for datasets</strong>; if the dataset has no spatial component (e.g. if it is a laboratory study) the resource type \u2018non-geographic data\u2019 should be used instead.</p>\n    "
      }), new SpatialRepresentationTypeView({
        model: this.model,
        modelAttribute: 'spatialRepresentationTypes',
        label: 'Spatial Representation Types'
      }), new ParentView({
        model: this.model,
        modelAttribute: 'spatialResolutions',
        ModelType: SpatialResolution,
        label: 'Spatial resolution',
        ObjectInputView: SpatialResolutionView,
        helpText: "\n    <p>This is an indication of the level of spatial detail/accuracy.</p><p>For gridded data, distance is the area of the ground (in metres) represented in each pixel. For point data, it is the degree of confidence in the point's location (e.g. for a point expressed as a six-figure grid reference, SN666781, the resolution would be 100m)</p>\n    "
      })]
    }, {
      label: 'Quality',
      title: 'Quality',
      views: [new TextareaView({
        model: this.model,
        modelAttribute: 'lineage',
        label: 'Lineage',
        rows: 15,
        helpText: "<p>Information about the source data used in the construction of this data resource.</p>\n<p>Quality assessments and enhancement processes applied to the data resource can also be noted and summarised here.</p>"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'reasonChanged',
        label: 'Reason for change',
        rows: 7,
        helpText: "\n<p>If this record is being retracted, the reasons for withdrawal or replacement should be explained here.</p>\n"
      }), new ParentView({
        model: this.model,
        modelAttribute: 'resourceMaintenance',
        label: 'Resource maintenance',
        ObjectInputView: ResourceMaintenanceView,
        helpText: "\n<p>This states how often the updated data resource is made available to the user.  For the vast majority of EIDC data, this value will be \"not planned\".</p>\n"
      })]
    }, {
      label: 'Supplemental',
      title: 'Additional information and funding',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'supplemental',
        ModelType: Supplemental,
        multiline: true,
        label: 'Additional information',
        ObjectInputView: SupplementalEIDCView,
        helpText: "<p>You can add information not documented elsewhere here. This includes links to related papers, grey literature or websites.  For example:</p>\n<ul><li>papers that cite this resource</li><li>papers/reports that provide relevant supporting information but which do not cite this resource</li><li>project websites</li></ul>\n<p>When linking to published articles, please use DOIs whenever possible.</p>\n<p><small class='text-danger'><i class='fas fa-exclamation-triangle'> </i> NOTE: Some websites may be maintained for a limited period and may therefore soon become unavailable.</small></p>"
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'funding',
        ModelType: models_Funding,
        multiline: true,
        label: 'Funding',
        ObjectInputView: FundingView,
        predefined: {
          BBSRC: {
            funderName: 'Biotechnology and Biological Sciences Research Council',
            funderIdentifier: 'https://ror.org/00cwqg982'
          },
          Defra: {
            funderName: 'Department for Environment Food and Rural Affairs',
            funderIdentifier: 'https://ror.org/00tnppw48'
          },
          EPSRC: {
            funderName: 'Engineering and Physical Sciences Research Council',
            funderIdentifier: 'https://ror.org/0439y7842'
          },
          ESRC: {
            funderName: 'Economic and Social Research Council',
            funderIdentifier: 'https://ror.org/03n0ht308'
          },
          'Innovate UK': {
            funderName: 'Innovate UK',
            funderIdentifier: 'https://ror.org/05ar5fy68'
          },
          MRC: {
            funderName: 'Medical Research Council',
            funderIdentifier: 'https://ror.org/03x94j517'
          },
          NERC: {
            funderName: 'Natural Environment Research Council',
            funderIdentifier: 'https://ror.org/02b5d8509'
          },
          STFC: {
            funderName: 'Science and Technology Facilities Council',
            funderIdentifier: 'https://ror.org/057g20z61'
          }
        },
        helpText: "<p>Include here details of any grants or awards that were used to generate this resource.</p>\n<p>If you include funding information, the Funding body is MANDATORY, other fields are useful but optional.</p>\n<p>Award URL is either the unique identifier for the award or sa link to the funder's  grant page (if it exists). It is <b>NOT</b> a link to a project website.</p>",
        disabled: disabled
      })]
    }, {
      label: 'Web service',
      title: 'Web service details',
      views: [new ServiceView({
        model: this.model,
        modelAttribute: 'service',
        ModelType: Service,
        label: 'Service',
        disabled: disabled
      }), new ParentView({
        model: this.model,
        modelAttribute: 'mapDataDefinition.data',
        ModelType: MapDataSource,
        multiline: true,
        label: 'Web map service',
        ObjectInputView: MapDataSourceView,
        helpText: "<p>Link this metadata record to an ingested geospatial file and create a WMS (<strong>https://catalogue.ceh.ac.uk/maps/{METADATA_ID}?request=getCapabilities&service=WMS</strong>). The supported formats are:</p>\n<ul>\n  <li>Shapefiles - Vector (ignore the .shp extension when specifying the path) </li>\n  <li>GeoTiff - Raster</li>\n</ul>\n<p>To maximise performance, it is generally best to provide reprojected variants of data sources in common EPSG codes.</p>\n<p>Vector datasets should be spatially indexed (using <a href=\"http://mapserver.org/utilities/shptree.html\">shptree</a>)</p>\n<p>Raster datasets should be provided with <a href=\"http://www.gdal.org/gdaladdo.html\">overviews</a>. GeoTiff supports internal overviews.</p>\n<p>The 'Byte?' option that appears for raster (GeoTiff) datasets is used to indicate whether the GeoTiff is a 'byte' or 'non-byte' datatype.\nThis is only needed if you configure 'Stylying=Classification' for your GeoTiff.</p>\n<p>Paths should be specified relative to the base of the datastore. e.g. <strong>5b3fcf9f-19d4-4ad3-a8bb-0a5ea02c857e/my_shapefile</strong></p>",
        disabled: disabled
      })]
    }];
    return EditorView/* default.prototype.initialize.apply */.Z.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/InfrastructureRecordEditorView.js




/* harmony default export */ const InfrastructureRecordEditorView = (src/* EditorView.extend */.tk.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'infrastructureRecord');
    }

    this.sections = [{
      label: 'General',
      title: 'General',
      views: [new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'title',
        label: 'Name',
        helpText: "\n<p>Should reflect purpose (succinctly)</p><p>Should be consistent (within and across assets)</p>\n"
      }), new TextOnlyView({
        model: this.model,
        label: 'Purpose',
        text: "<p>Explain strategic relevance: What does it do? Why? Who cares?<br>Write in plain English and avoid (or define) acronyms.<br>Explain relevance to government policy agenda</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        rows: 6
      }), new PredefinedParentView({
        model: this.model,
        ModelType: models_Contact,
        modelAttribute: 'owners',
        label: 'Owner',
        ObjectInputView: ContactView,
        multiline: true,
        predefined: {
          'UKCEH Bangor': {
            organisationName: 'UK Centre for Ecology & Hydrology',
            role: 'owner',
            email: 'enquiries@ceh.ac.uk',
            organisationIdentifier: 'https://ror.org/00pggkr55',
            address: {
              city: 'Bangor'
            }
          },
          'UKCEH Edinburgh': {
            organisationName: 'UK Centre for Ecology & Hydrology',
            role: 'owner',
            email: 'enquiries@ceh.ac.uk',
            organisationIdentifier: 'https://ror.org/00pggkr55',
            address: {
              city: 'Edinburgh'
            }
          },
          'UKCEH Lancaster': {
            organisationName: 'UK Centre for Ecology & Hydrology',
            role: 'owner',
            email: 'enquiries@ceh.ac.uk',
            organisationIdentifier: 'https://ror.org/00pggkr55',
            address: {
              city: 'Lancaster'
            }
          },
          'UKCEH Wallingford': {
            organisationName: 'UK Centre for Ecology & Hydrology',
            role: 'owner',
            email: 'enquiries@ceh.ac.uk',
            organisationIdentifier: 'https://ror.org/00pggkr55',
            address: {
              city: 'Wallingford'
            }
          }
        }
      })]
    }, {
      label: 'Categorisation',
      title: 'Categorisation',
      views: [new SingleObjectView({
        model: this.model,
        modelAttribute: 'infrastructureCategory',
        ModelType: InfrastructureCategory,
        label: 'Infrastructure category',
        ObjectInputView: InfrastructureCategoryView
      }), new ParentView({
        model: this.model,
        ModelType: InfrastructureChallenge,
        modelAttribute: 'infrastructureChallenge',
        label: 'Challenge/goal',
        ObjectInputView: InfrastructureChallengeView
      }), new src/* SelectView */.e7({
        model: this.model,
        modelAttribute: 'scienceArea',
        label: 'Science area',
        options: [{
          value: 'Atmospheric Chemistry and Effects',
          label: 'Atmospheric Chemistry and Effects'
        }, {
          value: 'Biodiversity',
          label: 'Biodiversity'
        }, {
          value: 'Hydro-climate Risks',
          label: 'Hydro-climate Risks'
        }, {
          value: 'Pollution',
          label: 'Pollution'
        }, {
          value: 'Soils and Land Use',
          label: 'Soils and Land Use'
        }, {
          value: 'Water Resources',
          label: 'Water Resources'
        }]
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Other keywords',
        ObjectInputView: KeywordView
      })]
    }, {
      label: 'Scale',
      title: 'Scale',
      views: [new TextareaView({
        model: this.model,
        modelAttribute: 'locationText',
        label: 'Location'
      }), new src/* SelectView */.e7({
        model: this.model,
        modelAttribute: 'infrastructureScale',
        label: 'Scale',
        options: [{
          value: 'UK',
          label: 'UK-wide'
        }, {
          value: 'Landscape or catchment',
          label: 'Landscape or catchment'
        }, {
          value: 'Area, city, farm, habitat',
          label: 'Area, city, farm, habitat'
        }]
      }), new PredefinedParentView({
        model: this.model,
        modelAttribute: 'boundingBoxes',
        ModelType: geometryMap_BoundingBox,
        label: 'Spatial extent',
        ObjectInputView: BoundingBoxView,
        multiline: true,
        predefined: {
          'Great Britain': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648
          }
        }
      })]
    }, {
      label: 'Capability',
      title: 'Capability',
      views: [new TextareaView({
        model: this.model,
        modelAttribute: 'capabilities',
        label: 'Capabilities',
        rows: 6,
        helpText: "\n<p>Describe the facility, experimental design.  What is it equipped to do or measure?</p><p>Be informative for external partners and users</p>\n"
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'lifecycle',
        label: 'Lifecycle',
        rows: 6
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'uniqueness',
        label: 'Uniqueness',
        rows: 6
      })]
    }, {
      label: 'Use',
      title: 'Use',
      views: [new TextareaView({
        model: this.model,
        modelAttribute: 'partners',
        label: 'Partners',
        rows: 6
      }), new ParentStringView({
        model: this.model,
        modelAttribute: 'users',
        label: 'Users'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'access',
        label: 'Access',
        rows: 6
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'userCosts',
        label: 'User costs',
        rows: 6
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'fundingSources',
        label: 'Funding sources',
        rows: 6,
        helpText: "\n<p>Include all funding sources</p><p>Be specific about NC awards/programmes</p>\n"
      })]
    }, {
      label: 'Other',
      title: 'Other',
      views: [new ParentView({
        model: this.model,
        modelAttribute: 'onlineResources',
        label: 'Online resources',
        ObjectInputView: InfrastructureOnlineLinkView,
        multiline: true
      }), new ParentView({
        model: this.model,
        modelAttribute: 'relatedRecords',
        label: 'Related records',
        ObjectInputView: RelatedRecordView,
        multiline: true
      })]
    }];
    return src/* EditorView.prototype.initialize.apply */.tk.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/CodeDocumentEditorView.js




/* harmony default export */ const CodeDocumentEditorView = (src/* EditorView.extend */.tk.extend({
  initialize: function initialize() {
    if (!this.model.has('type')) {
      this.model.set('type', 'notebook');
    }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [new src/* SelectView */.e7({
        model: this.model,
        modelAttribute: 'type',
        label: 'Record type',
        options: [{
          value: 'notebook',
          label: 'Notebook'
        }, {
          value: 'codeProject',
          label: 'Code project'
        }, {
          value: 'codeSnippet',
          label: 'Code snippet'
        }]
      }), new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'title',
        label: 'Title'
      }), new TextareaView({
        model: this.model,
        modelAttribute: 'description',
        label: 'Description',
        rows: 5
      }), new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'masterUrl',
        label: 'Master URL'
      }), new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'primaryLanguage',
        label: 'Primary language',
        listAttribute: "<option value='Python' />\n<option value='R' />"
      }), new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'secondaryLanguage',
        label: 'Secondary language',
        listAttribute: "<option value='Python' />\n<option value='R' />"
      }), new src/* SelectView */.e7({
        model: this.model,
        modelAttribute: 'assetType',
        label: 'Type',
        options: [{
          value: '',
          label: ''
        }, {
          value: 'Jupyter notebook',
          label: 'Jupyter notebook'
        }, {
          value: 'Zeppelin notebook',
          label: 'Zeppelin notebook'
        }, {
          value: 'RStudio project',
          label: 'RStudio project'
        }, {
          value: 'RShiny app',
          label: 'RShiny app'
        }],
        helpText: "\n<p>(only relevant for notebooks)</p>\n"
      }), new src/* InputView */.Em({
        model: this.model,
        modelAttribute: 'version',
        label: 'Version'
      }), new SingleObjectView({
        model: this.model,
        modelAttribute: 'referenceDate',
        ModelType: MultipleDate,
        label: 'Dates',
        ObjectInputView: DatasetReferenceDateView
      })]
    }, {
      label: 'Inputs & Outputs',
      title: 'Inputs, outputs, packages and review',
      views: [new ParentStringView({
        model: this.model,
        modelAttribute: 'inputs',
        label: 'Inputs'
      }), new ParentStringView({
        model: this.model,
        modelAttribute: 'outputs',
        label: 'Outputs'
      }), new ParentStringView({
        model: this.model,
        modelAttribute: 'packages',
        label: 'Packages'
      }), new ParentView({
        model: this.model,
        modelAttribute: 'review',
        label: 'Review',
        ObjectInputView: ReviewView,
        multiline: true
      })]
    }, {
      label: 'Licensing & Contacts',
      title: 'Licensing and contacts',
      views: [new PredefinedParentView({
        model: this.model,
        modelAttribute: 'useConstraints',
        label: 'Use constraints',
        ObjectInputView: ResourceConstraintView,
        multiline: true,
        predefined: {
          'Licence - OGL': {
            value: 'Open Government Licence v3',
            uri: 'https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/',
            code: 'license'
          },
          'Licence - GNU GPLv3': {
            value: 'GNU',
            uri: 'http://www.gnu.org/licenses/gpl-3.0.txt',
            code: 'license'
          },
          'Licence - CC-BY': {
            value: 'Attribution 4.0 International (CC BY 4.0)',
            uri: 'https://creativecommons.org/licenses/by/4.0/',
            code: 'license'
          },
          'Licence - CC-BY-SA': {
            value: 'Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)',
            uri: 'https://creativecommons.org/licenses/by-sa/4.0/',
            code: 'license'
          }
        }
      }), new PredefinedParentView({
        model: this.model,
        ModelType: models_Contact,
        modelAttribute: 'responsibleParties',
        label: 'Contacts',
        ObjectInputView: ContactView,
        multiline: true,
        predefined: {
          'UKCEH owner': {
            organisationName: 'UK Centre for Ecology & Hydrology',
            role: 'owner',
            email: 'enquiries@ceh.ac.uk',
            organisationIdentifier: 'https://ror.org/00pggkr55'
          },
          'Oher owner': {
            role: 'owner'
          }
        }
      })]
    }, {
      label: 'Classification',
      title: 'Classification',
      views: [new PredefinedParentView({
        model: this.model,
        modelAttribute: 'boundingBoxes',
        ModelType: geometryMap_BoundingBox,
        label: 'Spatial extent',
        ObjectInputView: BoundingBoxView,
        multiline: true,
        predefined: {
          England: {
            northBoundLatitude: 55.812,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -6.452,
            extentName: 'England',
            extentUri: 'http://sws.geonames.org/6269131'
          },
          'Great Britain': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648,
            extentName: 'Great Britain'
          },
          'Northern Ireland': {
            northBoundLatitude: 55.313,
            eastBoundLongitude: -5.432,
            southBoundLatitude: 54.022,
            westBoundLongitude: -8.178,
            extentName: 'Northern Ireland',
            extentUri: 'http://sws.geonames.org/2641364'
          },
          Scotland: {
            northBoundLatitude: 60.861,
            eastBoundLongitude: -0.728,
            southBoundLatitude: 54.634,
            westBoundLongitude: -8.648,
            extentName: 'Scotland',
            extentUri: 'http://sws.geonames.org/2638360'
          },
          'United Kingdom': {
            northBoundLatitude: 60.861,
            eastBoundLongitude: 1.768,
            southBoundLatitude: 49.864,
            westBoundLongitude: -8.648,
            extentName: 'United Kingdom',
            extentUri: 'http://sws.geonames.org/2635167'
          },
          Wales: {
            northBoundLatitude: 53.434,
            eastBoundLongitude: -2.654,
            southBoundLatitude: 51.375,
            westBoundLongitude: -5.473,
            extentName: 'Wales',
            extentUri: 'http://sws.geonames.org/2634895'
          },
          World: {
            northBoundLatitude: 90.00,
            eastBoundLongitude: 180.00,
            southBoundLatitude: -90.00,
            westBoundLongitude: -180.00
          }
        }
      }), new ParentView({
        model: this.model,
        modelAttribute: 'temporalExtents',
        ModelType: MultipleDate,
        label: 'Temporal extent',
        ObjectInputView: TemporalExtentView
      }), new ParentView({
        model: this.model,
        ModelType: InspireTheme,
        modelAttribute: 'inspireThemes',
        label: 'INSPIRE theme',
        ObjectInputView: InspireThemeView
      }), new ParentView({
        model: this.model,
        modelAttribute: 'keywords',
        label: 'Keywords',
        ObjectInputView: KeywordView
      }), new ParentView({
        model: this.model,
        modelAttribute: 'relatedRecords',
        label: 'Related records',
        ObjectInputView: RelatedRecordView,
        multiline: true
      })]
    }];
    return src/* EditorView.prototype.initialize.apply */.tk.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./editor/src/editors/index.js


























;// CONCATENATED MODULE: ./editor/src/bootstrap.js





var lookup = {
  GEMINI_DOCUMENT: {
    View: GeminiEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/gemini+json'
  },
  EF_DOCUMENT: {
    View: MonitoringEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/monitoring+json'
  },
  IMP_DOCUMENT: {
    View: ModelEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/model+json'
  },
  CEH_MODEL: {
    View: CehModelEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.ceh.model+json'
  },
  CEH_MODEL_APPLICATION: {
    View: CehModelApplicationEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.ceh.model.application+json'
  },
  LINK_DOCUMENT: {
    View: LinkEditorView,
    Model: LinkEditorMetadata,
    mediaType: 'application/link+json'
  },
  'osdp-agent': {
    View: OsdpAgentEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.osdp.agent+json'
  },
  'osdp-dataset': {
    View: OsdpDatasetEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.osdp.dataset+json'
  },
  'osdp-model': {
    View: OsdpModelEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.osdp.model+json'
  },
  'osdp-sample': {
    View: OsdpSampleEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.osdp.sample+json'
  },
  'osdp-publication': {
    View: OsdpPublicationEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.osdp.publication+json'
  },
  'osdp-monitoring-activity': {
    View: OsdpMonitoringActivityEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.osdp.monitoring-activity+json'
  },
  'osdp-monitoring-programme': {
    View: OsdpMonitoringProgrammeEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.osdp.monitoring-programme+json'
  },
  'osdp-monitoring-facility': {
    View: OsdpMonitoringFacilityEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.osdp.monitoring-facility+json'
  },
  'sample-archive': {
    View: SampleArchiveEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.sample-archive+json'
  },
  'erammp-model': {
    View: ErammpModelEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.erammp-model+json'
  },
  'nerc-model': {
    View: NercModelEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.nerc-model+json'
  },
  'nerc-model-use': {
    View: NercModelUseEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.nerc-model-use+json'
  },
  'erammp-datacube': {
    View: ErammpDatacubeEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.erammp-datacube+json'
  },
  'data-type': {
    View: DataTypeEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.data-type+json'
  },
  elter: {
    View: ElterEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.elter+json'
  },
  'linked-elter': {
    View: ElterLinkedEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.linked-elter+json'
  },
  'service-agreement': {
    View: ServiceAgreementEditorView,
    Model: ServiceAgreement,
    mediaType: 'application/json'
  },
  'ukems-document': {
    View: UkemsDocumentEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.ukems-document+json'
  },
  'code-document': {
    View: CodeDocumentEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.code-document+json'
  },
  infrastructurerecord: {
    View: InfrastructureRecordEditorView,
    Model: EditorMetadata/* default */.Z,
    mediaType: 'application/vnd.infrastructure+json'
  }
};

if (jquery_default()('.edit-control').length) {
  initEditor();
}

if (jquery_default()('.service-agreement').length) {
  initServiceAgreement();
}

function initEditor() {
  // the create document dropdown
  var $editorCreate = jquery_default()('#editorCreate');
  jquery_default()('.edit-control').on('click', function (event) {
    event.preventDefault();
    var title = jquery_default()(event.target).data('documentType');
    var documentType = lookup[title];

    if ($editorCreate.length) {
      return new documentType.View({
        model: new documentType.Model(null, documentType, title),
        el: '#search'
      });
    } else {
      jquery_default().ajax({
        url: jquery_default()(location).attr('href'),
        dataType: 'json',
        accepts: {
          json: documentType.mediaType
        },
        success: function success(data) {
          // eslint-disable-next-line no-unused-vars
          var document = new documentType.View({
            model: new documentType.Model(data, documentType, title),
            el: '#metadata'
          });
        }
      });
    }
  });
}

function initServiceAgreement() {
  var $gemini = jquery_default()('#service-agreement-gemini');
  jquery_default()('.service-agreement').on('click', function (event) {
    event.preventDefault();
    var id = jquery_default()(event.currentTarget).data('id');
    var data = {
      eidcContactDetails: 'info@eidc.ac.uk'
    };
    var options = {
      id: id
    };

    if ($gemini.length) {
      jquery_default().ajax({
        url: "/service-agreement/".concat(id),
        type: 'GET',
        success: function success() {
          window.location.href = "/service-agreement/".concat(id);
        },
        error: function error() {
          // eslint-disable-next-line no-unused-vars
          var serviceAgreement = new ServiceAgreementEditorView({
            el: '#metadata',
            model: new ServiceAgreement(data, options)
          });
        }
      });
    }
  });
}

/***/ })

}]);