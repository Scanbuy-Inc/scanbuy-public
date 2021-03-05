const axios = require('axios');
var parser = require('fast-xml-parser');
const helper = require('./helper.js');

const scanbuyApiEndPoint = "http://api.scanlife.com/api/code/";
const apiKey = "YOUR_API_KEY";
const secretKey = "YOUR_SECRET_KEY";

const codename = "myTestCode";
const url = "http://scanbuy.com";
const codeType = "web";
const timeStamp = helper.getTimeStamp();

const queryParamMap = new Map();
//TODO: Make sure parameters below are named as mentioned in API documentation
queryParamMap.set("apikey", apiKey);
queryParamMap.set("codename",codename);
queryParamMap.set("url",url);
queryParamMap.set("codetype",codeType);
queryParamMap.set("timestamp", timeStamp);

const sortedQueryParamMap = new Map([...queryParamMap.entries()].sort()); //params needs to be sorted alphabetically
console.log(sortedQueryParamMap);

let serializedQueryString = helper.serialize(sortedQueryParamMap);

const signature = helper.generateHash(serializedQueryString, secretKey);
console.log("signature= " + signature);

const apiUrl = scanbuyApiEndPoint.concat("createcode?" + serializedQueryString + "&signature=" + signature);
console.log(apiUrl);

axios.get(apiUrl)
    .then(response => {
        console.log(response.data); // xml response here
        if(response.data) {
            const xmlData = response.data;
            if( parser.validate(xmlData) === true) { //optional (it'll return an object in case it's not valid)
                const jsonObj = parser.parse(xmlData);
                console.log(jsonObj)
            }
        }
    })
    .catch(error => {
        console.log(error);
    });


