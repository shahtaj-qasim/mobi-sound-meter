package com.mobi.soundmeter;

import android.app.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;

import android.location.Address;
import android.location.Geocoder;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONException;
import org.json.JSONObject;
import rabbitmqconfig.MQConfiguration;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import static android.content.ContentValues.TAG;
import static rabbitmqconfig.MQConfiguration.*;

public class MainActivity extends Activity {
    ArrayList<Entry> yVals;
    boolean refreshed=false;
    Speedometer speedometer;
    public static Typeface tf;
    ImageButton infoButton;
    ImageButton refreshButton, Health;
    Button saveDataButton, stopButton;

    ImageButton result;
    LineChart mChart;
    TextView minVal;
    TextView maxVal;
    TextView mmVal;
    TextView curVal;
    long currentTime=0;
    long savedTime=0;
    boolean isChart=false;
    boolean isMoney=false;
    boolean stopClicked=false;
    double latitude, longitude;
    Geocoder geocoder;
    List<Address> addresses;
    /* Decibel */
    private boolean bListener = true;
    private boolean isThreadRun = true;
    private Thread thread;
    float volume = 10000;
    int refresh=0;
    private MyMediaRecorder mRecorder ;
    int id=0;
    final String corrId = UUID.randomUUID().toString();
    final Channel channel = MQConfiguration.createQueue();
    BasicProperties props;
    String callbackQueueName = channel.queueDeclare().getQueue();

    String avgResult, sumResult, minResult, maxResult;
    String[] parts;
    String postalCodeResult, cityResult, timestampResult;
    FirebaseFirestore fbStore;

    int iteration =0 ;
    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");

    Map<String, Object> msg;

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            DecimalFormat df1 = new DecimalFormat("####.0");
            if(msg.what == 1){
                if(!isChart){
                    initChart();
                    return;
                }
                speedometer.refresh();
                minVal.setText(df1.format(World.minDB));
                mmVal.setText(df1.format((World.minDB+World.maxDB)/2));
                maxVal.setText(df1.format(World.maxDB));
                curVal.setText(df1.format(World.dbCount));
                updateData(World.dbCount,0);
                if(refresh==1){
                    long now=new Date().getTime();
                    now=now-currentTime;
                    now=now/1000;
                    refresh=0;
                }else {
                    refresh++;
                }
            }
        }
    };

    public MainActivity() throws IOException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
            getLocation();
        }
        ///////////////////////////LOCATION PERMISSION CHECK///////////////////////////////
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);

        tf= Typeface.createFromAsset(this.getAssets(), "fonts/Let_s go Digital Regular.ttf");
        minVal=(TextView)findViewById(R.id.minval);minVal.setTypeface(tf);
        mmVal=(TextView)findViewById(R.id.mmval);mmVal.setTypeface(tf);
        maxVal=(TextView)findViewById(R.id.maxval);maxVal.setTypeface(tf);
        curVal=(TextView)findViewById(R.id.curval);curVal.setTypeface(tf);
        infoButton=(ImageButton)findViewById(R.id.infobutton);
        result=findViewById(R.id.results);

