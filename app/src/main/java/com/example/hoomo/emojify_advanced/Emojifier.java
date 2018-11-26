package com.example.hoomo.emojify_advanced;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class Emojifier {

    private static final float EMOJI_SCALE_FACTOR = .9f;
    private static final double SMILING_PROB_THRESHOLD = .15;
    private static final double EYE_OPEN_PROB_THRESHOLD = .5;

    //this function detect the faces and make a red square over each one
    public Bitmap detectFaces(Context context, Bitmap myBitmap){

        // Create a Paint object for drawing with
        Paint myRictPaint = new Paint();
        myRictPaint.setStrokeWidth(5); //set width 5
        myRictPaint.setColor(Color.RED); //set color red
        myRictPaint.setStyle(Paint.Style.STROKE); //set it not fill only the border

        //Create a Canvas object for drawing on
        Bitmap tempbitmap = Bitmap.createBitmap(myBitmap.getWidth(),myBitmap.getHeight(),Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempbitmap);
        tempCanvas.drawBitmap(myBitmap,0,0,null);

        //Create the Face Detector
        FaceDetector faceDetector = new
                FaceDetector.Builder(context).setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
        //if our detector is operational before we use it.

        //create frame of the bit map to detect
        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();

        //detect the faces in the frame using faceDetector that return array of face
        SparseArray<Face>faces=faceDetector.detect(frame);

        // draw the rect on each face detected in the img
        for(int i=0; i<faces.size(); i++) {
            Face thisFace = faces.valueAt(i);
            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();
            tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRictPaint);
        }

        //release face detector
        faceDetector.release();

        //return the BitmapDrawable of the img after detection
        return tempbitmap;
    }

    //this function detect the faces and make his emoje over each one
    public Bitmap detectFaces1(Context context, Bitmap myBitmap){
        //Create the Face Detector
        FaceDetector faceDetector = new
                FaceDetector.Builder(context).setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        //create frame of the bit map to detect
        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();

        //detect the faces in the frame using faceDetector that return array of face
        SparseArray<Face>faces=faceDetector.detect(frame);

        //if there is no faces detected
        if(faces.size() <= 0)
            Toast.makeText(context,"there is no faces",Toast.LENGTH_LONG).show();

        Bitmap retBitMap=myBitmap;
        // draw the rect on each face detected in the img
        for(int i=0; i<faces.size(); i++) {
            Face thisFace = faces.valueAt(i);
            Bitmap emojeBitMap;
            switch (getClassifications(thisFace)){
                case smiling:
                    emojeBitMap=BitmapFactory.decodeResource(context.getResources(),R.drawable.smile);
                    break;
                case frowning:
                    emojeBitMap=BitmapFactory.decodeResource(context.getResources(),R.drawable.frown);
                    break;
                case right_wink:
                    emojeBitMap=BitmapFactory.decodeResource(context.getResources(),R.drawable.rightwink);
                    break;
                case left_wink:
                    emojeBitMap=BitmapFactory.decodeResource(context.getResources(),R.drawable.leftwink);
                    break;
                case right_wink_frowning:
                    emojeBitMap=BitmapFactory.decodeResource(context.getResources(),R.drawable.rightwinkfrown);
                    break;
                case left_wink_frowning:
                    emojeBitMap=BitmapFactory.decodeResource(context.getResources(),R.drawable.leftwinkfrown);
                    break;
                case closed_eye_smiling:
                    emojeBitMap=BitmapFactory.decodeResource(context.getResources(),R.drawable.closed_smile);
                    break;
                case close_eye_frowning:
                    emojeBitMap=BitmapFactory.decodeResource(context.getResources(),R.drawable.closed_frown);
                    break;
                default:
                    emojeBitMap=null;
                    Toast.makeText(context,"9",Toast.LENGTH_LONG).show();
                    Toast.makeText(context, "sorry we cant find the emojr", Toast.LENGTH_SHORT).show();
            }

            retBitMap=addBitmapToFace(retBitMap,emojeBitMap,thisFace);
        }
        //release face detector
        faceDetector.release();

        return retBitMap;
    }

    //this function to draw the emoje on the bit map
    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {

        // Initialize the results bitmap to be a mutable copy of the original image
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        // Scale the emoji so it looks better on the face
        float scaleFactor = EMOJI_SCALE_FACTOR;

        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() *
                newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);


        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

        // Determine the emoji position so it best lines up with the face
        float emojiPositionX =
                (face.getPosition().x + face.getWidth() / 2) - emojiBitmap.getWidth() / 2;
        float emojiPositionY =
                (face.getPosition().y + face.getHeight() / 2) - emojiBitmap.getHeight() / 3;

        // Create the canvas and draw the bitmaps to it
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
    }


    //this function to get the emoje of the face
    public emoje getClassifications (Face face){
        //if the face smile or not and get the state of the eye close or open for each one
        boolean leftopen = face.getIsLeftEyeOpenProbability() > EYE_OPEN_PROB_THRESHOLD ;
        boolean rightopen = face.getIsRightEyeOpenProbability() > EYE_OPEN_PROB_THRESHOLD ;
        boolean smile = face.getIsSmilingProbability() > SMILING_PROB_THRESHOLD ;
        //return emoje that detect which face emoje
        emoje retemoje;
        //check probability to detect the emoje of the face
        if(smile){
            if(leftopen && rightopen){
                retemoje=emoje.smiling;
            }else if(!leftopen && rightopen){
                retemoje=emoje.left_wink;
            }else if(leftopen && !rightopen) {
                retemoje=emoje.right_wink;
            }else {
                retemoje=emoje.closed_eye_smiling;
            }
        }else{
            if(leftopen && rightopen){
                retemoje=emoje.frowning;
            }else if(!leftopen && rightopen){
                retemoje=emoje.left_wink_frowning;
            }else if(leftopen && !rightopen) {
                retemoje=emoje.right_wink_frowning;
            }else {
                retemoje=emoje.close_eye_frowning;
            }
        }
        //return which emoje
        return retemoje;
    }

    //enum class has the only probability code be with only eye and smile or frown
    private enum emoje{
        smiling, frowning, left_wink, right_wink, left_wink_frowning, right_wink_frowning, closed_eye_smiling, close_eye_frowning
    }
}
