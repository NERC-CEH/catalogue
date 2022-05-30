"use strict";
(self["webpackChunkweb"] = self["webpackChunkweb"] || []).push([[628],{

/***/ 4628:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

// ESM COMPAT FLAG
__webpack_require__.r(__webpack_exports__);

// EXTERNAL MODULE: ./node_modules/jquery/dist/jquery.js
var jquery = __webpack_require__(9755);
var jquery_default = /*#__PURE__*/__webpack_require__.n(jquery);
// EXTERNAL MODULE: ./node_modules/bootstrap/dist/js/bootstrap.esm.js
var bootstrap_esm = __webpack_require__(9909);
// EXTERNAL MODULE: ./node_modules/backbone/backbone.js
var backbone = __webpack_require__(2316);
var backbone_default = /*#__PURE__*/__webpack_require__.n(backbone);
;// CONCATENATED MODULE: ./catalogue/src/CatalogueApp/Catalogue.js

/* harmony default export */ const Catalogue = (backbone_default().Model.extend({
  url: function url() {
    return this.urlRoot();
  },
  urlRoot: function urlRoot() {
    return "/documents/".concat(this.id, "/catalogue");
  }
}));
// EXTERNAL MODULE: ./editor/src/index.js
var src = __webpack_require__(5456);
;// CONCATENATED MODULE: ./catalogue/src/CatalogueApp/CatalogueView.js

/* harmony default export */ const CatalogueView = (src/* EditorView.extend */.tk.extend({
  initialize: function initialize() {
    this.sections = [{
      label: 'One',
      title: 'Catalogue',
      views: [new src/* SelectView */.e7({
        model: this.model,
        modelAttribute: 'value',
        label: 'Catalogue',
        options: this.model.options,
        helpText: '<p>Catalogue</p>'
      })]
    }];
    return src/* EditorView.prototype.initialize.apply */.tk.prototype.initialize.apply(this);
  }
}));
;// CONCATENATED MODULE: ./catalogue/src/CatalogueApp/index.js


;// CONCATENATED MODULE: ./catalogue/src/bootstrap.js



jquery_default()('.catalogue-control').on('click', function (event) {
  event.preventDefault();
  jquery_default().getJSON(jquery_default()(event.target).attr('href'), function (data) {
    var model = new Catalogue(data);
    jquery_default().getJSON('/catalogues', function (data) {
      jquery_default()(document).ready(function () {
        model.options = data.map(function (val) {
          return {
            value: val.id,
            label: val.title
          };
        });
        var view = new CatalogueView({
          el: '#metadata',
          model: model
        });
      });
    });
  });
});

/***/ }),

/***/ 6813:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Z": () => (__WEBPACK_DEFAULT_EXPORT__)
/* harmony export */ });
/* harmony import */ var underscore__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(8860);
/* harmony import */ var backbone__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(2316);
/* harmony import */ var backbone__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(backbone__WEBPACK_IMPORTED_MODULE_1__);


/* harmony default export */ const __WEBPACK_DEFAULT_EXPORT__ = (backbone__WEBPACK_IMPORTED_MODULE_1___default().Model.extend({
  url: function url() {
    return this.urlRoot();
  },
  urlRoot: function urlRoot() {
    if (this.isNew()) {
      return "/documents?catalogue=".concat(window.location.pathname.split('/')[1]);
    } else {
      return "/documents/".concat(this.id);
    }
  },
  initialize: function initialize() {
    if (arguments.length > 1) {
      this.mediaType = arguments[1].mediaType;
      this.title = arguments[2];
    } else {
      this.mediaType = 'application/json';
    }
  },
  sync: function sync(method, model, options) {
    return backbone__WEBPACK_IMPORTED_MODULE_1___default().sync.call(this, method, model, underscore__WEBPACK_IMPORTED_MODULE_0__/* ["default"].extend */ .ZP.extend(options, {
      accepts: {
        json: this.mediaType
      },
      contentType: this.mediaType
    }));
  },
  validate: function validate(attrs) {
    var errors = [];

    if ((attrs != null ? attrs.title : undefined) == null) {
      errors.push('A title is mandatory');
    }

    if (underscore__WEBPACK_IMPORTED_MODULE_0__/* ["default"].isEmpty */ .ZP.isEmpty(errors)) {// return nothing from Backbone.Model.validate because returning something signals a validation error.
    } else {
      return errors;
    }
  }
}));

