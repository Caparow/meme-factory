var counter = 0;

$(document).ready(function () {
    $('#fieldTypesList').on('change', function (e) {
        var $target = $(e.target),
            $addFieldButton = $('#addFieldButton');

        if ($target.val() !== 'default') {
            $addFieldButton.addClass('pulsing');
        } else {
            $addFieldButton.removeClass('pulsing');
        }
    });

    $('#addFieldButton').on('click', function () {
        var fieldValue = $('#fieldTypesList').val(),
            fieldWrapper, fieldInput, closeButton;

        counter += 1;

        switch (fieldValue) {
            case 'text':
                createTextNode(fieldValue);
                break;
            case 'file':
                fieldWrapper = document.createElement('div');
                fieldWrapper.classList.add('form-group', 'create-meme-field-wrapper');
                fieldInput = document.createElement('input');
                fieldInput.classList.add('form-control-file');
                fieldInput.id = 'FILE-' + counter.toString();
                fieldInput.setAttribute('type', 'file');
                fieldInput.setAttribute('accept', 'image/*,video/*,audio/mpeg');
                fieldInput.setAttribute('aria-describedby', 'fileHelp');
                fieldInput.setAttribute('name', 'FILE-' + counter.toString());

                closeButton = document.createElement('div');
                closeButton.className = 'close-meme-field';

                fieldWrapper.append(fieldInput);
                fieldWrapper.appendChild(fieldInput);
                fieldWrapper.appendChild(closeButton);
                $('#newPostForm').append(fieldWrapper);
                break;
            case 'html':
                createTextNode(fieldValue);
                break;
        }
    });

    function createTextNode(fieldValue) {
        var fieldWrapper, fieldInput, closeButton;

        fieldWrapper = document.createElement('div');
        fieldWrapper.classList.add('form-group', 'create-meme-field-wrapper');
        fieldInput = document.createElement('textarea');
        fieldInput.classList.add('form-control');

        if (fieldValue === 'text') {
            fieldInput.id = 'TEXT-' + counter.toString();
            fieldInput.setAttribute('placeholder', 'Meme Text');
            fieldInput.setAttribute('name', 'TEXT-' + counter.toString());
        } else {
            fieldInput.id = 'HTML-' + counter.toString();
            fieldInput.setAttribute('placeholder', 'Meme HTML content');
            fieldInput.setAttribute('name', 'HTML-' + counter.toString());
        }

        closeButton = document.createElement('div');
        closeButton.className = 'close-meme-field';

        fieldWrapper.appendChild(fieldInput);
        fieldWrapper.appendChild(closeButton);
        $('#newPostForm').append(fieldWrapper);
    }

    $(document).on('click', '.close-meme-field', function (e) {
        counter -= 1;
        $(e.target).parents('.create-meme-field-wrapper').remove();
    })
});