(function ($) {
    $.fn.scrollView = function () {
        return this.each(function () {
            $('html, body').animate({
                scrollTop: $(this).offset().top - ($(window).height() / 2)
            }, 1000);
        });
    };
    $(document).ready(function() {
        $('.highlight').scrollView();
    });
})(jQuery);



