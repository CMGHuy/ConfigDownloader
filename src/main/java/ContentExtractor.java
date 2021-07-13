import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ContentExtractor {

    public void callWebService(String requestFilePath, String responseFileLocation) throws IOException {
        String requestURL = "https://dev.cheetah.dlh.red-reply.net/siebel/app/eai/enu?SWEExtSource=SecureWebService&SWEExtCmd=Execute&WSSOAP=1";
        File soapRequestFile = new File(requestFilePath);

        // SOAP Post request
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost postRequest = new HttpPost(requestURL);

        // Request parameters and other properties
        postRequest.addHeader("Content-Type", "\"text/xml; charset=UTF-8\""); //adding header
        postRequest.addHeader("SOAPAction", "\"document/http://siebel.com/CustomUI:AutomationExecConfigAttachmentQueryByExample\"");
        postRequest.setEntity(new InputStreamEntity(new FileInputStream(soapRequestFile)));

        CloseableHttpResponse postResponse = httpClient.execute(postRequest);

        // Extract response message
        int statusCode = postResponse.getStatusLine().getStatusCode();
        System.out.println("Status code: " + statusCode);

        String responseMessage = EntityUtils.toString(postResponse.getEntity(), "UTF-8");
        Files.write(Paths.get(responseFileLocation), responseMessage.getBytes(StandardCharsets.UTF_8));
    }

    public Map<String, String> getXMLAttribute(String responseFilePath, String masterSuiteName) throws ParserConfigurationException, IOException, SAXException, NullPointerException {
        File xmlFile = new File(responseFilePath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = factory.newDocumentBuilder();
        Document xml = dBuilder.parse(xmlFile);
        xml.getDocumentElement().normalize();

        Map<String, String> outputMap = new HashMap();

        NodeList listOfAutomationExecConfig = xml.getDocumentElement().getElementsByTagName("AutomationExecConfig");
        NodeList automationExeConfigContent = null;
        for (int i = 0; i <= listOfAutomationExecConfig.getLength() - 1; ++i) {
            automationExeConfigContent = listOfAutomationExecConfig.item(i).getChildNodes();
            // Select the AutomationExecConfig with the intended master suite name
            if (automationExeConfigContent.item(8).getTextContent().equalsIgnoreCase(masterSuiteName)
            &&  automationExeConfigContent.item(18).getTextContent().equalsIgnoreCase("Hold")) {
                break;
            }
        }

        // Node masterSuiteName
        Node listOfAttachmentNodeList = automationExeConfigContent.item(55);
        // List of Node AutomationExecConfigurationAttachment
        NodeList attachmentNodeList = listOfAttachmentNodeList.getChildNodes();

        for (int k = 0; k <= attachmentNodeList.getLength() - 1; ++k) {
            if (attachmentNodeList.item(k).getChildNodes().item(8).getTextContent().equalsIgnoreCase("batchconfig")) {
                String batchConfigContent = attachmentNodeList.item(k).getChildNodes().item(12).getTextContent();
                outputMap.put("batchconfig", batchConfigContent);
                break;
            }
        }

        for (int k = 0; k <= attachmentNodeList.getLength() - 1; ++k) {
            if (attachmentNodeList.item(k).getChildNodes().item(8).getTextContent().equalsIgnoreCase(masterSuiteName)) {
                String batchScriptContent = attachmentNodeList.item(k).getChildNodes().item(12).getTextContent();
                outputMap.put("batchScript", batchScriptContent);
                return outputMap;
            }
        }

        return outputMap;
    }

    public void decoderPlusWriter(String encodedText, String encodedFileLocation, String encoding) throws IOException {
        byte[] contentInBytes = Base64.getMimeDecoder().decode(encodedText);
        String decodedMaster = new String(contentInBytes, encoding);
        File fileDirBatch = new File(encodedFileLocation);
        Writer pw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDirBatch), encoding));
        if (encoding.contains("UTF16_LE")) {
            System.out.println(encoding);
            pw.write(65279);
        }

        pw.append(decodedMaster);
        pw.flush();
    }

    public void convertAndWrite(String responseFilePath, String batchConfigPath, String masterSuitePath, String masterSuiteName) throws IOException, SAXException, ParserConfigurationException {
        Map<String, String> resultMap = getXMLAttribute(responseFilePath, masterSuiteName);
        String encodedText_masterSuite = resultMap.get("batchScript");
        String encodedText_batchConfig = resultMap.get("batchconfig");
        decoderPlusWriter(encodedText_batchConfig, batchConfigPath, "UTF-8");
        decoderPlusWriter(encodedText_masterSuite, masterSuitePath, "UTF-16LE");
    }
}