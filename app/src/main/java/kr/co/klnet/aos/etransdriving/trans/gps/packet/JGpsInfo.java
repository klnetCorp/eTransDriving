package kr.co.klnet.aos.etransdriving.trans.gps.packet;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.location.Location;
import java.util.ArrayList;
import java.util.List;

public class JGpsInfo {
    private static JGpsInfo mObject;
    private Object mQueueLock = new Object();
    private int mGpsStatus;
    private boolean mIsUpdate;
    private List<Location> mLocationArrayList = new ArrayList();

    public JGpsInfo() {
    }

    public static JGpsInfo getInst() {
        if (mObject == null) {
            mObject = new JGpsInfo();
        }

        return mObject;
    }

    public int getArrayListSize() {
        synchronized(this.mQueueLock) {
            return this.mLocationArrayList == null ? 0 : this.mLocationArrayList.size();
        }
    }

    public void setLocation(Location _location) {
        synchronized(this.mQueueLock) {
            if (_location != null) {
                int size = this.mLocationArrayList.size();
                if (size > 1) {
                    for(int i = 0; i < size - 1; ++i) {
                        this.mLocationArrayList.remove(0);
                    }
                }

                this.mLocationArrayList.add(_location);
            }

        }
    }

    public Location getLocation() {
        synchronized(this.mQueueLock) {
            Location location = null;
            if (this.mLocationArrayList != null) {
                int size = this.mLocationArrayList.size();
                if (size > 0) {
                    location = (Location)this.mLocationArrayList.get(0);
                    //tom.lee: why they remove the gps-array?
                    this.mLocationArrayList.remove(0);
                }
            }

            return location;
        }
    }

    public void removeLocation() {
        synchronized(this.mQueueLock) {
            int size = 0;

            if (this.mLocationArrayList != null) {
                size = this.mLocationArrayList.size();
                if (size > 0) {
                    this.mLocationArrayList.remove(0);
                }
            }

            size = this.mLocationArrayList.size();
        }
    }

    public double getLongitude() {
        synchronized(this.mQueueLock) {
            double longitude = 0.0D;
            if (this.mLocationArrayList != null) {
                int size = this.mLocationArrayList.size();
                if (size > 0) {
                    Location location = (Location)this.mLocationArrayList.get(0);
                    longitude = location.getLongitude();
                }
            }

            return longitude;
        }
    }

    public double getLatitude() {
        synchronized(this.mQueueLock) {
            double latitude = 0.0D;
            if (this.mLocationArrayList != null) {
                int size = this.mLocationArrayList.size();
                if (size > 0) {
                    Location location = (Location)this.mLocationArrayList.get(0);
                    latitude = location.getLatitude();
                }
            }

            return latitude;
        }
    }

    public double getAltitude() {
        synchronized(this.mQueueLock) {
            double altitude = 0.0D;
            if (this.mLocationArrayList != null) {
                int size = this.mLocationArrayList.size();
                if (size > 0) {
                    Location location = (Location)this.mLocationArrayList.get(0);
                    altitude = location.getAltitude();
                }
            }

            return altitude;
        }
    }

    public float getAccuracy() {
        synchronized(this.mQueueLock) {
            float accuracy = 0.0F;
            if (this.mLocationArrayList != null) {
                int size = this.mLocationArrayList.size();
                if (size > 0) {
                    Location location = (Location)this.mLocationArrayList.get(0);
                    accuracy = location.getAccuracy();
                }
            }

            return accuracy;
        }
    }

    public float getSpeed() {
        synchronized(this.mQueueLock) {
            float speed = 0.0F;
            if (this.mLocationArrayList != null) {
                int size = this.mLocationArrayList.size();
                if (size > 0) {
                    Location location = (Location)this.mLocationArrayList.get(0);
                    speed = location.getSpeed();
                }
            }

            return speed;
        }
    }

    public float getBearing() {
        synchronized(this.mQueueLock) {
            float bearing = 0.0F;
            if (this.mLocationArrayList != null) {
                int size = this.mLocationArrayList.size();
                if (size > 0) {
                    Location location = (Location)this.mLocationArrayList.get(0);
                    bearing = location.getBearing();
                }
            }

            return bearing;
        }
    }

    public String getProvider() {
        synchronized(this.mQueueLock) {
            String provider = "I";
            if (this.mLocationArrayList != null) {
                int size = this.mLocationArrayList.size();
                if (size > 0) {
                    Location location = (Location)this.mLocationArrayList.get(0);
                    provider = location.getProvider();
                }
            }

            return provider;
        }
    }

    public void setGpsStatus(int _gpsStatus) {
        synchronized(this.mQueueLock) {
            this.mGpsStatus = _gpsStatus;
        }
    }

    public int getGpsStatus() {
        synchronized(this.mQueueLock) {
            return this.mGpsStatus;
        }
    }

    public void setIsUpdate(boolean _isUpdate) {
        synchronized(this.mQueueLock) {
            this.mIsUpdate = _isUpdate;
        }
    }

    public boolean getIsUpdate() {
        synchronized(this.mQueueLock) {
            return this.mIsUpdate;
        }
    }
}
