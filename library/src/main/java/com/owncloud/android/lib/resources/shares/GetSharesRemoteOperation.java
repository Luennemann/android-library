/* ownCloud Android Library is available under MIT license
 *   @author masensio
 *   @author David A. Velasco
 *   Copyright (C) 2015 ownCloud Inc.
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

package com.owncloud.android.lib.resources.shares;

import static com.owncloud.android.lib.resources.shares.ShareUtils.INCLUDE_TAGS;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.LegacyRemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.util.List;

/**
 * Get the data from the server about ALL the known shares owned by the requester.
 */
public class GetSharesRemoteOperation extends LegacyRemoteOperation<List<OCShare>> {

    private static final String TAG = GetSharesRemoteOperation.class.getSimpleName();
    private boolean sharedWithMe = false;

    public GetSharesRemoteOperation() {
        this(false);
    }

    public GetSharesRemoteOperation(boolean sharedWithMe) {
        this.sharedWithMe = sharedWithMe;
    }

    @Override
    protected RemoteOperationResult<List<OCShare>> run(OwnCloudClient client) {
        RemoteOperationResult<List<OCShare>> result;
        int status;

        // Get Method
        GetMethod get = null;

        // Get the response
        try {
            get = new GetMethod(client.getBaseUri() + ShareUtils.SHARING_API_PATH);
            get.setQueryString(INCLUDE_TAGS);
            get.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            if (sharedWithMe) {
                get.setQueryString("shared_with_me=true");
            }

            status = client.executeMethod(get);

            if (isSuccess(status)) {
                String response = get.getResponseBodyAsString();

                // Parse xml response and obtain the list of shares
                ShareToRemoteOperationResultParser parser = new ShareToRemoteOperationResultParser(
                        new ShareXMLParser()
                );
                parser.setServerBaseUri(client.getBaseUri());
                result = parser.parse(response);
            } else {
                result = new RemoteOperationResult<>(false, get);
            }

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Exception while getting remote shares ", e);

        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
        return result;
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK);
    }


}
