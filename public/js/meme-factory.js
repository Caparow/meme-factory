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

        (function changeTimeFormat() {
            var $timeElements = $('.creating-time');

            if ($timeElements.length > 0) {
                $timeElements.each(function(id, elem) {
                    var $elem = $(elem),
                        date = Date.parse($elem.text());
                    
                    date = new Date(date);
                    $elem.text(date.toLocaleString());
                });
            }
        })();

        (function initializeLikeDislike() {
            var url = '',
                $votingForm = $('#votingForm');

            $('.up-vote-post').on('click', function(e) {
                callSubmitAction(e)
            });

            $('.down-vote-post').on('click', function (e) {
                callSubmitAction(e)
            });
        })();

        function callSubmitAction(e) {
            var $target = $(e.target),
                url;

            if (!$target.attr('formaction')) {
                url = $target.parent().attr('formaction');
            } else {
                url = $target.attr('formaction');
            }

            $target.parents('.voting-form').addClass('currently-voting');

            votingFormSubmitAction(url, $target);
        }

        function votingFormSubmitAction(url, target) {
            var xhr;

            xhr = new XMLHttpRequest();
            
            xhr.onreadystatechange = function (e) {
                if (this.readyState == 4 && this.status == 200) {
                    $('.currently-voting .points-number').text(xhr.response);
                    $('.currently-voting').removeClass('currently-voting');
                }
            }

            xhr.open('POST', url, true);
            xhr.send();
        }
    });
})();