package org.tensorflow.lite.examples.classification;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;

import org.tensorflow.lite.examples.classification.tflite.Classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<ImageItem> selImageList;
    private int maxImgCount = 1;
    public static final int REQUEST_CODE_SELECT = 100;
    public static final int REQUEST_CODE_PREVIEW = 101;
    private Classifier.Model model = Classifier.Model.QUANTIZED;
    private Classifier.Device device = Classifier.Device.CPU;
    private int numThreads = -1;
    public Classifier classifier;
    private TextView textView;
    private TextView textView2;
    private Button button;
    private ArrayList<String> classNames;
    private TFLiteClassificationUtil tfLiteClassificationUtil;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
         button = findViewById(R.id.button);
         textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        initImagePicker();
        classNames = Utils.ReadListFromFile(getAssets(), "label_list.txt");
        String classificationModelPath = getCacheDir().getAbsolutePath() + File.separator + "mobilenet_v2.tflite";
        Log.e("here",classificationModelPath);
        Utils.copyFileFromAsset(MainActivity.this, "mobilenet_v2.tflite", classificationModelPath);
        try {
            tfLiteClassificationUtil = new TFLiteClassificationUtil(classificationModelPath);
            Toast.makeText(MainActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }
        try {
            classifier= Classifier.create(this,model,device,numThreads);
        } catch (IOException e) {
            e.printStackTrace();
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.getInstance().setSelectLimit(1);
                Intent intent1 = new Intent(MainActivity.this, ImageGridActivity.class);
                startActivityForResult(intent1, REQUEST_CODE_SELECT);
            }
        });
    }

    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //?????????????????????
        imagePicker.setShowCamera(true);                      //??????????????????
        imagePicker.setCrop(true);                            //?????????????????????????????????
        imagePicker.setSaveRectangle(true);                   //??????????????? ????????????
        imagePicker.setSelectLimit(1);                         //??????????????????
        imagePicker.setMultiMode(false);                      //??????
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //??????????????????
        imagePicker.setFocusWidth(1000);                       //?????????????????????????????????????????????????????????????????????
        imagePicker.setFocusHeight(1000);                      //?????????????????????????????????????????????????????????????????????
        imagePicker.setOutPutX(1000);                         //????????????????????????????????????
        imagePicker.setOutPutY(1000);                         //????????????????????????????????????
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            //??????????????????
            if (data != null && requestCode == REQUEST_CODE_SELECT) {
                final ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                if (images != null){
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(images.get(0).path);
                        long start = System.currentTimeMillis();
                        float[] result = tfLiteClassificationUtil.predictImage(images.get(0).path);
                        long end = System.currentTimeMillis();
                        String show_text = "?????????????????????" + (int) result[0] +
                                "\n?????????" +  classNames.get((int) result[0]) +
                                "\n?????????" + result[1] +
                                "\n?????????" + (end - start) + "ms";
                        textView.setText(show_text);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }
        } else if (resultCode == ImagePicker.RESULT_CODE_BACK) {
            //??????????????????
            if (data != null && requestCode == REQUEST_CODE_PREVIEW) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_ITEMS);
                if (images != null){
                    selImageList.clear();
                    selImageList.addAll(images);
                }
            }
        }
    }

}
