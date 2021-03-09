const CryptoJS = require("crypto-js");
const moment = require('moment');

/**
 * returns gmt timestamp
 */
const getTimeStamp = () => {
    let now = moment.utc();
    return moment(now, "Y-M-d'T'H:m:s'Z'", true).format();
};
/**
 * urlEncodes query string
 * @param mapToSerialize
 * @returns {string}
 */
const serialize = (mapToSerialize) => {
    var str = [];
    mapToSerialize.forEach((value, key) => {
        str.push(encodeURIComponent(key) + "=" + encodeURIComponent(value));
    });
    return str.join("&");
};
/**
 * generates base64 hash using HMAC SHA256
 */
const generateHash = (message, secret) => {
    var hash = CryptoJS.HmacSHA256(message, secret);
    var encodedHash = CryptoJS.enc.Base64.stringify(hash);
    return encodeURIComponent(encodedHash);
};

module.exports = {
    serialize,
    generateHash,
    getTimeStamp
}