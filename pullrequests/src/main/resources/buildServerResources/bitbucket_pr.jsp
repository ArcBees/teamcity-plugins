<%@ include file="/include.jsp" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="keys" class="com.arcbees.bitbucket.BitbucketConstants"/>
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

<tr>
    <td colspan="2">Specify Bitbucket repository name and credentials</td>
</tr>
<l:settingsGroup title="Authentication">
    <tr>
        <th>User Name:<l:star/></th>
        <td>
            <props:textProperty name="${keys.userNameKey}" className="longField"/>
            <span class="error" id="error_${keys.userNameKey}"></span>
            <span class="smallNote">Bitbucket user name</span>
        </td>
    </tr>
    <tr>
        <th>Password:<l:star/></th>
        <td>
            <props:passwordProperty name="${keys.passwordKey}" className="longField"/>
            <span class="error" id="error_${keys.passwordKey}"></span>
            <span class="smallNote">Bitbucket password</span>
        </td>
    </tr>
</l:settingsGroup>
<l:settingsGroup title="Repository">
    <tr>
        <th>Owner:<l:star/></th>
        <td>
            <props:textProperty name="${keys.repositoryOwnerKey}" className="longField"/>
            <span class="error" id="error_${keys.repositoryOwnerKey}"></span>
            <span class="smallNote">Bitbucket repository owner (user or organization)</span>
        </td>
    </tr>
    <tr>
        <th>Repository:<l:star/></th>
        <td>
            <props:textProperty name="${keys.repositoryNameKey}" className="longField"/>
            <span class="error" id="error_${keys.repositoryNameKey}"></span>
            <span class="smallNote">Bitbucket repository name</span>
        </td>
    </tr>
</l:settingsGroup>
