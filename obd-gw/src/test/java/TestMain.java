import com.diyiliu.plugin.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.util.Calendar;

/**
 * Description: TestMain
 * Author: DIYILIU
 * Update: 2018-06-25 15:41
 */
public class TestMain {

    @Test
    public void test() {

        System.out.println(String.format("%02X", 9));
    }

    @Test
    public void test1() {
        String str = "00000C0751AB";

        str = CommonUtil.parseSIM(CommonUtil.hexStringToBytes(str));

        System.out.println(str);
    }

    @Test
    public void test2() {

        long l = 2147483650l;
        l = formatLatLng(l);
        System.out.println(l);
    }


    private long formatLatLng(long l) {
        long i = l >> 31;

        return (l & 0x7FFFFFFF) * (i == 0 ? 1 : -1);
    }

    @Test
    public void test3() {

        double d = 1.5;

        System.out.println(String.format("%.0f", d));
    }


    @Test
    public void test4(){
        String str = "47 45 54 20 2F 63 6F 6E 66 20 48 54 54 50 2F 31 2E 31 0D 0A 48 6F 73 74 3A 20 32 31 38 2E 33 2E 32 34 37 2E 32 32 37 3A 38 30 38 38 0D 0A 55 73 65 72 2D 41 67 65 6E 74 3A 20 4D 6F 7A 69 6C 6C 61 2F 35 2E 30 20 28 57 69 6E 64 6F 77 73 20 4E 54 20 36 2E 31 29 20 41 70 70 6C 65 57 65 62 4B 69 74 2F 35 33 37 2E 33 36 20 28 4B 48 54 4D 4C 2C 20 6C 69 6B 65 20 47 65 63 6B 6F 29 20 43 68 72 6F 6D 65 2F 34 31 2E 30 2E 32 32 32 38 2E 30 20 53 61 66 61 72 69 2F 35 33 37 2E 33 36 0D 0A 43 6F 6E 6E 65 63 74 69 6F 6E 3A 20 63 6C 6F 73 65 0D 0A 41 63 63 65 70 74 2D 45 6E 63 6F 64 69 6E 67 3A 20 67 7A 69 70 0D 0A 43 6F 6E 6E 65 63 74 69 6F 6E 3A 20 63 6C 6F 73 65 0D 0A 0D 0A";

        System.out.println(new String(CommonUtil.hexStringToBytes(str.replaceAll(" ", ""))));
    }

    @Test
    public void test6() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes("1".getBytes());
        buf.writeByte(0x2C);
        buf.writeBytes("0".getBytes());
        buf.writeByte(0x2C);
        buf.writeBytes("218.3.247.227".getBytes());
        buf.writeByte(0x2C);
        buf.writeBytes("5005".getBytes());
        buf.writeByte(0x2C);
        buf.writeBytes("0.0.0.0".getBytes());
        buf.writeByte(0x2C);
        buf.writeBytes("cmnet".getBytes());
        buf.writeByte(0x2C);
        buf.writeByte(0x2C);
        buf.writeByte(0x2C);

        byte[] content = new byte[buf.writerIndex()];
        buf.readBytes(content);

        byte[] raw = CommonUtil.buildRaw(new byte[]{(byte) 0xFA, (byte) 0xFA}, new byte[]{(byte) 0xFB}, content, true);


        System.out.println(new String(raw));

        System.out.println(CommonUtil.bytesToStr(raw));
    }

    @Test
    public void test7() {

        Calendar calendar = Calendar.getInstance();

        System.out.println(calendar.get(Calendar.MONTH));
    }


    @Test
    public void test8(){

        String str = "0002000AB91915B90020023431120719057F007F00497F007F010D7F007F01277F007F02007F007F060A";

        byte[] bytes = CommonUtil.hexStringToBytes(str);

        System.out.println(String.format("%02X", CommonUtil.getCheck(bytes)));


    }
}
