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

    private static Monitoring monitor=null;
    public static Monitoring getInstance() {
        if (monitor == null) {
            monitor = new Monitoring();
            monitor.sendQQMessage("监控启动");
        }
        return monitor;
    }

    public static void start(){
        Monitoring m = Monitoring.getInstance();
        m.initConfig();
    }

    public void initConfig(){
        // 读取EXCEL
//        String jsonData =
//                "[{url:'1dC7d304801180Eac8442eFD59Db3DB947115c34', name:'黄健', ignore:'', mill:''}]";
        String jsonData =
                "[{url:'26172f0a3ca20757bf366eb314f4c2cb36e470f4', name:'梁栋', ignore:'T1,1060,1080TI,RX570', mill:''}," +
                        "{url:'90625a484f7c07f912a9fbf93730881921b5ad46', name:'陈飞', ignore:'', mill:'cf-006,cf01,cf002,cf004,ETH-46,ETH-48,ETH-50,ETH-54,ETH-56,ETH-57,ETH-58,Eth-59'}," +
                        "{url:'1dc7d304801180eac8442efd59db3db947115c34', name:'黄健', ignore:'eth1.0', mill:''}," +
                        "{url:'4c0d720257c1d0da0756473719b2295e7783b518', name:'吴凯1', ignore:'eth1.0', mill:''}," +
                        "{url:'83e7ee60d97ba88470de048b5e263e68a56f5d13', name:'吴凯2', ignore:'eth1.0', mill:''}," +
                        "{url:'3a04d7c8426e077641a3b5f7c74e112d53fcf9b0', name:'吴增凯', ignore:'eth1.0', mill:'J840,J841,J842,J843,J844,J845,J846,wzk-01,wzk-2,wzk-03,wzk-04'}," +
                        "]";

        JSONArray ja = JSONArray.fromString(jsonData);
        for (int i = 0; i < ja.length(); i++){
            JSONObject roleInfo = (JSONObject)ja.get(i);
            String url = "https://eth.sparkpool.com/api/page/miner?value=" + roleInfo.getString("url");
            String name = roleInfo.getString("name");
            String ignore = roleInfo.getString("ignore");
            String mill = roleInfo.getString("mill");
            this.checkRole(url, name, mill, ignore);
            this.sendMessage();
        }
    }

    public void checkRole(String url, String name, String mill, String ignore){
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

            if (ignore.length() > 0 && ignore.indexOf(millName) >= 0)
                continue;

            if (mill.length() > 0 && mill.indexOf(millName) < 0)
                continue;

            // 检测矿机是否掉线
            Date time = this.dateFormat(timeStr);
            if (this.checkOffline(time)){
                lost = true;
                this.lostMillNum++;
                SimpleDateFormat timeformat=new SimpleDateFormat("hh:mm:ss");
                String lostTime = timeformat.format(this.getLostTime(time));
//                System.out.println("lostTime:" + lostTime);
                ms += (millName + "掉线  ");
            }
        }
        if (lost){
            this.lostRoleNum++;
            ms = name + "--" + ms;
            this.addMessage(ms);
        }else{
//            this.addMessage(name + "设备正常，在线矿机 " + data.length() + " 台");
            System.out.println(name + "设备正常，总矿机 " + data.length() + " 台");
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
//        System.out.println(time);
        return date;
    }

    public Boolean checkOffline(Date date){
        Date time = this.getLostTime(date);
        return time.getTime() >= 1000 * 60 * 6;
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
//        System.out.println("lostTime:" + lostTime);
        return result;
    }

    public void addMessage(String mes){
        this.ms += mes;
    }

    public void sendMessage(){
        System.out.println(this.ms);
        this.sendQQMessage(this.ms);
        this.ms = "";
    }

    public void sendQQMessage(String ms){
        if(ms.length() > 0){
            try{
                URL url = new URL("http://127.0.0.1:8188/send/group/托管群/" + ms);
                URLConnection conn = (URLConnection)url.openConnection();
                var reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
                reader.close();
            }catch (Exception e){
                System.out.println("QQ消息发送失败");
            }
        }
    }

}
