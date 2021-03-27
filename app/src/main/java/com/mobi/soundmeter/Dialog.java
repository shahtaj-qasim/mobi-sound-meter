package com.mobi.soundmeter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Dialog extends Activity {

    private ImageView mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        int[] images = new int[] {R.drawable.image1, R.drawable.image2, R.drawable.image3,R.drawable.image4,
                R.drawable.image5,R.drawable.image6,R.drawable.image7,R.drawable.image8,R.drawable.image9,R.drawable.image10};

        mDialog = (ImageView)findViewById(R.id.image);
        int imageId = (int)(Math.random() * images.length);
        mDialog.setClickable(true);
        mDialog.setBackgroundResource(images[imageId]);

        //finish the activity (dismiss the image dialog) if the user clicks
        //anywhere on the image
        mDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}