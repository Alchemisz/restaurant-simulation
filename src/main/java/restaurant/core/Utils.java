package restaurant.core;

public class Utils {

    private static int scale = 100;

    public static long convertToUnit(long value) {
        return value * scale;
    }

    synchronized public static void changeScale(int newValue) {
        if(newValue <= 0) {
            return;
        }

        scale = newValue;
    }
}
