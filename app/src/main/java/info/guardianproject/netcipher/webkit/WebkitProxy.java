/*
 * Copyright 2015 Anthony Restaino
 * Copyright 2012-2016 Nathan Freitas

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.guardianproject.netcipher.webkit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.os.Parcelable;
import android.util.ArrayMap;

import net.opendasharchive.openarchive.util.Utility;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.annotation.Nullable;

import info.guardianproject.netcipher.proxy.OrbotHelper;
import timber.log.Timber;

public class WebkitProxy {

    private final static int REQUEST_CODE = 0;

    private WebkitProxy() {
        // this is a utility class with only static methods
    }

    public static boolean setProxy(Context ctx, String host, int port) {
        setSystemProperties(host, port);

        return setWebkitProxyLollipop(ctx, host, port);
    }

    private static void setSystemProperties(String host, int port) {

        System.setProperty("proxyHost", host);
        System.setProperty("proxyPort", Integer.toString(port));

        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", Integer.toString(port));

        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", Integer.toString(port));

        System.setProperty("socks.proxyHost", host);
        System.setProperty("socks.proxyPort", Integer.toString(OrbotHelper.DEFAULT_PROXY_SOCKS_PORT));

        System.setProperty("socksProxyHost", host);
        System.setProperty("socksProxyPort", Integer.toString(OrbotHelper.DEFAULT_PROXY_SOCKS_PORT));
    }

    private static void resetSystemProperties() {

        System.setProperty("proxyHost", "");
        System.setProperty("proxyPort", "");

        System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort", "");

        System.setProperty("https.proxyHost", "");
        System.setProperty("https.proxyPort", "");

        System.setProperty("socks.proxyHost", "");
        System.setProperty("socks.proxyPort", Integer.toString(OrbotHelper.DEFAULT_PROXY_SOCKS_PORT));

        System.setProperty("socksProxyHost", "");
        System.setProperty("socksProxyPort", Integer.toString(OrbotHelper.DEFAULT_PROXY_SOCKS_PORT));
    }


    public static boolean resetLollipopProxy(Context appContext) {
        return setWebkitProxyLollipop(appContext, null, 0);
    }

    // http://stackanswers.com/questions/25272393/android-webview-set-proxy-programmatically-on-android-l
     // for android.util.ArrayMap methods
    @SuppressWarnings({"rawtypes", "JavaReflectionMemberAccess", "unchecked"})
    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
    private static boolean setWebkitProxyLollipop(Context appContext, String host, int port) {

        try {
            Class applictionClass = Class.forName("android.app.Application");
            Field mLoadedApkField = applictionClass.getDeclaredField("mLoadedApk");
            mLoadedApkField.setAccessible(true);

            Object mloadedApk = mLoadedApkField.get(appContext);
            Class loadedApkClass = Class.forName("android.app.LoadedApk");
            Field mReceiversField = loadedApkClass.getDeclaredField("mReceivers");
            mReceiversField.setAccessible(true);

            ArrayMap receivers = (ArrayMap) mReceiversField.get(mloadedApk);

            if (receivers != null) {
                for (Object receiverMap : receivers.values()) {
                    for (Object receiver : ((ArrayMap) receiverMap).keySet()) {
                        Class clazz = receiver.getClass();

                        if (clazz.getName().contains("ProxyChangeListener")) {
                            Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                            Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);
                            Object proxyInfo = null;

                            if (host != null) {
                                final String CLASS_NAME = "android.net.ProxyInfo";
                                Class cls = Class.forName(CLASS_NAME);
                                Method buildDirectProxyMethod = cls.getMethod("buildDirectProxy", String.class, Integer.TYPE);
                                proxyInfo = buildDirectProxyMethod.invoke(cls, host, port);
                            }

                            intent.putExtra("proxy", (Parcelable) proxyInfo);
                            onReceiveMethod.invoke(receiver, appContext, intent);
                        }
                    }
                }
            }

            return true;
        }
        catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException |
                 NoSuchMethodException | InvocationTargetException e)
        {
            Timber.d(e,"Exception setting WebKit proxy on Lollipop through ProxyChangeListener.");
        }

        return false;
    }

    @SuppressWarnings("unused")
    public static boolean resetProxy(String appClass, Context ctx) {
        resetSystemProperties();

        return resetLollipopProxy(ctx);
    }

    @SuppressWarnings({"unused", "rawtypes"})
    @SuppressLint("PrivateApi")
    @Nullable
    public static Object getRequestQueue(Context ctx) throws Exception {
        Object ret = null;
        Class networkClass = Class.forName("android.webkit.Network");

        Object networkObj = invokeMethod(networkClass, "getInstance",
                new Object[]{ ctx }, Context.class);

        if (networkObj != null) {
            ret = getDeclaredField(networkObj, "mRequestQueue");
        }

        return ret;
    }

    @SuppressWarnings("SameParameterValue")
    private static Object getDeclaredField(Object obj, String name)
            throws NoSuchFieldException, IllegalAccessException
    {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);

        return f.get(obj);
    }

    @SuppressWarnings({"SameParameterValue", "rawtypes", "unchecked"})
    private static Object invokeMethod(Object object, String methodName, Object[] params,
                                       Class... types) throws Exception
    {
        Object out;
        Class c = object instanceof Class ? (Class) object : object.getClass();

        if (types != null) {
            Method method = c.getMethod(methodName, types);
            out = method.invoke(object, params);
        }
        else {
            Method method = c.getMethod(methodName);
            out = method.invoke(object);
        }

        return out;
    }

    public static Socket getSocket(String proxyHost, int proxyPort) throws IOException
    {
        Socket sock = new Socket();

        sock.connect(new InetSocketAddress(proxyHost, proxyPort), 10000);

        return sock;
    }

    @SuppressWarnings("unused")
    public static Socket getSocket() throws IOException {
        return getSocket(OrbotHelper.DEFAULT_PROXY_HOST, OrbotHelper.DEFAULT_PROXY_SOCKS_PORT);
    }

    @SuppressWarnings("unused")
    @Nullable
    public static AlertDialog initOrbot(Activity activity,
                                        CharSequence stringTitle,
                                        CharSequence stringMessage,
                                        CharSequence stringButtonYes,
                                        CharSequence stringButtonNo)
    {
        Intent intentScan = new Intent("org.torproject.android.START_TOR");
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);

        try {
            activity.startActivityForResult(intentScan, REQUEST_CODE);
            return null;
        }
        catch (ActivityNotFoundException e) {
            return showDownloadDialog(activity, stringTitle, stringMessage, stringButtonYes,
                    stringButtonNo);
        }
    }

    private static AlertDialog showDownloadDialog(final Activity activity,
                                                  CharSequence stringTitle,
                                                  CharSequence stringMessage,
                                                  CharSequence stringButtonYes,
                                                  CharSequence stringButtonNo)
    {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(stringTitle);
        downloadDialog.setMessage(stringMessage);

        downloadDialog.setPositiveButton(stringButtonYes, (dialogInterface, i) ->
                Utility.INSTANCE.openStore(activity, "org.torproject.android"));

        downloadDialog.setNegativeButton(stringButtonNo, (dialogInterface, i) -> {});

        return downloadDialog.show();
    }
}
