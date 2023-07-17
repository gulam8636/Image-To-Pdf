package com.ali.pdf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.ali.pdf.Adapter.Gridview_Adapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private GridView gridView;
    Gridview_Adapter gridview_adapter;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_SELECT_IMAGES = 2;
    private boolean isPermissionGranted = false;
    private ArrayList<Uri> selectedImageUris;
    private ImageView imageView;
    private Button convertBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.addImg);
        convertBtn = findViewById(R.id.ConvertBtn);
        gridView = findViewById(R.id.My_Grid_View);
      convertBtn.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View view) {
        if (isPermissionGranted) {
            // Ask for the PDF file name after the image selection
            askForPdfName();
          }
        }
      });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionGranted==true){
                    selectImagesFromGallery();
                }
            }
        });
        // Check if the READ_EXTERNAL_STORAGE permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // Permission already granted
            isPermissionGranted = true;

        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                isPermissionGranted = true;
            } else {
                // Permission denied, show a message or take appropriate action
                Toast.makeText(this, "Read external storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void selectImagesFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), REQUEST_SELECT_IMAGES);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SELECT_IMAGES && resultCode == RESULT_OK && data != null) {
            selectedImageUris = new ArrayList<>();

            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);

                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                selectedImageUris.add(imageUri);
            }
            gridview_adapter = new Gridview_Adapter(selectedImageUris);
            gridView.setAdapter(gridview_adapter);
            gridview_adapter.notifyDataSetChanged();
        }
    }

    private void askForPdfName() {

         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Enter PDF File Name");
         final EditText editText = new EditText(this);
         builder.setView(editText);
         builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 String pdfFileName = editText.getText().toString().trim();
                 if (!pdfFileName.isEmpty()) {
                     savePdfFile(pdfFileName);
                 } else {
                     Toast.makeText(MainActivity.this, "Please enter a PDF file name", Toast.LENGTH_SHORT).show();
                 }
             }
         });
         builder.setNegativeButton("Cancel", null);
         builder.show();
    }


    private byte[] getByteArrayFromUri(Uri uri) throws IOException, FileNotFoundException {
        ContentResolver contentResolver = getContentResolver();
        InputStream inputStream = contentResolver.openInputStream(uri);
        if (inputStream != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        }
        return null;
    }

private void savePdfFile(String pdfFileName) {
    Document document = new Document();
    try {
        String pdfPath = getOutputPdfPath(pdfFileName);
        PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
        document.setPageSize(PageSize.A4); // Set the page size to portrait mode
        document.setMargins(0, 0, 0, 0); // Set zero margins
        document.open();
        for (Uri imageUri : selectedImageUris) {
           byte[] imageData = getByteArrayFromUri(imageUri);
                if (imageData != null) {
                    Image image = Image.getInstance(imageData);
                    image.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
                    image.setAbsolutePosition(0, document.getPageSize().getHeight() - image.getScaledHeight()); // Position the image at top-left corner
                    document.add(image);
                    document.newPage();
                }// Add a new page for each image
        }

        document.close();
        Toast.makeText(this, "PDF file saved successfully", Toast.LENGTH_SHORT).show();

        // Open the PDF file
        File pdfFile = new File(pdfPath);


        if (pdfFile.exists()) {
            openPdfFile(pdfPath);
        }
    } catch (Exception e) {
        e.printStackTrace();
        Toast.makeText(this, "Failed to save the PDF file", Toast.LENGTH_SHORT).show();
    }
}



    private void openPdfFile(String pdfPath) {
        File file = new File(pdfPath);
        Uri pdfUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private String getOutputPdfPath(String pdfFileName) {

        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MyPDFs");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return new File(directory, pdfFileName + ".pdf").getAbsolutePath();
    }



}
