public class HashCalc {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java HashCalc <value> [<value>...]");
            return;
        }
        for (String value : args) {
            int hash = value.hashCode();
            long abs = Math.abs((long) hash);
            System.out.println(value + "\t" + hash + "\t" + abs);
        }
    }
}
