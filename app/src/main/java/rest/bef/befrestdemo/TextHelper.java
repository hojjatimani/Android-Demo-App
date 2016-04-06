package rest.bef.befrestdemo;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hojjatimani on 4/3/2016 AD.
 */
public class TextHelper {
    private static final String TAG = "TextHelper";
    private static final String FONTS_PATH = "fonts/";
    private static final String FONTS_EXTENTION = ".ttf";
    private static Map<String, Typeface> fonts = new HashMap<>();

    public enum FontFamily {
        IranSans("IranSans"),
        Default(IranSans.toString());

        private String text;

        FontFamily(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public enum FontWeight {
        Bold("Bold"),
        Regular("Regular");


        private String text;

        FontWeight(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }


    public static void setFont(Context context, FontFamily fontFamily, FontWeight fontWeight, Object... elements) {
        Typeface typeFace = getTypeFace(context, fontFamily, fontWeight);
        setFont(typeFace, elements);
    }

    public static Typeface getTypeFace(Context context, FontFamily fontFamily, FontWeight fontWeight) {
        if (!fonts.containsKey(fontFamily.toString() + fontWeight.toString()))
            fonts.put(fontFamily.toString() + fontWeight.toString(), Typeface.createFromAsset(context.getAssets(), getTypeFacePath(fontFamily, fontWeight)));
        return fonts.get(fontFamily.toString() + fontWeight.toString());
    }

    private static void setFont(Typeface typeface, Object... elements) {
        for (Object element : elements) {
            if (element instanceof TextView)
                ((TextView) element).setTypeface(typeface);
            else if (element instanceof Button)
                ((Button) element).setTypeface(typeface);
            else if (element instanceof ToggleButton)
                ((ToggleButton) element).setTypeface(typeface);
            else
                Log.e(TAG, "invalid input!");
        }
    }

    private static String getTypeFacePath(FontFamily fontFamily, FontWeight fontWeight) {
        return FONTS_PATH + fontFamily.toString() + "-" + fontWeight.toString() + FONTS_EXTENTION;
    }
}
