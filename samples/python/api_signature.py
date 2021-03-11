#!/usr/bin/python3

import hashlib
import hmac
import base64
import urllib.parse
from datetime import datetime

# Production URL
cmpUrl = "https://api.scanlife.com/api"

apiKey = "<YOUR KEY HERE>"
apiSecret = "<YOUR SECRET HERE>"


###
# @params
#
def serialize_params(params):

    param_list = []
    for attr, value in params.items():
        param_list.append((attr,value))
        print(attr, value)

    print(param_list)
    param_list = sorted(param_list, key=lambda x: x[0])
    print(param_list)
    return urllib.parse.urlencode(param_list)

#//2021-3-11T06-27-45Z
def generate_timestamp():
    now = datetime.utcnow()  # current date and time
    return now.strftime("%Y-%-m-%dT%H:%M:%SZ")


# samples for :
def generate_url(params, action):
    params['apikey'] = apiKey
    params['timestamp'] = generate_timestamp()

    data_payload = serialize_params(params)

    h = hmac.new(bytes(apiSecret, "UTF-8"), bytes(data_payload, "UTF-8"), hashlib.sha256)
    signature = base64.b64encode(h.digest())
    signature = urllib.parse.quote_plus(signature.decode('ascii'))

    url = cmpUrl + "/code/" + action + "?" + data_payload + "&signature=" + signature
    return url


# get code
def get_code():

    params = {'codeid': 111111 }
    return generate_url(params, "getcode")


def get_codelist():
    params = {} # TODO: Add any optional parameters here
    return generate_url(params, "getcodelist")


def create_code():
    params = {
        'codename': 'my-test-code-name',
        'url': 'https://google.com'

        # TODO: Add any more parameters here
        # campaignid

    }

    return generate_url(params, "createcode")


# Run the sample methods here, generate URL's to test
print(get_code())
#print(get_codelist())

