function setCookie(key, value , duration) {
    var expires = new Date();
    expires.setTime(expires.getTime() + duration * (60 * 60 * 1000));
    document.cookie = key + '=' + value + ';expires=' + expires.toUTCString();
};

function getCookie(key) {
    var keyValue = document.cookie.match('(^|;) ?' + key + '=([^;]*)(;|$)');
    return keyValue ? keyValue[2] : null;
};

function checkSessionTimeout(){
    if(getCookie('access_token')==null&&getCookie('refresh_token')!=null)
        return true;
    else return false;
};