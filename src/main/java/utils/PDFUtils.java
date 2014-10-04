package utils;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by conor on 09/09/2014.
 */
public class PDFUtils {

    private PDFUtils() {
        // Prevents instantiation
    }

    public static PDFont getFont() {
        return PDType1Font.COURIER;
    }

    public static int getFontSize() {
        return 11;
    }

    public static String getNewLineString() {
        return " <NL> ";
    }

    public static String getNewLineString(int duplicate) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < duplicate; i++) {
            sb.append(getNewLineString());
        }
        return sb.toString().replace("  ", " ");
    }

    public static void setFont(PDPageContentStream stream) throws IOException {
        stream.setFont(getFont(), getFontSize());
    }

    public static int getMargin() {
        return 50;
    }

    public static void writeWrappedText (PDPage page, PDPageContentStream stream, String content,
                                         float currentX, float currentY) throws IOException {
        List<String> lines = new ArrayList<String>();
        PDRectangle mediabox = page.findMediaBox();
        float width = mediabox.getWidth() - 2*getMargin();

        float X = currentX == 0.0 ? mediabox.getLowerLeftX() + getMargin() : currentX;
        float Y = currentY == 0.0 ? mediabox.getUpperRightY() - getMargin() : currentY;

        stream.moveTextPositionByAmount(X, Y);

        StringBuilder builder = new StringBuilder();

        for (String word : content.split(" ")) {
            if(word.equals(getNewLineString().trim())) {
                lines.add(builder.toString());
                builder.setLength(0);
                continue;
            }

            if ((getStringWidth(builder.toString()) + getStringWidth(" " + word)) < width) {
                builder.append(" "+word);
            } else  {
                lines.add(builder.toString());
                builder.setLength(0);
                builder.append(word);
            }
        }
        if (builder.length() > 0) {
            lines.add(builder.toString());
        }

        for (String line : lines) {
            stream.drawString(line);
            stream.moveTextPositionByAmount(0, -getFontSize());
        }
    }

    public static float getStringWidth (String s) throws IOException {
        return getFont().getStringWidth(s) / 1000 * getFontSize();
    }
}
