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
    });
})();