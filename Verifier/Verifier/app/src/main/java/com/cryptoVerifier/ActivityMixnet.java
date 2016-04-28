package com.cryptoVerifier;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cryptoVerifier.MixnetVerifierUtils.MixnetProofs;
import com.example.ori.verifier.R;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import parametersMain.ParametersMain;
import workshop.Group;


public class ActivityMixnet extends AppCompatActivity {


    boolean DEBUG_useLocalMixnetFile = true;

    private Activity currentActivity = this;
    private ImageButton theButton;

    private SeekBar percentageBar = null;
    private TextView percentageText = null;
    private int percentage = 10;

    private static int UNEXPECTED_ERROR = 1;
    private static int NETWORK_ERROR = 2;

    private CheckBox dontShowAgain;


    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixnet);

        showInformationAlert();


        initButtons();
        initSeekBar();
        initTextView();
        initImageView();
    }


    private void buttonPressed() {

        startVerifying();



    }

    private void apiTesting() {
        Callback callBack = new TemplateCallback() {
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Toast.makeText(getApplicationContext(), response.body().string(), Toast.LENGTH_SHORT).show();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });

                JsonObject jsonObject = new JsonParser().parse(response.body().string()).getAsJsonObject();
                jsonObject.toString();// do what you want with the data
            }
        };
        BulletinBoardApi.enqueueGetRequest("http://validate.jsontest.com/?json=%5BJSON-code-to-validate%5D", callBack);


    }


    abstract class TemplateCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            Toast.makeText(getApplicationContext(), "Call failed", Toast.LENGTH_SHORT).show();
        }
    }


    private void initSeekBar() {
        percentageBar = (SeekBar) findViewById(R.id.seekBar);
        percentageBar.setMax(90);

        percentageBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                percentage = progress + 10;
                percentageText.setText(percentage + "%");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void initTextView() {
        percentageText = (TextView) findViewById(R.id.textView2);
    }

    private void initButtons() {
        theButton = (ImageButton) findViewById(R.id.b_1);

        theButton.setOnClickListener(myOnClickListener);


    }


    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener myOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.b_1:
                    buttonPressed();

                    break;

                default:
                    break;
            }
        }
    };

    private void updateUi(final boolean succeeded) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setBackgroundResource(succeeded ? R.drawable.mixnet_success : R.drawable.mixnet_failure);
            }
        });
    }


    private void startVerifying() {

        imageView.setBackgroundResource(0);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        String m = "החלה בדיקה של " + percentage + "% מתוך המיקסנט";
        progressDialog.setMessage(m);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // progressDialog.setMax(totalProgressTime);
        //progressDialog.setProgress(0);
        progressDialog.setIndeterminate(true);
        progressDialog.show();


        Group group = ParametersMain.ourGroup;
        final MixnetVerifier mixnetVerifier = new MixnetVerifier(group);


        //try to retrieve the data
        Callback mixnetCallback = new Callback() {
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    String proofsJson = response.body().string();
                    if (DEBUG_useLocalMixnetFile)
                        proofsJson = readFile(ParametersMain.getFile("RandomProofs.json"));

                    MixnetProofs mixnetProofs = MixnetVerifier.deserializeProofs(proofsJson);
                    final boolean verificationResult = mixnetVerifier.verifyMixnetRandomlyByPercentage(mixnetProofs, percentage);
                    //finishedProgress = true;
                    String x = "x";
                    updateUi(verificationResult);//TODO GUI - success

                } catch (Exception e) {
                    showFailureAlert(UNEXPECTED_ERROR);
                    updateUi(false);
                } finally {
                    progressDialog.dismiss();
                }

            }

            @Override
            public void onFailure(Call call, IOException e) {
                // tell user network request failed
                //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                showFailureAlert(NETWORK_ERROR);////TODO GUI - network request failed
                progressDialog.dismiss();
            }
        };
        BulletinBoardApi.enqueueGetRequest(BulletinBoardApi.MIXNET_REQUEST_URL, mixnetCallback);

    }





    private void initImageView() {
        imageView = (ImageView) findViewById(R.id.imageView1);
    }


    /*
         0: not assigned yet
         1: not assigned yet
         2: network failure
      */
    private void showFailureAlert(int error) {

        System.out.println("error code: " + error);

        String titleStringTmp = "", messageStringTmp = "", buttonStringTmp = "";
        titleStringTmp = "שגיאה";
        buttonStringTmp = "הבנתי";

        if (error == UNEXPECTED_ERROR) {
            messageStringTmp = "אנו מצטערים, חלה שגיאה בלתי צפויה.\nאנא נסה שנית מאוחר יותר.";
        } else if (error == NETWORK_ERROR) {
            messageStringTmp = "אנו נתקלים בתקלות תקשורת,\nאנא נסה שנית מאוחר יותר.";
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
        boolean showAgain = prefs.getBoolean("showAgainMixnet", true);
        if (!showAgain) return;


        final String titleString, messageString, buttonString, dontShowAgainString;
        titleString = "הנחיות לבדיקת אמינות ה-Mixnet";
        messageString = "מערכת ה-Mixnet אחראית על ערבול סדר ההצבעות והצפנתן מחדש.\nבדיקה זו נועדה לוודא שמערכת ה-Mixnet פועלת כשורה.\nאנא בחרו את היקף הבדיקה של ה-Mixnet באחוזים שברצונכם לבצע.\nהמערכת תבחר שכבות רנדומליות שה-Mixnet יצר בזמן הפעלתו.\nמספר השכבות יהיה בהתאם להיקף הבדיקה שתרצו לבצע.\nהמערכת תוודא שאלגוריתם ה-Mixnet פעל על השכבות האלה כמצופה.";
        buttonString = "הבנתי";


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
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
                    editor.putBoolean("showAgainMixnet", false);
                    editor.apply();
                }
            }
        });

        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.show();
    }






    private String readFile(File file) throws IOException {
        StringBuilder fileContents = new StringBuilder((int) file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }

}
