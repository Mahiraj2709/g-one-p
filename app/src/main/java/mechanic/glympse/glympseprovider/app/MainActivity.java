package mechanic.glympse.glympseprovider.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import mechanic.glympse.glympseprovider.GlympseApplication;
import mechanic.glympse.glympseprovider.R;
import mechanic.glympse.glympseprovider.data.DataManager;
import mechanic.glympse.glympseprovider.data.local.PrefsHelper;
import mechanic.glympse.glympseprovider.fragment.HomeFragment;
import mechanic.glympse.glympseprovider.fragment.ServiceHistoryFragment;
import mechanic.glympse.glympseprovider.interfaces.AvailabilityCallback;
import mechanic.glympse.glympseprovider.utils.ApplicationMetadata;
import mechanic.glympse.glympseprovider.utils.CommonMethods;
import mechanic.glympse.glympseprovider.utils.DateUtil;
import mechanic.glympse.glympseprovider.utils.DialogFactory;
import mechanic.glympse.glympseprovider.utils.ViewUtil;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private CircleImageView image_profile;
    private TextView tv_userName;
    private int mStackLevel = 1;
    DataManager dataManager = null;
    private PrefsHelper prefsHelper = null;
    private Map<String, String> requestParams = new HashMap<>();
    private Map<String, String> requestMap = new HashMap<>();
    private AvailabilityCallback availabilityCallback = null;
    private ImageView[] stars = new ImageView[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefsHelper = new PrefsHelper(this);
        dataManager = new DataManager(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tv_toolbarHeader = (TextView) findViewById(R.id.tv_toolbarHeader);
        tv_toolbarHeader.setText(getString(R.string.title_home));

        //if user click notifiction then hide nofitication
        CommonMethods.cancelNotification(this, 101);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        tv_userName = (TextView) header.findViewById(R.id.tv_userName);
        image_profile = (CircleImageView) header.findViewById(R.id.image_profile);

        stars[0] = (ImageView) header.findViewById(R.id.iv_star_one);
        stars[1] = (ImageView) header.findViewById(R.id.iv_star_two);
        stars[2] = (ImageView) header.findViewById(R.id.iv_star_three);
        stars[3] = (ImageView) header.findViewById(R.id.iv_star_four);
        stars[4] = (ImageView) header.findViewById(R.id.iv_star_five);

        //load initial data from shared prefereces
        loadData();
        //make mechanic available
        setMechAvailable();

        //update provider location every 5 minutes
        upldateLocationEveryFiveMinute();

        if (savedInstanceState == null) {
            Fragment newFragment = HomeFragment.newInstance(mStackLevel);
//            Fragment newFragment = IHaveArrivedFragment.newInstance(mStackLevel,new Customer());
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fl_container, newFragment, "map_fragment").commit();
        } else {
            mStackLevel = savedInstanceState.getInt("level");
        }
    }

    private void setMechAvailable() {
        requestParams.clear();
        requestParams.put(ApplicationMetadata.SESSION_TOKEN, prefsHelper.getPref(ApplicationMetadata.SESSION_TOKEN, ""));
        requestParams.put(ApplicationMetadata.LANGUAGE, prefsHelper.getPref(ApplicationMetadata.APP_LANGUAGE, ""));
        requestParams.put(ApplicationMetadata.APP_STATUS, ApplicationMetadata.AVAILABLE);

        dataManager.setCallback(new DataManager.RequestCallback() {
            @Override
            public void Data(Object data) {
                GlympseApplication.isAvailable = true;
                if (availabilityCallback != null) {
                    availabilityCallback.changeAvailability();
                }
            }
        });
        dataManager.changeAvailability(requestParams);
    }

    public void loadData() {
        //set name
        tv_userName.setText(prefsHelper.getPref(ApplicationMetadata.USER_NAME, "NO NAME"));
        //set stars

        int starCount = Integer.parseInt(prefsHelper.getPref(ApplicationMetadata.AVG_RATING, "0"));
        for (int i = 0; i < starCount; i++) {
            stars[i].setImageResource(R.drawable.ic_star_yellow);
        }

        Glide.with(this)
                .load(ApplicationMetadata.IMAGE_BASE_URL + prefsHelper.getPref(ApplicationMetadata.USER_IMAGE))
                .thumbnail(0.2f)
                .error(R.drawable.ic_profile_photo)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(image_profile);
    }

    private Fragment lastFragment = null;

    @SuppressWarnings("ResourceType")
    public void addFragmentToStack(Fragment fragment, String tag) {
        //hide soft keyboard if visible
        ViewUtil.hideKeyboard(this);
        mStackLevel++;
        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (lastFragment != null)
            transaction.remove(lastFragment);
        transaction.replace(R.id.fl_container, fragment);
        transaction.addToBackStack(tag);
        transaction.commit();

        lastFragment = fragment;
    }

    @Override
    public void onBackPressed() {
        ((TextView) findViewById(R.id.tv_toolbarHeader)).setText(getString(R.string.title_home));
        Log.i("back_pressed", getSupportFragmentManager().getBackStackEntryCount() + "");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                DialogFactory.createExitDialog(this);
            } else {
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
                    for (int i = 0; i < backStackCount; i++) {
                        getSupportFragmentManager().popBackStack(getSupportFragmentManager().getBackStackEntryAt(i).getId(), getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
                    }
                } else
                    super.onBackPressed();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {

            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
                for (int i = 0; i < backStackCount; i++) {
                    getSupportFragmentManager().popBackStack(getSupportFragmentManager().getBackStackEntryAt(i).getId(), getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
                }
            }

        } else if (id == R.id.nav_myProfile) {
            PrefsHelper prefsHelper = new PrefsHelper(this);
            Map<String, String> requestParams = new HashMap<>();
            requestParams.put(ApplicationMetadata.SESSION_TOKEN, prefsHelper.getPref(ApplicationMetadata.SESSION_TOKEN, ""));
            DataManager dataManager = new DataManager(this);
            dataManager.getProfile(requestParams);
        } else if (id == R.id.nav_history) {
            Fragment newFragment = ServiceHistoryFragment.newInstance(3);
            addFragmentToStack(newFragment,"service_history");
        } else if (id == R.id.nav_AboutUs) {
            //DialogFactory.createComingSoonDialog(this);
            PrefsHelper prefsHelper = new PrefsHelper(this);
            Map<String, String> requestParams = new HashMap<>();
            requestParams.put(ApplicationMetadata.PAGE_IDENTIFIER, ApplicationMetadata.ABOUT_MECH);

            DataManager dataManager = new DataManager(this);
            dataManager.getStaticPages(requestParams, ApplicationMetadata.ABOUT_MECH);
            //DialogFactory.createComingSoonDialog(this);
            /*Fragment newFragment = ResetPasswordFragment.newInstance(4);
            addFragmentToStack(newFragment, "reset_passord");*/
        } else if (id == R.id.nav_help) {
            //DialogFactory.createComingSoonDialog(this);
            /*PrefsHelper prefsHelper = new PrefsHelper(this);
            Map<String, String> requestParams = new HashMap<>();
            requestParams.put(ApplicationMetadata.PAGE_IDENTIFIER, ApplicationMetadata.ABOUT_CUSTOMER);
            requestParams.put(ApplicationMetadata.LANGUAGE, prefsHelper.getPref(ApplicationMetadata.APP_LANGUAGE, ""));

            DataManager dataManager = new DataManager(this);
            dataManager.getStaticPages(requestParams, ApplicationMetadata.ABOUT_CUSTOMER);*/
        }else if (id == R.id.nav_logout) {
            DialogFactory.createLogoutDialog(this, R.string.logout, R.string.logout_confirm).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //make mechanic unavailable
        requestParams.clear();
        requestParams.put(ApplicationMetadata.SESSION_TOKEN, prefsHelper.getPref(ApplicationMetadata.SESSION_TOKEN, ""));
        requestParams.put(ApplicationMetadata.LANGUAGE, prefsHelper.getPref(ApplicationMetadata.APP_LANGUAGE, ""));
        requestParams.put(ApplicationMetadata.APP_STATUS, ApplicationMetadata.NOT_AVAILABLE);

        dataManager.setCallback(new DataManager.RequestCallback() {
            @Override
            public void Data(Object data) {
                GlympseApplication.isAvailable = false;
            }
        });
        dataManager.changeAvailability(requestParams);
    }

    public void setOnAvailabilityChangeListener(AvailabilityCallback listener) {
        this.availabilityCallback = listener;
    }

    private void upldateLocationEveryFiveMinute() {

        requestMap.put("pub_chn", "na");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //call update location every 5 minutes
                if (MainActivity.super.currentLocatoin != null) {
                    requestMap.put("lat", MainActivity.super.currentLocatoin.getLatitude() + "");
                    requestMap.put("long", MainActivity.super.currentLocatoin.getLongitude() + "");
                    dataManager.updateLatLong(requestMap);
                }
            }
        }, 0, 5 * 60 * 1000);
    }
}