/***/ }),

/***/ 4003:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {


// EXPORTS
__webpack_require__.d(__webpack_exports__, {
  "Z": () => (/* binding */ EditorView)
});

// EXTERNAL MODULE: ./node_modules/jquery/dist/jquery.js
var jquery = __webpack_require__(9755);
var jquery_default = /*#__PURE__*/__webpack_require__.n(jquery);
// EXTERNAL MODULE: ./node_modules/underscore/modules/index-all.js
var index_all = __webpack_require__(8860);
// EXTERNAL MODULE: ./node_modules/backbone/backbone.js
var backbone = __webpack_require__(2316);
var backbone_default = /*#__PURE__*/__webpack_require__.n(backbone);
// EXTERNAL MODULE: ./node_modules/html-loader/dist/runtime/getUrl.js
var getUrl = __webpack_require__(7091);
var getUrl_default = /*#__PURE__*/__webpack_require__.n(getUrl);
;// CONCATENATED MODULE: ./editor/src/Editor.tpl
// Imports

var ___HTML_LOADER_IMPORT_0___ = new URL(/* asset import */ __webpack_require__(8793), __webpack_require__.b);
// Module
var ___HTML_LOADER_REPLACEMENT_0___ = getUrl_default()(___HTML_LOADER_IMPORT_0___);
var code = "<ol id=\"editorNav\" class=\"breadcrumb\"></ol> <div id=\"editor\" class=\"container-fluid\" role=\"form\"></div> <div class=\"navbar navbar-default navbar-fixed-bottom\"> <div class=\"container-fluid\"> <div class=\"navbar-left\"> <button id=\"editorDelete\" class=\"btn btn-sm btn-danger navbar-btn\">Delete <i class=\"fas fa-times\"></i></button> </div> <div class=\"navbar-right\"> <span id=\"editorAjax\" class=\"navbar-text\">Saving: <img src=\"" + ___HTML_LOADER_REPLACEMENT_0___ + "\"></span> <div class=\"btn-group\"> <button id=\"editorBack\" class=\"btn btn-sm btn-default navbar-btn\" disabled=\"disabled\"><i class=\"fas fa-chevron-left\"></i> Back</button> <button id=\"editorNext\" class=\"btn btn-sm btn-default navbar-btn\">Next <i class=\"fas fa-chevron-right\"></i></button> </div> <button id=\"editorSave\" class=\"btn btn-sm btn-default navbar-btn\">Save <i class=\"far fa-save\"></i></button> <button id=\"editorExit\" class=\"btn btn-sm btn-default navbar-btn\">Exit <i class=\"fas fa-power-off\"></i></button> </div> </div> </div> <div id=\"confirmExit\" class=\"modal\"> <div class=\"modal-dialog\"> <div class=\"modal-content\"> <div class=\"modal-header\"> <h4 class=\"modal-title\">Are you sure?</h4> </div> <div class=\"modal-body\"> <p>There are unsaved changes to this record<br>Do you want to exit without saving?</p> </div> <div class=\"modal-footer\"> <button type=\"button\" class=\"btn btn-primary\" data-dismiss=\"modal\">No</button> <button type=\"button\" class=\"btn btn-default\" id=\"exitWithoutSaving\">Yes, exit without saving</button> </div> </div> </div> </div> <div id=\"confirmDelete\" class=\"modal fade\"> <div class=\"modal-dialog\"> <div class=\"modal-content\"> <div class=\"modal-body\"> <h2 class=\"text-red\"><i class=\"fas fa-exclamation-triangle\"></i> <strong>Are you sure?</strong></h2> </div> <div class=\"modal-footer\"> <button type=\"button\" class=\"btn btn-primary\" data-dismiss=\"modal\">No</button> <button type=\"button\" class=\"btn btn-default\" id=\"confirmDeleteYes\">Yes, delete this record</button> </div> </div> </div> </div> <div id=\"editorErrorMessage\" class=\"modal fade\"> <div class=\"modal-dialog\"> <div class=\"modal-content\"> <div class=\"modal-header\"> <h4 class=\"modal-title\">There was a problem communicating with the server</h4> </div> <div class=\"modal-body\"> <p>Server response:</p> <pre id=\"editorErrorMessageResponse\"></pre> <p>Please save this record locally by copying the text below to a file.</p> <pre id=\"editorErrorMessageJson\" class=\"pre-scrollable\"></pre> </div> <div class=\"modal-footer\"> <button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">OK</button> </div> </div> </div> </div> <div id=\"editorValidationMessage\" class=\"modal fade\"> <div class=\"modal-dialog\"> <div class=\"modal-content\"> <div class=\"modal-header\"> <h4 class=\"modal-title\">Validation Errors</h4> </div> <div class=\"modal-body\"></div> <div class=\"modal-footer\"> <button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">OK</button> </div> </div> </div> </div>";
// Exports
/* harmony default export */ const Editor = (code);
;// CONCATENATED MODULE: ./editor/src/EditorView.js




/* harmony default export */ const EditorView = (backbone_default().View.extend({
  events: {
    'click #editorDelete': 'attemptDelete',
    'click #confirmDeleteYes': 'delete',
    'click #editorExit': 'attemptExit',
    'click #exitWithoutSaving': 'exit',
    'click #editorSave': 'save',
    'click #editorBack': 'back',
    'click #editorNext': 'next',
    'click #editorNav li': 'direct'
  },
  initialize: function initialize() {
    var _this = this;

    if (typeof this.template === 'undefined') {
      this.template = index_all/* default.template */.ZP.template(Editor);
    }

    this.currentStep = 1;
    this.saveRequired = false;
    this.catalogue = jquery_default()('html').data('catalogue');
    this.listenTo(this.model, 'error', function (model, response) {
      this.$('#editorAjax').toggleClass('visible');
      window.$('#editorErrorMessage').find('#editorErrorMessageResponse').text("".concat(response.status, " ").concat(response.statusText)).end().find('#editorErrorMessageJson').text(JSON.stringify(model.toJSON())).end().modal('show');
    });
    this.listenTo(this.model, 'sync', function () {
      this.$('#editorAjax').toggleClass('visible');
      this.saveRequired = false;
    });
    this.listenTo(this.model, 'change save:required', function () {
      this.saveRequired = true;
    });
    this.listenTo(this.model, 'request', function () {
      this.$('#editorAjax').toggleClass('visible');
    });
    this.listenTo(this.model, 'invalid', function (model, errors) {
      this.$('#editorValidationMessage .modal-body').html('');

      index_all/* default.each */.ZP.each(errors, function (error) {
        this.$('#editorValidationMessage .modal-body').append(this.$("<p>".concat(error, "</p>")));
      });

      window.$('#editorValidationMessage').modal('show');
    });
    this.render();

    index_all/* default.invoke */.ZP.invoke(this.sections[0].views, 'show');

    this.sections.forEach(function (section) {
      _this.$('#editorNav').append(jquery_default()("<li title='".concat(section.title, "'>").concat(section.label, "</li>")));
    });
    this.$('#editorNav').find('li').first().addClass('active');
  },
  attemptDelete: function attemptDelete() {
    window.$('#confirmDelete').modal('show');
  },
  "delete": function _delete() {
    var _this2 = this;

    window.$('#confirmDelete').modal('hide');
    this.model.destroy({
      success: function success() {
        index_all/* default.invoke */.ZP.invoke(_this2.sections, 'remove');

        _this2.remove();

        backbone_default().history.location.replace("/".concat(_this2.catalogue, "/documents"));
      }
    });
  },
  save: function save() {
    this.model.save();
  },
  attemptExit: function attemptExit() {
    if (this.saveRequired) {
      window.$('#confirmExit').modal('show');
    } else {
      this.exit();
    }
  },
  exit: function exit() {
    window.$('#confirmExit').modal('hide');

    index_all/* default.invoke */.ZP.invoke(this.sections, 'remove');

    this.remove();

    if ((backbone_default()).history.location.pathname === "/".concat(this.catalogue, "/documents") && !this.model.isNew()) {
      backbone_default().history.location.replace("/documents/".concat(this.model.get('id')));
    } else {
      backbone_default().history.location.reload();
    }
  },
  back: function back() {
    this.navigate(this.currentStep - 1);
  },
  next: function next() {
    this.navigate(this.currentStep + 1);
  },
  direct: function direct(event) {
    var node = event.currentTarget;
    var step = 0;

    while (node !== null) {
      step++;
      node = node.previousElementSibling;
    }

    this.navigate(step);
  },
  navigate: function navigate(newStep) {
    var _this3 = this;

    var $nav = this.$('#editorNav li');
    var maxStep = $nav.length;
    this.currentStep = newStep;

    if (this.currentStep < 1) {
      this.currentStep = 1;
    }

    if (this.currentStep > maxStep) {
      this.currentStep = maxStep;
    }

    var $back = this.$('#editorBack');

    if (this.currentStep === 1) {
      $back.prop('disabled', true);
    } else {
      $back.prop('disabled', false);
    }

    var $next = this.$('#editorNext');

    if (this.currentStep === maxStep) {
      $next.prop('disabled', true);
    } else {
      $next.prop('disabled', false);
    }

    $nav.filter('.active').toggleClass('active');
    this.$($nav[this.currentStep - 1]).toggleClass('active');

    index_all/* default.each */.ZP.each(this.sections, function (section, index) {
      var method = 'hide';

      if (_this3.currentStep - 1 === index) {
        method = 'show';
      }

      index_all/* default.invoke */.ZP.invoke(section.views, method);
    });
  },
  render: function render() {
    var _this4 = this;

    this.$el.html(this.template(this.model.attributes));
    this.sections.forEach(function (section) {
      section.views.forEach(function (view) {
        _this4.$('#editor').append(view.render().el);
      });
    });
    return this;
  }
}));

/***/ }),

