package utils;

import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.Markup;

import java.awt.*;

import static net.sf.dynamicreports.report.builder.DynamicReports.stl;

/**
 * Created by conor on 04/10/2014.
 */
public class DynamicReportStylesHelper {

    private DynamicReportStylesHelper() {
        // Prevents instantiation
    }

    public static StyleBuilder boldStyle() {
        return stl.style().bold();
    }

    public static StyleBuilder centeredStyle() {return stl.style().setHorizontalAlignment(HorizontalAlignment.CENTER);}

    public static StyleBuilder boldCenteredStyle() {
        return stl.style(boldStyle())
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
    }

    public static StyleBuilder columnTitleStyle() {
        return stl.style(boldCenteredStyle())
                .setBorder(stl.pen1Point())
                .setBackgroundColor(Color.LIGHT_GRAY);
    }

    public static StyleBuilder styledMarkupStyle() {
        return stl.style().setMarkup(Markup.STYLED);
    }
}
