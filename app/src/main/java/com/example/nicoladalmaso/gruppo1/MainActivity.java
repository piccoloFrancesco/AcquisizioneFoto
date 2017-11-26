package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public FloatingActionButton fab, fab1, fab2;
    public Animation fab_open, fab_close, rotate_forward, rotate_backward;
    public List <Scontrino> list = new LinkedList<Scontrino>();
    public Uri photoURI;
    public boolean isFabOpen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeComponents();
    }

    //Dal Maso
    //Gestione delle animazioni e visualizzazione delle foto salvate precedentemente
    public void initializeComponents(){
        printAllImages();
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab1 = (FloatingActionButton)findViewById(R.id.fab1);
        fab2 = (FloatingActionButton)findViewById(R.id.fab2);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                animateFAB();
            }
        });
        //Camera button
        fab1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePhotoIntent();
            }
        });
        //Gallery button
        fab2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });
    }

    //Dal Maso
    //Animazioni per il Floating Action Button (FAB)
    public void animateFAB(){

        if(isFabOpen){

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
            Log.d("Raj", "close");

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
            Log.d("Raj","open");

        }
    }

    //Dal Maso
    //Aggiunge una card alla lista
    //Accetta come input 2 string contenenti il titolo e la descrizione dello scontrino
    public void addToList(String title, String desc, Bitmap img){
        list.add(new Scontrino(title, desc, img));
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list,this);
        listView.setAdapter(adapter);
    }


    /**PICCOLO
     * Metodo che "ripulisce" lo schermo dalle immagini
     */
    public void clearAllImages(){
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list,this);
        adapter.clear();
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }//clearAllImages



    /**Lazzarin
     * crea un file temporaneo dove salvare la foto scattata
     * @Framing Directory Pictures
     *
     */
    private File createImageFile() throws IOException {

        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageAllocation = File.createTempFile("temp", ".jpg", storageDirectory);
        return imageAllocation;
    }
    static final int REQUEST_TAKE_PHOTO = 1;
    /**
     * Lazzarin
     * Funzione che apre la fotocamera e assegna alla foto il File restituito
     * dal metodo createImageFile.
     * @Framing Camera, directory modified by createImageFile
     */
    private void takePhotoIntent() {
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhoto.resolveActivity(getPackageManager()) != null)
        {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }
            catch (IOException e) {}

            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);

                takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                CropImage.activity(photoURI).start(this);
                startActivityForResult(takePhoto, REQUEST_TAKE_PHOTO);
            }
        }
    }
      //Dal Maso
    public void deleteTempFiles(){
        File[] files = readAllImages();
        String filename = "";
        for (int i = 0; i < files.length; i++)
        {
            filename = files[i].getName();
            Log.d("Sub", filename.substring(0,4));
            if(filename.substring(0,4).equals("temp")){
                files[i].delete();
            }
        }
    }


    //Dal Maso
    //Selezione foto da galleria
    public static final int PICK_PHOTO_FOR_AVATAR = 2;

    public void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }


    //Dal Maso
    //Cattura risultato degli intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            switch (requestCode) {

                /**Lazzarin
                 * Gestisce l'intent prodotto dalla fotocamera
                 *Per il momento lo tengo buono, per una futura implementazione senza crop ad ogni scatto
                 *@Framing adds photo into ViewList
                 */
              /*  case (REQUEST_TAKE_PHOTO):
                    Bundle extras = data.getExtras();
                    Uri imageUri = (Uri) extras.get("data");
                    try{
                        Bitmap bitFromUri = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        savePickedFile(bitFromUri);
                        printLastImage();}
                    catch(IOException e)
                    {}
                    break;*/
                /**lazzarin
                 * Gestisce l'intent prodotto dalla fotocamera,andando ad eliminare il file temporaneo
                 * che è già stato passato al metodo di crop
                 */
                case(REQUEST_TAKE_PHOTO):
                    deleteTempFiles();
                    break;

                //Dal Maso
                //Foto presa da galleria
                case (PICK_PHOTO_FOR_AVATAR):
                    photoURI = data.getData();
                    //Invoco la libreria che si occupa del resize
                    CropImage.activity(photoURI)
                            .start(this);
                    break;
                //Dal Maso
                //Gestisco il risultato del Resize
                case (CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE):
                    Log.d("Alla", "okoko");
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Uri resultUri = result.getUri();
                    try {
                        Bitmap btm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        savePickedFile(btm);
                        printLastImage();
                    }catch (Exception e){
                        //Fai qualcosa
                    }
                    break;
            }
        }
    }

    //Dal Maso
    //Salva il bitmap passato nell'apposita cartella
    private void savePickedFile(Bitmap imageToSave) {
        String root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root);
        myDir.mkdirs();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String fname = imageFileName+".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 90, out);
            //PICCOLO
            // aggiungo il file al db
            //DatabaseManager helper = DatabaseManager.getInstance(getApplicationContext());
            //helper.addPhoto(root+fname); DB ALTERNATIVO
            //DbManager db = new DbManager(getApplicationContext());
            //db.addRecord(root+fname,"","","");
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lazzarin
     * parametro di ingresso: Bitmap imageToCrop
     * @return Uri
     * il parametro Uri ritornato è preso dal file intermedio tra foto
     * e resize(allocato in Documents per evitare
     * venga visualizzato nella gallery)
     */
    private Uri savePhotoForCrop (Bitmap imageToCrop) {
        File allocation=temporaryFile();
        try {
            FileOutputStream out = new FileOutputStream(allocation);
            imageToCrop.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Uri uri=Uri.fromFile(allocation);
        return uri;

    }

    /**
     *
     * @return temporary allocation with a File object.
     */
    private File temporaryFile()
    {
        String root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root);
        String imageFileName = "photoToCrop.jpg";
        File file = new File(myDir, imageFileName);
        if (file.exists())
            file.delete();
        return file;
    }

    //Dal Maso
    //Legge tutte le immagini
    private File[] readAllImages(){
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        return files;
    }

    //Dal Maso
    //Stampa tutte le immagini
    public void printAllImages(){
        File[] files = readAllImages();
        for (int i = 0; i < files.length; i++)
        {
            Bitmap myBitmap = BitmapFactory.decodeFile(files[i].getAbsolutePath());
            addToList(files[i].getName(), "Descrizione della foto", myBitmap);
        }
    }

    //Dal Maso
    //Stampa l'ultima foto
    private void printLastImage(){
        File[] files = readAllImages();
        Bitmap myBitmap = BitmapFactory.decodeFile(files[files.length-1].getAbsolutePath());
        addToList(files[files.length-1].getName(), "Descrizione della foto", myBitmap);
    }

    //Dal Maso
    //Stampa il bitmap passato (Solo per testing)
    private void printThisBitmap(Bitmap myBitmap){
        addToList("Print this bitmap", "Descrizione della foto", myBitmap);
    }


    /**PICCOLO_Edit by Dal Maso
     * Metodo che cancella l'i-esimo file in una directory
     * @param toDelete l'indice del file da cancellare
     * @param path percorso del file da cancellare
     * @return se l'operazione è andata a buon fine
     */
    public boolean deleteFile(int toDelete, String path){
        File directory = new File(path);
        File[] files = directory.listFiles();
        return files[toDelete].delete();
    }//deleteFile


    /**PICCOLO_Edit by Dal Maso
     * Metodo che cancella permette all'utente di ridimensionare la foto
     * @param toCrop l'indice della foto di cui fire il resize
     * @param path percorso della foto
     */
    public void cropFile(int toCrop, String path){
        Toast.makeText(getApplicationContext(), "aaaaaaaaaaaaaaaaaaaaa", Toast.LENGTH_SHORT).show();
        boolean result = false;
        File directory = new File(path);
        File[] files = directory.listFiles();
        CropImage.activity(Uri.fromFile(files[toCrop])).start(this);
    }//cropFile

    /**
     * VERSIONE DATABASE
     *PICCOLO
     * @param filename il id del file da cancellare a
     */
    private void deleteFileAndRow(String filename){
        DbManager db = new DbManager(getApplicationContext());
        //cancello il file associato solo se la query va a buon fine
        if(db.delete(filename)){
            File file = new File(filename);
            boolean deleted = file.delete();
        }//if

    }//deletePickedFile

}