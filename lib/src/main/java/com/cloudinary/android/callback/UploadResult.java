package com.cloudinary.android.callback;

import com.cloudinary.android.CldAndroid;

import java.util.Map;

/**
 * This object contains the results of a single upload.
 * If the upload was successful the upload params will be available through {@link UploadResult#getSuccessResultData()} and {@link UploadResult#getError()}  will return {@link CldAndroid.Errors#NO_ERROR}.
 * If the upload encountered a fatal error (i.e. will not be rescheduled) there will be no data and {@link UploadResult#getError()} will return the error description.
 */
public class UploadResult {
    private final Map successResultData;
    private final int error;

    public UploadResult(Map successResultData, int error) {
        this.successResultData = successResultData;
        this.error = error;
    }

    /**
     * Upload result params. Null if the upload failed.
     */
    public Map getSuccessResultData() {
        return successResultData;
    }

    /**
     * Error description in case the upload failed. Otherwise null.
     */
    public int getError() {
        return error;
    }
}
