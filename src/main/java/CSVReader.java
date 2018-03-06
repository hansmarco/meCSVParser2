import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CSVReader {

    public static void main(String[] args) {

        String csvFile = "c:\\Users\\marco\\Desktop\\RR1234_xxx.csv";
        String line = "";
        String cvsSplitBy = ";";
        Date startingPoint = new Date();
        Long startingMillis = startingPoint.getTime();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            int rows = 0;
            while ((line = br.readLine()) != null) {

                if (rows >0) {
                    // use comma as separator
                    String[] country = line.split(cvsSplitBy);

                    // System.out.println("[time= " + country[0] + " , RR=" + country[1] + "]");
                    Long nanoPIT = new BigDecimal(country[0]).longValue();
                    //Long nanoPIT = Long.parseLong(country[0]);
                    Long millisPIT = startingMillis + nanoPIT/1000;

                    //System.out.println("row: "+rows+"; millisPIT: "+millisPIT);
                }
                rows++;
            }
            long millis = System.currentTimeMillis() % 1000;
            System.out.println("millis: "+millis);
            //myTime();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public  void myTime() throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String dateInString = "04-03-2018 10:20:56";
        Date date = sdf.parse(dateInString);

        System.out.println(dateInString);
        System.out.println("Date - Time in milliseconds : " + date.getTime());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        System.out.println("Calender - Time in milliseconds : " + calendar.getTimeInMillis());
    }
}