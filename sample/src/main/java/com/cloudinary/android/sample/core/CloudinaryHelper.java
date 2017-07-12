package com.cloudinary.android.sample.core;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.Transformation;
import com.cloudinary.android.CldAndroid;
import com.cloudinary.android.Utils;
import com.cloudinary.android.sample.model.Resource;

import java.util.Map;

public class CloudinaryHelper {
    public static String uploadImage(String uri) {
        return CldAndroid.get().upload(Uri.parse(uri))
                .unsigned("sample_app_preset")
//                .option("return_delete_token", true)
                .policy(CldAndroid.get().getGlobalUploadPolicy().newBuilder().maxRetries(10).build())
                .dispatch();
    }

    public static String getCroppedThumbnailUrl(int size, Resource resource) {

        return CldAndroid.get().getCloudinary().url()
                .resourceType(resource.getResourceType())
                .transformation(new Transformation().crop("thumb").gravity("auto").width(size).height(size))
                .generate(resource.getCloudinaryPublicId());
    }

    public static String getOriginalSizeImage(String imageId) {
        return CldAndroid.get().getCloudinary().url().generate(imageId);
    }

    public static String getUrlForMaxWidth(Context context, String imageId) {
        int width = Utils.getScreenWidth(context);
        return CldAndroid.get().getCloudinary().url().transformation(new Transformation().width(width)).generate(imageId);
    }

    public static void deleteByToken(final String token, final DeleteCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map res = CldAndroid.get().getCloudinary().uploader().deleteByToken(token);
                    if (res != null && res.containsKey("result") && res.get("result").equals("ok")) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Unknown error.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }

    public interface DeleteCallback {
        void onSuccess();

        void onError(String error);
    }
}