/***/ 8315:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {


// EXPORTS
__webpack_require__.d(__webpack_exports__, {
  "Z": () => (/* binding */ InputView)
});

// EXTERNAL MODULE: ./node_modules/underscore/modules/index-all.js
var index_all = __webpack_require__(8860);
// EXTERNAL MODULE: ./node_modules/jquery/dist/jquery.js
var jquery = __webpack_require__(9755);
var jquery_default = /*#__PURE__*/__webpack_require__.n(jquery);
// EXTERNAL MODULE: ./editor/src/SingleView.js + 1 modules
var SingleView = __webpack_require__(2798);
;// CONCATENATED MODULE: ./editor/src/Input.tpl
// Module
var code = "<input list=\"<%= data.modelAttribute %>List\" data-name=\"<%= data.modelAttribute %>\" type=\"<%= data.typeAttribute %>\" placeholder=\"<%= data.placeholderAttribute %>\" class=\"editor-input\" id=\"input-<%= data.modelAttribute %>\" value=\"<%= data.value %>\" <%= data.disabled%>> <datalist id=\"<%= data.modelAttribute %>List\"><%= data.listAttribute%></datalist>";
// Exports
/* harmony default export */ const Input = (code);
;// CONCATENATED MODULE: ./editor/src/InputView.js




/* harmony default export */ const InputView = (SingleView/* default.extend */.Z.extend({
  events: {
    change: 'modify'
  },
  initialize: function initialize(options) {
    SingleView/* default.prototype.initialize.call */.Z.prototype.initialize.call(this, options);

    if (typeof this.template === 'undefined') {
      this.template = index_all/* default.template */.ZP.template(Input);
    }

    this.render();
    this.listenTo(this.model, "change:".concat(this.data.modelAttribute), this.render);
  },
  render: function render() {
    SingleView/* default.prototype.render.apply */.Z.prototype.render.apply(this);
    this.$('.dataentry').append(this.template({
      data: index_all/* default.extend */.ZP.extend({}, this.data, {
        value: this.model.get(this.data.modelAttribute)
      })
    }));

    if (this.data.readonly) {
      this.$(':input').prop('readonly', true);
    }

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
  }
}));

/***/ }),

