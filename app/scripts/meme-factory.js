(function wrapScripts() {
    $(document).ready(function() {
        (function initializeSearchInput() {
            var $searchInput = $('.search-input'),
                $searchBtn = $('.search-btn');
    
            $searchInput.focusin(function() {
                $searchBtn.addClass('search-btn-focused');
            });
            $searchInput.focusout(function () {
                $searchBtn.removeClass('search-btn-focused');
            });
    
            $searchInput.hover(
                function() {
                    $searchBtn.addClass('search-btn-hovered');
                },
                function() {
                    $searchBtn.removeClass('search-btn-hovered');
                }
            );
        })();

        (function initializeCollapsableNav() {
            $(document).on('click', function(e) {
                var $target = $(e.target);

                if ($target.hasClass('navbar-toggler') || $target.parents('.navbar-toggler') > 0) return;

                if (($target.parents('.navbar-collapse').length === 0 || $target.hasClass('nav-link')) && !$('.navbar-toggler').hasClass('collapsed')) {
                    $('.navbar-toggler').click();
                }
            })
        })();

        (function initializeLikeDislike() {
            var url = '',
                $votingForm = $('#votingForm');

            $('#upVotePost').on('click', function(e) {
                url = $(e.target).attr('formaction');
            });

            $('#downVotePost').on('click', function (e) {
                url = $(e.target).attr('formaction');
            });

            $votingForm.on('submit', function() {
                votingFormSubmitAction(url, $votingForm);
            });
        })();

        function votingFormSubmitAction(url, $votingForm) {
            var xhr;

            xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function () {
                if (this.readyState == 4 && this.status == 200) {
                    console.log(xhr.response);
                    console.log(xhr.responseText);

                    url = '';
                }
            }

            xhr.open('POST', url, true);
            xhr.send();
            $votingForm.off('submit')
            $votingForm.on('submit', votingFormSubmitAction);
        }
    });
})();