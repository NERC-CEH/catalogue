<#import "../new-form.html.tpl" as form>

<@form.value label="Property Attributes" class="monitoring">
<ul <@form.ifReadonly>id="propertyAttributes"</@form.ifReadonly> class="list-unstyled">
    <li>
        <@form.delete name="propertyAttribute"></@form.delete>
        <@form.input name="propertyAttributes[0]" placeholder="Property Attribute (temporal entity)" value=""></@form.input>
    </li>
</ul>
</@form.value>