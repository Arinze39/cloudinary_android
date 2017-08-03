package com.cloudinary.android;

import android.net.Uri;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.payload.ByteArrayPayload;
import com.cloudinary.android.payload.FilePayload;
import com.cloudinary.android.payload.LocalUriPayload;
import com.cloudinary.android.payload.ResourcePayload;
import com.cloudinary.android.signed.Signature;
import com.cloudinary.android.signed.SignatureProvider;
import com.cloudinary.utils.ObjectUtils;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

public class RequestProcessorTest extends AbstractTest {

    /**
     * Centralize processor creation in case we want to test different implementations in the future.
     */
    private RequestProcessor provideRequestProcessor(DefaultCallbackDispatcher callbackDispatcher) {
        return new DefaultRequestProcessor(callbackDispatcher);
    }

    /**
     * Centralize params creation in case we want to test different implementations in the future.
     */
    private RequestParams provideRequestParams() {
        TestParams testParams = new TestParams();
        testParams.putString("requestId", UUID.randomUUID().toString());
        return testParams;
    }

    /**
     * Centralize callback dispatcher creation in case we want to test different implementations in the future.
     */
    private DefaultCallbackDispatcher provideCallbackDispatcher() {
        return new DefaultCallbackDispatcher(InstrumentationRegistry.getTargetContext());
    }

    /**
     * Call init with default parameters that fit most tests.
     */
    private void initCldAndroid() {
        CldAndroid.init(InstrumentationRegistry.getTargetContext(), ObjectUtils.asMap("cloud_name", "cloudName"));
    }

