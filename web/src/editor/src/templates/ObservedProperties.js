import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2">
        <label for="schema-name<%= data.index %>">Field</label>
    </div>
    <div class="col-sm-10">
        <input data-name="name" id="schema-name<%= data.index %>" class="editor-input" value="<%= data.name %>" placeholder="name of field/column" />
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="schema-title<%= data.index %>">Title</label>
    </div>
    <div class="col-sm-10">
        <input data-name="title" id="schema-title<%= data.index %>" class="editor-input" value="<%= data.title %>" placeholder="A nicer human readable label for the field (optional)" />
    </div>
</div>
<div class="row">
    <div class="col-sm-2 col-xs-12">
        <label for="schema-type<%= data.index %>">Type</label>
    </div>
    <div class="col-sm-4 col-xs-12">
        <input list="typeList" data-name="type" id="schema-type<%= data.index %>" class="editor-input" value="<%= data.type %>" placeholder="" />
    </div>
    <div class="col-sm-2 col-xs-12">
        <div class="hidden-xs text-right">
            <label for="schema-format<%= data.index %>">Format</label>
        </div>
        <div class="visible-xs-inline">
            <label for="schema-format<%= data.index %>">Format</label>
        </div>
    </div>
    <div class="col-sm-4 col-xs-12">
        <input list="formatList" data-name="format" id="schema-format<%= data.index %>" class="editor-input" value="<%= data.format %>" placeholder="recommended for dates and times" />
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="schema-units<%= data.index %>">Unit</label>
    </div>
    <div class="col-sm-4">
        <input data-name="units" id="schema-units<%= data.index %>" class="editor-input" value="<%= data.units %>"/>
    </div>
    <div class="col-sm-2">
        <div class="hidden-xs text-right">
            <label for="schema-unitsUri<%= data.index %>">>Unit uri</label>
        </div>
        <div class="visible-xs-inline">
            <label for="schema-unitsUri<%= data.index %>">>Unit uri</label>
        </div>
    </div>
    <div class="col-sm-4">
        <input data-name="unitsUri" id="schema-unitsUri<%= data.index %>" class="editor-input" value="<%= data.unitsUri %>"/>
    </div>
</div>
<div class="extended hidden" id="schemaDetail<%= data.index %>">
    <div class="row">
        <div class="col-sm-2">
            <label for="schema-description<%= data.index %>">Description</label>
        </div>
        <div class="col-sm-10">
            <textarea data-name="description" id="schema-description<%= data.index %>" class="editor-textarea" rows="3"><%= data.description %></textarea>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-2">
            <label for="schema-minimum<%= data.index %>">Minimum value</label>
        </div>
        <div class="col-sm-4">
            <input data-name="minimum" type="number" id="schema-minimum<%= data.index %>" class="editor-input" value="<%= data.constraints.minimum %>"/>
        </div>
        <div class="col-sm-2">
            <label for="schema-maximum<%= data.index %>">Maximum value</label>
        </div>
        <div class="col-sm-4">
            <input data-name="maximum" type="number" id="schema-maximum<%= data.index %>" class="editor-input" value="<%= data.constraints.maximum %>"/>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-2">
            <label for="schema-minLength<%= data.index %>">Minimum length</label>
        </div>
        <div class="col-sm-4">
            <input data-name="minLength" type="number" step="1" id="schema-minLength<%= data.index %>" class="editor-input" value="<%= data.constraints.minLength %>"/>
        </div>
        <div class="col-sm-2">
            <label for="schema-maxLength<%= data.index %>">Maximum length</label>
        </div>
        <div class="col-sm-4">
            <input data-name="maxLength" type="number" step="1" id="schema-maxLength<%= data.index %>" class="editor-input" value="<%= data.constraints.maxLength %>"/>
        </div>
    </div>

    <div class="row">
        <div class="col-sm-2">
            <label>Unique</label>
        </div>
        <div class="col-sm-10">
                <label class="radio-inline">
                    <input type="radio" data-name="unique" name="schema-unique<%= data.index %>" value="true" <% if (data.constraints.unique == true) { %> checked="checked" <% } %> /> Yes
                </label>
                <label class="radio-inline">
                    <input type="radio" data-name="unique" name="schema-unique<%= data.index %>" value="false" <% if (data.constraints.unique == false) { %> checked="checked" <% } %> />No
                </label>
        </div>
    </div>
</div>

<datalist id="typeList"><!-- -->
    <option value="boolean">True or false</option>
    <option value="date">Date (without time)</option>
    <option value="datetime">Date AND time</option>
    <option value="number">Decimal number </option>
    <option value="email">Email address</option>
    <option value="geopoint">Geographic point (e.g. lon, lat)</option>
    <option value="integer">Integer</option>
    <option value="string">Text string</option>
    <option value="time">Time</option>
    <option value="uri">URI such as a web address or urn</option>
    <option value="uuid">UUID/GUID</option>
    <option value="year">Four digit year</option>
    <option value="yearmonth">Year and month (e.g. 2015-07)</option>
</datalist>
<datalist id="formatList">
    <option value="YYYY">Four digit year e.g. 2018</option>
    <option value="YYYY-MM">Year and month e.g. 2018-12</option>
    <option value="YYYY-MM-DD">ISO date e.g. 2018-12-25</option>
    <option value="HH:MM:SS">ISO time e.g. 13:30:25</option>
</datalist>
`)
