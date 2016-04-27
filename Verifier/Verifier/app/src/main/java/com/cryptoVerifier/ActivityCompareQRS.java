package com.cryptoVerifier;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ori.verifier.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import parametersMain.ParametersMain;
import workshop.Group;

public class ActivityCompareQRS extends QRScanningActivity {

    private boolean DEBUGYemini = false;
    private boolean SHOWRESULTDEBUG = false;
    private int DEBUGCounter = 0;
    private Activity currentActivity = this;
    //private Button button1;
    private TextView textView;
    private ImageButton requestScanButtonMain, requestScanButtonAudit;
    private boolean scannedMainQrsuccessfully = false;

    private static final int SCAN_FAILED = 0;
    private static final int WRONG_BARCODE_SCANNED = 1;
    private static final int NETWORK_ERROR = 2;
    private static final int FAKE_CERTIFICATE = 3;
    private static final int CERTIFICATE_VERIFICATION_FAILED = 4;
    private static final int NO_MATCH = 5;
    private static final int QR_WRONG_LENGTH = 6;

    private CheckBox dontShowAgain;

    private MainQR mainQR;
    private AuditQR auditQR;


    List<String> groupList;
    List<String> childList;
    Map<String, List<String>> votesCollection;
    ExpandableListView expListView;

    ArrayList<String> ourRacesNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_qrs);

        showInformationAlert();

        initButtons();
        initTextViews();
        putEmptyTexts();
        initExpandableListView();
        hideList();

    }


    private void initButtons() {
        requestScanButtonMain = (ImageButton) findViewById(R.id.b_1);
        requestScanButtonAudit = (ImageButton) findViewById(R.id.b_2);
        requestScanButtonAudit.setEnabled(false);
        requestScanButtonAudit.setVisibility(View.GONE);
        requestScanButtonMain.setOnClickListener(myOnClickListener);
        requestScanButtonAudit.setOnClickListener(myOnClickListener);
    }

    private void initTextViews() {
        textView = (TextView) findViewById(R.id.textView);
    }

    private void putEmptyTexts() {
        textView.setText("");
    }

    private void initExpandableListView() {
        expListView = (ExpandableListView) findViewById(R.id.expandable_list);
    }

    private void hideList() {
        expListView.setVisibility(ExpandableListView.GONE);
        textView.setText("");
    }

    private void showList() {
        expListView.setVisibility(ExpandableListView.VISIBLE);
    }

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener myOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.b_1:

                    if (SHOWRESULTDEBUG) {
                        showResultAndResetValues();
                        break;
                    }
                    putEmptyTexts();
                    hideList();
                    requestQRString();
                    break;

                case R.id.b_2:
                    putEmptyTexts();
                    hideList();
                    requestQRString();
                default:
                    break;
            }
        }
    };


    @Override
    protected void onQrCodeReceived(final String qrString) {

        if (DEBUGYemini) {
            debugStuff();
            return;
        }

        if (qrString == null) {
            showFailureAlert(SCAN_FAILED); //couldnot scan
            return;
        }

        if (!scannedMainQrsuccessfully) { //the first stage: no barcode has been scanned
            mainQR = new MainQR(qrString);
            if (mainQR.getErrorCode() != ErrorCode.NO_ERROR) {
                if (mainQR.getErrorCode() == ErrorCode.INVALID_QR_LENGTH) {
                    showFailureAlert(QR_WRONG_LENGTH);
                } else {
                    showFailureAlert(WRONG_BARCODE_SCANNED);
                }
                return;
            }

            Callback callback = new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //TODO GUI notify network request fail
                            showFailureAlert(NETWORK_ERROR); //network failure
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (mainQR.getErrorCode() == ErrorCode.NO_ERROR) {
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
                    } else {
                        //TODO GUI
                        System.out.println("error: stopped at level: " + mainQR.level);
                        showFailureAlert(WRONG_BARCODE_SCANNED); //parsing error
                        return;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedMainQrsuccessfully = true;
                            requestScanButtonMain.setEnabled(false);
                            requestScanButtonMain.setImageResource(R.drawable.scan_main_qr_finished);
                            requestScanButtonAudit.setEnabled(true);
                            requestScanButtonAudit.setImageResource(R.drawable.scan_audit_qr);
                            requestScanButtonAudit.setVisibility(View.VISIBLE);
                        }
                    });


                }
            };

            BulletinBoardApi.enqueueGetRequest(BulletinBoardApi.PUBLIC_KEY_URL + String.valueOf(mainQR.getPartyId()), callback);

        } else {
            //we are here so the result is for the audit qr
            Group group = ParametersMain.ourGroup;
            auditQR = new AuditQR(qrString, group);

            if (auditQR.getErrorCode() != ErrorCode.NO_ERROR) {
                if (auditQR.getErrorCode() == ErrorCode.INVALID_QR_LENGTH) {
                    showFailureAlert(QR_WRONG_LENGTH);
                } else {
                    showFailureAlert(WRONG_BARCODE_SCANNED); //parsing error
                }
                return;
            }
            showResultAndResetValues();
        }


    }

    private void showResultAndResetValues() {

        scannedMainQrsuccessfully = false;
        requestScanButtonMain.setEnabled(true);
        requestScanButtonMain.setImageResource(R.drawable.scan_main_qr);
        requestScanButtonAudit.setEnabled(false);
        requestScanButtonAudit.setImageResource(0);
        requestScanButtonAudit.setVisibility(View.GONE);
        ArrayList<ArrayList<String>> racesList;

        if (SHOWRESULTDEBUG) {
            racesList = createListForResultDebug();
            enableListViewWithActualList(racesList);
            showList();
            textView.setText("זוהו הבחירות הבאות:");

        } else {
            boolean result = auditQR.compareToMainQR(mainQR);

            if (result) {
                ourRacesNames = auditQR.racesNames;

                textView.setText("זוהו הבחירות הבאות:");
                racesList = auditQR.getChosenCandidatesByRace();
                if (racesList == null) {
                    showFailureAlert(WRONG_BARCODE_SCANNED);
                    return;
                }
                enableListViewWithActualList(racesList);
                showList();
            } else {
                showFailureAlert(NO_MATCH);
            }
        }
    }


    private ArrayList<ArrayList<String>> createListForResultDebug() {

        ArrayList<ArrayList<String>> racesList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ArrayList<String> candidates = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                String s = "מאור";
                candidates.add(s + j);
            }
            racesList.add(candidates);
        }

        return racesList;
    }

    private void enableListViewWithActualList(ArrayList<ArrayList<String>> racesList) {

        createSelections(racesList);

        final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(
                this, groupList, votesCollection);
        expListView.setAdapter(expListAdapter);

        setGroupIndicatorToRight();

        expListView.setOnChildClickListener(new OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                final String selected = (String) expListAdapter.getChild(
                        groupPosition, childPosition);
                Toast.makeText(getBaseContext(), selected, Toast.LENGTH_LONG)
                        .show();

                return true;
            }
        });

    }

    private void showInformationAlert() {

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean showAgain = prefs.getBoolean("showAgainCompareQRS", true);
        if (!showAgain) return;


        String titleString, messageString, buttonString;
        titleString = "הנחיות לבדיקת אמינות תא ההצבעה";
        messageString = "אנא ודאו שפתק ההצבעה שבידיכם מכיל שני ברקודים.\nלאחר מכן, סרקו את הברקוד העליון, ואחריו את הברקוד התחתון.\nהמערכת תוודא שהברקוד העליון מכיל את ההצפנה של הבחירה שמיוצגת בברקוד התחתון.\nבמידה ותימצא התאמה בין הברקודים, המערכת תדפיס לכם את הבחירה שפתק ההצבעה שבידיכם מייצג.";
        buttonString = "הבנתי";

        //showAllertDialog(titleString, messageString, buttonString);

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
                    editor.putBoolean("showAgainCompareQRS", false);
                    editor.apply();
                }
            }
        });
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.show();


    }

    /*
        0: nothing scanned
        1: parsing error
        2: network failure
        3: certificate fauilre
        5: everything worked but no match
     */
    private void showFailureAlert(int error) {

        System.out.println("error code: " + error);

        String titleStringTmp = "", messageStringTmp = "", buttonStringTmp = "";
        titleStringTmp = "שגיאה";
        buttonStringTmp = "הבנתי";

        if (error == SCAN_FAILED) {
            messageStringTmp = "לא התבצעה סריקה,\nאנא נסה שנית.";
        } else if (error == WRONG_BARCODE_SCANNED) {
            if (!scannedMainQrsuccessfully) {
                messageStringTmp = "לא ניתן לפענח את הברקוד.\nאנא ודא כי מבוצעת סריקה של הברקוד הנמצא בחלקו העליון של הפתק ונסה שנית.";
            } else {
                messageStringTmp = "לא ניתן לפענח את הברקוד.\nאנא ודא כי מבוצעת סריקה של הברקוד הנמצא בחלקו התחתון של הפתק ונסה שנית.";
            }
        } else if (error == NETWORK_ERROR) {
            messageStringTmp = "אנו נתקלים בתקלות תקשורת,\nאנא נסה שנית מאוחר יותר";
        } else if (error == FAKE_CERTIFICATE) {
            messageStringTmp = "מבדיקת המערכת עולה שפתק ההצבעה שבידיכם אינו חתום על ידי תא ההצבעה.\nאין אפשרות להמשיך בבדיקה.";
        } else if (error == CERTIFICATE_VERIFICATION_FAILED) {
            messageStringTmp = "קרתה שגיאה בלתי צפויה במהלך ניסיון האימות של חתימת תא ההצבעה.\nאנא נסו שוב מאוחר יותר, עמכם הסליחה.";
        } else if (error == NO_MATCH) {
            messageStringTmp = "זיהינו חוסר התאמה בין הברקודים, יש לפנות עם הפתק לגורמים המוסמכים";
        } else if (error == QR_WRONG_LENGTH) {
            messageStringTmp = "הברקוד שבידיך אינו באורך המתאים.\nאנא ודא כי מבוצעת סריקה של הברקוד הנכון ונסה שנית.";
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


    private void debugStuff() {

        if (DEBUGCounter == 0) {
            //showFailedScanningAlert(4);
            DEBUGCounter = 1;
        } else if (DEBUGCounter == 1) {
            scannedMainQrsuccessfully = true;
            requestScanButtonMain.setEnabled(false);
            requestScanButtonMain.setImageResource(R.drawable.scan_main_qr_finished);
            requestScanButtonAudit.setEnabled(true);
            requestScanButtonAudit.setImageResource(R.drawable.scan_audit_qr);
            DEBUGCounter = 2;
        } else if (DEBUGCounter == 2) {
            scannedMainQrsuccessfully = false;
            requestScanButtonMain.setEnabled(true);
            requestScanButtonMain.setImageResource(R.drawable.scan_main_qr);
            requestScanButtonAudit.setEnabled(false);
            requestScanButtonAudit.setImageResource(0);


            //  textView1.setText("נמצאה התאמה בין הברקודים!");
            // textView2.setText("זוהתה הבחירה הבאה:");
            //textView3.setText("ליכוד");

            DEBUGCounter = 0;
        }
    }


    private void createSelections(ArrayList<ArrayList<String>> racesList) {

        createGroupList(racesList.size());
        createCollection(racesList);
    }

    private void createGroupList(int num) {
        //groupList = new ArrayList<>();
        groupList = ourRacesNames;
        /*
        String s = "מירוץ מספר ";
        for (int i = 0; i < num; i++) {
            groupList.add(s + (i + 1));
        }
        */
    }

    private void createCollection(ArrayList<ArrayList<String>> raceList) {

        votesCollection = new LinkedHashMap<String, List<String>>();
        for (int i = 0; i < raceList.size(); i++) {
            loadChild(raceList.get(i));
            votesCollection.put(groupList.get(i), childList);
        }
        /*// preparing laptops collection(child)
        String[] hpModels = { "HP Pavilion G6-2014TX", "ProBook HP 4540",
                "HP Envy 4-1025TX" };
        String[] hclModels = { "HCL S2101", "HCL L2102", "HCL V2002" };
        String[] lenovoModels = { "IdeaPad Z Series", "Essential G Series",
                "ThinkPad X Series", "Ideapad Z Series" };
        String[] sonyModels = { "VAIO E Series", "VAIO Z Series",
                "VAIO S Series", "VAIO YB Series" };
        String[] dellModels = { "Inspiron", "Vostro", "XPS" };
        String[] samsungModels = { "NP Series", "Series 5", "SF Series" };

        laptopCollection = new LinkedHashMap<String, List<String>>();

        for (String laptop : groupList) {
            if (laptop.equals("HP")) {
                loadChild(hpModels);
            } else if (laptop.equals("Dell"))
                loadChild(dellModels);
            else if (laptop.equals("Sony"))
                loadChild(sonyModels);
            else if (laptop.equals("HCL"))
                loadChild(hclModels);
            else if (laptop.equals("Samsung"))
                loadChild(samsungModels);
            else
                loadChild(lenovoModels);

            laptopCollection.put(laptop, childList);
        }
        */
    }

    private void loadChild(ArrayList<String> candidates) {
        childList = new ArrayList<String>();
        for (String cand : candidates)
            childList.add(cand);
    }

    private void setGroupIndicatorToRight() {
        /* Get the screen width */
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;

        expListView.setIndicatorBounds(width - getDipsFromPixel(35), width
                - getDipsFromPixel(5));
    }

    // Convert pixel to dip
    public int getDipsFromPixel(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }


}


