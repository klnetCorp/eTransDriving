package kr.co.klnet.aos.etransdriving.trans.gps.push;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.ETC1;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.lbsok.framework.gis.Distance;

import java.util.List;

import kr.co.klnet.aos.etransdriving.BuildConfig;
import kr.co.klnet.aos.etransdriving.EtransDrivingApp;
import kr.co.klnet.aos.etransdriving.trans.gps.packet.JGpsInfo;

import static android.os.Build.VERSION_CODES.M;

public class JLocationManager extends Service {
    private String TAG = "JLocationManager";

    public final static String FUSED_PROVIDER = "fused";

    private final static boolean SUPPORT_FUSED_PROVIDER = true; //fused location 지원
    private final static boolean SUPPORT_GPS_PROVIDER = false; //GPS 지원
    private final static boolean SUPPORT_NET_PROVIDER = false; //network provider 지원
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    private Context mContext;
    private Criteria mCriteria;
    private JGpsInfo mGpsInfo = JGpsInfo.getInst();
    private long mMinTime = 2000L;
    private float mMinDistance = 10.0F;
    private LocationManager mLocationManager;
    private JGpsLocationListener mGpsLocListener;
    private JNetworkLocationListener mNetLocListener;
    private Location currentBestLocation = null;
    private static final int TWO_MINUTES = 120000;
    private static double mLongitude;
    private static double mLatitude;
    private static long mLngDistance;

    private String mName = "[Report]";

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;

    public JLocationManager(Context context, String name) {
        this.mContext = context;
        this.mLocationManager = (LocationManager) this.mContext.getSystemService(LOCATION_SERVICE);
        mLongitude = 0.0D;
        mLatitude = 0.0D;
        mLngDistance = 0L;
        mName = name;
    }
    public JLocationManager(Context context) {
        this.mContext = context;
        this.mLocationManager = (LocationManager) this.mContext.getSystemService(LOCATION_SERVICE);
        mLongitude = 0.0D;
        mLatitude = 0.0D;
        mLngDistance = 0L;
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();

        if (this.mGpsLocListener != null) {
            this.mLocationManager.removeUpdates(this.mGpsLocListener);
            this.mGpsLocListener = null;
        }

        if (this.mNetLocListener != null) {
            this.mLocationManager.removeUpdates(this.mNetLocListener);
            this.mNetLocListener = null;
        }

        stopFusedLocationService();

    }

    public void execute() {
        this.mCriteria = new Criteria();
        this.mCriteria.setAccuracy(2);
        this.mCriteria.setAltitudeRequired(false);
        this.mCriteria.setSpeedRequired(true);
        this.mCriteria.setBearingRequired(true);
        this.mCriteria.setCostAllowed(true);
        this.mCriteria.setPowerRequirement(1);

        startFusedProvider();
        startGpsProvider();
        startNetworkProvider();

    }

    public void setName(String tag) {
        TAG = tag;
    }

    public  void registerSateliteGpsListener(JGpsLocationListener listener) {
        mGpsLocListener = listener;
    }

    public  void registerNetworkGpsListener(JNetworkLocationListener listener) {
        mNetLocListener = listener;
    }

