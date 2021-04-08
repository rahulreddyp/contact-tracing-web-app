public class ContactTracingMain {
    public static void main(String[] args) {
        Government gov = new Government("C:\\Users\\rahulreddy\\Documents\\MACS Winter 2021\\SDC- CSCI 3901\\contact-tracing-app\\src\\DBConfig.txt");
//        Government gov = new Government("..\\configs.properties");

        MobileDevice mDevice = new MobileDevice("C:\\Users\\rahulreddy\\Documents\\MACS Winter 2021\\SDC- CSCI 3901\\contact-tracing-app\\src\\config.txt", gov);
        mDevice.recordContact("h2jda2", 12, 20);
        mDevice.synchronizeData();

    }

}
