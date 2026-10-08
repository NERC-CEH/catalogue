import _ from 'underscore'

export default _.template(`
<%
    const excluded = [
        'ModelType',
        'ObjectInputView',
        'el',
        'index',
        'label',
        'model',
        'modelAttribute',
        'multiline',
        'parentModel'
    ]

    const predicate = Object.keys(data).find(key => !excluded.includes(key)) || ''
    const object = predicate ? data[predicate] : ''
%>
<!-- These will be replaced with autocomplete inputs-->
<div class="row">

    <div class="col-lg-3 col-sm-5">
        <div class="form-floating">
            <input data-name="predicate" type="text" id="thing_<%= data.index %>_Predicate" class="editor-input" value="<%= predicate %>" placeholder="Predicate">
            <label for="thing_<%= data.index %>_Predicate">Predicate</label>
        </div>
    </div>

    <div class="col-lg-9 col-sm-7">
        <div class="form-floating">
            <input data-name="object" type="text" id="thing_<%= data.index %>_Object" class="editor-input" value="<%= object %>" placeholder="Object">
            <label for="thing_<%= data.index %>_Object">Object</label>
        </div> 
    </div>
</div>
`)