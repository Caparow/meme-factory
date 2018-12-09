$(document).ready(function () {
    (function initializeLikeDislike() {
        var url = '',
            $votingForm = $('#votingForm');

        $('.up-vote-post').on('click', function (e) {
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

    console.log('here it is!');

    (function setUpInitialVotingFormPositioning() {
        var elementsToCheckOffset = $('.card').toArray(),
            currentScrollPosition = $(window).scrollTop(),
            $currentElement, $currentVotingForm;

        for (var i = 0, len = elementsToCheckOffset.length; i < len; i++) {
            $currentElement = $(elementsToCheckOffset[i]);
            $currentVotingForm = $currentElement.find('.voting-form');

            if ($currentElement.find('.card-footer').offset().top <= currentScrollPosition) {
                $currentVotingForm.addClass('sticked-to-bottom');
            } else if ($currentElement.find('.card-header').offset().top < currentScrollPosition) {
                $currentVotingForm.addClass('sticked-to-top');
            }
        }
    })();

    var lastScrollTop = $(window).scrollTop();

    (function initializeStickyScroll() {
        var $window = $(window),
            $stickedToBottom = $('.sticked-to-bottom'),
            $nextUpperElementToProcess = ($stickedToBottom.length > 0) ? $stickedToBottom.last().parents('.card') : undefined,
            $nextLowerElementToProcess = $('.voting-form').not('.sticked-to-top, .sticked-to-bottom').first().parents('.card');

        $window.on('scroll', handleScroll);

        function handleScroll() {
            $window.off('scroll');
            
            moveVotingForm();

            setTimeout(() => {
                $window.on('scroll', handleScroll);
                moveVotingForm();
            }, 100);
        }

        function moveVotingForm() {
            var currentScrollTop = $window.scrollTop(),
                $stickedToTopElement = $('.sticked-to-top'),
                $parentOfStickedToTop = $stickedToTopElement.parents('.card');

            if ($window.scrollTop() > lastScrollTop) {
                if ($stickedToTopElement.length > 0 && 
                    $stickedToTopElement.offset().top + $stickedToTopElement.outerHeight() >= $parentOfStickedToTop.offset().top + $parentOfStickedToTop.outerHeight()) {
                    $stickedToTopElement.removeClass('sticked-to-top').addClass('sticked-to-bottom');
                    $nextUpperElementToProcess = $parentOfStickedToTop;
                    $nextLowerElementToProcess = ($parentOfStickedToTop.next().length > 0 ? $parentOfStickedToTop.next() : undefined);
                } else if ($nextLowerElementToProcess && $nextLowerElementToProcess.find('.voting-form').offset().top <= $window.scrollTop()) {
                    $nextLowerElementToProcess.find('.voting-form').addClass('sticked-to-top');
                }
            } else {
                if ($stickedToTopElement.length > 0 && $stickedToTopElement.offset().top <= $parentOfStickedToTop.find('.card-header').offset().top) {
                    $stickedToTopElement.removeClass('sticked-to-top');
                    $nextUpperElementToProcess = ($parentOfStickedToTop.prev().length > 0) ? $parentOfStickedToTop.prev() : undefined;
                    $nextLowerElementToProcess = $parentOfStickedToTop;
                } else if ($nextUpperElementToProcess && $nextUpperElementToProcess.find('.voting-form').offset().top >= $window.scrollTop()) {
                    $nextUpperElementToProcess.find('.voting-form').removeClass('sticked-to-bottom').addClass('sticked-to-top');
                }
            }

            lastScrollTop = currentScrollTop;
        }
    })();
});