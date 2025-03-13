package hr.java.application.routeplanner.thread;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import hr.java.application.routeplanner.entity.Ride;

public class InsertToReport extends DataThread implements Runnable{
    private Ride r;
    private PdfPTable table;
    private static final Object lock = new Object();

    public InsertToReport(Ride r, PdfPTable table) {
        this.r = r;
        this.table = table;
    }

    @Override
    public void run() {
        PdfPCell cell1 = new PdfPCell(new Paragraph(r.getName().toString()));
        cell1.setBorderColor(BaseColor.BLACK);
        cell1.setPaddingLeft(5);
        cell1.setPaddingBottom(5);

        PdfPCell cell2 = new PdfPCell(new Paragraph(String.valueOf(r.getDistanceTraveled())));
        cell2.setBorderColor(BaseColor.BLACK);
        cell2.setPaddingLeft(5);
        cell2.setPaddingBottom(5);

        PdfPCell cell3 = new PdfPCell(new Paragraph(String.format("%02d:%02d:%02d", (int)(r.getDistanceTraveled()/r.getAvgSpeed()), (int)(r.getDistanceTraveled()/r.getAvgSpeed()*60%60), Math.round(r.getDistanceTraveled()/r.getAvgSpeed()*60*60%60))));
        cell3.setBorderColor(BaseColor.BLACK);
        cell3.setPaddingLeft(5);
        cell3.setPaddingBottom(5);

        PdfPCell cell4 = new PdfPCell(new Paragraph(String.valueOf(r.getAvgSpeed())));
        cell4.setBorderColor(BaseColor.BLACK);
        cell4.setPaddingLeft(5);
        cell4.setPaddingBottom(5);

        PdfPCell cell5 = new PdfPCell(new Paragraph(String.valueOf(r.getMaxSpeed())));
        cell5.setBorderColor(BaseColor.BLACK);
        cell5.setPaddingLeft(5);
        cell5.setPaddingBottom(5);

        synchronized (lock) {
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
        }
    }
}
