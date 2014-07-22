<%@ include file="/include.jsp" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="stagingKeys" class="com.arcbees.staging.Constants"/>
<jsp:useBean id="propertiesBean" type="jetbrains.buildServer.controllers.BasePropertiesBean" scope="request"/>

<script>
    BS.EditTriggersDialog.serializeParameters = function () {
        var e = BS.Util.serializeForm(this.formElement());
        var f = Form.getInputs(this.formElement(), "password");
        if (!f) {
            return e;
        }
        for (var c = 0; c < f.length; c++) {
            var b = f[c].name;
            if (b.indexOf("prop:") != 0) {
                continue;
            }
            var a = "prop:encrypted:" + f[c].id;
            e += "&" + a + "=";
            var d = $(a).value;
            if (d == "" || f[c].value != f[c].defaultValue) {
                d = BS.Encrypt.encryptData(f[c].value, this.formElement().publicKey.value);
            }
            e += d;
        }
        return e;
    }
</script>

<%@ include file="vcsSettings.jsp" %>
<tr>
    <td colspan="2">Specify Tomcat Manager URL and credentials</td>
</tr>
<l:settingsGroup title="Tomcat Manager">
    <tr>
        <th>User Name:<l:star/></th>
        <td>
            <props:textProperty name="${stagingKeys.userNameKey}" className="longField"/>
            <span class="error" id="error_${stagingKeys.userNameKey}"></span>
            <span class="smallNote">Manager user name</span>
        </td>
    </tr>
    <tr>
        <th>Password:<l:star/></th>
        <td>
            <props:passwordProperty name="${stagingKeys.passwordKey}" className="longField"/>
            <span class="error" id="error_${stagingKeys.passwordKey}"></span>
            <span class="smallNote">Manager password</span>
        </td>
    </tr>
    <tr>
        <th>Manager URL:<l:star/></th>
        <td>
            <props:textProperty name="${stagingKeys.tomcatUrl}" className="longField"/>
            <span class="error" id="error_${stagingKeys.tomcatUrl}"></span>
            <span class="smallNote">Tomcat Manager URL (ie: http://url.com/manager/text)</span>
        </td>
    </tr>
</l:settingsGroup>
<l:settingsGroup title="Other settings">
    <tr>
        <th>Base Context Path:<l:star/></th>
        <td>
            <props:textProperty name="${stagingKeys.baseContextKey}" className="longField"/>
            <span class="error" id="error_${stagingKeys.baseContextKey}"></span>
            <span class="smallNote">Base Context Path</span>
        </td>
    </tr>
    <tr>
        <th>Undeploy Branch:<l:star/></th>
        <td>
            <props:textProperty name="${stagingKeys.tomcatMergeBranch}" className="longField"/>
            <span class="error" id="error_${stagingKeys.tomcatMergeBranch}"></span>
            <span class="smallNote">Undeploy when merged into this branch</span>
        </td>
    </tr>
</l:settingsGroup>
