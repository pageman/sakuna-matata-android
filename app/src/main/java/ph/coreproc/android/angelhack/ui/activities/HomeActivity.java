package ph.coreproc.android.angelhack.ui.activities;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;

import butterknife.Bind;
import butterknife.OnClick;
import ph.coreproc.android.angelhack.AngelHack;
import ph.coreproc.android.angelhack.R;
import ph.coreproc.android.angelhack.ui.dialogs.ChannelDialogFragment;
import ph.coreproc.android.angelhack.utils.SMSSender;
import ph.coreproc.android.angelhack.utils.SingleShotLocationProvider;
import ph.coreproc.android.angelhack.utils.UiUtil;

/**
 * Created by Kaelito on 8/8/15.
 */
public class HomeActivity extends BaseActivity {

    public static final String TAG = "HomeActivity";

    @Bind(R.id.brgyStNoEditText)
    EditText mBrgyStNoEditText;

    @Bind(R.id.messageEditText)
    EditText mMessageEditText;

    @Bind(R.id.sendButton)
    CircularProgressButton mSendButton;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;

    @Bind(R.id.channelImageView)
    ImageView mChannelImageView;

    @Bind(R.id.provinceEditText)
    EditText mProvinceEditText;

    @Bind(R.id.cityMunicipalityEditText)
    EditText mCityMunicipalityEditText;

    @Bind(R.id.latLongTextView)
    TextView latLongTextView;



    String mChannel;
    double mLatitude;
    double mLongitude;
    String mLocationString;
    String mMessage;



    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialize();
    }

    private void initialize() {
        removeCurrentFocus();
        mCollapsingToolbar.setTitle("#TYPHOON");
        mChannel = "TYPHOON";
        getLocation();
    }

    private void getLocation() {
        latLongTextView.setText("Getting your location...");
        mSendButton.setEnabled(false);

        if(!SingleShotLocationProvider.isGPSEnabled(mContext) &&
                !SingleShotLocationProvider.isNetworkEnabled(mContext)) {
            UiUtil.showMessageDialog(getSupportFragmentManager(), "Location is turned OFF",
                    "Please turn on your location and try again.");
            latLongTextView.setText("Get my Location");
        } else {
            SingleShotLocationProvider.requestSingleUpdate(mContext, new SingleShotLocationProvider.LocationCallback() {
                @Override
                public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                    mLatitude = location.latitude;
                    mLongitude = location.longitude;
                    latLongTextView.setText("Latitude: " + location.latitude + " Longitude: " + location.longitude);
                    mSendButton.setEnabled(true);
                }
            });
        }
    }

    @OnClick(R.id.latLongTextView)
    public void getLatLong() {
        if(latLongTextView.getText().toString().equals("Get my Location")) {
            getLocation();
        } else {

        }
    }

    @OnClick(R.id.sendButton)
    public void sendButton() {
        if (mSendButton.getProgress() == 0) {
            removeCurrentFocus();

            String errorMessage = "Please fill up the following: ";
            if(mProvinceEditText.getText().toString().trim().length() == 0) {
                errorMessage += "\n\t*" + "Province";
            }
            if(mCityMunicipalityEditText.getText().toString().trim().length() == 0) {
                errorMessage += "\n\t*" + "City/Municipality";
            }
            if(mBrgyStNoEditText.getText().toString().trim().length() == 0) {
                errorMessage += "\n\t*" + "Barangay/Street/No.";
            }
            if(mMessageEditText.getText().toString().trim().length() == 0) {
                errorMessage += "\n\t*" + "Message";
            }


            if(!errorMessage.equals("Please fill up the following: ")) {
                UiUtil.showMessageDialog(getSupportFragmentManager(), errorMessage);
                return;
            }

            mLocationString = mProvinceEditText.getText().toString() + ", " +
                    mCityMunicipalityEditText.getText().toString() + ", " +
                    mBrgyStNoEditText.getText().toString();
            mLocationString = mLocationString.trim();

            mMessage = mMessageEditText.getText().toString().trim();

            // send
            String message = mChannel + "&|!" +
                    "" + mLatitude + "&|!" +
                    "" + mLongitude + "&|!" +
                    "" + mLocationString + "&|!" +
                    "" + mMessage;

            if(message.length() > 160) {
                UiUtil.showMessageDialog(getSupportFragmentManager(), "The message constructed" +
                        " containing your location and message exceeded 160 characters. " +
                        "Please minimize the number of characters and try again.");
            } else {

                Log.i(TAG, "message to send: " + message);

                mSendButton.setIndeterminateProgressMode(true);
                mSendButton.setProgress(50);

                SMSSender smsSender = new SMSSender(mContext) {
                    @Override
                    public void onMessageSent() {
                        mSendButton.setProgress(100);
                    }

                    @Override
                    public void onMessageSendFailed(String error) {
                        mSendButton.setProgress(-1);
                        Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
                    }
                };
                smsSender.sendSMS(AngelHack.CHIKKA_SHORT_KEY, message);
            }

        } else if (mSendButton.getProgress() < 0) {
            // error
            mSendButton.setProgress(0);
        } else if (mSendButton.getProgress() == 100) {
            // completed
            mSendButton.setProgress(0);
        } else {

        }
    }

    @OnClick(R.id.changeChannelFAB)
    public void changeChannel() {
        ChannelDialogFragment channelDialogFragment = new ChannelDialogFragment() {
            @Override
            public void onChannelChanged(String channel) {
                mChannel = channel;
                mCollapsingToolbar.setTitle("#" + mChannel);
            }
        };
        channelDialogFragment.show(getSupportFragmentManager(), "ChannelDialogFragment");
    }

    private void removeCurrentFocus() {
        View current = getCurrentFocus();
        if (current != null) current.clearFocus();
        mSendButton.requestFocus();
    }

}
