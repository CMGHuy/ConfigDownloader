public class ConfigDownloader {

    public static void main(String[] args) throws Exception {
        String postRequestPath = args[0];
        String postResponsePath = args[1];
        String batchConfigPath = args[2];
        String masterSuitePath = args[3];
        String masterSuiteName = args[4];

//        String postRequestPath = "C:\\Users\\h.cao\\Desktop\\Intellij\\ConfigDownloader\\src\\main\\java\\test.xml";
//        String postResponsePath = "C:\\Users\\h.cao\\Desktop\\output.xml";
//        String batchConfigPath = "C:\\Users\\h.cao\\Desktop\\batchconfig.xml";
//        String masterSuitePath = "C:\\Users\\h.cao\\Desktop\\mastersuite.csv";
//        String masterSuiteName = "C2CTestSuiteINT";

        ContentExtractor contentExtractor = new ContentExtractor();
        contentExtractor.callWebService(postRequestPath, postResponsePath);
        contentExtractor.getXMLAttribute(postResponsePath, masterSuiteName);
        contentExtractor.convertAndWrite(postResponsePath, batchConfigPath, masterSuitePath, masterSuiteName);
    }
}