    @Test
    public void testValidUpload() throws IOException {
        // no config, takes full credentials from env variable.
        CldAndroid.init(InstrumentationRegistry.getTargetContext());

        RequestParams params = provideRequestParams();
        params.putString("uri", new FilePayload(assetToFile(TEST_IMAGE).getAbsolutePath()).toUri());
        params.putString("options", UploadRequest.encodeOptions(new HashMap<String, Object>()));

        DefaultCallbackDispatcher callbackDispatcher = provideCallbackDispatcher();
        RequestProcessor processor = provideRequestProcessor(callbackDispatcher);
        final StatefulCallback statefulCallback = new StatefulCallback();
        callbackDispatcher.registerCallback(statefulCallback);
        processor.processRequest(InstrumentationRegistry.getTargetContext(), params);

        Awaitility.await().atMost(Duration.TEN_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return statefulCallback.lastSuccess != null &&
                        statefulCallback.lastSuccess.containsKey("public_id");
            }
        });

    }

    @Test
    public void testInvalidOptions() throws IOException {
        initCldAndroid();

        RequestParams params = provideRequestParams();
        params.putString("options", "bad options string");
        params.putString("uri", new FilePayload(assetToFile(TEST_IMAGE).getAbsolutePath()).toUri());

        verifyError(params, CldAndroid.Errors.OPTIONS_FAILURE);
    }

    @Test
    public void testNoPayload() throws IOException {
        initCldAndroid();
        RequestParams params = provideRequestParams();

        params.putString("options", UploadRequest.encodeOptions(new HashMap<String, Object>()));
        verifyError(params, CldAndroid.Errors.PAYLOAD_EMPTY);
    }

    @Test
    public void testInvalidPayload() throws IOException {
        initCldAndroid();
        RequestParams params = provideRequestParams();

        params.putString("options", UploadRequest.encodeOptions(new HashMap<String, Object>()));
        params.putString("uri", "bad uri!");

        verifyError(params, CldAndroid.Errors.PAYLOAD_LOAD_FAILURE);
    }

    @Test
    public void testInvalidUriPayload() throws IOException {
        initCldAndroid();
        RequestParams params = provideRequestParams();


        params.putString("options", UploadRequest.encodeOptions(new HashMap<String, Object>()));
        params.putString("uri", new LocalUriPayload(Uri.parse("bad uri!")).toUri());

        verifyError(params, CldAndroid.Errors.URI_DOES_NOT_EXIST);
    }

    @Test
    public void testInvalidFilePayload() throws IOException {
        initCldAndroid();
        RequestParams params = provideRequestParams();


        params.putString("options", UploadRequest.encodeOptions(new HashMap<String, Object>()));
        params.putString("uri", new FilePayload("bad path!").toUri());

        verifyError(params, CldAndroid.Errors.FILE_DOES_NOT_EXIST);
    }

    @Test
    public void testInvalidByteArrayPayload() throws IOException {
        initCldAndroid();
        RequestParams params = provideRequestParams();


        params.putString("options", UploadRequest.encodeOptions(new HashMap<String, Object>()));
        params.putString("uri", new ByteArrayPayload(new byte[]{}).toUri());

        verifyError(params, CldAndroid.Errors.BYTE_ARRAY_PAYLOAD_EMPTY);
    }

    @Test
    public void testInvalidResourcePayload() throws IOException {
        initCldAndroid();
        RequestParams params = provideRequestParams();


        params.putString("options", UploadRequest.encodeOptions(new HashMap<String, Object>()));
        params.putString("uri", new ResourcePayload(-10).toUri());

        verifyError(params, CldAndroid.Errors.RESOURCE_DOES_NOT_EXIST);
    }

    @Test
    public void testSignatureFailure() throws IOException {
        // init the library with a bad signature provider
        CldAndroid.init(InstrumentationRegistry.getTargetContext(), new SignatureProvider() {
            @Override
            public Signature provideSignature(Map options) {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }
        }, ObjectUtils.asMap("cloud_name", "cloudName"));

        RequestParams params = provideRequestParams();
        HashMap<String, Object> options = new HashMap<>();

        params.putString("options", UploadRequest.encodeOptions(options));
        params.putString("uri", new FilePayload(assetToFile(TEST_IMAGE).getAbsolutePath()).toUri());

        verifyError(params, CldAndroid.Errors.SIGNATURE_FAILURE);
    }

    /**
     * Utility method to handle error checking. Throws exception if the expected error does not occur.
     *
     * @param params            Params setup to generate a specific error
     * @param expectedErrorCode The expected error code that should be generated by the given params.
     */
    private void verifyError(RequestParams params, final int expectedErrorCode) {
        DefaultCallbackDispatcher callbackDispatcher = provideCallbackDispatcher();
        RequestProcessor processor = provideRequestProcessor(callbackDispatcher);
        final StatefulCallback statefulCallback = new StatefulCallback();
        callbackDispatcher.registerCallback(statefulCallback);
        processor.processRequest(InstrumentationRegistry.getTargetContext(), params);

        Awaitility.await().atMost(Duration.TWO_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return statefulCallback.lastError == expectedErrorCode;
            }
        });
    }

    /**
     * Bundle based implementation for RequestParams, for testing purposes.
     */
    private static final class TestParams implements RequestParams {
        private final Bundle values = new Bundle();

        @Override
        public void putString(String key, String value) {
            values.putString(key, value);
        }

        @Override
        public void putInt(String key, int value) {
            values.putInt(key, value);
        }

        @Override
        public void putLong(String key, long value) {
            values.putLong(key, value);
        }

        @Override
        public String getString(String key, String defaultValue) {
            return values.getString(key, defaultValue);
        }

        @Override
        public int getInt(String key, int defaultValue) {
            return values.getInt(key, defaultValue);
        }

        @Override
        public long getLong(String key, long defaultValue) {
            return values.getLong(key, defaultValue);
        }
    }

    /**
     * Callback implementations that saved the last result
     */
    private final static class StatefulCallback implements UploadCallback {

        private Integer lastError = null;
        private Map lastSuccess = null;

        @Override
        public void onStart(String requestId) {
        }

        @Override
        public void onProgress(String requestId, long bytes, long totalBytes) {
        }

        @Override
        public void onSuccess(String requestId, Map resultData) {
            this.lastSuccess = resultData;
            this.lastError = null;
        }

        @Override
        public void onError(String requestId, int error) {
            this.lastError = error;
        }

        @Override
        public void onReschedule(String requestId, int error) {
            this.lastError = error;
        }
    }
}