//        viewAnalysis=(Button)findViewById(R.id.buttonViewAnalysis);
//
//        viewAnalysis.setOnClickListener(new View.OnClickListener() {
//            Date date;
//            @Override
//            public void onClick(View view) {
//                //rough - removing afterwards
//                date = new Date(System.currentTimeMillis());
//                fbStore = FirebaseFirestore.getInstance();
//                CollectionReference collectionRef = fbStore.collection("noiseCollection");
//                collectionRef.whereEqualTo("date",formatter.format(date)).orderBy("average", Query.Direction.DESCENDING).limit(1)
//                        .get()
//                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                if (task.isSuccessful()) {
//                                    for (QueryDocumentSnapshot document : task.getResult()) {
//                                        Log.d(TAG, "I am maximum average"+ document.getId() + " => " + document.getData());
//                                        //msg  = document.getData();
//                                    }
//                                } else {
//                                    Log.d(TAG, "Error getting documents: ", task.getException());
//                                }
//                            }
//                        });
//
//                date = DateUtils.addDays(new Date(), -1);
//                collectionRef.whereEqualTo("date",formatter.format(date)).orderBy("average", Query.Direction.DESCENDING).limit(1)
//                        .get()
//                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                if (task.isSuccessful()) {
//                                    for (QueryDocumentSnapshot document : task.getResult()) {
//                                        Log.d(TAG, "I am maximum average"+ document.getId() + " => " + document.getData());
//                                        //msg  = document.getData();
//                                    }
//                                } else {
//                                    Log.d(TAG, "Error getting documents: ", task.getException());
//                                }
//                            }
//                        });
//
//
//                //end
//            }});

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InfoDialog.Builder builder = new InfoDialog.Builder(MainActivity.this);
                builder.setMessage(getString(R.string.activity_infobull));
                builder.setTitle(getString(R.string.activity_infotitle));
                builder.setNegativeButton(getString(R.string.activity_infobutton),
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
            }
        });

        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Results.class);
                MainActivity.this.startActivity(intent);
            }
        });



        saveDataButton=(Button)findViewById(R.id.buttonSave);
        stopButton=(Button)findViewById(R.id.buttonStop);
        saveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopClicked = false;
                saveDataButton.setVisibility(View.INVISIBLE);
                stopButton.setVisibility(View.VISIBLE);
                props = new BasicProperties
                        .Builder()
                        .correlationId(corrId)
                        .replyTo(callbackQueueName)
                        .build();

                Thread thread = new Thread(runnable);
                thread.start();


            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopButton.setVisibility(View.INVISIBLE);
                saveDataButton.setVisibility(View.VISIBLE);
                stopClicked = true;
                //consume from rabbitmq
                fbStore = FirebaseFirestore.getInstance();
                Thread thread = new Thread(runnableForConsume);
                thread.start();

            }
        });
        

        refreshButton=(ImageButton)findViewById(R.id.refreshbutton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshed=true;
                World.minDB=100;
                World.dbCount=0;
                World.lastDbCount=0;
                World.maxDB=0;
                initChart();
            }
        });

        speedometer=(Speedometer)findViewById(R.id.speed);
        mRecorder = new MyMediaRecorder();

        Health=(ImageButton)findViewById(R.id.education);
        Health.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(MainActivity.this, Dialog.class);
                MainActivity.this.startActivity(mainIntent);
            }
        });
    }

    Runnable runnableForConsume = new Runnable() {
        @Override
        public void run() {
            try {
                String ctag = channel.basicConsume(RECEIVING_QUEUE, true, (consumerTag, delivery) -> {
                    String msg = new String(delivery.getBody(), "UTF-8");
                    //real time attribute results
                    parts = msg.split("\\|");
                    avgResult = parts[1];
                    sumResult = parts[2];
                    minResult = parts[3];
                    maxResult = parts[4];
                    postalCodeResult = parts[5];
                    cityResult = parts[6];
                    timestampResult = parts[7];
                    postalCodeResult = postalCodeResult.replace("[", "");
                    postalCodeResult = postalCodeResult.replace("]", "");
                    cityResult = cityResult.replace("[", "");
                    cityResult = cityResult.replace("]", "");

                    //put real time noise decibels in database
                    Map<String, Object> noiseMap = new HashMap<>();
                    noiseMap.put("average", Double.parseDouble(avgResult));
                    noiseMap.put("sum", Double.parseDouble(sumResult));
                    noiseMap.put("minimum", Double.parseDouble(minResult));
                    noiseMap.put("maximum", Double.parseDouble(maxResult));
                    noiseMap.put("postalCode", postalCodeResult);
                    noiseMap.put("city", cityResult);
                    Date date = new Date(System.currentTimeMillis());
                    noiseMap.put("date",formatter.format(date));
                    fbStore.collection("noiseCollection").
                            document().
                            set(noiseMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        System.out.print("Successfulllllllllll");
                                    }
                                    else{
                                        Log.d("This", task.getException().getMessage());
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("This", e.getMessage());

                               }
                            });
                    iteration++;
                    System.out.println("!!! Received response in client:  "+noiseMap);
                }, consumerTag -> {
                });
                channel.basicCancel(ctag);

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    };

    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            while (!stopClicked) {
                JSONObject soundData = new JSONObject();
                try {
                    soundData.put("id", id);
                    soundData.put("minimumValue", (new BigDecimal(World.minDB).setScale(2, RoundingMode.DOWN)).doubleValue());
                    soundData.put("averageValue", new BigDecimal(((World.minDB + World.maxDB) / 2)).setScale(2, RoundingMode.DOWN).doubleValue());
                    soundData.put("maximumValue", (new BigDecimal(World.maxDB).setScale(2, RoundingMode.DOWN)).doubleValue());
                    soundData.put("realTimeValue", (new BigDecimal(World.dbCount).setScale(2, RoundingMode.DOWN)).doubleValue());
                    soundData.put("latitude", latitude);
                    soundData.put("longitude", longitude);
                    soundData.put("postalCode",getPostalCode());
                    soundData.put("city",getCityName());
                    Long tsLong = System.currentTimeMillis() / 1000;
                    soundData.put("timestamp", tsLong);
                    System.out.println("sound  " + soundData);


                    channel.basicPublish("", SENDING_QUEUE, props, soundData.toString().getBytes("UTF-8"));
                } catch (IOException | JSONException e) {
                    System.out.println(e.getMessage());
                }
                id = id + 1;

            }
        }
        };

    private void updateData(float val, long time) {
        if(mChart==null){
            return;
        }
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            LineDataSet set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals);
            Entry entry=new Entry(savedTime,val);
            set1.addEntry(entry);
            if(set1.getEntryCount()>200){
                set1.removeFirst();
                set1.setDrawFilled(false);
            }
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.invalidate();
            savedTime++;
        }
    }

    public void getLocation(){
        GpsTracker gpsTracker = new GpsTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            latitude = gpsTracker.getLatitude();
            longitude =  gpsTracker.getLongitude();
        }else{
            gpsTracker.showSettingsAlert();
        }

    }
    private String getPostalCode() throws IOException {

        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

       // String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
      //  String city = addresses.get(0).getLocality();
      //  String state = addresses.get(0).getAdminArea();
      //  String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
      //  String knownName = addresses.get(0).getFeatureName();
        return postalCode;
    }

    private String getCityName() throws IOException {
        geocoder = new Geocoder(this, Locale.getDefault());
        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        String city = addresses.get(0).getLocality();
        return city;
    }


    private void initChart() {
        if(mChart!=null){
            if (mChart.getData() != null &&
                    mChart.getData().getDataSetCount() > 0) {
                savedTime++;
                isChart=true;
            }
        }else{
            currentTime=new Date().getTime();
            mChart = (LineChart) findViewById(R.id.chart1);
            mChart.setViewPortOffsets(50, 20, 5, 60);
            // no description text
            mChart.setDescription("");
            // enable touch gestures
            mChart.setTouchEnabled(true);
            // enable scaling and dragging
            mChart.setDragEnabled(false);
            mChart.setScaleEnabled(true);
            // if disabled, scaling can be done on x- and y-axis separately
            mChart.setPinchZoom(false);
            mChart.setDrawGridBackground(false);
            //mChart.setMaxHighlightDistance(400);
            XAxis x = mChart.getXAxis();
            x.setLabelCount(8, false);
            x.setEnabled(true);
            x.setTypeface(tf);
            x.setTextColor(Color.GREEN);
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            x.setDrawGridLines(true);
            x.setAxisLineColor(Color.GREEN);
            YAxis y = mChart.getAxisLeft();
            y.setLabelCount(6, false);
            y.setTextColor(Color.GREEN);
            y.setTypeface(tf);
            y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            y.setDrawGridLines(false);
            y.setAxisLineColor(Color.GREEN);
            y.setAxisMinValue(0);
            y.setAxisMaxValue(120);
            mChart.getAxisRight().setEnabled(true);
            yVals = new ArrayList<Entry>();
            yVals.add(new Entry(0,0));
            LineDataSet set1 = new LineDataSet(yVals, "DataSet 1");
            set1.setValueTypeface(tf);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.02f);
            set1.setDrawFilled(true);
            set1.setDrawCircles(false);
            set1.setCircleColor(Color.GREEN);
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setColor(Color.GREEN);
            set1.setFillColor(Color.GREEN);
            set1.setFillAlpha(100);
            set1.setDrawHorizontalHighlightIndicator(false);
            set1.setFillFormatter(new FillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return -10;
                }
            });
            LineData data;
            if (mChart.getData() != null &&
                    mChart.getData().getDataSetCount() > 0) {
                data =  mChart.getLineData();
                data.clearValues();
                data.removeDataSet(0);
                data.addDataSet(set1);
            }else {
                data = new LineData(set1);
            }

            data.setValueTextSize(9f);
            data.setDrawValues(false);
            mChart.setData(data);
            mChart.getLegend().setEnabled(false);
            mChart.animateXY(2000, 2000);
            // dont forget to refresh the drawing
            mChart.invalidate();
            isChart=true;
        }

    }
    /* Sub-chant analysis */
    private void startListenAudio() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isThreadRun) {
                    try {
                        if(bListener) {
                            volume = mRecorder.getMaxAmplitude();  //Get the sound pressure value
                            if(volume > 0 && volume < 1000000) {
                                World.setDbCount(20 * (float)(Math.log10(volume)));  //Change the sound pressure value to the decibel value
                                // Update with thread
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                            }
                        }
                        if(refreshed){
                            Thread.sleep(1200);
                            refreshed=false;
                        }else{
                            Thread.sleep(200);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        bListener = false;
                    }
                }
            }
        });
        thread.start();
    }
    /**
     * Start recording
     * @param fFile
     */
    public void startRecord(File fFile){
        try{
            mRecorder.setMyRecAudioFile(fFile);
            if (mRecorder.startRecorder()) {
                startListenAudio();
            }else{
                Toast.makeText(this, getString(R.string.activity_recStartErr), Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            Toast.makeText(this, getString(R.string.activity_recBusyErr), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        File file = FileUtil.createFile("temp.amr");
        if (file != null) {
            startRecord(file);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.activity_recFileErr), Toast.LENGTH_LONG).show();
        }
        bListener = true;
    }

    /**
     * Stop recording
     */
    @Override
    protected void onPause() {
        super.onPause();
        bListener = false;
        mRecorder.delete(); //Stop recording and delete the recording file
        thread = null;
        isChart=false;
    }

    @Override
    protected void onDestroy() {
        if (thread != null) {
            isThreadRun = false;
            thread = null;
        }
        mRecorder.delete();
        super.onDestroy();
    }
}
