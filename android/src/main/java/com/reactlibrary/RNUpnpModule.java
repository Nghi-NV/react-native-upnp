
package com.reactlibrary;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.droidupnp.Main;
import org.droidupnp.controller.upnp.IUpnpServiceController;
import org.droidupnp.model.cling.CDevice;
import org.droidupnp.model.mediaserver.ContentDirectoryService;
import org.droidupnp.model.mediaserver.MediaServer;
import org.droidupnp.model.upnp.CallableRendererFilter;
import org.droidupnp.model.upnp.IDeviceDiscoveryObserver;
import org.droidupnp.model.upnp.IFactory;
import org.droidupnp.model.upnp.IRendererCommand;
import org.droidupnp.model.upnp.IUpnpDevice;
import org.droidupnp.model.upnp.didl.IDIDLItem;
import org.droidupnp.utils.ReactNativeJsonUtils;
import org.droidupnp.view.DeviceDisplay;
import org.droidupnp.view.SettingsActivity;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.json.JSONException;
import org.json.JSONObject;
import org.seamless.util.MimeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class RNUpnpModule extends ReactContextBaseJavaModule implements IDeviceDiscoveryObserver, Observer {

    private static final String TAG = RNUpnpModule.class.getName();
    private static ReactApplicationContext mReactContext = null;

    // Controller
    public static IUpnpServiceController upnpServiceController = null;
    public static IFactory factory = null;

    private static String mCurrentSpeakerIP;

    protected List<IUpnpDevice> list = new ArrayList<>();

    public RNUpnpModule(ReactApplicationContext reactContext) {
        super(reactContext);

        Log.e("@@@@@@", "RNUpnpModule created");

        mReactContext = reactContext;

    }

    public static String getCurrentSpeakerIP() {
        return mCurrentSpeakerIP;
    }

    @ReactMethod
    public void setCurrentSpeakerIP(String currentSpeakerIP) {
        mCurrentSpeakerIP = currentSpeakerIP;

        Log.e("setCurrentSpeakerIP", "currentSpeakerIP ==> " + currentSpeakerIP);
    }

    @ReactMethod
    public void onPause() {
        if (null != upnpServiceController) {
            upnpServiceController.pause();
            upnpServiceController.getServiceListener().getServiceConnexion().onServiceDisconnected(null);
        }

    }

    @ReactMethod
    public static void refresh() {
        if (null != upnpServiceController) {
            upnpServiceController.getServiceListener().refresh();
        }

    }

    @ReactMethod
    public void onDestroy() {
        if (null != upnpServiceController) {
            upnpServiceController.getRendererDiscovery().removeObserver(this);
            upnpServiceController.delSelectedRendererObserver(this);
        }

    }

    public static ReactApplicationContext getReactContext() {
        return mReactContext;
    }

    @Override
    public String getName() {
        return "RNUpnp";
    }

    @ReactMethod
    public void ping() {
        Toast.makeText(mReactContext, "Hello bavv", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "pingpong");
    }

    @ReactMethod
    public void loadSongs() {
        Log.d(TAG, "start loadMusics");
        mReactContext.startActivity(new Intent(mReactContext, Main.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @ReactMethod
    public void initUPNP() {
        Log.d(TAG, "start initUPNP");

        if (null == list) {
            list = new ArrayList<>();
        } else {
            list.clear();
        }

        // Use cling factory
        if (factory == null)
            factory = new org.droidupnp.controller.cling.Factory();

        // Upnp service
        if (upnpServiceController == null)
            upnpServiceController = factory.createUpnpServiceController(mReactContext);

        upnpServiceController.getRendererDiscovery().addObserver(this);
        upnpServiceController.addSelectedRendererObserver(this);

        upnpServiceController.resume(mReactContext);

        selectRenderer();
    }

    public static void selectRenderer() {
        if (null == mCurrentSpeakerIP || mCurrentSpeakerIP.trim().isEmpty()) return;
        if (null == upnpServiceController) return;

        final Collection<IUpnpDevice> upnpDevices = upnpServiceController.getServiceListener()
                .getFilteredDeviceList(new CallableRendererFilter());

        ArrayList<DeviceDisplay> list = new ArrayList<DeviceDisplay>();
        for (IUpnpDevice upnpDevice : upnpDevices)
            list.add(new DeviceDisplay(upnpDevice));

        for (DeviceDisplay deviceDisplay : list) {
            CDevice cDevice = ((CDevice) deviceDisplay.getDevice());
            RemoteDevice remoteDevice = (RemoteDevice) cDevice.getDevice();
            RemoteDeviceIdentity remoteDeviceIdentity = remoteDevice.getIdentity();
            String host = remoteDeviceIdentity.getDescriptorURL().getHost();
            if (mCurrentSpeakerIP.equals(host)) {
                upnpServiceController.setSelectedRenderer(deviceDisplay.getDevice(), false);
                return;
            }
        }
    }

    @ReactMethod
    public void next() {
        IRendererCommand rendererCommand = factory.createRendererCommand(factory.createRendererState());
        if (null != rendererCommand) rendererCommand.commandNext();
    }

    @ReactMethod
    public void previous() {
        IRendererCommand rendererCommand = factory.createRendererCommand(factory.createRendererState());
        if (null != rendererCommand) rendererCommand.commandPrevious();
    }

    @ReactMethod
    public void reloadSpeakers() {
        writeSpeakerList();
    }

    private List<Item> getSongList() {
        Log.e("getSongList", "MediaServer.getAddress() ==> " + MediaServer.getAddress());
        List<Item> songList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] columns = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM
        };
        Cursor cursor = mReactContext.getContentResolver().query(uri, columns, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String id = ContentDirectoryService.AUDIO_PREFIX + cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String creator = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));

                    String extension = "";
                    int dot = filePath.lastIndexOf('.');
                    if (dot >= 0)
                        extension = filePath.substring(dot).toLowerCase();

                    Res res = new Res(new MimeType(mimeType.substring(0, mimeType.indexOf('/')),
                            mimeType.substring(mimeType.indexOf('/') + 1)), size, "http://" + MediaServer.getAddress() + "/" + id + extension);

                    res.setDuration(duration / (1000 * 60 * 60) + ":"
                            + (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
                            + (duration % (1000 * 60)) / 1000);

                    songList.add(new MusicTrack(id, "", title, creator, album, new PersonWithRole(creator, "Performer"), res));

                    Log.v(TAG, "Added audio item " + title + " from " + filePath);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return songList;
    }

    @Override
    public void addedDevice(final IUpnpDevice device) {
        Log.v(TAG, "New device detected - addedDevice : " + device.getDisplayString());
        final String newHost = ((RemoteDevice) ((CDevice) device).getDevice()).getIdentity().getDescriptorURL().getHost();
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean isExist = false;
                    for (IUpnpDevice upnpDevice : list) {
                        String host = ((RemoteDevice) ((CDevice) upnpDevice).getDevice()).getIdentity().getDescriptorURL().getHost();
                        if (host.equals(newHost)) {
                            isExist = true;
                            break;
                        }
                    }
                    if (!isExist) {
                        list.add(device);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.v(TAG, "addedDevice : " + list.size());

                writeSpeakerList();
            }
        });
    }

    public void writeSpeakerList() {
        WritableMap params = Arguments.createMap();
        WritableArray writableArray = getHostsAsWritableArray();
        if (null == writableArray) return;

        params.putArray("hosts", writableArray);
        mReactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("speaker-found", params);
    }

    @Override
    public void removedDevice(IUpnpDevice device) {
        Log.v(TAG, "Device removed : " + device.getFriendlyName());

        final DeviceDisplay d = new DeviceDisplay(device, true);

        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Remove device from list
                    list.remove(d);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.v(TAG, "removedDevice : " + list.size());
            }
        });
    }

    @Override
    public void update(Observable observable, Object data) {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null == upnpServiceController) return;

                IUpnpDevice device = upnpServiceController.getSelectedRenderer();
                if (device != null) {
                    addedDevice(device);
                }
            }
        });
    }

    private WritableArray getHostsAsWritableArray() {
        if (null == list || list.isEmpty()) return null;

        List<String> hostList = new ArrayList<>();
        for (IUpnpDevice device : list) {
            try {
                hostList.add(((RemoteDevice) ((CDevice) device).getDevice()).getIdentity().getDescriptorURL().getHost());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Log.v(TAG, "hostList : " + hostList.size());

        WritableArray array = new WritableNativeArray();
        for (String host : hostList) {
            array.pushString(host);
        }
        return array;
    }
}