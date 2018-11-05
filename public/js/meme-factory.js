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

        (function initializeFormInputs() {
            var $formInputs = $('.login-field, .password-field, .firstname-field, .surname-field').find('input');

            $formInputs.focusin(function (e) {
                $(e.target).parent().addClass('form-input-focused');
            });
            $formInputs.focusout(function (e) {
                $(e.target).parent().removeClass('form-input-focused');
            });
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

        (function initializeUserMenu() {
            var $userMenu = $('.user-menu');

            $('.user-image').on('click', function(e) {
                e.stopPropagation();
                $userMenu.toggle();
            });

            $(document).on('click', function(e) {
                var $target = $(e.target);

                if (!$target.hasClass('user-menu') && $target.parents('.user-menu').length === 0) {
                    $userMenu.hide();
                }
            });
        })();

        (function initializeLikeDislike() {
            var url = '',
                $votingForm = $('#votingForm');

            $('#upVotePost').on('click', function(e) {
                url = $(e.target).attr('formaction');

                votingFormSubmitAction(url);
            });

            $('#downVotePost').on('click', function (e) {
                url = $(e.target).attr('formaction');
            
                votingFormSubmitAction(url);
            });
        })();

        function votingFormSubmitAction(url) {
            var xhr;

            xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function () {
                if (this.readyState == 4 && this.status == 200) {
                    $('#pointsNumber').text(xhr.response);
                }
            }

            xhr.open('POST', url, true);
            xhr.send();
        }
    });
})();