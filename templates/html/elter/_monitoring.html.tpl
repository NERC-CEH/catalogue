<#import "../new-form.html.tpl" as form>

<div class="value monitoring">
    <@form.value name="propertyAttributes" label="Property Attributes">
        <ul <@form.ifReadonly>id="propertyAttributes"</@form.ifReadonly> class="list-unstyled">
            <li>
                <@form.delete></@form.delete>
                <@form.input name="propertyAttributes[0]" placeholder="Property Attribute (temporal entity)" value=""></@form.input>
            </li>
        </ul>
    </@form.value>
</div>