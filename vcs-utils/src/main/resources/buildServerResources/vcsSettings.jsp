<%@ include file="/include.jsp" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="keys" class="com.arcbees.vcs.VcsConstants"/>

<script type="text/javascript">
    BS.VcsSettings = {
        onSelect: function (sel) {
            switch (sel.value) {
                case 'stash':
                    $j('#${keys.serverUrl}').show();
                    $j('#${keys.serverUrl} input').val('');
                    break;
                case 'github':
                    $j('#${keys.serverUrl}').show();
                    $j('#${keys.serverUrl} input').val('https://api.github.com');
                    break;
                case 'bitbucket':
                    $j('#${keys.serverUrl}').hide();
                    $j('#${keys.serverUrl} input').val('https://bitbucket.org');
            }
        }
    }

    window.onload = function () {
        BS.VcsSettings.onSelect($j('#${keys.vcsType}'));
    }
</script>

<tr>
    <td colspan="2">Specify VCS repository name and credentials</td>
</tr>
<tr>
    <th>VCS Type<l:star/></th>
    <td>
        <props:selectProperty name="${keys.vcsType}" id="${keys.vcsType}" onchange="BS.VcsSettings.onSelect(this)">
            <props:option value="bitbucket">Bitbucket</props:option>
            <props:option value="stash">Stash</props:option>
            <props:option value="github">GitHub</props:option>
        </props:selectProperty>
    </td>
</tr>
<tr id="${keys.serverUrl}">
    <th>Server URL:<l:star/></th>
    <td>
        <props:textProperty name="${keys.serverUrl}" className="longField"/>
        <span class="error" id="error_${keys.serverUrl}"></span>
        <span class="smallNote">Server URL</span>
    </td>
</tr>
<l:settingsGroup title="Authentication">
    <tr>
        <th>User Name:<l:star/></th>
        <td>
            <props:textProperty name="${keys.userNameKey}" className="longField"/>
            <span class="error" id="error_${keys.userNameKey}"></span>
            <span class="smallNote">VCS user name</span>
        </td>
    </tr>
    <tr>
        <th>Password:<l:star/></th>
        <td>
            <props:passwordProperty name="${keys.passwordKey}" className="longField"/>
            <span class="error" id="error_${keys.passwordKey}"></span>
            <span class="smallNote">VCS password</span>
        </td>
    </tr>
</l:settingsGroup>
<l:settingsGroup title="Repository">
    <tr>
        <th>Owner:<l:star/></th>
        <td>
            <props:textProperty name="${keys.repositoryOwnerKey}" className="longField"/>
            <span class="error" id="error_${keys.repositoryOwnerKey}"></span>
            <span class="smallNote">VCS repository owner (user or organization)</span>
        </td>
    </tr>
    <tr>
        <th>Repository:<l:star/></th>
        <td>
            <props:textProperty name="${keys.repositoryNameKey}" className="longField"/>
            <span class="error" id="error_${keys.repositoryNameKey}"></span>
            <span class="smallNote">VCS repository name</span>
        </td>
    </tr>
</l:settingsGroup>