    public void startGpsProvider() {
        if (!isSupportProvider(LocationManager.GPS_PROVIDER)) {
            return;
        }

        if(!isPermissionOk()) return;

        if (this.mGpsLocListener == null) {
            this.mGpsLocListener = new JGpsLocationListener(TAG);
        }

        try {
            this.mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, this.mMinTime, this.mMinDistance, this.mGpsLocListener);
        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    public void startNetworkProvider() {
        if (!isSupportProvider(LocationManager.NETWORK_PROVIDER)) {
            return;
        }

        if(!isPermissionOk()) return;

        if (this.mNetLocListener == null) {
            this.mNetLocListener = new JNetworkLocationListener(TAG);

            try {
                this.mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0.0F, this.mNetLocListener);
            } catch(SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void startFusedProvider() {
        if (!isSupportProvider(FUSED_PROVIDER)) {
            return;
        }

        if(!isPermissionOk()) return;

        if(locationRequest==null) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        if (this.mFusedLocationClient == null) {
            LocationSettingsRequest.Builder builder =
                    new LocationSettingsRequest.Builder();

            builder.addLocationRequest(locationRequest);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        }

        try {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    protected void stopFusedLocationService() {
        if (!isSupportProvider(FUSED_PROVIDER)) {
            return;
        }

        if (mFusedLocationClient != null) {

            Log.d(TAG, mName + "stopFusedLocationService : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }



    public boolean isPermissionOk() {
        if (android.os.Build.VERSION.SDK_INT >= M) {
            int permissionCoarseLocation = EtransDrivingApp.getInstance().getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
//            int permissionAccessBackgorundLocation = EtransDrivingApp.getInstance().getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);

            if (permissionCoarseLocation == PackageManager.PERMISSION_DENIED
//            || permissionAccessBackgorundLocation == PackageManager.PERMISSION_DENIED
            ) {
                Log.e(TAG, mName + "need permission");
                return false;
            }
        }
        return true;
    }

    public boolean isSupportProvider(String provider) {
        if(LocationManager.GPS_PROVIDER.equalsIgnoreCase(provider)) {
            if(!SUPPORT_GPS_PROVIDER) {
                Log.w(TAG, mName + "Not support [GPS] provider!!!");
                return false;
            }
        } else if(LocationManager.NETWORK_PROVIDER.equalsIgnoreCase(provider)) {
            if(!SUPPORT_NET_PROVIDER) {
                Log.w(TAG, mName + "Not support [NETWORK] provider!!!");
                return false;
            }
        } else if(FUSED_PROVIDER.equalsIgnoreCase(provider)) {
            if(!SUPPORT_FUSED_PROVIDER) {
                Log.w(TAG, mName + "Not support [FUSED] provider!!!");
                return false;
            }
        } else {
            Log.w(TAG, mName + "Not support [" + provider + "] provider!!!");
            return false;
        }

        return true;
    }

    public Location getLatestLocation(String provider) {
        if(!isSupportProvider(provider)) {
            return null;
        }

        Location loc = null;

        if(!isPermissionOk()) return null;

        try {
            if(currentBestLocation!=null) {
                loc = new Location(provider);
                loc = currentBestLocation;
            }

            if(FUSED_PROVIDER.equalsIgnoreCase(provider)) {
                requestSingleUpdate(provider);
            } else {
                loc = this.mLocationManager.getLastKnownLocation(provider);
            }
        } catch(SecurityException e) {
            e.printStackTrace();
        }

        return loc;
    }

    public void resetCurrentBestLocation() {
        currentBestLocation = null;

    }
    public void requestSingleUpdate(String provider) {
        if (!isSupportProvider(provider)) {
            return;
        }

        try {
            Criteria criteria = new Criteria();
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            criteria.setAltitudeRequired(true);
            criteria.setBearingRequired(true);
            criteria.setSpeedRequired(true);
            criteria.setCostAllowed(true);
            criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
            criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

            if(LocationManager.GPS_PROVIDER.equalsIgnoreCase(provider)) {
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                this.mLocationManager.requestSingleUpdate(criteria, mGpsLocListener, Looper.myLooper());

            } else if(LocationManager.NETWORK_PROVIDER.equalsIgnoreCase(provider)) {
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                this.mLocationManager.requestSingleUpdate(criteria, mNetLocListener, Looper.myLooper());

            } else if(FUSED_PROVIDER.equalsIgnoreCase(provider)) {

                this.mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location location = task.getResult();
                            if(location!=null)
                                fusedLocationUpdated(location);
                        } else {
                        Log.w(TAG, mName + "[FUSED] location not found");
                    }
                });
            }
        }catch(SecurityException e) {

        }
    }

    public void removeUpdates() {
        if (this.mGpsLocListener != null) {
            this.mLocationManager.removeUpdates(this.mGpsLocListener);
            this.mGpsLocListener = null;
        }

        if (this.mNetLocListener != null) {
            this.mLocationManager.removeUpdates(this.mNetLocListener);
            this.mNetLocListener = null;
        }

    }

    class JGpsLocationListener implements LocationListener {
        private String TAG = "GPS location";
        public String name_ = "";

        JGpsLocationListener(String name) {
            name_ = name;
            TAG = "GPS " + name_;
        }

        public Location getCurrentLocation() {
            return currentBestLocation;
        }

        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Log.d(TAG, mName + "onLocationChanged, lat=" + latitude + ", lon=" + longitude);

            if (this.isBetterLocation(location, JLocationManager.this.currentBestLocation)) {
                JLocationManager.this.currentBestLocation = location;

                if (JLocationManager.mLongitude > 0.0D && JLocationManager.mLatitude > 0.0D) {
                    JLocationManager.mLngDistance = (long) Distance.calcDistance(JLocationManager.mLongitude, JLocationManager.mLatitude, longitude, latitude);
                } else {
                    JLocationManager.mLngDistance = 0L;
                }

                if (JLocationManager.mLngDistance >= 2500L && location.getSpeed() <= 9.0F) {
                    JLocationManager.mLongitude = 0.0D;
                    JLocationManager.mLatitude = 0.0D;
                    return;
                }

                if (latitude > 0.0D && longitude > 0.0D) {
                    JLocationManager.this.mGpsInfo.setLocation(JLocationManager.this.currentBestLocation);
                    JLocationManager.mLongitude = longitude;
                    JLocationManager.mLatitude = latitude;
                }
            }

        }

        public void onProviderDisabled(String provider) {
            Log.d(TAG, mName + "onProviderDisabled");
        }

        public void onProviderEnabled(String provider) {
            Log.d(TAG, mName + "onProviderEnabled");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, mName + "onStatusChanged, status=" + status);
            JLocationManager.this.mGpsInfo.setGpsStatus(status);
            switch(status) {
                case 0:
                case 1:
                case 2:
                default:
            }
        }

        protected boolean isBetterLocation(Location location, Location currentBestLocation) {
            if (currentBestLocation == null) {
                return true;
            } else {
                long timeDelta = location.getTime() - currentBestLocation.getTime();
                boolean isSignificantlyNewer = timeDelta > 120000L;
                boolean isSignificantlyOlder = timeDelta < -120000L;
                boolean isNewer = timeDelta > 0L;
                if (isSignificantlyNewer) {
                    return true;
                } else if (isSignificantlyOlder) {
                    return false;
                } else {
                    int accuracyDelta = (int)(location.getAccuracy() - currentBestLocation.getAccuracy());
                    boolean isLessAccurate = accuracyDelta > 0;
                    boolean isMoreAccurate = accuracyDelta < 0;
                    boolean isSignificantlyLessAccurate = accuracyDelta > 200;
                    boolean isFromSameProvider = this.isSameProvider(location.getProvider(), currentBestLocation.getProvider());
                    if (isMoreAccurate) {
                        return true;
                    } else if (isNewer && !isLessAccurate) {
                        return true;
                    } else {
                        return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
                    }
                }
            }
        }

        private boolean isSameProvider(String provider1, String provider2) {
            if (provider1 == null) {
                return provider2 == null;
            } else {
                return provider1.equals(provider2);
            }
        }
    }

    class JNetworkLocationListener implements LocationListener {
        private String TAG = "NETWORK location";
        public String name_ = "";

        JNetworkLocationListener(String name) {
            name_ = name;
            TAG = "NETWORK " + name_;
        }

        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Log.d(TAG, mName + "onLocationChanged, lat=" + latitude + ", lon=" + longitude);
            if (this.isBetterLocation(location, JLocationManager.this.currentBestLocation)) {
                JLocationManager.this.currentBestLocation = location;

                if (JLocationManager.mLongitude > 0.0D && JLocationManager.mLatitude > 0.0D) {
                    JLocationManager.mLngDistance = (long)Distance.calcDistance(JLocationManager.mLongitude,JLocationManager.mLatitude, longitude, latitude);
                } else {
                    JLocationManager.mLngDistance = 0L;
                }

                if (JLocationManager.mLngDistance >= 2500L && location.getSpeed() <= 9.0F) {
                    JLocationManager.mLongitude = 0.0D;
                    JLocationManager.mLatitude = 0.0D;
                    return;
                }

                if (latitude > 0.0D && longitude > 0.0D) {
                    JLocationManager.this.mGpsInfo.setLocation(JLocationManager.this.currentBestLocation);
                    JLocationManager.mLongitude = longitude;
                    JLocationManager.mLatitude = latitude;
                }
            }

            if (JLocationManager.this.mNetLocListener != null) {
                JLocationManager.this.mLocationManager.removeUpdates(JLocationManager.this.mNetLocListener);
                JLocationManager.this.mNetLocListener = null;
            }

        }

        public void onProviderDisabled(String provider) {
            Log.d(TAG, mName + "onProviderDisabled");
        }

        public void onProviderEnabled(String provider) {
            Log.d(TAG, mName + "onProviderEnabled");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, mName + "onStatusChanged, status=" + status );
            switch(status) {
                case 0:
                case 1:
                case 2:
                default:
            }
        }

        protected boolean isBetterLocation(Location location, Location currentBestLocation) {
            if (currentBestLocation == null) {
                return true;
            } else {
                long timeDelta = location.getTime() - currentBestLocation.getTime();
                boolean isSignificantlyNewer = timeDelta > 120000L;
                boolean isSignificantlyOlder = timeDelta < -120000L;
                boolean isNewer = timeDelta > 0L;
                if (isSignificantlyNewer) {
                    return true;
                } else if (isSignificantlyOlder) {
                    return false;
                } else {
                    int accuracyDelta = (int)(location.getAccuracy() - currentBestLocation.getAccuracy());
                    boolean isLessAccurate = accuracyDelta > 0;
                    boolean isMoreAccurate = accuracyDelta < 0;
                    boolean isSignificantlyLessAccurate = accuracyDelta > 200;
                    boolean isFromSameProvider = this.isSameProvider(location.getProvider(), currentBestLocation.getProvider());
                    if (isMoreAccurate) {
                        return true;
                    } else if (isNewer && !isLessAccurate) {
                        return true;
                    } else {
                        return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
                    }
                }
            }
        }

        private boolean isSameProvider(String provider1, String provider2) {
            if (provider1 == null) {
                return provider2 == null;
            } else {
                return provider1.equals(provider2);
            }
        }
    }

    public void fusedLocationUpdated(Location location) {
        JLocationManager.this.currentBestLocation = location;

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        //Log.d(TAG, mName + "[Fused] fusedLocationUpdated, lat=" + latitude + ", lon=" + longitude);

        JLocationManager.this.mGpsInfo.setLocation(JLocationManager.this.currentBestLocation);
        JLocationManager.mLongitude = longitude;
        JLocationManager.mLatitude = latitude;

    /*
                   if (this.isBetterLocation(location, JLocationManager.this.currentBestLocation)) {
                    JLocationManager.this.currentBestLocation = location;

                    if (JLocationManager.mLongitude > 0.0D && JLocationManager.mLatitude > 0.0D) {
                        JLocationManager.mLngDistance = (long)Distance.calcDistance(JLocationManager.mLongitude,JLocationManager.mLatitude, longitude, latitude);
                    } else {
                        JLocationManager.mLngDistance = 0L;
                    }

                    if (JLocationManager.mLngDistance >= 2500L && location.getSpeed() <= 9.0F) {
                        JLocationManager.mLongitude = 0.0D;
                        JLocationManager.mLatitude = 0.0D;
                        return;
                    }

                    if (latitude > 0.0D && longitude > 0.0D) {
                        JLocationManager.this.mGpsInfo.setLocation(JLocationManager.this.currentBestLocation);
                        JLocationManager.mLongitude = longitude;
                        JLocationManager.mLatitude = latitude;
                    }
                }

                if (JLocationManager.this.mNetLocListener != null) {
                    JLocationManager.this.mLocationManager.removeUpdates(JLocationManager.this.mNetLocListener);
                    JLocationManager.this.mNetLocListener = null;
                }

            */
    }

    LocationCallback locationCallback = new LocationCallback() {
        private String TAG = "FUSED location";

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            String debuMsg = mName + "[Fused] onLocationResult. locations=" + locationList.toString();
//            Log.d(TAG, debuMsg);

            if (locationList.size() > 0) {
//                Location location = locationList.get(locationList.size() - 1);
                Location location = locationResult.getLastLocation();

                debuMsg = mName + "[Fused] onLocationResult, lat=" + location.getLatitude()
                        + ", lon=" + location.getLongitude()
                        + ", speed=" + location.getSpeed()
                        + ", bearing=" + location.getBearing();
                ;

                //EtransDrivingApp.getInstance().debugMessage(debuMsg);

                if(location!=null)
                    fusedLocationUpdated(location);
            } else {
                debuMsg = mName + "[Fused] onLocationResult, location not found";
                Log.w(TAG, debuMsg);
                EtransDrivingApp.getInstance().debugMessage(debuMsg);
            }
        }

        protected boolean isBetterLocation(Location location, Location currentBestLocation) {
            if (currentBestLocation == null) {
                return true;
            } else {
                long timeDelta = location.getTime() - currentBestLocation.getTime();
                boolean isSignificantlyNewer = timeDelta > 120000L;
                boolean isSignificantlyOlder = timeDelta < -120000L;
                boolean isNewer = timeDelta > 0L;
                if (isSignificantlyNewer) {
                    return true;
                } else if (isSignificantlyOlder) {
                    return false;
                } else {
                    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
                    boolean isLessAccurate = accuracyDelta > 0;
                    boolean isMoreAccurate = accuracyDelta < 0;
                    boolean isSignificantlyLessAccurate = accuracyDelta > 200;
                    boolean isFromSameProvider = this.isSameProvider(location.getProvider(), currentBestLocation.getProvider());
                    if (isMoreAccurate) {
                        return true;
                    } else if (isNewer && !isLessAccurate) {
                        return true;
                    } else {
                        return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
                    }
                }
            }
        }

        private boolean isSameProvider(String provider1, String provider2) {
            if (provider1 == null) {
                return provider2 == null;
            } else {
                return provider1.equals(provider2);
            }
        }

    };
}