/***/ 8751:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {


// EXPORTS
__webpack_require__.d(__webpack_exports__, {
  "Z": () => (/* binding */ SelectView)
});

// EXTERNAL MODULE: ./node_modules/underscore/modules/index-all.js
var index_all = __webpack_require__(8860);
// EXTERNAL MODULE: ./editor/src/InputView.js + 1 modules
var InputView = __webpack_require__(8315);
;// CONCATENATED MODULE: ./editor/src/Select.tpl
// Module
var code = "<select data-name=\"<%= data.modelAttribute %>\" class=\"editor-input\" id=\"input-<%= data.modelAttribute %>\"></select>";
// Exports
/* harmony default export */ const Select = (code);
;// CONCATENATED MODULE: ./editor/src/SelectView.js



/* harmony default export */ const SelectView = (InputView/* default.extend */.Z.extend({
  initialize: function initialize(options) {
    this.optionTemplate = index_all/* default.template */.ZP.template('<option value="<%= value %>"><%= label %></option>');
    this.options = options.options;
    this.template = index_all/* default.template */.ZP.template(Select);
    InputView/* default.prototype.initialize.call */.Z.prototype.initialize.call(this, options);
  },
  render: function render() {
    var _this = this;

    InputView/* default.prototype.render.apply */.Z.prototype.render.apply(this);
    this.options.forEach(function (option) {
      _this.$('select').append(_this.optionTemplate(option));
    });
    this.$('select').val(this.model.get(this.data.modelAttribute));
    return this;
  }
}));

/***/ }),

