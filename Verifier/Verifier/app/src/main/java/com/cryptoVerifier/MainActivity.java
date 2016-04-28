package com.cryptoVerifier;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ori.verifier.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import parametersMain.ParametersMain;

public class MainActivity extends AppCompatActivity {

    private Activity currentActivity = this;
    //private boolean pauseMailActionButton = false;
    private ImageButton button1, button2, button3, buttonInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        //initParametersForQRTest();
        // Drawable background = findViewById(R.id.main_layout).getBackground();
        //  background.setAlpha(75);
        //  initMailFloatinButton();
        initButtons();
        ParametersMain.main(null);
//        networkTesting();
//        AsyncTask initParametersTask = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protectetch (JSONException e) {
////                    Log.e("MainActivity", e.getMessage());
////                    e.printStackTrace();
////                }d Void doInBackground(Void... params) {
//                try {
////                    Parameters.init();
////                } ca
//                return null;
//            }
//        }.execute();
//
//
    }

//    //// TODO: 20/03/16 remove remove remove after tests!!!!!!!
//    private void initParametersForQRTest() {
//
//        BigInteger a = new BigInteger("-3");
//        BigInteger b = new BigInteger("5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16);
//        BigInteger p = new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951");
//        EllipticCurve curve = new EllipticCurve(a, b, p);
//        BigInteger gx = new BigInteger("6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296", 16);
//        BigInteger gy = new BigInteger("4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5", 16);
//        ECPoint g = new ECPoint(curve, gx, gy);
//        int integerSize = 32;
//        BigInteger order = new BigInteger("115792089210356248762697446949407573529996955224135760342422259061068512044369");
//        ECGroup group = new ECGroup(curve.toByteArray(), g.toByteArray(integerSize), integerSize * 2, order.toByteArray());
//
//        byte[] publicKey = group.getElement(new BigInteger("5").toByteArray()); // Just some example public key. Notice you will not know the
//        // exponent of the real publicKey.
//
//        ECClientCryptographyModule module = new ECClientCryptographyModule(group, group); // TODO change signGroup!!!!!!!
//
//        Parameters.ourGroup = group;
//        Parameters.cryptoClient = module;
//        Parameters.publicKey = publicKey;
//
//
//        Set<String> s1 = new HashSet<String>();
//        s1.add("BB");
//        s1.add("Bennet");
//        s1.add("Zehavush");
//        s1.add("Ben-Ari");
//        RaceProperties rc1 = new RaceProperties(s1, "prime minister", 1, false);
//    /*	Set<String> s2= new HashSet<String>();
//        s2.add("Emma");
//		s2.add("John");
//		s2.add("Michael");
//		s2.add("TIMMY");
//		RaceProperties rc2=new RaceProperties(s2, "minister", 2, false);
//		Set<String> s3= new HashSet<String>();
//		s3.add("Jason");
//		s3.add("Nathan");
//		s3.add("Ella");
//		s3.add("Roy");
//		RaceProperties rc3=new RaceProperties(s3, "bibi", 3, true);	*/
//        Parameters.racesProperties = new ArrayList<RaceProperties>();
//        Parameters.racesProperties.add(rc1);
//        //Parameters.racesProperties.add(rc2);
//        //	Parameters.racesProperties.add(rc3);
//        Parameters.timeStampLevel = 1;
//
//        Parameters.candidatesNames = new HashSet<String>();
//        for (RaceProperties race : Parameters.racesProperties) {
//            for (String name : race.getPossibleCandidates()) {
//                Parameters.candidatesNames.add(name);
//            }
//        }
//        Parameters.candidatesMap = Parameters.mapCandidates(Parameters.candidatesNames);
//    }

    /*private void initMailFloatinButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!pauseMailActionButton) {
                    pauseMailActionButton = true;
                    String str = "טענות/הערות/שאלות ? פנו אלינו במייל";
                    Snackbar.make(view, str, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    // Execute some code after 2 seconds have passed
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            Intent intentEmail = new Intent(Intent.ACTION_SEND);
                            intentEmail.setType("message/rfc822");
                            intentEmail.putExtra(Intent.EXTRA_SUBJECT, " בלה בלה נושא");
                            intentEmail.putExtra(Intent.EXTRA_TEXT, " בלה בלה טקסט");
                            intentEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{"ourEmail@gmail.com"});
                            startActivity(intentEmail);
                            pauseMailActionButton = false;
                        }
                    }, 2500);
                }
            }
        });
    }*/

    private void initButtons() {
        button1 = (ImageButton) findViewById(R.id.b_1);
        button2 = (ImageButton) findViewById(R.id.b_2);
        button3 = (ImageButton) findViewById(R.id.b_3);
        buttonInfo = (ImageButton) findViewById(R.id.b_info);

        button1.setOnClickListener(myOnClickListener);
        button2.setOnClickListener(myOnClickListener);
        button3.setOnClickListener(myOnClickListener);
        buttonInfo.setOnClickListener(myOnClickListener);


    }

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener myOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent myIntent;
            switch (v.getId()) {
                case R.id.b_1:
                    myIntent = new Intent(currentActivity, ActivityCompareQRS.class);
                    currentActivity.startActivity(myIntent);
                    break;

                case R.id.b_2:
                    myIntent = new Intent(currentActivity, ActivityVerifyVoteInBB.class);
                    currentActivity.startActivity(myIntent);

                    break;

                case R.id.b_3:

                    myIntent = new Intent(currentActivity, ActivityMixnet.class);
                    currentActivity.startActivity(myIntent);

                    break;

                case R.id.b_info:

                    showDialog();
                    break;

                default:
                    break;
            }
        }
    };


    private void networkTesting() {
//        Callback callback = new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.d("","");
//                        //TODO GUI notify network request fail
//                        //showFailureAlert(NETWORK_ERROR); //network failure
//                    }
//                });
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//                String s = response.body().string();
//
//            }
//        };
//
//        BulletinBoardApi.enqueueGetRequest(BulletinBoardApi.PUBLIC_KEY_URL + String.valueOf(5), callback);


//        };
//        BulletinBoardApi.sendComplaint(" bla bla bla i complain");
        BulletinBoardApi.enqueueGetRequest(BulletinBoardApi.PUBLIC_KEY_URL + 7, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("", "");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().string();
                Log.d("", "");

            }
        });
    }

    private void showDialog() {

        String titleString, messageString, buttonString;
        titleString = "מידע";
        messageString = "ברוכים הבאים לאפליקצית ה-Verifier.\nהאפליקציה נועדה לבדוק את אמינות רכיבי מערכת ההצבעות האלקטרוניות, והיא מציעה מספר בדיקות:\n\n1.בדיקת audit המאפשרת לוודא שתא ההצבעה מצפין את בחירתכם כראוי.\n\n2.בדיקה שהצבעתכם נקלטה במערכת ההצבעות.\n\n3.בדיקת אלגוריתם ה-Mixnet שנועד לערבל ולהצפין מחדש את ההצבעות שבמערכת.\n\nבכל אחד מהמסכים יופיעו לפניכם הנחיות לביצוע הבדיקה.\nכמו כן, עם סיום כל אחת מהבדיקות, המערכת תעדכן אתכם אם הבדיקה הצליחה או לא.\nבמקרה של כשלון בבדיקה, תישלח תלונה לועדת הבחירות המרכזית בחשד לחוסר אמינות של הרכיב הרלוונטי.";
        buttonString = "הבנתי";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(currentActivity.LAYOUT_INFLATER_SERVICE);
        //  View view = inflater.inflate(R.layout.viewname, null);
        //builder.setView(view);

        TextView title = new TextView(this);
        title.setText(titleString);
        title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        builder.setCustomTitle(title);
        builder.setMessage(messageString);
        builder.setPositiveButton(buttonString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
}
