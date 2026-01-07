import _ from 'underscore'

export default _.template(`
<div class="dataentry child observedProperty">
    <div class="row">
        <div class="col-xxl-2">
            <label for="schema-value<%= data.index %>">Name</label>
        </div>
        <div class="col-xxl-4">
            <input data-name="value" id="schema-value<%= data.index %>" class="editor-input" value="<%= data.value %>" placeholder="name of field/column" />
        </div>
        <div class="col-xxl-2">
            <label for="schema-uri<%= data.index %>">URI</label>
        </div>
        <div class="col-xxl-4">
            <input data-name="uri" id="schema-uri<%= data.index %>" class="editor-input" value="<%= data.uri %>" placeholder="uri of controlled term" />
        </div>
    </div>
    <div class="row">
        <div class="col-xxl-2">
            <label for="schema-title<%= data.index %>">Title</label>
        </div>
        <div class="col-xxl-10">
            <input data-name="title" id="schema-title<%= data.index %>" class="editor-input" value="<%= data.title %>" placeholder="A nicer human readable label for the field (optional)" />
        </div>
    </div>
    <div class="row">
        <div class="col-xxl-2">
            <label for="schema-type<%= data.index %>">Type</label>
        </div>
        <div class="col-xxl-4">
            <input list="dataTypeList" data-name="type" id="schema-type<%= data.index %>" class="editor-input" value="<%= data.type %>" autocomplete="off" aria-autocomplete="none" />
        </div>
        <div class="col-xxl-2">
            <div class="d-none d-xxl-block text-end">
                <label for="schema-format<%= data.index %>">Format</label>
            </div>
        </div>
        <div class="col-xxl-4">
            <input list="dataFormatList" data-name="format" id="schema-format<%= data.index %>" class="editor-input" value="<%= data.format %>" autocomplete="off" aria-autocomplete="none" placeholder="optional (recommended for dates and times)" />
        </div>
    </div>
    <div class="row">
        <div class="col-xxl-2">
            <label for="schema-units<%= data.index %>">Unit</label>
        </div>
        <div class="col-xxl-4">
            <input data-name="units" id="schema-units<%= data.index %>" class="editor-input" value="<%= data.units %>"/>
        </div>
        <div class="col-xxl-2">
            <label for="schema-unitsUri<%= data.index %>">Unit uri</label>
        </div>
        <div class="col-xxl-4">
            <input data-name="unitsUri" id="schema-unitsUri<%= data.index %>" class="editor-input" value="<%= data.unitsUri %>"/>
        </div>
    </div>
    <div class="extended d-none" id="schemaDetail<%= data.index %>">
        <div class="row">
            <div class="col-xxl-2">
                <label for="schema-description<%= data.index %>">Description</label>
            </div>
            <div class="col-xxl-10">
                <textarea data-name="description" id="schema-description<%= data.index %>" class="editor-textarea" rows="3"><%= data.description %></textarea>
            </div>
        </div>
        <div class="row">
            <div class="col-xxl-2">
                <label for="schema-minimum<%= data.index %>">Minimum value</label>
            </div>
            <div class="col-xxl-4">
                <input data-name="minimum" type="number" id="schema-minimum<%= data.index %>" class="editor-input" value="<%= data.constraints.minimum %>"/>
            </div>
            <div class="col-xxl-2">
                <label for="schema-maximum<%= data.index %>">Maximum value</label>
            </div>
            <div class="col-xxl-4">
                <input data-name="maximum" type="number" id="schema-maximum<%= data.index %>" class="editor-input" value="<%= data.constraints.maximum %>"/>
            </div>
        </div>
        <div class="row">
            <div class="col-xxl-2">
                <label for="schema-minLength<%= data.index %>">Minimum length</label>
            </div>
            <div class="col-xxl-4">
                <input data-name="minLength" type="number" step="1" id="schema-minLength<%= data.index %>" class="editor-input" value="<%= data.constraints.minLength %>"/>
            </div>
            <div class="col-xxl-2">
                <label for="schema-maxLength<%= data.index %>">Maximum length</label>
            </div>
            <div class="col-xxl-4">
                <input data-name="maxLength" type="number" step="1" id="schema-maxLength<%= data.index %>" class="editor-input" value="<%= data.constraints.maxLength %>"/>
            </div>
        </div>

        <div class="row">
            <div class="col-xxl-2">
                <label>Unique</label>
            </div>
            <div class="col-xxl-10">
              <div class="form-check form-check-inline">
                <input data-name="unique" name="schema-unique<%= data.index %>"  class="form-check-input" type="radio" name="exampleRadios" id="schema-unique<%= data.index %>" value="true" <% if (data.constraints.unique == true) { %> checked="checked" <% } %>>
                <label class="form-check-label" for="schema-unique<%= data.index %>">Yes</label>
              </div>
              <div class="form-check form-check-inline">
                <input type="radio" data-name="unique" name="schema-unique<%= data.index %>" class="form-check-input" type="radio" name="exampleRadios" id="schema-unique<%= data.index %>" value="false" <% if (data.constraints.unique == false) { %> checked="checked" <% } %> >
                <label class="form-check-label" for="schema-unique<%= data.index %>">No</label>
              </div>
            </div>
        </div>
    </div>
</div>
<datalist id="dataTypeList">
    <option value="float">Decimal number/float</option>
    <option value="integer">Integer</option>
    <option value="text">Text string</option>
    <option value="boolean">True or false</option>
    <option value="date">Date (without time)</option>
    <option value="time">Time</option>
    <option value="dateTime">Date AND time</option>
</datalist>
<datalist id="dataFormatList">
    <option value="YYYY">Four digit year e.g. 2018</option>
    <option value="YYYY-MM">Year and month e.g. 2018-12</option>
    <option value="YYYY-MM-DD">ISO date e.g. 2018-12-25</option>
    <option value="HH:MM:SS">ISO time e.g. 13:30:25</option>
</datalist>
`)