/***/ 2798:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {


// EXPORTS
__webpack_require__.d(__webpack_exports__, {
  "Z": () => (/* binding */ SingleView)
});

// EXTERNAL MODULE: ./node_modules/backbone/backbone.js
var backbone = __webpack_require__(2316);
var backbone_default = /*#__PURE__*/__webpack_require__.n(backbone);
// EXTERNAL MODULE: ./node_modules/underscore/modules/index-all.js
var index_all = __webpack_require__(8860);
;// CONCATENATED MODULE: ./editor/src/Single.tpl
// Module
var code = "<div class=\"row\"> <div class=\"col-sm-3 datalabel\"> <label for=\"input-<%= data.modelAttribute %>\"> <%= data.label %> <% if(data.helpText) { %> <a data-toggle=\"collapse\" title=\"Click for help\" href=\"#help-<%= data.modelAttribute.replace('.', '\\.') %>\" data-parent=\"#editor\"><i class=\"fas fa-question-circle\"></i></a> <% } %> </label> <div id=\"help-<%= data.modelAttribute %>\" class=\"editor-help\"> <%= data.helpText %> </div> </div> <div id=\"dataentry\" class=\"col-sm-9 dataentry\"></div> </div>";
// Exports
/* harmony default export */ const Single = (code);
;// CONCATENATED MODULE: ./editor/src/SingleView.js



/* harmony default export */ const SingleView = (backbone_default().View.extend({
  className: 'component',
  initialize: function initialize(options) {
    this.singleTemplate = index_all/* default.template */.ZP.template(Single);
    this.data = options;

    if (!this.data.ModelType) {
      this.data.ModelType = (backbone_default()).Model;
    }
  },
  show: function show() {
    this.$el.addClass('visible');
  },
  hide: function hide() {
    this.$el.removeClass('visible');
  },
  render: function render() {
    this.$el.html(this.singleTemplate({
      data: this.data
    }));
    return this;
  },
  updateMetadataModel: function updateMetadataModel(attribute) {
    this.model.set(this.data.modelAttribute, attribute);
  }
}));

/***/ }),

/***/ 5456:
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "Em": () => (/* reexport safe */ _InputView__WEBPACK_IMPORTED_MODULE_2__.Z),
/* harmony export */   "e7": () => (/* reexport safe */ _SelectView__WEBPACK_IMPORTED_MODULE_3__.Z),
/* harmony export */   "o4": () => (/* reexport safe */ _EditorMetadata__WEBPACK_IMPORTED_MODULE_1__.Z),
/* harmony export */   "tk": () => (/* reexport safe */ _EditorView__WEBPACK_IMPORTED_MODULE_0__.Z)
/* harmony export */ });
/* harmony import */ var _EditorView__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(4003);
/* harmony import */ var _EditorMetadata__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(6813);
/* harmony import */ var _InputView__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(8315);
/* harmony import */ var _SelectView__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(8751);
/* harmony import */ var _SingleView__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(2798);
Promise.all(/* import() */[__webpack_require__.e(367), __webpack_require__.e(243), __webpack_require__.e(758), __webpack_require__.e(10)]).then(__webpack_require__.bind(__webpack_require__, 2010));






/***/ }),

/***/ 8793:
/***/ ((module, __unused_webpack_exports, __webpack_require__) => {

module.exports = __webpack_require__.p + "a0b8bdcdb75e055d89d5.gif";

/***/ })

}]);