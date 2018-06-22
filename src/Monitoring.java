import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Monitoring {

    public String ms = "";
    public int lostRoleNum = 0;
    public int lostMillNum = 0;

    public Monitoring(){}


    public static void start(){
        Monitoring m = new Monitoring();
        m.initConfig();
    }

    public void initConfig(){
        // 读取EXCEL

        String url = "https://eth.sparkpool.com/api/page/miner?value=26172f0a3ca20757bf366eb314f4c2cb36e470f4";
        String name = "liangdong";
        String ignore = "T1,1060,1080TI,RX570";
        String mill = "";
        this.checkRole(url, name, ignore);
        this.sendMessage();
    }

    public void checkRole(String url, String name, String ignore){
        String urlStr = getData(url);
        JSONObject jo = JSONObject.fromString(urlStr);
        JSONArray data = jo.getJSONObject("workers").getJSONArray("data");

        boolean lost = false;
        String ms = "";
        for (int i = 0; i < data.length(); i++) {
            JSONObject info = (JSONObject)data.get(i);
            String timeStr = info.getString("time");
            String localHash = info.getString("meanLocalHashrate1d");
            String millName = info.getString("rig");
//          String ms = "矿机：" + millName + " 算力：" + localHash + "最后更新时间：" + time;

            // 检测矿机是否掉线
            Date time = this.dateFormat(timeStr);
            if (this.checkOffline(time)){
                lost = true;
                this.lostMillNum++;
                SimpleDateFormat timeformat=new SimpleDateFormat("hh:mm:ss");
                String lostTime = timeformat.format(this.getLostTime(time));
                System.out.println("lostTime:" + lostTime);
                ms += (millName + "掉线  ");
            }
        }
        if (lost){
            this.lostRoleNum++;
            ms = name + "--" + ms;
            this.addMessage(ms);
        }else{
            System.out.println(name + "设备正常，在线矿机 " + data.length() + " 台");
        }
    }

    // 获取用户矿机数据
    public static String getData(String urlString) {
        String res = "";
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            URLConnection conn = (URLConnection)url.openConnection();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                res += line;
            }
            reader.close();
        } catch (Exception e) {
            e.getMessage();
        }
//        System.out.println("url data：" + res);
        return res;
    }

    public Date dateFormat(String time){
//        String time = "2018-06-22 12:13:00.829";
        time = time.replaceAll("T", " ");
        time = time.replaceAll("Z", " ");
        Date date = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = dateFormat.parse(time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR,8);
            date =cal.getTime();
            dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
            time = dateFormat.format(date);
        } catch (Exception e){
            System.out.println(e.getMessage());
        };
        System.out.println(time);
        return date;
    }

    public Boolean checkOffline(Date date){
        Date time = this.getLostTime(date);
        return time.getTime() >= 1000 * 60 * 10;
    }

    public Date getLostTime(Date date){
        Date now = new Date();
        long time1 = date.getTime();
        long time2 = now.getTime();
        long value = Math.abs(time2-time1);
        SimpleDateFormat timeformat=new SimpleDateFormat("hh:mm:ss");
        Date result = new Date();
        result.setTime(value);
        String lostTime = timeformat.format(result);
        System.out.println("lostTime:" + lostTime);
        return result;
    }

    public void addMessage(String mes){
        this.ms += mes;
    }

    public void sendMessage(){
        System.out.println(ms);
    }
}
