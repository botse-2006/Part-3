package com.example.chatapp;

import org.junit.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.Assert.*;

public class ChatApp1Test {

    private ChatApp1.Login loginInstance;

    @Before
    public void setup() throws IOException {
        loginInstance = new ChatApp1.Login();
        ChatApp1.Login.users.clear();

        Files.deleteIfExists(Paths.get("messages.json"));
        Files.deleteIfExists(Paths.get("stored_messages.json"));

        resetMemoryArrays();
        executeAssignmentSystemSeedVariables();
    }

    private void resetMemoryArrays() {
        try {
            var sentCounter = ChatApp1.Message.class.getDeclaredField("sentCounter");
            var sentMessages = ChatApp1.Message.class.getDeclaredField("sentMessages");
            var disregardedMessages = ChatApp1.Message.class.getDeclaredField("disregardedMessages");
            var storedMessages = ChatApp1.Message.class.getDeclaredField("storedMessages");
            var messageHashes = ChatApp1.Message.class.getDeclaredField("messageHashes");
            var messageIDs = ChatApp1.Message.class.getDeclaredField("messageIDs");

            sentCounter.setAccessible(true);
            sentMessages.setAccessible(true);
            disregardedMessages.setAccessible(true);
            storedMessages.setAccessible(true);
            messageHashes.setAccessible(true);
            messageIDs.setAccessible(true);

            sentCounter.setInt(null, 0);
            ((List<?>) sentMessages.get(null)).clear();
            ((List<?>) disregardedMessages.get(null)).clear();
            ((List<?>) storedMessages.get(null)).clear();
            ((List<?>) messageHashes.get(null)).clear();
            ((List<?>) messageIDs.get(null)).clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void executeAssignmentSystemSeedVariables() {
        // Test Case 1 -> Sent
        ChatApp1.Message m1 = new ChatApp1.Message("0012345678", "+27834557896", "Did you get the cake?", 0);
        m1.sentMessage(1);

        // Test Case 2 -> Stored
        ChatApp1.Message m2 = new ChatApp1.Message("0023456789", "+27838884567", "Where are you? You are late! I have asked you to be on time.", 1);
        m2.sentMessage(2);

        // Test Case 3 -> Disregard
        ChatApp1.Message m3 = new ChatApp1.Message("0034567890", "+27834484567", "Yohoooo, I am at your gate.", 2);
        m3.sentMessage(3);

        // Test Case 4 -> Sent
        ChatApp1.Message m4 = new ChatApp1.Message("0045678901", "+27838884567", "It is dinner time !", 1);
        m4.sentMessage(1);

        // Test Case 5 -> Stored
        ChatApp1.Message m5 = new ChatApp1.Message("0056789012", "+27838884567", "Ok, I am leaving without you.", 3);
        m5.sentMessage(2);
    }

    @Test
    public void testUsernameValidationFormattingRules() {
        assertTrue(loginInstance.checkUsername("kyl_1"));
        assertFalse(loginInstance.checkUsername("kyle!!!!!!!"));
    }

    @Test
    public void testPasswordComplexityRules() {
        assertTrue(loginInstance.checkPasswordComplexity("Ch&&sec@ke99!"));
        assertFalse(loginInstance.checkPasswordComplexity("password"));
    }

    @Test
    public void testSouthAfricanCellphoneFormats() {
        assertTrue(loginInstance.checkCellphone("+27838968976"));
        // Asserting that local 10-digit number structure fails as requested
        assertFalse(loginInstance.checkCellphone("0838884567"));
    }

    @Test
    public void testAutoCalculatedHashFormatting() {
        ChatApp1.Message targetObj = new ChatApp1.Message("0012345678", "+27718693002", "Hi Mike, can you join us for dinner tonight?", 0);
        // Corrected to output exactly "00:0:HITONIGHT"
        assertEquals("00:0:HITONIGHT", targetObj.createMessageHash());
    }

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        List<ChatApp1.Message> sessionSentList = ChatApp1.Message.getSentMessages();
        assertEquals(2, sessionSentList.size());
        assertEquals("Did you get the cake?", sessionSentList.get(0).getMessageText());
        assertEquals("It is dinner time !", sessionSentList.get(1).getMessageText());
    }

    @Test
    public void testDisplayTheLongestMessage() {
        List<Map<String, String>> dataList = ChatApp1.Message.readJsonListFromFile("stored_messages.json");
        assertFalse(dataList.isEmpty());

        Map<String, String> longestRecord = dataList.get(0);
        for (Map<String, String> element : dataList) {
            if (element.get("Message").length() > longestRecord.get("Message").length()) {
                longestRecord = element;
            }
        }
        assertEquals("Where are you? You are late! I have asked you to be on time.", longestRecord.get("Message"));
    }

    @Test
    public void testSearchForMessageID() {
        List<Map<String, String>> combinedCollection = ChatApp1.Message.readJsonListFromFile("messages.json");
        String testSearchRes = ChatApp1.searchMessageByID("0045678901", combinedCollection);
        
        assertTrue(testSearchRes.contains("+27838884567"));
        assertTrue(testSearchRes.contains("It is dinner time !"));
    }

    @Test
    public void testSearchAllMessagesRegardingParticularRecipient() {
        List<Map<String, String>> searchCollection = ChatApp1.Message.readJsonListFromFile("stored_messages.json");
        List<String> collectedTextPayloads = new ArrayList<>();

        String queryCell = "+27838884567";
        for (Map<String, String> dataNode : searchCollection) {
            String cleanRecipient = dataNode.get("Recipient").replace("\u202a", "").replace("\u202c", "").trim();
            if (cleanRecipient.equals(queryCell)) {
                collectedTextPayloads.add(dataNode.get("Message"));
            }
        }

        assertEquals(2, collectedTextPayloads.size());
        assertTrue(collectedTextPayloads.contains("Where are you? You are late! I have asked you to be on time."));
        assertTrue(collectedTextPayloads.contains("Ok, I am leaving without you."));
    }

    @Test
    public void testDeleteMessageUsingMessageHash() {
        List<Map<String, String>> originalCollection = ChatApp1.Message.readJsonListFromFile("stored_messages.json");
        String targetedHashNodeCode = null;

        for (Map<String, String> msg : originalCollection) {
            if (msg.get("Message").startsWith("Where are you?")) {
                targetedHashNodeCode = msg.get("MessageHash");
                break;
            }
        }
        assertNotNull(targetedHashNodeCode);

        boolean flagResult = false;
        Iterator<Map<String, String>> iterator = originalCollection.iterator();
        while (iterator.hasNext()) {
            Map<String, String> node = iterator.next();
            if (node.get("MessageHash").equals(targetedHashNodeCode)) {
                iterator.remove();
                flagResult = true;
                break;
            }
        }

        assertTrue(flagResult);
    }

    @Test
    public void testDisplayReport() {
        List<Map<String, String>> primaryArray = ChatApp1.Message.readJsonListFromFile("messages.json");
        StringBuilder stringBuilder = new StringBuilder();

        for (Map<String, String> map : primaryArray) {
            stringBuilder.append(map.get("MessageHash")).append(" ")
                         .append(map.get("Recipient")).append(" ")
                         .append(map.get("Message")).append("\n");
        }

        String finalReportDump = stringBuilder.toString();
        assertTrue(finalReportDump.contains("Did you get the cake?"));
        assertTrue(finalReportDump.contains("It is dinner time !"));
    }

    @After
    public void cleanup() throws IOException {
        Files.deleteIfExists(Paths.get("messages.json"));
        Files.deleteIfExists(Paths.get("stored_messages.json"));
    }
}