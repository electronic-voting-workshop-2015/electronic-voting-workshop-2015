package com.cryptoVerifier;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ori.verifier.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ActivityVerifyVoteInBB extends QRScanningActivity {


    boolean DEBUG = false;
    private Activity currentActivity = this;
    private ImageView imageView;

    private CheckBox dontShowAgain;

    private boolean BUTTONVALUE = false;
    private static int SCAN_FAILED = 0;
    private static int WRONG_BARCODE_SCANNED = 1;
    private static int NETWORK_ERROR = 2;
    private static final int FAKE_CERTIFICATE = 3;
    private static final int CERTIFICATE_VERIFICATION_FAILED = 4;
    private static final int QR_WRONG_LENGTH = 6;
    private static final int NETWORK_CERTIFICATE_ERROR = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifi_vote_in_bb);

        showInformationAlert();
        initImageView();
        initButtons();
    }


    private void initButtons() {
        ImageButton requestScanButton = (ImageButton) findViewById(R.id.b_1);

        requestScanButton.setOnClickListener(myOnClickListener);

        Button debugButton = (Button) findViewById(R.id.buttonDEBUG);
        debugButton.setVisibility(View.VISIBLE);
        debugButton.setBackgroundColor(Color.TRANSPARENT);
        debugButton.setOnClickListener(myOnClickListener);
    }

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener myOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.b_1:
                    buttonPressed();

                    break;

                case R.id.buttonDEBUG:
                    //BUTTONVALUE = !BUTTONVALUE;
                    break;
                default:
                    break;
            }
        }
    };

    private void initImageView() {
        imageView = (ImageView) findViewById(R.id.imageView1);
    }

    private void buttonPressed() {
        imageView.setBackgroundResource(0);
        requestQRString();

    }


    @Override
    protected void onQrCodeReceived(final String qrString) {

        if (qrString == null) {//do whatever you want with the code
            showFailureAlert(SCAN_FAILED);//TODO GUI - bad qr String
            return;
        }
        try {
            // Toast.makeText(currentActivity, "אנא המתן בעת ביצוע הבדיקה", Toast.LENGTH_LONG).show();

            //Toast.makeText(this, qrString, Toast.LENGTH_SHORT).show();

            Callback bbVoteCallBack = new Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    showFailureAlert(NETWORK_ERROR);//TODO GUI - network request failed

                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    if (response.code() != 200)
                        showFailureAlert(NETWORK_ERROR);
                    else {

                        String returnedString = response.body().string();
                        if (BUTTONVALUE) {
                            updateUi(true);
                        } else {
                            MainQR mainQR = new MainQR(qrString);
                            if (mainQR.getErrorCode() != ErrorCode.NO_ERROR) {
                                Log.d("error", "error");
                                if (mainQR.getErrorCode() == ErrorCode.INVALID_QR_LENGTH) {
                                    showFailureAlert(QR_WRONG_LENGTH);
                                    return;
                                }
                                showFailureAlert(WRONG_BARCODE_SCANNED);
                                return;
                            }

                            // String qrPart = returnedString.substring(returnedString.indexOf("qr="))
                            if (verifyVoteFromJsonAccurate(returnedString, mainQR))
                                //if (returnedString != null && returnedString.contains("\"qr\":\"" + voteValue + "\""))
                                updateUi(true);//TODO GUI success
                            else
                                updateUi(false); //TODO GUI error: vote not in gui


                            verifyVoteCertificate(mainQR);
                        }
                    }


//
//                    BBVotesContainer bbVotesContainer = new BBJsonDeserializer<>(BBVotesContainer.class).deserialize(response.body().string());
//                    if (bbVotesContainer != null) {
//                        if (bbVotesContainer.veryfiyVoteValueExists(voteValue))
//                            updateUi(true);//TODO GUI success
//                        else {
//                            updateUi(false); //TODO GUI error: vote not in gui
//                        }
//                    } else {
//                        //TODO notify user a connection error occurred (or a parsing error)
//                        showFailureAlert(NETWORK_ERROR);
//                    }
                }
            };

            BulletinBoardApi.enqueueGetRequest(BulletinBoardApi.BB_VOTES_URL, bbVoteCallBack);
        } catch (NumberFormatException nfe) {
            //TODO GUI - bad qr String .  do this means parsing error?
            showFailureAlert(WRONG_BARCODE_SCANNED);
            return;
        }


    }

    private void verifyVoteCertificate(final MainQR mainQR) {

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //TODO GUI notify network request fail
                        showFailureAlert(NETWORK_CERTIFICATE_ERROR); //network failure
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                mainQR.verifyCertificate(response.body().string());
                if (mainQR.getErrorCode() != ErrorCode.NO_ERROR) {
                    if (mainQR.getErrorCode() == ErrorCode.CERTIFICATE_INVALID_ERROR) {
                        showFailureAlert(FAKE_CERTIFICATE);
                        return;
                    } else {
                        showFailureAlert(CERTIFICATE_VERIFICATION_FAILED);
                        return;
                    }
                }
            }
        };

        BulletinBoardApi.enqueueGetRequest(BulletinBoardApi.PUBLIC_KEY_URL + String.valueOf(mainQR.getPartyId()), callback);
    }

    /*
     true: vote in bb
     false: vote not in bb
     */
    private void updateUi(final boolean succeeded) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setBackgroundResource(succeeded ? R.drawable.verifi_vote_in_bb_success : R.drawable.verifi_vote_in_bb_failure);
            }
        });
    }


    /*
         0: nothing scanned
         1: parsing error
         2: network failure
      */
    private void showFailureAlert(int error) {

        System.out.println("error code: " + error);

        String titleStringTmp, messageStringTmp = "", buttonStringTmp;
        titleStringTmp = "שגיאה";
        buttonStringTmp = "הבנתי";

        if (error == SCAN_FAILED) {
            messageStringTmp = "לא התבצעה סריקה,\nאנא נסה שנית.";
        } else if (error == WRONG_BARCODE_SCANNED) {
            messageStringTmp = "לא ניתן לפענח את הברקוד.\nאנא ודא כי מדובר בפתק הצבעה רשמי";
        } else if (error == NETWORK_ERROR) {
            messageStringTmp = "אנו נתקלים בתקלות תקשורת,\nאנא נסה שנית מאוחר יותר";
        } else if (error == FAKE_CERTIFICATE) {
            messageStringTmp = "מבדיקת המערכת עולה שפתק ההצבעה שבידיכם אינו חתום על ידי תא ההצבעה.";
        } else if (error == CERTIFICATE_VERIFICATION_FAILED) {
            messageStringTmp = "קרתה שגיאה בלתי צפויה במהלך ניסיון האימות של חתימת תא ההצבעה.\nאנא נסו שוב מאוחר יותר, עמכם הסליחה.";
        } else if (error == QR_WRONG_LENGTH) {
            messageStringTmp = "הברקוד שבידיך אינו באורך המתאים.\nאנא ודא כי מדובר בפתק הצבעה רשמי";
        } else if (error == NETWORK_CERTIFICATE_ERROR) {
            messageStringTmp = "אנו נתקלים בתקלות תקשרות. לא ניתן לוודא את החתימה.\nאנא נסה שנית מאוחר יותרי";
        }


        final String titleString = titleStringTmp, messageString = messageStringTmp, buttonString = buttonStringTmp;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                showAlertDialog(titleString, messageString, buttonString);
            }
        });


    }

    private void showAlertDialog(String titleString, String messageString, String buttonString) {

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


    private void showInformationAlert() {

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean showAgain = prefs.getBoolean("showAgainVerify", true);
        if (!showAgain) return;


        String titleString, messageString, buttonString;
        titleString = "הנחיות לוידוא קליטת הצבעת הבוחר במערכת";
        messageString = "אנא ודאו שפתק ההצבעה שבידיכם מכיל ברקוד אחד.\nלאחר מכן, סרקו את הברקוד המופיע בפתק ההצבעה.\nלאחר סריקת הברקוד, המערכת תוודא שהצבעתכם נקלטה כשורה.";
        buttonString = "הבנתי";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(currentActivity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.checkbox, null);
        dontShowAgain = (CheckBox) view.findViewById(R.id.skip);
        builder.setView(view);

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
                if (dontShowAgain.isChecked()) {
                    SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                    editor.putBoolean("showAgainVerify", false);
                    editor.apply();
                }
            }
        });
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.show();
    }


    private boolean verifyVoteFromJsonAccurate(String jsonString, MainQR mainQR) {

        byte[] realQR = QR_ParsingUtils.getByteArrFromString(mainQR.getRawScan());
        List<String> votes = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                String curQR = ((JSONObject) jsonArray.get(i)).getString("qr");
                try {
                    byte[] qrFromBB = Base64.decode(curQR, Base64.DEFAULT);
                    if (QR_ParsingUtils.isSameArray(realQR, qrFromBB)) {
                        Log.d("qr was found", curQR);
                        votes.add(((JSONObject) jsonArray.get(i)).getString("vote_value"));
                    }
                } catch (IllegalArgumentException ignored) {
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

//        if (mainQR.getErrorCode() != ErrorCode.NO_ERROR)
//            return false;
        if (mainQR.getEncryptions().size() != votes.size())
            return false;

        for (int i = 0; i < mainQR.getEncryptions().size(); i++) {
            boolean foundVote = false;
            byte[] curVote = concat(mainQR.getEncryptions().get(i).first, mainQR.getEncryptions().get(i).second);
            for (int j = 0; j < votes.size(); j++) {
                byte[] bbVote = Base64.decode(votes.get(j), Base64.DEFAULT);
                if (QR_ParsingUtils.isSameArray(curVote, bbVote))
                    foundVote = true;
            }
            if (!foundVote)
                return false;
        }

        return true;
    }

    public byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

}


