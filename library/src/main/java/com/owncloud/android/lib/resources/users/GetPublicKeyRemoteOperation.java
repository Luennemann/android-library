/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2017 Tobias Kaminsky
 *   Copyright (C) 2017 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.resources.users;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * Remote operation performing the fetch of the public key for an user
 */

public class GetPublicKeyRemoteOperation extends RemoteOperation<String> {

    private static final String TAG = GetPublicKeyRemoteOperation.class.getSimpleName();
    private static final String PUBLIC_KEY_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/public-key";

    // JSON node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";
    private static final String NODE_PUBLIC_KEYS = "public-keys";

    private String user;

    public GetPublicKeyRemoteOperation() {
        this.user = "";
    }

    public GetPublicKeyRemoteOperation(String user) {
        this.user = user;
    }

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult<String> run(NextcloudClient client) {
        GetMethod getMethod = null;
        RemoteOperationResult<String> result;

        try {
            // remote request
            getMethod = new GetMethod(client.getBaseUri() + PUBLIC_KEY_URL + JSON_FORMAT, true);

            if (!user.isEmpty()) {
                HashMap<String, String> map = new HashMap<>();
                map.put("users", "[\"" + user + "\"]");
                getMethod.setQueryString(map);
            } else {
                user = client.getUserId();
            }

            int status = client.execute(getMethod);

            if (status == HttpStatus.SC_OK) {
                String response = getMethod.getResponseBodyAsString();

                // Parse the response
                JSONObject respJSON = new JSONObject(response);
                String key = respJSON
                        .getJSONObject(NODE_OCS)
                        .getJSONObject(NODE_DATA)
                        .getJSONObject(NODE_PUBLIC_KEYS)
                        .getString(user);

                result = new RemoteOperationResult<>(true, getMethod);
                result.setResultData(key);
            } else {
                result = new RemoteOperationResult<>(false, getMethod);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG,
                     "Fetching of public key failed for user " + user + ": " + result.getLogMessage(),
                     result.getException());
        } finally {
            if (getMethod != null)
                getMethod.releaseConnection();
        }
        return result;
    }
}
