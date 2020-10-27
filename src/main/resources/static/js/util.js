const MONTH_NAMES = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

function formatDate(date) {
    if (!date) {
        return null;
    }

    const DAY_IN_MS = 86400000; // 24 * 60 * 60 * 1000
    const today = new Date();
    const yesterday = new Date(today - DAY_IN_MS);
    const d = new Date(date);
    const day = d.getDate();
    const month = MONTH_NAMES[d.getMonth()];

    const seconds = Math.round((today - d) / 1000);
    const minutes = Math.round(seconds / 60);
    const hours = Math.round(minutes / 60);

    const isToday = today.toDateString() === d.toDateString();
    // const isYesterday = yesterday.toDateString() === date.toDateString();
    // const isThisYear = today.getFullYear() === date.getFullYear();

    if (isToday) {
        if (seconds < 5) {
            return 'now';
        } else if (seconds < 60) {
            return `${seconds}s`;
        } else if (minutes < 60) {
            return `${minutes}m`;
        } else {
            return `${hours}h`;
        }
    } else {
        return month + ' ' + day;
    }
}

const ajaxFunc = function (method, url, data, sendAs, callback) {
    let requestData = null;
    let cType = true;
    let pData = true;

    if (sendAs === 1) {
        requestData = JSON.stringify(data);
        cType = "application/json";
    } else {
        requestData = data;
        cType = "application/x-www-form-urlencoded; charset=UTF-8";
    }

    if (sendAs === 3) {
        cType = false;
        pData = false;
    }
    $.ajax({
        method: method,
        url: encodeURI(url),
        data: requestData,
        contentType: cType,
        processData: pData
    })
        .done(callback)
        .fail(callback);
};

function initializeImagePreview() {
    let $lightbox = $('#lightbox');
    $(document).on('click', 'a.img-prev', function (event) {
        let $img = $(this).find('img'),
            src = $img.attr('src'),
            alt = $img.attr('alt');
        $lightbox.find('img').attr('src', src);
        $lightbox.find('img').attr('alt', alt);
    });
    $lightbox.on('shown.bs.modal', function (e) {
        let $img = $lightbox.find('img');
        $lightbox.find('.modal-dialog').css({'width': $img.width()});
        $lightbox.find('.close').removeClass('hidden');
    });
}

function errorMessage(toastrType, message, title) {
    switch (toastrType.toLowerCase()) {
        case "info":
            $.toast({
                heading: title,
                text: message,
                position: 'top-right',
                showHideTransition: 'plain',
                loaderBg: '#3b98b5',
                icon: 'info',
                hideAfter: 4000,
                stack: 1
            });
            break;
        case "success":
            $.toast({
                heading: title,
                text: message,
                position: 'top-right',
                showHideTransition: 'plain',
                loaderBg: '#5ba035',
                icon: 'success',
                hideAfter: 3000,
                stack: 1
            });
            break;
        case "error":
            $.toast({
                heading: title,
                text: message,
                position: 'top-right',
                showHideTransition: 'plain',
                loaderBg: '#bf441d',
                icon: 'error',
                hideAfter: 5000,
                stack: 1
            });
            break;
        case "warning":
            $.toast({
                heading: title,
                text: message,
                position: 'top-right',
                showHideTransition: 'plain',
                loaderBg: '#da8609',
                icon: 'warning',
                hideAfter: 4000,
                stack: 1
            });
            break;
    }
}