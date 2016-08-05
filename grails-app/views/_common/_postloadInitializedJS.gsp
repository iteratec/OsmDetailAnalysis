<script type="text/javascript">
    $( window ).load(function() {

        window.addEventListener("PostLoadedScriptArrived",function(){

            var idOfItemToBeDeleted = ${item ? item.id : params.id ?: 'null'};

            POSTLOADED = new PostLoaded({
                %{--i18n_deletionConfirmMessage: '${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}',--}%
            });
        });

        var loader = new PostLoader();
        loader.loadJavascript('<g:assetPath src="postload/application-postload.js" absolute="true"/>');

    });
</script>