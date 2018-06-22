import java.util.Timer;
import java.util.TimerTask;

public class Main {

    public static void main(String[] args) {
        System.out.println("Start monitoring");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Monitoring.start();
            }
        }, 1000, 1000 * 60 * 5);// 设定指定的时间time,此处为2000毫秒
        System.out.println("Done");
    }

    public static void test(){
//        String content = "2018-06-22 12:13:00.829";
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        try{
//            Date date = dateFormat.parse(content);
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(date);
//            cal.add(Calendar.HOUR,8);
//            date =cal.getTime();
//            dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
//            content = dateFormat.format(date);
//        } catch (java.text.ParseException e){
//            System.out.println(e.getMessage());
//        };
//        System.out.println(content);
    }


}
