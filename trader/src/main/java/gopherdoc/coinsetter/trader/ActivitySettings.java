package gopherdoc.coinsetter.trader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import gopherdoc.coinsetter.trader.Coinsetter.ObjAPI;
import gopherdoc.coinsetter.trader.security.KeyStore;
import gopherdoc.coinsetter.trader.security.KeyStoreJb43;
import gopherdoc.coinsetter.trader.security.KeyStoreKk;

@SuppressWarnings("WeakerAccess")
public class ActivitySettings extends Activity {
    private ListItemSettings username;
    private ListItemSettings password;
    private NumberPicker rnp;
    private NumberPicker dnp;
    private Integer idQR;
    private SecurePreferences mSecurePrefs;
    private static final boolean IS_JB43 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    private static final boolean IS_JB = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    private static final boolean IS_KK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    public static final String OLD_UNLOCK_ACTION = "android.credentials.UNLOCK";

    public static final String UNLOCK_ACTION = "com.android.credentials.UNLOCK";
    public static final String RESET_ACTION = "com.android.credentials.RESET";
    private KeyStore ks;
    private boolean validKeys;
    private SecretKey key;
    private boolean _doubleBackToExitPressedOnce;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        username = (ListItemSettings)findViewById(R.id.settings_username);
        username.setHead("Username");
        password = (ListItemSettings)findViewById(R.id.settings_password);
        password.setHead("Password");
        password.setSub("<Hidden>");
        rnp = (NumberPicker)findViewById(R.id.refreshPicker);
        rnp.setFocusableInTouchMode(true);
        final String[] secondValues = new String[12];
        for (int i=0; i< secondValues.length; i++){
            String number = Integer.toString(5+i*5);
            secondValues[i] = number.length() < 2 ? "0" + number: number;
        }
        rnp.setDisplayedValues(secondValues);
        rnp.setMinValue(0);
        rnp.setMaxValue(secondValues.length - 1);
        rnp.setWrapSelectorWheel(false);

        dnp = (NumberPicker) findViewById((R.id.depthRange));
        dnp.setFocusableInTouchMode(true);
        final String[] depthValues = new String[5];
        depthValues[0] = "10";
        depthValues[1] = "25";
        depthValues[2] = "50";
        depthValues[3] = "75";
        depthValues[4] = "100";
        dnp.setDisplayedValues(depthValues);
        dnp.setMinValue(0);
        dnp.setMaxValue(depthValues.length-1);
        dnp.setWrapSelectorWheel(false);
        validKeys = false;
        if (IS_KK){
            ks = KeyStoreKk.getInstance();
        } else if (IS_JB43){
            ks = KeyStoreJb43.getInstance();
        } else {
            ks = KeyStore.getInstance();
        }
        if (ks.state() == KeyStore.State.UNLOCKED) {
            byte[] keyBytes = ks.get("hashkey");
            boolean success;
            if (keyBytes != null) {
                key = new SecretKeySpec(keyBytes, "AES");
                success = true;
            } else {
                try {
                    KeyGenerator kgen = KeyGenerator.getInstance("AES");
                    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
                    byte[] keyStart = "startseed".getBytes();
                    sr.setSeed(keyStart);
                    kgen.init(128, sr);
                    key = kgen.generateKey();
                    success = ks.put("hashkey", key.getEncoded());
                } catch (NoSuchAlgorithmException ignored) {
                    success = false;
                    key = null;
                }
            }
            if (success && key != null) {
                mSecurePrefs = new SecurePreferences(getApplicationContext(),getString(R.string.preference_file_key), key.toString(),true);
                String primary = mSecurePrefs.getString(getString(R.string.prefusername));
                validKeys = ObjAPI.validateKeys(this, key);
                if (!validKeys) {
                    Toast.makeText(this, "Please check credentials", Toast.LENGTH_SHORT).show();
                }
                username.setSub(primary);
                username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final EditText input = new EditText(ActivitySettings.this);
                        input.setText(mSecurePrefs.getString(getString(R.string.prefusername)));
                        new AlertDialog.Builder(ActivitySettings.this)
                                .setTitle("Update Username")
                                .setMessage("Please insert username")
                                .setView(input)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Editable value = input.getText();
                                        assert value != null;
                                        mSecurePrefs.put(getString(R.string.prefusername), value.toString());
                                        username.setSub(value.toString());
                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                    }
                });
                password.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final EditText input = new EditText(ActivitySettings.this);
                        input.setText(mSecurePrefs.getString(getString(R.string.prefpassword)));
                        new AlertDialog.Builder(ActivitySettings.this)
                                .setTitle("Update password")
                                .setMessage("Please insert password")
                                .setView(input)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Editable value = input.getText();
                                        assert value != null;
                                        mSecurePrefs.put(getString(R.string.prefpassword), value.toString());
                                        password.setSub("<Hidden>");
                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                    }
                });
                String currentInterval = mSecurePrefs.getString("refreshInterval");
                int index = Arrays.asList(secondValues).indexOf(currentInterval);
                rnp.setValue(index);
                rnp.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        rnp.setValue((newVal < oldVal)?oldVal-1:oldVal+1);
                        mSecurePrefs.put("refreshInterval", secondValues[newVal]);
                    }
                });
                String currentDepth = mSecurePrefs.getString("depthRange");
                final int depthindex = Arrays.asList(depthValues).indexOf(currentDepth);
                dnp.setValue(depthindex);
                dnp.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        dnp.setValue((newVal < oldVal)?oldVal-1:oldVal+1);
                        mSecurePrefs.put("depthRange",depthValues[newVal]);
                    }
                });
            }
        } else {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    startActivity(new Intent(OLD_UNLOCK_ACTION));
                } else {
                    startActivity(new Intent(UNLOCK_ACTION));
                }
            } catch (ActivityNotFoundException e) {
                Log.e("Coinsetter", "No UNLOCK activity: " + e.getMessage(), e);
                Toast.makeText(this, "No keystore unlock activity found.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
    public void OnSettingsQRClick(View v){
        if (v.getId() == R.id.settings_btn_username){
            idQR = v.getId();
        } else if (v.getId() == R.id.settings_btn_password) {
            idQR = v.getId();
        }
        try{
            IntentIntegrator integrator = new IntentIntegrator(ActivitySettings.this);
            integrator.initiateScan();
        } catch (Exception ignored) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if ( scanResult != null){
            String value = scanResult.getContents();
            if (value != null && idQR != null) {
                if (idQR == R.id.settings_btn_username) {
                    mSecurePrefs.put(getString(R.string.prefusername), value);
                    username.setSub(value);
                } else if (idQR == R.id.settings_btn_password) {
                    mSecurePrefs.put(getString(R.string.prefpassword), value);
                    password.setSub(value);
                    password.hideSub();
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        validKeys = ObjAPI.validateKeys(this, key);
        if (_doubleBackToExitPressedOnce) {
            super.onBackPressed();
            if (!validKeys) {
                finish();
                return;
            } else {
                startActivity(new Intent(this, MainActivity.class));
                return;
            }
        }
        this._doubleBackToExitPressedOnce = true;
        if (!validKeys) {
            Toast.makeText(this, "Credentials are invalid. Please enter credentials or press back again to quit", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Press again to return to trading", Toast.LENGTH_SHORT).show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                _doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}